package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.LocationDto
import com.ileader.app.data.repository.OrganizerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocationsListData(
    val locations: List<LocationDto>,
    val tournamentCountByLocation: Map<String?, Int>
)

class OrganizerLocationsViewModel : ViewModel() {

    private val repo = OrganizerRepository()

    private val _state = MutableStateFlow<UiState<LocationsListData>>(UiState.Loading)
    val state: StateFlow<UiState<LocationsListData>> = _state.asStateFlow()

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val locations = repo.getMyLocations(userId)
                val tournaments = repo.getMyTournaments(userId)
                val countByLocation = tournaments
                    .groupBy { it.locationId }
                    .mapValues { it.value.size }
                _state.value = UiState.Success(
                    LocationsListData(locations, countByLocation)
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun deleteLocation(locationId: String, userId: String) {
        viewModelScope.launch {
            try {
                repo.deleteLocation(locationId)
                load(userId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
