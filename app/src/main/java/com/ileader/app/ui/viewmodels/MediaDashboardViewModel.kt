package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.ArticleStatsDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.AccreditationStats
import com.ileader.app.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MediaDashboardData(
    val upcomingTournaments: List<TournamentWithCountsDto>,
    val accreditationStats: AccreditationStats,
    val recentArticles: List<ArticleDto>,
    val articleStats: ArticleStatsDto
)

class MediaDashboardViewModel : ViewModel() {
    private val repo = MediaRepository()

    private val _state = MutableStateFlow<UiState<MediaDashboardData>>(UiState.Loading)
    val state: StateFlow<UiState<MediaDashboardData>> = _state

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val tournaments = repo.getUpcomingTournaments(4)
                val accreditationStats = repo.getAccreditationStats(userId)
                val articles = repo.getMyArticles(userId).take(3)
                val articleStats = repo.getArticleStats(userId)

                _state.value = UiState.Success(
                    MediaDashboardData(
                        upcomingTournaments = tournaments,
                        accreditationStats = accreditationStats,
                        recentArticles = articles,
                        articleStats = articleStats
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
