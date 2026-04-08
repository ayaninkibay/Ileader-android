package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ConversationDto
import com.ileader.app.data.remote.dto.MessageDto
import com.ileader.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConversationsListViewModel : ViewModel() {
    private val repo = ChatRepository()

    private val _state = MutableStateFlow<UiState<List<ConversationDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ConversationDto>>> = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                _state.value = UiState.Success(repo.getConversations(userId))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}

class ChatViewModel : ViewModel() {
    private val repo = ChatRepository()

    private val _state = MutableStateFlow<UiState<List<MessageDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<MessageDto>>> = _state.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    private var convId: String = ""
    private var myUserId: String = ""

    fun load(conversationId: String, userId: String) {
        convId = conversationId
        myUserId = userId
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                _state.value = UiState.Success(repo.getMessages(conversationId))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки сообщений")
            }
        }
    }

    fun send(content: String) {
        if (content.isBlank() || convId.isEmpty()) return
        viewModelScope.launch {
            _sending.value = true
            try {
                val msg = repo.sendMessage(convId, myUserId, content.trim())
                val current = (_state.value as? UiState.Success)?.data ?: emptyList()
                _state.value = UiState.Success(current + msg)
            } catch (_: Exception) {
                // keep state
            } finally {
                _sending.value = false
            }
        }
    }
}

class StartConversationViewModel : ViewModel() {
    private val repo = ChatRepository()

    private val _state = MutableStateFlow<UiState<String>>(UiState.Loading)
    val state: StateFlow<UiState<String>> = _state.asStateFlow()

    fun start(myUserId: String, otherUserId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val id = repo.createConversation(listOf(myUserId, otherUserId))
                _state.value = UiState.Success(id)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Не удалось создать диалог")
            }
        }
    }
}
