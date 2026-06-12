package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.local.AppDatabase
import com.ileader.app.data.local.toCached
import com.ileader.app.data.local.toDto
import com.ileader.app.data.util.AppLogger
import com.ileader.app.data.util.MemoryCache
import com.ileader.app.data.util.safeApiCall
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ViewerRepository {
    private val client = SupabaseModule.client
    private val db: AppDatabase? get() = dbInstance

    companion object {
        private val roleIdCache = mutableMapOf<String, String>()
        private val roleIdMutex = Mutex()

        // Reused across getLegalPages() calls — was being recreated on every read.
        private val legalPagesJson = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

        @Volatile
        var dbInstance: AppDatabase? = null

        fun init(db: AppDatabase) {
            dbInstance = db
        }
    }

    private suspend fun getRoleId(roleName: String): String {
        return roleIdMutex.withLock {
            roleIdCache.getOrPut(roleName) {
                client.from("roles")
                    .select { filter { eq("name", roleName) } }
                    .decodeSingle<RoleDto>()
                    .id
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // HOME
    // ══════════════════════════════════════════════════════════

    suspend fun getPlatformStats(): Triple<Int, Int, Int> =
        MemoryCache.cached("platform_stats", ttlMs = 900_000L) {
            safeApiCall("ViewerRepo.getPlatformStats") {
                // Count-only HEAD-запросы вместо декода всех строк.
                // Иначе на 10к профилях прилетает 10к строк только чтобы получить число.
                val usersCount = client.from("profiles")
                    .select(Columns.raw("id")) {
                        filter { eq("status", "active") }
                        count(Count.EXACT)
                        limit(0)
                    }
                    .countOrNull()?.toInt() ?: 0

                val tournamentsCount = client.from("tournaments")
                    .select(Columns.raw("id")) {
                        count(Count.EXACT)
                        limit(0)
                    }
                    .countOrNull()?.toInt() ?: 0

                val sportsCount = client.from("sports")
                    .select(Columns.raw("id")) {
                        filter { eq("is_active", true) }
                        count(Count.EXACT)
                        limit(0)
                    }
                    .countOrNull()?.toInt() ?: 0

                Triple(usersCount, tournamentsCount, sportsCount)
            }
        }

    suspend fun getSports(): List<SportDto> {
        // L1 memory (10 min) → L2 Supabase + Room write-through → L3 Room (offline fallback)
        return MemoryCache.cached("sports", ttlMs = 600_000L) {
            try {
                val result = client.from("sports")
                    .select { filter { eq("is_active", true) } }
                    .decodeList<SportDto>()
                db?.sportDao()?.let { dao ->
                    dao.deleteAll()
                    dao.insertAll(result.map { it.toCached() })
                }
                result
            } catch (e: Exception) {
                AppLogger.w("ViewerRepo.getSports: ${e.message}")
                db?.sportDao()?.getAll()?.map { it.toDto() } ?: emptyList()
            }
        }
    }

    suspend fun getUpcomingTournaments(limit: Int = 10): List<TournamentWithCountsDto> =
        MemoryCache.cached("tournaments:upcoming:$limit", ttlMs = 60_000L) {
            safeApiCall("ViewerRepo.getUpcomingTournaments") {
                client.from("v_tournament_with_counts")
                    .select {
                        filter {
                            eq("visibility", "public")
                            or {
                                eq("status", "registration_open")
                                eq("status", "in_progress")
                            }
                        }
                        order("start_date", Order.ASCENDING)
                        limit(limit.toLong())
                    }
                    .decodeList<TournamentWithCountsDto>()
            }
        }

    suspend fun getTournamentsByIds(ids: List<String>): List<TournamentWithCountsDto> {
        if (ids.isEmpty()) return emptyList()
        return safeApiCall("ViewerRepo.getTournamentsByIds") {
            client.from("v_tournament_with_counts")
                .select {
                    filter { isIn("id", ids) }
                    order("start_date", Order.DESCENDING)
                }
                .decodeList<TournamentWithCountsDto>()
        }
    }

    // ══════════════════════════════════════════════════════════
    // TOURNAMENTS
    // ══════════════════════════════════════════════════════════

    suspend fun getPublicTournaments(): List<TournamentWithCountsDto> =
        MemoryCache.cached("tournaments:public", ttlMs = 120_000L) {
            try {
                val result = client.from("v_tournament_with_counts")
                    .select {
                        filter { eq("visibility", "public") }
                        order("start_date", Order.DESCENDING)
                        limit(100)
                    }
                    .decodeList<TournamentWithCountsDto>()
                // Cache for offline
                db?.tournamentDao()?.let { dao ->
                    dao.deleteAll()
                    dao.insertAll(result.map { it.toCached() })
                }
                result
            } catch (e: Exception) {
                AppLogger.w("ViewerRepo.getPublicTournaments: ${e.message}")
                db?.tournamentDao()?.getAll()?.map { it.toDto() } ?: throw e
            }
        }

    suspend fun getTournamentDetail(tournamentId: String): TournamentDto =
        MemoryCache.cached("tournament:$tournamentId", ttlMs = 30_000L) {
            safeApiCall("ViewerRepo.getTournamentDetail") {
                client.from("tournaments")
                    .select(Columns.raw("*, sports(id, name, slug), locations(*), profiles!organizer_id(name)"))
                    { filter { eq("id", tournamentId) } }
                    .decodeSingle<TournamentDto>()
            }
        }

    suspend fun getTournamentParticipants(tournamentId: String): List<ParticipantDto> =
        MemoryCache.cached("participants:$tournamentId", ttlMs = 30_000L) {
            client.from("tournament_participants")
                .select(Columns.raw("*, profiles(name, avatar_url, city)"))
                {
                    filter {
                        eq("tournament_id", tournamentId)
                        eq("status", "confirmed")
                    }
                    order("seed", Order.ASCENDING)
                    limit(2000)
                }
                .decodeList<ParticipantDto>()
        }

    suspend fun getTournamentResults(tournamentId: String): List<ResultDto> =
        MemoryCache.cached("results:$tournamentId", ttlMs = 60_000L) {
            client.from("tournament_results")
                .select(Columns.raw("*, profiles!athlete_id(name, avatar_url, city)"))
                {
                    filter { eq("tournament_id", tournamentId) }
                    order("position", Order.ASCENDING)
                }
                .decodeList<ResultDto>()
        }

    suspend fun getTournamentBracket(tournamentId: String): List<BracketMatchDto> =
        // Short TTL because live scoring changes bracket frequently.
        // Invalidation on match save below keeps readers from seeing stale scores too long.
        MemoryCache.cached("bracket:$tournamentId", ttlMs = 15_000L) {
            client.from("bracket_matches")
                .select {
                    filter { eq("tournament_id", tournamentId) }
                    order("round", Order.ASCENDING)
                    order("match_number", Order.ASCENDING)
                    limit(2000)
                }
                .decodeList<BracketMatchDto>()
        }

    suspend fun getTournamentGroups(tournamentId: String): List<TournamentGroupDto> =
        MemoryCache.cached("groups:$tournamentId", ttlMs = 30_000L) {
            client.from("tournament_groups")
                .select { filter { eq("tournament_id", tournamentId) } }
                .decodeList<TournamentGroupDto>()
        }

    suspend fun getTournamentReferees(tournamentId: String): List<RefereeAssignmentDto> =
        MemoryCache.cached("referees:$tournamentId", ttlMs = 600_000L) {
            client.from("tournament_referees")
                .select(Columns.raw("tournament_id, referee_id, role, assigned_at, profiles!referee_id(id, name, avatar_url, city)"))
                { filter { eq("tournament_id", tournamentId) } }
                .decodeList<RefereeAssignmentDto>()
        }

    suspend fun getTournamentSponsors(tournamentId: String): List<TournamentSponsorshipDto> =
        MemoryCache.cached("sponsors:$tournamentId", ttlMs = 600_000L) {
            client.from("sponsorships")
                .select(Columns.raw("sponsor_id, tournament_id, tier, amount, profiles!sponsor_id(id, name, avatar_url)"))
                { filter { eq("tournament_id", tournamentId) } }
                .decodeList<TournamentSponsorshipDto>()
        }

    suspend fun getTournamentArticles(tournamentId: String, limit: Int = 10): List<ArticleDto> =
        MemoryCache.cached("articles:tournament:$tournamentId:$limit", ttlMs = 600_000L) {
            client.from("articles")
                .select(Columns.raw("id, title, excerpt, cover_image_url, category, views, published_at, created_at"))
                {
                    filter { eq("tournament_id", tournamentId); eq("status", "published") }
                    order("published_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<ArticleDto>()
        }

    // ══════════════════════════════════════════════════════════
    // SPECTATORS
    // ══════════════════════════════════════════════════════════

    suspend fun getMySpectatorRegistration(tournamentId: String, userId: String): SpectatorDto? {
        return client.from("tournament_spectators")
            .select(Columns.raw("id, tournament_id, user_id, ticket_type, payment_status, check_in_status, created_at"))
            {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<SpectatorDto>()
    }

    suspend fun registerAsSpectator(tournamentId: String, userId: String) {
        client.from("tournament_spectators")
            .insert(buildMap {
                put("tournament_id", tournamentId)
                put("user_id", userId)
                put("ticket_type", "free")
                put("payment_status", "free")
                put("check_in_status", "pending")
            })
        // New ticket appeared — drop tickets cache for this user.
        MemoryCache.invalidate("tickets:$userId")
    }

    suspend fun getMySpectatorRegistrations(userId: String): List<SpectatorDto> {
        return client.from("tournament_spectators")
            .select(Columns.raw("id, tournament_id, user_id, ticket_type, payment_status, check_in_status, created_at"))
            { filter { eq("user_id", userId) } }
            .decodeList<SpectatorDto>()
    }

    // ══════════════════════════════════════════════════════════
    // NEWS (articles)
    // ══════════════════════════════════════════════════════════

    suspend fun getPublishedArticles(limit: Int = 50): List<ArticleDto> =
        MemoryCache.cached("articles:published:$limit", ttlMs = 300_000L) {
            client.from("articles")
                .select(Columns.raw("id, title, excerpt, cover_image_url, category, tags, views, published_at, created_at, profiles!author_id(id, name, avatar_url), sports(id, name)")) {
                    filter { eq("status", "published") }
                    order("published_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<ArticleDto>()
        }

    suspend fun getRecentArticles(limit: Int = 5): List<ArticleDto> =
        MemoryCache.cached("articles:recent:$limit", ttlMs = 120_000L) {
            client.from("articles")
                .select(Columns.raw("id, title, excerpt, cover_image_url, category, views, published_at, profiles!author_id(id, name)")) {
                    filter { eq("status", "published") }
                    order("published_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<ArticleDto>()
        }

    suspend fun getArticleDetail(articleId: String): ArticleDto =
        MemoryCache.cached("article:$articleId", ttlMs = 900_000L) {
            client.from("articles")
                .select(Columns.raw("*, profiles!author_id(id, name, avatar_url), sports(id, name)"))
                { filter { eq("id", articleId) } }
                .decodeSingle<ArticleDto>()
        }

    // ══════════════════════════════════════════════════════════
    // COMMUNITY
    // ══════════════════════════════════════════════════════════

    suspend fun getAthletes(): List<CommunityProfileDto> =
        MemoryCache.cached("community:athletes", ttlMs = 180_000L) {
            val roleId = getRoleId("athlete")
            client.from("profiles")
                .select(Columns.raw("id, name, avatar_url, city, bio, athlete_subtype, user_sports(rating, is_primary, sports(id, name))"))
                {
                    filter {
                        eq("primary_role_id", roleId)
                        eq("status", "active")
                    }
                    limit(50)
                }
                .decodeList<CommunityProfileDto>()
        }

    suspend fun getTrainers(): List<CommunityProfileDto> =
        MemoryCache.cached("community:trainers", ttlMs = 180_000L) {
            val roleId = getRoleId("trainer")
            client.from("profiles")
                .select(Columns.raw("id, name, avatar_url, city, bio, user_sports(rating, sports(id, name))"))
                {
                    filter {
                        eq("primary_role_id", roleId)
                        eq("status", "active")
                    }
                    limit(50)
                }
                .decodeList<CommunityProfileDto>()
        }

    suspend fun getReferees(): List<CommunityProfileDto> =
        MemoryCache.cached("community:referees", ttlMs = 180_000L) {
            val roleId = getRoleId("referee")
            client.from("profiles")
                .select(Columns.raw("id, name, avatar_url, city, bio, user_sports(rating, sports(id, name))"))
                {
                    filter {
                        eq("primary_role_id", roleId)
                        eq("status", "active")
                    }
                    limit(50)
                }
                .decodeList<CommunityProfileDto>()
        }

    suspend fun getTeams(): List<TeamWithStatsDto> =
        MemoryCache.cached("community:teams", ttlMs = 180_000L) {
            client.from("teams")
                .select(Columns.raw("*, sports(id, name), profiles!owner_id(name), team_members(count)"))
                { filter { eq("is_active", true) } }
                .decodeList<TeamWithStatsDto>()
        }

    // ══════════════════════════════════════════════════════════
    // SPORT IMAGES
    // ══════════════════════════════════════════════════════════

    suspend fun getSportImageUrls(sportSlug: String): List<String> =
        MemoryCache.cached("sport_images:$sportSlug", ttlMs = 1_800_000L) {
            try {
                val bucket = client.storage.from("sport-images")
                val files = bucket.list(sportSlug)
                files.filter { f ->
                    f.name.endsWith(".jpeg") || f.name.endsWith(".jpg") || f.name.endsWith(".png") || f.name.endsWith(".webp")
                }.map { f -> bucket.publicUrl("$sportSlug/${f.name}") }
            } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); emptyList() }
        }

    // ══════════════════════════════════════════════════════════
    // SPORT DETAIL
    // ══════════════════════════════════════════════════════════

    suspend fun getTournamentsBySport(sportId: String, limit: Int = 5): List<TournamentWithCountsDto> =
        MemoryCache.cached("tournaments:sport:$sportId:$limit", ttlMs = 120_000L) {
            safeApiCall("ViewerRepo.getTournamentsBySport") {
                client.from("v_tournament_with_counts")
                    .select {
                        filter { eq("visibility", "public"); eq("sport_id", sportId) }
                        order("start_date", Order.DESCENDING)
                        limit(limit.toLong())
                    }
                    .decodeList<TournamentWithCountsDto>()
            }
        }

    suspend fun getProfilesBySportAndRole(sportId: String, roleName: String, limit: Int = 5): List<CommunityProfileDto> =
        MemoryCache.cached("profiles:sport_role:$sportId:$roleName:$limit", ttlMs = 600_000L) {
            val roleId = getRoleId(roleName)
            client.from("profiles")
                .select(Columns.raw("id, name, avatar_url, city, bio, athlete_subtype, user_sports!inner(rating, is_primary, sports(id, name))"))
                {
                    filter { eq("primary_role_id", roleId); eq("status", "active"); eq("user_sports.sport_id", sportId) }
                    limit(limit.toLong())
                }
                .decodeList<CommunityProfileDto>()
        }

    suspend fun getTeamsBySport(sportId: String, limit: Int = 5): List<TeamWithStatsDto> =
        MemoryCache.cached("teams:sport:$sportId:$limit", ttlMs = 600_000L) {
            client.from("teams")
                .select(Columns.raw("*, sports(id, name), profiles!owner_id(name), team_members(count)"))
                { filter { eq("is_active", true); eq("sport_id", sportId) }; limit(limit.toLong()) }
                .decodeList<TeamWithStatsDto>()
        }

    suspend fun getArticlesBySport(sportId: String, limit: Int = 5): List<ArticleDto> =
        MemoryCache.cached("articles:sport:$sportId:$limit", ttlMs = 600_000L) {
            client.from("articles")
                .select(Columns.raw("id, title, excerpt, cover_image_url, category, views, published_at, created_at, profiles!author_id(id, name), sports(id, name)"))
                { filter { eq("status", "published"); eq("sport_id", sportId) }; order("published_at", Order.DESCENDING); limit(limit.toLong()) }
                .decodeList<ArticleDto>()
        }

    // ══════════════════════════════════════════════════════════
    // REFEREE
    // ══════════════════════════════════════════════════════════

    suspend fun getRefereeAssignments(userId: String): List<RefereeAssignmentDto> =
        MemoryCache.cached("referee:assignments:$userId", ttlMs = 120_000L) {
            try {
                client.from("tournament_referees")
                    .select(Columns.raw("tournament_id, referee_id, role, tournaments(id, name, status, start_date, sports(id, name))"))
                    { filter { eq("referee_id", userId) } }
                    .decodeList<RefereeAssignmentDto>()
            } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); emptyList() }
        }

    suspend fun getRefereeAssignmentsFull(userId: String): List<RefereeAssignmentDto> =
        MemoryCache.cached("referee:assignments_full:$userId", ttlMs = 300_000L) {
            try {
                client.from("tournament_referees")
                    .select(Columns.raw("tournament_id, referee_id, role, assigned_at, tournaments(id, name, status, start_date, end_date, image_url, sport_id, sports(id, name), locations(id, name, city))"))
                    { filter { eq("referee_id", userId) } }
                    .decodeList<RefereeAssignmentDto>()
            } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); emptyList() }
        }

    suspend fun getUserLicense(userId: String): LicenseDto? {
        val cached = MemoryCache.cached("user:license:$userId", ttlMs = 1_800_000L) {
            try {
                client.from("licenses")
                    .select(Columns.raw("id, user_id, number, category, class, federation, status, issue_date, expiry_date"))
                    { filter { eq("user_id", userId) } }
                    .decodeList<LicenseDto>()
            } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); emptyList() }
        }
        return cached.firstOrNull()
    }

    suspend fun getUserGoals(userId: String): List<GoalDto> =
        MemoryCache.cached("user:goals:$userId", ttlMs = 600_000L) {
            try {
                client.from("athlete_goals")
                    .select {
                        filter { eq("athlete_id", userId) }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<GoalDto>()
            } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); emptyList() }
        }

    // ══════════════════════════════════════════════════════════
    // PUBLIC PROFILES
    // ══════════════════════════════════════════════════════════

    suspend fun getPublicProfile(userId: String): ProfileDto =
        MemoryCache.cached("public_profile:$userId", ttlMs = 600_000L) {
            client.from("profiles")
                .select(columns = Columns.raw("*, roles(*)")) { filter { eq("id", userId) } }
                .decodeSingle<ProfileDto>()
        }

    suspend fun getUserSports(userId: String): List<UserSportDto> =
        MemoryCache.cached("user:sports:$userId", ttlMs = 180_000L) {
            client.from("user_sports")
                .select(Columns.raw("*, sports(id, name, slug)"))
                { filter { eq("user_id", userId) } }
                .decodeList<UserSportDto>()
        }

    suspend fun getUserSportStats(userId: String): List<UserSportStatsDto> =
        MemoryCache.cached("user:sport_stats:$userId", ttlMs = 120_000L) {
            client.from("v_user_sport_stats")
                .select { filter { eq("user_id", userId) } }
                .decodeList<UserSportStatsDto>()
        }

    suspend fun getAthleteResults(athleteId: String, limit: Int = 10): List<ResultDto> =
        MemoryCache.cached("athlete:results:$athleteId:$limit", ttlMs = 60_000L) {
            client.from("tournament_results")
                .select(Columns.raw("*, tournaments(id, name, start_date, sports(id, name))"))
                {
                    filter { eq("athlete_id", athleteId) }
                    order("position", Order.ASCENDING)
                    limit(limit.toLong())
                }
                .decodeList<ResultDto>()
        }

    suspend fun getAthleteMembership(athleteId: String): TeamMembershipDto? {
        // Wrap in single-element list because MemoryCache requires T : Any.
        val cached = MemoryCache.cached("athlete:membership:$athleteId", ttlMs = 180_000L) {
            try {
                val membership = client.from("team_members")
                    .select(Columns.raw("*, teams(id, name, city, sports(id, name))"))
                    { filter { eq("user_id", athleteId) } }
                    .decodeSingleOrNull<TeamMembershipDto>()
                if (membership != null) return@cached listOf(membership)

                // Fallback: trainer is team owner but not in team_members
                val ownedTeam = client.from("teams")
                    .select(Columns.raw("id, name, city, sports(id, name)"))
                    { filter { eq("owner_id", athleteId); eq("is_active", true) }; limit(1) }
                    .decodeSingleOrNull<TeamNameWithSportDto>()
                if (ownedTeam != null) {
                    listOf(TeamMembershipDto(teamId = ownedTeam.id, userId = athleteId, role = "captain", teams = ownedTeam))
                } else emptyList()
            } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); emptyList() }
        }
        return cached.firstOrNull()
    }

    suspend fun getTeamTournamentIds(teamId: String): List<String> {
        return try {
            client.from("tournament_participants")
                .select(Columns.raw("tournament_id"))
                { filter { eq("team_id", teamId) } }
                .decodeList<ParticipantTournamentIdDto>()
                .mapNotNull { it.tournamentId }
                .distinct()
        } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); emptyList() }
    }

    suspend fun getTeamDetail(teamId: String): TeamDto =
        MemoryCache.cached("team:$teamId", ttlMs = 600_000L) {
            client.from("teams")
                .select(Columns.raw("*, sports(id, name), profiles!owner_id(name, avatar_url)"))
                { filter { eq("id", teamId) } }
                .decodeSingle<TeamDto>()
        }

    suspend fun getTeamMembers(teamId: String): List<TeamMemberDto> =
        MemoryCache.cached("team:members:$teamId", ttlMs = 600_000L) {
            client.from("team_members")
                .select(Columns.raw("*, profiles(name, avatar_url, city)"))
                { filter { eq("team_id", teamId) } }
                .decodeList<TeamMemberDto>()
        }

    suspend fun getTeamMemberResults(memberIds: List<String>, limit: Int = 10): List<ResultDto> {
        return try {
            client.from("tournament_results")
                .select(Columns.raw("*, profiles(id, name), tournaments(id, name, start_date, sport_id, sports(id, name))"))
                {
                    filter { isIn("athlete_id", memberIds) }
                    order("position", Order.ASCENDING)
                    limit(limit.toLong())
                }
                .decodeList<ResultDto>()
        } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); emptyList() }
    }

    // ══════════════════════════════════════════════════════════
    // PROFILE (own)
    // ══════════════════════════════════════════════════════════

    suspend fun getProfile(userId: String): ProfileDto =
        MemoryCache.cached("profile:$userId", ttlMs = 60_000L) {
            client.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingle<ProfileDto>()
        }

    suspend fun updateProfile(userId: String, data: ProfileUpdateDto) {
        client.from("profiles")
            .update(data) { filter { eq("id", userId) } }
        // Profile changed — drop the cached copy so next read returns fresh data.
        MemoryCache.invalidate("profile:$userId")
    }

    suspend fun getLegalPages(): List<LegalPageDto> =
        // Legal pages change very rarely — cache for 30 minutes. Avoids hitting
        // platform_settings every time the user opens Settings or the legal sheet.
        MemoryCache.cached("legal_pages", ttlMs = 1_800_000L) {
            val row = client.from("platform_settings")
                .select { filter { eq("key", "legal_pages") } }
                .decodeSingleOrNull<PlatformSettingValueDto>()
                ?: return@cached emptyList()

            try {
                legalPagesJson.decodeFromString<List<LegalPageDto>>(row.value ?: "[]")
            } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); emptyList() }
        }

    suspend fun getUserTournaments(userId: String, limit: Int = 10): List<TournamentWithCountsDto> =
        MemoryCache.cached("user:tournaments:$userId:$limit", ttlMs = 60_000L) {
            // Get tournament IDs where user is a participant
            val participantRows = client.from("tournament_participants")
                .select(Columns.raw("tournament_id")) {
                    filter { eq("athlete_id", userId) }
                }
                .decodeList<ParticipantTournamentIdDto>()

            val ids = participantRows.mapNotNull { it.tournamentId }
            if (ids.isEmpty()) return@cached emptyList()

            client.from("v_tournament_with_counts")
                .select {
                    filter { isIn("id", ids) }
                    order("start_date", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<TournamentWithCountsDto>()
        }

    // ══════════════════════════════════════════════════════════
    // LEAGUES
    // ══════════════════════════════════════════════════════════

    suspend fun getLeagues(): List<LeagueDto> =
        MemoryCache.cached("viewer:leagues:all", ttlMs = 600_000L) {
            try {
                client.from("leagues")
                    .select(Columns.raw("*, sports(id, name), profiles(id, name)"))
                    { order("created_at", Order.DESCENDING) }
                    .decodeList<LeagueDto>()
            } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); emptyList() }
        }

    suspend fun getLeagueById(leagueId: String): LeagueDto =
        MemoryCache.cached("viewer:league:$leagueId", ttlMs = 600_000L) {
            client.from("leagues")
                .select(Columns.raw("*, sports(id, name), profiles(id, name)"))
                { filter { eq("id", leagueId) } }
                .decodeSingle<LeagueDto>()
        }

    suspend fun getLeagueStages(leagueId: String): List<LeagueStageDto> =
        MemoryCache.cached("viewer:league:stages:$leagueId", ttlMs = 300_000L) {
            try {
                client.from("league_stages")
                    .select(Columns.raw("*, tournaments(id, name, start_date)"))
                    {
                        filter { eq("league_id", leagueId) }
                        order("stage_number", Order.ASCENDING)
                    }
                    .decodeList<LeagueStageDto>()
            } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); emptyList() }
        }

    suspend fun getLeagueStandings(leagueId: String): List<LeagueStandingDto> =
        MemoryCache.cached("viewer:league:standings:$leagueId", ttlMs = 180_000L) {
            try {
                client.from("league_standings")
                    .select(Columns.raw("*, profiles(id, name, avatar_url)"))
                    {
                        filter { eq("league_id", leagueId) }
                        order("total_points", Order.DESCENDING)
                    }
                    .decodeList<LeagueStandingDto>()
            } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); emptyList() }
        }

    suspend fun getLeagueParticipantCount(leagueId: String): Int {
        return try {
            (client.from("league_standings")
                .select(Columns.raw("id")) {
                    count(Count.EXACT)
                    filter { eq("league_id", leagueId) }
                }
                .countOrNull() ?: 0L).toInt()
        } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); 0 }
    }

    suspend fun getLeagueCompletedStages(leagueId: String): Int {
        return try {
            (client.from("league_stages")
                .select(Columns.raw("id")) {
                    count(Count.EXACT)
                    filter {
                        eq("league_id", leagueId)
                        eq("status", "completed")
                    }
                }
                .countOrNull() ?: 0L).toInt()
        } catch (e: Exception) { AppLogger.w("ViewerRepo: ${e.message}"); 0 }
    }
}

@kotlinx.serialization.Serializable
private data class ParticipantTournamentIdDto(
    @kotlinx.serialization.SerialName("tournament_id") val tournamentId: String? = null
)
