package com.ileader.app.ui.screens.media

/**
 * Format ISO date (2026-03-15) to human-readable (15 мар 2026).
 */
internal fun formatDate(isoDate: String?): String {
    if (isoDate == null) return ""
    return try {
        val parts = isoDate.take(10).split("-")
        if (parts.size == 3) {
            val months = listOf(
                "", "янв", "фев", "мар", "апр", "мая",
                "июн", "июл", "авг", "сен", "окт", "ноя", "дек"
            )
            val day = parts[2].trimStart('0')
            val month = months[parts[1].toInt()]
            val year = parts[0]
            "$day $month $year"
        } else {
            isoDate.take(10)
        }
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

/**
 * Map tournament status string from DB to Russian label.
 */
internal fun tournamentStatusLabel(status: String?): String = when (status) {
    "draft" -> "Черновик"
    "registration_open" -> "Регистрация"
    "registration_closed" -> "Закрыта"
    "check_in" -> "Чек-ин"
    "in_progress" -> "Идёт"
    "completed" -> "Завершён"
    "cancelled" -> "Отменён"
    else -> status ?: ""
}

/**
 * Check if tournament is considered active (not completed/cancelled).
 */
internal fun isTournamentActive(status: String?): Boolean =
    status != "completed" && status != "cancelled"

/**
 * Map invite status string from DB to Russian label.
 */
internal fun inviteStatusLabel(status: String?): String = when (status) {
    "pending" -> "Ожидает"
    "accepted" -> "Принято"
    "declined" -> "Отклонено"
    else -> status ?: ""
}
