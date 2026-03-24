package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.bracket.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.repository.OrganizerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TournamentDetailData(
    val tournament: TournamentDto,
    val participants: List<ParticipantDto>,
    val referees: List<RefereeAssignmentDto>,
    val inviteCodes: List<InviteCodeDto>,
    val bracket: List<BracketMatchDto> = emptyList(),
    val groups: List<TournamentGroupDto> = emptyList()
)

class OrganizerTournamentDetailViewModel : ViewModel() {

    private val repo = OrganizerRepository()

    private val _state = MutableStateFlow<UiState<TournamentDetailData>>(UiState.Loading)
    val state: StateFlow<UiState<TournamentDetailData>> = _state.asStateFlow()

    private val _mutationError = MutableStateFlow<String?>(null)
    val mutationError: StateFlow<String?> = _mutationError.asStateFlow()

    private val _generating = MutableStateFlow(false)
    val generating: StateFlow<Boolean> = _generating.asStateFlow()

    fun clearMutationError() { _mutationError.value = null }

    private var currentTournamentId: String? = null

    fun load(tournamentId: String) {
        currentTournamentId = tournamentId
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val tournament = repo.getTournamentDetail(tournamentId)
                val participants = repo.getParticipants(tournamentId)
                val referees = repo.getReferees(tournamentId)
                val inviteCodes = repo.getInviteCodes(tournamentId)
                val bracket = repo.getBracket(tournamentId)
                val groups = repo.getGroups(tournamentId)
                _state.value = UiState.Success(
                    TournamentDetailData(tournament, participants, referees, inviteCodes, bracket, groups)
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun generateBracket(tournamentId: String) {
        val currentState = (_state.value as? UiState.Success) ?: return
        val data = currentState.data
        val tournament = data.tournament
        val confirmed = data.participants.filter { it.status == "confirmed" }

        if (confirmed.size < 2) {
            _mutationError.value = "Нужно минимум 2 подтверждённых участника"
            return
        }

        _generating.value = true
        viewModelScope.launch {
            try {
                val bracketParticipants = confirmed.mapIndexed { index, p ->
                    BracketParticipant(
                        id = p.athleteId,
                        name = p.profiles?.name ?: "Участник ${index + 1}",
                        seed = p.seed
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
                                seed = s.seed
                            )
                        }
                    )
                }

                repo.saveBracketMatches(tournamentId, matchDtos)
                if (groupDtos.isNotEmpty()) {
                    repo.saveGroups(tournamentId, groupDtos)
                }

                load(tournamentId)
            } catch (e: Exception) {
                _mutationError.value = "Ошибка генерации: ${e.message}"
            } finally {
                _generating.value = false
            }
        }
    }

    fun updateMatchResult(matchId: String, update: MatchResultUpdateDto) {
        val tournamentId = currentTournamentId ?: return
        viewModelScope.launch {
            try {
                repo.updateMatch(matchId, update)
                // Auto-advance winner
                val currentState = (_state.value as? UiState.Success) ?: return@launch
                val match = currentState.data.bracket.find { it.id == matchId }
                if (match != null && update.winnerId != null) {
                    advanceWinner(match, update.winnerId, currentState.data.bracket)
                }
                load(tournamentId)
            } catch (e: Exception) {
                _mutationError.value = "Ошибка сохранения: ${e.message}"
            }
        }
    }

    private suspend fun advanceWinner(match: BracketMatchDto, winnerId: String, allMatches: List<BracketMatchDto>) {
        val nextMatchId = match.nextMatchId
        if (nextMatchId != null) {
            val nextMatch = allMatches.find { it.id == nextMatchId }
            if (nextMatch != null) {
                val slot = if (nextMatch.participant1Id == null) 1 else 2
                repo.setMatchParticipant(nextMatchId, slot, winnerId)
            }
        }

        val loserId = if (winnerId == match.participant1Id) match.participant2Id else match.participant1Id
        val loserNextMatchId = match.loserNextMatchId
        if (loserNextMatchId != null && loserId != null) {
            val loserNextMatch = allMatches.find { it.id == loserNextMatchId }
            if (loserNextMatch != null) {
                val slot = if (loserNextMatch.participant1Id == null) 1 else 2
                repo.setMatchParticipant(loserNextMatchId, slot, loserId)
            }
        }
    }

    fun approveParticipant(tournamentId: String, athleteId: String) {
        viewModelScope.launch {
            try {
                repo.approveParticipant(tournamentId, athleteId)
                load(tournamentId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun declineParticipant(tournamentId: String, athleteId: String) {
        viewModelScope.launch {
            try {
                repo.declineParticipant(tournamentId, athleteId)
                load(tournamentId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }

    fun updateStatus(tournamentId: String, status: String) {
        viewModelScope.launch {
            try {
                repo.updateTournamentStatus(tournamentId, status)
                load(tournamentId)
            } catch (e: Exception) {
                _mutationError.value = e.message ?: "Ошибка операции"
            }
        }
    }
}
