package com.ileader.app.data.util

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Process-wide TTL cache with in-flight request deduplication.
 *
 * Two purposes:
 *  1. Serve fresh-enough data from memory instead of hitting Supabase.
 *     Cuts read load and round-trip latency when the same data is
 *     requested repeatedly in a short window (e.g., user navigating
 *     back to Home, two screens showing the same sports list, etc).
 *
 *  2. Deduplicate concurrent in-flight requests for the same key.
 *     If VM A and VM B both ask for key "sports" at the same moment
 *     and the cache is cold, only one HTTP call is made and both
 *     coroutines suspend on it.
 *
 * Not persisted across process death — for that use Room.
 *
 * Tombstone semantics: invalidate()/invalidateMatching()/clear() mark any
 * in-flight loader so its result is NOT written to the entries map after
 * completion. Existing joiners still receive the value (they need to make
 * forward progress), but future readers won't see stale data. This makes
 * write-through correctness safe across coroutine races — without it, a
 * loader started before a write could clobber the just-invalidated entry.
 */
object MemoryCache {

    private data class Entry<T>(val value: T, val expiresAtMs: Long)

    private val entries = mutableMapOf<String, Entry<*>>()
    private val inFlight = mutableMapOf<String, CompletableDeferred<*>>()
    // Deferreds whose result must NOT land in [entries] when they finish —
    // they were tombstoned by a concurrent invalidate()/clear(). Joiners still
    // get the value, but the cache stays clean. Loader removes itself on exit.
    private val tombstoned = HashSet<CompletableDeferred<*>>()
    private val mutex = Mutex()

    suspend fun <T : Any> cached(
        key: String,
        ttlMs: Long,
        loader: suspend () -> T
    ): T {
        val now = System.currentTimeMillis()

        // 1. Fast path: fresh value in cache.
        @Suppress("UNCHECKED_CAST")
        val fresh: Entry<T>? = mutex.withLock {
            (entries[key] as? Entry<T>)?.takeIf { it.expiresAtMs > now }
        }
        if (fresh != null) return fresh.value

        // 2. Join an existing in-flight request if one is already running.
        //    Otherwise register ourselves as the in-flight request.
        val ours = CompletableDeferred<T>()
        @Suppress("UNCHECKED_CAST")
        val other: Deferred<T>? = mutex.withLock {
            val existing = inFlight[key] as? Deferred<T>
            if (existing != null) {
                existing
            } else {
                inFlight[key] = ours
                null
            }
        }
        if (other != null) return other.await()

        // 3. We own the load. Run it, publish result, clean up.
        return try {
            val value = loader()
            mutex.withLock {
                // Skip commit if invalidate()/clear() hit us while loading.
                if (!tombstoned.remove(ours)) {
                    entries[key] = Entry(value, now + ttlMs)
                }
                // Only remove if our deferred is still the registered one —
                // invalidate() may already have pulled it out.
                if (inFlight[key] === ours) inFlight.remove(key)
            }
            ours.complete(value)
            value
        } catch (t: Throwable) {
            mutex.withLock {
                tombstoned.remove(ours)
                if (inFlight[key] === ours) inFlight.remove(key)
            }
            ours.completeExceptionally(t)
            throw t
        }
    }

    /** Drop a specific cache entry. Next read re-fetches from source. */
    suspend fun invalidate(key: String) {
        mutex.withLock {
            entries.remove(key)
            // Tombstone the in-flight loader (if any) so its result won't be
            // committed when it returns. Future callers start a fresh load.
            inFlight.remove(key)?.let { tombstoned.add(it) }
        }
    }

    /** Drop every entry whose key starts with [prefix]. */
    suspend fun invalidateMatching(prefix: String) {
        mutex.withLock {
            entries.keys.filter { it.startsWith(prefix) }.toList()
                .forEach { entries.remove(it) }
            inFlight.keys.filter { it.startsWith(prefix) }.toList()
                .forEach { k -> inFlight.remove(k)?.let { tombstoned.add(it) } }
        }
    }

    /** Wipe everything (e.g., on sign-out). */
    suspend fun clear() {
        mutex.withLock {
            entries.clear()
            // Tombstone every in-flight loader so post-signOut completions
            // don't repopulate the cache with the previous user's data.
            inFlight.values.forEach { tombstoned.add(it) }
            inFlight.clear()
        }
    }

    /** Snapshot of cache size — for debugging / metrics. */
    suspend fun size(): Int = mutex.withLock { entries.size }
}
