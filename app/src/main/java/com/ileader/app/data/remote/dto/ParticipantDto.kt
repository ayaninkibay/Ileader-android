package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParticipantDto(
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("athlete_id") val athleteId: String,
    @SerialName("team_id") val teamId: String? = null,
    val status: String? = null,
    val seed: Int? = null,
    @SerialName("check_in_status") val checkInStatus: String? = null,
    val number: Int? = null,
    @SerialName("group_id") val groupId: String? = null,
    @SerialName("registered_at") val registeredAt: String? = null,
    // JOIN fields
    val profiles: ProfileMinimalDto? = null,
    val tournaments: TournamentDto? = null,
    val teams: TeamNameDto? = null
)

@Serializable
data class ParticipantInsertDto(
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("athlete_id") val athleteId: String,
    @SerialName("team_id") val teamId: String? = null,
    val status: String = "pending"
)
