package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.LocationDto
import com.ileader.app.data.remote.dto.LocationInsertDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocationDetailData(
    val location: LocationDto,
    val tournaments: List<TournamentWithCountsDto> = emptyList()
)

class AdminLocationsViewModel : ViewModel() {
    private val repo = AdminRepository()

    private val _state = MutableStateFlow<UiState<List<LocationDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<LocationDto>>> = _state

    private val _detailState = MutableStateFlow<UiState<LocationDetailData>>(UiState.Loading)
    val detailState: StateFlow<UiState<LocationDetailData>> = _detailState

    private val _saveState = MutableStateFlow<AdminUserEditViewModel.SaveState>(AdminUserEditViewModel.SaveState.Idle)
    val saveState: StateFlow<AdminUserEditViewModel.SaveState> = _saveState

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val locations = repo.getAllLocations()
                _state.value = UiState.Success(locations)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun loadDetail(locationId: String) {
        viewModelScope.launch {
            _detailState.value = UiState.Loading
            try {
                val location = repo.getLocationDetail(locationId)
                val tournaments = repo.getLocationTournaments(locationId)
                _detailState.value = UiState.Success(LocationDetailData(location, tournaments))
            } catch (e: Exception) {
                _detailState.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun deleteLocation(locationId: String) {
        viewModelScope.launch {
            try {
                repo.deleteLocation(locationId)
                refresh()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun updateLocation(locationId: String, data: LocationInsertDto) {
        viewModelScope.launch {
            _saveState.value = AdminUserEditViewModel.SaveState.Saving
            try {
                repo.updateLocation(locationId, data)
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
            val locations = repo.getAllLocations()
            _state.value = UiState.Success(locations)
        } catch (e: Exception) {
            _mutationError.value = e.message ?: "Ошибка обновления списка"
        }
    }
}
