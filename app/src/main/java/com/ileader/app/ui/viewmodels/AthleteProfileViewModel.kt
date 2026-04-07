package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class AthleteProfileData(
    val profile: ProfileDto,
    val sports: List<UserSportDto>,
    val stats: List<UserSportStatsDto>,
    val results: List<ResultDto>,
    val membership: TeamMembershipDto?,
    val teamDetail: TeamDto? = null,
    val teamMembers: List<TeamMemberDto> = emptyList(),
    val upcomingTournaments: List<TournamentWithCountsDto>,
    val license: LicenseDto?,
    val goals: List<GoalDto>
)

class AthleteProfileViewModel : ViewModel() {
    private val repo = ViewerRepository()

    var state by mutableStateOf<UiState<AthleteProfileData>>(UiState.Loading)
        private set

    fun load(userId: String) {
        viewModelScope.launch {
            state = UiState.Loading
            try {
                val profileDef = async { repo.getPublicProfile(userId) }
                val sportsDef = async { repo.getUserSports(userId) }
                val statsDef = async { repo.getUserSportStats(userId) }
                val resultsDef = async { repo.getAthleteResults(userId, 10) }
                val memberDef = async { repo.getAthleteMembership(userId) }
                val tournamentsDef = async { repo.getUserTournaments(userId, 10) }
                val licenseDef = async { repo.getUserLicense(userId) }
                val goalsDef = async { repo.getUserGoals(userId) }

                val membership = memberDef.await()

                // Load team details if athlete has a team
                val teamDetail = if (membership?.teamId != null) {
                    try { repo.getTeamDetail(membership.teamId) } catch (_: Exception) { null }
                } else null
                val teamMembers = if (membership?.teamId != null) {
                    try { repo.getTeamMembers(membership.teamId) } catch (_: Exception) { emptyList() }
                } else emptyList()

                state = UiState.Success(
                    AthleteProfileData(
                        profile = profileDef.await(),
                        sports = sportsDef.await(),
                        stats = statsDef.await(),
                        results = resultsDef.await(),
                        membership = membership,
                        teamDetail = teamDetail,
                        teamMembers = teamMembers,
                        upcomingTournaments = tournamentsDef.await(),
                        license = licenseDef.await(),
                        goals = goalsDef.await()
                    )
                )
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Ошибка загрузки профиля")
            }
        }
    }
}
