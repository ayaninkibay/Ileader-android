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
    val profiles: ProfileMinimalDto? = null,
    val tournaments: MediaTournamentJoinDto? = null
)

@Serializable
data class TournamentSponsorshipInsertDto(
    @SerialName("sponsor_id") val sponsorId: String,
    @SerialName("tournament_id") val tournamentId: String,
    val tier: String,
    val amount: Double
)

data class SponsorStats(
    val totalSponsored: Int = 0,
    val activeSponsorships: Int = 0,
    val totalAmount: Double = 0.0
)
