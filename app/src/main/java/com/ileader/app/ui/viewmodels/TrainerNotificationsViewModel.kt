package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.TrainerNotificationData
import com.ileader.app.data.repository.TrainerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrainerNotificationsScreenData(
    val notifications: List<TrainerNotificationData>
)

class TrainerNotificationsViewModel : ViewModel() {
    private val repo = TrainerRepository()

    private val _state = MutableStateFlow<UiState<TrainerNotificationsScreenData>>(UiState.Loading)
    val state: StateFlow<UiState<TrainerNotificationsScreenData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val teamRequests = repo.getTeamRequests(userId)
                val all = teamRequests.sortedByDescending { it.createdAt }

                _state.value = UiState.Success(
                    TrainerNotificationsScreenData(notifications = all)
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun respondToRequest(requestId: String, accept: Boolean, userId: String) {
        viewModelScope.launch {
            try {
                repo.respondToTeamRequest(requestId, accept)
                load(userId) // reload
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
