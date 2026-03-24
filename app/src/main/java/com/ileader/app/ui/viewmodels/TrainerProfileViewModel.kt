package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ProfileUpdateDto
import com.ileader.app.data.repository.TrainerNotificationData
import com.ileader.app.data.repository.TrainerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrainerProfileScreenData(
    val profile: User,
    val sports: List<Pair<String, String>>,
    val pendingNotificationsCount: Int
)

class TrainerProfileViewModel : ViewModel() {
    private val repo = TrainerRepository()

    private val _state = MutableStateFlow<UiState<TrainerProfileScreenData>>(UiState.Loading)
    val state: StateFlow<UiState<TrainerProfileScreenData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val profile = repo.getProfile(userId)
                val sports = repo.getSports(userId)
                val requests = repo.getTeamRequests(userId)
                val pendingCount = requests.count { it.status == InviteStatus.PENDING }

                _state.value = UiState.Success(
                    TrainerProfileScreenData(
                        profile = profile,
                        sports = sports,
                        pendingNotificationsCount = pendingCount
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun updateProfile(userId: String, name: String, phone: String, city: String, bio: String) {
        viewModelScope.launch {
            try {
                repo.updateProfile(userId, ProfileUpdateDto(
                    name = name,
                    phone = phone,
                    city = city,
                    bio = bio
                ))
                load(userId) // reload
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
