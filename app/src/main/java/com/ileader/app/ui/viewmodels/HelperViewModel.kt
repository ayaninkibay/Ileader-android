package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TournamentHelperDto
import com.ileader.app.data.repository.HelperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HelperViewModel : ViewModel() {
    private val repo = HelperRepository()

    private val _state = MutableStateFlow<UiState<List<TournamentHelperDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<TournamentHelperDto>>> = _state

    fun loadAssignments(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val assignments = repo.getMyAssignments(userId)
                _state.value = UiState.Success(assignments)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
