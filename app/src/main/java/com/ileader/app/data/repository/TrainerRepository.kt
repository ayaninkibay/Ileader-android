package com.ileader.app.data.repository

import com.ileader.app.data.models.*
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage

class TrainerRepository {
    private val client = SupabaseModule.client

    // ── PROFILE ──

    suspend fun getProfile(userId: String): User {
        val dto = client.from("profiles")
            .select(Columns.raw("*, roles!primary_role_id(id, name)")) {
                filter { eq("id", userId) }
            }
            .decodeSingle<ProfileDto>()
        return dto.toDomain()
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

    // ── TEAMS ──

    suspend fun getMyTeams(userId: String): List<TrainerTeamData> {
        val teams = client.from("teams")
            .select(Columns.raw("*, sports(id, name)")) {
                filter { eq("owner_id", userId) }
            }
            .decodeList<TeamDto>()

        return teams.map { teamDto ->
            val members = getTeamMembers(teamDto.id, teamDto.sportId)
            TrainerTeamData(
                id = teamDto.id,
                name = teamDto.name,
                sportId = teamDto.sportId ?: "",
                sportName = teamDto.sports?.name ?: "",
                description = teamDto.description ?: "",
                foundedYear = teamDto.foundedYear ?: 0,
                ageCategory = "", // no age_category on teams table
                members = members
            )
        }
    }

    suspend fun getTeamMembers(teamId: String, sportId: String? = null): List<TrainerAthleteData> {
        val members = client.from("team_members")
            .select(Columns.raw("*, profiles(id, name, avatar_url, email, city, birth_date, bio, created_at)")) {
                filter { eq("team_id", teamId) }
            }
            .decodeList<TeamMemberDto>()

        return members.map { m ->
            val memberId = m.profiles?.id ?: ""
            val stats = try {
                client.from("v_user_sport_stats")
                    .select {
                        filter {
                            eq("user_id", memberId)
                            if (sportId != null) eq("sport_id", sportId)
                        }
                    }
                    .decodeList<UserSportStatsDto>()
                    .firstOrNull()
            } catch (_: Exception) { null }

            TrainerAthleteData(
                id = memberId,
                name = m.profiles?.name ?: "",
                email = m.profiles?.email ?: "",
                avatarUrl = m.profiles?.avatarUrl,
                joinedDate = m.joinedAt ?: "",
                tournaments = stats?.tournaments ?: 0,
                wins = stats?.wins ?: 0,
                podiums = stats?.podiums ?: 0,
                rating = stats?.rating ?: 1000,
                bio = "",
                role = m.role ?: "member"
            )
        }
    }

    // ── TEAM REQUESTS (notifications for trainer) ──

    suspend fun getTeamRequests(userId: String): List<TrainerNotificationData> {
        // Get all teams owned by this trainer
        val teamIds = client.from("teams")
            .select(Columns.raw("id, name")) {
                filter { eq("owner_id", userId) }
            }
            .decodeList<TeamDto>()

        val allRequests = mutableListOf<TrainerNotificationData>()

        for (team in teamIds) {
            val requests = client.from("team_requests")
                .select(Columns.raw("*, profiles(id, name, email)")) {
                    filter { eq("team_id", team.id) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<TeamRequestDto>()

            allRequests.addAll(requests.map { req ->
                TrainerNotificationData(
                    id = req.id,
                    type = "join_request",
                    title = "Запрос на вступление",
                    message = req.message ?: "Запрос на вступление в команду",
                    fromName = req.profiles?.name ?: "",
                    teamName = team.name,
                    status = when (req.status) {
                        "accepted" -> InviteStatus.ACCEPTED
                        "declined" -> InviteStatus.DECLINED
                        else -> InviteStatus.PENDING
                    },
                    createdAt = req.createdAt ?: ""
                )
            })
        }

        return allRequests.sortedByDescending { it.createdAt }
    }

    suspend fun respondToTeamRequest(requestId: String, accept: Boolean, responseMessage: String? = null) {
        val updateData = mutableMapOf<String, String>(
            "status" to if (accept) "accepted" else "declined"
        )
        if (responseMessage != null) {
            updateData["response_message"] = responseMessage
        }

        client.from("team_requests")
            .update(updateData) {
                filter { eq("id", requestId) }
            }

        // If accepted, fetch request details and add user to team_members
        if (accept) {
            val request = client.from("team_requests")
                .select(Columns.raw("team_id, user_id")) {
                    filter { eq("id", requestId) }
                }
                .decodeSingle<TeamRequestDto>()

            val teamId = request.teamId
            val userId = request.userId ?: return

            client.from("team_members").insert(mapOf(
                "team_id" to teamId,
                "user_id" to userId,
                "role" to "member"
            ))
        }
    }

    // Sponsor offers removed — sponsorship management is web-only

    // ── TOURNAMENTS ──

    suspend fun getAvailableTournaments(sportIds: List<String>): List<Tournament> {
        val tournaments = client.from("v_tournament_with_counts")
            .select {
                filter { eq("visibility", "public") }
                order("start_date", Order.ASCENDING)
            }
            .decodeList<TournamentWithCountsDto>()

        return tournaments.map { it.toDomain() }
    }

    suspend fun getTeamRegisteredTournamentIds(teamId: String): List<String> {
        val participants = client.from("tournament_participants")
            .select(Columns.raw("tournament_id, athlete_id")) {
                filter {
                    eq("team_id", teamId)
                    neq("status", "cancelled")
                }
            }
            .decodeList<ParticipantDto>()

        return participants.map { it.tournamentId }
    }

    suspend fun registerTeamForTournament(tournamentId: String, teamId: String, memberIds: List<String>) {
        for (memberId in memberIds) {
            client.from("tournament_participants")
                .insert(ParticipantInsertDto(
                    tournamentId = tournamentId,
                    athleteId = memberId,
                    teamId = teamId,
                    status = "pending"
                ))
        }
    }

    suspend fun unregisterTeamFromTournament(tournamentId: String, teamId: String) {
        client.from("tournament_participants")
            .delete {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("team_id", teamId)
                }
            }
    }

    // ── ATHLETE RESULTS ──

    suspend fun getAthleteResults(athleteId: String): List<TournamentResult> {
        val results = client.from("tournament_results")
            .select(Columns.raw("*, tournaments(id, name, start_date, sport_id, sports(id, name))")) {
                filter { eq("athlete_id", athleteId) }
                order("tournaments.start_date", Order.DESCENDING)
            }
            .decodeList<ResultDto>()

        return results.map { it.toDomain() }
    }

    suspend fun getAthleteStats(athleteId: String, sportId: String?): UserSportStatsDto? {
        return try {
            val query = client.from("v_user_sport_stats")
                .select {
                    filter {
                        eq("user_id", athleteId)
                        if (sportId != null) eq("sport_id", sportId)
                    }
                }
                .decodeList<UserSportStatsDto>()
            query.firstOrNull()
        } catch (_: Exception) { null }
    }

    // ── INVITE ATHLETE ──

    suspend fun inviteAthlete(teamId: String, email: String) {
        val profile = client.from("profiles")
            .select(Columns.raw("id, name, email")) {
                filter { eq("email", email) }
            }
            .decodeList<ProfileMinimalDto>()
            .firstOrNull() ?: throw Exception("Пользователь с email $email не найден")

        client.from("team_requests").insert(mapOf(
            "team_id" to teamId,
            "user_id" to profile.id,
            "status" to "pending",
            "direction" to "outgoing",
            "message" to "Приглашение от тренера"
        ))
    }

    // ── PENDING INVITES (outgoing from trainer) ──

    suspend fun getPendingInvites(userId: String): List<PendingInviteData> {
        val teams = client.from("teams")
            .select(Columns.raw("id, name")) {
                filter { eq("owner_id", userId) }
            }
            .decodeList<TeamDto>()

        val allInvites = mutableListOf<PendingInviteData>()

        for (team in teams) {
            val requests = client.from("team_requests")
                .select(Columns.raw("*, profiles(id, name, email)")) {
                    filter {
                        eq("team_id", team.id)
                        eq("status", "pending")
                        eq("direction", "outgoing")
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<TeamRequestDto>()

            allInvites.addAll(requests.map { req ->
                PendingInviteData(
                    id = req.id,
                    athleteName = req.profiles?.name ?: "",
                    athleteEmail = req.profiles?.email ?: "",
                    teamId = team.id,
                    teamName = team.name,
                    sentAt = req.createdAt ?: "",
                    status = InviteStatus.PENDING
                )
            })
        }

        return allInvites
    }

    // ── REMOVE ATHLETE FROM TEAM ──

    suspend fun removeAthleteFromTeam(teamId: String, athleteId: String) {
        client.from("team_members")
            .delete {
                filter {
                    eq("team_id", teamId)
                    eq("user_id", athleteId)
                }
            }
    }

    // ── ATHLETE GOALS ──

    suspend fun getAthleteGoals(athleteId: String): List<GoalDto> {
        return client.from("athlete_goals")
            .select {
                filter { eq("athlete_id", athleteId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<GoalDto>()
    }

    suspend fun createGoalForAthlete(goal: GoalInsertDto) {
        client.from("athlete_goals").insert(goal)
    }

    // ── TEAM STATISTICS ──

    suspend fun getTeamStatistics(teamId: String): List<UserSportStatsDto> {
        val memberIds = client.from("team_members")
            .select(Columns.raw("user_id")) {
                filter { eq("team_id", teamId) }
            }
            .decodeList<TeamMemberDto>()
            .mapNotNull { it.userId }

        if (memberIds.isEmpty()) return emptyList()

        return client.from("v_user_sport_stats")
            .select {
                filter { isIn("user_id", memberIds) }
            }
            .decodeList<UserSportStatsDto>()
    }

    suspend fun getTeamResultsDistribution(teamId: String): List<ResultDistribution> {
        val memberIds = client.from("team_members")
            .select(Columns.raw("user_id")) {
                filter { eq("team_id", teamId) }
            }
            .decodeList<TeamMemberDto>()
            .mapNotNull { it.userId }

        if (memberIds.isEmpty()) return defaultDistribution()

        val results = client.from("tournament_results")
            .select(Columns.raw("position, tournament_id, athlete_id")) {
                filter { isIn("athlete_id", memberIds) }
            }
            .decodeList<ResultDto>()

        if (results.isEmpty()) return defaultDistribution()

        val first = results.count { it.position == 1 }
        val second = results.count { it.position == 2 }
        val third = results.count { it.position == 3 }
        val other = results.count { it.position > 3 }

        return listOf(
            ResultDistribution("1 место", first, 0xFFFFD700),
            ResultDistribution("2 место", second, 0xFFC0C0C0),
            ResultDistribution("3 место", third, 0xFFCD7F32),
            ResultDistribution("Другое", other, 0xFF6B7280)
        )
    }

    private fun defaultDistribution() = listOf(
        ResultDistribution("1 место", 0, 0xFFFFD700),
        ResultDistribution("2 место", 0, 0xFFC0C0C0),
        ResultDistribution("3 место", 0, 0xFFCD7F32),
        ResultDistribution("Другое", 0, 0xFF6B7280)
    )

}

// ── Data classes for Trainer screens ──

data class TrainerTeamData(
    val id: String,
    val name: String,
    val sportId: String,
    val sportName: String,
    val description: String,
    val foundedYear: Int,
    val ageCategory: String,
    val members: List<TrainerAthleteData>
)

data class TrainerAthleteData(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val joinedDate: String,
    val tournaments: Int,
    val wins: Int,
    val podiums: Int,
    val rating: Int,
    val bio: String = "",
    val role: String = "member"
)

data class TrainerNotificationData(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val fromName: String,
    val teamName: String? = null,
    val status: InviteStatus = InviteStatus.PENDING,
    val createdAt: String
)

data class PendingInviteData(
    val id: String,
    val athleteName: String,
    val athleteEmail: String,
    val teamId: String,
    val teamName: String,
    val sentAt: String,
    val status: InviteStatus = InviteStatus.PENDING
)

data class TrainerTeamStats(
    val athleteCount: Int,
    val totalTournaments: Int,
    val totalWins: Int,
    val totalPodiums: Int,
    val avgRating: Int,
    val winRate: Float
)

data class ResultDistribution(
    val label: String,
    val value: Int,
    val color: Long
)

