package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SponsorshipDto(
    val id: String? = null,
    @SerialName("sponsor_id") val sponsorId: String,
    @SerialName("tournament_id") val tournamentId: String? = null,
    @SerialName("team_id") val teamId: String? = null,
    val tier: String = "partner",
    val amount: Double? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String? = null,
    // JOIN
    val teams: TeamNameDto? = null,
    val tournaments: TournamentMinimalDto? = null,
    val profiles: ProfileMinimalDto? = null
)

@Serializable
data class SponsorshipInsertDto(
    @SerialName("sponsor_id") val sponsorId: String,
    @SerialName("tournament_id") val tournamentId: String? = null,
    @SerialName("team_id") val teamId: String? = null,
    val tier: String = "partner",
    val amount: Double? = null,
    val status: String = "pending"
)
