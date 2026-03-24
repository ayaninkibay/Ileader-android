package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class SponsorRepository {
    private val client = SupabaseModule.client

    // ── Dashboard ──

    suspend fun getSponsorships(sponsorId: String): List<SponsorshipDto> {
        return client.from("sponsorships")
            .select(Columns.raw("*, teams(id, name), tournaments(id, name, start_date, sport_id, sports(id, name)), profiles!sponsor_id(name)")) {
                filter { eq("sponsor_id", sponsorId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<SponsorshipDto>()
    }

    suspend fun getOpenTournaments(): List<TournamentWithCountsDto> {
        return client.from("v_tournament_with_counts")
            .select {
                filter {
                    or {
                        eq("status", "registration_open")
                        eq("status", "in_progress")
                    }
                }
                order("start_date", Order.ASCENDING)
                limit(10)
            }
            .decodeList<TournamentWithCountsDto>()
    }

    // ── Teams ──

    suspend fun getAvailableTeams(): List<TeamWithStatsDto> {
        return client.from("teams")
            .select(Columns.raw("*, sports(id, name), profiles!owner_id(name), team_members(count)")) {
                filter { eq("is_active", true) }
                order("name", Order.ASCENDING)
            }
            .decodeList<TeamWithStatsDto>()
    }

    suspend fun getSports(): List<SportDto> {
        return client.from("sports")
            .select {
                filter { eq("is_active", true) }
                order("name", Order.ASCENDING)
            }
            .decodeList<SportDto>()
    }

    suspend fun createSponsorshipRequest(sponsorId: String, teamId: String) {
        client.from("sponsorships")
            .insert(SponsorshipInsertDto(
                sponsorId = sponsorId,
                teamId = teamId,
                tier = "partner",
                status = "pending"
            ))
    }

    suspend fun requestTournamentSponsorship(sponsorId: String, tournamentId: String, amount: Double? = null) {
        client.from("sponsorships")
            .insert(SponsorshipInsertDto(
                sponsorId = sponsorId,
                tournamentId = tournamentId,
                tier = "partner",
                amount = amount,
                status = "pending"
            ))
    }

    // ── My Team ──

    suspend fun getSponsoredTeams(sponsorId: String): List<SponsorshipDto> {
        return client.from("sponsorships")
            .select(Columns.raw("*, teams(id, name, sport_id, owner_id, city, sports(id, name), profiles!owner_id(name))")) {
                filter {
                    eq("sponsor_id", sponsorId)
                    neq("team_id", "null")
                    eq("status", "active")
                }
            }
            .decodeList<SponsorshipDto>()
    }

    suspend fun getTeamMembers(teamId: String): List<TeamMemberDto> {
        return client.from("team_members")
            .select(Columns.raw("*, profiles(id, name, avatar_url)")) {
                filter { eq("team_id", teamId) }
                order("joined_at", Order.ASCENDING)
            }
            .decodeList<TeamMemberDto>()
    }

    // ── Tournaments ──

    suspend fun getSponsoredTournaments(sponsorId: String): List<SponsorshipDto> {
        return client.from("sponsorships")
            .select(Columns.raw("*, tournaments(id, name, status, start_date, end_date, sport_id, location_id, max_participants, sports(id, name), locations(name, city))")) {
                filter {
                    eq("sponsor_id", sponsorId)
                    neq("tournament_id", "null")
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<SponsorshipDto>()
    }

    suspend fun getAllTournaments(): List<TournamentWithCountsDto> {
        return client.from("v_tournament_with_counts")
            .select {
                order("start_date", Order.DESCENDING)
                limit(50)
            }
            .decodeList<TournamentWithCountsDto>()
    }

    // ── Tournament Detail ──

    suspend fun getTournamentDetail(tournamentId: String): TournamentDto {
        return client.from("tournaments")
            .select(Columns.raw("*, sports(id, name), locations(name, city), profiles!organizer_id(name)")) {
                filter { eq("id", tournamentId) }
            }
            .decodeSingle<TournamentDto>()
    }

    suspend fun getTournamentParticipantCount(tournamentId: String): Int {
        return client.from("tournament_participants")
            .select(Columns.raw("count")) {
                filter { eq("tournament_id", tournamentId) }
            }
            .decodeList<MemberCountDto>()
            .firstOrNull()?.count ?: 0
    }

    suspend fun getTournamentRefereeCount(tournamentId: String): Int {
        return client.from("tournament_referees")
            .select(Columns.raw("count")) {
                filter { eq("tournament_id", tournamentId) }
            }
            .decodeList<MemberCountDto>()
            .firstOrNull()?.count ?: 0
    }

    suspend fun getSponsorshipForTournament(sponsorId: String, tournamentId: String): SponsorshipDto? {
        return try {
            client.from("sponsorships")
                .select {
                    filter {
                        eq("sponsor_id", sponsorId)
                        eq("tournament_id", tournamentId)
                    }
                }
                .decodeList<SponsorshipDto>()
                .firstOrNull()
        } catch (_: Exception) { null }
    }

    // ── Notifications (Invites) ──

    suspend fun getInvites(sponsorId: String): List<TournamentInviteDto> {
        return client.from("tournament_invites")
            .select(Columns.raw("*, tournaments(id, name, start_date, sport_id, sports(id, name)), profiles!user_id(name)")) {
                filter {
                    eq("user_id", sponsorId)
                    eq("role", "sponsor")
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<TournamentInviteDto>()
    }

    suspend fun acceptInvite(inviteId: String) {
        client.from("tournament_invites")
            .update({ set("status", "accepted") }) {
                filter { eq("id", inviteId) }
            }
    }

    suspend fun declineInvite(inviteId: String, responseMessage: String?) {
        client.from("tournament_invites")
            .update({
                set("status", "declined")
                if (responseMessage != null) set("response_message", responseMessage)
            }) {
                filter { eq("id", inviteId) }
            }
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

    // ── Profile ──

    suspend fun getProfile(userId: String): ProfileDto {
        return client.from("profiles")
            .select(Columns.raw("*, roles!primary_role_id(id, name)")) {
                filter { eq("id", userId) }
            }
            .decodeSingle<ProfileDto>()
    }

    suspend fun updateProfile(userId: String, data: ProfileUpdateDto) {
        client.from("profiles")
            .update(data) {
                filter { eq("id", userId) }
            }
    }
}

