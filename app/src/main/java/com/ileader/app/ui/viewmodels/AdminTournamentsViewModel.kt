package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TournamentDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminTournamentsViewModel : ViewModel() {
    private val repo = AdminRepository()

    private val _state = MutableStateFlow<UiState<List<TournamentWithCountsDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<TournamentWithCountsDto>>> = _state

    private val _detailState = MutableStateFlow<UiState<TournamentDto>>(UiState.Loading)
    val detailState: StateFlow<UiState<TournamentDto>> = _detailState

    private val _saveState = MutableStateFlow<AdminUserEditViewModel.SaveState>(AdminUserEditViewModel.SaveState.Idle)
    val saveState: StateFlow<AdminUserEditViewModel.SaveState> = _saveState

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val tournaments = repo.getAllTournaments()
                _state.value = UiState.Success(tournaments)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun deleteTournament(tournamentId: String) {
        viewModelScope.launch {
            try {
                repo.deleteTournament(tournamentId)
                refresh()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun updateStatus(tournamentId: String, status: String) {
        viewModelScope.launch {
            try {
                repo.updateTournamentStatus(tournamentId, status)
                refresh()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun loadDetail(tournamentId: String) {
        viewModelScope.launch {
            _detailState.value = UiState.Loading
            try {
                val detail = repo.getTournamentDetail(tournamentId)
                _detailState.value = UiState.Success(detail)
            } catch (e: Exception) {
                _detailState.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun updateTournament(tournamentId: String, data: Map<String, String>) {
        viewModelScope.launch {
            _saveState.value = AdminUserEditViewModel.SaveState.Saving
            try {
                repo.updateTournament(tournamentId, data)
                _saveState.value = AdminUserEditViewModel.SaveState.Success
            } catch (e: Exception) {
                _saveState.value = AdminUserEditViewModel.SaveState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = AdminUserEditViewModel.SaveState.Idle
    }

    private suspend fun refresh() {
        try {
            val tournaments = repo.getAllTournaments()
            _state.value = UiState.Success(tournaments)
        } catch (e: Exception) {
            _mutationError.value = e.message ?: "Ошибка обновления списка"
        }
    }
}
