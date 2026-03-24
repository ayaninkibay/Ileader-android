package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ViewerTournamentsData(
    val tournaments: List<TournamentWithCountsDto>,
    val sports: List<SportDto>
)

class ViewerTournamentsViewModel : ViewModel() {
    private val repo = ViewerRepository()

    private val _state = MutableStateFlow<UiState<ViewerTournamentsData>>(UiState.Loading)
    val state: StateFlow<UiState<ViewerTournamentsData>> = _state

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
}
