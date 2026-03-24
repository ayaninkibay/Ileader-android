package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganizerStatsDto(
    @SerialName("organizer_id") val organizerId: String? = null,
    @SerialName("total_tournaments") val totalTournaments: Int = 0,
    @SerialName("active_tournaments") val activeTournaments: Int = 0,
    @SerialName("total_locations") val totalLocations: Int = 0,
    @SerialName("total_participants") val totalParticipants: Int = 0
)
