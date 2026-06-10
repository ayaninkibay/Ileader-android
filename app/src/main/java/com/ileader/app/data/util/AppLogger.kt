package com.ileader.app.data.util

import android.util.Log
import com.ileader.app.BuildConfig

/**
 * Single logging entry-point for the app.
 *
 * Debug/info levels are stripped in release builds — anything written there
 * could leak diagnostic info (user actions, IDs, error shapes) to logcat,
 * which is readable by attached debuggers. Warning/error levels stay live
 * because they're needed to diagnose crashes in the wild.
 *
 * Callsites must not include PII (emails, phone numbers, raw tokens) in
 * messages even at error level — pass the exception, not the user input.
 */
object AppLogger {
    private const val TAG = "iLeader"

    fun d(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    fun i(message: String) {
        if (BuildConfig.DEBUG) Log.i(TAG, message)
    }

    fun w(message: String, t: Throwable? = null) = Log.w(TAG, message, t)
    fun e(message: String, t: Throwable? = null) = Log.e(TAG, message, t)

    /**
     * Залогировать ошибку И показать алерт пользователю одной строкой.
     * Текст алерта — это то, что увидит юзер, поэтому он должен быть человеческим:
     * не «HTTP 500 from /tournaments», а «Не удалось загрузить турниры».
     */
    fun e(message: String, t: Throwable?, alert: String) {
        Log.e(TAG, message, t)
        Alerts.error(alert)
    }

    fun network(method: String, error: Throwable) {
        Log.e(TAG, "[$method] Network error: ${error.message}", error)
    }
}
