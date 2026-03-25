package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.RefereeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RefereeTournamentsData(
    val assigned: List<RefereeTournament>,
    val history: List<RefereeTournament>
) {
    val all: List<RefereeTournament> get() = assigned + history.filter { h -> assigned.none { it.id == h.id } }
}

class RefereeTournamentsViewModel : ViewModel() {
    private val repo = RefereeRepository()

    private val _state = MutableStateFlow<UiState<RefereeTournamentsData>>(UiState.Loading)
    val state: StateFlow<UiState<RefereeTournamentsData>> = _state

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val assigned = repo.getAssignedTournaments(userId)
                val history = repo.getTournamentHistory(userId)

                _state.value = UiState.Success(
                    RefereeTournamentsData(
                        assigned = assigned,
                        history = history
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun refresh(userId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val assigned = repo.getAssignedTournaments(userId)
                val history = repo.getTournamentHistory(userId)
                _state.value = UiState.Success(
                    RefereeTournamentsData(assigned = assigned, history = history)
                )
            } catch (_: Exception) { }
            _isRefreshing.value = false
        }
    }
}
