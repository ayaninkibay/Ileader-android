package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.ParticipantDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class CheckInRepository {
    private val client = SupabaseModule.client

    suspend fun getParticipant(tournamentId: String, athleteId: String): ParticipantDto? {
        return client.from("tournament_participants")
            .select(Columns.raw("tournament_id, athlete_id, team_id, status, check_in_status, number, profiles(id, name, avatar_url, city), teams(id, name)")) {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("athlete_id", athleteId)
                }
            }
            .decodeSingleOrNull<ParticipantDto>()
    }

    suspend fun markCheckIn(tournamentId: String, athleteId: String) {
        client.from("tournament_participants")
            .update({ set("check_in_status", "checked_in") }) {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("athlete_id", athleteId)
                }
            }
    }
}
