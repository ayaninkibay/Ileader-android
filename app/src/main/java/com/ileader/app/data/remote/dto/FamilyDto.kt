package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FamilyLinkDto(
    val id: String,
    @SerialName("parent_id") val parentId: String,
    @SerialName("child_id") val childId: String,
    val status: String = "pending",
    @SerialName("confirmation_text") val confirmationText: String? = null,
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    @SerialName("linked_at") val linkedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // JOINs — parent profile
    @SerialName("parent") val parent: ProfileMinimalDto? = null,
    // JOINs — child profile
    @SerialName("child") val child: FamilyChildDto? = null
)

@Serializable
data class FamilyChildDto(
    val id: String? = null,
    val name: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val email: String? = null,
    @SerialName("birth_date") val birthDate: String? = null
)

@Serializable
data class FamilyLinkInsertDto(
    @SerialName("parent_id") val parentId: String,
    @SerialName("child_id") val childId: String,
    val status: String = "pending"
)

@Serializable
data class ParentalApprovalDto(
    val id: String,
    @SerialName("family_link_id") val familyLinkId: String,
    @SerialName("action_type") val actionType: String,
    @SerialName("reference_id") val referenceId: String? = null,
    @SerialName("reference_name") val referenceName: String? = null,
    val status: String = "pending",
    @SerialName("parent_comment") val parentComment: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("responded_at") val respondedAt: String? = null,
    // JOINs
    @SerialName("family_links") val familyLink: FamilyLinkMinimalDto? = null
)

@Serializable
data class FamilyLinkMinimalDto(
    @SerialName("child_id") val childId: String? = null,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("child") val child: ProfileMinimalDto? = null
)
