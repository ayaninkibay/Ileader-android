package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.TournamentHelperDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class HelperRepository {
    private val client = SupabaseModule.client

    /**
     * Get all active tournament helper assignments for a user
     */
    suspend fun getMyAssignments(userId: String): List<TournamentHelperDto> {
        return client.from("tournament_helpers")
            .select(Columns.raw("""
                id, tournament_id, user_id, assigned_by, status, created_at,
                tournaments(id, name, status, start_date, end_date, image_url, has_check_in,
                    sports(id, name)
                )
            """.trimIndent())) {
                filter {
                    eq("user_id", userId)
                    eq("status", "active")
                }
            }
            .decodeList<TournamentHelperDto>()
    }

    /**
     * Check if a user is a helper for a specific tournament
     */
    suspend fun isHelperForTournament(userId: String, tournamentId: String): Boolean {
        val result = client.from("tournament_helpers")
            .select(Columns.raw("id")) {
                filter {
                    eq("user_id", userId)
                    eq("tournament_id", tournamentId)
                    eq("status", "active")
                }
            }
            .decodeList<IdOnlyDto>()
        return result.isNotEmpty()
    }
}

@kotlinx.serialization.Serializable
private data class IdOnlyDto(val id: String = "")
