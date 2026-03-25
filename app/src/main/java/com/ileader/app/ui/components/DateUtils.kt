package com.ileader.app.ui.components

import java.time.LocalDate
import java.time.LocalDateTime

private val MONTHS = arrayOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря"
)

/**
 * Форматирует ISO-дату в формат "20 мая 2026г 11:30".
 * "2026-05-20T11:30:00+00:00" → "20 мая 2026г 11:30"
 * "2026-05-20" → "20 мая 2026г"
 */
fun formatShortDate(raw: String?): String {
    if (raw.isNullOrEmpty()) return ""
    return try {
        val clean = raw.substringBefore("+").substringBefore("Z")
        if (clean.contains("T")) {
            val dt = LocalDateTime.parse(clean)
            "${dt.dayOfMonth} ${MONTHS[dt.monthValue - 1]} ${dt.year}г %02d:%02d".format(
                dt.hour, dt.minute
            )
        } else {
            val d = LocalDate.parse(clean)
            "${d.dayOfMonth} ${MONTHS[d.monthValue - 1]} ${d.year}г"
        }
    } catch (_: Exception) {
        raw
    }
}
