package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AchievementDto(
    val id: String? = null,
    @SerialName("athlete_id") val athleteId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val rarity: String? = null,
    val date: String? = null,
    @SerialName("tournament_id") val tournamentId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
