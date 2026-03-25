package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.ParticipantDto
import com.ileader.app.data.remote.dto.RefereeCheckInDto
import com.ileader.app.data.remote.dto.SpectatorDto
import com.ileader.app.data.util.safeApiCall
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class CheckInRepository {
    private val client = SupabaseModule.client

    // ── Athletes (tournament_participants) ──

    suspend fun getParticipant(tournamentId: String, athleteId: String): ParticipantDto? = safeApiCall("CheckInRepo.getParticipant") {
        client.from("tournament_participants")
            .select(Columns.raw("tournament_id, athlete_id, team_id, status, check_in_status, number, profiles(id, name, avatar_url, city), teams(id, name)")) {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("athlete_id", athleteId)
                }
            }
            .decodeSingleOrNull<ParticipantDto>()
    }

    suspend fun markParticipantCheckIn(tournamentId: String, athleteId: String) = safeApiCall("CheckInRepo.markParticipantCheckIn") {
        client.from("tournament_participants")
            .update({ set("check_in_status", "checked_in") }) {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("athlete_id", athleteId)
                }
            }
    }

    // ── Spectators / Media / Trainers (tournament_spectators) ──

    suspend fun getSpectator(tournamentId: String, userId: String): SpectatorDto? = safeApiCall("CheckInRepo.getSpectator") {
        client.from("tournament_spectators")
            .select(Columns.raw("id, tournament_id, user_id, ticket_type, payment_status, check_in_status, created_at, profiles(id, name, avatar_url, city)")) {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<SpectatorDto>()
    }

    suspend fun markSpectatorCheckIn(tournamentId: String, userId: String) = safeApiCall("CheckInRepo.markSpectatorCheckIn") {
        client.from("tournament_spectators")
            .update({ set("check_in_status", "checked_in") }) {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("user_id", userId)
                }
            }
    }

    // ── Referees (tournament_referees) ──

    suspend fun getReferee(tournamentId: String, refereeId: String): RefereeCheckInDto? = safeApiCall("CheckInRepo.getReferee") {
        client.from("tournament_referees")
            .select(Columns.raw("tournament_id, referee_id, role, check_in_status, profiles!referee_id(id, name, avatar_url, city)")) {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("referee_id", refereeId)
                }
            }
            .decodeSingleOrNull<RefereeCheckInDto>()
    }

    suspend fun markRefereeCheckIn(tournamentId: String, refereeId: String) = safeApiCall("CheckInRepo.markRefereeCheckIn") {
        client.from("tournament_referees")
            .update({ set("check_in_status", "checked_in") }) {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("referee_id", refereeId)
                }
            }
    }

    // ── Manual search ──

    data class AttendeeInfo(
        val userId: String,
        val name: String,
        val city: String?,
        val avatarUrl: String?,
        val type: String, // athlete, spectator, referee
        val checkInStatus: String?,
        val extra: String? = null // team name, role, ticket_type
    )

    suspend fun searchAttendees(tournamentId: String, query: String): List<AttendeeInfo> = safeApiCall("CheckInRepo.searchAttendees") {
        val results = mutableListOf<AttendeeInfo>()

        // Athletes
        val athletes = client.from("tournament_participants")
            .select(Columns.raw("athlete_id, status, check_in_status, profiles!athlete_id(id, name, avatar_url, city), teams(name)")) {
                filter {
                    eq("tournament_id", tournamentId)
                    neq("status", "withdrawn")
                }
            }
            .decodeList<ParticipantDto>()

        athletes.filter { it.profiles?.name?.contains(query, ignoreCase = true) == true }
            .forEach { p ->
                results.add(AttendeeInfo(
                    userId = p.athleteId,
                    name = p.profiles?.name ?: "",
                    city = p.profiles?.city,
                    avatarUrl = p.profiles?.avatarUrl,
                    type = "athlete",
                    checkInStatus = p.checkInStatus,
                    extra = p.teams?.name
                ))
            }

        // Spectators / media / trainers
        val spectators = client.from("tournament_spectators")
            .select(Columns.raw("user_id, ticket_type, check_in_status, profiles(id, name, avatar_url, city)")) {
                filter { eq("tournament_id", tournamentId) }
            }
            .decodeList<SpectatorDto>()

        spectators.filter { it.profiles?.name?.contains(query, ignoreCase = true) == true }
            .forEach { s ->
                results.add(AttendeeInfo(
                    userId = s.userId,
                    name = s.profiles?.name ?: "",
                    city = s.profiles?.city,
                    avatarUrl = s.profiles?.avatarUrl,
                    type = "spectator",
                    checkInStatus = s.checkInStatus,
                    extra = when (s.ticketType) {
                        "media" -> "СМИ"
                        "trainer" -> "Тренер"
                        else -> "Зритель"
                    }
                ))
            }

        // Referees
        val referees = client.from("tournament_referees")
            .select(Columns.raw("referee_id, role, check_in_status, profiles!referee_id(id, name, avatar_url, city)")) {
                filter { eq("tournament_id", tournamentId) }
            }
            .decodeList<RefereeCheckInDto>()

        referees.filter { it.profiles?.name?.contains(query, ignoreCase = true) == true }
            .forEach { r ->
                results.add(AttendeeInfo(
                    userId = r.refereeId,
                    name = r.profiles?.name ?: "",
                    city = r.profiles?.city,
                    avatarUrl = r.profiles?.avatarUrl,
                    type = "referee",
                    checkInStatus = r.checkInStatus,
                    extra = "Судья"
                ))
            }

        results
    }
}
