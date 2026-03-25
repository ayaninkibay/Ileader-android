package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.OrganizerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrganizerTournamentsViewModel : ViewModel() {

    private val repo = OrganizerRepository()

    private val _state = MutableStateFlow<UiState<List<TournamentWithCountsDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<TournamentWithCountsDto>>> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val tournaments = repo.getMyTournaments(userId)
                _state.value = UiState.Success(tournaments)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun refresh(userId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val tournaments = repo.getMyTournaments(userId)
                _state.value = UiState.Success(tournaments)
            } catch (_: Exception) { }
            _isRefreshing.value = false
        }
    }
}
