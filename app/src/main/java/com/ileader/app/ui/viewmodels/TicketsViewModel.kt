package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TicketItem
import com.ileader.app.data.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TicketsViewModel : ViewModel() {
    private val repo = TicketRepository()

    private val _state = MutableStateFlow<UiState<List<TicketItem>>>(UiState.Loading)
    val state: StateFlow<UiState<List<TicketItem>>> = _state

    private val _hasTickets = MutableStateFlow(false)
    val hasTickets: StateFlow<Boolean> = _hasTickets

    fun loadTickets(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val tickets = repo.getMyTickets(userId)
                _state.value = UiState.Success(tickets)
                _hasTickets.value = tickets.isNotEmpty()
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun checkHasTickets(userId: String) {
        viewModelScope.launch {
            try {
                _hasTickets.value = repo.hasActiveTickets(userId)
            } catch (_: Exception) {
                _hasTickets.value = false
            }
        }
    }
}
