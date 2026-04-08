package com.ileader.app.data.util

suspend fun <T> safeApiCall(
    method: String,
    block: suspend () -> T
): T {
    return try {
        block()
    } catch (e: Exception) {
        AppLogger.network(method, e)
        throw e
    }
}

/**
 * Like [safeApiCall] but returns [default] instead of throwing.
 * Logs the error so silent failures are still observable.
 */
suspend fun <T> safeApiCallOrDefault(
    method: String,
    default: T,
    block: suspend () -> T
): T {
    return try {
        block()
    } catch (e: Exception) {
        AppLogger.network(method, e)
        default
    }
}
