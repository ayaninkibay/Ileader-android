package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.UserSportDto
import com.ileader.app.data.repository.OrganizerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SportsData(
    val sports: List<SportDto>,
    val mySports: List<UserSportDto>
)

class OrganizerSportsViewModel : ViewModel() {

    private val repo = OrganizerRepository()

    private val _state = MutableStateFlow<UiState<SportsData>>(UiState.Loading)
    val state: StateFlow<UiState<SportsData>> = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val sports = repo.getSports()
                val mySports = repo.getMySports(userId)
                _state.value = UiState.Success(SportsData(sports, mySports))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
