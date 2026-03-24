package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminUsersViewModel : ViewModel() {
    private val repo = AdminRepository()

    private val _state = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val state: StateFlow<UiState<List<User>>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val users = repo.getAllUsers()
                _state.value = UiState.Success(users)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun blockUser(userId: String) {
        viewModelScope.launch {
            try {
                repo.blockUser(userId)
                refreshUsers()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            try {
                repo.unblockUser(userId)
                refreshUsers()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                repo.deleteUser(userId)
                refreshUsers()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun verifyUser(userId: String) {
        viewModelScope.launch {
            try {
                repo.verifyUser(userId)
                refreshUsers()
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    private suspend fun refreshUsers() {
        try {
            val users = repo.getAllUsers()
            _state.value = UiState.Success(users)
        } catch (e: Exception) {
            _mutationError.value = e.message ?: "Ошибка обновления списка"
        }
    }
}
