package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LocationReviewDto(
    val id: String,
    @SerialName("location_id") val locationId: String,
    @SerialName("user_id") val userId: String,
    val overall: Double,
    val criteria: JsonElement? = null,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // JOIN
    val profiles: ProfileMinimalDto? = null
)

@Serializable
data class LocationReviewInsertDto(
    @SerialName("location_id") val locationId: String,
    @SerialName("user_id") val userId: String,
    val overall: Double,
    val criteria: JsonElement? = null,
    val comment: String? = null
)
