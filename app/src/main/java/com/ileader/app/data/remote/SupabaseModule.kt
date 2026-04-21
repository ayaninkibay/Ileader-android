@file:OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class)

package com.ileader.app.data.remote

import com.ileader.app.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout

object SupabaseModule {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)

        // Tighten the HTTP client so a slow/dead Supabase instance can't hold
        // sockets forever under load. Without these, Ktor's default is "no limit"
        // and one bad request blocks a coroutine indefinitely.
        httpConfig {
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }
            // Transient 5xx / network blips — one automatic retry with backoff.
            // Does NOT retry on 4xx (those are real errors we want surfaced).
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 2)
                retryOnExceptionIf(maxRetries = 2) { _, cause ->
                    cause is java.io.IOException
                }
                exponentialDelay(base = 2.0, maxDelayMs = 3_000)
            }
        }
    }
}
