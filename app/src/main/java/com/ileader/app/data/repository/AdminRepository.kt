package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Репозиторий для admin-функций: пользователи, верификации, заявки на спорт, настройки.
 */
class AdminRepository {

    private val client = SupabaseModule.client

    // ── DASHBOARD STATS ──

    suspend fun getStats(): AdminStatsDto {
        val totalUsers = client.from("profiles")
            .select(Columns.raw("id"))
            .decodeList<IdOnlyDto>().size

        val totalTournaments = client.from("tournaments")
            .select(Columns.raw("id"))
            .decodeList<IdOnlyDto>().size

        val activeTournaments = client.from("tournaments")
            .select(Columns.raw("id")) {
                filter {
                    neq("status", "completed")
                    neq("status", "cancelled")
                    neq("status", "draft")
                }
            }
            .decodeList<IdOnlyDto>().size

        val pendingVerifications = client.from("profiles")
            .select(Columns.raw("id")) {
                filter { eq("verification", "pending") }
            }
            .decodeList<IdOnlyDto>().size

        return AdminStatsDto(
            totalUsers = totalUsers,
            totalTournaments = totalTournaments,
            activeTournaments = activeTournaments,
            pendingVerifications = pendingVerifications
        )
    }

    // ── USERS ──

    /**
     * Получить пользователей с опциональным фильтром по роли (role name) и поиском по имени.
     */
    suspend fun getAllUsers(roleName: String? = null, query: String = ""): List<AdminUserDto> {
        val roleId: String? = if (!roleName.isNullOrBlank()) {
            client.from("roles")
                .select(Columns.raw("id")) { filter { eq("name", roleName) } }
                .decodeSingleOrNull<IdOnlyDto>()?.id
        } else null

        return client.from("profiles")
            .select(Columns.raw("*, roles!primary_role_id(id, name)")) {
                filter {
                    if (roleId != null) eq("primary_role_id", roleId)
                    if (query.isNotBlank()) ilike("name", "%$query%")
                }
                order("created_at", Order.DESCENDING)
                limit(100)
            }
            .decodeList<AdminUserDto>()
    }

    suspend fun updateUserRole(userId: String, newRoleName: String) {
        val role = client.from("roles")
            .select(Columns.raw("id")) { filter { eq("name", newRoleName) } }
            .decodeSingleOrNull<IdOnlyDto>() ?: return

        client.from("profiles")
            .update({
                set("primary_role_id", role.id)
                set("role_ids", listOf(role.id))
            }) {
                filter { eq("id", userId) }
            }
    }

    suspend fun blockUser(userId: String) {
        client.from("profiles")
            .update({ set("status", "blocked") }) { filter { eq("id", userId) } }
    }

    suspend fun unblockUser(userId: String) {
        client.from("profiles")
            .update({ set("status", "active") }) { filter { eq("id", userId) } }
    }

    // ── VERIFICATIONS ──

    suspend fun getPendingVerifications(): List<AdminUserDto> {
        return client.from("profiles")
            .select(Columns.raw("*, roles!primary_role_id(id, name)")) {
                filter { eq("verification", "pending") }
                order("updated_at", Order.DESCENDING)
                limit(100)
            }
            .decodeList<AdminUserDto>()
    }

    suspend fun approveVerification(userId: String) {
        client.from("profiles")
            .update({ set("verification", "verified") }) { filter { eq("id", userId) } }
    }

    suspend fun rejectVerification(userId: String) {
        client.from("profiles")
            .update({ set("verification", "rejected") }) { filter { eq("id", userId) } }
    }

    // ── SPORT REQUESTS ──

    suspend fun getSportRequests(): List<SportRequestDto> {
        return client.from("sport_requests")
            .select(Columns.raw("*, profiles!requested_by(id, name, avatar_url, email)")) {
                order("created_at", Order.DESCENDING)
                limit(100)
            }
            .decodeList<SportRequestDto>()
    }

    suspend fun approveSportRequest(requestId: String) {
        // Получаем заявку
        val request = client.from("sport_requests")
            .select { filter { eq("id", requestId) } }
            .decodeSingle<SportRequestDto>()

        // Создаём вид спорта
        val slug = request.name.lowercase()
            .replace(Regex("[^a-z0-9а-я]+"), "-")
            .trim('-')
        client.from("sports").insert(
            buildJsonObject {
                put("name", request.name)
                put("slug", slug)
                request.description?.let { put("description", it) }
                put("is_active", true)
            }
        )

        // Помечаем заявку как одобренную
        client.from("sport_requests")
            .update({ set("status", "approved") }) { filter { eq("id", requestId) } }
    }

    suspend fun rejectSportRequest(requestId: String) {
        client.from("sport_requests")
            .update({ set("status", "rejected") }) { filter { eq("id", requestId) } }
    }

    // ── PLATFORM SETTINGS ──

    suspend fun getPlatformSettings(): List<PlatformSettingDto> {
        return client.from("platform_settings")
            .select { order("key", Order.ASCENDING) }
            .decodeList<PlatformSettingDto>()
    }

    suspend fun updatePlatformSetting(key: String, value: JsonElement) {
        client.from("platform_settings")
            .update({ set("value", value) }) { filter { eq("key", key) } }
    }

    suspend fun updatePlatformSettingString(key: String, value: String) {
        updatePlatformSetting(key, JsonPrimitive(value))
    }

    // ── VERIFICATION REQUEST (user-side) ──

    suspend fun submitVerificationRequest(userId: String, description: String?) {
        val payload = if (!description.isNullOrBlank()) {
            VerificationRequestInsertDto(
                verification = "pending",
                roleData = buildJsonObject {
                    put("verification_request", buildJsonObject {
                        put("description", description)
                    })
                }
            )
        } else {
            VerificationRequestInsertDto(verification = "pending")
        }
        client.from("profiles")
            .update({
                set("verification", payload.verification)
                payload.roleData?.let { set("role_data", it) }
            }) {
                filter { eq("id", userId) }
            }
    }
}
