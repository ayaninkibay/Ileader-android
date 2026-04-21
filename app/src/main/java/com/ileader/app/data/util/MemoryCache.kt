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
 * Usage:
 *   val articles = MemoryCache.cached("articles:recent:10", 120_000L) {
 *       client.from("articles").select(...).decodeList<ArticleDto>()
 *   }
 *
 *   // When a write happens, invalidate the key so next reader re-fetches:
 *   MemoryCache.invalidate("articles:recent:10")
 *   MemoryCache.invalidateMatching("articles:") // clears all article-related keys
 */
object MemoryCache {

    private data class Entry<T>(val value: T, val expiresAtMs: Long)

    private val entries = mutableMapOf<String, Entry<*>>()
    private val inFlight = mutableMapOf<String, CompletableDeferred<*>>()
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
                entries[key] = Entry(value, now + ttlMs)
                inFlight.remove(key)
            }
            ours.complete(value)
            value
        } catch (t: Throwable) {
            mutex.withLock { inFlight.remove(key) }
            ours.completeExceptionally(t)
            throw t
        }
    }

    /** Drop a specific cache entry. Next read re-fetches from source. */
    suspend fun invalidate(key: String) {
        mutex.withLock { entries.remove(key) }
    }

    /** Drop every entry whose key starts with [prefix]. */
    suspend fun invalidateMatching(prefix: String) {
        mutex.withLock {
            val keysToRemove = entries.keys.filter { it.startsWith(prefix) }
            keysToRemove.forEach { entries.remove(it) }
        }
    }

    /** Wipe everything (e.g., on sign-out). */
    suspend fun clear() {
        mutex.withLock { entries.clear() }
    }

    /** Snapshot of cache size — for debugging / metrics. */
    suspend fun size(): Int = mutex.withLock { entries.size }
}
