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

data class LeagueDetailData(
    val league: LeagueDto,
    val stages: List<LeagueStageDto>,
    val standings: List<LeagueStandingDto>,
    val participantCount: Int,
    val completedStages: Int
) {
    val sportName: String get() = league.sports?.name ?: ""
    val organizerName: String get() = league.profiles?.name ?: ""
}

class LeagueDetailViewModel : ViewModel() {
    private val repo = ViewerRepository()

    var state by mutableStateOf<UiState<LeagueDetailData>>(UiState.Loading)
        private set

    fun load(leagueId: String) {
        viewModelScope.launch {
            state = UiState.Loading
            try {
                val leagueDef = async { repo.getLeagueById(leagueId) }
                val stagesDef = async { repo.getLeagueStages(leagueId) }
                val standingsDef = async { repo.getLeagueStandings(leagueId) }
                val participantsDef = async { repo.getLeagueParticipantCount(leagueId) }
                val completedDef = async { repo.getLeagueCompletedStages(leagueId) }

                state = UiState.Success(
                    LeagueDetailData(
                        league = leagueDef.await(),
                        stages = stagesDef.await(),
                        standings = standingsDef.await(),
                        participantCount = participantsDef.await(),
                        completedStages = completedDef.await()
                    )
                )
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Ошибка загрузки лиги")
            }
        }
    }
}
