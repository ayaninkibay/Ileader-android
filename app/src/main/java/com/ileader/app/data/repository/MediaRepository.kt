package com.ileader.app.data.repository

import com.ileader.app.data.models.User
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MediaRepository {
    private val client = SupabaseModule.client

    // ══════════════════════════════════════════════════════════
    // PROFILE
    // ══════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════
    // TOURNAMENTS
    // ══════════════════════════════════════════════════════════

    suspend fun getAllTournaments(): List<TournamentWithCountsDto> {
        return client.from("v_tournament_with_counts")
            .select {
                order("start_date", Order.DESCENDING)
            }
            .decodeList<TournamentWithCountsDto>()
    }

    suspend fun getUpcomingTournaments(limit: Int = 4): List<TournamentWithCountsDto> {
        return client.from("v_tournament_with_counts")
            .select {
                filter {
                    neq("status", "completed")
                    neq("status", "cancelled")
                }
                order("start_date", Order.ASCENDING)
                limit(limit.toLong())
            }
            .decodeList<TournamentWithCountsDto>()
    }

    // ══════════════════════════════════════════════════════════
    // ACCREDITATIONS (tournament_invites with role='media')
    // ══════════════════════════════════════════════════════════

    /**
     * Get accreditation statuses for this media user across all tournaments.
     * Returns a map of tournamentId → status (pending/accepted/declined).
     */
    suspend fun getAccreditationMap(userId: String): Map<String, String> {
        val invites = client.from("tournament_invites")
            .select(Columns.raw("id, tournament_id, status")) {
                filter {
                    eq("user_id", userId)
                    eq("role", "media")
                }
            }
            .decodeList<MediaAccreditationDto>()
        return invites.associate { it.tournamentId to (it.status ?: "pending") }
    }

    /**
     * Get total accreditation stats for dashboard.
     */
    suspend fun getAccreditationStats(userId: String): AccreditationStats {
        val invites = client.from("tournament_invites")
            .select(Columns.raw("id, tournament_id, status")) {
                filter {
                    eq("user_id", userId)
                    eq("role", "media")
                }
            }
            .decodeList<MediaAccreditationDto>()
        return AccreditationStats(
            total = invites.size,
            accepted = invites.count { it.status == "accepted" },
            pending = invites.count { it.status == "pending" }
        )
    }

    /**
     * Request accreditation for a tournament (creates an invite).
     */
    suspend fun requestAccreditation(userId: String, tournamentId: String, message: String? = null) {
        client.from("tournament_invites")
            .insert(
                MediaInviteInsertDto(
                    tournamentId = tournamentId,
                    userId = userId,
                    message = message
                )
            )
    }

    /**
     * Join a tournament by invite code (for media accreditation).
     */
    suspend fun joinByInviteCode(code: String, userId: String) {
        val result = client.postgrest.rpc("get_tournament_by_invite_code", buildJsonObject { put("code", code) })
            .decodeSingle<MediaInviteCodeResultDto>()
        val tournamentId = result.tournamentId
            ?: throw Exception("Инвайт-код не найден или недействителен")
        // Use the invite code (increments used_count)
        client.postgrest.rpc("use_invite_code", buildJsonObject { put("code", code) })
        // Create accreditation
        requestAccreditation(userId, tournamentId)
    }

    /**
     * Cancel/delete accreditation request.
     */
    suspend fun cancelAccreditation(userId: String, tournamentId: String) {
        client.from("tournament_invites")
            .delete {
                filter {
                    eq("user_id", userId)
                    eq("tournament_id", tournamentId)
                    eq("role", "media")
                }
            }
    }

    // ══════════════════════════════════════════════════════════
    // INVITES / NOTIFICATIONS
    // ══════════════════════════════════════════════════════════

    /**
     * Get all media invites with full tournament details.
     */
    suspend fun getMediaInvites(userId: String): List<MediaInviteFullDto> {
        return client.from("tournament_invites")
            .select(
                Columns.raw("*, tournaments(id, name, start_date, status, organizer_id, sports(id, name), locations(name, city))")
            ) {
                filter {
                    eq("user_id", userId)
                    eq("role", "media")
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<MediaInviteFullDto>()
    }

    /**
     * Accept a media invite with optional contact phone and message.
     */
    suspend fun acceptInvite(inviteId: String, contactPhone: String, message: String?) {
        val commentsJson = buildJsonArray {
            add(buildJsonObject {
                put("contact_phone", contactPhone)
            })
        }
        client.from("tournament_invites")
            .update(
                MediaInviteAcceptDto(
                    responseMessage = message,
                    comments = commentsJson
                )
            ) {
                filter { eq("id", inviteId) }
            }
    }

    /**
     * Decline a media invite with a reason.
     */
    suspend fun declineInvite(inviteId: String, reason: String) {
        client.from("tournament_invites")
            .update(
                MediaInviteDeclineDto(responseMessage = reason)
            ) {
                filter { eq("id", inviteId) }
            }
    }

    // ══════════════════════════════════════════════════════════
    // ARTICLES
    // ══════════════════════════════════════════════════════════

    suspend fun getMyArticles(authorId: String): List<ArticleDto> {
        return client.from("articles")
            .select(Columns.raw("id, author_id, title, excerpt, cover_image_url, sport_id, tournament_id, status, category, tags, views, published_at, created_at, updated_at, sports(id, name)")) {
                filter { eq("author_id", authorId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<ArticleDto>()
    }

    suspend fun getArticleById(articleId: String): ArticleDto {
        return client.from("articles")
            .select(Columns.raw("id, author_id, title, content, excerpt, cover_image_url, sport_id, tournament_id, status, category, tags, views, published_at, created_at, updated_at, profiles!author_id(id, name, avatar_url), sports(id, name)")) {
                filter { eq("id", articleId) }
            }
            .decodeSingle<ArticleDto>()
    }

    suspend fun getArticleStats(authorId: String): ArticleStatsDto {
        val articles = client.from("articles")
            .select(Columns.raw("id, status, views")) {
                filter { eq("author_id", authorId) }
            }
            .decodeList<ArticleDto>()

        return ArticleStatsDto(
            total = articles.size,
            published = articles.count { it.status == "published" },
            drafts = articles.count { it.status == "draft" },
            totalViews = articles.sumOf { it.views }
        )
    }

    suspend fun createArticle(data: ArticleInsertDto): ArticleDto {
        return client.from("articles")
            .insert(data) {
                select(Columns.raw("id, author_id, title, content, excerpt, cover_image_url, sport_id, tournament_id, status, category, tags, views, published_at, created_at, updated_at"))
            }
            .decodeSingle<ArticleDto>()
    }

    suspend fun updateArticle(articleId: String, data: ArticleUpdateDto) {
        client.from("articles")
            .update(data) {
                filter { eq("id", articleId) }
            }
    }

    suspend fun deleteArticle(articleId: String) {
        client.from("articles")
            .delete {
                filter { eq("id", articleId) }
            }
    }

    /**
     * Get top articles by views for the given author (for analytics).
     */
    suspend fun getTopArticlesByViews(authorId: String, limit: Int = 5): List<ArticleDto> {
        return client.from("articles")
            .select(Columns.raw("id, title, views, status, category, published_at, created_at")) {
                filter {
                    eq("author_id", authorId)
                    eq("status", "published")
                }
                order("views", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<ArticleDto>()
    }

    /**
     * Get article count grouped by category for analytics.
     */
    suspend fun getArticlesByCategory(authorId: String): Map<String, Int> {
        val articles = client.from("articles")
            .select(Columns.raw("id, category")) {
                filter { eq("author_id", authorId) }
            }
            .decodeList<ArticleDto>()
        return articles.groupBy { it.category ?: "other" }.mapValues { it.value.size }
    }

    // ══════════════════════════════════════════════════════════
    // NOTIFICATIONS
    // ══════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════
    // TEAM
    // ══════════════════════════════════════════════════════════

    suspend fun getTeam(teamId: String): TeamDto {
        return client.from("teams")
            .select(Columns.raw("*, sports(id, name), profiles!owner_id(id, name, avatar_url)")) {
                filter { eq("id", teamId) }
            }
            .decodeSingle<TeamDto>()
    }

    suspend fun getTeamMembers(teamId: String): List<TeamMemberDto> {
        return client.from("team_members")
            .select(Columns.raw("*, profiles(id, name, avatar_url, city, email)")) {
                filter { eq("team_id", teamId) }
                order("joined_at", Order.ASCENDING)
            }
            .decodeList<TeamMemberDto>()
    }
}

data class AccreditationStats(
    val total: Int,
    val accepted: Int,
    val pending: Int
)
