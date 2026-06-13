package com.ileader.app.data

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.util.AppLogger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable

/** Страна для регистрации (1:1 с web `src/data/constants.ts` COUNTRIES). */
data class CountryOption(val code: String, val name: String)

object RegistrationSupport {

    val countries: List<CountryOption> = listOf(
        CountryOption("KZ", "Казахстан"),
        CountryOption("RU", "Россия"),
        CountryOption("UZ", "Узбекистан"),
        CountryOption("KG", "Кыргызстан"),
        CountryOption("TJ", "Таджикистан"),
        CountryOption("TM", "Туркменистан"),
        CountryOption("AZ", "Азербайджан"),
        CountryOption("GE", "Грузия"),
        CountryOption("AM", "Армения"),
        CountryOption("BY", "Беларусь"),
        CountryOption("UA", "Украина"),
        CountryOption("TR", "Турция"),
        CountryOption("AE", "ОАЭ"),
        CountryOption("US", "США"),
        CountryOption("DE", "Германия"),
        CountryOption("GB", "Великобритания"),
        CountryOption("CN", "Китай"),
        CountryOption("JP", "Япония"),
        CountryOption("KR", "Южная Корея"),
    )

    fun countryName(code: String): String =
        countries.firstOrNull { it.code == code }?.name ?: code

    /** Фолбэк-список KZ — совпадает с web `CITIES` константой. */
    val fallbackCitiesKZ = listOf(
        "Алматы", "Астана", "Шымкент", "Караганда", "Актау",
        "Атырау", "Павлодар", "Семей", "Актобе", "Тараз",
    )

    @Serializable
    private data class CityRow(val name: String)

    /**
     * Активные города страны из таблицы `cities`, отсортированы как на web
     * (sort_order, name). При ошибке/пустоте — фолбэк (для KZ) или [].
     * Источник истины — БД, чтобы в profiles.city не было разнобоя.
     */
    suspend fun fetchCities(country: String = "KZ"): List<String> = try {
        val rows = SupabaseModule.client.from("cities")
            .select(Columns.raw("name")) {
                filter {
                    eq("country", country)
                    eq("is_active", true)
                }
                order("sort_order", Order.ASCENDING)
                order("name", Order.ASCENDING)
            }
            .decodeList<CityRow>()
        val names = rows.map { it.name }.filter { it.isNotBlank() }
        names.ifEmpty { if (country == "KZ") fallbackCitiesKZ else emptyList() }
    } catch (e: Exception) {
        AppLogger.w("RegistrationSupport.fetchCities failed: ${e.message}", e)
        if (country == "KZ") fallbackCitiesKZ else emptyList()
    }

    /** Маска телефона "+7 (777) 123-45-67" (1:1 с web formatPhone). */
    fun formatPhone(raw: String): String {
        var digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        if (digits.startsWith("8") && digits.length >= 11) {
            digits = "7" + digits.substring(1)
        }
        fun s(lo: Int, hi: Int) = digits.substring(lo, minOf(hi, digits.length))
        return when (digits.length) {
            in 0..1 -> "+$digits"
            in 2..4 -> "+${s(0,1)} (${s(1,4)}"
            in 5..7 -> "+${s(0,1)} (${s(1,4)}) ${s(4,7)}"
            in 8..9 -> "+${s(0,1)} (${s(1,4)}) ${s(4,7)}-${s(7,9)}"
            in 10..11 -> "+${s(0,1)} (${s(1,4)}) ${s(4,7)}-${s(7,9)}-${s(9,11)}"
            else -> "+$digits"
        }
    }

    /** 10–15 цифр (как web isValidPhone). */
    fun isValidPhone(value: String): Boolean {
        val n = value.count { it.isDigit() }
        return n in 10..15
    }
}
