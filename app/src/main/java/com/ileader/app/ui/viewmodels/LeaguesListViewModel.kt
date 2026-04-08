package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.LeagueDto
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.repository.LeagueRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LeaguesListData(
    val leagues: List<LeagueDto>,
    val sports: List<SportDto>
)

class LeaguesListViewModel : ViewModel() {
    private val repo = LeagueRepository()

    private val _state = MutableStateFlow<UiState<LeaguesListData>>(UiState.Loading)
    val state: StateFlow<UiState<LeaguesListData>> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val leaguesDef = async { repo.getAll() }
                val sportsDef = async { repo.getSports() }
                _state.value = UiState.Success(
                    LeaguesListData(
                        leagues = leaguesDef.await(),
                        sports = sportsDef.await()
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки лиг")
            }
        }
    }
}
