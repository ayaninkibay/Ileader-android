package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamDto(
    val id: String,
    val name: String,
    @SerialName("sport_id") val sportId: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    val description: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("founded_year") val foundedYear: Int? = null,
    val city: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // JOIN fields
    val sports: SportDto? = null,
    val profiles: ProfileMinimalDto? = null
)

@Serializable
data class TeamMemberDto(
    @SerialName("team_id") val teamId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val role: String? = null,
    @SerialName("joined_at") val joinedAt: String? = null,
    // JOIN
    val profiles: ProfileMinimalDto? = null
)

@Serializable
data class TeamRequestDto(
    val id: String,
    @SerialName("team_id") val teamId: String,
    @SerialName("user_id") val userId: String? = null,
    val status: String? = null,
    val message: String? = null,
    @SerialName("response_message") val responseMessage: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // JOIN
    val profiles: ProfileMinimalDto? = null,
    val teams: TeamNameDto? = null
)

@Serializable
data class TeamNameDto(
    val name: String? = null
)
