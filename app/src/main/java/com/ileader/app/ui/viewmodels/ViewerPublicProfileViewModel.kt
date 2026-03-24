package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── Aggregate data for public profiles ──

data class AthletePublicProfileData(
    val profile: ProfileDto,
    val sports: List<UserSportDto>,
    val stats: List<UserSportStatsDto>,
    val recentResults: List<ResultDto>,
    val teamMembership: TeamMembershipDto? = null
)

data class TrainerPublicProfileData(
    val profile: ProfileDto,
    val sports: List<UserSportDto>
)

data class RefereePublicProfileData(
    val profile: ProfileDto,
    val sports: List<UserSportDto>
)

data class TeamPublicProfileData(
    val team: TeamDto,
    val members: List<TeamMemberDto>
)

class ViewerPublicProfileViewModel : ViewModel() {
    private val repo = ViewerRepository()

    // Athlete profile
    private val _athleteState = MutableStateFlow<UiState<AthletePublicProfileData>>(UiState.Loading)
    val athleteState: StateFlow<UiState<AthletePublicProfileData>> = _athleteState

    fun loadAthlete(athleteId: String) {
        viewModelScope.launch {
            _athleteState.value = UiState.Loading
            try {
                val profile = repo.getPublicProfile(athleteId)
                val sports = repo.getUserSports(athleteId)
                val stats = repo.getUserSportStats(athleteId)
                val results = repo.getAthleteResults(athleteId)
                val team = repo.getAthleteMembership(athleteId)

                _athleteState.value = UiState.Success(
                    AthletePublicProfileData(profile, sports, stats, results, team)
                )
            } catch (e: Exception) {
                _athleteState.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    // Trainer profile
    private val _trainerState = MutableStateFlow<UiState<TrainerPublicProfileData>>(UiState.Loading)
    val trainerState: StateFlow<UiState<TrainerPublicProfileData>> = _trainerState

    fun loadTrainer(trainerId: String) {
        viewModelScope.launch {
            _trainerState.value = UiState.Loading
            try {
                val profile = repo.getPublicProfile(trainerId)
                val sports = repo.getUserSports(trainerId)

                _trainerState.value = UiState.Success(
                    TrainerPublicProfileData(profile, sports)
                )
            } catch (e: Exception) {
                _trainerState.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    // Referee profile
    private val _refereeState = MutableStateFlow<UiState<RefereePublicProfileData>>(UiState.Loading)
    val refereeState: StateFlow<UiState<RefereePublicProfileData>> = _refereeState

    fun loadReferee(refereeId: String) {
        viewModelScope.launch {
            _refereeState.value = UiState.Loading
            try {
                val profile = repo.getPublicProfile(refereeId)
                val sports = repo.getUserSports(refereeId)

                _refereeState.value = UiState.Success(
                    RefereePublicProfileData(profile, sports)
                )
            } catch (e: Exception) {
                _refereeState.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    // Team profile
    private val _teamState = MutableStateFlow<UiState<TeamPublicProfileData>>(UiState.Loading)
    val teamState: StateFlow<UiState<TeamPublicProfileData>> = _teamState

    fun loadTeam(teamId: String) {
        viewModelScope.launch {
            _teamState.value = UiState.Loading
            try {
                val team = repo.getTeamDetail(teamId)
                val members = repo.getTeamMembers(teamId)

                _teamState.value = UiState.Success(
                    TeamPublicProfileData(team, members)
                )
            } catch (e: Exception) {
                _teamState.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
