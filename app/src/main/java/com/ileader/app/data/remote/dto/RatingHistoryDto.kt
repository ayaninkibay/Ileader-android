package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RatingHistoryDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("sport_id") val sportId: String? = null,
    @SerialName("tournament_id") val tournamentId: String? = null,
    @SerialName("new_rating") val newRating: Int? = null,
    val rating: Int? = null,
    @SerialName("points_earned") val pointsEarned: Int? = null,
    @SerialName("rating_change") val ratingChange: Int? = null,
    val placement: Int? = null,
    val position: Int? = null,
    val reason: String? = null,
    @SerialName("recorded_at") val recordedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val sports: SportNameRefDto? = null,
    val tournaments: TournamentNameRefDto? = null
) {
    val effectiveRating: Int get() = newRating ?: rating ?: 0
    val effectiveDelta: Int get() = ratingChange ?: pointsEarned ?: 0
    val effectiveDate: String get() = (recordedAt ?: createdAt ?: "").take(10)
}

@Serializable
data class SportNameRefDto(val id: String? = null, val name: String? = null)

@Serializable
data class TournamentNameRefDto(val id: String? = null, val name: String? = null)
