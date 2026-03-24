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

data class RefereeRequestsData(
    val incoming: List<RefereeInvite>,
    val outgoing: List<RefereeInvite>
)

class RefereeRequestsViewModel : ViewModel() {
    private val repo = RefereeRepository()

    private val _state = MutableStateFlow<UiState<RefereeRequestsData>>(UiState.Loading)
    val state: StateFlow<UiState<RefereeRequestsData>> = _state

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    private var currentUserId: String = ""

    fun load(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val incoming = repo.getIncomingInvites(userId)
                val outgoing = repo.getOutgoingApplications(userId)

                _state.value = UiState.Success(
                    RefereeRequestsData(
                        incoming = incoming,
                        outgoing = outgoing
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun respond(inviteId: String, accept: Boolean) {
        viewModelScope.launch {
            try {
                repo.respondToInvite(inviteId, accept)
                load(currentUserId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
