package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LocationDto(
    val id: String? = null,
    val name: String,
    val type: String? = null,
    val address: String? = null,
    val city: String? = null,
    val capacity: Int? = null,
    val facilities: List<String>? = null,
    val description: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    val rating: Double? = null,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    @SerialName("image_urls") val imageUrls: List<String>? = null,
    val coordinates: JsonElement? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class LocationInsertDto(
    val name: String,
    val type: String,
    val address: String? = null,
    val city: String? = null,
    val capacity: Int? = null,
    val facilities: List<String>? = null,
    val description: String? = null,
    @SerialName("owner_id") val ownerId: String,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null
)
