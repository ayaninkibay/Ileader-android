package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.util.AppLogger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class FamilyRepository {
    private val client = SupabaseModule.client

    /**
     * Get all family links where user is parent or child.
     */
    suspend fun getFamilyLinks(userId: String): List<FamilyLinkDto> {
        return try {
            val asParent = client.from("family_links")
                .select(Columns.raw("*, child:profiles!child_id(id, name, avatar_url, email, birth_date)"))
                { filter { eq("parent_id", userId) } }
                .decodeList<FamilyLinkDto>()

            val asChild = client.from("family_links")
                .select(Columns.raw("*, parent:profiles!parent_id(id, name, avatar_url, email)"))
                { filter { eq("child_id", userId) } }
                .decodeList<FamilyLinkDto>()

            asParent + asChild
        } catch (e: Exception) {
            AppLogger.w("FamilyRepo.getFamilyLinks: ${e.message}")
            emptyList()
        }
    }

    /**
     * Find a child profile by email.
     */
    suspend fun findChildByEmail(email: String): ProfileMinimalDto? {
        return try {
            client.from("profiles")
                .select(Columns.raw("id, name, avatar_url, email, birth_date"))
                { filter { eq("email", email) } }
                .decodeList<ProfileMinimalDto>()
                .firstOrNull()
        } catch (e: Exception) {
            AppLogger.w("FamilyRepo.findChildByEmail: ${e.message}")
            null
        }
    }

    /**
     * Create a family link (parent → child).
     */
    suspend fun createFamilyLink(parentId: String, childId: String) {
        client.from("family_links")
            .insert(FamilyLinkInsertDto(parentId = parentId, childId = childId))
    }

    /**
     * Confirm a pending family link (child confirms).
     */
    suspend fun confirmFamilyLink(linkId: String) {
        client.from("family_links")
            .update(mapOf(
                "status" to "active",
                "confirmed_at" to java.time.Instant.now().toString()
            )) {
                filter { eq("id", linkId) }
            }
    }

    /**
     * Remove a family link.
     */
    suspend fun removeFamilyLink(linkId: String) {
        client.from("family_links")
            .update(mapOf("status" to "removed")) {
                filter { eq("id", linkId) }
            }
    }

    /**
     * Get pending parental approvals for parent.
     */
    suspend fun getPendingApprovals(parentId: String): List<ParentalApprovalDto> {
        return try {
            // Get family link IDs where user is parent
            val linkIds = client.from("family_links")
                .select(Columns.raw("id"))
                {
                    filter {
                        eq("parent_id", parentId)
                        eq("status", "active")
                    }
                }
                .decodeList<IdOnlyDto>()
                .map { it.id }

            if (linkIds.isEmpty()) return emptyList()

            client.from("parental_approvals")
                .select(Columns.raw("*, family_links(child_id, parent_id, child:profiles!child_id(id, name))"))
                {
                    filter {
                        isIn("family_link_id", linkIds)
                        eq("status", "pending")
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<ParentalApprovalDto>()
        } catch (e: Exception) {
            AppLogger.w("FamilyRepo.getPendingApprovals: ${e.message}")
            emptyList()
        }
    }

    /**
     * Respond to a parental approval (approve/reject).
     */
    suspend fun respondToApproval(approvalId: String, approved: Boolean, comment: String? = null) {
        val data = mutableMapOf<String, String>(
            "status" to if (approved) "approved" else "rejected",
            "responded_at" to java.time.Instant.now().toString()
        )
        if (comment != null) data["parent_comment"] = comment

        client.from("parental_approvals")
            .update(data) { filter { eq("id", approvalId) } }
    }
}
