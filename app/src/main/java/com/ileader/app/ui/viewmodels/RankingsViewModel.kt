package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.CommunityProfileDto
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.UserSportStatsDto
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class RankingEntry(
    val rank: Int,
    val athleteId: String,
    val name: String,
    val city: String?,
    val avatarUrl: String?,
    val sportName: String?,
    val totalPoints: Int,
    val tournaments: Int,
    val wins: Int,
    val podiums: Int,
    val rating: Int,
    val isCurrentUser: Boolean = false
)

data class RankingsState(
    val entries: UiState<List<RankingEntry>> = UiState.Loading,
    val sports: List<SportDto> = emptyList(),
    val selectedSportId: String? = null,
    val totalAthletes: Int = 0,
    val maxPoints: Int = 0,
    val totalTournaments: Int = 0
)

class RankingsViewModel : ViewModel() {

    private val repo = ViewerRepository()

    var state by mutableStateOf(RankingsState())
        private set

    private var allStats: List<UserSportStatsDto> = emptyList()
    private var profiles: Map<String, CommunityProfileDto> = emptyMap()
    private var currentUserId: String? = null

    fun init(userId: String?) {
        currentUserId = userId
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val sportsDeferred = async { repo.getSports() }
                val athletesDeferred = async { repo.getAthletes() }

                val sports = sportsDeferred.await()
                val athletes = athletesDeferred.await()

                profiles = athletes.associateBy { it.id }

                // Build ranking from user_sports data in athlete profiles
                val entries = buildRanking(athletes, state.selectedSportId)

                state = state.copy(
                    sports = sports,
                    entries = UiState.Success(entries),
                    totalAthletes = athletes.size,
                    maxPoints = entries.maxOfOrNull { it.rating } ?: 0,
                    totalTournaments = entries.sumOf { it.tournaments }
                )
            } catch (e: Exception) {
                state = state.copy(entries = UiState.Error(e.message ?: "Ошибка загрузки"))
            }
        }
    }

    fun selectSport(sportId: String?) {
        state = state.copy(selectedSportId = sportId)
        val athletes = profiles.values.toList()
        val entries = buildRanking(athletes, sportId)
        state = state.copy(
            entries = UiState.Success(entries),
            maxPoints = entries.maxOfOrNull { it.rating } ?: 0,
            totalTournaments = entries.sumOf { it.tournaments }
        )
    }

    fun retry() { loadData() }

    private fun buildRanking(
        athletes: List<CommunityProfileDto>,
        sportId: String?
    ): List<RankingEntry> {
        data class AthleteRank(
            val id: String,
            val name: String,
            val city: String?,
            val avatarUrl: String?,
            val sportName: String?,
            val rating: Int,
            val tournaments: Int,
            val wins: Int,
            val podiums: Int,
            val totalPoints: Int
        )

        val ranked = athletes.mapNotNull { athlete ->
            val sports = athlete.userSports ?: return@mapNotNull null
            val relevantSport = if (sportId != null) {
                sports.find { it.sports?.id == sportId }
            } else {
                sports.maxByOrNull { it.rating }
            } ?: return@mapNotNull null

            AthleteRank(
                id = athlete.id,
                name = athlete.name ?: "—",
                city = athlete.city,
                avatarUrl = athlete.avatarUrl,
                sportName = relevantSport.sports?.name,
                rating = relevantSport.rating,
                tournaments = 0,
                wins = 0,
                podiums = 0,
                totalPoints = relevantSport.rating
            )
        }.sortedByDescending { it.rating }

        return ranked.mapIndexed { index, a ->
            RankingEntry(
                rank = index + 1,
                athleteId = a.id,
                name = a.name,
                city = a.city,
                avatarUrl = a.avatarUrl,
                sportName = a.sportName,
                totalPoints = a.totalPoints,
                tournaments = a.tournaments,
                wins = a.wins,
                podiums = a.podiums,
                rating = a.rating,
                isCurrentUser = a.id == currentUserId
            )
        }
    }
}
