package com.ileader.app.data.models

enum class TournamentStatus(val displayName: String) {
    DRAFT("Черновик"),
    REGISTRATION_OPEN("Регистрация открыта"),
    REGISTRATION_CLOSED("Регистрация закрыта"),
    CHECK_IN("Регистрация участников"),
    IN_PROGRESS("В процессе"),
    COMPLETED("Завершён"),
    CANCELLED("Отменён");
}

data class Tournament(
    val id: String,
    val name: String,
    val sportId: String,
    val sportName: String,
    val status: TournamentStatus,
    val startDate: String,
    val endDate: String? = null,
    val location: String,
    val description: String = "",
    val format: String = "",
    val maxParticipants: Int = 0,
    val currentParticipants: Int = 0,
    val prize: String = "",
    val imageUrl: String? = null,
    val organizerName: String = "",
    val requirements: List<String> = emptyList(),
    val schedule: List<ScheduleItem> = emptyList(),
    val prizes: List<String> = emptyList(),
    val ageCategory: String? = null,
    val organizerId: String = "",
    val locationId: String = "",
    val city: String = "",
    val visibility: String = "public",
    val categories: List<String> = emptyList(),
    val minParticipants: Int = 0
)

data class ScheduleItem(
    val time: String,
    val title: String,
    val description: String = ""
)
