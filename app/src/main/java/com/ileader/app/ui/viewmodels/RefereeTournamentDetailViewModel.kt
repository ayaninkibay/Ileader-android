package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.repository.RefereeRepository
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RefereeTournamentDetailData(
    val tournament: RefereeTournament,
    val matches: List<RefereeMatch>,
    val participants: List<RefereeParticipant>,
    val violations: List<RefereeViolation>,
    val results: List<ResultDto> = emptyList(),
    val bracket: List<BracketMatchDto> = emptyList(),
    val bracketParticipants: List<ParticipantDto> = emptyList(),
    val groups: List<TournamentGroupDto> = emptyList(),
    val format: String = "single_elimination"
)

class RefereeTournamentDetailViewModel : ViewModel() {
    private val repo = RefereeRepository()
    private val viewerRepo = ViewerRepository()

    private val _state = MutableStateFlow<UiState<RefereeTournamentDetailData>>(UiState.Loading)
    val state: StateFlow<UiState<RefereeTournamentDetailData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    private var currentTournamentId: String = ""
    private var currentUserId: String = ""

    fun load(tournamentId: String, userId: String) {
        currentTournamentId = tournamentId
        currentUserId = userId
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val tournament = repo.getTournamentDetail(tournamentId)
                val matches = repo.getMatches(tournamentId)
                val participants = repo.getParticipants(tournamentId)
                val violations = repo.getViolationsByTournament(tournamentId, userId)
                val results = repo.getTournamentResults(tournamentId)

                // Load bracket data
                var bracket = emptyList<BracketMatchDto>()
                var bracketParticipants = emptyList<ParticipantDto>()
                var groups = emptyList<TournamentGroupDto>()
                var format = "single_elimination"
                try {
                    bracket = viewerRepo.getTournamentBracket(tournamentId)
                    bracketParticipants = viewerRepo.getTournamentParticipants(tournamentId)
                    groups = viewerRepo.getTournamentGroups(tournamentId)
                    val detail = viewerRepo.getTournamentDetail(tournamentId)
                    format = detail.format ?: "single_elimination"
                } catch (_: Exception) { /* optional */ }

                _state.value = UiState.Success(
                    RefereeTournamentDetailData(
                        tournament = tournament,
                        matches = matches,
                        participants = participants,
                        violations = violations,
                        results = results,
                        bracket = bracket,
                        bracketParticipants = bracketParticipants,
                        groups = groups,
                        format = format
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun addViolation(data: ViolationInsertDto) {
        viewModelScope.launch {
            try {
                repo.createViolation(data)
                load(currentTournamentId, currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun updateMatchResult(matchId: String, data: MatchResultUpdateDto) {
        viewModelScope.launch {
            try {
                repo.updateMatchResult(matchId, data)
                load(currentTournamentId, currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun startMatch(matchId: String) {
        viewModelScope.launch {
            try {
                repo.startMatch(matchId)
                load(currentTournamentId, currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun saveResults(results: List<ResultInsertDto>, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repo.saveResults(currentTournamentId, results)
                load(currentTournamentId, currentUserId)
                onSuccess()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
