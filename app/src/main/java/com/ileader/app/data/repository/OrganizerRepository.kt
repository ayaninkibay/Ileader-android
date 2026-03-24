package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage

class OrganizerRepository {

    private val client = SupabaseModule.client

    // ── DASHBOARD ──

    suspend fun getStats(userId: String): OrganizerStatsDto {
        return client.from("v_organizer_stats")
            .select { filter { eq("organizer_id", userId) } }
            .decodeSingleOrNull<OrganizerStatsDto>()
            ?: OrganizerStatsDto()
    }

    suspend fun getUpcomingTournaments(userId: String): List<TournamentDto> {
        return client.from("tournaments")
            .select(Columns.raw("*, sports(id, name, slug), locations(name, city)")) {
                filter {
                    eq("organizer_id", userId)
                    neq("status", "completed")
                    neq("status", "cancelled")
                }
                order("start_date", Order.ASCENDING)
                limit(5)
            }
            .decodeList<TournamentDto>()
    }

    suspend fun getRecentRegistrations(userId: String): List<ParticipantDto> {
        val tournamentIds = client.from("tournaments")
            .select(Columns.raw("id"))
            { filter { eq("organizer_id", userId) } }
            .decodeList<IdOnlyDto>()
            .map { it.id }

        if (tournamentIds.isEmpty()) return emptyList()

        return client.from("tournament_participants")
            .select(Columns.raw("*, profiles(id, name, avatar_url), tournaments(id, name)")) {
                filter { isIn("tournament_id", tournamentIds) }
                order("registered_at", Order.DESCENDING)
                limit(10)
            }
            .decodeList<ParticipantDto>()
    }

    // ── TOURNAMENTS ──

    suspend fun getMyTournaments(userId: String): List<TournamentWithCountsDto> {
        return client.from("v_tournament_with_counts")
            .select { filter { eq("organizer_id", userId) } }
            .decodeList<TournamentWithCountsDto>()
    }

    suspend fun getTournamentDetail(tournamentId: String): TournamentDto {
        return client.from("tournaments")
            .select(Columns.raw("*, sports(id, name, slug), locations(*), profiles!organizer_id(name)"))
            { filter { eq("id", tournamentId) } }
            .decodeSingle<TournamentDto>()
    }

    suspend fun createTournament(data: TournamentInsertDto): String {
        val result = client.from("tournaments")
            .insert(data) { select() }
            .decodeSingle<TournamentDto>()
        return result.id
    }

    suspend fun updateTournament(tournamentId: String, data: TournamentInsertDto) {
        client.from("tournaments")
            .update({
                set("name", data.name)
                set("sport_id", data.sportId)
                set("location_id", data.locationId)
                set("status", data.status)
                set("start_date", data.startDate)
                set("end_date", data.endDate)
                set("description", data.description)
                set("format", data.format)
                set("match_format", data.matchFormat)
                set("seeding_type", data.seedingType)
                set("visibility", data.visibility)
                set("max_participants", data.maxParticipants)
                set("min_participants", data.minParticipants)
                set("prize", data.prize)
                set("requirements", data.requirements)
                set("categories", data.categories)
                set("age_category", data.ageCategory)
                set("group_count", data.groupCount)
                set("has_third_place_match", data.hasThirdPlaceMatch)
                set("has_check_in", data.hasCheckIn)
                set("access_code", data.accessCode)
                set("image_url", data.imageUrl)
                set("registration_deadline", data.registrationDeadline)
                set("check_in_starts_before", data.checkInStartsBefore)
                set("prizes", data.prizes)
                set("schedule", data.schedule)
                set("stage_match_formats", data.stageMatchFormats)
            }) {
                filter { eq("id", tournamentId) }
            }
    }

    suspend fun deleteTournament(tournamentId: String) {
        client.from("tournaments")
            .delete { filter { eq("id", tournamentId) } }
    }

    suspend fun updateTournamentStatus(tournamentId: String, status: String) {
        client.from("tournaments")
            .update({ set("status", status) })
            { filter { eq("id", tournamentId) } }
    }

    // ── PARTICIPANTS ──

    suspend fun getParticipants(tournamentId: String): List<ParticipantDto> {
        return client.from("tournament_participants")
            .select(Columns.raw("*, profiles(id, name, email, avatar_url), teams(id, name)"))
            { filter { eq("tournament_id", tournamentId) } }
            .decodeList<ParticipantDto>()
    }

    suspend fun approveParticipant(tournamentId: String, athleteId: String) {
        client.from("tournament_participants")
            .update({ set("status", "confirmed") }) {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("athlete_id", athleteId)
                }
            }
    }

    suspend fun declineParticipant(tournamentId: String, athleteId: String) {
        client.from("tournament_participants")
            .update({ set("status", "cancelled") }) {
                filter {
                    eq("tournament_id", tournamentId)
                    eq("athlete_id", athleteId)
                }
            }
    }

    // ── RESULTS ──

    suspend fun getResults(tournamentId: String): List<ResultDto> {
        return client.from("tournament_results")
            .select(Columns.raw("*, profiles(id, name, avatar_url)")) {
                filter { eq("tournament_id", tournamentId) }
                order("position", Order.ASCENDING)
            }
            .decodeList<ResultDto>()
    }

    suspend fun saveResults(results: List<ResultInsertDto>) {
        if (results.isNotEmpty()) {
            client.from("tournament_results").upsert(results)
        }
    }

    // ── INVITE CODES ──

    suspend fun getInviteCodes(tournamentId: String): List<InviteCodeDto> {
        return client.from("tournament_invite_codes")
            .select { filter { eq("tournament_id", tournamentId) } }
            .decodeList<InviteCodeDto>()
    }

    suspend fun createInviteCode(data: InviteCodeInsertDto): String {
        val result = client.from("tournament_invite_codes")
            .insert(data) { select() }
            .decodeSingle<InviteCodeDto>()
        return result.id ?: ""
    }

    suspend fun deactivateInviteCode(codeId: String) {
        client.from("tournament_invite_codes")
            .update({ set("is_active", false) })
            { filter { eq("id", codeId) } }
    }

    suspend fun sendInvite(data: TournamentInviteInsertDto) {
        client.from("tournament_invites").insert(data)
    }

    // ── BRACKET ──

    suspend fun getBracket(tournamentId: String): List<BracketMatchDto> {
        return client.from("bracket_matches")
            .select { filter { eq("tournament_id", tournamentId) } }
            .decodeList<BracketMatchDto>()
    }

    suspend fun saveBracketMatches(tournamentId: String, matches: List<BracketMatchInsertDto>) {
        // Delete existing bracket
        client.from("bracket_matches")
            .delete { filter { eq("tournament_id", tournamentId) } }
        // Insert new matches
        if (matches.isNotEmpty()) {
            client.from("bracket_matches").insert(matches)
        }
    }

    suspend fun saveGroups(tournamentId: String, groups: List<TournamentGroupInsertDto>) {
        // Delete existing groups
        client.from("tournament_groups")
            .delete { filter { eq("tournament_id", tournamentId) } }
        if (groups.isNotEmpty()) {
            client.from("tournament_groups").insert(groups)
        }
    }

    suspend fun setMatchParticipant(matchId: String, slot: Int, participantId: String) {
        val field = if (slot == 1) "participant1_id" else "participant2_id"
        client.from("bracket_matches")
            .update({ set(field, participantId) }) {
                filter { eq("id", matchId) }
            }
    }

    suspend fun getGroups(tournamentId: String): List<TournamentGroupDto> {
        return client.from("tournament_groups")
            .select { filter { eq("tournament_id", tournamentId) } }
            .decodeList<TournamentGroupDto>()
    }

    suspend fun updateMatch(matchId: String, data: MatchResultUpdateDto) {
        client.from("bracket_matches")
            .update({
                set("participant1_score", data.participant1Score)
                set("participant2_score", data.participant2Score)
                data.games?.let { set("games", it) }
                data.winnerId?.let { set("winner_id", it) }
                data.loserId?.let { set("loser_id", it) }
                set("status", data.status)
            }) {
                filter { eq("id", matchId) }
            }
    }

    // ── LOCATIONS ──

    suspend fun getMyLocations(userId: String): List<LocationDto> {
        return client.from("locations")
            .select { filter { eq("owner_id", userId) } }
            .decodeList<LocationDto>()
    }

    suspend fun getLocationDetail(locationId: String): LocationDto {
        return client.from("locations")
            .select { filter { eq("id", locationId) } }
            .decodeSingle<LocationDto>()
    }

    suspend fun createLocation(data: LocationInsertDto): String {
        val result = client.from("locations")
            .insert(data) { select() }
            .decodeSingle<LocationDto>()
        return result.id ?: ""
    }

    suspend fun updateLocation(locationId: String, data: LocationInsertDto) {
        client.from("locations")
            .update({
                set("name", data.name)
                set("type", data.type)
                set("address", data.address)
                set("city", data.city)
                set("capacity", data.capacity)
                set("facilities", data.facilities)
                set("description", data.description)
                set("phone", data.phone)
                set("email", data.email)
                set("website", data.website)
            }) {
                filter { eq("id", locationId) }
            }
    }

    suspend fun deleteLocation(locationId: String) {
        client.from("locations")
            .delete { filter { eq("id", locationId) } }
    }

    // ── SPORTS ──

    suspend fun getSports(): List<SportDto> {
        return client.from("sports")
            .select { filter { eq("is_active", true) } }
            .decodeList<SportDto>()
    }

    suspend fun getMySports(userId: String): List<UserSportDto> {
        return client.from("user_sports")
            .select(Columns.raw("*, sports(*)"))
            { filter { eq("user_id", userId) } }
            .decodeList<UserSportDto>()
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

    // ── PROFILE ──

    suspend fun getProfile(userId: String): ProfileDto {
        return client.from("profiles")
            .select(Columns.raw("*, roles!primary_role_id(id, name)"))
            { filter { eq("id", userId) } }
            .decodeSingle<ProfileDto>()
    }

    suspend fun updateProfile(userId: String, data: ProfileUpdateDto) {
        client.from("profiles")
            .update({
                data.name?.let { set("name", it) }
                data.phone?.let { set("phone", it) }
                data.city?.let { set("city", it) }
                data.bio?.let { set("bio", it) }
                data.avatarUrl?.let { set("avatar_url", it) }
            }) {
                filter { eq("id", userId) }
            }
    }

    // ── REFEREES ──

    suspend fun getReferees(tournamentId: String): List<RefereeAssignmentDto> {
        return client.from("tournament_referees")
            .select(Columns.raw("*, profiles(name)"))
            { filter { eq("tournament_id", tournamentId) } }
            .decodeList<RefereeAssignmentDto>()
    }

    // ── STORAGE ──

    suspend fun uploadTournamentImage(tournamentId: String, imageBytes: ByteArray): String {
        val bucket = client.storage.from("tournament-images")
        bucket.upload("$tournamentId/cover.jpg", imageBytes) { upsert = true }
        return bucket.publicUrl("$tournamentId/cover.jpg")
    }

    suspend fun uploadLocationImage(locationId: String, imageBytes: ByteArray): String {
        val bucket = client.storage.from("location-images")
        bucket.upload("$locationId/main.jpg", imageBytes) { upsert = true }
        return bucket.publicUrl("$locationId/main.jpg")
    }
}
