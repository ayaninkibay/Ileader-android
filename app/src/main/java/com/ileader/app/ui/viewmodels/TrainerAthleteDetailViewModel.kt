package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.GoalDto
import com.ileader.app.data.remote.dto.GoalInsertDto
import com.ileader.app.data.repository.TrainerAthleteData
import com.ileader.app.data.repository.TrainerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrainerAthleteDetailData(
    val athlete: TrainerAthleteData,
    val teamName: String,
    val results: List<TournamentResult>,
    val ratingHistory: List<Pair<String, Int>>,
    val goals: List<GoalDto> = emptyList()
)

class TrainerAthleteDetailViewModel : ViewModel() {
    private val repo = TrainerRepository()

    private val _state = MutableStateFlow<UiState<TrainerAthleteDetailData>>(UiState.Loading)
    val state: StateFlow<UiState<TrainerAthleteDetailData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    fun load(userId: String, athleteId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val teams = repo.getMyTeams(userId)
                val team = teams.find { t -> t.members.any { it.id == athleteId } }
                val athlete = team?.members?.find { it.id == athleteId }

                if (athlete == null) {
                    _state.value = UiState.Error("Спортсмен не найден")
                    return@launch
                }

                val results = repo.getAthleteResults(athleteId)

                // TODO: подключить к реальным данным (rating_history)
                val ratingHistory = emptyList<Pair<String, Int>>()

                // Goals from DB
                val goals = try {
                    repo.getAthleteGoals(athleteId)
                } catch (_: Exception) { emptyList() }

                _state.value = UiState.Success(
                    TrainerAthleteDetailData(
                        athlete = athlete,
                        teamName = team.name,
                        results = results,
                        ratingHistory = ratingHistory,
                        goals = goals
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun createGoal(
        athleteId: String,
        trainerId: String,
        type: String,
        title: String,
        description: String? = null,
        targetRating: Int? = null,
        targetWins: Int? = null,
        targetPodiums: Int? = null,
        deadline: String? = null,
        sportId: String? = null
    ) {
        viewModelScope.launch {
            try {
                repo.createGoalForAthlete(GoalInsertDto(
                    athleteId = athleteId,
                    type = type,
                    title = title,
                    description = description,
                    createdBy = "trainer",
                    createdById = trainerId,
                    deadline = deadline,
                    targetRating = targetRating,
                    targetWins = targetWins,
                    targetPodiums = targetPodiums,
                    sportId = sportId
                ))
                load(trainerId, athleteId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
