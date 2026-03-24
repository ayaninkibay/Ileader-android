package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ProfileUpdateDto
import com.ileader.app.data.repository.RefereeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RefereeProfileData(
    val profile: User,
    val sports: List<Pair<String, String>> // name to id
)

class RefereeProfileViewModel : ViewModel() {
    private val repo = RefereeRepository()

    private val _state = MutableStateFlow<UiState<RefereeProfileData>>(UiState.Loading)
    val state: StateFlow<UiState<RefereeProfileData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    private var currentUserId: String = ""

    fun load(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val profile = repo.getProfile(userId)
                val sports = repo.getSports(userId)

                _state.value = UiState.Success(
                    RefereeProfileData(
                        profile = profile,
                        sports = sports
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun updateProfile(name: String, phone: String, city: String, bio: String) {
        viewModelScope.launch {
            try {
                repo.updateProfile(currentUserId, ProfileUpdateDto(
                    name = name,
                    phone = phone,
                    city = city,
                    bio = bio
                ))
                load(currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
