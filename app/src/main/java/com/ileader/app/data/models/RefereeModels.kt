package com.ileader.app.data.models

/**
 * Domain models for Referee screens.
 * Mirror RefereeMockData types but designed for real DB data.
 */

// ── Enums ──

enum class ViolationSeverity(val label: String) {
    WARNING("Предупреждение"),
    PENALTY("Штраф"),
    DISQUALIFICATION("Дисквалификация")
}

enum class ViolationCategory(val label: String) {
    FALSE_START("Фальстарт"),
    DANGEROUS_DRIVING("Опасное вождение"),
    TRACK_LIMITS("Выход за пределы трассы"),
    UNSPORTSMANLIKE("Неспортивное поведение"),
    EQUIPMENT("Нарушение экипировки"),
    SAFETY("Нарушение безопасности"),
    RULES("Нарушение правил"),
    CONTACT("Запрещённый контакт"),
    DELAY("Задержка / неявка"),
    OTHER("Прочее")
}

enum class RefereeMatchStatus(val label: String) {
    COMPLETED("Завершён"),
    IN_PROGRESS("Идёт сейчас"),
    SCHEDULED("Ожидает")
}


enum class RefereeRole(val label: String) {
    HEAD_REFEREE("Главный судья"),
    ASSISTANT("Помощник"),
    REFEREE("Судья")
}

// ── Data classes ──

data class RefereeStats(
    val totalTournaments: Int,
    val thisMonth: Int,
    val pendingResults: Int,
    val totalParticipants: Int,
    val totalViolations: Int
)

data class RefereeTournament(
    val id: String,
    val name: String,
    val sport: String,
    val sportId: String = "",
    val location: String,
    val date: String,
    val time: String = "",
    val status: TournamentStatus,
    val participants: Int,
    val matchesTotal: Int = 0,
    val matchesCompleted: Int = 0,
    val description: String = "",
    val organizer: String = "",
    val prizeFund: String = "",
    val categories: List<String> = emptyList(),
    val refereeRole: RefereeRole = RefereeRole.REFEREE,
    val rating: Float? = null,
    val feedback: String? = null
)

data class RefereeViolation(
    val id: String,
    val participantId: String,
    val participantName: String,
    val tournamentId: String,
    val tournamentName: String,
    val sport: String,
    val date: String,
    val severity: ViolationSeverity,
    val category: ViolationCategory,
    val description: String,
    val matchNumber: Int? = null,
    val time: String? = null,
    val penaltyApplied: String? = null
)

data class RefereeInvite(
    val id: String,
    val tournamentId: String,
    val tournamentName: String,
    val sportName: String = "",
    val role: RefereeRole,
    val status: InviteStatus,
    val locationCity: String = "",
    val startDate: String = "",
    val createdAt: String,
    val contactPhone: String? = null,
    val responseMessage: String? = null
)

data class RefereeMatch(
    val id: String,
    val number: Int,
    val time: String,
    val category: String,
    val status: RefereeMatchStatus,
    val participants: Int
)

data class RefereeParticipant(
    val id: String,
    val name: String,
    val number: String,
    val team: String,
    val category: String
)

data class RefereeMonthlyStats(
    val month: String,
    val tournaments: Int,
    val avgRating: Float,
    val violations: Int
)
