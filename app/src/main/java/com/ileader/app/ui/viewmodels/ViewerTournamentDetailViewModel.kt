package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ViewerTournamentDetailData(
    val tournament: TournamentDto,
    val participants: List<ParticipantDto>,
    val results: List<ResultDto>,
    val bracket: List<BracketMatchDto>,
    val groups: List<TournamentGroupDto> = emptyList()
)

sealed class SpectatorRegState {
    object Idle : SpectatorRegState()
    object Loading : SpectatorRegState()
    object Registered : SpectatorRegState()
    object NotRegistered : SpectatorRegState()
    data class Error(val message: String) : SpectatorRegState()
}

class ViewerTournamentDetailViewModel : ViewModel() {
    private val repo = ViewerRepository()

    private val _state = MutableStateFlow<UiState<ViewerTournamentDetailData>>(UiState.Loading)
    val state: StateFlow<UiState<ViewerTournamentDetailData>> = _state

    private val _spectatorState = MutableStateFlow<SpectatorRegState>(SpectatorRegState.Idle)
    val spectatorState: StateFlow<SpectatorRegState> = _spectatorState

    fun load(tournamentId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val tournament = repo.getTournamentDetail(tournamentId)
                val participants = repo.getTournamentParticipants(tournamentId)
                val results = repo.getTournamentResults(tournamentId)
                val bracket = repo.getTournamentBracket(tournamentId)
                val groups = repo.getTournamentGroups(tournamentId)

                _state.value = UiState.Success(
                    ViewerTournamentDetailData(
                        tournament = tournament,
                        participants = participants,
                        results = results,
                        bracket = bracket,
                        groups = groups
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun checkSpectatorStatus(tournamentId: String, userId: String) {
        viewModelScope.launch {
            try {
                val reg = repo.getMySpectatorRegistration(tournamentId, userId)
                _spectatorState.value = if (reg != null) SpectatorRegState.Registered else SpectatorRegState.NotRegistered
            } catch (_: Exception) {
                _spectatorState.value = SpectatorRegState.NotRegistered
            }
        }
    }

    fun registerAsSpectator(tournamentId: String, userId: String) {
        viewModelScope.launch {
            _spectatorState.value = SpectatorRegState.Loading
            try {
                repo.registerAsSpectator(tournamentId, userId)
                _spectatorState.value = SpectatorRegState.Registered
            } catch (e: Exception) {
                _spectatorState.value = SpectatorRegState.Error(e.message ?: "Ошибка регистрации")
            }
        }
    }
}
