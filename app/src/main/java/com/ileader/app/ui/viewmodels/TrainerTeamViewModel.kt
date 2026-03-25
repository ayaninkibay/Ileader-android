package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.PendingInviteData
import com.ileader.app.data.repository.TrainerRepository
import com.ileader.app.data.repository.TrainerTeamData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log
import kotlinx.coroutines.launch

data class TrainerTeamScreenData(
    val teams: List<TrainerTeamData>,
    val pendingInvites: List<PendingInviteData>
)

class TrainerTeamViewModel : ViewModel() {
    private val repo = TrainerRepository()

    private val _state = MutableStateFlow<UiState<TrainerTeamScreenData>>(UiState.Loading)
    val state: StateFlow<UiState<TrainerTeamScreenData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    private val _inviteResult = MutableStateFlow<InviteResult?>(null)
    val inviteResult: StateFlow<InviteResult?> = _inviteResult

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val teams = repo.getMyTeams(userId)
                val pendingInvites = repo.getPendingInvites(userId)

                _state.value = UiState.Success(
                    TrainerTeamScreenData(
                        teams = teams,
                        pendingInvites = pendingInvites
                    )
                )
            } catch (e: Exception) {
                Log.e("TrainerTeam", "Load error", e)
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun inviteAthlete(teamId: String, email: String, userId: String) {
        viewModelScope.launch {
            try {
                repo.inviteAthlete(teamId, email)
                _inviteResult.value = InviteResult.Success
                load(userId)
            } catch (e: Exception) {
                _inviteResult.value = InviteResult.Error(e.message ?: "Ошибка приглашения")
            }
        }
    }

    fun clearInviteResult() {
        _inviteResult.value = null
    }

    fun removeAthlete(teamId: String, athleteId: String, userId: String) {
        viewModelScope.launch {
            try {
                repo.removeAthleteFromTeam(teamId, athleteId)
                load(userId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}

sealed class InviteResult {
    data object Success : InviteResult()
    data class Error(val message: String) : InviteResult()
}
