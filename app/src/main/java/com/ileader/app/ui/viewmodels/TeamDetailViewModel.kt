package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TeamDto
import com.ileader.app.data.remote.dto.TeamMemberDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.ViewerRepository
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
        // Mock team IDs — show mock data
        if (!teamId.contains("-")) {
            state = UiState.Success(mockTeamData(teamId))
            return
        }

        viewModelScope.launch {
            state = UiState.Loading
            try {
                val team = try { repo.getTeamDetail(teamId) } catch (e: Exception) {
                    state = UiState.Error("Команда не найдена"); return@launch
                }
                val members = try { repo.getTeamMembers(teamId) } catch (_: Exception) { emptyList() }
                val tournamentIds = try { repo.getTeamTournamentIds(teamId) } catch (_: Exception) { emptyList() }

                val tournaments = if (tournamentIds.isNotEmpty()) {
                    try { repo.getTournamentsByIds(tournamentIds) } catch (_: Exception) { emptyList() }
                } else emptyList()

                state = UiState.Success(TeamDetailData(team, members, tournaments))
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    private fun mockTeamData(teamId: String): TeamDetailData {
        val isFirst = teamId == "1"
        return TeamDetailData(
            team = TeamDto(
                id = teamId,
                name = if (isFirst) "Red Racers" else "Storm Eagles",
                description = if (isFirst) "Профессиональная картинг-команда, многократные чемпионы Казахстана" else "Молодая амбициозная команда из Астаны",
                city = if (isFirst) "Алматы" else "Астана",
                foundedYear = if (isFirst) 2019 else 2022,
                sports = SportDto(id = "", name = "Картинг")
            ),
            members = emptyList(),
            tournaments = emptyList()
        )
    }
}
