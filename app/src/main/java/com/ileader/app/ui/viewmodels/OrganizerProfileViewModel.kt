package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ProfileDto
import com.ileader.app.data.remote.dto.ProfileUpdateDto
import com.ileader.app.data.repository.OrganizerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrganizerProfileViewModel : ViewModel() {

    private val repo = OrganizerRepository()

    private val _state = MutableStateFlow<UiState<ProfileDto>>(UiState.Loading)
    val state: StateFlow<UiState<ProfileDto>> = _state.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Boolean>?>(null)
    val saveState: StateFlow<UiState<Boolean>?> = _saveState.asStateFlow()

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
            _saveState.value = UiState.Loading
            try {
                repo.updateProfile(userId, data)
                _saveState.value = UiState.Success(true)
                load(userId)
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }
}
