package com.ileader.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FavoritesPreference(private val context: Context) {

    private val key = stringPreferencesKey("favorite_tournament_ids")

    val favoriteTournamentIds: Flow<List<String>> = context.ileaderDataStore.data.map { prefs ->
        val json = prefs[key]
        if (json.isNullOrEmpty()) emptyList()
        else try { Json.decodeFromString<List<String>>(json) } catch (_: Exception) { emptyList() }
    }

    suspend fun toggleFavorite(tournamentId: String) {
        val current = favoriteTournamentIds.first()
        val updated = if (tournamentId in current) current - tournamentId else current + tournamentId
        save(updated)
    }

    suspend fun isFavorite(tournamentId: String): Boolean {
        return tournamentId in favoriteTournamentIds.first()
    }

    suspend fun addFavorite(tournamentId: String) {
        val current = favoriteTournamentIds.first()
        if (tournamentId !in current) save(current + tournamentId)
    }

    suspend fun removeFavorite(tournamentId: String) {
        val current = favoriteTournamentIds.first()
        save(current - tournamentId)
    }

    private suspend fun save(ids: List<String>) {
        context.ileaderDataStore.edit { prefs ->
            prefs[key] = Json.encodeToString(ids)
        }
    }
}
