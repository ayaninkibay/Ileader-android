package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class SponsorRepository {
    private val client = SupabaseModule.client

    suspend fun getMySponsorships(sponsorId: String): List<TournamentSponsorshipDto> {
        return client.from("tournament_sponsorships")
            .select(
                Columns.raw(
                    "sponsor_id, tournament_id, tier, amount, " +
                    "tournaments(id, name, start_date, status, sports(id, name), locations(name, city), organizer_id)"
                )
            ) {
                filter { eq("sponsor_id", sponsorId) }
            }
            .decodeList<TournamentSponsorshipDto>()
            .sortedByDescending { it.tournaments?.startDate ?: "" }
    }

    suspend fun getStats(sponsorId: String): SponsorStats {
        val items = getMySponsorships(sponsorId)
        val active = items.count {
            val status = it.tournaments?.status
            status != null && status != "completed" && status != "cancelled"
        }
        return SponsorStats(
            totalSponsored = items.size,
            activeSponsorships = active,
            totalAmount = items.sumOf { it.amount ?: 0.0 }
        )
    }

    suspend fun searchTournaments(query: String, limit: Int = 50): List<TournamentWithCountsDto> {
        return client.from("v_tournament_with_counts")
            .select {
                filter {
                    neq("status", "completed")
                    neq("status", "cancelled")
                    if (query.isNotBlank()) {
                        ilike("name", "%$query%")
                    }
                }
                order("start_date", Order.ASCENDING)
                limit(limit.toLong())
            }
            .decodeList<TournamentWithCountsDto>()
    }

    suspend fun getSponsoredTournamentIds(sponsorId: String): Set<String> {
        return client.from("tournament_sponsorships")
            .select(Columns.raw("tournament_id")) {
                filter { eq("sponsor_id", sponsorId) }
            }
            .decodeList<TournamentSponsorshipDto>()
            .map { it.tournamentId }
            .toSet()
    }

    suspend fun createSponsorship(sponsorId: String, tournamentId: String, tier: String, amount: Double) {
        client.from("tournament_sponsorships")
            .insert(
                TournamentSponsorshipInsertDto(
                    sponsorId = sponsorId,
                    tournamentId = tournamentId,
                    tier = tier,
                    amount = amount
                )
            )
    }

    suspend fun deleteSponsorship(sponsorId: String, tournamentId: String) {
        client.from("tournament_sponsorships")
            .delete {
                filter {
                    eq("sponsor_id", sponsorId)
                    eq("tournament_id", tournamentId)
                }
            }
    }
}
