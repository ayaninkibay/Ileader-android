package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ResultDto
import com.ileader.app.data.remote.dto.TeamDto
import com.ileader.app.data.remote.dto.TeamMemberDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class TeamDetailData(
    val team: TeamDto,
    val members: List<TeamMemberDto>,
    val tournaments: List<TournamentWithCountsDto>,
    val results: List<ResultDto> = emptyList()
) {
    val activeTournaments: List<TournamentWithCountsDto>
        get() = tournaments.filter { it.status == "in_progress" || it.status == "check_in" }

    val upcomingTournaments: List<TournamentWithCountsDto>
        get() = tournaments.filter { it.status == "registration_open" || it.status == "registration_closed" }

    val completedTournaments: List<TournamentWithCountsDto>
        get() = tournaments.filter { it.status == "completed" }
}

class TeamDetailViewModel : ViewModel() {
    private val repo = ViewerRepository()

    var state by mutableStateOf<UiState<TeamDetailData>>(UiState.Loading)
        private set

    fun load(teamId: String) {
        viewModelScope.launch {
            state = UiState.Loading
            try {
                val teamDef = async { repo.getTeamDetail(teamId) }
                val membersDef = async { repo.getTeamMembers(teamId) }
                val tournamentIdsDef = async { repo.getTeamTournamentIds(teamId) }

                val team = teamDef.await()
                val members = membersDef.await()
                val tournamentIds = tournamentIdsDef.await()

                val tournaments = if (tournamentIds.isNotEmpty()) {
                    try { repo.getTournamentsByIds(tournamentIds) } catch (_: Exception) { emptyList() }
                } else emptyList()

                // Fetch results for all team members
                val memberIds = members.mapNotNull { it.userId }
                val results = if (memberIds.isNotEmpty()) {
                    try { repo.getTeamMemberResults(memberIds, 10) } catch (_: Exception) { emptyList() }
                } else emptyList()

                state = UiState.Success(TeamDetailData(team, members, tournaments, results))
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
