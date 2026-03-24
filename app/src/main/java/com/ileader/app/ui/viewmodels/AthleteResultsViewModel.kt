package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.repository.AthleteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AthleteResultsData(
    val results: List<TournamentResult>,
    val sports: List<Pair<String, String>>,
    val stats: AthleteStats
)

class AthleteResultsViewModel : ViewModel() {
    private val repo = AthleteRepository()

    private val _state = MutableStateFlow<UiState<AthleteResultsData>>(UiState.Loading)
    val state: StateFlow<UiState<AthleteResultsData>> = _state

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val results = repo.getMyResults(userId)
                val sports = repo.getSports(userId)
                val stats = repo.getStats(userId)

                _state.value = UiState.Success(
                    AthleteResultsData(
                        results = results,
                        sports = sports,
                        stats = stats
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
