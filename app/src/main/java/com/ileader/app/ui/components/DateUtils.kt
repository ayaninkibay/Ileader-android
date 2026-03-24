package com.ileader.app.ui.components

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Форматирует ISO-дату в формат "20.02.2026 / 20:00".
 * "2026-02-20T20:00:00+00:00" → "20.02.2026 / 20:00"
 * "2026-02-20" → "20.02.2026"
 */
fun formatShortDate(raw: String?): String {
    if (raw.isNullOrEmpty()) return ""
    return try {
        val clean = raw.substringBefore("+").substringBefore("Z")
        if (clean.contains("T")) {
            val dt = LocalDateTime.parse(clean)
            "%02d.%02d.%d / %02d:%02d".format(
                dt.dayOfMonth, dt.monthValue, dt.year,
                dt.hour, dt.minute
            )
        } else {
            val d = LocalDate.parse(clean)
            "%02d.%02d.%d".format(d.dayOfMonth, d.monthValue, d.year)
        }
    } catch (_: Exception) {
        raw
    }
}
