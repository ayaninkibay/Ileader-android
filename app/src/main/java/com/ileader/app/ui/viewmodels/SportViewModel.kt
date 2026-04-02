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

class SportViewModel : ViewModel() {

    private val repo = ViewerRepository()

    var state by mutableStateOf(SportState())
        private set

    // Full loaded lists (before client-side filtering)
    private var allTournaments: List<TournamentWithCountsDto> = emptyList()
    private var allPeople: List<CommunityProfileDto> = emptyList()
    private var allNews: List<ArticleDto> = emptyList()

    // Track which role each person belongs to
    private var athleteIds: Set<String> = emptySet()
    private var trainerIds: Set<String> = emptySet()
    private var refereeIds: Set<String> = emptySet()

    // Pagination offsets
    private var tournamentOffset = 0
    private var peopleOffset = 0
    private var newsOffset = 0

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val sportsDeferred = async { repo.getSports() }
                val tournamentsDeferred = async { repo.getPublicTournaments() }
                val athletesDeferred = async { repo.getAthletes() }
                val trainersDeferred = async { repo.getTrainers() }
                val refereesDeferred = async { repo.getReferees() }
                val articlesDeferred = async { repo.getPublishedArticles(PAGE_SIZE) }

                val sports = sportsDeferred.await()
                val tournaments = tournamentsDeferred.await()
                val athletes = athletesDeferred.await()
                val trainers = trainersDeferred.await()
                val referees = refereesDeferred.await()
                val articles = articlesDeferred.await()

                athleteIds = athletes.map { it.id }.toSet()
                trainerIds = trainers.map { it.id }.toSet()
                refereeIds = referees.map { it.id }.toSet()

                val people = (athletes + trainers + referees).distinctBy { it.id }

                allTournaments = tournaments
                allPeople = people
                allNews = articles

                tournamentOffset = tournaments.size
                peopleOffset = people.size
                newsOffset = articles.size

                state = state.copy(
                    sports = sports,
                    tournaments = UiState.Success(tournaments),
                    people = UiState.Success(people),
                    news = UiState.Success(articles),
                    hasMoreTournaments = false,
                    hasMorePeople = false,
                    hasMoreNews = articles.size >= PAGE_SIZE
                )
            } catch (e: Exception) {
                val msg = e.message ?: "Ошибка загрузки"
                state = state.copy(
                    tournaments = UiState.Error(msg),
                    people = UiState.Error(msg),
                    news = UiState.Error(msg)
                )
            }
        }
    }

    fun search(query: String) {
        state = state.copy(searchQuery = query)
        applyClientFilters()
    }

    fun setTab(tab: SportSubTab) {
        state = state.copy(activeTab = tab)
    }

    fun applyFilters(filters: SportFilterState) {
        state = state.copy(filters = filters)
        applyClientFilters()
    }

    fun resetFilters() {
        state = state.copy(filters = SportFilterState())
        applyClientFilters()
    }

    fun loadMore() {
        when (state.activeTab) {
            SportSubTab.TOURNAMENTS -> loadMoreTournaments()
            SportSubTab.PEOPLE -> loadMorePeople()
            SportSubTab.NEWS -> loadMoreNews()
            SportSubTab.LEAGUES -> { /* mock data, no pagination */ }
        }
    }

    fun retry() {
        loadInitialData()
    }

    private fun loadMoreTournaments() {
        viewModelScope.launch {
            try {
                val more = repo.getPublicTournaments()
                // The repo returns up to 100; we simulate pagination client-side
                // by tracking what we've shown
                if (more.size <= tournamentOffset) {
                    state = state.copy(hasMoreTournaments = false)
                    return@launch
                }
                state = state.copy(hasMoreTournaments = false)
            } catch (_: Exception) { }
        }
    }

    private fun loadMorePeople() {
        viewModelScope.launch {
            try {
                val athletes = repo.getAthletes()
                val trainers = repo.getTrainers()
                val referees = repo.getReferees()
                val all = (athletes + trainers + referees).distinctBy { it.id }
                if (all.size <= peopleOffset) {
                    state = state.copy(hasMorePeople = false)
                    return@launch
                }
                state = state.copy(hasMorePeople = false)
            } catch (_: Exception) { }
        }
    }

    private fun loadMoreNews() {
        viewModelScope.launch {
            try {
                val more = repo.getPublishedArticles(newsOffset + PAGE_SIZE)
                val newItems = more.drop(newsOffset)
                if (newItems.isEmpty()) {
                    state = state.copy(hasMoreNews = false)
                    return@launch
                }
                allNews = allNews + newItems
                newsOffset = allNews.size
                state = state.copy(
                    hasMoreNews = newItems.size >= PAGE_SIZE
                )
                applyClientFilters()
            } catch (_: Exception) { }
        }
    }

    private fun applyClientFilters() {
        val query = state.searchQuery.trim()
        val filters = state.filters

        // Filter tournaments
        val filteredTournaments = allTournaments.filter { t ->
            val matchesQuery = query.isEmpty() || t.name.contains(query, ignoreCase = true)
            val matchesSport = filters.sportId == null || t.sportId == filters.sportId
            val matchesStatus = filters.status == null || t.status == filters.status
            val matchesCity = filters.city.isNullOrBlank() ||
                    (t.locationName?.contains(filters.city, ignoreCase = true) == true) ||
                    (t.region?.contains(filters.city, ignoreCase = true) == true)
            val matchesAge = filters.ageCategory == null || t.ageCategory == filters.ageCategory
            matchesQuery && matchesSport && matchesStatus && matchesCity && matchesAge
        }

        // Filter people
        val filteredPeople = allPeople.filter { p ->
            val matchesQuery = query.isEmpty() || (p.name?.contains(query, ignoreCase = true) == true)
            val matchesSport = filters.sportId == null ||
                    p.userSports?.any { it.sports?.id == filters.sportId } == true
            val matchesCity = filters.city.isNullOrBlank() ||
                    (p.city?.contains(filters.city, ignoreCase = true) == true)
            val matchesRole = filters.role == null || when (filters.role) {
                "athlete" -> p.id in athleteIds
                "trainer" -> p.id in trainerIds
                "referee" -> p.id in refereeIds
                else -> true
            }
            matchesQuery && matchesSport && matchesCity && matchesRole
        }

        // Filter news
        val filteredNews = allNews.filter { a ->
            val matchesQuery = query.isEmpty() || a.title.contains(query, ignoreCase = true)
            val matchesSport = filters.sportId == null || a.sportId == filters.sportId
            val matchesCategory = filters.category == null || a.category == filters.category
            matchesQuery && matchesSport && matchesCategory
        }

        state = state.copy(
            tournaments = UiState.Success(filteredTournaments),
            people = UiState.Success(filteredPeople),
            news = UiState.Success(filteredNews)
        )
    }

    companion object {
        private const val PAGE_SIZE = 50
    }

    // ── Data classes ──

    data class SportState(
        val activeTab: SportSubTab = SportSubTab.TOURNAMENTS,
        val searchQuery: String = "",
        val filters: SportFilterState = SportFilterState(),
        val tournaments: UiState<List<TournamentWithCountsDto>> = UiState.Loading,
        val people: UiState<List<CommunityProfileDto>> = UiState.Loading,
        val news: UiState<List<ArticleDto>> = UiState.Loading,
        val sports: List<SportDto> = emptyList(),
        val hasMoreTournaments: Boolean = true,
        val hasMorePeople: Boolean = true,
        val hasMoreNews: Boolean = true
    )

    enum class SportSubTab { TOURNAMENTS, PEOPLE, NEWS, LEAGUES }

    data class SportFilterState(
        val sportId: String? = null,
        val status: String? = null,
        val city: String? = null,
        val ageCategory: String? = null,
        val role: String? = null,
        val category: String? = null
    )
}
