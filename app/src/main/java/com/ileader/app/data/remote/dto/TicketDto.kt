package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Unified ticket — either athlete registration or spectator registration.
 * Used by MyTicketsScreen to show all QR codes in one place.
 */
data class TicketItem(
    val tournamentId: String,
    val tournamentName: String,
    val sportName: String?,
    val startDate: String?,
    val status: String?,
    val checkInStatus: String?,
    val type: String // "athlete" | "spectator"
)

@Serializable
data class ParticipantTicketDto(
    @SerialName("tournament_id") val tournamentId: String = "",
    @SerialName("athlete_id") val athleteId: String = "",
    val status: String? = null,
    @SerialName("check_in_status") val checkInStatus: String? = null,
    val tournaments: TicketTournamentDto? = null
)

@Serializable
data class SpectatorTicketDto(
    @SerialName("tournament_id") val tournamentId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("check_in_status") val checkInStatus: String? = null,
    val tournaments: TicketTournamentDto? = null
)

@Serializable
data class RefereeTicketDto(
    @SerialName("tournament_id") val tournamentId: String = "",
    @SerialName("referee_id") val refereeId: String = "",
    @SerialName("check_in_status") val checkInStatus: String? = null,
    val tournaments: TicketTournamentDto? = null
)

@Serializable
data class TicketTournamentDto(
    val id: String = "",
    val name: String = "",
    val status: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    val sports: TicketSportDto? = null
)

@Serializable
data class TicketSportDto(
    val name: String = ""
)
