package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.mock.ViewerMockData
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ViewerHomeData(
    val totalUsers: Int,
    val totalTournaments: Int,
    val totalSports: Int,
    val sports: List<SportDto>,
    val upcomingTournaments: List<TournamentWithCountsDto>,
    val news: List<ViewerMockData.NewsArticle>
)

class ViewerHomeViewModel : ViewModel() {
    private val repo = ViewerRepository()

    private val _state = MutableStateFlow<UiState<ViewerHomeData>>(UiState.Loading)
    val state: StateFlow<UiState<ViewerHomeData>> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val (users, tournaments, sports) = repo.getPlatformStats()
                val sportsList = repo.getSports()
                val upcoming = repo.getUpcomingTournaments(10)
                val news = ViewerMockData.newsArticles // stays mock — no articles table

                _state.value = UiState.Success(
                    ViewerHomeData(
                        totalUsers = users,
                        totalTournaments = tournaments,
                        totalSports = sports,
                        sports = sportsList,
                        upcomingTournaments = upcoming,
                        news = news
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
