package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.SponsorshipDto
import com.ileader.app.data.remote.dto.TeamMemberDto
import com.ileader.app.data.repository.SponsorRepository
import com.ileader.app.data.remote.dto.TeamWithStatsDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── Teams List ──

data class SponsorTeamsData(
    val teams: List<TeamWithStatsDto>,
    val sports: List<SportDto>
)

class SponsorTeamsViewModel : ViewModel() {
    private val repo = SponsorRepository()

    private val _state = MutableStateFlow<UiState<SponsorTeamsData>>(UiState.Loading)
    val state: StateFlow<UiState<SponsorTeamsData>> = _state

    private val _requestState = MutableStateFlow<RequestState>(RequestState.Idle)
    val requestState: StateFlow<RequestState> = _requestState

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val teams = repo.getAvailableTeams()
                val sports = repo.getSports()
                _state.value = UiState.Success(SponsorTeamsData(teams, sports))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun requestSponsorship(sponsorId: String, teamId: String) {
        viewModelScope.launch {
            _requestState.value = RequestState.Loading
            try {
                repo.createSponsorshipRequest(sponsorId, teamId)
                _requestState.value = RequestState.Success
            } catch (e: Exception) {
                _requestState.value = RequestState.Error(e.message ?: "Ошибка отправки запроса")
            }
        }
    }

    fun resetRequestState() {
        _requestState.value = RequestState.Idle
    }
}

sealed class RequestState {
    data object Idle : RequestState()
    data object Loading : RequestState()
    data object Success : RequestState()
    data class Error(val message: String) : RequestState()
}

// ── My Team ──

data class SponsorMyTeamData(
    val sponsorship: SponsorshipDto,
    val members: List<TeamMemberDto>
)

class SponsorMyTeamViewModel : ViewModel() {
    private val repo = SponsorRepository()

    private val _state = MutableStateFlow<UiState<SponsorMyTeamData>>(UiState.Loading)
    val state: StateFlow<UiState<SponsorMyTeamData>> = _state

    fun load(sponsorId: String, teamId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val sponsoredTeams = repo.getSponsoredTeams(sponsorId)
                val sponsorship = sponsoredTeams.find { it.teamId == teamId }
                if (sponsorship != null) {
                    val members = repo.getTeamMembers(teamId)
                    _state.value = UiState.Success(SponsorMyTeamData(sponsorship, members))
                } else {
                    _state.value = UiState.Error("Команда не найдена")
                }
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
