package com.ileader.app.ui.screens.sponsor

import androidx.compose.ui.graphics.Color
import com.ileader.app.ui.theme.ILeaderColors

/**
 * Utility functions for Sponsor screens.
 * Extracted from SponsorMockData to decouple screens from mock data.
 */
object SponsorUtils {

    fun formatAmount(amount: Long): String = when {
        amount >= 1_000_000 -> {
            val m = amount.toDouble() / 1_000_000
            if (m == m.toLong().toDouble()) "${m.toLong()}M ₸" else "${"%.1f".format(m)}M ₸"
        }
        amount >= 1_000 -> "${amount / 1_000}K ₸"
        else -> "$amount ₸"
    }

    fun getStatusLabel(status: String): String = when (status) {
        "draft" -> "Черновик"
        "registration_open" -> "Регистрация"
        "registration_closed" -> "Рег. закрыта"
        "check_in" -> "Check-in"
        "in_progress" -> "Идёт"
        "completed" -> "Завершён"
        "cancelled" -> "Отменён"
        else -> status
    }

    fun getStatusColor(status: String): Color = when (status) {
        "registration_open", "in_progress" -> ILeaderColors.PrimaryRed
        else -> ILeaderColors.ViewerColor
    }
}
