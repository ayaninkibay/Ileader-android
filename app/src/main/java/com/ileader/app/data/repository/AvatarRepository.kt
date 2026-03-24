package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class AvatarRepository {
    private val client = SupabaseModule.client

    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): String {
        val bucket = client.storage.from("avatars")
        bucket.upload("$userId/avatar.jpg", imageBytes) { upsert = true }
        val url = bucket.publicUrl("$userId/avatar.jpg")
        val urlWithCacheBust = "$url?t=${System.currentTimeMillis()}"
        client.from("profiles")
            .update(mapOf("avatar_url" to urlWithCacheBust)) {
                filter { eq("id", userId) }
            }
        return urlWithCacheBust
    }
}
