package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.TeamRequest
import com.ileader.app.data.models.TournamentInvite
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.AthleteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AthleteNotificationsData(
    val invites: List<TournamentInvite>,
    val teamRequests: List<TeamRequest>
)

class AthleteNotificationsViewModel : ViewModel() {
    private val repo = AthleteRepository()

    private val _state = MutableStateFlow<UiState<AthleteNotificationsData>>(UiState.Loading)
    val state: StateFlow<UiState<AthleteNotificationsData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    private var currentUserId: String = ""

    fun load(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val invites = repo.getTournamentInvites(userId)
                val teamRequests = repo.getTeamRequests(userId)
                _state.value = UiState.Success(
                    AthleteNotificationsData(
                        invites = invites,
                        teamRequests = teamRequests
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun respondToInvite(inviteId: String, accept: Boolean) {
        viewModelScope.launch {
            try {
                repo.respondToInvite(inviteId, accept)
                load(currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun respondToTeamRequest(requestId: String, accept: Boolean) {
        viewModelScope.launch {
            try {
                repo.respondToTeamRequest(requestId, accept)
                load(currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
