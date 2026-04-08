package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.LocationDto
import com.ileader.app.data.remote.dto.LocationReviewDto
import com.ileader.app.data.remote.dto.LocationReviewInsertDto
import com.ileader.app.data.repository.LocationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement

data class LocationDetailData(
    val location: LocationDto,
    val reviews: List<LocationReviewDto>
)

class LocationDetailViewModel : ViewModel() {
    private val repo = LocationRepository()

    private val _state = MutableStateFlow<UiState<LocationDetailData>>(UiState.Loading)
    val state: StateFlow<UiState<LocationDetailData>> = _state.asStateFlow()

    fun load(locationId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val locDef = async { repo.getLocationDetail(locationId) }
                val reviewsDef = async { repo.getLocationReviews(locationId) }
                _state.value = UiState.Success(
                    LocationDetailData(locDef.await(), reviewsDef.await())
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}

class LocationReviewFormViewModel : ViewModel() {
    private val repo = LocationRepository()

    private val _submit = MutableStateFlow<UiState<Unit>?>(null)
    val submit: StateFlow<UiState<Unit>?> = _submit.asStateFlow()

    fun submit(
        locationId: String,
        userId: String,
        overall: Double,
        criteria: JsonElement?,
        comment: String?
    ) {
        viewModelScope.launch {
            _submit.value = UiState.Loading
            try {
                repo.createReview(
                    LocationReviewInsertDto(
                        locationId = locationId,
                        userId = userId,
                        overall = overall,
                        criteria = criteria,
                        comment = comment
                    )
                )
                _submit.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _submit.value = UiState.Error(e.message ?: "Ошибка отправки")
            }
        }
    }

    fun reset() { _submit.value = null }
}
