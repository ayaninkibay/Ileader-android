package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class TournamentDto(
    val id: String,
    val name: String,
    @SerialName("sport_id") val sportId: String? = null,
    @SerialName("location_id") val locationId: String? = null,
    @SerialName("organizer_id") val organizerId: String? = null,
    val status: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val description: String? = null,
    val format: String? = null,
    @SerialName("match_format") val matchFormat: String? = null,
    @SerialName("seeding_type") val seedingType: String? = null,
    val visibility: String? = "public",
    @SerialName("access_code") val accessCode: String? = null,
    @SerialName("max_participants") val maxParticipants: Int? = null,
    @SerialName("min_participants") val minParticipants: Int? = null,
    val prize: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val documents: JsonElement? = null,
    val requirements: List<String>? = null,
    val categories: List<String>? = null,
    @SerialName("age_category") val ageCategory: String? = null,
    @SerialName("skill_level") val skillLevel: String? = null,
    @SerialName("gender_category") val genderCategory: String? = null,
    @SerialName("entry_fee") val entryFee: Double? = null,
    val region: String? = null,
    val discipline: String? = null,
    @SerialName("group_count") val groupCount: Int? = null,
    @SerialName("has_third_place_match") val hasThirdPlaceMatch: Boolean? = null,
    @SerialName("has_check_in") val hasCheckIn: Boolean? = null,
    @SerialName("registration_deadline") val registrationDeadline: String? = null,
    @SerialName("check_in_starts_before") val checkInStartsBefore: Int? = null,
    val prizes: List<String>? = null,
    val schedule: JsonElement? = null,
    @SerialName("stage_match_formats") val stageMatchFormats: JsonElement? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // JOIN fields
    val sports: SportDto? = null,
    val locations: LocationDto? = null,
    val profiles: ProfileMinimalDto? = null
)

@Serializable
data class TournamentWithCountsDto(
    val id: String,
    val name: String,
    @SerialName("sport_id") val sportId: String? = null,
    @SerialName("location_id") val locationId: String? = null,
    @SerialName("organizer_id") val organizerId: String? = null,
    val status: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val description: String? = null,
    val format: String? = null,
    val visibility: String? = null,
    @SerialName("max_participants") val maxParticipants: Int? = null,
    val prize: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val region: String? = null,
    @SerialName("age_category") val ageCategory: String? = null,
    // From view
    @SerialName("sport_name") val sportName: String? = null,
    @SerialName("location_name") val locationName: String? = null,
    @SerialName("organizer_name") val organizerName: String? = null,
    @SerialName("participant_count") val participantCount: Int = 0,
    @SerialName("referee_count") val refereeCount: Int = 0
)

@Serializable
data class TournamentInsertDto(
    val name: String,
    @SerialName("sport_id") val sportId: String,
    @SerialName("location_id") val locationId: String? = null,
    @SerialName("organizer_id") val organizerId: String,
    val status: String = "draft",
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String? = null,
    val description: String? = null,
    val format: String? = null,
    @SerialName("match_format") val matchFormat: String? = null,
    @SerialName("seeding_type") val seedingType: String? = null,
    val visibility: String = "public",
    @SerialName("max_participants") val maxParticipants: Int? = null,
    @SerialName("min_participants") val minParticipants: Int? = null,
    val prize: String? = null,
    val requirements: List<String>? = null,
    val categories: List<String>? = null,
    @SerialName("age_category") val ageCategory: String? = null,
    val region: String? = null,
    val discipline: String? = null,
    @SerialName("group_count") val groupCount: Int? = null,
    @SerialName("has_third_place_match") val hasThirdPlaceMatch: Boolean? = null,
    @SerialName("has_check_in") val hasCheckIn: Boolean? = null,
    @SerialName("access_code") val accessCode: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("registration_deadline") val registrationDeadline: String? = null,
    @SerialName("check_in_starts_before") val checkInStartsBefore: Int? = null,
    val prizes: List<String>? = null,
    val schedule: JsonElement? = null,
    @SerialName("stage_match_formats") val stageMatchFormats: JsonElement? = null
)

@Serializable
data class ScheduleItemDto(
    val time: String,
    val title: String
)
