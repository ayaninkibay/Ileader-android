package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.RefereeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RefereeDashboardData(
    val stats: RefereeStats,
    val pendingInvites: List<RefereeInvite>,
    val activeTournaments: List<RefereeTournament>,
    val upcomingTournaments: List<RefereeTournament>,
    val calendarTournaments: List<RefereeTournament>
)

class RefereeDashboardViewModel : ViewModel() {
    private val repo = RefereeRepository()

    private val _state = MutableStateFlow<UiState<RefereeDashboardData>>(UiState.Loading)
    val state: StateFlow<UiState<RefereeDashboardData>> = _state

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh(userId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val stats = repo.getStats(userId)
                val assigned = repo.getAssignedTournaments(userId)
                val invites = repo.getIncomingInvites(userId)
                val pendingInvites = invites.filter { it.status == InviteStatus.PENDING }
                val activeTournaments = assigned.filter { it.status == TournamentStatus.IN_PROGRESS }
                val upcomingTournaments = assigned.filter {
                    it.status in listOf(TournamentStatus.REGISTRATION_OPEN, TournamentStatus.REGISTRATION_CLOSED, TournamentStatus.CHECK_IN)
                }
                val calendarTournaments = assigned.filter { it.status != TournamentStatus.COMPLETED }
                _state.value = UiState.Success(
                    RefereeDashboardData(
                        stats = stats,
                        pendingInvites = pendingInvites,
                        activeTournaments = activeTournaments,
                        upcomingTournaments = upcomingTournaments,
                        calendarTournaments = calendarTournaments
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
                val assigned = repo.getAssignedTournaments(userId)
                val invites = repo.getIncomingInvites(userId)

                val pendingInvites = invites.filter { it.status == InviteStatus.PENDING }
                val activeTournaments = assigned.filter { it.status == TournamentStatus.IN_PROGRESS }
                val upcomingTournaments = assigned.filter {
                    it.status in listOf(TournamentStatus.REGISTRATION_OPEN, TournamentStatus.REGISTRATION_CLOSED, TournamentStatus.CHECK_IN)
                }
                val calendarTournaments = assigned.filter { it.status != TournamentStatus.COMPLETED }

                _state.value = UiState.Success(
                    RefereeDashboardData(
                        stats = stats,
                        pendingInvites = pendingInvites,
                        activeTournaments = activeTournaments,
                        upcomingTournaments = upcomingTournaments,
                        calendarTournaments = calendarTournaments
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
