package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.RefereeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RefereeTeamData(
    val team: Team
)

class RefereeTeamViewModel : ViewModel() {
    private val repo = RefereeRepository()

    private val _state = MutableStateFlow<UiState<RefereeTeamData>>(UiState.Loading)
    val state: StateFlow<UiState<RefereeTeamData>> = _state

    fun load(teamId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val team = repo.getTeam(teamId)

                _state.value = UiState.Success(
                    RefereeTeamData(team = team)
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
