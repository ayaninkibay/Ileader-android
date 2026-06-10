package com.ileader.app.data.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.util.AppLogger
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ILeaderMessagingService : FirebaseMessagingService() {

    // SupervisorJob: одна сбоящая корутина не валит соседние.
    // cancel() в onDestroy: иначе scope переживает сервис, корутины висят
    // в фоне держа ссылки на сервис и Supabase-клиент.
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "iLeader"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: ""
        val type = remoteMessage.data["type"]

        NotificationHelper.showNotification(
            context = this,
            title = title,
            body = body,
            type = type,
            data = remoteMessage.data
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        AppLogger.d("FCM new token received")
        saveFcmTokenToSupabase(token)
    }

    private fun saveFcmTokenToSupabase(token: String) {
        scope.launch {
            try {
                val userId = SupabaseModule.client.auth.currentUserOrNull()?.id ?: return@launch
                SupabaseModule.client.from("profiles")
                    .update(mapOf("fcm_token" to token)) {
                        filter { eq("id", userId) }
                    }
            } catch (e: Exception) {
                AppLogger.e("FCM token save failed", e)
            }
        }
    }
}
