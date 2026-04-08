package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.CommunityProfileDto
import com.ileader.app.data.remote.dto.LeagueDto
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TeamWithStatsDto
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
    private var allAthletes: List<CommunityProfileDto> = emptyList()
    private var allTrainers: List<CommunityProfileDto> = emptyList()
    private var allReferees: List<CommunityProfileDto> = emptyList()
    private var allNews: List<ArticleDto> = emptyList()
    private var allTeams: List<TeamWithStatsDto> = emptyList()
    private var allLeagues: List<LeagueDto> = emptyList()

    // Pagination offsets
    private var tournamentOffset = 0
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
                val teamsDeferred = async { repo.getTeams() }
                val leaguesDeferred = async { repo.getLeagues() }

                val sports = sportsDeferred.await()
                val tournaments = tournamentsDeferred.await()
                val athletes = athletesDeferred.await()
                val trainers = trainersDeferred.await()
                val referees = refereesDeferred.await()
                val articles = articlesDeferred.await()
                val teams = teamsDeferred.await()
                val leagues = leaguesDeferred.await()

                allTournaments = tournaments
                allAthletes = athletes
                allTrainers = trainers
                allReferees = referees
                allNews = articles
                allTeams = teams
                allLeagues = leagues

                tournamentOffset = tournaments.size
                newsOffset = articles.size

                state = state.copy(
                    sports = sports,
                    selectedIndices = if (sports.isNotEmpty()) setOf(0) else emptySet(),
                    filters = state.filters.copy(sportId = sports.firstOrNull()?.id),
                    tournaments = UiState.Success(tournaments),
                    athletes = UiState.Success(athletes),
                    trainers = UiState.Success(trainers),
                    referees = UiState.Success(referees),
                    news = UiState.Success(articles),
                    teams = UiState.Success(teams),
                    leagues = UiState.Success(allLeagues),
                    hasMoreTournaments = false,
                    hasMoreNews = articles.size >= PAGE_SIZE
                )
                if (sports.isNotEmpty()) applyClientFilters()
            } catch (e: Exception) {
                val msg = e.message ?: "Ошибка загрузки"
                state = state.copy(
                    tournaments = UiState.Error(msg),
                    athletes = UiState.Error(msg),
                    trainers = UiState.Error(msg),
                    referees = UiState.Error(msg),
                    news = UiState.Error(msg),
                    teams = UiState.Error(msg)
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
        // Sync selectedIndices with sportId
        val idx = if (filters.sportId != null) {
            state.sports.indexOfFirst { it.id == filters.sportId }.takeIf { it >= 0 }
        } else null
        state = state.copy(
            filters = filters,
            selectedIndices = if (idx != null) setOf(idx) else emptySet()
        )
        applyClientFilters()
    }

    fun resetFilters() {
        state = state.copy(
            filters = SportFilterState(),
            selectedIndices = emptySet()
        )
        applyClientFilters()
    }

    fun loadMore() {
        when (state.activeTab) {
            SportSubTab.TOURNAMENTS -> loadMoreTournaments()
            SportSubTab.NEWS -> loadMoreNews()
            else -> { /* no pagination for other tabs */ }
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

        // Multi-sport filter
        val selectedSportIds = state.selectedIndices
            .mapNotNull { state.sports.getOrNull(it)?.id }
            .toSet()
        val selectedSportNames = state.selectedIndices
            .mapNotNull { state.sports.getOrNull(it)?.name }
            .toSet()

        fun matchesPerson(p: CommunityProfileDto): Boolean {
            val matchesQuery = query.isEmpty() || (p.name?.contains(query, ignoreCase = true) == true)
            val matchesSport = selectedSportIds.isEmpty() ||
                    p.userSports?.any { it.sports?.id in selectedSportIds } == true
            val matchesCity = filters.city.isNullOrBlank() ||
                    (p.city?.contains(filters.city, ignoreCase = true) == true)
            return matchesQuery && matchesSport && matchesCity
        }

        // Filter tournaments
        val filteredTournaments = allTournaments.filter { t ->
            val matchesQuery = query.isEmpty() || t.name.contains(query, ignoreCase = true)
            val matchesSport = selectedSportIds.isEmpty() || t.sportId in selectedSportIds
            val matchesStatus = filters.status == null || t.status == filters.status
            val matchesCity = filters.city.isNullOrBlank() ||
                    (t.locationName?.contains(filters.city, ignoreCase = true) == true) ||
                    (t.region?.contains(filters.city, ignoreCase = true) == true)
            val matchesAge = filters.ageCategory == null || t.ageCategory == filters.ageCategory
            matchesQuery && matchesSport && matchesStatus && matchesCity && matchesAge
        }

        // Filter each role separately
        val filteredAthletes = allAthletes.filter { matchesPerson(it) }
        val filteredTrainers = allTrainers.filter { matchesPerson(it) }
        val filteredReferees = allReferees.filter { matchesPerson(it) }

        // Filter news
        val filteredNews = allNews.filter { a ->
            val matchesQuery = query.isEmpty() || a.title.contains(query, ignoreCase = true)
            val matchesSport = selectedSportIds.isEmpty() || a.sportId in selectedSportIds
            val matchesCategory = filters.category == null || a.category == filters.category
            matchesQuery && matchesSport && matchesCategory
        }

        // Filter teams
        val filteredTeams = allTeams.filter { t ->
            val matchesQuery = query.isEmpty() || t.name.contains(query, ignoreCase = true)
            val matchesSport = selectedSportIds.isEmpty() || t.sportId in selectedSportIds
            matchesQuery && matchesSport
        }

        // Filter leagues
        val filteredLeagues = allLeagues.filter { l ->
            val matchesQuery = query.isEmpty() || l.name.contains(query, ignoreCase = true)
            val matchesSport = selectedSportIds.isEmpty() || l.sportId in selectedSportIds
            matchesQuery && matchesSport
        }

        state = state.copy(
            tournaments = UiState.Success(filteredTournaments),
            athletes = UiState.Success(filteredAthletes),
            trainers = UiState.Success(filteredTrainers),
            referees = UiState.Success(filteredReferees),
            news = UiState.Success(filteredNews),
            teams = UiState.Success(filteredTeams),
            leagues = UiState.Success(filteredLeagues)
        )
    }

    // ── Data classes ──

    fun toggleSport(index: Int) {
        val sport = state.sports.getOrNull(index) ?: return
        // Already selected — do nothing (cannot deselect)
        if (index in state.selectedIndices) return
        state = state.copy(
            selectedIndices = setOf(index),
            filters = state.filters.copy(sportId = sport.id)
        )
        applyClientFilters()
    }

    val selectedSports: List<SportDto>
        get() = state.selectedIndices.mapNotNull { state.sports.getOrNull(it) }

    companion object {
        private const val PAGE_SIZE = 50

        fun getFallbackImage(sport: SportDto): String? = when (sport.slug ?: sport.name.lowercase().trim()) {
            "karting", "картинг" -> "https://ileader.kz/img/karting/karting-04-1280x853.jpeg"
            "shooting", "стрельба" -> "https://ileader.kz/img/shooting/shooting-01-1280x853.jpeg"
            "tennis", "теннис" -> "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=800&q=80"
            "football", "soccer", "футбол" -> "https://images.unsplash.com/photo-1431324155629-1a6deb1dec8d?w=800&q=80"
            "boxing", "бокс" -> "https://images.unsplash.com/photo-1549719386-74dfcbf7dbed?w=800&q=80"
            "swimming", "плавание" -> "https://images.unsplash.com/photo-1530549387789-4c1017266635?w=800&q=80"
            "athletics", "атлетика", "лёгкая атлетика", "легкая атлетика" -> "https://images.unsplash.com/photo-1532444458054-01a7dd3e9fca?w=800&q=80"
            "rowing", "гребля" -> "https://clkbmjsmfzjuqdwnoejv.supabase.co/storage/v1/object/public/sport-images/rowing.jpg"
            "strayk-bol", "страйк бол" -> "https://clkbmjsmfzjuqdwnoejv.supabase.co/storage/v1/object/public/sport-images/strikeball.jpg"
            "sportivnoe-rybolovstvo", "спортивное рыболовство" -> "https://images.unsplash.com/photo-1504309092620-4d0ec726efa4?w=800&q=80"
            else -> null
        }
    }

    data class SportState(
        val activeTab: SportSubTab = SportSubTab.TOURNAMENTS,
        val searchQuery: String = "",
        val filters: SportFilterState = SportFilterState(),
        val tournaments: UiState<List<TournamentWithCountsDto>> = UiState.Loading,
        val athletes: UiState<List<CommunityProfileDto>> = UiState.Loading,
        val trainers: UiState<List<CommunityProfileDto>> = UiState.Loading,
        val referees: UiState<List<CommunityProfileDto>> = UiState.Loading,
        val news: UiState<List<ArticleDto>> = UiState.Loading,
        val teams: UiState<List<TeamWithStatsDto>> = UiState.Loading,
        val leagues: UiState<List<LeagueDto>> = UiState.Loading,
        val sports: List<SportDto> = emptyList(),
        val selectedIndices: Set<Int> = emptySet(),
        val sportImages: Map<String, List<String>> = emptyMap(),
        val hasMoreTournaments: Boolean = true,
        val hasMoreNews: Boolean = true
    ) {
        val selectedSports: List<SportDto>
            get() = selectedIndices.mapNotNull { sports.getOrNull(it) }
    }

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
