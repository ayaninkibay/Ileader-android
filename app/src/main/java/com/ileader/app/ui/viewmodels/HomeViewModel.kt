package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.CommunityProfileDto
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class HomeState(
    val sports: List<SportDto> = emptyList(),
    val news: UiState<List<ArticleDto>> = UiState.Loading,
    val tournaments: UiState<List<TournamentWithCountsDto>> = UiState.Loading,
    val people: UiState<List<CommunityProfileDto>> = UiState.Loading
)

class HomeViewModel : ViewModel() {
    private val repo = ViewerRepository()

    var state by mutableStateOf(HomeState())
        private set

    init {
        viewModelScope.launch {
            try {
                val sports = repo.getSports()
                state = state.copy(sports = sports)
            } catch (_: Exception) {}
        }
    }

    fun load() {
        viewModelScope.launch {
            val currentSports = state.sports

            val sportsDeferred = async {
                if (currentSports.isNotEmpty()) currentSports
                else try { repo.getSports() } catch (_: Exception) { emptyList() }
            }

            val newsDeferred = async {
                try { UiState.Success(repo.getRecentArticles(10)) }
                catch (e: Exception) { UiState.Error(e.message ?: "Ошибка") }
            }

            val tournamentsDeferred = async {
                try { UiState.Success(repo.getUpcomingTournaments(10)) }
                catch (e: Exception) { UiState.Error(e.message ?: "Ошибка") }
            }

            val peopleDeferred = async {
                try { UiState.Success(repo.getAthletes().take(10)) }
                catch (e: Exception) { UiState.Error(e.message ?: "Ошибка") }
            }

            state = HomeState(
                sports = sportsDeferred.await(),
                news = newsDeferred.await(),
                tournaments = tournamentsDeferred.await(),
                people = peopleDeferred.await()
            )
        }
    }
}
