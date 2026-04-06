package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.util.safeApiCall
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ViewerRepository {
    private val client = SupabaseModule.client

    companion object {
        private val roleIdCache = mutableMapOf<String, String>()
        private val roleIdMutex = Mutex()
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

    suspend fun getPlatformStats(): Triple<Int, Int, Int> = safeApiCall("ViewerRepo.getPlatformStats") {
        val usersCount = client.from("profiles")
            .select(Columns.raw("id")) { filter { eq("status", "active") } }
            .decodeList<IdOnlyDto>().size

        val tournamentsCount = client.from("tournaments")
            .select(Columns.raw("id"))
            .decodeList<IdOnlyDto>().size

        val sportsCount = client.from("sports")
            .select(Columns.raw("id")) { filter { eq("is_active", true) } }
            .decodeList<IdOnlyDto>().size

        Triple(usersCount, tournamentsCount, sportsCount)
    }

    suspend fun getSports(): List<SportDto> {
        return client.from("sports")
            .select { filter { eq("is_active", true) } }
            .decodeList<SportDto>()
    }

    suspend fun getUpcomingTournaments(limit: Int = 10): List<TournamentWithCountsDto> = safeApiCall("ViewerRepo.getUpcomingTournaments") {
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

    suspend fun getPublicTournaments(): List<TournamentWithCountsDto> = safeApiCall("ViewerRepo.getPublicTournaments") {
        client.from("v_tournament_with_counts")
            .select {
                filter { eq("visibility", "public") }
                order("start_date", Order.DESCENDING)
                limit(100)
            }
            .decodeList<TournamentWithCountsDto>()
    }

    suspend fun getTournamentDetail(tournamentId: String): TournamentDto = safeApiCall("ViewerRepo.getTournamentDetail") {
        client.from("tournaments")
            .select(Columns.raw("*, sports(id, name, slug), locations(*), profiles!organizer_id(name)"))
            { filter { eq("id", tournamentId) } }
            .decodeSingle<TournamentDto>()
    }

    suspend fun getTournamentParticipants(tournamentId: String): List<ParticipantDto> {
        return client.from("tournament_participants")
            .select(Columns.raw("*, profiles(name, avatar_url, city)"))
            {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("status", "confirmed")
                }
                order("seed", Order.ASCENDING)
            }
            .decodeList<ParticipantDto>()
    }

    suspend fun getTournamentResults(tournamentId: String): List<ResultDto> {
        return client.from("tournament_results")
            .select(Columns.raw("*, profiles!athlete_id(name, avatar_url, city)"))
            {
                filter { eq("tournament_id", tournamentId) }
                order("position", Order.ASCENDING)
            }
            .decodeList<ResultDto>()
    }

    suspend fun getTournamentBracket(tournamentId: String): List<BracketMatchDto> {
        return client.from("bracket_matches")
            .select {
                filter { eq("tournament_id", tournamentId) }
                order("round", Order.ASCENDING)
                order("match_number", Order.ASCENDING)
            }
            .decodeList<BracketMatchDto>()
    }

    suspend fun getTournamentGroups(tournamentId: String): List<TournamentGroupDto> {
        return client.from("tournament_groups")
            .select { filter { eq("tournament_id", tournamentId) } }
            .decodeList<TournamentGroupDto>()
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

    suspend fun getPublishedArticles(limit: Int = 50): List<ArticleDto> {
        return client.from("articles")
            .select(Columns.raw("id, title, excerpt, cover_image_url, category, tags, views, published_at, created_at, profiles!author_id(id, name, avatar_url), sports(id, name)")) {
                filter { eq("status", "published") }
                order("published_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<ArticleDto>()
    }

    suspend fun getRecentArticles(limit: Int = 5): List<ArticleDto> {
        return client.from("articles")
            .select(Columns.raw("id, title, excerpt, cover_image_url, category, views, published_at, profiles!author_id(id, name)")) {
                filter { eq("status", "published") }
                order("published_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<ArticleDto>()
    }

    suspend fun getArticleDetail(articleId: String): ArticleDto {
        return client.from("articles")
            .select(Columns.raw("*, profiles!author_id(id, name, avatar_url), sports(id, name)"))
            { filter { eq("id", articleId) } }
            .decodeSingle<ArticleDto>()
    }

    // ══════════════════════════════════════════════════════════
    // COMMUNITY
    // ══════════════════════════════════════════════════════════

    suspend fun getAthletes(): List<CommunityProfileDto> {
        val roleId = getRoleId("athlete")
        return client.from("profiles")
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

    suspend fun getTrainers(): List<CommunityProfileDto> {
        val roleId = getRoleId("trainer")
        return client.from("profiles")
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

    suspend fun getReferees(): List<CommunityProfileDto> {
        val roleId = getRoleId("referee")
        return client.from("profiles")
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

    suspend fun getTeams(): List<TeamWithStatsDto> {
        return client.from("teams")
            .select(Columns.raw("*, sports(id, name), profiles!owner_id(name), team_members(count)"))
            { filter { eq("is_active", true) } }
            .decodeList<TeamWithStatsDto>()
    }

    // ══════════════════════════════════════════════════════════
    // SPORT IMAGES
    // ══════════════════════════════════════════════════════════

    suspend fun getSportImageUrls(sportSlug: String): List<String> {
        return try {
            val bucket = client.storage.from("sport-images")
            val files = bucket.list(sportSlug)
            files.filter { f ->
                f.name.endsWith(".jpeg") || f.name.endsWith(".jpg") || f.name.endsWith(".png") || f.name.endsWith(".webp")
            }.map { f -> bucket.publicUrl("$sportSlug/${f.name}") }
        } catch (_: Exception) { emptyList() }
    }

    // ══════════════════════════════════════════════════════════
    // SPORT DETAIL
    // ══════════════════════════════════════════════════════════

    suspend fun getTournamentsBySport(sportId: String, limit: Int = 5): List<TournamentWithCountsDto> = safeApiCall("ViewerRepo.getTournamentsBySport") {
        client.from("v_tournament_with_counts")
            .select {
                filter { eq("visibility", "public"); eq("sport_id", sportId) }
                order("start_date", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<TournamentWithCountsDto>()
    }

    suspend fun getProfilesBySportAndRole(sportId: String, roleName: String, limit: Int = 5): List<CommunityProfileDto> {
        val roleId = getRoleId(roleName)
        return client.from("profiles")
            .select(Columns.raw("id, name, avatar_url, city, bio, athlete_subtype, user_sports!inner(rating, is_primary, sports(id, name))"))
            {
                filter { eq("primary_role_id", roleId); eq("status", "active"); eq("user_sports.sport_id", sportId) }
                limit(limit.toLong())
            }
            .decodeList<CommunityProfileDto>()
    }

    suspend fun getTeamsBySport(sportId: String, limit: Int = 5): List<TeamWithStatsDto> {
        return client.from("teams")
            .select(Columns.raw("*, sports(id, name), profiles!owner_id(name), team_members(count)"))
            { filter { eq("is_active", true); eq("sport_id", sportId) }; limit(limit.toLong()) }
            .decodeList<TeamWithStatsDto>()
    }

    suspend fun getArticlesBySport(sportId: String, limit: Int = 5): List<ArticleDto> {
        return client.from("articles")
            .select(Columns.raw("id, title, excerpt, cover_image_url, category, views, published_at, created_at, profiles!author_id(id, name), sports(id, name)"))
            { filter { eq("status", "published"); eq("sport_id", sportId) }; order("published_at", Order.DESCENDING); limit(limit.toLong()) }
            .decodeList<ArticleDto>()
    }

    // ══════════════════════════════════════════════════════════
    // PUBLIC PROFILES
    // ══════════════════════════════════════════════════════════

    suspend fun getPublicProfile(userId: String): ProfileDto {
        return client.from("profiles")
            .select(columns = Columns.raw("*, roles(*)")) { filter { eq("id", userId) } }
            .decodeSingle<ProfileDto>()
    }

    suspend fun getUserSports(userId: String): List<UserSportDto> {
        return client.from("user_sports")
            .select(Columns.raw("*, sports(id, name, slug)"))
            { filter { eq("user_id", userId) } }
            .decodeList<UserSportDto>()
    }

    suspend fun getUserSportStats(userId: String): List<UserSportStatsDto> {
        return client.from("v_user_sport_stats")
            .select { filter { eq("user_id", userId) } }
            .decodeList<UserSportStatsDto>()
    }

    suspend fun getAthleteResults(athleteId: String, limit: Int = 10): List<ResultDto> {
        return client.from("tournament_results")
            .select(Columns.raw("*, tournaments(id, name, start_date, sports(id, name))"))
            {
                filter { eq("athlete_id", athleteId) }
                order("position", Order.ASCENDING)
                limit(limit.toLong())
            }
            .decodeList<ResultDto>()
    }

    suspend fun getAthleteMembership(athleteId: String): TeamMembershipDto? {
        return try {
            val membership = client.from("team_members")
                .select(Columns.raw("*, teams(id, name, city, sports(id, name))"))
                { filter { eq("user_id", athleteId) } }
                .decodeSingleOrNull<TeamMembershipDto>()
            if (membership != null) return membership

            // Fallback: trainer is team owner but not in team_members
            val ownedTeam = client.from("teams")
                .select(Columns.raw("id, name, city, sports(id, name)"))
                { filter { eq("owner_id", athleteId); eq("is_active", true) }; limit(1) }
                .decodeSingleOrNull<TeamNameWithSportDto>()
            if (ownedTeam != null) {
                TeamMembershipDto(teamId = ownedTeam.id, userId = athleteId, role = "captain", teams = ownedTeam)
            } else null
        } catch (_: Exception) { null }
    }

    suspend fun getTeamTournamentIds(teamId: String): List<String> {
        return try {
            client.from("tournament_participants")
                .select(Columns.raw("tournament_id"))
                { filter { eq("team_id", teamId) } }
                .decodeList<ParticipantTournamentIdDto>()
                .mapNotNull { it.tournamentId }
                .distinct()
        } catch (_: Exception) { emptyList() }
    }

    suspend fun getTeamDetail(teamId: String): TeamDto {
        return client.from("teams")
            .select(Columns.raw("*, sports(id, name), profiles!owner_id(name, avatar_url)"))
            { filter { eq("id", teamId) } }
            .decodeSingle<TeamDto>()
    }

    suspend fun getTeamMembers(teamId: String): List<TeamMemberDto> {
        return client.from("team_members")
            .select(Columns.raw("*, profiles(name, avatar_url, city)"))
            { filter { eq("team_id", teamId) } }
            .decodeList<TeamMemberDto>()
    }

    // ══════════════════════════════════════════════════════════
    // PROFILE (own)
    // ══════════════════════════════════════════════════════════

    suspend fun getProfile(userId: String): ProfileDto {
        return client.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingle<ProfileDto>()
    }

    suspend fun updateProfile(userId: String, data: ProfileUpdateDto) {
        client.from("profiles")
            .update(data) { filter { eq("id", userId) } }
    }

    suspend fun getLegalPages(): List<LegalPageDto> {
        val row = client.from("platform_settings")
            .select { filter { eq("key", "legal_pages") } }
            .decodeSingleOrNull<PlatformSettingValueDto>()
            ?: return emptyList()

        return try {
            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .decodeFromString<List<LegalPageDto>>(row.value ?: "[]")
        } catch (_: Exception) { emptyList() }
    }

    suspend fun getUserTournaments(userId: String, limit: Int = 10): List<TournamentWithCountsDto> {
        // Get tournament IDs where user is a participant
        val participantRows = client.from("tournament_participants")
            .select(Columns.raw("tournament_id")) {
                filter { eq("athlete_id", userId) }
            }
            .decodeList<ParticipantTournamentIdDto>()

        val ids = participantRows.mapNotNull { it.tournamentId }
        if (ids.isEmpty()) return emptyList()

        return client.from("v_tournament_with_counts")
            .select {
                filter { isIn("id", ids) }
                order("start_date", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<TournamentWithCountsDto>()
    }
}

@kotlinx.serialization.Serializable
private data class ParticipantTournamentIdDto(
    @kotlinx.serialization.SerialName("tournament_id") val tournamentId: String? = null
)
