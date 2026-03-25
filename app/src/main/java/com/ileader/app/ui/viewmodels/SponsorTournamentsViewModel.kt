package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SponsorshipDto
import com.ileader.app.data.remote.dto.TournamentDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.SponsorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── Tournaments List ──

data class SponsorTournamentsData(
    val sponsoredTournaments: List<SponsorshipDto>,
    val allTournaments: List<TournamentWithCountsDto>
)

class SponsorTournamentsViewModel : ViewModel() {
    private val repo = SponsorRepository()

    private val _state = MutableStateFlow<UiState<SponsorTournamentsData>>(UiState.Loading)
    val state: StateFlow<UiState<SponsorTournamentsData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    private val _appliedTournaments = MutableStateFlow<Set<String>>(emptySet())
    val appliedTournaments: StateFlow<Set<String>> = _appliedTournaments

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun load(sponsorId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val sponsored = repo.getSponsoredTournaments(sponsorId)
                val all = repo.getAllTournaments()
                _state.value = UiState.Success(SponsorTournamentsData(sponsored, all))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun refresh(sponsorId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val sponsored = repo.getSponsoredTournaments(sponsorId)
                val all = repo.getAllTournaments()
                _state.value = UiState.Success(SponsorTournamentsData(sponsored, all))
            } catch (_: Exception) { }
            _isRefreshing.value = false
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

// ── Tournament Detail ──

data class SponsorTournamentDetailData(
    val tournament: TournamentDto,
    val participantCount: Int,
    val refereeCount: Int,
    val sponsorship: SponsorshipDto?
)

class SponsorTournamentDetailViewModel : ViewModel() {
    private val repo = SponsorRepository()

    private val _state = MutableStateFlow<UiState<SponsorTournamentDetailData>>(UiState.Loading)
    val state: StateFlow<UiState<SponsorTournamentDetailData>> = _state

    private val _sponsorRequestState = MutableStateFlow<RequestState>(RequestState.Idle)
    val sponsorRequestState: StateFlow<RequestState> = _sponsorRequestState

    fun load(sponsorId: String, tournamentId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val tournament = repo.getTournamentDetail(tournamentId)
                val participantCount = repo.getTournamentParticipantCount(tournamentId)
                val refereeCount = repo.getTournamentRefereeCount(tournamentId)
                val sponsorship = repo.getSponsorshipForTournament(sponsorId, tournamentId)
                _state.value = UiState.Success(
                    SponsorTournamentDetailData(tournament, participantCount, refereeCount, sponsorship)
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun requestSponsorship(sponsorId: String, tournamentId: String) {
        viewModelScope.launch {
            _sponsorRequestState.value = RequestState.Loading
            try {
                repo.requestTournamentSponsorship(sponsorId, tournamentId)
                _sponsorRequestState.value = RequestState.Success
                load(sponsorId, tournamentId) // reload to update sponsorship status
            } catch (e: Exception) {
                _sponsorRequestState.value = RequestState.Error(e.message ?: "Ошибка")
            }
        }
    }
}
