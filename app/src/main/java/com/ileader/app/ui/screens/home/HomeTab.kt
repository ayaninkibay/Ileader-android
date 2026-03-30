package com.ileader.app.ui.screens.home

import androidx.compose.runtime.*
import com.ileader.app.data.models.User
import com.ileader.app.ui.screens.detail.ArticleDetailScreen
import com.ileader.app.ui.screens.detail.PublicProfileScreen
import com.ileader.app.ui.screens.detail.TournamentDetailScreen

sealed class HomeNavState {
    object Home : HomeNavState()
    data class ArticleDetail(val id: String) : HomeNavState()
    data class TournamentDetail(val id: String) : HomeNavState()
    data class PublicProfile(val id: String) : HomeNavState()
}

@Composable
fun HomeTab(user: User) {
    var navState by remember { mutableStateOf<HomeNavState>(HomeNavState.Home) }

    when (val state = navState) {
        is HomeNavState.Home -> HomeScreen(
            onArticleClick = { navState = HomeNavState.ArticleDetail(it) },
            onTournamentClick = { navState = HomeNavState.TournamentDetail(it) },
            onProfileClick = { navState = HomeNavState.PublicProfile(it) }
        )
        is HomeNavState.ArticleDetail -> ArticleDetailScreen(
            articleId = state.id,
            onBack = { navState = HomeNavState.Home }
        )
        is HomeNavState.TournamentDetail -> TournamentDetailScreen(
            tournamentId = state.id,
            user = user,
            onBack = { navState = HomeNavState.Home }
        )
        is HomeNavState.PublicProfile -> PublicProfileScreen(
            userId = state.id,
            onBack = { navState = HomeNavState.Home }
        )
    }
}
