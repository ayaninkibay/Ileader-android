package com.ileader.app.ui.screens.sport

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ileader.app.data.models.User
import com.ileader.app.ui.screens.detail.ArticleDetailScreen
import com.ileader.app.ui.screens.detail.PublicProfileScreen
import com.ileader.app.ui.screens.detail.TeamDetailScreen
import com.ileader.app.ui.screens.detail.TournamentDetailScreen

sealed class SportNavState {
    data object Search : SportNavState()
    data class TournamentDetail(val id: String) : SportNavState()
    data class ArticleDetail(val id: String) : SportNavState()
    data class PublicProfile(val id: String) : SportNavState()
    data class LeagueDetail(val name: String, val sportName: String, val imageUrl: String?) : SportNavState()
    data class TeamDetail(val name: String, val sportName: String, val city: String) : SportNavState()
}

@Composable
fun SportTab(user: User) {
    var navState: SportNavState by remember {
        mutableStateOf(SportNavState.Search)
    }

    AnimatedContent(
        targetState = navState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "sport_tab_nav"
    ) { currentState ->
        when (currentState) {
            is SportNavState.Search -> SportScreen(
                user = user,
                onTournamentClick = { navState = SportNavState.TournamentDetail(it) },
                onArticleClick = { navState = SportNavState.ArticleDetail(it) },
                onProfileClick = { navState = SportNavState.PublicProfile(it) },
                onLeagueClick = { name, sport, img -> navState = SportNavState.LeagueDetail(name, sport, img) },
                onTeamClick = { name, sport, city -> navState = SportNavState.TeamDetail(name, sport, city) }
            )
            is SportNavState.TournamentDetail -> TournamentDetailScreen(
                tournamentId = currentState.id,
                user = user,
                onBack = { navState = SportNavState.Search }
            )
            is SportNavState.ArticleDetail -> ArticleDetailScreen(
                articleId = currentState.id,
                onBack = { navState = SportNavState.Search }
            )
            is SportNavState.PublicProfile -> PublicProfileScreen(
                userId = currentState.id,
                onBack = { navState = SportNavState.Search }
            )
            is SportNavState.LeagueDetail -> LeagueDetailScreen(
                leagueName = currentState.name,
                sportName = currentState.sportName,
                imageUrl = currentState.imageUrl,
                onBack = { navState = SportNavState.Search }
            )
            is SportNavState.TeamDetail -> TeamDetailScreen(
                teamName = currentState.name,
                sportName = currentState.sportName,
                city = currentState.city,
                onBack = { navState = SportNavState.Search }
            )
        }
    }
}
