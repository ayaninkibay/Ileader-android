package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.MessageDto
import com.ileader.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repo = ChatRepository()

    private val _messages = MutableStateFlow<UiState<List<MessageDto>>>(UiState.Loading)
    val messages: StateFlow<UiState<List<MessageDto>>> = _messages

    private val _sendingMessage = MutableStateFlow(false)
    val sendingMessage: StateFlow<Boolean> = _sendingMessage

    private var currentConversationId: String? = null
    private var currentUserId: String? = null

    fun load(conversationId: String, userId: String) {
        currentConversationId = conversationId
        currentUserId = userId
        viewModelScope.launch {
            _messages.value = UiState.Loading
            try {
                val msgs = repo.getMessages(conversationId)
                _messages.value = UiState.Success(msgs)
                // Пометить как прочитанное
                repo.markAsRead(conversationId, userId)
                // Подписка на realtime
                subscribeToRealtime(conversationId)
            } catch (e: Exception) {
                _messages.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun sendMessage(content: String) {
        val convId = currentConversationId ?: return
        val userId = currentUserId ?: return
        if (content.isBlank()) return

        viewModelScope.launch {
            _sendingMessage.value = true
            try {
                repo.sendMessage(convId, userId, content.trim())
                // Перезагрузить сообщения (realtime тоже подхватит, но для надёжности)
                val msgs = repo.getMessages(convId)
                _messages.value = UiState.Success(msgs)
            } catch (_: Exception) {}
            _sendingMessage.value = false
        }
    }

    private fun subscribeToRealtime(conversationId: String) {
        viewModelScope.launch {
            try {
                repo.subscribeChannel(conversationId)
                repo.subscribeToMessages(conversationId).collect { newMessage ->
                    val current = (_messages.value as? UiState.Success)?.data ?: emptyList()
                    if (current.none { it.id == newMessage.id }) {
                        _messages.value = UiState.Success(current + newMessage)
                    }
                }
            } catch (_: Exception) {
                // Realtime недоступен — fallback на polling нет (пользователь может pull-to-refresh)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentConversationId?.let { convId ->
            // Попытка отписки — fire and forget
            viewModelScope.launch {
                repo.unsubscribeChannel(convId)
            }
        }
    }
}
