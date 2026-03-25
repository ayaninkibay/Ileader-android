package com.ileader.app.data.util

import android.util.Log

object AppLogger {
    private const val TAG = "iLeader"

    fun d(message: String) = Log.d(TAG, message)
    fun i(message: String) = Log.i(TAG, message)
    fun w(message: String, t: Throwable? = null) = Log.w(TAG, message, t)
    fun e(message: String, t: Throwable? = null) = Log.e(TAG, message, t)

    fun network(method: String, error: Throwable) {
        Log.e(TAG, "[$method] Network error: ${error.message}", error)
    }
}
