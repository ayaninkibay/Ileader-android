package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.repository.OrganizerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardData(
    val stats: OrganizerStatsDto,
    val upcomingTournaments: List<TournamentDto>,
    val recentRegistrations: List<ParticipantDto>
)

class OrganizerDashboardViewModel : ViewModel() {

    private val repo = OrganizerRepository()

    private val _state = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
    val state: StateFlow<UiState<DashboardData>> = _state.asStateFlow()

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val stats = repo.getStats(userId)
                val upcoming = repo.getUpcomingTournaments(userId)
                val registrations = repo.getRecentRegistrations(userId)
                _state.value = UiState.Success(
                    DashboardData(stats, upcoming, registrations)
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun approveParticipant(tournamentId: String, athleteId: String, userId: String) {
        viewModelScope.launch {
            try {
                repo.approveParticipant(tournamentId, athleteId)
                load(userId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun declineParticipant(tournamentId: String, athleteId: String, userId: String) {
        viewModelScope.launch {
            try {
                repo.declineParticipant(tournamentId, athleteId)
                load(userId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
