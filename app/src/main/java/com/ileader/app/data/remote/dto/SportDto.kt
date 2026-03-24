package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SportDto(
    val id: String,
    val name: String,
    val slug: String? = null,
    @SerialName("athlete_label") val athleteLabel: String? = null,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class UserSportDto(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("sport_id") val sportId: String? = null,
    val rating: Int = 1000,
    @SerialName("is_primary") val isPrimary: Boolean = false,
    // JOIN
    val sports: SportDto? = null
)

@Serializable
data class UserSportStatsDto(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("sport_id") val sportId: String? = null,
    @SerialName("sport_name") val sportName: String? = null,
    val tournaments: Int = 0,
    val wins: Int = 0,
    val podiums: Int = 0,
    @SerialName("total_points") val totalPoints: Int = 0,
    val rating: Int = 1000
)
