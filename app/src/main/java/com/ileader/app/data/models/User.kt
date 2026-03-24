package com.ileader.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val country: String? = null,
    val bio: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
    @SerialName("athlete_subtype") val athleteSubtype: AthleteSubtype? = null,
    @SerialName("team_id") val teamId: String? = null,
    @SerialName("sport_ids") val sportIds: List<String>? = null,
    val verification: VerificationStatus = VerificationStatus.NOT_REQUIRED,
    val status: UserStatus = UserStatus.ACTIVE,
    @SerialName("created_at") val createdAt: String? = null
) {
    val displayName: String
        get() = name.ifEmpty { email }
}

data class SignUpData(
    val name: String,
    val email: String,
    val password: String,
    val phone: String,
    val city: String,
    val role: UserRole,
    val sportIds: List<String>? = null,
    val athleteSubtype: AthleteSubtype? = null
)
