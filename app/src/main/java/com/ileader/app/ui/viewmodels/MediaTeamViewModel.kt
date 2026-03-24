package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TeamDto
import com.ileader.app.data.remote.dto.TeamMemberDto
import com.ileader.app.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MediaTeamData(
    val team: TeamDto,
    val members: List<TeamMemberDto>
)

class MediaTeamViewModel : ViewModel() {
    private val repo = MediaRepository()

    private val _state = MutableStateFlow<UiState<MediaTeamData>>(UiState.Loading)
    val state: StateFlow<UiState<MediaTeamData>> = _state

    fun load(teamId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val team = repo.getTeam(teamId)
                val members = repo.getTeamMembers(teamId)
                _state.value = UiState.Success(MediaTeamData(team, members))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
