package com.ileader.app.data.models

enum class BracketType(val value: String) {
    UPPER("upper"),
    LOWER("lower"),
    GRAND_FINAL("grand_final"),
    THIRD_PLACE("third_place");

    companion object {
        fun fromString(s: String?) = entries.find { it.value == s } ?: UPPER
    }
}

enum class MatchStatus(val value: String, val displayName: String) {
    SCHEDULED("scheduled", "Запланирован"),
    IN_PROGRESS("in_progress", "Идёт"),
    COMPLETED("completed", "Завершён"),
    CANCELLED("cancelled", "Отменён");

    companion object {
        fun fromString(s: String?) = entries.find { it.value == s } ?: SCHEDULED
    }
}

enum class TournamentFormat(val value: String, val displayName: String) {
    SINGLE_ELIMINATION("single_elimination", "Олимпийская система"),
    DOUBLE_ELIMINATION("double_elimination", "Двойное выбывание"),
    ROUND_ROBIN("round_robin", "Круговая система"),
    GROUPS_SINGLE_ELIM("groups_single_elim", "Группы + олимпийская"),
    GROUPS_DOUBLE_ELIM("groups_double_elim", "Группы + двойное выбывание");

    companion object {
        fun fromString(s: String?) = entries.find { it.value == s }
    }
}

enum class MatchFormat(val value: String, val displayName: String, val gameCount: Int) {
    BO1("BO1", "BO1", 1),
    BO2("BO2", "BO2", 2),
    BO3("BO3", "BO3", 3),
    BO5("BO5", "BO5", 5);

    companion object {
        fun fromString(s: String?) = entries.find { it.value == s } ?: BO1
    }
}

enum class SeedingType(val value: String, val displayName: String) {
    RANDOM("random", "Случайная"),
    RATING("rating", "По рейтингу"),
    MANUAL("manual", "Ручная");

    companion object {
        fun fromString(s: String?) = entries.find { it.value == s } ?: RANDOM
    }
}

data class MatchGame(
    val gameNumber: Int,
    val participant1Score: Int = 0,
    val participant2Score: Int = 0,
    val winnerId: String? = null,
    val status: String = "pending"
)

data class BracketMatch(
    val id: String,
    val tournamentId: String,
    val round: Int,
    val matchNumber: Int,
    val bracketType: BracketType,
    val participant1Id: String? = null,
    val participant2Id: String? = null,
    val participant1Name: String? = null,
    val participant2Name: String? = null,
    val participant1Seed: Int? = null,
    val participant2Seed: Int? = null,
    val participant1Score: Int = 0,
    val participant2Score: Int = 0,
    val games: List<MatchGame> = emptyList(),
    val winnerId: String? = null,
    val loserId: String? = null,
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val nextMatchId: String? = null,
    val loserNextMatchId: String? = null,
    val groupId: String? = null,
    val isBye: Boolean = false,
    val scheduledAt: String? = null
)

data class GroupParticipant(
    val participantId: String,
    val athleteName: String,
    val team: String? = null,
    val seed: Int? = null,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val points: Int = 0,
    val gamesPlayed: Int = 0,
    val position: Int = 0,
    val qualified: Boolean = false
)

data class TournamentGroup(
    val id: String,
    val tournamentId: String,
    val name: String,
    val participants: List<GroupParticipant> = emptyList()
)
