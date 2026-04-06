package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TournamentSponsorshipDto(
    @SerialName("sponsor_id") val sponsorId: String,
    @SerialName("tournament_id") val tournamentId: String,
    val tier: String? = null,
    val amount: Double? = null,
    // JOIN
    val profiles: ProfileMinimalDto? = null
)
