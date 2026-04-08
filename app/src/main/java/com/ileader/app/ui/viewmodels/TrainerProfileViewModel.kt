package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.repository.TrainerRepository
import com.ileader.app.data.repository.TrainerTeamData
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class TrainerProfileData(
    val profile: ProfileDto,
    val sports: List<UserSportDto>,
    val team: TrainerTeamData?,
    val teamStats: List<UserSportStatsDto>,
    val upcomingTournaments: List<TournamentWithCountsDto>,
    val results: List<ResultDto>
) {
    val primarySportName: String
        get() = sports.firstOrNull { it.isPrimary }?.sports?.name
            ?: sports.firstOrNull()?.sports?.name ?: ""

    val athleteCount: Int get() = team?.members?.size ?: 0
    val totalTournaments: Int get() = teamStats.sumOf { it.tournaments }
    val totalWins: Int get() = teamStats.sumOf { it.wins }
    val totalPodiums: Int get() = teamStats.sumOf { it.podiums }
    val avgRating: Int get() = if (teamStats.isNotEmpty()) teamStats.map { it.rating }.average().toInt() else 0

    val activeTournaments: List<TournamentWithCountsDto>
        get() = upcomingTournaments.filter { it.status == "in_progress" || it.status == "check_in" }

    val upcomingOnly: List<TournamentWithCountsDto>
        get() = upcomingTournaments.filter { it.status == "registration_open" || it.status == "registration_closed" }
}

class TrainerProfileViewModel : ViewModel() {
    private val viewerRepo = ViewerRepository()
    private val trainerRepo = TrainerRepository()

    var state by mutableStateOf<UiState<TrainerProfileData>>(UiState.Loading)
        private set

    fun load(trainerId: String) {
        viewModelScope.launch {
            state = UiState.Loading
            try {
                val profileDef = async { viewerRepo.getPublicProfile(trainerId) }
                val sportsDef = async { viewerRepo.getUserSports(trainerId) }
                val teamsDef = async { trainerRepo.getMyTeams(trainerId) }
                val tournamentsDef = async { viewerRepo.getUserTournaments(trainerId, 20) }
                val resultsDef = async { viewerRepo.getAthleteResults(trainerId, 10) }

                val team = teamsDef.await().firstOrNull()
                val teamStats = if (team != null) {
                    try { trainerRepo.getTeamStatistics(team.id) } catch (_: Exception) { emptyList() }
                } else emptyList()

                state = UiState.Success(
                    TrainerProfileData(
                        profile = profileDef.await(),
                        sports = sportsDef.await(),
                        team = team,
                        teamStats = teamStats,
                        upcomingTournaments = tournamentsDef.await(),
                        results = resultsDef.await()
                    )
                )
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Ошибка загрузки профиля тренера")
            }
        }
    }
}
