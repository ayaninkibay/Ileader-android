package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseDto(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("sport_id") val sportId: String? = null,
    @SerialName("author_id") val authorId: String? = null,
    @SerialName("is_free") val isFree: Boolean = true,
    val price: Double? = null,
    val currency: String? = "KZT",
    val status: String? = "draft",
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // JOINs
    val profiles: CourseAuthorDto? = null,
    val sports: CourseSportDto? = null
)

@Serializable
data class CourseAuthorDto(
    val name: String? = null
)

@Serializable
data class CourseSportDto(
    val name: String? = null
)

@Serializable
data class CourseLessonDto(
    val id: String,
    @SerialName("course_id") val courseId: String,
    val title: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("text_content") val textContent: String? = null,
    @SerialName("video_url") val videoUrl: String? = null,
    @SerialName("audio_url") val audioUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int? = null,
    @SerialName("is_free_preview") val isFreePreview: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class UserCourseDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("course_id") val courseId: String,
    @SerialName("granted_by") val grantedBy: String? = null,
    @SerialName("granted_at") val grantedAt: String? = null
)

@Serializable
data class CourseLessonCountDto(
    @SerialName("course_id") val courseId: String
)

@Serializable
data class UserCourseCountDto(
    @SerialName("course_id") val courseId: String
)
