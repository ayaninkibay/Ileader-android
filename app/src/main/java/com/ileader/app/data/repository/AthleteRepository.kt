package com.ileader.app.data.repository

import com.ileader.app.data.models.*
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.util.AppLogger
import com.ileader.app.data.util.MemoryCache
import com.ileader.app.data.util.safeApiCall
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AthleteRepository {
    private val client = SupabaseModule.client

    // ── LICENSE ──

    suspend fun getLicense(userId: String): License? {
        val cached = MemoryCache.cached("athlete:license:$userId", ttlMs = 1_800_000L) {
            client.from("licenses")
                .select(Columns.raw("id, user_id, number, category, class, federation, status, issue_date, expiry_date, medical_check_date, medical_check_expiry, created_at, updated_at")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<LicenseDto>()
        }
        return cached.firstOrNull()?.toDomain()
    }

    suspend fun upsertLicense(data: LicenseUpsertDto) = safeApiCall("AthleteRepo.upsertLicense") {
        if (data.id != null) {
            client.from("licenses").update(data) { filter { eq("id", data.id) } }
        } else {
            client.from("licenses").insert(data)
        }
        MemoryCache.invalidate("athlete:license:${data.userId}")
    }

    // ── RATING HISTORY ──

    suspend fun getRatingHistory(userId: String, sportId: String? = null): List<RatingHistoryEntry> =
        MemoryCache.cached("athlete:rating_history:$userId:${sportId ?: "all"}", ttlMs = 600_000L) {
            safeApiCall("AthleteRepo.getRatingHistory") {
                val rows = client.from("rating_history")
                    .select(Columns.raw("*, sports(id, name), tournaments(id, name)")) {
                        filter {
                            eq("user_id", userId)
                            if (sportId != null) eq("sport_id", sportId)
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<RatingHistoryDto>()
                rows.map { r ->
                    RatingHistoryEntry(
                        id = r.id ?: "",
                        rating = r.effectiveRating,
                        delta = r.effectiveDelta,
                        date = r.effectiveDate,
                        reason = r.reason ?: (if (r.tournamentId != null) "tournament_result" else ""),
                        sportName = r.sports?.name,
                        tournamentName = r.tournaments?.name
                    )
                }
            }
        }

    // ── ACHIEVEMENTS ──

    suspend fun getAchievements(userId: String): List<AchievementItem> =
        MemoryCache.cached("athlete:achievements:$userId", ttlMs = 1_800_000L) {
            safeApiCall("AthleteRepo.getAchievements") {
                val rows = try {
                    client.from("achievements")
                        .select {
                            filter { eq("athlete_id", userId) }
                            order("date", Order.DESCENDING)
                        }
                        .decodeList<AchievementDto>()
                } catch (e: Exception) {
                    AppLogger.w("AthleteRepo.getAchievements: ${e.message}", e)
                    emptyList()
                }
                rows.map {
                    AchievementItem(
                        id = it.id ?: "",
                        title = it.title ?: "",
                        description = it.description ?: "",
                        rarity = AchievementRarity.fromString(it.rarity),
                        date = (it.date ?: it.createdAt ?: "").take(10)
                    )
                }
            }
        }

    // ── LAP TIMES ──

    suspend fun getLapTimes(userId: String): List<LapTimeItem> =
        MemoryCache.cached("athlete:lap_times:$userId", ttlMs = 600_000L) {
            safeApiCall("AthleteRepo.getLapTimes") {
                val rows = try {
                    client.from("lap_times")
                        .select {
                            filter { eq("athlete_id", userId) }
                            order("time_seconds", Order.ASCENDING)
                        }
                        .decodeList<LapTimeDto>()
                } catch (e: Exception) {
                    AppLogger.w("AthleteRepo.getLapTimes: ${e.message}", e)
                    emptyList()
                }
                rows.map {
                    LapTimeItem(
                        id = it.id ?: "",
                        date = (it.date ?: it.createdAt ?: "").take(10),
                        timeSeconds = it.timeSeconds ?: 0.0,
                        lapNumber = it.lapNumber,
                        isBest = it.isBest ?: false,
                        conditions = it.conditions,
                        equipment = it.equipment
                    )
                }
            }
        }

    suspend fun addLapTime(data: LapTimeInsertDto) = safeApiCall("AthleteRepo.addLapTime") {
        client.from("lap_times").insert(data)
        MemoryCache.invalidate("athlete:lap_times:${data.athleteId}")
    }

    // ── PROFILE ──

    suspend fun getProfile(userId: String): User =
        MemoryCache.cached("athlete:profile_domain:$userId", ttlMs = 600_000L) {
            safeApiCall("AthleteRepo.getProfile") {
                val dto = client.from("profiles")
                    .select(Columns.raw("*, roles!primary_role_id(id, name)")) {
                        filter { eq("id", userId) }
                    }
                    .decodeSingle<ProfileDto>()
                dto.toDomain()
            }
        }

    suspend fun updateProfile(userId: String, data: ProfileUpdateDto) {
        client.from("profiles")
            .update(data) {
                filter { eq("id", userId) }
            }
        MemoryCache.invalidate("athlete:profile_domain:$userId")
        MemoryCache.invalidate("profile:$userId")
        MemoryCache.invalidate("public_profile:$userId")
    }

    suspend fun getSports(userId: String): List<Pair<String, String>> =
        MemoryCache.cached("athlete:user_sport_pairs:$userId", ttlMs = 600_000L) {
            val userSports = client.from("user_sports")
                .select(Columns.raw("*, sports(id, name)")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<UserSportDto>()
            userSports.mapNotNull { us ->
                us.sports?.let { it.name to it.id }
            }
        }

    suspend fun getAllSports(): List<Pair<String, String>> =
        MemoryCache.cached("athlete:all_sport_pairs", ttlMs = 1_800_000L) {
            val sports = client.from("sports")
                .select {
                    filter { eq("is_active", true) }
                }
                .decodeList<SportDto>()
            sports.map { it.name to it.id }
        }

    // ── STATISTICS ──

    suspend fun getStats(userId: String): AthleteStats =
        MemoryCache.cached("athlete:stats:$userId", ttlMs = 300_000L) {
            val stats = client.from("v_user_sport_stats")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<UserSportStatsDto>()

            AthleteStats(
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

    suspend fun getMyTournaments(userId: String): List<Tournament> =
        MemoryCache.cached("athlete:my_tournaments:$userId", ttlMs = 300_000L) {
            safeApiCall("AthleteRepo.getMyTournaments") {
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
        }

    suspend fun getAvailableTournaments(): List<Tournament> =
        MemoryCache.cached("athlete:available_tournaments", ttlMs = 300_000L) {
            val tournaments = client.from("v_tournament_with_counts")
                .select {
                    filter { eq("visibility", "public") }
                    order("start_date", Order.ASCENDING)
                }
                .decodeList<TournamentWithCountsDto>()

            tournaments.map { it.toDomain() }
        }

    suspend fun getTournamentDetail(tournamentId: String): Tournament =
        MemoryCache.cached("athlete:tournament:$tournamentId", ttlMs = 60_000L) {
            coroutineScope {
                // Two independent reads — fetch in parallel. Participant count uses
                // head+count so no rows are decoded.
                val dtoDef = async {
                    client.from("tournaments")
                        .select(Columns.raw("*, sports(id, name), locations(name, city), profiles!organizer_id(name)")) {
                            filter { eq("id", tournamentId) }
                        }
                        .decodeSingle<TournamentDto>()
                }
                val countDef = async {
                    (client.from("tournament_participants")
                        .select(Columns.raw("id")) {
                            count(Count.EXACT)
                            filter {
                                eq("tournament_id", tournamentId)
                                neq("status", "cancelled")
                            }
                        }
                        .countOrNull() ?: 0L).toInt()
                }
                dtoDef.await().toDomain().copy(currentParticipants = countDef.await())
            }
        }

    suspend fun getMyParticipation(tournamentId: String, userId: String): Boolean {
        // Boolean can't be cached via MemoryCache (T : Any). Wrap in single-element list.
        val cached = MemoryCache.cached("athlete:participation:$tournamentId:$userId", ttlMs = 300_000L) {
            val count = client.from("tournament_participants")
                .select(Columns.raw("athlete_id")) {
                    count(Count.EXACT)
                    filter {
                        eq("tournament_id", tournamentId)
                        eq("athlete_id", userId)
                        neq("status", "cancelled")
                    }
                }
                .countOrNull() ?: 0L
            listOf(count > 0L)
        }
        return cached.first()
    }

    suspend fun registerForTournament(tournamentId: String, userId: String) = safeApiCall("AthleteRepo.registerForTournament") {
        client.from("tournament_participants")
            .insert(ParticipantInsertDto(
                tournamentId = tournamentId,
                athleteId = userId,
                status = "pending"
            ))
        MemoryCache.invalidate("athlete:participation:$tournamentId:$userId")
        MemoryCache.invalidate("athlete:my_tournaments:$userId")
        MemoryCache.invalidate("participants:$tournamentId")
        MemoryCache.invalidate("athlete:tournament:$tournamentId")
        MemoryCache.invalidate("tournament:$tournamentId")
        MemoryCache.invalidateMatching("user:tournaments:$userId")
    }

    suspend fun cancelRegistration(tournamentId: String, userId: String) {
        client.from("tournament_participants")
            .delete {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("athlete_id", userId)
                }
            }
        MemoryCache.invalidate("athlete:participation:$tournamentId:$userId")
        MemoryCache.invalidate("athlete:my_tournaments:$userId")
        MemoryCache.invalidate("participants:$tournamentId")
        MemoryCache.invalidate("athlete:tournament:$tournamentId")
        MemoryCache.invalidate("tournament:$tournamentId")
        MemoryCache.invalidateMatching("user:tournaments:$userId")
    }

    suspend fun joinByInviteCode(code: String): String {
        val result = client.postgrest.rpc("use_invite_code", buildJsonObject {
            put("code", code)
        })
        // RPC may register the caller for any tournament — drop user's tournament-list caches.
        MemoryCache.invalidateMatching("athlete:my_tournaments:")
        MemoryCache.invalidateMatching("athlete:available_tournaments")
        return result.data
    }

    // ── RESULTS ──

    suspend fun getMyResults(userId: String): List<TournamentResult> =
        MemoryCache.cached("athlete:my_results:$userId", ttlMs = 300_000L) {
            safeApiCall("AthleteRepo.getMyResults") {
                val results = client.from("tournament_results")
                    .select(Columns.raw("*, tournaments(id, name, start_date, sport_id, sports(id, name))")) {
                        filter { eq("athlete_id", userId) }
                    }
                    .decodeList<ResultDto>()

                results.map { it.toDomain() }
            }
        }

    // ── GOALS ──

    suspend fun getGoals(userId: String): List<AthleteGoal> =
        MemoryCache.cached("athlete:goals:$userId", ttlMs = 600_000L) {
            val goals = client.from("athlete_goals")
                .select {
                    filter { eq("athlete_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<GoalDto>()

            goals.map { it.toDomain() }
        }

    suspend fun createGoal(goal: GoalInsertDto) {
        client.from("athlete_goals").insert(goal)
        MemoryCache.invalidate("athlete:goals:${goal.athleteId}")
    }

    suspend fun updateGoal(goalId: String, data: GoalUpdateDto) {
        client.from("athlete_goals")
            .update(data) {
                filter { eq("id", goalId) }
            }
        // Don't know athleteId here — drop all goal caches.
        MemoryCache.invalidateMatching("athlete:goals:")
    }

    suspend fun deleteGoal(goalId: String) {
        client.from("athlete_goals")
            .delete {
                filter { eq("id", goalId) }
            }
        MemoryCache.invalidateMatching("athlete:goals:")
    }

    // ── TEAM ──

    suspend fun getTeam(teamId: String): Team =
        MemoryCache.cached("athlete:team:$teamId", ttlMs = 300_000L) {
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

            Team(
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

    suspend fun getTournamentInvites(userId: String): List<TournamentInvite> =
        MemoryCache.cached("athlete:invites:$userId", ttlMs = 120_000L) {
            val invites = client.from("tournament_invites")
                .select(Columns.raw("*, tournaments(id, name)")) {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<TournamentInviteDto>()

            invites.map { inv ->
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

    suspend fun getTeamRequests(userId: String): List<TeamRequest> =
        MemoryCache.cached("athlete:team_requests:$userId", ttlMs = 120_000L) {
            val requests = client.from("team_requests")
                .select(Columns.raw("*, teams(id, name)")) {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<TeamRequestDto>()

            requests.map { req ->
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
        // Don't know which user this invite belongs to — drop all invite caches.
        MemoryCache.invalidateMatching("athlete:invites:")
    }

    suspend fun respondToTeamRequest(requestId: String, accept: Boolean) {
        client.from("team_requests")
            .update(mapOf("status" to if (accept) "accepted" else "declined")) {
                filter { eq("id", requestId) }
            }
        MemoryCache.invalidateMatching("athlete:team_requests:")
        MemoryCache.invalidateMatching("trainer:team_requests:")
        if (accept) {
            // Membership likely changed — drop community + team caches.
            MemoryCache.invalidateMatching("athlete:membership:")
            MemoryCache.invalidateMatching("athlete:team:")
            MemoryCache.invalidate("community:teams")
        }
    }

}
