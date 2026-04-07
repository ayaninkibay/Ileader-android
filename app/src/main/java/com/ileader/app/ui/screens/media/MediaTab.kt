package com.ileader.app.ui.screens.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ileader.app.data.models.User
import com.ileader.app.ui.screens.detail.PublicProfileScreen
import com.ileader.app.ui.screens.detail.TeamDetailScreen
import com.ileader.app.ui.screens.detail.TournamentDetailScreen

sealed class MediaNavState {
    data object Accreditations : MediaNavState()
    data class TournamentDetail(val id: String) : MediaNavState()
    data object Articles : MediaNavState()
    data class ArticleEditor(val articleId: String? = null) : MediaNavState()
    data object Interviews : MediaNavState()
    data class InterviewEditor(val interviewId: String? = null) : MediaNavState()
    data class PublicProfile(val id: String) : MediaNavState()
    data class TeamDetail(val id: String) : MediaNavState()
}

@Composable
fun MediaTab(user: User) {
    var navState by remember { mutableStateOf<MediaNavState>(MediaNavState.Accreditations) }

    when (val state = navState) {
        is MediaNavState.Accreditations -> {
            MediaAccreditationsScreen(
                user = user,
                onTournamentClick = { id -> navState = MediaNavState.TournamentDetail(id) },
                onInterviewsClick = { navState = MediaNavState.Interviews }
            )
        }
        is MediaNavState.TournamentDetail -> {
            TournamentDetailScreen(
                tournamentId = state.id,
                user = user,
                onBack = { navState = MediaNavState.Accreditations },
                onProfileClick = { navState = MediaNavState.PublicProfile(it) },
                onTeamClick = { navState = MediaNavState.TeamDetail(it) }
            )
        }
        is MediaNavState.PublicProfile -> {
            PublicProfileScreen(
                userId = state.id,
                onBack = { navState = MediaNavState.Accreditations }
            )
        }
        is MediaNavState.TeamDetail -> {
            TeamDetailScreen(
                teamId = state.id,
                onBack = { navState = MediaNavState.Accreditations }
            )
        }
        is MediaNavState.Articles -> {
            MediaArticlesScreen(
                user = user,
                onBack = { navState = MediaNavState.Accreditations },
                onArticleClick = { id -> navState = MediaNavState.ArticleEditor(id) },
                onCreateArticle = { navState = MediaNavState.ArticleEditor(null) }
            )
        }
        is MediaNavState.ArticleEditor -> {
            MediaArticleEditorScreen(
                userId = user.id,
                articleId = state.articleId,
                onBack = { navState = MediaNavState.Articles }
            )
        }
        is MediaNavState.Interviews -> {
            MediaInterviewsScreen(
                user = user,
                onBack = { navState = MediaNavState.Accreditations },
                onInterviewClick = { id -> navState = MediaNavState.InterviewEditor(id) },
                onCreateInterview = { navState = MediaNavState.InterviewEditor(null) }
            )
        }
        is MediaNavState.InterviewEditor -> {
            MediaInterviewEditorScreen(
                userId = user.id,
                interviewId = state.interviewId,
                onBack = { navState = MediaNavState.Interviews }
            )
        }
    }
}
