package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.models.AthleteGoal
import com.ileader.app.data.models.RefereeTournament
import com.ileader.app.data.models.Tournament
import com.ileader.app.data.models.UserRole
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.MediaInviteFullDto
import com.ileader.app.data.remote.dto.SpectatorDto
import com.ileader.app.data.remote.dto.TournamentHelperDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.AthleteRepository
import com.ileader.app.data.repository.HelperRepository
import com.ileader.app.data.repository.MediaRepository
import com.ileader.app.data.repository.OrganizerRepository
import com.ileader.app.data.repository.RefereeRepository
import com.ileader.app.data.repository.TrainerRepository
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyTournamentsViewModel : ViewModel() {
    private val helperRepo = HelperRepository()

    private val _roleTournaments = MutableStateFlow<UiState<List<Any>>>(UiState.Loading)
    val roleTournaments: StateFlow<UiState<List<Any>>> = _roleTournaments

    private val _goals = MutableStateFlow<UiState<List<AthleteGoal>>?>(null)
    val goals: StateFlow<UiState<List<AthleteGoal>>?> = _goals

    private val _helperAssignments = MutableStateFlow<UiState<List<TournamentHelperDto>>>(UiState.Loading)
    val helperAssignments: StateFlow<UiState<List<TournamentHelperDto>>> = _helperAssignments

    fun load(userId: String, role: UserRole) {
        viewModelScope.launch {
            _roleTournaments.value = UiState.Loading
            _helperAssignments.value = UiState.Loading

            val helperDeferred = async {
                try {
                    helperRepo.getMyAssignments(userId)
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val roleDeferred = async {
                try {
                    when (role) {
                        UserRole.USER, UserRole.SPONSOR, UserRole.ADMIN, UserRole.CONTENT_MANAGER -> {
                            ViewerRepository().getMySpectatorRegistrations(userId)
                        }
                        UserRole.ATHLETE -> {
                            AthleteRepository().getMyTournaments(userId)
                        }
                        UserRole.TRAINER -> {
                            TrainerRepository().getMyTeams(userId)
                        }
                        UserRole.ORGANIZER -> {
                            OrganizerRepository().getMyTournaments(userId)
                        }
                        UserRole.REFEREE -> {
                            RefereeRepository().getAssignedTournaments(userId)
                        }
                        UserRole.MEDIA -> {
                            MediaRepository().getMediaInvites(userId)
                        }
                    }
                } catch (e: Exception) {
                    throw e
                }
            }

            // Load goals for athlete
            if (role == UserRole.ATHLETE) {
                _goals.value = UiState.Loading
                launch {
                    try {
                        val goalsList = AthleteRepository().getGoals(userId)
                        _goals.value = UiState.Success(goalsList)
                    } catch (e: Exception) {
                        _goals.value = UiState.Error(e.message ?: "Ошибка загрузки целей")
                    }
                }
            }

            try {
                val data = roleDeferred.await()
                @Suppress("UNCHECKED_CAST")
                _roleTournaments.value = UiState.Success(data as List<Any>)
            } catch (e: Exception) {
                _roleTournaments.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }

            try {
                val helpers = helperDeferred.await()
                _helperAssignments.value = UiState.Success(helpers)
            } catch (e: Exception) {
                _helperAssignments.value = UiState.Success(emptyList())
            }
        }
    }
}
