package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.InviteCodeDto
import com.ileader.app.data.remote.dto.InviteCodeInsertDto
import com.ileader.app.data.remote.dto.TournamentHelperDto
import com.ileader.app.data.remote.dto.TournamentHelperInsertDto
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

    /**
     * Assign a user as helper for a tournament
     */
    suspend fun assignHelper(tournamentId: String, userId: String, assignedBy: String): String {
        val data = TournamentHelperInsertDto(
            tournamentId = tournamentId,
            userId = userId,
            assignedBy = assignedBy,
            status = "active"
        )
        val result = client.from("tournament_helpers")
            .insert(data) { select() }
            .decodeSingle<TournamentHelperDto>()
        return result.id
    }

    /**
     * Revoke a helper assignment
     */
    suspend fun revokeHelper(helperId: String) {
        client.from("tournament_helpers")
            .update({ set("status", "revoked") }) {
                filter { eq("id", helperId) }
            }
    }

    /**
     * Get all helpers for a tournament (for organizer view)
     */
    suspend fun getTournamentHelpers(tournamentId: String): List<TournamentHelperDto> {
        return client.from("tournament_helpers")
            .select(Columns.raw("""
                id, tournament_id, user_id, assigned_by, status, created_at,
                profiles(id, name, avatar_url, email)
            """.trimIndent())) {
                filter {
                    eq("tournament_id", tournamentId)
                }
            }
            .decodeList<TournamentHelperDto>()
    }

    /**
     * Create an invite code for helpers
     */
    suspend fun createHelperInviteCode(tournamentId: String, createdBy: String): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val randomPart = (1..6).map { chars.random() }.joinToString("")
        val code = "HLP-$randomPart"

        val data = InviteCodeInsertDto(
            tournamentId = tournamentId,
            code = code,
            type = "helper",
            createdBy = createdBy
        )
        client.from("tournament_invite_codes")
            .insert(data) { select() }
            .decodeSingle<InviteCodeDto>()
        return code
    }
}

@kotlinx.serialization.Serializable
private data class IdOnlyDto(val id: String = "")
