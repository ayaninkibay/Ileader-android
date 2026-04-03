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
    val people: UiState<List<CommunityProfileDto>> = UiState.Loading,
    val selectedSportSlug: String? = null
)

class HomeViewModel : ViewModel() {
    private val repo = ViewerRepository()

    var state by mutableStateOf(HomeState())
        private set

    private var allTournaments: List<TournamentWithCountsDto> = emptyList()
    private var allNews: List<ArticleDto> = emptyList()
    private var allPeople: List<CommunityProfileDto> = emptyList()

    init {
        viewModelScope.launch {
            try {
                val sports = repo.getSports()
                state = state.copy(sports = sports)
            } catch (_: Exception) {}
        }
    }

    fun load(sportIds: List<String> = emptyList()) {
        viewModelScope.launch {
            val currentSports = state.sports
            val currentSlug = state.selectedSportSlug

            // Keep selectedSportSlug — don't reset
            state = state.copy(
                news = UiState.Loading,
                tournaments = UiState.Loading,
                people = UiState.Loading
            )

            val sportsDeferred = async {
                if (currentSports.isNotEmpty()) currentSports
                else try { repo.getSports() } catch (_: Exception) { emptyList() }
            }

            val newsDeferred = async {
                try { repo.getRecentArticles(20) }
                catch (_: Exception) { emptyList() }
            }

            val tournamentsDeferred = async {
                try { repo.getUpcomingTournaments(20) }
                catch (_: Exception) { emptyList() }
            }

            val peopleDeferred = async {
                try { repo.getAthletes().take(20) }
                catch (_: Exception) { emptyList() }
            }

            val sports = sportsDeferred.await()
            allTournaments = tournamentsDeferred.await()
            allNews = newsDeferred.await()
            allPeople = peopleDeferred.await()

            state = state.copy(
                sports = sports,
                news = UiState.Success(allNews),
                tournaments = UiState.Success(allTournaments),
                people = UiState.Success(allPeople),
                selectedSportSlug = currentSlug
            )

            // Re-apply filter if one was selected
            if (currentSlug != null) {
                applyFilter()
            }
        }
    }

    fun filterBySport(slug: String?) {
        state = state.copy(selectedSportSlug = slug)
        applyFilter()
    }

    private fun applyFilter() {
        val slug = state.selectedSportSlug

        if (slug == null) {
            // Show all
            state = state.copy(
                tournaments = UiState.Success(allTournaments),
                news = UiState.Success(allNews),
                people = UiState.Success(allPeople)
            )
            return
        }

        // Find sportId by slug (check both slug field and lowercased name)
        val sportId = state.sports.find {
            (it.slug ?: it.name.lowercase()) == slug || it.name.lowercase() == slug
        }?.id

        if (sportId == null) {
            // Slug not found — show all
            state = state.copy(
                tournaments = UiState.Success(allTournaments),
                news = UiState.Success(allNews),
                people = UiState.Success(allPeople)
            )
            return
        }

        val filteredTournaments = allTournaments.filter { it.sportId == sportId }

        val filteredNews = allNews.filter { it.sportId == sportId }

        // People: show those with matching sport OR all if userSports is null/empty
        val filteredPeople = allPeople.filter { p ->
            val sports = p.userSports
            if (sports.isNullOrEmpty()) false
            else sports.any { it.sports?.id == sportId }
        }

        state = state.copy(
            tournaments = UiState.Success(filteredTournaments),
            news = UiState.Success(filteredNews),
            people = UiState.Success(filteredPeople)
        )
    }
}
