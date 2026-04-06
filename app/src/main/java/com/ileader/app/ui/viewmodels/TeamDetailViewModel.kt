package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TeamDto
import com.ileader.app.data.remote.dto.TeamMemberDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class TeamDetailData(
    val team: TeamDto,
    val members: List<TeamMemberDto>,
    val tournaments: List<TournamentWithCountsDto>
)

class TeamDetailViewModel : ViewModel() {
    private val repo = ViewerRepository()

    var state by mutableStateOf<UiState<TeamDetailData>>(UiState.Loading)
        private set

    fun load(teamId: String) {
        viewModelScope.launch {
            state = UiState.Loading
            try {
                val teamDeferred = async { repo.getTeamDetail(teamId) }
                val membersDeferred = async { repo.getTeamMembers(teamId) }
                val tournamentIdsDeferred = async { repo.getTeamTournamentIds(teamId) }

                val team = teamDeferred.await()
                val members = membersDeferred.await()
                val tournamentIds = tournamentIdsDeferred.await()

                val tournaments = if (tournamentIds.isNotEmpty()) {
                    repo.getTournamentsByIds(tournamentIds)
                } else emptyList()

                state = UiState.Success(TeamDetailData(team, members, tournaments))
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
