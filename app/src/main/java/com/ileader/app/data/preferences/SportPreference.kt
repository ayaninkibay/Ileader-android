package com.ileader.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SportPreference(private val context: Context) {

    private val sportIdsKey = stringPreferencesKey("selected_sport_ids")
    private val sportNamesKey = stringPreferencesKey("selected_sport_names")

    val selectedSportIds: Flow<List<String>> = context.ileaderDataStore.data.map { prefs ->
        val json = prefs[sportIdsKey]
        if (json.isNullOrEmpty()) emptyList()
        else try { Json.decodeFromString<List<String>>(json) } catch (_: Exception) { emptyList() }
    }

    val selectedSportNames: Flow<List<String>> = context.ileaderDataStore.data.map { prefs ->
        val json = prefs[sportNamesKey]
        if (json.isNullOrEmpty()) emptyList()
        else try { Json.decodeFromString<List<String>>(json) } catch (_: Exception) { emptyList() }
    }

    suspend fun setSports(ids: List<String>, names: List<String>) {
        context.ileaderDataStore.edit { prefs ->
            prefs[sportIdsKey] = Json.encodeToString(ids)
            prefs[sportNamesKey] = Json.encodeToString(names)
        }
    }

    suspend fun clearSports() {
        context.ileaderDataStore.edit { prefs ->
            prefs.remove(sportIdsKey)
            prefs.remove(sportNamesKey)
        }
    }
}
