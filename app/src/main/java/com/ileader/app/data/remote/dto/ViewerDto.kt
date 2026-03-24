package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ══════════════════════════════════════════════════════════
// Viewer-specific DTOs (Чат #8)
// ══════════════════════════════════════════════════════════

/**
 * Community listing profile — minimal info + sport + rating.
 * Used for athletes/trainers/referees listing in CommunityScreen.
 */
@Serializable
data class CommunityProfileDto(
    val id: String,
    val name: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val city: String? = null,
    val bio: String? = null,
    @SerialName("athlete_subtype") val athleteSubtype: String? = null,
    @SerialName("user_sports") val userSports: List<UserSportDto>? = null
) {
    val primarySportName: String get() = userSports?.firstOrNull()?.sports?.name ?: ""
    val primaryRating: Int get() = userSports?.firstOrNull()?.rating ?: 0
    val subtypeLabel: String?
        get() = when (athleteSubtype) {
            "pilot" -> "Пилот"
            "shooter" -> "Стрелок"
            "tennis" -> "Теннисист"
            "football" -> "Футболист"
            "boxer" -> "Боксёр"
            "general" -> "Спортсмен"
            else -> null
        }
}

/**
 * Team with member count from team_members(count) JOIN.
 */
@Serializable
data class TeamWithStatsDto(
    val id: String,
    val name: String,
    @SerialName("sport_id") val sportId: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    val description: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    val city: String? = null,
    val sports: SportDto? = null,
    val profiles: ProfileMinimalDto? = null,
    @SerialName("team_members") val teamMembers: List<MemberCountDto>? = null
) {
    val sportName: String get() = sports?.name ?: ""
    val ownerName: String get() = profiles?.name ?: ""
    val memberCount: Int get() = teamMembers?.firstOrNull()?.count ?: 0
}

@Serializable
data class MemberCountDto(val count: Int = 0)

/**
 * Team membership for athlete's team lookup.
 */
@Serializable
data class TeamMembershipDto(
    @SerialName("team_id") val teamId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val role: String? = null,
    val teams: TeamNameWithSportDto? = null
)

@Serializable
data class TeamNameWithSportDto(
    val id: String? = null,
    val name: String? = null,
    val city: String? = null,
    val sports: SportDto? = null
)
