package com.ileader.app.data.models

data class AthleteStats(
    val totalTournaments: Int,
    val wins: Int,
    val rating: Int,
    val podiums: Int,
    val points: Int,
    val accuracy: Float
)

enum class GoalType(val displayName: String) {
    RATING("Рейтинг"),
    TOURNAMENT("Турниры"),
    POINTS("Очки");
}

enum class GoalStatus(val displayName: String) {
    ACTIVE("Активна"),
    COMPLETED("Выполнена"),
    FAILED("Просрочена");
}

data class AthleteGoal(
    val id: String,
    val type: GoalType,
    val title: String,
    val description: String,
    val deadline: String? = null,
    val status: GoalStatus = GoalStatus.ACTIVE,
    val targetValue: Int = 0,
    val currentValue: Int = 0,
    val createdAt: String = ""
)

data class TournamentResult(
    val id: String,
    val tournamentId: String,
    val tournamentName: String,
    val date: String,
    val position: Int,
    val points: Int,
    val participants: Int,
    val sportId: String,
    val sportName: String
)

data class License(
    val number: String,
    val category: String,
    val issueDate: String,
    val expiryDate: String,
    val status: String,
    val className: String,
    val federation: String,
    val medicalCheckDate: String,
    val medicalCheckExpiry: String
)
