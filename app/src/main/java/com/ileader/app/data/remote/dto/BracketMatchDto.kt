package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchGameDto(
    @SerialName("gameNumber") val gameNumber: Int,
    @SerialName("participant1Score") val participant1Score: Int = 0,
    @SerialName("participant2Score") val participant2Score: Int = 0,
    @SerialName("winnerId") val winnerId: String? = null,
    val status: String = "pending" // pending | completed
)

@Serializable
data class BracketMatchDto(
    val id: String,
    @SerialName("tournament_id") val tournamentId: String,
    val round: Int,
    @SerialName("match_number") val matchNumber: Int,
    @SerialName("bracket_type") val bracketType: String? = null,
    @SerialName("participant1_id") val participant1Id: String? = null,
    @SerialName("participant2_id") val participant2Id: String? = null,
    @SerialName("participant1_score") val participant1Score: Int = 0,
    @SerialName("participant2_score") val participant2Score: Int = 0,
    val games: List<MatchGameDto>? = null,
    @SerialName("winner_id") val winnerId: String? = null,
    @SerialName("loser_id") val loserId: String? = null,
    val status: String = "scheduled",
    @SerialName("next_match_id") val nextMatchId: String? = null,
    @SerialName("loser_next_match_id") val loserNextMatchId: String? = null,
    @SerialName("group_id") val groupId: String? = null,
    @SerialName("is_bye") val isBye: Boolean? = null,
    @SerialName("scheduled_at") val scheduledAt: String? = null
)

@Serializable
data class MatchResultUpdateDto(
    @SerialName("participant1_score") val participant1Score: Int,
    @SerialName("participant2_score") val participant2Score: Int,
    val games: List<MatchGameDto>? = null,
    @SerialName("winner_id") val winnerId: String? = null,
    @SerialName("loser_id") val loserId: String? = null,
    val status: String = "completed"
)

@Serializable
data class GroupStandingDto(
    @SerialName("participantId") val participantId: String,
    @SerialName("athleteName") val athleteName: String = "",
    val team: String? = null,
    val seed: Int? = null,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val points: Int = 0,
    @SerialName("gamesPlayed") val gamesPlayed: Int = 0,
    val position: Int = 0,
    val qualified: Boolean = false
)

@Serializable
data class TournamentGroupDto(
    val id: String,
    @SerialName("tournament_id") val tournamentId: String,
    val name: String,
    val standings: List<GroupStandingDto>? = null
)

@Serializable
data class BracketMatchInsertDto(
    val id: String,
    @SerialName("tournament_id") val tournamentId: String,
    val round: Int,
    @SerialName("match_number") val matchNumber: Int,
    @SerialName("bracket_type") val bracketType: String? = null,
    @SerialName("participant1_id") val participant1Id: String? = null,
    @SerialName("participant2_id") val participant2Id: String? = null,
    @SerialName("participant1_score") val participant1Score: Int = 0,
    @SerialName("participant2_score") val participant2Score: Int = 0,
    val games: List<MatchGameDto>? = null,
    @SerialName("winner_id") val winnerId: String? = null,
    @SerialName("loser_id") val loserId: String? = null,
    val status: String = "scheduled",
    @SerialName("next_match_id") val nextMatchId: String? = null,
    @SerialName("loser_next_match_id") val loserNextMatchId: String? = null,
    @SerialName("group_id") val groupId: String? = null,
    @SerialName("is_bye") val isBye: Boolean? = null
)

@Serializable
data class TournamentGroupInsertDto(
    val id: String,
    @SerialName("tournament_id") val tournamentId: String,
    val name: String,
    val standings: List<GroupStandingDto>? = null
)
