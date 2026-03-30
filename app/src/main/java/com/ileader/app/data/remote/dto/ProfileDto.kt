package com.ileader.app.data.remote.dto

import com.ileader.app.data.models.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ProfileDto(
    val id: String,
    val name: String? = null,
    val nickname: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val country: String? = null,
    val bio: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
    @SerialName("athlete_subtype") val athleteSubtype: String? = null,
    @SerialName("age_category") val ageCategory: String? = null,
    val verification: String? = null,
    val status: String? = null,
    @SerialName("role_ids") val roleIds: List<String>? = null,
    @SerialName("primary_role_id") val primaryRoleId: String? = null,
    @SerialName("role_data") val roleData: JsonElement? = null,
    val settings: JsonElement? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // JOIN fields
    val roles: RoleDto? = null
) {
    fun toDomain(): User {
        val roleName = roles?.name
        val userRole = when (roleName) {
            "athlete" -> UserRole.ATHLETE
            "trainer" -> UserRole.TRAINER
            "organizer" -> UserRole.ORGANIZER
            "referee" -> UserRole.REFEREE
            "sponsor" -> UserRole.SPONSOR
            "media" -> UserRole.MEDIA
            "admin" -> UserRole.ADMIN
            else -> UserRole.USER
        }
        val verificationStatus = when (verification) {
            "pending" -> VerificationStatus.PENDING
            "verified" -> VerificationStatus.VERIFIED
            "rejected" -> VerificationStatus.REJECTED
            else -> VerificationStatus.NOT_REQUIRED
        }
        val userStatus = when (status) {
            "blocked" -> UserStatus.BLOCKED
            else -> UserStatus.ACTIVE
        }
        val subtype = when (athleteSubtype) {
            "pilot" -> AthleteSubtype.PILOT
            "shooter" -> AthleteSubtype.SHOOTER
            "tennis" -> AthleteSubtype.TENNIS
            "football" -> AthleteSubtype.FOOTBALL
            "boxer" -> AthleteSubtype.BOXER
            "general" -> AthleteSubtype.GENERAL
            else -> null
        }
        return User(
            id = id,
            name = name ?: "",
            email = email ?: "",
            role = userRole,
            avatarUrl = avatarUrl,
            phone = phone,
            city = city,
            country = country,
            bio = bio,
            birthDate = birthDate,
            athleteSubtype = subtype,
            verification = verificationStatus,
            status = userStatus,
            createdAt = createdAt
        )
    }
}

@Serializable
data class ProfileUpdateDto(
    val name: String? = null,
    val nickname: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val country: String? = null,
    val bio: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
    @SerialName("athlete_subtype") val athleteSubtype: String? = null,
    @SerialName("age_category") val ageCategory: String? = null
)

@Serializable
data class ProfileMinimalDto(
    val id: String? = null,
    val name: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val city: String? = null,
    val email: String? = null
)
