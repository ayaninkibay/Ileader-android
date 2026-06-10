package com.ileader.app.data.notifications

import com.google.firebase.messaging.FirebaseMessaging
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.util.AppLogger
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Получает FCM token и сохраняет в profiles.fcm_token.
 * Вызывается после успешного логина/регистрации.
 */
object FcmTokenManager {

    private val scope = CoroutineScope(Dispatchers.IO)

    fun saveTokenForUser(userId: String) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                scope.launch {
                    try {
                        SupabaseModule.client.from("profiles")
                            .update(mapOf("fcm_token" to token)) {
                                filter { eq("id", userId) }
                            }
                        AppLogger.d("FCM token saved")
                    } catch (e: Exception) {
                        AppLogger.e("FCM token save failed", e)
                    }
                }
            }
            .addOnFailureListener { e ->
                AppLogger.e("FCM token fetch failed", e)
            }
    }

    suspend fun clearTokenForUser(userId: String) {
        try {
            SupabaseModule.client.from("profiles")
                .update(mapOf("fcm_token" to null)) {
                    filter { eq("id", userId) }
                }
        } catch (e: Exception) {
            AppLogger.e("FCM token clear failed", e)
        }
    }
}
