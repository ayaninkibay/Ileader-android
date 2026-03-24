package com.ileader.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole(val displayName: String) {
    @SerialName("athlete") ATHLETE("Спортсмен"),
    @SerialName("trainer") TRAINER("Тренер"),
    @SerialName("organizer") ORGANIZER("Организатор"),
    @SerialName("referee") REFEREE("Судья"),
    @SerialName("sponsor") SPONSOR("Спонсор"),
    @SerialName("media") MEDIA("СМИ"),
    @SerialName("admin") ADMIN("Администратор"),
    @SerialName("user") USER("Зритель");

    val requiresVerification: Boolean
        get() = this in listOf(TRAINER, ORGANIZER, REFEREE, SPONSOR, MEDIA)
}

@Serializable
enum class AthleteSubtype(val displayName: String) {
    @SerialName("pilot") PILOT("Пилот"),
    @SerialName("shooter") SHOOTER("Стрелок"),
    @SerialName("tennis") TENNIS("Теннисист"),
    @SerialName("football") FOOTBALL("Футболист"),
    @SerialName("boxer") BOXER("Боксёр"),
    @SerialName("general") GENERAL("Спортсмен");
}

@Serializable
enum class VerificationStatus {
    @SerialName("not_required") NOT_REQUIRED,
    @SerialName("pending") PENDING,
    @SerialName("verified") VERIFIED,
    @SerialName("rejected") REJECTED;
}

@Serializable
enum class UserStatus {
    @SerialName("active") ACTIVE,
    @SerialName("blocked") BLOCKED;
}
