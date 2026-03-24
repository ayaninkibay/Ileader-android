package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.GoalInsertDto
import com.ileader.app.data.repository.AthleteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AthleteGoalsData(
    val goals: List<AthleteGoal>,
    val stats: AthleteStats
)

class AthleteGoalsViewModel : ViewModel() {
    private val repo = AthleteRepository()

    private val _state = MutableStateFlow<UiState<AthleteGoalsData>>(UiState.Loading)
    val state: StateFlow<UiState<AthleteGoalsData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    private var currentUserId: String = ""

    fun load(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val goals = repo.getGoals(userId)
                val stats = repo.getStats(userId)

                _state.value = UiState.Success(
                    AthleteGoalsData(goals = goals, stats = stats)
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun createGoal(
        type: GoalType,
        title: String,
        description: String,
        targetValue: Int
    ) {
        viewModelScope.launch {
            try {
                val goalType = when (type) {
                    GoalType.RATING -> "rating"
                    GoalType.TOURNAMENT -> "tournament"
                    GoalType.POINTS -> "points"
                }
                repo.createGoal(GoalInsertDto(
                    athleteId = currentUserId,
                    type = goalType,
                    title = title,
                    description = description.ifEmpty { null },
                    createdById = currentUserId,
                    targetRating = if (type == GoalType.RATING) targetValue else null,
                    targetWins = if (type == GoalType.TOURNAMENT) targetValue else null,
                    targetPoints = if (type == GoalType.POINTS) targetValue else null
                ))
                load(currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            try {
                repo.deleteGoal(goalId)
                load(currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
