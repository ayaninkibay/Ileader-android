package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.TrainerRepository
import com.ileader.app.data.repository.TrainerTeamData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrainerTournamentsScreenData(
    val teams: List<TrainerTeamData>,
    val tournaments: List<Tournament>,
    val registeredTournamentIds: Map<String, List<String>>
)

class TrainerTournamentsViewModel : ViewModel() {
    private val repo = TrainerRepository()

    private val _state = MutableStateFlow<UiState<TrainerTournamentsScreenData>>(UiState.Loading)
    val state: StateFlow<UiState<TrainerTournamentsScreenData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val teams = repo.getMyTeams(userId)
                val tournaments = repo.getAvailableTournaments(teams.map { it.sportId }.distinct())

                val registeredMap = mutableMapOf<String, List<String>>()
                for (team in teams) {
                    registeredMap[team.id] = repo.getTeamRegisteredTournamentIds(team.id)
                }

                _state.value = UiState.Success(
                    TrainerTournamentsScreenData(
                        teams = teams,
                        tournaments = tournaments,
                        registeredTournamentIds = registeredMap
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun registerTeam(tournamentId: String, teamId: String, memberIds: List<String>, userId: String) {
        viewModelScope.launch {
            try {
                repo.registerTeamForTournament(tournamentId, teamId, memberIds)
                load(userId) // reload
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun unregisterTeam(tournamentId: String, teamId: String, userId: String) {
        viewModelScope.launch {
            try {
                repo.unregisterTeamFromTournament(tournamentId, teamId)
                load(userId) // reload
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
