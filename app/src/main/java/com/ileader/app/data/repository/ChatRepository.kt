package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ChatRepository {
    private val client = SupabaseModule.client

    /**
     * Получить список диалогов пользователя.
     * Загружает conversation_participants → profiles + последнее сообщение.
     */
    suspend fun getConversations(userId: String): List<ConversationItem> {
        // 1. Получить все conversation_id где участвует пользователь
        val myParticipations = client.from("conversation_participants")
            .select(Columns.raw("conversation_id, last_read_at")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<ConversationParticipantDto>()

        if (myParticipations.isEmpty()) return emptyList()

        val conversationIds = myParticipations.map { it.conversationId }

        // 2. Получить других участников этих диалогов (с профилями)
        val otherParticipants = client.from("conversation_participants")
            .select(Columns.raw("conversation_id, user_id, profiles(id, name, avatar_url)")) {
                filter {
                    isIn("conversation_id", conversationIds)
                    neq("user_id", userId)
                }
            }
            .decodeList<ConversationParticipantDto>()

        // 3. Получить последнее сообщение для каждого диалога
        val conversations = conversationIds.mapNotNull { convId ->
            val other = otherParticipants.find { it.conversationId == convId } ?: return@mapNotNull null
            val myLastRead = myParticipations.find { it.conversationId == convId }?.lastReadAt

            val lastMessages = client.from("messages")
                .select(Columns.raw("id, content, created_at, sender_id")) {
                    filter { eq("conversation_id", convId) }
                    order("created_at", Order.DESCENDING)
                    limit(1)
                }
                .decodeList<MessageDto>()

            val lastMsg = lastMessages.firstOrNull()

            // Подсчёт непрочитанных
            val unread = if (myLastRead != null) {
                client.from("messages")
                    .select {
                        filter {
                            eq("conversation_id", convId)
                            neq("sender_id", userId)
                            gt("created_at", myLastRead)
                        }
                    }
                    .decodeList<MessageDto>()
                    .size
            } else {
                client.from("messages")
                    .select {
                        filter {
                            eq("conversation_id", convId)
                            neq("sender_id", userId)
                        }
                    }
                    .decodeList<MessageDto>()
                    .size
            }

            ConversationItem(
                conversationId = convId,
                otherUserId = other.profiles?.id ?: other.userId,
                otherUserName = other.profiles?.name ?: "Пользователь",
                otherUserAvatar = other.profiles?.avatarUrl,
                lastMessage = lastMsg?.content,
                lastMessageTime = lastMsg?.createdAt,
                unreadCount = unread
            )
        }

        return conversations.sortedByDescending { it.lastMessageTime ?: "" }
    }

    /**
     * Получить сообщения диалога.
     */
    suspend fun getMessages(conversationId: String): List<MessageDto> {
        return client.from("messages")
            .select(Columns.raw("id, conversation_id, sender_id, content, created_at, profiles(id, name, avatar_url)")) {
                filter { eq("conversation_id", conversationId) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList()
    }

    /**
     * Отправить сообщение.
     */
    suspend fun sendMessage(conversationId: String, senderId: String, content: String): MessageDto {
        val insert = MessageInsertDto(
            conversationId = conversationId,
            senderId = senderId,
            content = content
        )
        return client.from("messages")
            .insert(insert) { select() }
            .decodeSingle()
    }

    /**
     * Обновить last_read_at для участника.
     */
    suspend fun markAsRead(conversationId: String, userId: String) {
        client.from("conversation_participants")
            .update(mapOf("last_read_at" to java.time.Instant.now().toString())) {
                filter {
                    eq("conversation_id", conversationId)
                    eq("user_id", userId)
                }
            }
    }

    /**
     * Создать новый диалог между двумя пользователями.
     * Возвращает conversation_id (существующий или новый).
     */
    suspend fun getOrCreateConversation(userId1: String, userId2: String): String {
        // Проверить, есть ли уже диалог между этими пользователями
        val myConversations = client.from("conversation_participants")
            .select(Columns.raw("conversation_id")) {
                filter { eq("user_id", userId1) }
            }
            .decodeList<ConversationParticipantDto>()
            .map { it.conversationId }

        if (myConversations.isNotEmpty()) {
            val otherInSame = client.from("conversation_participants")
                .select(Columns.raw("conversation_id")) {
                    filter {
                        eq("user_id", userId2)
                        isIn("conversation_id", myConversations)
                    }
                }
                .decodeList<ConversationParticipantDto>()

            if (otherInSame.isNotEmpty()) {
                return otherInSame.first().conversationId
            }
        }

        // Создать новый диалог
        val conversation = client.from("conversations")
            .insert(mapOf<String, String>()) { select() }
            .decodeSingle<ConversationDto>()

        // Добавить участников
        client.from("conversation_participants")
            .insert(listOf(
                mapOf("conversation_id" to conversation.id, "user_id" to userId1),
                mapOf("conversation_id" to conversation.id, "user_id" to userId2)
            ))

        return conversation.id
    }

    /**
     * Подписка на новые сообщения через Supabase Realtime.
     */
    fun subscribeToMessages(conversationId: String): Flow<MessageDto> {
        val channel = client.channel("chat:$conversationId")
        val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
            filter = "conversation_id=eq.$conversationId"
        }.map { action ->
            val record = action.record
            MessageDto(
                id = record["id"]?.jsonPrimitive?.content ?: "",
                conversationId = record["conversation_id"]?.jsonPrimitive?.content ?: "",
                senderId = record["sender_id"]?.jsonPrimitive?.content ?: "",
                content = record["content"]?.jsonPrimitive?.content ?: "",
                createdAt = record["created_at"]?.jsonPrimitive?.content
            )
        }
        return flow
    }

    suspend fun subscribeChannel(conversationId: String) {
        val channel = client.channel("chat:$conversationId")
        channel.subscribe()
    }

    suspend fun unsubscribeChannel(conversationId: String) {
        try {
            val channel = client.channel("chat:$conversationId")
            channel.unsubscribe()
        } catch (_: Exception) {}
    }
}
