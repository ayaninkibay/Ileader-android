package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ProfileDto
import com.ileader.app.data.remote.dto.ProfileUpdateDto
import com.ileader.app.data.repository.SponsorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SponsorProfileViewModel : ViewModel() {
    private val repo = SponsorRepository()

    private val _state = MutableStateFlow<UiState<ProfileDto>>(UiState.Loading)
    val state: StateFlow<UiState<ProfileDto>> = _state

    private val _saveState = MutableStateFlow<RequestState>(RequestState.Idle)
    val saveState: StateFlow<RequestState> = _saveState

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val profile = repo.getProfile(userId)
                _state.value = UiState.Success(profile)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun saveProfile(userId: String, name: String, phone: String, city: String, bio: String) {
        viewModelScope.launch {
            _saveState.value = RequestState.Loading
            try {
                repo.updateProfile(userId, ProfileUpdateDto(
                    name = name,
                    phone = phone.ifBlank { null },
                    city = city.ifBlank { null },
                    bio = bio.ifBlank { null }
                ))
                _saveState.value = RequestState.Success
                load(userId) // reload
            } catch (e: Exception) {
                _saveState.value = RequestState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = RequestState.Idle
    }
}
