package com.ileader.app.data.bracket

import com.ileader.app.data.models.*
import com.ileader.app.data.remote.dto.BracketMatchDto
import com.ileader.app.data.remote.dto.ParticipantDto
import com.ileader.app.data.remote.dto.TournamentGroupDto
import com.ileader.app.data.remote.dto.GroupStandingDto

object BracketUtils {

    fun mapDtoToMatch(
        dto: BracketMatchDto,
        participantNames: Map<String, String> = emptyMap(),
        participantSeeds: Map<String, Int> = emptyMap()
    ): BracketMatch = BracketMatch(
        id = dto.id,
        tournamentId = dto.tournamentId,
        round = dto.round,
        matchNumber = dto.matchNumber,
        bracketType = BracketType.fromString(dto.bracketType),
        participant1Id = dto.participant1Id,
        participant2Id = dto.participant2Id,
        participant1Name = dto.participant1Id?.let { participantNames[it] },
        participant2Name = dto.participant2Id?.let { participantNames[it] },
        participant1Seed = dto.participant1Id?.let { participantSeeds[it] },
        participant2Seed = dto.participant2Id?.let { participantSeeds[it] },
        participant1Score = dto.participant1Score,
        participant2Score = dto.participant2Score,
        games = dto.games?.map { g ->
            MatchGame(g.gameNumber, g.participant1Score, g.participant2Score, g.winnerId, g.status)
        } ?: emptyList(),
        winnerId = dto.winnerId,
        loserId = dto.loserId,
        status = MatchStatus.fromString(dto.status),
        nextMatchId = dto.nextMatchId,
        loserNextMatchId = dto.loserNextMatchId,
        groupId = dto.groupId,
        isBye = dto.isBye ?: false,
        scheduledAt = dto.scheduledAt
    )

    fun mapDtosToMatches(
        dtos: List<BracketMatchDto>,
        participants: List<ParticipantDto> = emptyList()
    ): List<BracketMatch> {
        val nameMap = participants.associate { it.athleteId to (it.profiles?.name ?: "") }
        val seedMap = participants.mapNotNull { p -> p.seed?.let { p.athleteId to it } }.toMap()
        return dtos.map { mapDtoToMatch(it, nameMap, seedMap) }
    }

    fun mapGroupDtos(dtos: List<TournamentGroupDto>): List<TournamentGroup> =
        dtos.map { g ->
            TournamentGroup(
                id = g.id,
                tournamentId = g.tournamentId,
                name = g.name,
                participants = g.standings?.map { s ->
                    GroupParticipant(
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
                } ?: emptyList()
            )
        }

    fun getUpperBracketMatches(matches: List<BracketMatch>): List<BracketMatch> =
        matches.filter { it.bracketType == BracketType.UPPER }

    fun getLowerBracketMatches(matches: List<BracketMatch>): List<BracketMatch> =
        matches.filter { it.bracketType == BracketType.LOWER }

    fun getGrandFinalMatch(matches: List<BracketMatch>): BracketMatch? =
        matches.find { it.bracketType == BracketType.GRAND_FINAL }

    fun getThirdPlaceMatch(matches: List<BracketMatch>): BracketMatch? =
        matches.find { it.bracketType == BracketType.THIRD_PLACE }

    fun getGroupMatches(matches: List<BracketMatch>, groupId: String): List<BracketMatch> =
        matches.filter { it.groupId == groupId }

    fun getMaxRound(matches: List<BracketMatch>, bracketType: BracketType? = null): Int =
        matches.filter { bracketType == null || it.bracketType == bracketType }
            .maxOfOrNull { it.round } ?: 0

    fun getRoundName(round: Int, totalRounds: Int): String {
        val fromEnd = totalRounds - round
        return when {
            fromEnd == 0 -> "Финал"
            fromEnd == 1 -> "Полуфинал"
            fromEnd == 2 -> "Четвертьфинал"
            else -> "Раунд $round"
        }
    }

    fun getPlayoffMatches(matches: List<BracketMatch>): List<BracketMatch> =
        matches.filter { it.groupId == null }

    fun getMatchesByRound(matches: List<BracketMatch>): Map<Int, List<BracketMatch>> =
        matches.groupBy { it.round }.toSortedMap()
}
