package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.BracketMatchDto
import com.ileader.app.data.remote.dto.ParticipantDto
import com.ileader.app.data.remote.dto.TournamentGroupDto
import com.ileader.app.data.repository.AthleteRepository
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AthleteTournamentsData(
    val myTournaments: List<Tournament>,
    val availableTournaments: List<Tournament>,
    val sports: List<Pair<String, String>>
)

data class AthleteTournamentBracketData(
    val bracket: List<BracketMatchDto> = emptyList(),
    val participants: List<ParticipantDto> = emptyList(),
    val groups: List<TournamentGroupDto> = emptyList(),
    val format: String = "single_elimination"
)

class AthleteTournamentsViewModel : ViewModel() {
    private val repo = AthleteRepository()
    private val viewerRepo = ViewerRepository()

    private val _state = MutableStateFlow<UiState<AthleteTournamentsData>>(UiState.Loading)
    val state: StateFlow<UiState<AthleteTournamentsData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val myTournaments = repo.getMyTournaments(userId)
                val available = repo.getAvailableTournaments()
                val sports = repo.getSports(userId)

                // Combine: my tournaments + available (dedup by id)
                val myIds = myTournaments.map { it.id }.toSet()
                val allTournaments = myTournaments + available.filter { it.id !in myIds }

                _state.value = UiState.Success(
                    AthleteTournamentsData(
                        myTournaments = myTournaments,
                        availableTournaments = allTournaments,
                        sports = sports
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    // ── Tournament Detail ──

    private val _detailState = MutableStateFlow<UiState<Tournament>>(UiState.Loading)
    val detailState: StateFlow<UiState<Tournament>> = _detailState

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered

    private val _bracketData = MutableStateFlow(AthleteTournamentBracketData())
    val bracketData: StateFlow<AthleteTournamentBracketData> = _bracketData

    fun loadDetail(tournamentId: String, userId: String) {
        viewModelScope.launch {
            _detailState.value = UiState.Loading
            try {
                val tournament = repo.getTournamentDetail(tournamentId)
                val registered = repo.getMyParticipation(tournamentId, userId)
                _isRegistered.value = registered
                _detailState.value = UiState.Success(tournament)

                // Load bracket data in background
                try {
                    val bracket = viewerRepo.getTournamentBracket(tournamentId)
                    val participants = viewerRepo.getTournamentParticipants(tournamentId)
                    val groups = viewerRepo.getTournamentGroups(tournamentId)
                    val detail = viewerRepo.getTournamentDetail(tournamentId)
                    _bracketData.value = AthleteTournamentBracketData(
                        bracket = bracket,
                        participants = participants,
                        groups = groups,
                        format = detail.format ?: "single_elimination"
                    )
                } catch (_: Exception) { /* bracket loading is optional */ }
            } catch (e: Exception) {
                _detailState.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun toggleRegistration(tournamentId: String, userId: String) {
        viewModelScope.launch {
            val previousValue = _isRegistered.value
            try {
                if (previousValue) {
                    _isRegistered.value = false
                    repo.cancelRegistration(tournamentId, userId)
                } else {
                    _isRegistered.value = true
                    repo.registerForTournament(tournamentId, userId)
                }
            } catch (e: Exception) {
                _isRegistered.value = previousValue
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
