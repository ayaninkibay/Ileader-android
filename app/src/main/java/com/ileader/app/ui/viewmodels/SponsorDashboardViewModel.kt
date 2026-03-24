package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SponsorshipDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.SponsorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SponsorDashboardData(
    val sponsorships: List<SponsorshipDto>,
    val openTournaments: List<TournamentWithCountsDto>,
    val totalInvested: Double,
    val teamCount: Int,
    val tournamentCount: Int
)

class SponsorDashboardViewModel : ViewModel() {
    private val repo = SponsorRepository()

    private val _state = MutableStateFlow<UiState<SponsorDashboardData>>(UiState.Loading)
    val state: StateFlow<UiState<SponsorDashboardData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    private val _appliedTournaments = MutableStateFlow<Set<String>>(emptySet())
    val appliedTournaments: StateFlow<Set<String>> = _appliedTournaments

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val sponsorships = repo.getSponsorships(userId)
                val openTournaments = repo.getOpenTournaments()

                val totalInvested = sponsorships.sumOf { it.amount ?: 0.0 }
                val teamCount = sponsorships.count { it.teamId != null }
                val tournamentCount = sponsorships.count { it.tournamentId != null }

                _state.value = UiState.Success(
                    SponsorDashboardData(
                        sponsorships = sponsorships,
                        openTournaments = openTournaments,
                        totalInvested = totalInvested,
                        teamCount = teamCount,
                        tournamentCount = tournamentCount
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun requestTournamentSponsorship(sponsorId: String, tournamentId: String) {
        viewModelScope.launch {
            try {
                repo.requestTournamentSponsorship(sponsorId, tournamentId)
                _appliedTournaments.value = _appliedTournaments.value + tournamentId
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
