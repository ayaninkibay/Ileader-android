package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.bracket.BracketGenerator
import com.ileader.app.data.bracket.BracketGeneratorOptions
import com.ileader.app.data.bracket.BracketParticipant
import com.ileader.app.data.models.MatchGame
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.repository.AthleteRepository
import com.ileader.app.data.repository.HelperRepository
import com.ileader.app.data.repository.OrganizerRepository
import com.ileader.app.data.repository.TrainerRepository
import com.ileader.app.data.repository.TrainerTeamData
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class HomeTournamentDetailData(
    val tournament: TournamentDto,
    val participants: List<ParticipantDto>,
    val results: List<ResultDto>,
    val bracket: List<BracketMatchDto>,
    val groups: List<TournamentGroupDto> = emptyList(),
    val sponsors: List<TournamentSponsorshipDto> = emptyList(),
    val articles: List<ArticleDto> = emptyList(),
    val referees: List<RefereeAssignmentDto> = emptyList()
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
    private val organizerRepo = OrganizerRepository()
    private val helperRepo = HelperRepository()
    private val trainerRepo = TrainerRepository()

    var trainerTeams by mutableStateOf<List<TrainerTeamData>>(emptyList())
        private set

    var registeredTeamIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var teamRegLoading by mutableStateOf(false)
        private set

    fun loadTrainerTeams(userId: String, tournamentId: String, sportId: String?) {
        viewModelScope.launch {
            try {
                val all = trainerRepo.getMyTeams(userId)
                trainerTeams = if (sportId != null) all.filter { it.sportId == sportId } else all
                // Check which teams are already registered
                val registered = mutableSetOf<String>()
                trainerTeams.forEach { team ->
                    try {
                        val ids = trainerRepo.getTeamRegisteredTournamentIds(team.id)
                        if (tournamentId in ids) registered.add(team.id)
                    } catch (_: Exception) {}
                }
                registeredTeamIds = registered
            } catch (_: Exception) {
                trainerTeams = emptyList()
            }
        }
    }

    fun registerTeamForTournament(tournamentId: String, teamId: String, athleteIds: List<String>) {
        viewModelScope.launch {
            teamRegLoading = true
            try {
                trainerRepo.registerTeamForTournament(tournamentId, teamId, athleteIds)
                snackbarMessage = "Команда зарегистрирована (${athleteIds.size} атлетов)"
                registeredTeamIds = registeredTeamIds + teamId
                load(tournamentId)
            } catch (e: Exception) {
                snackbarMessage = e.message ?: "Ошибка регистрации команды"
            } finally {
                teamRegLoading = false
            }
        }
    }

    fun unregisterTeamFromTournament(tournamentId: String, teamId: String) {
        viewModelScope.launch {
            teamRegLoading = true
            try {
                trainerRepo.unregisterTeamFromTournament(tournamentId, teamId)
                snackbarMessage = "Команда снята с турнира"
                registeredTeamIds = registeredTeamIds - teamId
                load(tournamentId)
            } catch (e: Exception) {
                snackbarMessage = e.message ?: "Ошибка"
            } finally {
                teamRegLoading = false
            }
        }
    }

    var isHelper by mutableStateOf(false)
        private set

    fun checkHelperStatus(tournamentId: String, userId: String) {
        viewModelScope.launch {
            isHelper = try { helperRepo.isHelperForTournament(userId, tournamentId) } catch (_: Exception) { false }
        }
    }

    var state by mutableStateOf<UiState<HomeTournamentDetailData>>(UiState.Loading)
        private set

    var registrationState by mutableStateOf<RegistrationState>(RegistrationState.Idle)
        private set

    var actionLoading by mutableStateOf(false)
        private set

    var participantActionId by mutableStateOf<String?>(null)
        private set

    var statusActionLoading by mutableStateOf(false)
        private set

    var snackbarMessage by mutableStateOf<String?>(null)
        private set

    fun clearSnackbar() { snackbarMessage = null }

    fun load(tournamentId: String) {
        viewModelScope.launch {
            state = UiState.Loading
            try {
                val tournamentDef = async { viewerRepo.getTournamentDetail(tournamentId) }
                val participantsDef = async { viewerRepo.getTournamentParticipants(tournamentId) }
                val resultsDef = async { viewerRepo.getTournamentResults(tournamentId) }
                val bracketDef = async { viewerRepo.getTournamentBracket(tournamentId) }
                val groupsDef = async { viewerRepo.getTournamentGroups(tournamentId) }
                val sponsorsDef = async { try { viewerRepo.getTournamentSponsors(tournamentId) } catch (_: Exception) { emptyList() } }
                val articlesDef = async { try { viewerRepo.getTournamentArticles(tournamentId) } catch (_: Exception) { emptyList() } }
                val refereesDef = async { try { viewerRepo.getTournamentReferees(tournamentId) } catch (_: Exception) { emptyList() } }

                state = UiState.Success(
                    HomeTournamentDetailData(
                        tournament = tournamentDef.await(),
                        participants = participantsDef.await(),
                        results = resultsDef.await(),
                        bracket = bracketDef.await(),
                        groups = groupsDef.await(),
                        sponsors = sponsorsDef.await(),
                        articles = articlesDef.await(),
                        referees = refereesDef.await()
                    )
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

    // ── Organizer: Participant Management ──

    fun approveParticipant(tournamentId: String, athleteId: String) {
        viewModelScope.launch {
            participantActionId = athleteId
            try {
                organizerRepo.approveParticipant(tournamentId, athleteId)
                snackbarMessage = "Участник подтверждён"
                load(tournamentId)
            } catch (e: Exception) {
                snackbarMessage = e.message ?: "Ошибка подтверждения"
            } finally {
                participantActionId = null
            }
        }
    }

    fun declineParticipant(tournamentId: String, athleteId: String) {
        viewModelScope.launch {
            participantActionId = athleteId
            try {
                organizerRepo.declineParticipant(tournamentId, athleteId)
                snackbarMessage = "Участник отклонён"
                load(tournamentId)
            } catch (e: Exception) {
                snackbarMessage = e.message ?: "Ошибка отклонения"
            } finally {
                participantActionId = null
            }
        }
    }

    // ── Organizer: Tournament Status ──

    fun updateTournamentStatus(tournamentId: String, newStatus: String) {
        viewModelScope.launch {
            statusActionLoading = true
            try {
                organizerRepo.updateTournamentStatus(tournamentId, newStatus)
                snackbarMessage = when (newStatus) {
                    "registration_open" -> "Регистрация открыта"
                    "registration_closed" -> "Регистрация закрыта"
                    "check_in" -> "Check-in начат"
                    "in_progress" -> "Турнир начат"
                    "completed" -> "Турнир завершён"
                    "cancelled" -> "Турнир отменён"
                    else -> "Статус обновлён"
                }
                load(tournamentId)
            } catch (e: Exception) {
                snackbarMessage = e.message ?: "Ошибка смены статуса"
            } finally {
                statusActionLoading = false
            }
        }
    }

    // ── Organizer: Delete Tournament ──

    fun deleteTournament(tournamentId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            statusActionLoading = true
            try {
                organizerRepo.deleteTournament(tournamentId)
                snackbarMessage = "Турнир удалён"
                onDeleted()
            } catch (e: Exception) {
                snackbarMessage = e.message ?: "Ошибка удаления"
            } finally {
                statusActionLoading = false
            }
        }
    }

    // ── Organizer/Referee: Match Result ──

    fun saveMatchResult(tournamentId: String, matchId: String, games: List<MatchGame>, winnerId: String) {
        viewModelScope.launch {
            try {
                val p1Wins = games.count { it.participant1Score > it.participant2Score }
                val p2Wins = games.count { it.participant2Score > it.participant1Score }
                val loserId = (state as? UiState.Success)?.data?.bracket?.find { it.id == matchId }?.let { m ->
                    if (winnerId == m.participant1Id) m.participant2Id else m.participant1Id
                }
                organizerRepo.updateMatch(
                    matchId,
                    MatchResultUpdateDto(
                        participant1Score = p1Wins,
                        participant2Score = p2Wins,
                        games = games.map { g ->
                            MatchGameDto(
                                gameNumber = g.gameNumber,
                                participant1Score = g.participant1Score,
                                participant2Score = g.participant2Score,
                                winnerId = g.winnerId,
                                status = g.status
                            )
                        },
                        winnerId = winnerId,
                        loserId = loserId,
                        status = "completed"
                    )
                )
                snackbarMessage = "Результат сохранён"
                load(tournamentId)
            } catch (e: Exception) {
                snackbarMessage = e.message ?: "Ошибка сохранения результата"
            }
        }
    }

    // ── Organizer: Generate Bracket ──

    var bracketGenerating by mutableStateOf(false)
        private set

    fun generateBracket(tournamentId: String) {
        viewModelScope.launch {
            bracketGenerating = true
            try {
                val data = (state as? UiState.Success)?.data ?: return@launch
                val tournament = data.tournament
                val confirmedParticipants = data.participants.filter {
                    it.status == "confirmed" || it.status == "registered"
                }
                if (confirmedParticipants.size < 2) {
                    snackbarMessage = "Нужно минимум 2 подтверждённых участника"
                    return@launch
                }

                val bracketParticipants = confirmedParticipants.map { p ->
                    BracketParticipant(
                        id = p.athleteId,
                        name = p.profiles?.name ?: "—",
                        seed = p.seed,
                        rating = null
                    )
                }

                val options = BracketGeneratorOptions(
                    tournamentId = tournamentId,
                    format = tournament.format ?: "single_elimination",
                    seedingType = tournament.seedingType ?: "random",
                    matchFormat = tournament.matchFormat ?: "BO1",
                    hasThirdPlaceMatch = tournament.hasThirdPlaceMatch ?: false,
                    groupCount = tournament.groupCount ?: 2
                )

                val result = BracketGenerator.generate(bracketParticipants, options)

                // Convert to insert DTOs
                val matchDtos = result.matches.map { m ->
                    BracketMatchInsertDto(
                        id = m.id,
                        tournamentId = tournamentId,
                        round = m.round,
                        matchNumber = m.matchNumber,
                        bracketType = m.bracketType,
                        participant1Id = m.participant1Id,
                        participant2Id = m.participant2Id,
                        participant1Score = m.participant1Score,
                        participant2Score = m.participant2Score,
                        games = m.games.map { g ->
                            MatchGameDto(g.gameNumber, g.participant1Score, g.participant2Score, g.winnerId, g.status)
                        },
                        winnerId = m.winnerId,
                        loserId = m.loserId,
                        status = m.status,
                        nextMatchId = m.nextMatchId,
                        loserNextMatchId = m.loserNextMatchId,
                        groupId = m.groupId,
                        isBye = m.isBye
                    )
                }
                val groupDtos = result.groups.map { g ->
                    TournamentGroupInsertDto(
                        id = g.id,
                        tournamentId = tournamentId,
                        name = g.name,
                        standings = g.participants.map { s ->
                            GroupStandingDto(
                                participantId = s.participantId,
                                athleteName = s.athleteName,
                                team = s.team,
                                seed = s.seed,
                                wins = s.wins,
                                losses = s.losses,
                                draws = s.draws,
                                points = s.points,
                                gamesPlayed = s.gamesPlayed,
                                position = s.position,
                                qualified = s.qualified
                            )
                        }
                    )
                }

                organizerRepo.saveBracketMatches(tournamentId, matchDtos)
                if (groupDtos.isNotEmpty()) {
                    organizerRepo.saveGroups(tournamentId, groupDtos)
                }

                snackbarMessage = "Сетка сгенерирована (${matchDtos.size} матчей)"
                load(tournamentId)
            } catch (e: Exception) {
                snackbarMessage = e.message ?: "Ошибка генерации сетки"
            } finally {
                bracketGenerating = false
            }
        }
    }

    // ── Organizer: Save Final Results ──

    fun saveFinalResults(tournamentId: String, results: List<ResultInsertDto>) {
        viewModelScope.launch {
            statusActionLoading = true
            try {
                organizerRepo.saveResults(results)
                snackbarMessage = "Результаты сохранены"
                load(tournamentId)
            } catch (e: Exception) {
                snackbarMessage = e.message ?: "Ошибка сохранения результатов"
            } finally {
                statusActionLoading = false
            }
        }
    }

    fun revertMatch(tournamentId: String, matchId: String) {
        viewModelScope.launch {
            try {
                organizerRepo.updateMatch(
                    matchId,
                    MatchResultUpdateDto(
                        participant1Score = 0,
                        participant2Score = 0,
                        games = null,
                        winnerId = null,
                        loserId = null,
                        status = "scheduled"
                    )
                )
                snackbarMessage = "Результат отменён"
                load(tournamentId)
            } catch (e: Exception) {
                snackbarMessage = e.message ?: "Ошибка отката"
            }
        }
    }
}
