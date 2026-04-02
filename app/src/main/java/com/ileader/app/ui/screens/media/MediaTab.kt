package com.ileader.app.ui.screens.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ileader.app.data.models.User
import com.ileader.app.ui.screens.detail.TournamentDetailScreen

sealed class MediaNavState {
    data object Accreditations : MediaNavState()
    data class TournamentDetail(val id: String) : MediaNavState()
    data object Articles : MediaNavState()
    data class ArticleEditor(val articleId: String? = null) : MediaNavState()
}

@Composable
fun MediaTab(user: User) {
    var navState by remember { mutableStateOf<MediaNavState>(MediaNavState.Accreditations) }

    when (val state = navState) {
        is MediaNavState.Accreditations -> {
            MediaAccreditationsScreen(
                user = user,
                onTournamentClick = { id -> navState = MediaNavState.TournamentDetail(id) }
            )
        }
        is MediaNavState.TournamentDetail -> {
            TournamentDetailScreen(
                tournamentId = state.id,
                user = user,
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
    }
}
