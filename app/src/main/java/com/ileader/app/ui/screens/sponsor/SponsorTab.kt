package com.ileader.app.ui.screens.sponsor

import androidx.compose.runtime.*
import com.ileader.app.data.models.User
import com.ileader.app.ui.screens.detail.PublicProfileScreen
import com.ileader.app.ui.screens.detail.TeamDetailScreen
import com.ileader.app.ui.screens.detail.TournamentDetailScreen

sealed class SponsorNavState {
    data object List : SponsorNavState()
    data object Search : SponsorNavState()
    data class TournamentDetail(val id: String) : SponsorNavState()
    data class PublicProfile(val id: String) : SponsorNavState()
    data class TeamDetail(val id: String) : SponsorNavState()
}

@Composable
fun SponsorTab(user: User) {
    var navState by remember { mutableStateOf<SponsorNavState>(SponsorNavState.List) }

    when (val state = navState) {
        is SponsorNavState.List -> SponsorshipsScreen(
            user = user,
            onBack = { /* root tab */ },
            onTournamentClick = { navState = SponsorNavState.TournamentDetail(it) },
            onSearchClick = { navState = SponsorNavState.Search }
        )
        is SponsorNavState.Search -> SponsorTournamentSearchScreen(
            user = user,
            onBack = { navState = SponsorNavState.List },
            onTournamentClick = { navState = SponsorNavState.TournamentDetail(it) },
            onCreated = { navState = SponsorNavState.List }
        )
        is SponsorNavState.TournamentDetail -> TournamentDetailScreen(
            tournamentId = state.id,
            user = user,
            onBack = { navState = SponsorNavState.List },
            onProfileClick = { navState = SponsorNavState.PublicProfile(it) },
            onTeamClick = { navState = SponsorNavState.TeamDetail(it) }
        )
        is SponsorNavState.PublicProfile -> PublicProfileScreen(
            userId = state.id,
            onBack = { navState = SponsorNavState.List }
        )
        is SponsorNavState.TeamDetail -> TeamDetailScreen(
            teamId = state.id,
            onBack = { navState = SponsorNavState.List }
        )
    }
}
