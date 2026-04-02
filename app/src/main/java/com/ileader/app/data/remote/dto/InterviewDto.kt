package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InterviewDto(
    val id: String,
    @SerialName("media_user_id") val mediaUserId: String? = null,
    @SerialName("athlete_id") val athleteId: String? = null,
    @SerialName("tournament_id") val tournamentId: String? = null,
    val title: String,
    val content: String? = null,
    val status: String? = "scheduled",
    @SerialName("scheduled_date") val scheduledDate: String? = null,
    val time: String? = null,
    val location: String? = null,
    val topic: String? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // JOINs — athlete profile
    @SerialName("athlete") val athlete: ProfileMinimalDto? = null,
    // JOIN — tournament
    val tournaments: InterviewTournamentDto? = null
)

@Serializable
data class InterviewTournamentDto(
    val id: String? = null,
    val name: String? = null,
    @SerialName("start_date") val startDate: String? = null
)

@Serializable
data class InterviewInsertDto(
    @SerialName("media_user_id") val mediaUserId: String,
    @SerialName("athlete_id") val athleteId: String,
    @SerialName("tournament_id") val tournamentId: String? = null,
    val title: String,
    val content: String? = null,
    val status: String = "scheduled",
    @SerialName("scheduled_date") val scheduledDate: String? = null,
    val time: String? = null,
    val location: String? = null,
    val topic: String? = null,
    val notes: String? = null
)

@Serializable
data class InterviewUpdateDto(
    val title: String? = null,
    val content: String? = null,
    @SerialName("athlete_id") val athleteId: String? = null,
    @SerialName("tournament_id") val tournamentId: String? = null,
    val status: String? = null,
    @SerialName("scheduled_date") val scheduledDate: String? = null,
    val time: String? = null,
    val location: String? = null,
    val topic: String? = null,
    val notes: String? = null
)

data class InterviewStatsDto(
    val total: Int = 0,
    val scheduled: Int = 0,
    val completed: Int = 0,
    val published: Int = 0
)
