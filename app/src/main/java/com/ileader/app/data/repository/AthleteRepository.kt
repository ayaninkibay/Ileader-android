package com.ileader.app.data.repository

import com.ileader.app.data.models.*
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.util.safeApiCall
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AthleteRepository {
    private val client = SupabaseModule.client

    // ── LICENSE ──

    suspend fun getLicense(userId: String): License? {
        val licenses = client.from("licenses")
            .select(Columns.raw("id, user_id, number, category, class, federation, status, issue_date, expiry_date, medical_check_date, medical_check_expiry, created_at, updated_at")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<LicenseDto>()
        return licenses.firstOrNull()?.toDomain()
    }

    // ── PROFILE ──

    suspend fun getProfile(userId: String): User = safeApiCall("AthleteRepo.getProfile") {
        val dto = client.from("profiles")
            .select(Columns.raw("*, roles!primary_role_id(id, name)")) {
                filter { eq("id", userId) }
            }
            .decodeSingle<ProfileDto>()
        dto.toDomain()
    }

    suspend fun updateProfile(userId: String, data: ProfileUpdateDto) {
        client.from("profiles")
            .update(data) {
                filter { eq("id", userId) }
            }
    }

    suspend fun getSports(userId: String): List<Pair<String, String>> {
        val userSports = client.from("user_sports")
            .select(Columns.raw("*, sports(id, name)")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<UserSportDto>()
        return userSports.mapNotNull { us ->
            us.sports?.let { it.name to it.id }
        }
    }

    suspend fun getAllSports(): List<Pair<String, String>> {
        val sports = client.from("sports")
            .select {
                filter { eq("is_active", true) }
            }
            .decodeList<SportDto>()
        return sports.map { it.name to it.id }
    }

    // ── STATISTICS ──

    suspend fun getStats(userId: String): AthleteStats {
        val stats = client.from("v_user_sport_stats")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<UserSportStatsDto>()

        return AthleteStats(
            totalTournaments = stats.sumOf { it.tournaments },
            wins = stats.sumOf { it.wins },
            rating = stats.maxOfOrNull { it.rating } ?: 1000,
            podiums = stats.sumOf { it.podiums },
            points = stats.sumOf { it.totalPoints },
            accuracy = if (stats.sumOf { it.tournaments } > 0)
                (stats.sumOf { it.wins }.toFloat() / stats.sumOf { it.tournaments } * 100)
            else 0f
        )
    }

    // ── TOURNAMENTS ──

    suspend fun getMyTournaments(userId: String): List<Tournament> = safeApiCall("AthleteRepo.getMyTournaments") {
        val participants = client.from("tournament_participants")
            .select(Columns.raw("*, tournaments(*, sports(id, name), locations(name, city), profiles!organizer_id(name))")) {
                filter {
                    eq("athlete_id", userId)
                    neq("status", "cancelled")
                }
            }
            .decodeList<ParticipantDto>()

        participants.mapNotNull { p ->
            p.tournaments?.toDomain()
        }
    }

    suspend fun getAvailableTournaments(): List<Tournament> {
        val tournaments = client.from("v_tournament_with_counts")
            .select {
                filter { eq("visibility", "public") }
                order("start_date", Order.ASCENDING)
            }
            .decodeList<TournamentWithCountsDto>()

        return tournaments.map { it.toDomain() }
    }

    suspend fun getTournamentDetail(tournamentId: String): Tournament {
        val dto = client.from("tournaments")
            .select(Columns.raw("*, sports(id, name), locations(name, city), profiles!organizer_id(name)")) {
                filter { eq("id", tournamentId) }
            }
            .decodeSingle<TournamentDto>()

        val participantCount = client.from("tournament_participants")
            .select(Columns.raw("id")) {
                filter {
                    eq("tournament_id", tournamentId)
                    neq("status", "cancelled")
                }
            }
            .decodeList<IdOnlyDto>()
            .size

        return dto.toDomain().copy(currentParticipants = participantCount)
    }

    suspend fun getMyParticipation(tournamentId: String, userId: String): Boolean {
        val result = client.from("tournament_participants")
            .select {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("athlete_id", userId)
                    neq("status", "cancelled")
                }
            }
            .decodeList<ParticipantDto>()
        return result.isNotEmpty()
    }

    suspend fun registerForTournament(tournamentId: String, userId: String) = safeApiCall("AthleteRepo.registerForTournament") {
        client.from("tournament_participants")
            .insert(ParticipantInsertDto(
                tournamentId = tournamentId,
                athleteId = userId,
                status = "pending"
            ))
    }

    suspend fun cancelRegistration(tournamentId: String, userId: String) {
        client.from("tournament_participants")
            .delete {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("athlete_id", userId)
                }
            }
    }

    suspend fun joinByInviteCode(code: String): String {
        val result = client.postgrest.rpc("use_invite_code", buildJsonObject {
            put("code", code)
        })
        return result.data
    }

    // ── RESULTS ──

    suspend fun getMyResults(userId: String): List<TournamentResult> = safeApiCall("AthleteRepo.getMyResults") {
        val results = client.from("tournament_results")
            .select(Columns.raw("*, tournaments(id, name, start_date, sport_id, sports(id, name))")) {
                filter { eq("athlete_id", userId) }
            }
            .decodeList<ResultDto>()

        results.map { it.toDomain() }
    }

    // ── GOALS ──

    suspend fun getGoals(userId: String): List<AthleteGoal> {
        val goals = client.from("athlete_goals")
            .select {
                filter { eq("athlete_id", userId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<GoalDto>()

        return goals.map { it.toDomain() }
    }

    suspend fun createGoal(goal: GoalInsertDto) {
        client.from("athlete_goals").insert(goal)
    }

    suspend fun updateGoal(goalId: String, data: GoalUpdateDto) {
        client.from("athlete_goals")
            .update(data) {
                filter { eq("id", goalId) }
            }
    }

    suspend fun deleteGoal(goalId: String) {
        client.from("athlete_goals")
            .delete {
                filter { eq("id", goalId) }
            }
    }

    // ── TEAM ──

    suspend fun getTeam(teamId: String): Team {
        val teamDto = client.from("teams")
            .select(Columns.raw("*, sports(id, name), profiles!owner_id(name)")) {
                filter { eq("id", teamId) }
            }
            .decodeSingle<TeamDto>()

        val membersDto = client.from("team_members")
            .select(Columns.raw("*, profiles(id, name, avatar_url)")) {
                filter { eq("team_id", teamId) }
            }
            .decodeList<TeamMemberDto>()

        // Get stats for each member from v_user_sport_stats
        val membersList = membersDto.map { m ->
            val memberId = m.profiles?.id ?: ""
            TeamMember(
                id = memberId,
                name = m.profiles?.name ?: "",
                role = when (m.role) {
                    "captain" -> "Капитан"
                    "member" -> "Участник"
                    "reserve" -> "Запасной"
                    else -> m.role ?: "Участник"
                },
                avatarUrl = m.profiles?.avatarUrl
            )
        }

        return Team(
            id = teamDto.id,
            name = teamDto.name,
            logoUrl = teamDto.logoUrl,
            sportId = teamDto.sportId ?: "",
            sportName = teamDto.sports?.name ?: "",
            trainerId = teamDto.ownerId ?: "",
            trainerName = teamDto.profiles?.name ?: "",
            foundedDate = teamDto.foundedYear?.toString() ?: "",
            description = teamDto.description ?: "",
            members = membersList
        )
    }

    // ── NOTIFICATIONS ──

    suspend fun getTournamentInvites(userId: String): List<TournamentInvite> {
        val invites = client.from("tournament_invites")
            .select(Columns.raw("*, tournaments(id, name)")) {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<TournamentInviteDto>()

        return invites.map { inv ->
            TournamentInvite(
                id = inv.id,
                tournamentId = inv.tournamentId,
                tournamentName = inv.tournaments?.name ?: "",
                status = when (inv.status) {
                    "accepted" -> InviteStatus.ACCEPTED
                    "declined" -> InviteStatus.DECLINED
                    else -> InviteStatus.PENDING
                },
                message = inv.message,
                createdAt = inv.createdAt ?: ""
            )
        }
    }

    suspend fun getTeamRequests(userId: String): List<TeamRequest> {
        val requests = client.from("team_requests")
            .select(Columns.raw("*, teams(id, name)")) {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<TeamRequestDto>()

        return requests.map { req ->
            TeamRequest(
                id = req.id,
                teamId = req.teamId,
                teamName = req.teams?.name ?: "",
                status = when (req.status) {
                    "accepted" -> InviteStatus.ACCEPTED
                    "declined" -> InviteStatus.DECLINED
                    else -> InviteStatus.PENDING
                },
                message = req.message,
                responseMessage = req.responseMessage,
                createdAt = req.createdAt ?: ""
            )
        }
    }

    suspend fun respondToInvite(inviteId: String, accept: Boolean) {
        client.from("tournament_invites")
            .update(mapOf("status" to if (accept) "accepted" else "declined")) {
                filter { eq("id", inviteId) }
            }
    }

    suspend fun respondToTeamRequest(requestId: String, accept: Boolean) {
        client.from("team_requests")
            .update(mapOf("status" to if (accept) "accepted" else "declined")) {
                filter { eq("id", requestId) }
            }
    }

}
