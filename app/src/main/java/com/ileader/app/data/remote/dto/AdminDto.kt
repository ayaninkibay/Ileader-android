package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Admin user — alias for ProfileDto kept as a separate type for clarity in admin screens.
 */
typealias AdminUserDto = ProfileDto

/**
 * Aggregated stats shown on the admin dashboard.
 */
data class AdminStatsDto(
    val totalUsers: Int = 0,
    val totalTournaments: Int = 0,
    val activeTournaments: Int = 0,
    val pendingVerifications: Int = 0
)

/**
 * Заявка на новый вид спорта (sport_requests).
 */
@Serializable
data class SportRequestDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("requested_by") val requestedBy: String? = null,
    val status: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // JOIN
    val profiles: ProfileMinimalDto? = null
)

/**
 * Системная настройка платформы (platform_settings).
 */
@Serializable
data class PlatformSettingDto(
    val id: String? = null,
    val key: String,
    val value: JsonElement? = null,
    val description: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Payload для подачи заявки на верификацию (обновление profiles).
 */
@Serializable
data class VerificationRequestInsertDto(
    val verification: String = "pending",
    @SerialName("role_data") val roleData: JsonElement? = null
)
