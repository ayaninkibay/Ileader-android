package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

/**
 * Dedicated repository for Leagues feature.
 * Thin wrapper around ViewerRepository league methods for consistency with
 * the task spec (getAll / getDetail / getStandings / getStages).
 */
class LeagueRepository {
    private val client = SupabaseModule.client

    suspend fun getAll(sportId: String? = null): List<LeagueDto> {
        return client.from("leagues")
            .select(Columns.raw("id, name, sport_id, organizer_id, description, image_url, season, status, total_stages, best_of, scoring_table, allow_late_join, entry_fee, entry_fee_per_stage, created_at, sports(id, name), profiles(id, name)")) {
                filter {
                    if (sportId != null) eq("sport_id", sportId)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<LeagueDto>()
    }

    suspend fun getDetail(leagueId: String): LeagueDto {
        return client.from("leagues")
            .select(Columns.raw("id, name, sport_id, organizer_id, description, image_url, season, status, total_stages, best_of, scoring_table, allow_late_join, entry_fee, entry_fee_per_stage, created_at, sports(id, name), profiles(id, name)")) {
                filter { eq("id", leagueId) }
            }
            .decodeSingle<LeagueDto>()
    }

    suspend fun getStages(leagueId: String): List<LeagueStageDto> {
        return client.from("league_stages")
            .select(Columns.raw("id, league_id, tournament_id, stage_number, title, status, created_at, tournaments(id, name, start_date)")) {
                filter { eq("league_id", leagueId) }
                order("stage_number", Order.ASCENDING)
            }
            .decodeList<LeagueStageDto>()
    }

    suspend fun getStandings(leagueId: String): List<LeagueStandingDto> {
        return client.from("league_standings")
            .select(Columns.raw("id, league_id, athlete_id, total_points, stages_participated, best_finish, worst_finish, updated_at, profiles(id, name, avatar_url)")) {
                filter { eq("league_id", leagueId) }
                order("total_points", Order.DESCENDING)
            }
            .decodeList<LeagueStandingDto>()
    }

    suspend fun getSports(): List<SportDto> {
        return client.from("sports")
            .select { filter { eq("is_active", true) } }
            .decodeList<SportDto>()
    }
}
