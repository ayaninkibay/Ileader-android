package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ProfileDto
import com.ileader.app.data.remote.dto.ProfileUpdateDto
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ViewerProfileViewModel : ViewModel() {
    private val repo = ViewerRepository()

    private val _state = MutableStateFlow<UiState<ProfileDto>>(UiState.Loading)
    val state: StateFlow<UiState<ProfileDto>> = _state

    private val _updateState = MutableStateFlow<UiState<Unit>?>(null)
    val updateState: StateFlow<UiState<Unit>?> = _updateState

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

    fun updateProfile(userId: String, data: ProfileUpdateDto) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            try {
                repo.updateProfile(userId, data)
                _updateState.value = UiState.Success(Unit)
                load(userId) // reload profile
            } catch (e: Exception) {
                _updateState.value = UiState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }
}
