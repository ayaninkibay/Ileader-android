package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.Team
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.AthleteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AthleteTeamViewModel : ViewModel() {
    private val repo = AthleteRepository()

    private val _state = MutableStateFlow<UiState<Team>>(UiState.Loading)
    val state: StateFlow<UiState<Team>> = _state

    fun load(teamId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val team = repo.getTeam(teamId)
                _state.value = UiState.Success(team)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
