package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.mock.AthleteMockData
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.AthleteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AthleteDashboardData(
    val stats: AthleteStats,
    val upcoming: List<Tournament>,
    val recentResults: List<TournamentResult>,
    val leaderboard: List<Triple<String, Int, Int>>,
    val ratingHistory: List<Pair<String, Int>>
)

class AthleteDashboardViewModel : ViewModel() {
    private val repo = AthleteRepository()

    private val _state = MutableStateFlow<UiState<AthleteDashboardData>>(UiState.Loading)
    val state: StateFlow<UiState<AthleteDashboardData>> = _state

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh(userId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val stats = repo.getStats(userId)
                val myTournaments = repo.getMyTournaments(userId)
                val upcoming = myTournaments.filter {
                    it.status in listOf(
                        TournamentStatus.REGISTRATION_OPEN,
                        TournamentStatus.REGISTRATION_CLOSED,
                        TournamentStatus.CHECK_IN,
                        TournamentStatus.IN_PROGRESS
                    )
                }.take(3)
                val recentResults = repo.getMyResults(userId).take(4)
                val leaderboard = AthleteMockData.leaderboard
                val ratingHistory = AthleteMockData.ratingHistory

                _state.value = UiState.Success(
                    AthleteDashboardData(
                        stats = stats,
                        upcoming = upcoming,
                        recentResults = recentResults,
                        leaderboard = leaderboard,
                        ratingHistory = ratingHistory
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
                val stats = repo.getStats(userId)
                val myTournaments = repo.getMyTournaments(userId)
                val upcoming = myTournaments.filter {
                    it.status in listOf(
                        TournamentStatus.REGISTRATION_OPEN,
                        TournamentStatus.REGISTRATION_CLOSED,
                        TournamentStatus.CHECK_IN,
                        TournamentStatus.IN_PROGRESS
                    )
                }.take(3)
                val recentResults = repo.getMyResults(userId).take(4)

                // Leaderboard and rating history — no DB table yet, use mock as fallback
                val leaderboard = AthleteMockData.leaderboard
                val ratingHistory = AthleteMockData.ratingHistory

                _state.value = UiState.Success(
                    AthleteDashboardData(
                        stats = stats,
                        upcoming = upcoming,
                        recentResults = recentResults,
                        leaderboard = leaderboard,
                        ratingHistory = ratingHistory
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
