package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.AdminPlatformStats
import com.ileader.app.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminDashboardData(
    val stats: AdminPlatformStats = AdminPlatformStats(),
    val recentUsers: List<User> = emptyList(),
    val roleDistribution: List<Pair<String, Int>> = emptyList()
)

class AdminDashboardViewModel : ViewModel() {
    private val repo = AdminRepository()

    private val _state = MutableStateFlow<UiState<AdminDashboardData>>(UiState.Loading)
    val state: StateFlow<UiState<AdminDashboardData>> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val stats = repo.getPlatformStats()
                val recentUsers = repo.getRecentUsers()
                val roleDistribution = repo.getRoleDistribution()
                _state.value = UiState.Success(
                    AdminDashboardData(stats, recentUsers, roleDistribution)
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
