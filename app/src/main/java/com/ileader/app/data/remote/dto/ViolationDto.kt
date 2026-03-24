package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ViolationDto(
    val id: String? = null,
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("athlete_id") val athleteId: String,
    @SerialName("referee_id") val refereeId: String,
    val severity: String,
    val category: String,
    val description: String? = null,
    @SerialName("match_number") val matchNumber: Int? = null,
    val time: String? = null,
    @SerialName("penalty_applied") val penaltyApplied: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // JOIN
    val profiles: ProfileMinimalDto? = null,
    val tournaments: TournamentMinimalDto? = null
)

@Serializable
data class ViolationInsertDto(
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("athlete_id") val athleteId: String,
    @SerialName("referee_id") val refereeId: String,
    val severity: String,
    val category: String,
    val description: String? = null,
    @SerialName("match_number") val matchNumber: Int? = null,
    val time: String? = null,
    @SerialName("penalty_applied") val penaltyApplied: String? = null
)
