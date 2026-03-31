package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.AthleteGoal
import com.ileader.app.data.models.UserRole
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ProfileDto
import com.ileader.app.data.remote.dto.ProfileUpdateDto
import com.ileader.app.data.remote.dto.UserSportDto
import com.ileader.app.data.remote.dto.UserSportStatsDto
import com.ileader.app.data.repository.AthleteRepository
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val viewerRepo = ViewerRepository()
    private val athleteRepo = AthleteRepository()

    private val _profile = MutableStateFlow<UiState<ProfileDto>>(UiState.Loading)
    val profile: StateFlow<UiState<ProfileDto>> = _profile

    private val _stats = MutableStateFlow<List<UserSportStatsDto>>(emptyList())
    val stats: StateFlow<List<UserSportStatsDto>> = _stats

    private val _userSports = MutableStateFlow<List<UserSportDto>>(emptyList())
    val userSports: StateFlow<List<UserSportDto>> = _userSports

    private val _goals = MutableStateFlow<UiState<List<AthleteGoal>>?>(null)
    val goals: StateFlow<UiState<List<AthleteGoal>>?> = _goals

    private val _saveState = MutableStateFlow<UiState<Unit>?>(null)
    val saveState: StateFlow<UiState<Unit>?> = _saveState

    fun load(userId: String, role: UserRole? = null) {
        viewModelScope.launch {
            _profile.value = UiState.Loading

            val profileDeferred = async {
                viewerRepo.getProfile(userId)
            }
            val statsDeferred = async {
                try { viewerRepo.getUserSportStats(userId) } catch (_: Exception) { emptyList() }
            }
            val sportsDeferred = async {
                try { viewerRepo.getUserSports(userId) } catch (_: Exception) { emptyList() }
            }

            try {
                val profileData = profileDeferred.await()
                _profile.value = UiState.Success(profileData)
            } catch (e: Exception) {
                _profile.value = UiState.Error(e.message ?: "Ошибка загрузки профиля")
            }

            _stats.value = statsDeferred.await()
            _userSports.value = sportsDeferred.await()

            // Load goals for athlete
            if (role == UserRole.ATHLETE) {
                _goals.value = UiState.Loading
                launch {
                    try {
                        val goalsList = athleteRepo.getGoals(userId)
                        _goals.value = UiState.Success(goalsList)
                    } catch (e: Exception) {
                        _goals.value = UiState.Error(e.message ?: "Ошибка загрузки целей")
                    }
                }
            }
        }
    }

    fun updateProfile(userId: String, data: ProfileUpdateDto) {
        viewModelScope.launch {
            _saveState.value = UiState.Loading
            try {
                viewerRepo.updateProfile(userId, data)
                _saveState.value = UiState.Success(Unit)
                load(userId)
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    fun clearSaveState() {
        _saveState.value = null
    }
}
