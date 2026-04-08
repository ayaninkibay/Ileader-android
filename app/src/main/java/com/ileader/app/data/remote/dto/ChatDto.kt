package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    val id: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // JOIN: participants with profile
    @SerialName("conversation_participants")
    val participants: List<ConversationParticipantDto>? = null,
    // JOIN: latest message (optional)
    val messages: List<MessageDto>? = null
)

@Serializable
data class ConversationParticipantDto(
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("last_read_at") val lastReadAt: String? = null,
    val profiles: ProfileMinimalDto? = null
)

@Serializable
data class MessageDto(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    val content: String,
    @SerialName("created_at") val createdAt: String? = null,
    val profiles: ProfileMinimalDto? = null
)

@Serializable
data class MessageInsertDto(
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    val content: String
)

@Serializable
data class ConversationInsertDto(
    @SerialName("created_by") val createdBy: String? = null
)

@Serializable
data class ConversationParticipantInsertDto(
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("user_id") val userId: String
)
