package com.ileader.app.data.notifications

// TODO: Раскомментировать после подключения Firebase (google-services.json + deps)
//
// import com.google.firebase.messaging.FirebaseMessagingService
// import com.google.firebase.messaging.RemoteMessage
// import android.util.Log
// import com.ileader.app.data.remote.SupabaseModule
// import io.github.jan.supabase.auth.auth
// import io.github.jan.supabase.postgrest.from
// import kotlinx.coroutines.CoroutineScope
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.launch
//
// /**
//  * Firebase Cloud Messaging Service для приёма push-уведомлений.
//  *
//  * Обрабатывает:
//  * - Входящие push-уведомления → показ через NotificationHelper
//  * - Обновление FCM token → сохранение в Supabase profiles
//  */
// class ILeaderMessagingService : FirebaseMessagingService() {
//
//     private val scope = CoroutineScope(Dispatchers.IO)
//
//     override fun onMessageReceived(remoteMessage: RemoteMessage) {
//         super.onMessageReceived(remoteMessage)
//
//         val title = remoteMessage.notification?.title
//             ?: remoteMessage.data["title"]
//             ?: "iLeader"
//         val body = remoteMessage.notification?.body
//             ?: remoteMessage.data["message"]
//             ?: ""
//         val type = remoteMessage.data["type"]
//
//         NotificationHelper.showNotification(
//             context = this,
//             title = title,
//             body = body,
//             type = type,
//             data = remoteMessage.data
//         )
//     }
//
//     override fun onNewToken(token: String) {
//         super.onNewToken(token)
//         Log.d("FCM", "New token: $token")
//         saveFcmTokenToSupabase(token)
//     }
//
//     private fun saveFcmTokenToSupabase(token: String) {
//         scope.launch {
//             try {
//                 val userId = SupabaseModule.client.auth.currentUserOrNull()?.id ?: return@launch
//                 SupabaseModule.client.from("profiles")
//                     .update(mapOf("fcm_token" to token)) {
//                         filter { eq("id", userId) }
//                     }
//             } catch (e: Exception) {
//                 Log.e("FCM", "Failed to save token", e)
//             }
//         }
//     }
// }

/**
 * Заглушка: FCM-сервис будет активирован после настройки Firebase.
 *
 * Шаги для активации:
 * 1. Создать Firebase проект: https://console.firebase.google.com
 * 2. Добавить Android-приложение (package: com.ileader.app)
 * 3. Скачать google-services.json → положить в app/
 * 4. Раскомментировать google-services plugin в build.gradle.kts (оба файла)
 * 5. Раскомментировать Firebase deps в app/build.gradle.kts
 * 6. Раскомментировать код выше и зарегистрировать сервис в AndroidManifest.xml:
 *
 *    <service
 *        android:name=".data.notifications.ILeaderMessagingService"
 *        android:exported="false">
 *        <intent-filter>
 *            <action android:name="com.google.firebase.MESSAGING_EVENT" />
 *        </intent-filter>
 *    </service>
 *
 * 7. Добавить колонку fcm_token в таблицу profiles (Supabase):
 *    ALTER TABLE profiles ADD COLUMN fcm_token TEXT;
 *
 * 8. Создать Edge Function для отправки push через Firebase Admin SDK:
 *    - При вставке в notifications → отправка push на fcm_token пользователя
 */
object FcmSetupInstructions {
    const val STATUS = "NOT_CONFIGURED"
}
