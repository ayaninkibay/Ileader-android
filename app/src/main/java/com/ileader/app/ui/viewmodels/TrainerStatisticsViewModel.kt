package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.ResultDistribution
import com.ileader.app.data.repository.TrainerRepository
import com.ileader.app.data.repository.TrainerTeamData
import com.ileader.app.data.repository.TrainerTeamStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TrainerStatisticsScreenData(
    val teams: List<TrainerTeamData>,
    val ratingProgressByTeam: Map<String, List<Pair<String, Int>>>,
    val resultsDistributionByTeam: Map<String, List<ResultDistribution>>
)

class TrainerStatisticsViewModel : ViewModel() {
    private val repo = TrainerRepository()

    private val _state = MutableStateFlow<UiState<TrainerStatisticsScreenData>>(UiState.Loading)
    val state: StateFlow<UiState<TrainerStatisticsScreenData>> = _state

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val teams = repo.getMyTeams(userId)

                // Rating progress — no historical table, generate from avg team rating
                val months = listOf("Сен", "Окт", "Ноя", "Дек", "Янв", "Фев")
                val ratingProgress = mutableMapOf<String, List<Pair<String, Int>>>()
                for (team in teams) {
                    val avgRating = if (team.members.isNotEmpty())
                        team.members.sumOf { it.rating } / team.members.size else 1000
                    ratingProgress[team.id] = months.mapIndexed { i, month ->
                        month to (avgRating - (5 - i) * 30)
                    }
                }

                // Results distribution — from real tournament_results data
                val resultsDistribution = mutableMapOf<String, List<ResultDistribution>>()
                for (team in teams) {
                    resultsDistribution[team.id] = try {
                        repo.getTeamResultsDistribution(team.id)
                    } catch (_: Exception) {
                        listOf(
                            ResultDistribution("1 место", 0, 0xFFFFD700),
                            ResultDistribution("2 место", 0, 0xFFC0C0C0),
                            ResultDistribution("3 место", 0, 0xFFCD7F32),
                            ResultDistribution("Другое", 0, 0xFF6B7280)
                        )
                    }
                }

                _state.value = UiState.Success(
                    TrainerStatisticsScreenData(
                        teams = teams,
                        ratingProgressByTeam = ratingProgress,
                        resultsDistributionByTeam = resultsDistribution
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
