package com.ileader.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppLanguage(val code: String, val label: String, val flag: String) {
    RUSSIAN("ru", "Русский", "🇷🇺"),
    KAZAKH("kk", "Қазақша", "🇰🇿"),
    ENGLISH("en", "English", "🇬🇧");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.find { it.code == code } ?: RUSSIAN
    }
}

class LanguagePreference(private val context: Context) {

    private val langKey = stringPreferencesKey("app_language")

    val language: Flow<AppLanguage> = context.ileaderDataStore.data.map { prefs ->
        AppLanguage.fromCode(prefs[langKey])
    }

    suspend fun setLanguage(lang: AppLanguage) {
        context.ileaderDataStore.edit { prefs ->
            prefs[langKey] = lang.code
        }
    }
}
