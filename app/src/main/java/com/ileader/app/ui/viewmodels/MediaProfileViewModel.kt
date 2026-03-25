package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ProfileUpdateDto
import com.ileader.app.data.repository.AccreditationStats
import com.ileader.app.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// TODO: подключить к реальным данным
data class MediaProfile(
    val phone: String = "",
    val mediaName: String = "",
    val mediaType: String = "",
    val website: String = "",
    val description: String = ""
)

data class MediaProfileData(
    val profile: User,
    val accreditationStats: AccreditationStats,
    // TODO: подключить к реальным данным (profiles.role_data JSONB)
    val mediaProfile: MediaProfile,
    val coverageAreas: List<String>,
    val achievements: List<String>,
    val publishedArticles: Int,
    val totalViews: Int
)

class MediaProfileViewModel : ViewModel() {
    private val repo = MediaRepository()

    private val _state = MutableStateFlow<UiState<MediaProfileData>>(UiState.Loading)
    val state: StateFlow<UiState<MediaProfileData>> = _state

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    private var currentUserId: String = ""

    fun load(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val profile = repo.getProfile(userId)
                val accreditationStats = repo.getAccreditationStats(userId)

                val articleStats = repo.getArticleStats(userId)

                // TODO: coverageAreas and achievements from profiles.role_data JSONB
                _state.value = UiState.Success(
                    MediaProfileData(
                        profile = profile,
                        accreditationStats = accreditationStats,
                        mediaProfile = MediaProfile(),
                        coverageAreas = emptyList(),
                        achievements = emptyList(),
                        publishedArticles = articleStats.published,
                        totalViews = articleStats.totalViews
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun updateProfile(name: String, phone: String, city: String, bio: String?) {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                repo.updateProfile(
                    currentUserId,
                    ProfileUpdateDto(
                        name = name,
                        phone = phone.ifBlank { null },
                        city = city.ifBlank { null },
                        bio = bio?.ifBlank { null }
                    )
                )
                _saveState.value = SaveState.Success
                load(currentUserId)
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
