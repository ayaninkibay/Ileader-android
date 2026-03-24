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

data class OrgLocationDetailData(
    val location: LocationDto,
    val tournamentsAtLocation: List<TournamentWithCountsDto>
)

class OrganizerLocationDetailViewModel : ViewModel() {

    private val repo = OrganizerRepository()

    private val _state = MutableStateFlow<UiState<OrgLocationDetailData>>(UiState.Loading)
    val state: StateFlow<UiState<OrgLocationDetailData>> = _state.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Boolean>?>(null)
    val saveState: StateFlow<UiState<Boolean>?> = _saveState.asStateFlow()

    fun load(locationId: String, userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val location = repo.getLocationDetail(locationId)
                val allTournaments = repo.getMyTournaments(userId)
                val tournamentsAtLocation = allTournaments.filter { it.locationId == locationId }
                _state.value = UiState.Success(
                    OrgLocationDetailData(location, tournamentsAtLocation)
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun loadForEdit(locationId: String?) {
        if (locationId == null) {
            _state.value = UiState.Success(
                OrgLocationDetailData(
                    LocationDto(name = ""),
                    emptyList()
                )
            )
            return
        }
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val location = repo.getLocationDetail(locationId)
                _state.value = UiState.Success(
                    OrgLocationDetailData(location, emptyList())
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun saveLocation(locationId: String?, data: LocationInsertDto) {
        viewModelScope.launch {
            _saveState.value = UiState.Loading
            try {
                if (locationId != null) {
                    repo.updateLocation(locationId, data)
                } else {
                    repo.createLocation(data)
                }
                _saveState.value = UiState.Success(true)
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }
}
