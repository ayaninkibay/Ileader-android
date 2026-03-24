package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefereeAssignmentDto(
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("referee_id") val refereeId: String? = null,
    val role: String? = null,
    @SerialName("assigned_at") val assignedAt: String? = null,
    // JOIN
    val profiles: ProfileMinimalDto? = null,
    val tournaments: TournamentDto? = null
)
