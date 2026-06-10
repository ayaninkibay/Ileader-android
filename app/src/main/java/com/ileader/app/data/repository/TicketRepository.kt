package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.util.MemoryCache
import com.ileader.app.data.util.safeApiCall
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class TicketRepository {
    private val client = SupabaseModule.client

    private val activeStatuses = listOf("registration_open", "registration_closed", "check_in", "in_progress")

    /**
     * Get all tickets for a user — both as athlete participant and as spectator.
     * Returns unified list sorted by tournament start_date.
     *
     * Three role tables are read in parallel and the result is cached for 30s
     * so repeated visits to "Мои билеты" don't replay all three round-trips.
     */
    suspend fun getMyTickets(userId: String): List<TicketItem> =
        MemoryCache.cached("tickets:$userId", ttlMs = 30_000L) {
            safeApiCall("TicketRepo.getMyTickets") {
                coroutineScope {
                    val athleteDef = async {
                        client.from("tournament_participants")
                            .select(Columns.raw("tournament_id, athlete_id, status, check_in_status, tournaments(id, name, status, start_date, sports(name))")) {
                                filter {
                                    eq("athlete_id", userId)
                                    neq("status", "cancelled")
                                }
                            }
                            .decodeList<ParticipantTicketDto>()
                            .filter { it.tournaments?.status in activeStatuses }
                            .map { p ->
                                TicketItem(
                                    tournamentId = p.tournamentId,
                                    tournamentName = p.tournaments?.name ?: "",
                                    sportName = p.tournaments?.sports?.name,
                                    startDate = p.tournaments?.startDate,
                                    status = p.tournaments?.status,
                                    checkInStatus = p.checkInStatus,
                                    type = "athlete"
                                )
                            }
                    }

                    val spectatorDef = async {
                        client.from("tournament_spectators")
                            .select(Columns.raw("tournament_id, user_id, check_in_status, tournaments(id, name, status, start_date, sports(name))")) {
                                filter { eq("user_id", userId) }
                            }
                            .decodeList<SpectatorTicketDto>()
                            .filter { it.tournaments?.status in activeStatuses }
                            .map { s ->
                                TicketItem(
                                    tournamentId = s.tournamentId,
                                    tournamentName = s.tournaments?.name ?: "",
                                    sportName = s.tournaments?.sports?.name,
                                    startDate = s.tournaments?.startDate,
                                    status = s.tournaments?.status,
                                    checkInStatus = s.checkInStatus,
                                    type = "spectator"
                                )
                            }
                    }

                    val refereeDef = async {
                        client.from("tournament_referees")
                            .select(Columns.raw("tournament_id, referee_id, check_in_status, tournaments(id, name, status, start_date, sports(name))")) {
                                filter { eq("referee_id", userId) }
                            }
                            .decodeList<RefereeTicketDto>()
                            .filter { it.tournaments?.status in activeStatuses }
                            .map { r ->
                                TicketItem(
                                    tournamentId = r.tournamentId,
                                    tournamentName = r.tournaments?.name ?: "",
                                    sportName = r.tournaments?.sports?.name,
                                    startDate = r.tournaments?.startDate,
                                    status = r.tournaments?.status,
                                    checkInStatus = r.checkInStatus,
                                    type = "referee"
                                )
                            }
                    }

                    (athleteDef.await() + spectatorDef.await() + refereeDef.await()).sortedBy { it.startDate }
                }
            }
        }

    /**
     * Quick check — does the user have any active tickets?
     * Uses head+count so no row payloads are decoded.
     */
    suspend fun hasActiveTickets(userId: String): Boolean = safeApiCall("TicketRepo.hasActiveTickets") {
        val participantCount = client.from("tournament_participants")
            .select(Columns.raw("tournament_id")) {
                count(Count.EXACT)
                filter {
                    eq("athlete_id", userId)
                    neq("status", "cancelled")
                }
            }
            .countOrNull() ?: 0L
        if (participantCount > 0L) return@safeApiCall true

        val spectatorCount = client.from("tournament_spectators")
            .select(Columns.raw("tournament_id")) {
                count(Count.EXACT)
                filter { eq("user_id", userId) }
            }
            .countOrNull() ?: 0L
        if (spectatorCount > 0L) return@safeApiCall true

        val refereeCount = client.from("tournament_referees")
            .select(Columns.raw("tournament_id")) {
                count(Count.EXACT)
                filter { eq("referee_id", userId) }
            }
            .countOrNull() ?: 0L
        refereeCount > 0L
    }
}
