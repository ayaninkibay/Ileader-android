package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class NotificationDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val type: String? = null,
    val title: String? = null,
    val message: String? = null,
    val data: JsonElement? = null,
    val read: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)
