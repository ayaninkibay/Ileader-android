package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class ChatRepository {
    private val client = SupabaseModule.client

    /**
     * Get all conversations for a given user, including participants with profile info.
     */
    suspend fun getConversations(userId: String): List<ConversationDto> {
        // First query: conversation IDs the user participates in
        val myRows = client.from("conversation_participants")
            .select(Columns.raw("conversation_id")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<ConversationIdRow>()

        val ids = myRows.map { it.conversationId }.distinct()
        if (ids.isEmpty()) return emptyList()

        return client.from("conversations")
            .select(Columns.raw("id, created_at, updated_at, conversation_participants(conversation_id, user_id, last_read_at, profiles(id, name, avatar_url))")) {
                filter { isIn("id", ids) }
                order("updated_at", Order.DESCENDING)
            }
            .decodeList<ConversationDto>()
    }

    suspend fun getMessages(conversationId: String, limit: Int = 200): List<MessageDto> {
        return client.from("messages")
            .select(Columns.raw("id, conversation_id, sender_id, content, created_at, profiles!sender_id(id, name, avatar_url)")) {
                filter { eq("conversation_id", conversationId) }
                order("created_at", Order.ASCENDING)
                limit(limit.toLong())
            }
            .decodeList<MessageDto>()
    }

    suspend fun sendMessage(conversationId: String, senderId: String, content: String): MessageDto {
        return client.from("messages")
            .insert(
                MessageInsertDto(
                    conversationId = conversationId,
                    senderId = senderId,
                    content = content
                )
            ) { select(Columns.raw("id, conversation_id, sender_id, content, created_at")) }
            .decodeSingle<MessageDto>()
    }

    /**
     * Find an existing 1:1 conversation between these two users, or create a new one.
     * Returns the conversation id.
     */
    suspend fun createConversation(userIds: List<String>): String {
        require(userIds.size >= 2) { "Нужно минимум 2 участника" }

        // Try to find an existing conversation shared by all users.
        if (userIds.size == 2) {
            val a = userIds[0]
            val b = userIds[1]

            val aConvs = client.from("conversation_participants")
                .select(Columns.raw("conversation_id")) { filter { eq("user_id", a) } }
                .decodeList<ConversationIdRow>()
                .map { it.conversationId }

            if (aConvs.isNotEmpty()) {
                val bConvs = client.from("conversation_participants")
                    .select(Columns.raw("conversation_id")) {
                        filter { eq("user_id", b); isIn("conversation_id", aConvs) }
                    }
                    .decodeList<ConversationIdRow>()
                    .map { it.conversationId }

                if (bConvs.isNotEmpty()) {
                    return bConvs.first()
                }
            }
        }

        // Create new conversation
        val created = client.from("conversations")
            .insert(ConversationInsertDto(createdBy = userIds.firstOrNull())) {
                select(Columns.raw("id"))
            }
            .decodeSingle<ConversationIdOnly>()

        val participants = userIds.map {
            ConversationParticipantInsertDto(conversationId = created.id, userId = it)
        }
        client.from("conversation_participants").insert(participants)

        return created.id
    }
}

@kotlinx.serialization.Serializable
private data class ConversationIdRow(
    @kotlinx.serialization.SerialName("conversation_id") val conversationId: String
)

@kotlinx.serialization.Serializable
private data class ConversationIdOnly(
    val id: String
)
