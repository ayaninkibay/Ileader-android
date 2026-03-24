package com.ileader.app.data

import android.content.Intent
import android.net.Uri

/**
 * Парсит deep link URI и определяет навигацию.
 *
 * Поддерживаемые deep links:
 * - ileader.kz/tournaments/{id} → TournamentDetailScreen
 * - ileader.kz/community/athlete/{id} → AthleteProfileScreen
 * - ileader.kz/community/team/{id} → TeamProfileScreen
 */
data class DeepLinkTarget(
    val type: DeepLinkType,
    val id: String
)

enum class DeepLinkType {
    TOURNAMENT,
    ATHLETE_PROFILE,
    TEAM_PROFILE
}

object DeepLinkHandler {

    fun parse(intent: Intent?): DeepLinkTarget? {
        val uri = intent?.data ?: return null
        return parseUri(uri)
    }

    fun parseUri(uri: Uri): DeepLinkTarget? {
        val host = uri.host ?: return null
        if (host != "ileader.kz" && host != "www.ileader.kz") return null

        val segments = uri.pathSegments ?: return null

        return when {
            // /tournaments/{id}
            segments.size >= 2 && segments[0] == "tournaments" -> {
                DeepLinkTarget(DeepLinkType.TOURNAMENT, segments[1])
            }
            // /community/athlete/{id}
            segments.size >= 3 && segments[0] == "community" && segments[1] == "athlete" -> {
                DeepLinkTarget(DeepLinkType.ATHLETE_PROFILE, segments[2])
            }
            // /community/team/{id}
            segments.size >= 3 && segments[0] == "community" && segments[1] == "team" -> {
                DeepLinkTarget(DeepLinkType.TEAM_PROFILE, segments[2])
            }
            else -> null
        }
    }
}
