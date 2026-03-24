package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.MediaInviteFullDto
import com.ileader.app.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MediaNotificationsData(
    val invites: List<MediaInviteFullDto>
)

class MediaNotificationsViewModel : ViewModel() {
    private val repo = MediaRepository()

    private val _state = MutableStateFlow<UiState<MediaNotificationsData>>(UiState.Loading)
    val state: StateFlow<UiState<MediaNotificationsData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    private var currentUserId: String = ""

    fun load(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val invites = repo.getMediaInvites(userId)
                _state.value = UiState.Success(MediaNotificationsData(invites))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun acceptInvite(inviteId: String, contactPhone: String, message: String?) {
        viewModelScope.launch {
            try {
                repo.acceptInvite(inviteId, contactPhone, message)
                load(currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun declineInvite(inviteId: String, reason: String) {
        viewModelScope.launch {
            try {
                repo.declineInvite(inviteId, reason)
                load(currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
