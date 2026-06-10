package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.NotificationDto
import com.ileader.app.data.util.MemoryCache
import com.ileader.app.data.util.safeApiCall
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
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

    /**
     * Hot path — read on every Home tab entry. Cached for 30s so rapid
     * navigation between tabs doesn't replay the request. Writes below
     * invalidate the key so badge count updates immediately after read.
     */
    suspend fun getUnreadCount(userId: String): Int =
        MemoryCache.cached("notifications:unread:$userId", ttlMs = 30_000L) {
            safeApiCall("NotificationRepo.getUnreadCount") {
                // head + count → Postgres returns the count in a header, no rows
                // are sent over the wire. Replaces the old "fetch ids and .size".
                val result = client.from("notifications")
                    .select(Columns.raw("id")) {
                        count(Count.EXACT)
                        filter {
                            eq("user_id", userId)
                            eq("read", false)
                        }
                    }
                (result.countOrNull() ?: 0L).toInt()
            }
        }

    suspend fun markAsRead(notificationId: String, userId: String? = null) = safeApiCall("NotificationRepo.markAsRead") {
        client.from("notifications")
            .update(mapOf("read" to true)) {
                filter { eq("id", notificationId) }
            }
        userId?.let { MemoryCache.invalidate("notifications:unread:$it") }
    }

    suspend fun markAllAsRead(userId: String) = safeApiCall("NotificationRepo.markAllAsRead") {
        client.from("notifications")
            .update(mapOf("read" to true)) {
                filter {
                    eq("user_id", userId)
                    eq("read", false)
                }
            }
        MemoryCache.invalidate("notifications:unread:$userId")
    }

    suspend fun deleteNotification(notificationId: String, userId: String? = null) = safeApiCall("NotificationRepo.deleteNotification") {
        client.from("notifications")
            .delete {
                filter { eq("id", notificationId) }
            }
        userId?.let { MemoryCache.invalidate("notifications:unread:$it") }
    }
}
