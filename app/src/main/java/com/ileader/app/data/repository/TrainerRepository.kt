package com.ileader.app.data.repository

import com.ileader.app.data.models.*
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.util.AppLogger
import com.ileader.app.data.util.MemoryCache
import com.ileader.app.data.util.escapeLikePattern
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage

class TrainerRepository {
    private val client = SupabaseModule.client

    // ── PROFILE ──

    suspend fun getProfile(userId: String): User =
        MemoryCache.cached("trainer:profile:$userId", ttlMs = 600_000L) {
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
        MemoryCache.invalidate("trainer:profile:$userId")
        MemoryCache.invalidate("profile:$userId")
        MemoryCache.invalidate("public_profile:$userId")
    }

    suspend fun getSports(userId: String): List<Pair<String, String>> =
        MemoryCache.cached("trainer:user_sport_pairs:$userId", ttlMs = 600_000L) {
            val userSports = client.from("user_sports")
                .select(Columns.raw("*, sports(id, name)")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<UserSportDto>()
            userSports.mapNotNull { us ->
                us.sports?.let { it.name to it.id }
            }
        }

    // ── TEAMS ──

    suspend fun getMyTeams(userId: String): List<TrainerTeamData> =
        MemoryCache.cached("trainer:my_teams:$userId", ttlMs = 300_000L) {
            getMyTeamsInternal(userId)
        }

    private suspend fun getMyTeamsInternal(userId: String): List<TrainerTeamData> {
        val teams = client.from("teams")
            .select(Columns.raw("*, sports(id, name)")) {
                filter { eq("owner_id", userId) }
            }
            .decodeList<TeamDto>()

        if (teams.isEmpty()) return emptyList()

        // Batch members for all teams in one query, then batch stats for all members
        // in another. Was N+1 over teams × members; now 3 queries total.
        val teamIds = teams.map { it.id }
        val allMembers = client.from("team_members")
            .select(Columns.raw("*, profiles(id, name, avatar_url, email, city, birth_date, bio, created_at)")) {
                filter { isIn("team_id", teamIds) }
            }
            .decodeList<TeamMemberDto>()

        val memberIds = allMembers.mapNotNull { it.profiles?.id }.distinct()
        val sportIds = teams.mapNotNull { it.sportId }.distinct()

        val statsByUserSport: Map<Pair<String, String>, UserSportStatsDto> =
            if (memberIds.isEmpty()) emptyMap()
            else try {
                client.from("v_user_sport_stats")
                    .select {
                        filter {
                            isIn("user_id", memberIds)
                            if (sportIds.isNotEmpty()) isIn("sport_id", sportIds)
                        }
                    }
                    .decodeList<UserSportStatsDto>()
                    .associateBy { (it.userId ?: "") to (it.sportId ?: "") }
            } catch (e: Exception) {
                AppLogger.w("TrainerRepo.getMyTeams stats: ${e.message}"); emptyMap()
            }

        val membersByTeam = allMembers.groupBy { it.teamId ?: "" }

        return teams.map { teamDto ->
            val teamSportId = teamDto.sportId ?: ""
            val members = (membersByTeam[teamDto.id] ?: emptyList()).map { m ->
                val memberId = m.profiles?.id ?: ""
                val stats = statsByUserSport[memberId to teamSportId]
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
            TrainerTeamData(
                id = teamDto.id,
                name = teamDto.name,
                sportId = teamSportId,
                sportName = teamDto.sports?.name ?: "",
                description = teamDto.description ?: "",
                foundedYear = teamDto.foundedYear ?: 0,
                ageCategory = "",
                members = members
            )
        }
    }

    suspend fun getTeamMembers(teamId: String, sportId: String? = null): List<TrainerAthleteData> =
        MemoryCache.cached("trainer:team_members:$teamId:${sportId ?: "any"}", ttlMs = 300_000L) {
            getTeamMembersInternal(teamId, sportId)
        }

    private suspend fun getTeamMembersInternal(teamId: String, sportId: String?): List<TrainerAthleteData> {
        val members = client.from("team_members")
            .select(Columns.raw("*, profiles(id, name, avatar_url, email, city, birth_date, bio, created_at)")) {
                filter { eq("team_id", teamId) }
            }
            .decodeList<TeamMemberDto>()

        if (members.isEmpty()) return emptyList()

        // Batch all members' stats in one query instead of one query per member.
        val memberIds = members.mapNotNull { it.profiles?.id }
        val statsByUser: Map<String, UserSportStatsDto> =
            if (memberIds.isEmpty()) emptyMap()
            else try {
                client.from("v_user_sport_stats")
                    .select {
                        filter {
                            isIn("user_id", memberIds)
                            if (sportId != null) eq("sport_id", sportId)
                        }
                    }
                    .decodeList<UserSportStatsDto>()
                    .associateBy { it.userId ?: "" }
            } catch (e: Exception) {
                AppLogger.w("TrainerRepo.getTeamMembers stats: ${e.message}"); emptyMap()
            }

        return members.map { m ->
            val memberId = m.profiles?.id ?: ""
            val stats = statsByUser[memberId]
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

    suspend fun getTeamRequests(userId: String): List<TrainerNotificationData> =
        MemoryCache.cached("trainer:team_requests:$userId", ttlMs = 120_000L) {
            getTeamRequestsInternal(userId)
        }

    private suspend fun getTeamRequestsInternal(userId: String): List<TrainerNotificationData> {
        // Get all teams owned by this trainer
        val teams = client.from("teams")
            .select(Columns.raw("id, name")) {
                filter { eq("owner_id", userId) }
            }
            .decodeList<TeamDto>()

        if (teams.isEmpty()) return emptyList()

        val teamIds = teams.map { it.id }
        val teamNameMap = teams.associate { it.id to it.name }

        // Single query for all team requests instead of N+1
        val allRequestDtos = client.from("team_requests")
            .select(Columns.raw("*, profiles(id, name, email)")) {
                filter { isIn("team_id", teamIds) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<TeamRequestDto>()

        return allRequestDtos.map { req ->
            TrainerNotificationData(
                id = req.id,
                type = "join_request",
                title = "Запрос на вступление",
                message = req.message ?: "Запрос на вступление в команду",
                fromName = req.profiles?.name ?: "",
                teamName = teamNameMap[req.teamId] ?: "",
                status = when (req.status) {
                    "accepted" -> InviteStatus.ACCEPTED
                    "declined" -> InviteStatus.DECLINED
                    else -> InviteStatus.PENDING
                },
                createdAt = req.createdAt ?: ""
            )
        }.sortedByDescending { it.createdAt }
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
            // Membership changed — drop team and member caches.
            MemoryCache.invalidate("trainer:team_members:$teamId:any")
            MemoryCache.invalidateMatching("trainer:team_members:$teamId:")
            MemoryCache.invalidate("athlete:team:$teamId")
            MemoryCache.invalidate("athlete:membership:$userId")
            MemoryCache.invalidate("team:members:$teamId")
            MemoryCache.invalidate("team:$teamId")
        }
        MemoryCache.invalidateMatching("trainer:team_requests:")
        MemoryCache.invalidateMatching("trainer:my_teams:")
        MemoryCache.invalidateMatching("athlete:team_requests:")
    }

    // Sponsor offers removed — sponsorship management is web-only

    // ── TOURNAMENTS ──

    suspend fun getAvailableTournaments(sportIds: List<String>): List<Tournament> =
        MemoryCache.cached("trainer:available_tournaments", ttlMs = 300_000L) {
            val tournaments = client.from("v_tournament_with_counts")
                .select {
                    filter { eq("visibility", "public") }
                    order("start_date", Order.ASCENDING)
                }
                .decodeList<TournamentWithCountsDto>()

            tournaments.map { it.toDomain() }
        }

    suspend fun createTeam(data: TeamInsertDto): String {
        val result = client.from("teams")
            .insert(data) { select() }
            .decodeSingle<TeamDto>()
        MemoryCache.invalidateMatching("trainer:my_teams:")
        MemoryCache.invalidate("community:teams")
        return result.id
    }

    suspend fun deleteTeam(teamId: String) {
        client.from("teams").delete { filter { eq("id", teamId) } }
        MemoryCache.invalidateMatching("trainer:my_teams:")
        MemoryCache.invalidate("athlete:team:$teamId")
        MemoryCache.invalidate("team:$teamId")
        MemoryCache.invalidate("team:members:$teamId")
        MemoryCache.invalidate("community:teams")
    }

    suspend fun searchAthletes(query: String): List<ProfileMinimalDto> {
        if (query.isBlank()) return emptyList()
        // Find athlete role id
        val athleteRole = client.from("roles")
            .select(Columns.raw("id")) { filter { eq("name", "athlete") } }
            .decodeSingleOrNull<IdOnlyDto>() ?: return emptyList()

        return client.from("profiles")
            .select(Columns.raw("id, name, avatar_url, city, email")) {
                filter {
                    eq("primary_role_id", athleteRole.id)
                    ilike("name", "%${query.escapeLikePattern()}%")
                }
                limit(20)
            }
            .decodeList<ProfileMinimalDto>()
    }

    suspend fun addTeamMember(teamId: String, userId: String, role: String = "member") {
        client.from("team_members").insert(TeamMemberInsertDto(teamId, userId, role))
        MemoryCache.invalidateMatching("trainer:team_members:$teamId:")
        MemoryCache.invalidateMatching("trainer:my_teams:")
        MemoryCache.invalidate("athlete:team:$teamId")
        MemoryCache.invalidate("athlete:membership:$userId")
        MemoryCache.invalidate("team:members:$teamId")
    }

    suspend fun removeTeamMember(teamId: String, userId: String) {
        client.from("team_members").delete {
            filter {
                eq("team_id", teamId)
                eq("user_id", userId)
            }
        }
        MemoryCache.invalidateMatching("trainer:team_members:$teamId:")
        MemoryCache.invalidateMatching("trainer:my_teams:")
        MemoryCache.invalidate("athlete:team:$teamId")
        MemoryCache.invalidate("athlete:membership:$userId")
        MemoryCache.invalidate("team:members:$teamId")
    }

    /**
     * Returns all tournaments that any of the trainer's teams' members participate in
     */
    suspend fun getMyTeamsTournaments(userId: String): List<TournamentWithCountsDto> =
        MemoryCache.cached("trainer:my_teams_tournaments:$userId", ttlMs = 180_000L) {
            getMyTeamsTournamentsInternal(userId)
        }

    private suspend fun getMyTeamsTournamentsInternal(userId: String): List<TournamentWithCountsDto> {
        // Get all team ids owned by trainer
        val teamIds = client.from("teams")
            .select(Columns.raw("id")) { filter { eq("owner_id", userId) } }
            .decodeList<IdOnlyDto>()
            .map { it.id }
        if (teamIds.isEmpty()) return emptyList()

        // Get tournament ids where any team member participates
        val tournamentIds = client.from("tournament_participants")
            .select(Columns.raw("tournament_id")) {
                filter {
                    isIn("team_id", teamIds)
                    neq("status", "cancelled")
                }
            }
            .decodeList<ParticipantDto>()
            .map { it.tournamentId }
            .distinct()

        if (tournamentIds.isEmpty()) return emptyList()

        return client.from("v_tournament_with_counts")
            .select { filter { isIn("id", tournamentIds) } }
            .decodeList<TournamentWithCountsDto>()
    }

    suspend fun getTeamRegisteredTournamentIds(teamId: String): List<String> =
        MemoryCache.cached("trainer:team_registrations:$teamId", ttlMs = 180_000L) {
            val participants = client.from("tournament_participants")
                .select(Columns.raw("tournament_id, athlete_id")) {
                    filter {
                        eq("team_id", teamId)
                        neq("status", "cancelled")
                    }
                }
                .decodeList<ParticipantDto>()

            participants.map { it.tournamentId }
        }

    /**
     * Of the given [teamIds], returns the subset that already has at least one
     * non-cancelled registration for [tournamentId]. Replaces an O(teams)
     * loop of single-team lookups with one query.
     */
    suspend fun getRegisteredTeamIdsForTournament(tournamentId: String, teamIds: List<String>): Set<String> {
        if (teamIds.isEmpty()) return emptySet()
        return client.from("tournament_participants")
            .select(Columns.raw("team_id")) {
                filter {
                    eq("tournament_id", tournamentId)
                    isIn("team_id", teamIds)
                    neq("status", "cancelled")
                }
            }
            .decodeList<ParticipantDto>()
            .mapNotNull { it.teamId }
            .toSet()
    }

    suspend fun registerTeamForTournament(tournamentId: String, teamId: String, memberIds: List<String>) {
        if (memberIds.isEmpty()) return
        val inserts = memberIds.map { memberId ->
            ParticipantInsertDto(
                tournamentId = tournamentId,
                athleteId = memberId,
                teamId = teamId,
                status = "pending"
            )
        }
        client.from("tournament_participants").insert(inserts)
        MemoryCache.invalidate("trainer:team_registrations:$teamId")
        MemoryCache.invalidateMatching("trainer:my_teams_tournaments:")
        MemoryCache.invalidate("participants:$tournamentId")
        MemoryCache.invalidate("tournament:$tournamentId")
        memberIds.forEach { memberId ->
            MemoryCache.invalidate("athlete:my_tournaments:$memberId")
            MemoryCache.invalidate("athlete:participation:$tournamentId:$memberId")
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
        MemoryCache.invalidate("trainer:team_registrations:$teamId")
        MemoryCache.invalidateMatching("trainer:my_teams_tournaments:")
        MemoryCache.invalidate("participants:$tournamentId")
        MemoryCache.invalidate("tournament:$tournamentId")
        MemoryCache.invalidateMatching("athlete:my_tournaments:")
        MemoryCache.invalidateMatching("athlete:participation:$tournamentId:")
    }

    // ── ATHLETE RESULTS ──

    suspend fun getAthleteResults(athleteId: String): List<TournamentResult> =
        MemoryCache.cached("trainer:athlete_results:$athleteId", ttlMs = 300_000L) {
            val results = client.from("tournament_results")
                .select(Columns.raw("*, tournaments(id, name, start_date, sport_id, sports(id, name))")) {
                    filter { eq("athlete_id", athleteId) }
                    order("tournaments.start_date", Order.DESCENDING)
                }
                .decodeList<ResultDto>()

            results.map { it.toDomain() }
        }

    suspend fun getAthleteStats(athleteId: String, sportId: String?): UserSportStatsDto? {
        val cached = MemoryCache.cached("trainer:athlete_stats:$athleteId:${sportId ?: "any"}", ttlMs = 300_000L) {
            try {
                val query = client.from("v_user_sport_stats")
                    .select {
                        filter {
                            eq("user_id", athleteId)
                            if (sportId != null) eq("sport_id", sportId)
                        }
                    }
                    .decodeList<UserSportStatsDto>()
                listOfNotNull(query.firstOrNull())
            } catch (e: Exception) { AppLogger.w("TrainerRepo: ${e.message}"); emptyList() }
        }
        return cached.firstOrNull()
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
        MemoryCache.invalidateMatching("trainer:pending_invites:")
        MemoryCache.invalidate("athlete:team_requests:${profile.id}")
    }

    // ── PENDING INVITES (outgoing from trainer) ──

    suspend fun getPendingInvites(userId: String): List<PendingInviteData> =
        MemoryCache.cached("trainer:pending_invites:$userId", ttlMs = 120_000L) {
            getPendingInvitesInternal(userId)
        }

    private suspend fun getPendingInvitesInternal(userId: String): List<PendingInviteData> {
        val teams = client.from("teams")
            .select(Columns.raw("id, name")) {
                filter { eq("owner_id", userId) }
            }
            .decodeList<TeamDto>()

        if (teams.isEmpty()) return emptyList()

        val teamIds = teams.map { it.id }
        val teamNameMap = teams.associate { it.id to it.name }

        // Single query for all pending invites instead of N+1
        val allRequestDtos = client.from("team_requests")
            .select(Columns.raw("*, profiles(id, name, email)")) {
                filter {
                    isIn("team_id", teamIds)
                    eq("status", "pending")
                    eq("direction", "outgoing")
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<TeamRequestDto>()

        return allRequestDtos.map { req ->
            PendingInviteData(
                id = req.id,
                athleteName = req.profiles?.name ?: "",
                athleteEmail = req.profiles?.email ?: "",
                teamId = req.teamId,
                teamName = teamNameMap[req.teamId] ?: "",
                sentAt = req.createdAt ?: "",
                status = InviteStatus.PENDING
            )
        }
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
        MemoryCache.invalidateMatching("trainer:team_members:$teamId:")
        MemoryCache.invalidateMatching("trainer:my_teams:")
        MemoryCache.invalidate("athlete:team:$teamId")
        MemoryCache.invalidate("athlete:membership:$athleteId")
        MemoryCache.invalidate("team:members:$teamId")
    }

    // ── ATHLETE GOALS ──

    suspend fun getAthleteGoals(athleteId: String): List<GoalDto> =
        MemoryCache.cached("trainer:athlete_goals:$athleteId", ttlMs = 300_000L) {
            client.from("athlete_goals")
                .select {
                    filter { eq("athlete_id", athleteId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<GoalDto>()
        }

    suspend fun createGoalForAthlete(goal: GoalInsertDto) {
        client.from("athlete_goals").insert(goal)
        MemoryCache.invalidate("trainer:athlete_goals:${goal.athleteId}")
        MemoryCache.invalidate("athlete:goals:${goal.athleteId}")
    }

    // ── TEAM STATISTICS ──

    suspend fun getTeamStatistics(teamId: String): List<UserSportStatsDto> =
        MemoryCache.cached("trainer:team_stats:$teamId", ttlMs = 300_000L) {
            val memberIds = client.from("team_members")
                .select(Columns.raw("user_id")) {
                    filter { eq("team_id", teamId) }
                }
                .decodeList<TeamMemberDto>()
                .mapNotNull { it.userId }

            if (memberIds.isEmpty()) emptyList()
            else client.from("v_user_sport_stats")
                .select {
                    filter { isIn("user_id", memberIds) }
                }
                .decodeList<UserSportStatsDto>()
        }

    suspend fun getTeamResultsDistribution(teamId: String): List<ResultDistribution> =
        MemoryCache.cached("trainer:team_distribution:$teamId", ttlMs = 300_000L) {
            getTeamResultsDistributionInternal(teamId)
        }

    private suspend fun getTeamResultsDistributionInternal(teamId: String): List<ResultDistribution> {
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

