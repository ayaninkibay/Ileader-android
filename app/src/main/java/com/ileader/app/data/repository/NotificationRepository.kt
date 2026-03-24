package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.NotificationDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class NotificationRepository {
    private val client = SupabaseModule.client

    suspend fun getNotifications(userId: String): List<NotificationDto> {
        return client.from("notifications")
            .select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
                limit(50)
            }
            .decodeList()
    }

    suspend fun getUnreadCount(userId: String): Int {
        return client.from("notifications")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("read", false)
                }
            }
            .decodeList<NotificationDto>()
            .size
    }

    suspend fun markAsRead(notificationId: String) {
        client.from("notifications")
            .update(mapOf("read" to true)) {
                filter { eq("id", notificationId) }
            }
    }

    suspend fun markAllAsRead(userId: String) {
        client.from("notifications")
            .update(mapOf("read" to true)) {
                filter {
                    eq("user_id", userId)
                    eq("read", false)
                }
            }
    }

    suspend fun deleteNotification(notificationId: String) {
        client.from("notifications")
            .delete {
                filter { eq("id", notificationId) }
            }
    }
}
