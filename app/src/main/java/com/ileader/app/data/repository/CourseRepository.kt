package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class CourseRepository {
    private val client = SupabaseModule.client

    // ══════════════════════════════════════════════════════════
    // COURSES LIST
    // ══════════════════════════════════════════════════════════

    suspend fun getPublishedCourses(sportId: String? = null): List<CourseDto> {
        val courses = client.from("courses")
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

        return courses
    }

    suspend fun getLessonCounts(courseIds: List<String>): Map<String, Int> {
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

    suspend fun getCourseDetail(courseId: String): CourseDto {
        return client.from("courses")
            .select(Columns.raw("id, title, description, sport_id, author_id, is_free, price, currency, status, cover_url, created_at, updated_at, profiles(name), sports(name)")) {
                filter { eq("id", courseId) }
            }
            .decodeSingle<CourseDto>()
    }

    suspend fun getCourseLessons(courseId: String): List<CourseLessonDto> {
        return client.from("course_lessons")
            .select(Columns.raw("id, course_id, title, sort_order, text_content, video_url, audio_url, image_url, duration_minutes, is_free_preview, created_at")) {
                filter { eq("course_id", courseId) }
                order("sort_order", Order.ASCENDING)
            }
            .decodeList<CourseLessonDto>()
    }

    // ══════════════════════════════════════════════════════════
    // USER COURSES & ACCESS
    // ══════════════════════════════════════════════════════════

    suspend fun getUserCourses(userId: String): List<UserCourseDto> {
        return client.from("user_courses")
            .select(Columns.raw("id, user_id, course_id, granted_by, granted_at")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<UserCourseDto>()
    }

    suspend fun checkCourseAccess(courseId: String, userId: String): Boolean {
        // Check if course is free
        val course = getCourseDetail(courseId)
        if (course.isFree) return true
        if (course.authorId == userId) return true

        // Check user_courses
        val access = client.from("user_courses")
            .select(Columns.raw("id")) {
                filter {
                    eq("course_id", courseId)
                    eq("user_id", userId)
                }
            }
            .decodeList<IdOnlyDto>()

        return access.isNotEmpty()
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
    }

    suspend fun getSports(): List<SportDto> {
        return client.from("sports")
            .select { filter { eq("is_active", true) } }
            .decodeList<SportDto>()
    }
}
