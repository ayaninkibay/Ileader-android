package com.ileader.app.data.repository

import com.ileader.app.data.models.*
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AdminRepository {
    private val client = SupabaseModule.client

    // ══════════════════════════════════════════════════════════════
    // DASHBOARD
    // ══════════════════════════════════════════════════════════════

    suspend fun getPlatformStats(): AdminPlatformStats {
        val users = client.from("profiles")
            .select(Columns.raw("id, status, verification, primary_role_id"))
            .decodeList<AdminProfileCountDto>()

        val totalTournaments = client.from("tournaments")
            .select(Columns.raw("id"))
            .decodeList<IdOnlyDto>().size

        val activeTournaments = client.from("tournaments")
            .select(Columns.raw("id")) {
                filter { eq("status", "in_progress") }
            }
            .decodeList<IdOnlyDto>().size

        val totalSports = client.from("sports")
            .select(Columns.raw("id"))
            .decodeList<IdOnlyDto>().size

        val totalLocations = client.from("locations")
            .select(Columns.raw("id"))
            .decodeList<IdOnlyDto>().size

        return AdminPlatformStats(
            totalUsers = users.size,
            activeUsers = users.count { it.status == "active" },
            blockedUsers = users.count { it.status == "blocked" },
            pendingVerifications = users.count { it.verification == "pending" },
            totalTournaments = totalTournaments,
            activeTournaments = activeTournaments,
            totalSports = totalSports,
            totalLocations = totalLocations
        )
    }

    suspend fun getRecentUsers(limit: Long = 10): List<User> {
        return client.from("profiles")
            .select(Columns.raw("*, roles!primary_role_id(id, name)")) {
                order("created_at", Order.DESCENDING)
                limit(limit)
            }
            .decodeList<ProfileDto>()
            .map { it.toDomain() }
    }

    suspend fun getRoles(): List<RoleDto> {
        return client.from("roles")
            .select()
            .decodeList<RoleDto>()
    }

    suspend fun getRoleDistribution(): List<Pair<String, Int>> {
        val users = client.from("profiles")
            .select(Columns.raw("id, primary_role_id"))
            .decodeList<AdminProfileCountDto>()

        val roles = getRoles()
        return roles
            .filter { it.name != "user" && it.name != "admin" }
            .map { role ->
                val displayName = role.name
                val count = users.count { it.primaryRoleId == role.id }
                displayName to count
            }
    }

    // ══════════════════════════════════════════════════════════════
    // USERS
    // ══════════════════════════════════════════════════════════════

    suspend fun getAllUsers(): List<User> {
        return client.from("profiles")
            .select(Columns.raw("*, roles!primary_role_id(id, name)")) {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<ProfileDto>()
            .map { it.toDomain() }
    }

    suspend fun getUserDetail(userId: String): User {
        return client.from("profiles")
            .select(Columns.raw("*, roles!primary_role_id(id, name)")) {
                filter { eq("id", userId) }
            }
            .decodeSingle<ProfileDto>()
            .toDomain()
    }

    suspend fun updateUser(userId: String, data: AdminUserUpdateDto) {
        client.from("profiles")
            .update(data) {
                filter { eq("id", userId) }
            }
    }

    suspend fun blockUser(userId: String) {
        client.from("profiles")
            .update(buildJsonObject { put("status", "blocked") }) {
                filter { eq("id", userId) }
            }
    }

    suspend fun unblockUser(userId: String) {
        client.from("profiles")
            .update(buildJsonObject { put("status", "active") }) {
                filter { eq("id", userId) }
            }
    }

    suspend fun verifyUser(userId: String) {
        client.from("profiles")
            .update(buildJsonObject { put("verification", "verified") }) {
                filter { eq("id", userId) }
            }
    }

    suspend fun rejectVerification(userId: String) {
        client.from("profiles")
            .update(buildJsonObject { put("verification", "rejected") }) {
                filter { eq("id", userId) }
            }
    }

    suspend fun deleteUser(userId: String) {
        client.from("profiles")
            .delete {
                filter { eq("id", userId) }
            }
    }

    // Создание пользователя через Edge Function
    // TODO: Подключить Edge Function create-user когда Functions plugin будет добавлен
    suspend fun createUser(data: AdminCreateUserRequest): String {
        // Пока заглушка — реальная реализация требует Edge Function
        // чтобы не разлогинить текущего админа
        error("Создание пользователя временно недоступно в мобильном приложении. Используйте веб-панель.")
    }

    // ══════════════════════════════════════════════════════════════
    // VERIFICATIONS
    // ══════════════════════════════════════════════════════════════

    suspend fun getPendingVerifications(): List<User> {
        return client.from("profiles")
            .select(Columns.raw("*, roles!primary_role_id(id, name)")) {
                filter { eq("verification", "pending") }
            }
            .decodeList<ProfileDto>()
            .map { it.toDomain() }
    }

    // ══════════════════════════════════════════════════════════════
    // TOURNAMENTS
    // ══════════════════════════════════════════════════════════════

    suspend fun getAllTournaments(): List<TournamentWithCountsDto> {
        return client.from("v_tournament_with_counts")
            .select()
            .decodeList<TournamentWithCountsDto>()
    }

    suspend fun deleteTournament(tournamentId: String) {
        client.from("tournaments")
            .delete { filter { eq("id", tournamentId) } }
    }

    suspend fun updateTournamentStatus(tournamentId: String, status: String) {
        client.from("tournaments")
            .update(buildJsonObject { put("status", status) }) {
                filter { eq("id", tournamentId) }
            }
    }

    suspend fun getTournamentDetail(tournamentId: String): TournamentDto {
        return client.from("tournaments")
            .select(Columns.raw("*, sports(id, name, slug), locations(name, city), profiles!organizer_id(name)")) {
                filter { eq("id", tournamentId) }
            }
            .decodeSingle<TournamentDto>()
    }

    suspend fun updateTournament(tournamentId: String, data: Map<String, String>) {
        val json = buildJsonObject {
            data.forEach { (key, value) -> put(key, value) }
        }
        client.from("tournaments")
            .update(json) {
                filter { eq("id", tournamentId) }
            }
    }

    // ══════════════════════════════════════════════════════════════
    // LOCATIONS
    // ══════════════════════════════════════════════════════════════

    suspend fun getAllLocations(): List<LocationDto> {
        return client.from("locations")
            .select() { order("created_at", Order.DESCENDING) }
            .decodeList<LocationDto>()
    }

    suspend fun getLocationDetail(locationId: String): LocationDto {
        return client.from("locations")
            .select { filter { eq("id", locationId) } }
            .decodeSingle<LocationDto>()
    }

    suspend fun deleteLocation(locationId: String) {
        client.from("locations")
            .delete { filter { eq("id", locationId) } }
    }

    suspend fun updateLocation(locationId: String, data: LocationInsertDto) {
        client.from("locations")
            .update(data) {
                filter { eq("id", locationId) }
            }
    }

    suspend fun getLocationTournaments(locationId: String): List<TournamentWithCountsDto> {
        return client.from("v_tournament_with_counts")
            .select {
                filter { eq("location_id", locationId) }
            }
            .decodeList<TournamentWithCountsDto>()
    }

    // ══════════════════════════════════════════════════════════════
    // SPORTS
    // ══════════════════════════════════════════════════════════════

    suspend fun getAllSports(): List<SportDto> {
        return client.from("sports")
            .select()
            .decodeList<SportDto>()
    }

    suspend fun getSportDetail(sportId: String): SportDto {
        return client.from("sports")
            .select { filter { eq("id", sportId) } }
            .decodeSingle<SportDto>()
    }

    suspend fun createSport(data: SportInsertDto): String {
        val result = client.from("sports")
            .insert(data) {
                select()
            }
            .decodeSingle<IdOnlyDto>()
        return result.id
    }

    suspend fun updateSport(sportId: String, data: SportUpdateDto) {
        client.from("sports")
            .update(data) {
                filter { eq("id", sportId) }
            }
    }

    suspend fun toggleSportActive(sportId: String, isActive: Boolean) {
        client.from("sports")
            .update(buildJsonObject { put("is_active", isActive) }) {
                filter { eq("id", sportId) }
            }
    }

    suspend fun deleteSport(sportId: String) {
        client.from("sports")
            .delete { filter { eq("id", sportId) } }
    }

    suspend fun getSportAthleteCount(sportId: String): Int {
        return client.from("user_sports")
            .select(Columns.raw("id")) {
                filter { eq("sport_id", sportId) }
            }
            .decodeList<IdOnlyDto>().size
    }

    suspend fun getSportTournamentCount(sportId: String): Int {
        return client.from("tournaments")
            .select(Columns.raw("id")) {
                filter { eq("sport_id", sportId) }
            }
            .decodeList<IdOnlyDto>().size
    }

    // ══════════════════════════════════════════════════════════════
    // REQUESTS & INVITES
    // ══════════════════════════════════════════════════════════════

    suspend fun getTeamRequests(): List<TeamRequestDto> {
        return client.from("team_requests")
            .select(Columns.raw("*, profiles!user_id(name, avatar_url), teams(id, name)")) {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<TeamRequestDto>()
    }

    suspend fun getTournamentInvites(): List<TournamentInviteDto> {
        return client.from("tournament_invites")
            .select(Columns.raw("*, profiles!user_id(name, avatar_url), tournaments(id, name)")) {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<TournamentInviteDto>()
    }

    suspend fun respondToTeamRequest(requestId: String, approve: Boolean) {
        val status = if (approve) "accepted" else "declined"
        client.from("team_requests")
            .update(buildJsonObject { put("status", status) }) {
                filter { eq("id", requestId) }
            }
    }

    suspend fun respondToInvite(inviteId: String, approve: Boolean) {
        val status = if (approve) "accepted" else "declined"
        client.from("tournament_invites")
            .update(buildJsonObject { put("status", status) }) {
                filter { eq("id", inviteId) }
            }
    }

    suspend fun getPendingSponsorships(): List<SponsorshipDto> {
        return client.from("sponsorships")
            .select(Columns.raw("*, profiles!sponsor_id(name, avatar_url), teams(id, name), tournaments(id, name)")) {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<SponsorshipDto>()
    }

    suspend fun respondToSponsorship(sponsorshipId: String, approve: Boolean) {
        val status = if (approve) "active" else "cancelled"
        client.from("sponsorships")
            .update(buildJsonObject { put("status", status) }) {
                filter { eq("id", sponsorshipId) }
            }
    }
}
