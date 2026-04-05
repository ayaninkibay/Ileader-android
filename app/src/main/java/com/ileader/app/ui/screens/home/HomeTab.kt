package com.ileader.app.ui.screens.home

import androidx.compose.runtime.*
import com.ileader.app.data.models.User
import com.ileader.app.ui.screens.detail.ArticleDetailScreen
import com.ileader.app.ui.screens.detail.PublicProfileScreen
import com.ileader.app.ui.screens.detail.TournamentDetailScreen
import com.ileader.app.ui.screens.sport.RankingsScreen

sealed class HomeNavState {
    object Home : HomeNavState()
    data class ArticleDetail(val id: String) : HomeNavState()
    data class TournamentDetail(val id: String) : HomeNavState()
    data class PublicProfile(val id: String) : HomeNavState()
    object Rankings : HomeNavState()
    object Notifications : HomeNavState()
}

@Composable
fun HomeTab(user: User) {
    var navState by remember { mutableStateOf<HomeNavState>(HomeNavState.Home) }

    when (val state = navState) {
        is HomeNavState.Home -> HomeScreen(
            user = user,
            onArticleClick = { navState = HomeNavState.ArticleDetail(it) },
            onTournamentClick = { navState = HomeNavState.TournamentDetail(it) },
            onProfileClick = { navState = HomeNavState.PublicProfile(it) },
            onRankingsClick = { navState = HomeNavState.Rankings },
            onNotificationsClick = { navState = HomeNavState.Notifications },
            onAllNewsClick = { /* TODO: full news list */ },
            onAllTournamentsClick = { /* TODO: full tournaments list */ },
            onAllPeopleClick = { /* TODO: full people list */ }
        )
        is HomeNavState.ArticleDetail -> ArticleDetailScreen(
            articleId = state.id,
            onBack = { navState = HomeNavState.Home }
        )
        is HomeNavState.TournamentDetail -> TournamentDetailScreen(
            tournamentId = state.id,
            user = user,
            onBack = { navState = HomeNavState.Home },
            onProfileClick = { navState = HomeNavState.PublicProfile(it) }
        )
        is HomeNavState.PublicProfile -> PublicProfileScreen(
            userId = state.id,
            onBack = { navState = HomeNavState.Home }
        )
        is HomeNavState.Notifications -> com.ileader.app.ui.screens.common.NotificationsScreen(
            user = user,
            onBack = { navState = HomeNavState.Home }
        )
        is HomeNavState.Rankings -> RankingsScreen(
            userId = user.id,
            onBack = { navState = HomeNavState.Home },
            onProfileClick = { navState = HomeNavState.PublicProfile(it) }
        )
    }
}
