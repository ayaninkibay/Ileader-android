package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.util.MemoryCache
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class CourseRepository {
    private val client = SupabaseModule.client

    // ══════════════════════════════════════════════════════════
    // COURSES LIST
    // ══════════════════════════════════════════════════════════

    suspend fun getPublishedCourses(sportId: String? = null): List<CourseDto> =
        MemoryCache.cached("courses:published:${sportId ?: "all"}", ttlMs = 900_000L) {
            client.from("courses")
                .select(Columns.raw("id, title, description, sport_id, author_id, is_free, price, currency, status, cover_url, created_at, updated_at, profiles(name), sports(name)")) {
                    filter {
                        eq("status", "published")
                        if (sportId != null) {
                            eq("sport_id", sportId)
                        }
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<CourseDto>()
        }

    suspend fun getLessonCounts(courseIds: List<String>): Map<String, Int> {
        // Caller-supplied id list — skip caching (composition varies per call).
        if (courseIds.isEmpty()) return emptyMap()
        val lessons = client.from("course_lessons")
            .select(Columns.raw("course_id")) {
                filter { isIn("course_id", courseIds) }
            }
            .decodeList<CourseLessonCountDto>()

        return lessons.groupBy { it.courseId }.mapValues { it.value.size }
    }

    suspend fun getStudentCounts(courseIds: List<String>): Map<String, Int> {
        if (courseIds.isEmpty()) return emptyMap()
        val students = client.from("user_courses")
            .select(Columns.raw("course_id")) {
                filter { isIn("course_id", courseIds) }
            }
            .decodeList<UserCourseCountDto>()

        return students.groupBy { it.courseId }.mapValues { it.value.size }
    }

    // ══════════════════════════════════════════════════════════
    // COURSE DETAIL
    // ══════════════════════════════════════════════════════════

    suspend fun getCourseDetail(courseId: String): CourseDto =
        MemoryCache.cached("course:$courseId", ttlMs = 900_000L) {
            client.from("courses")
                .select(Columns.raw("id, title, description, sport_id, author_id, is_free, price, currency, status, cover_url, created_at, updated_at, profiles(name), sports(name)")) {
                    filter { eq("id", courseId) }
                }
                .decodeSingle<CourseDto>()
        }

    suspend fun getCourseLessons(courseId: String): List<CourseLessonDto> =
        MemoryCache.cached("course:lessons:$courseId", ttlMs = 900_000L) {
            client.from("course_lessons")
                .select(Columns.raw("id, course_id, title, sort_order, text_content, video_url, audio_url, image_url, duration_minutes, is_free_preview, created_at")) {
                    filter { eq("course_id", courseId) }
                    order("sort_order", Order.ASCENDING)
                }
                .decodeList<CourseLessonDto>()
        }

    // ══════════════════════════════════════════════════════════
    // USER COURSES & ACCESS
    // ══════════════════════════════════════════════════════════

    suspend fun getUserCourses(userId: String): List<UserCourseDto> =
        MemoryCache.cached("user:courses:$userId", ttlMs = 600_000L) {
            client.from("user_courses")
                .select(Columns.raw("id, user_id, course_id, granted_by, granted_at")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<UserCourseDto>()
        }

    suspend fun checkCourseAccess(courseId: String, userId: String): Boolean {
        val cached = MemoryCache.cached("course:access:$courseId:$userId", ttlMs = 600_000L) {
            // Check if course is free
            val course = getCourseDetail(courseId)
            if (course.isFree) return@cached listOf(true)
            if (course.authorId == userId) return@cached listOf(true)

            // Check user_courses
            val access = client.from("user_courses")
                .select(Columns.raw("id")) {
                    filter {
                        eq("course_id", courseId)
                        eq("user_id", userId)
                    }
                }
                .decodeList<IdOnlyDto>()

            listOf(access.isNotEmpty())
        }
        return cached.first()
    }

    suspend fun enrollInFreeCourse(userId: String, courseId: String) {
        client.from("user_courses")
            .insert(
                mapOf(
                    "user_id" to userId,
                    "course_id" to courseId,
                    "granted_by" to userId
                )
            )
        MemoryCache.invalidate("user:courses:$userId")
        MemoryCache.invalidate("course:access:$courseId:$userId")
    }

    suspend fun getSports(): List<SportDto> =
        MemoryCache.cached("sports", ttlMs = 1_800_000L) {
            client.from("sports")
                .select { filter { eq("is_active", true) } }
                .decodeList<SportDto>()
        }

    // ══════════════════════════════════════════════════════════
    // PAID COURSE ACCESS REQUESTS
    // ══════════════════════════════════════════════════════════

    /** Returns true if user already has a pending request for this course. */
    suspend fun hasPendingAccessRequest(courseId: String, userId: String): Boolean {
        val rows = client.from("course_access_requests")
            .select(Columns.raw("id")) {
                filter {
                    eq("course_id", courseId)
                    eq("user_id", userId)
                    eq("status", "pending")
                }
            }
            .decodeList<IdOnlyDto>()
        return rows.isNotEmpty()
    }

    /**
     * Creates a course access request. The actual access is granted later by
     * a content manager via [grantCourseAccess]-style flow on the web admin.
     * Mirrors web `createCourseAccessRequest`: `whatsapp` column hosts handle
     * for any messenger; `messenger_type` distinguishes which channel was picked.
     */
    suspend fun createAccessRequest(
        courseId: String,
        userId: String,
        userName: String,
        userEmail: String?,
        userPhone: String?,
        messengerType: String,
        messengerHandle: String,
        message: String?,
    ) {
        if (hasPendingAccessRequest(courseId, userId)) {
            throw IllegalStateException("У вас уже есть активная заявка на этот курс")
        }
        client.from("course_access_requests")
            .insert(buildMap<String, String?> {
                put("course_id", courseId)
                put("user_id", userId)
                put("user_name", userName)
                put("user_email", userEmail ?: "")
                put("user_phone", userPhone)
                put("whatsapp", messengerHandle)
                put("messenger_type", messengerType)
                put("message", message)
                put("status", "pending")
            })
    }
}
