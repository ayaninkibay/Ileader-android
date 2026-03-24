package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class TournamentInviteDto(
    val id: String,
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("user_id") val userId: String? = null,
    val role: String? = null,
    val direction: String? = null,
    val status: String? = null,
    @SerialName("referee_role") val refereeRole: String? = null,
    @SerialName("sponsor_tier") val sponsorTier: String? = null,
    @SerialName("sponsor_amount") val sponsorAmount: Double? = null,
    val message: String? = null,
    val comments: JsonElement? = null,
    @SerialName("response_message") val responseMessage: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // JOIN
    val tournaments: TournamentMinimalDto? = null,
    val profiles: ProfileMinimalDto? = null
)

@Serializable
data class InviteCodeDto(
    val id: String? = null,
    @SerialName("tournament_id") val tournamentId: String,
    val code: String,
    val type: String? = null,
    @SerialName("referee_role") val refereeRole: String? = null,
    @SerialName("max_uses") val maxUses: Int? = null,
    @SerialName("used_count") val usedCount: Int = 0,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class InviteCodeInsertDto(
    @SerialName("tournament_id") val tournamentId: String,
    val code: String,
    val type: String,
    @SerialName("referee_role") val refereeRole: String? = null,
    @SerialName("max_uses") val maxUses: Int? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_by") val createdBy: String
)

@Serializable
data class TournamentInviteInsertDto(
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("user_id") val userId: String? = null,
    val role: String,
    val direction: String = "outgoing",
    val status: String = "pending",
    @SerialName("referee_role") val refereeRole: String? = null,
    val message: String? = null
)
