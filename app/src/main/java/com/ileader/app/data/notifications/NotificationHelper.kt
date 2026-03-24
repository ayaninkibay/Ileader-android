package com.ileader.app.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ileader.app.MainActivity
import com.ileader.app.R

/**
 * Управляет каналами и показом Android-уведомлений.
 * Типы: турнирные, командные, сообщения, системные.
 */
object NotificationHelper {

    // Каналы уведомлений
    private const val CHANNEL_TOURNAMENTS = "tournaments"
    private const val CHANNEL_TEAMS = "teams"
    private const val CHANNEL_MESSAGES = "messages"
    private const val CHANNEL_SYSTEM = "system"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_TOURNAMENTS,
                    "Турниры",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Регистрации, результаты, обновления турниров" },

                NotificationChannel(
                    CHANNEL_TEAMS,
                    "Команды",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Заявки и приглашения в команды" },

                NotificationChannel(
                    CHANNEL_MESSAGES,
                    "Сообщения",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Новые сообщения в чате" },

                NotificationChannel(
                    CHANNEL_SYSTEM,
                    "Системные",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Системные уведомления и обновления" }
            )

            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        type: String? = null,
        data: Map<String, String> = emptyMap()
    ) {
        val channelId = when (type) {
            "tournament_registration", "tournament_update", "tournament_result" -> CHANNEL_TOURNAMENTS
            "team_invite", "team_update" -> CHANNEL_TEAMS
            "message" -> CHANNEL_MESSAGES
            else -> CHANNEL_SYSTEM
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Передаём данные для deep link навигации
            data.forEach { (key, value) -> putExtra(key, value) }
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (channelId == CHANNEL_MESSAGES || channelId == CHANNEL_TOURNAMENTS)
                    NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
