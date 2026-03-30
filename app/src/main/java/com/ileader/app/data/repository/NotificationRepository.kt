package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.NotificationDto
import com.ileader.app.data.util.safeApiCall
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class NotificationRepository {
    private val client = SupabaseModule.client

    suspend fun getNotifications(userId: String): List<NotificationDto> = safeApiCall("NotificationRepo.getNotifications") {
        client.from("notifications")
            .select(Columns.raw("id, user_id, type, title, message, data, read, created_at")) {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
                limit(50)
            }
            .decodeList()
    }

    suspend fun getUnreadCount(userId: String): Int = safeApiCall("NotificationRepo.getUnreadCount") {
        client.from("notifications")
            .select(Columns.raw("id")) {
                filter {
                    eq("user_id", userId)
                    eq("read", false)
                }
            }
            .decodeList<NotificationDto>()
            .size
    }

    suspend fun markAsRead(notificationId: String) = safeApiCall("NotificationRepo.markAsRead") {
        client.from("notifications")
            .update(mapOf("read" to true)) {
                filter { eq("id", notificationId) }
            }
    }

    suspend fun markAllAsRead(userId: String) = safeApiCall("NotificationRepo.markAllAsRead") {
        client.from("notifications")
            .update(mapOf("read" to true)) {
                filter {
                    eq("user_id", userId)
                    eq("read", false)
                }
            }
    }

    suspend fun deleteNotification(notificationId: String) = safeApiCall("NotificationRepo.deleteNotification") {
        client.from("notifications")
            .delete {
                filter { eq("id", notificationId) }
            }
    }
}
