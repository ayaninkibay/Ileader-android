package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.AdminUserUpdateDto
import com.ileader.app.data.remote.dto.RoleDto
import com.ileader.app.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserEditData(
    val user: User,
    val roles: List<RoleDto> = emptyList()
)

class AdminUserEditViewModel : ViewModel() {
    private val repo = AdminRepository()

    private val _state = MutableStateFlow<UiState<UserEditData>>(UiState.Loading)
    val state: StateFlow<UiState<UserEditData>> = _state

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val user = repo.getUserDetail(userId)
                val roles = repo.getRoles()
                _state.value = UiState.Success(UserEditData(user, roles))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun save(userId: String, data: AdminUserUpdateDto) {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                repo.updateUser(userId, data)
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    sealed class SaveState {
        data object Idle : SaveState()
        data object Saving : SaveState()
        data object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }
}
