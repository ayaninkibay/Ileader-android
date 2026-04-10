package com.ileader.app.data.repository

import com.ileader.app.data.models.*
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.util.AppLogger
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray

class RefereeRepository {
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

    // ── STATS ──

    suspend fun getStats(userId: String): RefereeStats {
        val assignments = client.from("tournament_referees")
            .select(Columns.raw("tournament_id, tournaments(id, name, status, start_date)")) {
                filter { eq("referee_id", userId) }
            }
            .decodeList<RefereeAssignmentDto>()

        val totalTournaments = assignments.size
        val thisMonth = assignments.count { a ->
            a.tournaments?.startDate?.startsWith(getCurrentYearMonth()) == true
        }
        val pendingResults = assignments.count { a ->
            a.tournaments?.status in listOf("in_progress", "registration_closed", "check_in")
        }

        val violations = client.from("violations")
            .select(Columns.raw("id")) {
                filter { eq("referee_id", userId) }
            }
            .decodeList<IdOnlyDto>()

        val participantCounts = assignments.mapNotNull { a ->
            a.tournaments?.id?.let { tid ->
                try {
                    val data = client.from("tournament_participants")
                        .select(Columns.raw("athlete_id")) {
                            filter {
                                eq("tournament_id", tid)
                                neq("status", "cancelled")
                            }
                        }
                        .data
                    Json.parseToJsonElement(data).jsonArray.size
                } catch (e: Exception) { AppLogger.w("RefereeRepo: ${e.message}"); 0 }
            }
        }

        return RefereeStats(
            totalTournaments = totalTournaments,
            thisMonth = thisMonth,
            pendingResults = pendingResults,
            totalParticipants = participantCounts.sum(),
            totalViolations = violations.size
        )
    }

    // ── TOURNAMENTS ──

    suspend fun getAssignedTournaments(userId: String): List<RefereeTournament> {
        val assignments = client.from("tournament_referees")
            .select(Columns.raw("*, tournaments(*, sports(id, name), locations(name, city), profiles!organizer_id(name))")) {
                filter { eq("referee_id", userId) }
            }
            .decodeList<RefereeAssignmentDto>()

        return assignments.mapNotNull { it.toRefereeTournament() }
    }

    suspend fun getTournamentHistory(userId: String): List<RefereeTournament> {
        val assignments = client.from("tournament_referees")
            .select(Columns.raw("*, tournaments(*, sports(id, name), locations(name, city), profiles!organizer_id(name))")) {
                filter { eq("referee_id", userId) }
            }
            .decodeList<RefereeAssignmentDto>()

        return assignments
            .filter { it.tournaments?.status == "completed" }
            .mapNotNull { it.toRefereeTournament() }
            .sortedByDescending { it.date }
    }

    suspend fun getTournamentDetail(tournamentId: String): RefereeTournament {
        val dto = client.from("tournaments")
            .select(Columns.raw("*, sports(id, name), locations(name, city), profiles!organizer_id(name)")) {
                filter { eq("id", tournamentId) }
            }
            .decodeSingle<TournamentDto>()

        val participantCount = try {
            val data = client.from("tournament_participants")
                .select(Columns.raw("athlete_id")) {
                    filter {
                        eq("tournament_id", tournamentId)
                        neq("status", "cancelled")
                    }
                }
                .data
            Json.parseToJsonElement(data).jsonArray.size
        } catch (e: Exception) { AppLogger.w("RefereeRepo: ${e.message}"); 0 }

        val matchesAll = client.from("bracket_matches")
            .select {
                filter { eq("tournament_id", tournamentId) }
            }
            .decodeList<BracketMatchDto>()

        val matchesCompleted = matchesAll.count { it.status == "completed" }

        val refereeAssignment = try {
            client.from("tournament_referees")
                .select(Columns.raw("tournament_id, role")) {
                    filter {
                        eq("tournament_id", tournamentId)
                    }
                }
                .decodeList<RefereeAssignmentDto>()
                .firstOrNull()
        } catch (e: Exception) { AppLogger.w("RefereeRepo: ${e.message}"); null }

        val locationStr = dto.locations?.let {
            buildString {
                append(it.name)
                if (!it.city.isNullOrEmpty()) append(", ${it.city}")
            }
        } ?: ""

        return RefereeTournament(
            id = dto.id,
            name = dto.name,
            sport = dto.sports?.name ?: "",
            sportId = dto.sportId ?: "",
            location = locationStr,
            date = dto.startDate ?: "",
            status = mapTournamentStatus(dto.status),
            participants = participantCount,
            matchesTotal = matchesAll.size,
            matchesCompleted = matchesCompleted,
            description = dto.description ?: "",
            organizer = dto.profiles?.name ?: "",
            prizeFund = dto.prize ?: "",
            categories = dto.categories ?: emptyList(),
            refereeRole = mapRefereeRole(refereeAssignment?.role)
        )
    }

    // ── MATCHES ──

    suspend fun getMatches(tournamentId: String): List<RefereeMatch> {
        val matches = client.from("bracket_matches")
            .select {
                filter { eq("tournament_id", tournamentId) }
                order("round", Order.ASCENDING)
                order("match_number", Order.ASCENDING)
            }
            .decodeList<BracketMatchDto>()

        return matches.map { it.toRefereeMatch() }
    }

    suspend fun updateMatchResult(matchId: String, data: MatchResultUpdateDto) {
        client.from("bracket_matches")
            .update(data) {
                filter { eq("id", matchId) }
            }
    }

    suspend fun updateMatchSlot(matchId: String, data: BracketSlotUpdateDto) {
        client.from("bracket_matches")
            .update({
                data.participant1Id?.let { set("participant1_id", it) }
                data.participant2Id?.let { set("participant2_id", it) }
            }) {
                filter { eq("id", matchId) }
            }
    }

    /**
     * Get all matches across every tournament the referee is assigned to.
     * Enriched with tournament metadata and participant names for the "My matches" screen.
     */
    suspend fun getMyMatches(userId: String): List<RefereeMyMatch> {
        // Step 1: find all tournaments where user is referee
        val assignments = client.from("tournament_referees")
            .select(Columns.raw("tournament_id, tournaments(id, name, status, sports(name))")) {
                filter { eq("referee_id", userId) }
            }
            .decodeList<RefereeAssignmentDto>()

        val tournamentInfoMap = assignments.mapNotNull { a ->
            a.tournaments?.let { t ->
                t.id to RefereeMyMatchTournamentInfo(
                    id = t.id,
                    name = t.name,
                    status = t.status ?: "",
                    sportName = t.sports?.name ?: ""
                )
            }
        }.toMap()

        if (tournamentInfoMap.isEmpty()) return emptyList()

        val tournamentIds = tournamentInfoMap.keys.toList()

        // Step 2: fetch all matches in those tournaments
        val matches = client.from("bracket_matches")
            .select {
                filter { isIn("tournament_id", tournamentIds) }
                order("tournament_id", Order.ASCENDING)
                order("round", Order.ASCENDING)
                order("match_number", Order.ASCENDING)
            }
            .decodeList<BracketMatchDto>()

        if (matches.isEmpty()) return emptyList()

        // Step 3: fetch participant names for all involved tournaments
        val participants = client.from("tournament_participants")
            .select(Columns.raw("tournament_id, athlete_id, seed, profiles(id, name)")) {
                filter { isIn("tournament_id", tournamentIds) }
            }
            .decodeList<ParticipantDto>()

        val nameById = participants.associate {
            it.athleteId to (it.profiles?.name ?: "")
        }

        // Step 4: map to domain
        return matches.mapNotNull { m ->
            val tInfo = tournamentInfoMap[m.tournamentId] ?: return@mapNotNull null
            RefereeMyMatch(
                matchId = m.id,
                tournamentId = m.tournamentId,
                tournamentName = tInfo.name,
                tournamentStatus = tInfo.status,
                sportName = tInfo.sportName,
                round = m.round,
                matchNumber = m.matchNumber,
                bracketType = m.bracketType ?: "upper",
                participant1Id = m.participant1Id,
                participant2Id = m.participant2Id,
                participant1Name = m.participant1Id?.let { pid ->
                    if (pid.startsWith("tbd-")) null else nameById[pid]
                },
                participant2Name = m.participant2Id?.let { pid ->
                    if (pid.startsWith("tbd-")) null else nameById[pid]
                },
                participant1Score = m.participant1Score,
                participant2Score = m.participant2Score,
                games = m.games?.map { g ->
                    MatchGame(g.gameNumber, g.participant1Score, g.participant2Score, g.winnerId, g.status)
                } ?: emptyList(),
                winnerId = m.winnerId,
                status = m.status,
                groupId = m.groupId,
                isBye = m.isBye ?: false
            )
        }.filter {
            // Hide byes and unfilled TBD matches — referee can't do anything with them
            !it.isBye && it.participant1Id != null && it.participant2Id != null &&
            !(it.participant1Id.startsWith("tbd-") || it.participant2Id.startsWith("tbd-"))
        }
    }

    suspend fun startMatch(matchId: String) {
        client.from("bracket_matches")
            .update(mapOf("status" to "in_progress")) {
                filter { eq("id", matchId) }
            }
    }

    // ── PARTICIPANTS ──

    suspend fun getParticipants(tournamentId: String): List<RefereeParticipant> {
        val participants = client.from("tournament_participants")
            .select(Columns.raw("*, profiles(id, name), teams(id, name)")) {
                filter {
                    eq("tournament_id", tournamentId)
                    neq("status", "cancelled")
                }
            }
            .decodeList<ParticipantDto>()

        return participants.map { p ->
            RefereeParticipant(
                id = p.athleteId,
                name = p.profiles?.name ?: "",
                number = (p.number ?: p.seed ?: 0).toString().padStart(2, '0'),
                team = p.teams?.name ?: "",
                category = p.groupId ?: ""
            )
        }
    }

    // ── RESULTS ──

    suspend fun getTournamentResults(tournamentId: String): List<ResultDto> {
        return client.from("tournament_results")
            .select(Columns.raw("*, profiles!athlete_id(id, name)")) {
                filter { eq("tournament_id", tournamentId) }
                order("position", Order.ASCENDING)
            }
            .decodeList<ResultDto>()
    }

    suspend fun saveResults(tournamentId: String, results: List<ResultInsertDto>) {
        if (results.isNotEmpty()) {
            client.from("tournament_results").upsert(results)
        }
    }

    // ── VIOLATIONS ──

    suspend fun getViolations(refereeId: String): List<RefereeViolation> {
        val violations = client.from("violations")
            .select(Columns.raw("*, profiles!athlete_id(id, name), tournaments(id, name, sport_id, sports(id, name))")) {
                filter { eq("referee_id", refereeId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<ViolationDto>()

        return violations.map { it.toRefereeViolation() }
    }

    suspend fun getViolationsByTournament(tournamentId: String, refereeId: String): List<RefereeViolation> {
        val violations = client.from("violations")
            .select(Columns.raw("*, profiles!athlete_id(id, name), tournaments(id, name, sport_id, sports(id, name))")) {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("referee_id", refereeId)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<ViolationDto>()

        return violations.map { it.toRefereeViolation() }
    }

    suspend fun createViolation(data: ViolationInsertDto) {
        client.from("violations").insert(data)
    }

    // ── INVITES ──

    suspend fun getIncomingInvites(userId: String): List<RefereeInvite> {
        val invites = client.from("tournament_invites")
            .select(Columns.raw("*, tournaments(id, name, start_date, sport_id, sports(id, name), locations(name, city))")) {
                filter {
                    eq("user_id", userId)
                    eq("role", "referee")
                    eq("direction", "outgoing") // outgoing from organizer = incoming for referee
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<TournamentInviteDto>()

        return invites.map { it.toRefereeInvite() }
    }

    suspend fun getOutgoingApplications(userId: String): List<RefereeInvite> {
        val invites = client.from("tournament_invites")
            .select(Columns.raw("*, tournaments(id, name, start_date, sport_id, sports(id, name), locations(name, city))")) {
                filter {
                    eq("user_id", userId)
                    eq("role", "referee")
                    eq("direction", "incoming") // incoming from referee = outgoing application
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<TournamentInviteDto>()

        return invites.map { it.toRefereeInvite() }
    }

    suspend fun respondToInvite(inviteId: String, accept: Boolean) {
        client.from("tournament_invites")
            .update(mapOf("status" to if (accept) "accepted" else "declined")) {
                filter { eq("id", inviteId) }
            }
    }

    suspend fun createApplication(tournamentId: String, userId: String, refereeRole: String, message: String? = null) {
        client.from("tournament_invites")
            .insert(TournamentInviteInsertDto(
                tournamentId = tournamentId,
                userId = userId,
                role = "referee",
                direction = "incoming",
                refereeRole = refereeRole,
                message = message
            ))
    }

    suspend fun joinByInviteCode(code: String, userId: String): String {
        return client.postgrest.rpc("use_invite_code", buildJsonObject { put("code", code) })
            .data
    }

    // ── NOTIFICATIONS ──

    suspend fun getNotifications(userId: String): List<NotificationDto> {
        return client.from("notifications")
            .select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
                limit(50)
            }
            .decodeList<NotificationDto>()
    }

    suspend fun getUnreadNotificationCount(userId: String): Int {
        return client.from("notifications")
            .select(Columns.raw("id")) {
                filter {
                    eq("user_id", userId)
                    eq("read", false)
                }
            }
            .decodeList<IdOnlyDto>()
            .size
    }

    suspend fun markNotificationAsRead(notificationId: String) {
        client.from("notifications")
            .update(mapOf("read" to true)) {
                filter { eq("id", notificationId) }
            }
    }

    suspend fun markAllNotificationsAsRead(userId: String) {
        client.from("notifications")
            .update(mapOf("read" to true)) {
                filter {
                    eq("user_id", userId)
                    eq("read", false)
                }
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

        val membersList = membersDto.map { m ->
            TeamMember(
                id = m.profiles?.id ?: "",
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

}

// ── HELPERS ──

private fun getCurrentYearMonth(): String {
    val now = java.time.LocalDate.now()
    return "${now.year}-${now.monthValue.toString().padStart(2, '0')}"
}

private fun mapTournamentStatus(status: String?): TournamentStatus {
    return when (status) {
        "draft" -> TournamentStatus.DRAFT
        "registration_open" -> TournamentStatus.REGISTRATION_OPEN
        "registration_closed" -> TournamentStatus.REGISTRATION_CLOSED
        "check_in" -> TournamentStatus.CHECK_IN
        "in_progress" -> TournamentStatus.IN_PROGRESS
        "completed" -> TournamentStatus.COMPLETED
        "cancelled" -> TournamentStatus.CANCELLED
        else -> TournamentStatus.DRAFT
    }
}

private fun mapRefereeRole(role: String?): RefereeRole {
    return when (role) {
        "head_referee" -> RefereeRole.HEAD_REFEREE
        "assistant" -> RefereeRole.ASSISTANT
        else -> RefereeRole.REFEREE
    }
}

private fun mapViolationSeverity(severity: String): ViolationSeverity {
    return when (severity) {
        "warning" -> ViolationSeverity.WARNING
        "penalty" -> ViolationSeverity.PENALTY
        "disqualification" -> ViolationSeverity.DISQUALIFICATION
        else -> ViolationSeverity.WARNING
    }
}

private fun mapViolationCategory(category: String): ViolationCategory {
    return when (category) {
        "false_start" -> ViolationCategory.FALSE_START
        "dangerous_driving" -> ViolationCategory.DANGEROUS_DRIVING
        "track_limits" -> ViolationCategory.TRACK_LIMITS
        "unsportsmanlike" -> ViolationCategory.UNSPORTSMANLIKE
        "equipment" -> ViolationCategory.EQUIPMENT
        "safety" -> ViolationCategory.SAFETY
        "rules" -> ViolationCategory.RULES
        "contact" -> ViolationCategory.CONTACT
        "delay" -> ViolationCategory.DELAY
        else -> ViolationCategory.OTHER
    }
}

private fun mapInviteStatus(status: String?): InviteStatus {
    return when (status) {
        "accepted" -> InviteStatus.ACCEPTED
        "declined" -> InviteStatus.DECLINED
        else -> InviteStatus.PENDING
    }
}

private fun mapMatchStatus(status: String): RefereeMatchStatus {
    return when (status) {
        "completed" -> RefereeMatchStatus.COMPLETED
        "in_progress" -> RefereeMatchStatus.IN_PROGRESS
        else -> RefereeMatchStatus.SCHEDULED
    }
}

// ── DTO → Domain Mappers ──

private fun RefereeAssignmentDto.toRefereeTournament(): RefereeTournament? {
    val t = tournaments ?: return null
    val locationStr = t.locations?.let {
        buildString {
            append(it.name)
            if (!it.city.isNullOrEmpty()) append(", ${it.city}")
        }
    } ?: ""

    return RefereeTournament(
        id = t.id,
        name = t.name,
        sport = t.sports?.name ?: "",
        sportId = t.sportId ?: "",
        location = locationStr,
        date = t.startDate ?: "",
        status = mapTournamentStatus(t.status),
        participants = 0,
        description = t.description ?: "",
        organizer = t.profiles?.name ?: "",
        prizeFund = t.prize ?: "",
        categories = t.categories ?: emptyList(),
        refereeRole = mapRefereeRole(role)
    )
}

private fun ViolationDto.toRefereeViolation(): RefereeViolation {
    return RefereeViolation(
        id = id ?: "",
        participantId = athleteId,
        participantName = profiles?.name ?: "",
        tournamentId = tournamentId,
        tournamentName = tournaments?.name ?: "",
        sport = tournaments?.sports?.name ?: "",
        date = createdAt?.take(10) ?: "",
        severity = mapViolationSeverity(severity),
        category = mapViolationCategory(category),
        description = description ?: "",
        matchNumber = matchNumber,
        time = time,
        penaltyApplied = penaltyApplied
    )
}

private fun TournamentInviteDto.toRefereeInvite(): RefereeInvite {
    return RefereeInvite(
        id = id,
        tournamentId = tournamentId,
        tournamentName = tournaments?.name ?: "",
        sportName = tournaments?.sports?.name ?: "",
        role = mapRefereeRole(refereeRole),
        status = mapInviteStatus(status),
        locationCity = tournaments?.locations?.city ?: "",
        startDate = tournaments?.startDate ?: "",
        createdAt = createdAt ?: "",
        responseMessage = responseMessage
    )
}

private fun BracketMatchDto.toRefereeMatch(): RefereeMatch {
    val participantCount = listOfNotNull(participant1Id, participant2Id).size
    return RefereeMatch(
        id = id,
        number = matchNumber,
        time = scheduledAt?.let {
            try { it.substring(11, 16) } catch (_: Exception) { "" }
        } ?: "",
        category = bracketType ?: "",
        status = mapMatchStatus(status),
        participants = participantCount
    )
}
