package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.repository.AthleteRepository
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.launch

data class HomeTournamentDetailData(
    val tournament: TournamentDto,
    val participants: List<ParticipantDto>,
    val results: List<ResultDto>,
    val bracket: List<BracketMatchDto>,
    val groups: List<TournamentGroupDto> = emptyList()
)

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    object RegisteredAsParticipant : RegistrationState()
    object RegisteredAsSpectator : RegistrationState()
    object NotRegistered : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

class TournamentDetailViewModel : ViewModel() {
    private val viewerRepo = ViewerRepository()
    private val athleteRepo = AthleteRepository()

    var state by mutableStateOf<UiState<HomeTournamentDetailData>>(UiState.Loading)
        private set

    var registrationState by mutableStateOf<RegistrationState>(RegistrationState.Idle)
        private set

    var actionLoading by mutableStateOf(false)
        private set

    fun load(tournamentId: String) {
        viewModelScope.launch {
            state = UiState.Loading
            try {
                val tournament = viewerRepo.getTournamentDetail(tournamentId)
                val participants = viewerRepo.getTournamentParticipants(tournamentId)
                val results = viewerRepo.getTournamentResults(tournamentId)
                val bracket = viewerRepo.getTournamentBracket(tournamentId)
                val groups = viewerRepo.getTournamentGroups(tournamentId)

                state = UiState.Success(
                    HomeTournamentDetailData(tournament, participants, results, bracket, groups)
                )
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun checkRegistration(tournamentId: String, userId: String, role: String) {
        viewModelScope.launch {
            registrationState = RegistrationState.Loading
            try {
                if (role in listOf("athlete", "trainer")) {
                    val isParticipant = athleteRepo.getMyParticipation(tournamentId, userId)
                    registrationState = if (isParticipant) {
                        RegistrationState.RegisteredAsParticipant
                    } else {
                        RegistrationState.NotRegistered
                    }
                } else {
                    val spectator = viewerRepo.getMySpectatorRegistration(tournamentId, userId)
                    registrationState = if (spectator != null) {
                        RegistrationState.RegisteredAsSpectator
                    } else {
                        RegistrationState.NotRegistered
                    }
                }
            } catch (_: Exception) {
                registrationState = RegistrationState.NotRegistered
            }
        }
    }

    fun registerAsParticipant(tournamentId: String, userId: String) {
        viewModelScope.launch {
            actionLoading = true
            try {
                athleteRepo.registerForTournament(tournamentId, userId)
                registrationState = RegistrationState.RegisteredAsParticipant
                load(tournamentId) // reload participants
            } catch (e: Exception) {
                registrationState = RegistrationState.Error(e.message ?: "Ошибка регистрации")
            } finally {
                actionLoading = false
            }
        }
    }

    fun registerAsSpectator(tournamentId: String, userId: String) {
        viewModelScope.launch {
            actionLoading = true
            try {
                viewerRepo.registerAsSpectator(tournamentId, userId)
                registrationState = RegistrationState.RegisteredAsSpectator
            } catch (e: Exception) {
                registrationState = RegistrationState.Error(e.message ?: "Ошибка регистрации")
            } finally {
                actionLoading = false
            }
        }
    }

    fun unregister(tournamentId: String, userId: String) {
        viewModelScope.launch {
            actionLoading = true
            try {
                athleteRepo.cancelRegistration(tournamentId, userId)
                registrationState = RegistrationState.NotRegistered
                load(tournamentId)
            } catch (e: Exception) {
                registrationState = RegistrationState.Error(e.message ?: "Ошибка отмены")
            } finally {
                actionLoading = false
            }
        }
    }
}
