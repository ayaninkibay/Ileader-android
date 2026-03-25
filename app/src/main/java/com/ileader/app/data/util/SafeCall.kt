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
