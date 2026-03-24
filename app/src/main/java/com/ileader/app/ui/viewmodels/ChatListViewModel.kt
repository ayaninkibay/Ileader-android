package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ConversationItem
import com.ileader.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatListViewModel : ViewModel() {
    private val repo = ChatRepository()

    private val _state = MutableStateFlow<UiState<List<ConversationItem>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ConversationItem>>> = _state

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val conversations = repo.getConversations(userId)
                _state.value = UiState.Success(conversations)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun startConversation(userId: String, otherUserId: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val conversationId = repo.getOrCreateConversation(userId, otherUserId)
                onCreated(conversationId)
            } catch (_: Exception) {}
        }
    }
}
