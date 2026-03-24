package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoalDto(
    val id: String,
    @SerialName("athlete_id") val athleteId: String,
    val type: String,
    val title: String,
    val description: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_by_id") val createdById: String? = null,
    val status: String,
    val progress: Int = 0,
    val deadline: String? = null,
    @SerialName("target_rating") val targetRating: Int? = null,
    @SerialName("target_wins") val targetWins: Int? = null,
    @SerialName("target_podiums") val targetPodiums: Int? = null,
    @SerialName("target_points") val targetPoints: Int? = null,
    @SerialName("current_wins") val currentWins: Int? = null,
    @SerialName("current_podiums") val currentPodiums: Int? = null,
    @SerialName("current_points") val currentPoints: Int? = null,
    @SerialName("sport_id") val sportId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class GoalInsertDto(
    @SerialName("athlete_id") val athleteId: String,
    val type: String,
    val title: String,
    val description: String? = null,
    @SerialName("created_by") val createdBy: String = "athlete",
    @SerialName("created_by_id") val createdById: String,
    val status: String = "active",
    val progress: Int = 0,
    val deadline: String? = null,
    @SerialName("target_rating") val targetRating: Int? = null,
    @SerialName("target_wins") val targetWins: Int? = null,
    @SerialName("target_podiums") val targetPodiums: Int? = null,
    @SerialName("target_points") val targetPoints: Int? = null,
    @SerialName("sport_id") val sportId: String? = null
)

@Serializable
data class GoalUpdateDto(
    val status: String? = null,
    val progress: Int? = null,
    @SerialName("current_wins") val currentWins: Int? = null,
    @SerialName("current_podiums") val currentPodiums: Int? = null,
    @SerialName("current_points") val currentPoints: Int? = null
)
