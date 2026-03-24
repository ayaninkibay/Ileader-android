package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TournamentInviteDto
import com.ileader.app.data.repository.SponsorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SponsorNotificationsData(
    val invites: List<TournamentInviteDto>
)

class SponsorNotificationsViewModel : ViewModel() {
    private val repo = SponsorRepository()

    private val _state = MutableStateFlow<UiState<SponsorNotificationsData>>(UiState.Loading)
    val state: StateFlow<UiState<SponsorNotificationsData>> = _state

    private val _actionState = MutableStateFlow<RequestState>(RequestState.Idle)
    val actionState: StateFlow<RequestState> = _actionState

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val invites = repo.getInvites(userId)
                _state.value = UiState.Success(SponsorNotificationsData(invites))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun acceptInvite(inviteId: String, userId: String) {
        viewModelScope.launch {
            _actionState.value = RequestState.Loading
            try {
                repo.acceptInvite(inviteId)
                _actionState.value = RequestState.Success
                load(userId) // reload
            } catch (e: Exception) {
                _actionState.value = RequestState.Error(e.message ?: "Ошибка")
            }
        }
    }

    fun declineInvite(inviteId: String, responseMessage: String?, userId: String) {
        viewModelScope.launch {
            _actionState.value = RequestState.Loading
            try {
                repo.declineInvite(inviteId, responseMessage)
                _actionState.value = RequestState.Success
                load(userId) // reload
            } catch (e: Exception) {
                _actionState.value = RequestState.Error(e.message ?: "Ошибка")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = RequestState.Idle
    }
}
