package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.CommunityProfileDto
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TeamWithStatsDto
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ViewerCommunityData(
    val athletes: List<CommunityProfileDto>,
    val trainers: List<CommunityProfileDto>,
    val referees: List<CommunityProfileDto>,
    val teams: List<TeamWithStatsDto>,
    val sports: List<SportDto>
)

class ViewerCommunityViewModel : ViewModel() {
    private val repo = ViewerRepository()

    private val _state = MutableStateFlow<UiState<ViewerCommunityData>>(UiState.Loading)
    val state: StateFlow<UiState<ViewerCommunityData>> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val athletes = repo.getAthletes()
                val trainers = repo.getTrainers()
                val referees = repo.getReferees()
                val teams = repo.getTeams()
                val sports = repo.getSports()

                _state.value = UiState.Success(
                    ViewerCommunityData(
                        athletes = athletes,
                        trainers = trainers,
                        referees = referees,
                        teams = teams,
                        sports = sports
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
