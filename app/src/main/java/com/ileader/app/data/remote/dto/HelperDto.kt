package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TournamentHelperDto(
    val id: String = "",
    @SerialName("tournament_id") val tournamentId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("assigned_by") val assignedBy: String = "",
    val status: String = "active",
    @SerialName("created_at") val createdAt: String = "",
    // JOIN: tournament info
    val tournaments: HelperTournamentDto? = null,
    // JOIN: user profile info (used by organizer view)
    val profiles: HelperProfileDto? = null
)

@Serializable
data class HelperProfileDto(
    val id: String = "",
    val name: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val email: String? = null
)

@Serializable
data class HelperTournamentDto(
    val id: String = "",
    val name: String = "",
    val status: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("has_check_in") val hasCheckIn: Boolean? = null,
    val sports: HelperSportDto? = null
)

@Serializable
data class HelperSportDto(
    val id: String = "",
    val name: String = ""
)

@Serializable
data class TournamentHelperInsertDto(
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("assigned_by") val assignedBy: String,
    val status: String = "active"
)
