package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleDto(
    val id: String,
    @SerialName("author_id") val authorId: String? = null,
    val title: String,
    val content: String? = null,
    val excerpt: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("sport_id") val sportId: String? = null,
    @SerialName("tournament_id") val tournamentId: String? = null,
    val status: String? = "draft",
    val category: String? = null,
    val tags: List<String>? = null,
    val views: Int = 0,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // JOINs
    val profiles: ProfileMinimalDto? = null,
    val sports: SportDto? = null
)

@Serializable
data class ArticleStatsDto(
    val total: Int = 0,
    val published: Int = 0,
    val drafts: Int = 0,
    @SerialName("total_views") val totalViews: Int = 0
)
