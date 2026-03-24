package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.SportInsertDto
import com.ileader.app.data.remote.dto.SportUpdateDto
import com.ileader.app.data.remote.dto.SportWithCountsDto
import com.ileader.app.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminSportsViewModel : ViewModel() {
    private val repo = AdminRepository()

    private val _state = MutableStateFlow<UiState<List<SportWithCountsDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<SportWithCountsDto>>> = _state

    // Single sport for edit screen
    private val _sportDetail = MutableStateFlow<UiState<SportWithCountsDto>>(UiState.Loading)
    val sportDetail: StateFlow<UiState<SportWithCountsDto>> = _sportDetail

    private val _saveState = MutableStateFlow<AdminUserEditViewModel.SaveState>(AdminUserEditViewModel.SaveState.Idle)
    val saveState: StateFlow<AdminUserEditViewModel.SaveState> = _saveState

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val sports = repo.getAllSports()
                val withCounts = sports.map { sport ->
                    val athleteCount = try { repo.getSportAthleteCount(sport.id) } catch (_: Exception) { 0 }
                    val tournamentCount = try { repo.getSportTournamentCount(sport.id) } catch (_: Exception) { 0 }
                    SportWithCountsDto(
                        id = sport.id,
                        name = sport.name,
                        slug = sport.slug,
                        athleteLabel = sport.athleteLabel,
                        iconUrl = sport.iconUrl,
                        isActive = sport.isActive,
                        athleteCount = athleteCount,
                        tournamentCount = tournamentCount
                    )
                }
                _state.value = UiState.Success(withCounts)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun loadSportDetail(sportId: String) {
        viewModelScope.launch {
            _sportDetail.value = UiState.Loading
            try {
                val sport = repo.getSportDetail(sportId)
                val athleteCount = try { repo.getSportAthleteCount(sportId) } catch (_: Exception) { 0 }
                val tournamentCount = try { repo.getSportTournamentCount(sportId) } catch (_: Exception) { 0 }
                _sportDetail.value = UiState.Success(
                    SportWithCountsDto(
                        id = sport.id,
                        name = sport.name,
                        slug = sport.slug,
                        athleteLabel = sport.athleteLabel,
                        iconUrl = sport.iconUrl,
                        isActive = sport.isActive,
                        athleteCount = athleteCount,
                        tournamentCount = tournamentCount
                    )
                )
            } catch (e: Exception) {
                _sportDetail.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun createSport(data: SportInsertDto) {
        viewModelScope.launch {
            _saveState.value = AdminUserEditViewModel.SaveState.Saving
            try {
                repo.createSport(data)
                _saveState.value = AdminUserEditViewModel.SaveState.Success
                load()
            } catch (e: Exception) {
                _saveState.value = AdminUserEditViewModel.SaveState.Error(e.message ?: "Ошибка создания")
            }
        }
    }

    fun updateSport(sportId: String, data: SportUpdateDto) {
        viewModelScope.launch {
            _saveState.value = AdminUserEditViewModel.SaveState.Saving
            try {
                repo.updateSport(sportId, data)
                _saveState.value = AdminUserEditViewModel.SaveState.Success
            } catch (e: Exception) {
                _saveState.value = AdminUserEditViewModel.SaveState.Error(e.message ?: "Ошибка обновления")
            }
        }
    }

    fun toggleSportActive(sportId: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                repo.toggleSportActive(sportId, isActive)
                load()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun deleteSport(sportId: String) {
        viewModelScope.launch {
            try {
                repo.deleteSport(sportId)
                load()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = AdminUserEditViewModel.SaveState.Idle
    }
}
