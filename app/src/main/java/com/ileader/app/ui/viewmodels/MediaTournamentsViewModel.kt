package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MediaTournamentItem(
    val tournament: TournamentWithCountsDto,
    val accreditationStatus: String? // null = not accredited, "pending", "accepted", "declined"
)

data class MediaTournamentsData(
    val tournaments: List<MediaTournamentItem>
)

class MediaTournamentsViewModel : ViewModel() {
    private val repo = MediaRepository()

    private val _state = MutableStateFlow<UiState<MediaTournamentsData>>(UiState.Loading)
    val state: StateFlow<UiState<MediaTournamentsData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    private var currentUserId: String = ""

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun load(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val tournaments = repo.getAllTournaments()
                val accreditationMap = repo.getAccreditationMap(userId)

                val items = tournaments.map { tournament ->
                    MediaTournamentItem(
                        tournament = tournament,
                        accreditationStatus = accreditationMap[tournament.id]
                    )
                }

                _state.value = UiState.Success(MediaTournamentsData(items))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun refresh(userId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val tournaments = repo.getAllTournaments()
                val accreditationMap = repo.getAccreditationMap(userId)
                val items = tournaments.map { tournament ->
                    MediaTournamentItem(
                        tournament = tournament,
                        accreditationStatus = accreditationMap[tournament.id]
                    )
                }
                _state.value = UiState.Success(MediaTournamentsData(items))
            } catch (_: Exception) { }
            _isRefreshing.value = false
        }
    }

    fun requestAccreditation(tournamentId: String) {
        viewModelScope.launch {
            try {
                repo.requestAccreditation(currentUserId, tournamentId)
                // Reload data to reflect change
                load(currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun cancelAccreditation(tournamentId: String) {
        viewModelScope.launch {
            try {
                repo.cancelAccreditation(currentUserId, tournamentId)
                load(currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
