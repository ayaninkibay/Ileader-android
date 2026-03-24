package com.ileader.app.data.notifications

import android.util.Log
import com.ileader.app.data.remote.SupabaseModule
import io.github.jan.supabase.postgrest.from

/**
 * Управляет сохранением FCM токена в Supabase.
 * Вызывается при логине пользователя.
 *
 * TODO: Активировать после подключения Firebase:
 * - import com.google.firebase.messaging.FirebaseMessaging
 * - Раскомментировать getAndSaveToken()
 */
object FcmTokenManager {

    /**
     * Получает текущий FCM token и сохраняет в профиль пользователя.
     * Вызвать после успешного логина.
     */
    suspend fun saveTokenForUser(userId: String) {
        // TODO: Раскомментировать после подключения Firebase
        // try {
        //     FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
        //         CoroutineScope(Dispatchers.IO).launch {
        //             try {
        //                 SupabaseModule.client.from("profiles")
        //                     .update(mapOf("fcm_token" to token)) {
        //                         filter { eq("id", userId) }
        //                     }
        //                 Log.d("FCM", "Token saved for user $userId")
        //             } catch (e: Exception) {
        //                 Log.e("FCM", "Failed to save token", e)
        //             }
        //         }
        //     }
        // } catch (e: Exception) {
        //     Log.e("FCM", "Firebase not initialized", e)
        // }
    }

    /**
     * Удаляет FCM token из профиля при логауте.
     */
    suspend fun clearTokenForUser(userId: String) {
        try {
            SupabaseModule.client.from("profiles")
                .update(mapOf("fcm_token" to null)) {
                    filter { eq("id", userId) }
                }
        } catch (e: Exception) {
            Log.e("FCM", "Failed to clear token", e)
        }
    }
}
