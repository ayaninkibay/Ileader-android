package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ViewerTournamentsData(
    val tournaments: List<TournamentWithCountsDto>,
    val sports: List<SportDto>
)

class ViewerTournamentsViewModel : ViewModel() {
    private val repo = ViewerRepository()

    private val _state = MutableStateFlow<UiState<ViewerTournamentsData>>(UiState.Loading)
    val state: StateFlow<UiState<ViewerTournamentsData>> = _state

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val tournaments = repo.getPublicTournaments()
                val sports = repo.getSports()

                _state.value = UiState.Success(
                    ViewerTournamentsData(
                        tournaments = tournaments,
                        sports = sports
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val tournaments = repo.getPublicTournaments()
                val sports = repo.getSports()
                _state.value = UiState.Success(
                    ViewerTournamentsData(tournaments = tournaments, sports = sports)
                )
            } catch (_: Exception) { }
            _isRefreshing.value = false
        }
    }
}
