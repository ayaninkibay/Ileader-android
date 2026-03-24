package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ProfileUpdateDto
import com.ileader.app.data.repository.AthleteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AthleteProfileData(
    val user: User,
    val sports: List<Pair<String, String>>
)

class AthleteProfileViewModel : ViewModel() {
    private val repo = AthleteRepository()

    private val _state = MutableStateFlow<UiState<AthleteProfileData>>(UiState.Loading)
    val state: StateFlow<UiState<AthleteProfileData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    private var currentUserId: String = ""

    fun load(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val user = repo.getProfile(userId)
                val sports = repo.getSports(userId)
                _state.value = UiState.Success(
                    AthleteProfileData(user = user, sports = sports)
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
                    name = name.ifEmpty { null },
                    phone = phone.ifEmpty { null },
                    city = city.ifEmpty { null },
                    bio = bio.ifEmpty { null }
                ))
                load(currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
