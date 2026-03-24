package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResultDto(
    val id: String? = null,
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("athlete_id") val athleteId: String,
    val position: Int,
    val points: Int? = null,
    val time: String? = null,
    val penalty: String? = null,
    val category: String? = null,
    val notes: String? = null,
    // JOIN fields
    val profiles: ProfileMinimalDto? = null,
    val tournaments: TournamentMinimalDto? = null
)

@Serializable
data class ResultInsertDto(
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("athlete_id") val athleteId: String,
    val position: Int,
    val points: Int? = null,
    val time: String? = null,
    val penalty: String? = null,
    val category: String? = null,
    val notes: String? = null
)

@Serializable
data class TournamentMinimalDto(
    val id: String? = null,
    val name: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("sport_id") val sportId: String? = null,
    val sports: SportDto? = null,
    val locations: LocationDto? = null
)
