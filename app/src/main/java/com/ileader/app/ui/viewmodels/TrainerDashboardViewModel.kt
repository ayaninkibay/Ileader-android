package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.TrainerRepository
import com.ileader.app.data.repository.TrainerTeamData
import com.ileader.app.data.repository.TrainerTeamStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrainerDashboardScreenData(
    val teams: List<TrainerTeamData>,
    val tournaments: List<Tournament>,
    val registeredTournamentIds: Map<String, List<String>>
)

class TrainerDashboardViewModel : ViewModel() {
    private val repo = TrainerRepository()

    private val _state = MutableStateFlow<UiState<TrainerDashboardScreenData>>(UiState.Loading)
    val state: StateFlow<UiState<TrainerDashboardScreenData>> = _state

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh(userId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val teams = repo.getMyTeams(userId)
                val sportIds = teams.map { it.sportId }.distinct()
                val tournaments = repo.getAvailableTournaments(sportIds)
                val registeredMap = mutableMapOf<String, List<String>>()
                for (team in teams) {
                    registeredMap[team.id] = repo.getTeamRegisteredTournamentIds(team.id)
                }
                _state.value = UiState.Success(
                    TrainerDashboardScreenData(
                        teams = teams,
                        tournaments = tournaments,
                        registeredTournamentIds = registeredMap
                    )
                )
            } catch (_: Exception) { }
            _isRefreshing.value = false
        }
    }

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val teams = repo.getMyTeams(userId)
                val sportIds = teams.map { it.sportId }.distinct()
                val tournaments = repo.getAvailableTournaments(sportIds)

                val registeredMap = mutableMapOf<String, List<String>>()
                for (team in teams) {
                    registeredMap[team.id] = repo.getTeamRegisteredTournamentIds(team.id)
                }

                _state.value = UiState.Success(
                    TrainerDashboardScreenData(
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
}

fun getTeamStats(team: TrainerTeamData): TrainerTeamStats {
    val members = team.members
    val totalT = members.sumOf { it.tournaments }
    val totalW = members.sumOf { it.wins }
    val totalP = members.sumOf { it.podiums }
    val avgR = if (members.isNotEmpty()) members.sumOf { it.rating } / members.size else 0
    val winR = if (totalT > 0) totalW.toFloat() / totalT * 100 else 0f
    return TrainerTeamStats(members.size, totalT, totalW, totalP, avgR, winR)
}
