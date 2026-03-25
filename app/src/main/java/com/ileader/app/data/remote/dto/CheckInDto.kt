package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * QR payload format:
 * - type: "athlete" or "spectator"
 * - uid: user ID
 * - tid: tournament ID
 * - ts: timestamp
 * - v: version (for backwards compat, old QRs without type are treated as "athlete")
 */
@Serializable
data class QrPayload(
    val v: Int = 1,
    val uid: String,
    val tid: String,
    val ts: Long,
    val type: String = "athlete" // "athlete" | "spectator"
)

@Serializable
data class RefereeCheckInDto(
    @SerialName("tournament_id") val tournamentId: String = "",
    @SerialName("referee_id") val refereeId: String = "",
    val role: String? = null,
    @SerialName("check_in_status") val checkInStatus: String? = null,
    // JOIN
    val profiles: ProfileMinimalDto? = null
)

@Serializable
data class SpectatorDto(
    val id: String = "",
    @SerialName("tournament_id") val tournamentId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("ticket_type") val ticketType: String = "free",
    @SerialName("payment_status") val paymentStatus: String = "free",
    @SerialName("check_in_status") val checkInStatus: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    // JOIN
    val profiles: ProfileMinimalDto? = null
)
