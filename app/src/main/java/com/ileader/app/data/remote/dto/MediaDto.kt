package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ══════════════════════════════════════════════════════════
// Media-specific DTOs (Чат #6)
// ══════════════════════════════════════════════════════════

/**
 * Minimal location for tournament JOIN in invites.
 */
@Serializable
data class LocationMinimalDto(
    val name: String? = null,
    val city: String? = null
)

/**
 * Tournament info for media invite JOINs — includes sport + location.
 */
@Serializable
data class MediaTournamentJoinDto(
    val id: String? = null,
    val name: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    val status: String? = null,
    val sports: SportDto? = null,
    val locations: LocationMinimalDto? = null,
    @SerialName("organizer_id") val organizerId: String? = null
)

/**
 * Full invite with detailed tournament JOIN — used in notifications screen.
 */
@Serializable
data class MediaInviteFullDto(
    val id: String,
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("user_id") val userId: String? = null,
    val role: String? = null,
    val direction: String? = null,
    val status: String? = null,
    val message: String? = null,
    val comments: JsonElement? = null,
    @SerialName("response_message") val responseMessage: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // JOIN
    val tournaments: MediaTournamentJoinDto? = null
)

/**
 * Minimal invite DTO for checking accreditation status per tournament.
 */
@Serializable
data class MediaAccreditationDto(
    val id: String,
    @SerialName("tournament_id") val tournamentId: String,
    val status: String? = null
)

/**
 * Insert DTO for creating a new media accreditation request.
 */
@Serializable
data class MediaInviteInsertDto(
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("user_id") val userId: String,
    val role: String = "media",
    val direction: String = "incoming",
    val status: String = "pending",
    val message: String? = null
)

/**
 * Result DTO from get_tournament_by_invite_code RPC function.
 */
@Serializable
data class MediaInviteCodeResultDto(
    @SerialName("tournament_id") val tournamentId: String? = null,
    val code: String? = null,
    val type: String? = null
)

/**
 * Update DTO for accepting an invite.
 */
@Serializable
data class MediaInviteAcceptDto(
    val status: String = "accepted",
    @SerialName("response_message") val responseMessage: String? = null,
    val comments: JsonElement? = null
)

/**
 * Update DTO for declining an invite.
 */
@Serializable
data class MediaInviteDeclineDto(
    val status: String = "declined",
    @SerialName("response_message") val responseMessage: String
)
