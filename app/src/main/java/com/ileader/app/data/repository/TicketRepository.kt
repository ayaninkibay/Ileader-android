package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.util.safeApiCall
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class TicketRepository {
    private val client = SupabaseModule.client

    /**
     * Get all tickets for a user — both as athlete participant and as spectator.
     * Returns unified list sorted by tournament start_date.
     */
    suspend fun getMyTickets(userId: String): List<TicketItem> = safeApiCall("TicketRepo.getMyTickets") {
        val athleteTickets = client.from("tournament_participants")
            .select(Columns.raw("tournament_id, athlete_id, status, check_in_status, tournaments(id, name, status, start_date, sports(name))")) {
                filter {
                    eq("athlete_id", userId)
                    neq("status", "withdrawn")
                }
            }
            .decodeList<ParticipantTicketDto>()
            .filter { it.tournaments?.status in listOf("registration_open", "registration_closed", "check_in", "in_progress") }
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

        val spectatorTickets = client.from("tournament_spectators")
            .select(Columns.raw("tournament_id, user_id, check_in_status, tournaments(id, name, status, start_date, sports(name))")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<SpectatorTicketDto>()
            .filter { it.tournaments?.status in listOf("registration_open", "registration_closed", "check_in", "in_progress") }
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

        val refereeTickets = client.from("tournament_referees")
            .select(Columns.raw("tournament_id, referee_id, check_in_status, tournaments(id, name, status, start_date, sports(name))")) {
                filter { eq("referee_id", userId) }
            }
            .decodeList<RefereeTicketDto>()
            .filter { it.tournaments?.status in listOf("registration_open", "registration_closed", "check_in", "in_progress") }
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

        (athleteTickets + spectatorTickets + refereeTickets).sortedBy { it.startDate }
    }

    /**
     * Quick check — does the user have any active tickets?
     */
    suspend fun hasActiveTickets(userId: String): Boolean = safeApiCall("TicketRepo.hasActiveTickets") {
        val participantCount = client.from("tournament_participants")
            .select(Columns.raw("tournament_id")) {
                filter {
                    eq("athlete_id", userId)
                    neq("status", "withdrawn")
                }
            }
            .decodeList<IdOnlyTicketDto>()

        if (participantCount.isNotEmpty()) return@safeApiCall true

        val spectatorCount = client.from("tournament_spectators")
            .select(Columns.raw("tournament_id")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<IdOnlyTicketDto>()

        if (spectatorCount.isNotEmpty()) return@safeApiCall true

        val refereeCount = client.from("tournament_referees")
            .select(Columns.raw("tournament_id")) {
                filter { eq("referee_id", userId) }
            }
            .decodeList<IdOnlyTicketDto>()

        refereeCount.isNotEmpty()
    }
}

@kotlinx.serialization.Serializable
private data class IdOnlyTicketDto(@kotlinx.serialization.SerialName("tournament_id") val tournamentId: String = "")
