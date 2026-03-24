package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Dashboard counting DTO ──────────────────────────────────────
@Serializable
data class AdminProfileCountDto(
    val id: String,
    val status: String? = null,
    val verification: String? = null,
    @SerialName("primary_role_id") val primaryRoleId: String? = null
)

// ── Dashboard stats data holder ─────────────────────────────────
data class AdminPlatformStats(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val blockedUsers: Int = 0,
    val pendingVerifications: Int = 0,
    val totalTournaments: Int = 0,
    val activeTournaments: Int = 0,
    val totalSports: Int = 0,
    val totalLocations: Int = 0
)

// ── Admin user update ───────────────────────────────────────────
@Serializable
data class AdminUserUpdateDto(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val status: String? = null,
    val verification: String? = null,
    @SerialName("primary_role_id") val primaryRoleId: String? = null,
    @SerialName("athlete_subtype") val athleteSubtype: String? = null
)

// ── Sport insert/update ─────────────────────────────────────────
@Serializable
data class SportInsertDto(
    val name: String,
    val slug: String,
    @SerialName("athlete_label") val athleteLabel: String,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class SportUpdateDto(
    val name: String? = null,
    val slug: String? = null,
    @SerialName("athlete_label") val athleteLabel: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)

// ── Create user request (Edge Function) ─────────────────────────
@Serializable
data class AdminCreateUserRequest(
    val email: String,
    val password: String,
    val name: String,
    val role: String,
    val phone: String? = null,
    val city: String? = null
)

// ── Sport with counts (for admin sport list) ────────────────────
@Serializable
data class SportWithCountsDto(
    val id: String,
    val name: String,
    val slug: String? = null,
    @SerialName("athlete_label") val athleteLabel: String? = null,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    val description: String? = null,
    val athleteCount: Int = 0,
    val tournamentCount: Int = 0
)

// ── Location with tournament count ──────────────────────────────
@Serializable
data class LocationWithCountsDto(
    val id: String,
    val name: String,
    val type: String? = null,
    val address: String? = null,
    val city: String? = null,
    val capacity: Int? = null,
    val facilities: List<String>? = null,
    val description: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    val rating: Double? = null,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    @SerialName("image_urls") val imageUrls: List<String>? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val tournamentsCount: Int = 0
)
