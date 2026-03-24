package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SponsorshipDto
import com.ileader.app.data.remote.dto.TeamRequestDto
import com.ileader.app.data.remote.dto.TournamentInviteDto
import com.ileader.app.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminRequestsData(
    val verifications: List<User> = emptyList(),
    val invites: List<TournamentInviteDto> = emptyList(),
    val teamRequests: List<TeamRequestDto> = emptyList(),
    val sponsorships: List<SponsorshipDto> = emptyList()
)

class AdminRequestsViewModel : ViewModel() {
    private val repo = AdminRepository()

    private val _state = MutableStateFlow<UiState<AdminRequestsData>>(UiState.Loading)
    val state: StateFlow<UiState<AdminRequestsData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val verifications = repo.getPendingVerifications()
                val invites = repo.getTournamentInvites()
                val teamRequests = repo.getTeamRequests()
                val sponsorships = repo.getPendingSponsorships()
                _state.value = UiState.Success(
                    AdminRequestsData(verifications, invites, teamRequests, sponsorships)
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun approveVerification(userId: String) {
        viewModelScope.launch {
            try {
                repo.verifyUser(userId)
                refresh()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun declineVerification(userId: String) {
        viewModelScope.launch {
            try {
                repo.rejectVerification(userId)
                refresh()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun respondToInvite(inviteId: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                repo.respondToInvite(inviteId, approve)
                refresh()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun respondToTeamRequest(requestId: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                repo.respondToTeamRequest(requestId, approve)
                refresh()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun respondToSponsorship(sponsorshipId: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                repo.respondToSponsorship(sponsorshipId, approve)
                refresh()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    private suspend fun refresh() {
        try {
            val verifications = repo.getPendingVerifications()
            val invites = repo.getTournamentInvites()
            val teamRequests = repo.getTeamRequests()
            val sponsorships = repo.getPendingSponsorships()
            _state.value = UiState.Success(
                AdminRequestsData(verifications, invites, teamRequests, sponsorships)
            )
        } catch (e: Exception) {
            _mutationError.value = e.message ?: "Ошибка обновления списка"
        }
    }
}
