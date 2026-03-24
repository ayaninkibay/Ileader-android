package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.RefereeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RefereeHistoryData(
    val history: List<RefereeTournament>,
    val violations: List<RefereeViolation>,
    val monthlyStats: List<RefereeMonthlyStats>
)

class RefereeHistoryViewModel : ViewModel() {
    private val repo = RefereeRepository()

    private val _state = MutableStateFlow<UiState<RefereeHistoryData>>(UiState.Loading)
    val state: StateFlow<UiState<RefereeHistoryData>> = _state

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val history = repo.getTournamentHistory(userId)
                val violations = repo.getViolations(userId)

                // Compute monthly stats from history data
                val monthlyStats = computeMonthlyStats(history, violations)

                _state.value = UiState.Success(
                    RefereeHistoryData(
                        history = history,
                        violations = violations,
                        monthlyStats = monthlyStats
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    private fun computeMonthlyStats(
        history: List<RefereeTournament>,
        violations: List<RefereeViolation>
    ): List<RefereeMonthlyStats> {
        val months = listOf("Янв", "Фев", "Мар", "Апр", "Май", "Июн",
            "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек")

        val now = java.time.LocalDate.now()
        val recentMonths = (0..2).map { now.minusMonths(it.toLong()) }.reversed()

        return recentMonths.map { date ->
            val monthPrefix = "${date.year}-${date.monthValue.toString().padStart(2, '0')}"
            val monthTournaments = history.count { it.date.startsWith(monthPrefix) }
            val monthViolations = violations.count { it.date.startsWith(monthPrefix) }

            RefereeMonthlyStats(
                month = months[date.monthValue - 1],
                tournaments = monthTournaments,
                avgRating = 0f,
                violations = monthViolations
            )
        }
    }
}
