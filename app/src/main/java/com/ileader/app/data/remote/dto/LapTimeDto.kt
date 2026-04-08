package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LapTimeDto(
    val id: String? = null,
    @SerialName("athlete_id") val athleteId: String? = null,
    @SerialName("location_id") val locationId: String? = null,
    @SerialName("tournament_id") val tournamentId: String? = null,
    val date: String? = null,
    @SerialName("lap_number") val lapNumber: Int? = null,
    @SerialName("time_seconds") val timeSeconds: Double? = null,
    @SerialName("is_best") val isBest: Boolean? = null,
    val conditions: String? = null,
    val equipment: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class LapTimeInsertDto(
    @SerialName("athlete_id") val athleteId: String,
    val date: String,
    @SerialName("time_seconds") val timeSeconds: Double,
    @SerialName("lap_number") val lapNumber: Int? = null,
    val conditions: String? = null,
    val equipment: String? = null,
    @SerialName("is_best") val isBest: Boolean? = null
)
