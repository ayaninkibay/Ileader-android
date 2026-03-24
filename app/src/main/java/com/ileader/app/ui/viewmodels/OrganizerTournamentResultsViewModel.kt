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

data class TournamentResultsData(
    val tournament: TournamentDto?,
    val results: List<ResultDto>,
    val participants: List<ParticipantDto>
)

class OrganizerTournamentResultsViewModel : ViewModel() {

    private val repo = OrganizerRepository()

    private val _state = MutableStateFlow<UiState<TournamentResultsData>>(UiState.Loading)
    val state: StateFlow<UiState<TournamentResultsData>> = _state.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Boolean>?>(null)
    val saveState: StateFlow<UiState<Boolean>?> = _saveState.asStateFlow()

    fun load(tournamentId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val tournament = try { repo.getTournamentDetail(tournamentId) } catch (_: Exception) { null }
                val results = repo.getResults(tournamentId)
                val participants = repo.getParticipants(tournamentId)
                _state.value = UiState.Success(
                    TournamentResultsData(tournament, results, participants)
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun saveResults(tournamentId: String, results: List<ResultInsertDto>) {
        viewModelScope.launch {
            _saveState.value = UiState.Loading
            try {
                repo.saveResults(results)
                _saveState.value = UiState.Success(true)
                load(tournamentId)
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    fun publishResults(tournamentId: String) {
        viewModelScope.launch {
            _saveState.value = UiState.Loading
            try {
                repo.updateTournamentStatus(tournamentId, "completed")
                _saveState.value = UiState.Success(true)
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Ошибка публикации")
            }
        }
    }
}
