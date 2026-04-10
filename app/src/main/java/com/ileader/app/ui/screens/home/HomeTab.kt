package com.ileader.app.ui.screens.home

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.screens.detail.ArticleDetailScreen
import com.ileader.app.ui.screens.detail.AthleteProfilePage
import com.ileader.app.ui.screens.detail.PublicProfileScreen
import com.ileader.app.ui.screens.detail.RefereeProfilePage
import com.ileader.app.ui.screens.detail.TrainerProfilePage
import com.ileader.app.ui.screens.detail.TeamDetailScreen
import com.ileader.app.ui.screens.detail.TournamentDetailScreen
import com.ileader.app.ui.screens.admin.AdminSettingsScreen
import com.ileader.app.ui.screens.admin.AdminSportRequestsScreen
import com.ileader.app.ui.screens.admin.AdminUsersScreen
import com.ileader.app.ui.screens.admin.AdminVerificationsScreen
import com.ileader.app.ui.screens.chat.ChatScreen
import com.ileader.app.ui.screens.common.ManualCheckInScreen
import com.ileader.app.ui.screens.common.QrScannerScreen
import com.ileader.app.ui.screens.leagues.LeagueDetailScreen
import com.ileader.app.ui.screens.leagues.LeaguesListScreen
import com.ileader.app.ui.screens.location.LocationDetailScreen
import com.ileader.app.ui.screens.location.LocationReviewFormScreen
import com.ileader.app.ui.screens.mytournaments.HelperManagementScreen
import com.ileader.app.ui.screens.mytournaments.InviteCodesScreen
import com.ileader.app.ui.screens.mytournaments.RefereeManagementScreen
import com.ileader.app.ui.screens.mytournaments.TournamentEditScreen
import com.ileader.app.ui.screens.sport.RankingsScreen
import com.ileader.app.ui.viewmodels.StartConversationViewModel

sealed class HomeNavState {
    object Home : HomeNavState()
    data class ArticleDetail(val id: String) : HomeNavState()
    data class TournamentDetail(val id: String) : HomeNavState()
    data class PublicProfile(val id: String) : HomeNavState()
    data class AthleteProfile(val id: String) : HomeNavState()
    data class RefereeProfile(val id: String) : HomeNavState()
    data class TrainerProfile(val id: String) : HomeNavState()
    data class TeamDetail(val id: String) : HomeNavState()
    object Rankings : HomeNavState()
    object Notifications : HomeNavState()
    object AdminUsers : HomeNavState()
    object AdminVerifications : HomeNavState()
    object AdminSportRequests : HomeNavState()
    object AdminSettings : HomeNavState()
    object Leagues : HomeNavState()
    data class LeagueDetail(val id: String) : HomeNavState()
    data class LocationDetail(val id: String) : HomeNavState()
    data class LocationReview(val id: String) : HomeNavState()
    data class StartChat(val otherUserId: String) : HomeNavState()
    data class Chat(val conversationId: String, val otherName: String) : HomeNavState()
    // Organizer flow from a tournament opened from Home feed
    data class TournamentEdit(val id: String) : HomeNavState()
    data class QrScanner(val tournamentId: String, val tournamentName: String) : HomeNavState()
    data class ManualCheckIn(val tournamentId: String, val tournamentName: String) : HomeNavState()
    data class HelperManagement(val tournamentId: String, val tournamentName: String) : HomeNavState()
    data class RefereeManagement(val tournamentId: String, val tournamentName: String) : HomeNavState()
    data class InviteCodes(val tournamentId: String, val tournamentName: String) : HomeNavState()
}

@Composable
fun HomeTab(user: User, onNavigateToSport: () -> Unit = {}) {
    var navState by remember { mutableStateOf<HomeNavState>(HomeNavState.Home) }

    when (val state = navState) {
        is HomeNavState.Home -> HomeScreen(
            user = user,
            onArticleClick = { navState = HomeNavState.ArticleDetail(it) },
            onTournamentClick = { navState = HomeNavState.TournamentDetail(it) },
            onProfileClick = { navState = HomeNavState.PublicProfile(it) },
            onRankingsClick = { navState = HomeNavState.Rankings },
            onNotificationsClick = { navState = HomeNavState.Notifications },
            onAllNewsClick = onNavigateToSport,
            onAllTournamentsClick = onNavigateToSport,
            onAllPeopleClick = onNavigateToSport,
            onAdminUsersClick = { navState = HomeNavState.AdminUsers },
            onAdminVerificationsClick = { navState = HomeNavState.AdminVerifications },
            onAdminSportRequestsClick = { navState = HomeNavState.AdminSportRequests },
            onAdminSettingsClick = { navState = HomeNavState.AdminSettings },
            onLeaguesClick = { navState = HomeNavState.Leagues }
        )
        is HomeNavState.ArticleDetail -> ArticleDetailScreen(
            articleId = state.id,
            onBack = { navState = HomeNavState.Home }
        )
        is HomeNavState.TournamentDetail -> TournamentDetailScreen(
            tournamentId = state.id,
            user = user,
            onBack = { navState = HomeNavState.Home },
            onEditTournament = { id -> navState = HomeNavState.TournamentEdit(id) },
            onQrScan = { id, name -> navState = HomeNavState.QrScanner(id, name) },
            onManualCheckIn = { id, name -> navState = HomeNavState.ManualCheckIn(id, name) },
            onHelperManagement = { id, name -> navState = HomeNavState.HelperManagement(id, name) },
            onRefereeManagement = { id, name -> navState = HomeNavState.RefereeManagement(id, name) },
            onInviteCodes = { id, name -> navState = HomeNavState.InviteCodes(id, name) },
            onProfileClick = { navState = HomeNavState.PublicProfile(it) },
            onAthleteProfileClick = { navState = HomeNavState.AthleteProfile(it) },
            onRefereeProfileClick = { navState = HomeNavState.RefereeProfile(it) },
            onTrainerProfileClick = { navState = HomeNavState.TrainerProfile(it) },
            onTeamClick = { navState = HomeNavState.TeamDetail(it) }
        )
        is HomeNavState.PublicProfile -> PublicProfileScreen(
            userId = state.id,
            onBack = { navState = HomeNavState.Home },
            onStartChat = { otherId -> navState = HomeNavState.StartChat(otherId) }
        )
        is HomeNavState.Notifications -> com.ileader.app.ui.screens.common.NotificationsScreen(
            user = user,
            onBack = { navState = HomeNavState.Home }
        )
        is HomeNavState.AthleteProfile -> AthleteProfilePage(
            athleteId = state.id,
            onBack = { navState = HomeNavState.Home },
            onTournamentClick = { navState = HomeNavState.TournamentDetail(it) },
            onProfileClick = { navState = HomeNavState.PublicProfile(it) }
        )
        is HomeNavState.RefereeProfile -> RefereeProfilePage(
            refereeId = state.id,
            onBack = { navState = HomeNavState.Home },
            onTournamentClick = { navState = HomeNavState.TournamentDetail(it) },
            onProfileClick = { navState = HomeNavState.PublicProfile(it) }
        )
        is HomeNavState.TrainerProfile -> TrainerProfilePage(
            trainerId = state.id,
            onBack = { navState = HomeNavState.Home },
            onTournamentClick = { navState = HomeNavState.TournamentDetail(it) },
            onAthleteClick = { navState = HomeNavState.AthleteProfile(it) },
            onProfileClick = { navState = HomeNavState.PublicProfile(it) }
        )
        is HomeNavState.TeamDetail -> TeamDetailScreen(
            teamId = state.id,
            onBack = { navState = HomeNavState.Home }
        )
        is HomeNavState.Rankings -> RankingsScreen(
            userId = user.id,
            onBack = { navState = HomeNavState.Home },
            onProfileClick = { navState = HomeNavState.PublicProfile(it) }
        )
        is HomeNavState.AdminUsers -> AdminUsersScreen(onBack = { navState = HomeNavState.Home })
        is HomeNavState.AdminVerifications -> AdminVerificationsScreen(onBack = { navState = HomeNavState.Home })
        is HomeNavState.AdminSportRequests -> AdminSportRequestsScreen(onBack = { navState = HomeNavState.Home })
        is HomeNavState.AdminSettings -> AdminSettingsScreen(onBack = { navState = HomeNavState.Home })

        is HomeNavState.Leagues -> LeaguesListScreen(
            onBack = { navState = HomeNavState.Home },
            onOpenLeague = { id -> navState = HomeNavState.LeagueDetail(id) }
        )
        is HomeNavState.LeagueDetail -> LeagueDetailScreen(
            leagueId = state.id,
            onBack = { navState = HomeNavState.Leagues }
        )
        is HomeNavState.LocationDetail -> LocationDetailScreen(
            locationId = state.id,
            onBack = { navState = HomeNavState.Home },
            onWriteReview = { navState = HomeNavState.LocationReview(state.id) }
        )
        is HomeNavState.LocationReview -> LocationReviewFormScreen(
            locationId = state.id,
            userId = user.id,
            onBack = { navState = HomeNavState.LocationDetail(state.id) },
            onSubmitted = { navState = HomeNavState.LocationDetail(state.id) }
        )

        is HomeNavState.StartChat -> {
            val startVm: StartConversationViewModel = viewModel()
            val startState by startVm.state.collectAsState()
            LaunchedEffect(state.otherUserId) {
                startVm.start(user.id, state.otherUserId)
            }
            when (val s = startState) {
                is UiState.Loading -> com.ileader.app.ui.components.LoadingScreen()
                is UiState.Error -> com.ileader.app.ui.components.ErrorScreen(s.message) {
                    startVm.start(user.id, state.otherUserId)
                }
                is UiState.Success -> {
                    LaunchedEffect(s.data) {
                        navState = HomeNavState.Chat(s.data, "Диалог")
                    }
                    com.ileader.app.ui.components.LoadingScreen()
                }
            }
        }
        is HomeNavState.Chat -> ChatScreen(
            conversationId = state.conversationId,
            myUserId = user.id,
            title = state.otherName,
            onBack = { navState = HomeNavState.Home }
        )

        is HomeNavState.TournamentEdit -> TournamentEditScreen(
            tournamentId = state.id,
            onBack = { navState = HomeNavState.TournamentDetail(state.id) }
        )
        is HomeNavState.QrScanner -> QrScannerScreen(
            tournamentId = state.tournamentId,
            tournamentName = state.tournamentName,
            onBack = { navState = HomeNavState.TournamentDetail(state.tournamentId) }
        )
        is HomeNavState.ManualCheckIn -> ManualCheckInScreen(
            tournamentId = state.tournamentId,
            tournamentName = state.tournamentName,
            onBack = { navState = HomeNavState.TournamentDetail(state.tournamentId) }
        )
        is HomeNavState.HelperManagement -> HelperManagementScreen(
            tournamentId = state.tournamentId,
            tournamentName = state.tournamentName,
            userId = user.id,
            onBack = { navState = HomeNavState.TournamentDetail(state.tournamentId) }
        )
        is HomeNavState.RefereeManagement -> RefereeManagementScreen(
            tournamentId = state.tournamentId,
            tournamentName = state.tournamentName,
            onBack = { navState = HomeNavState.TournamentDetail(state.tournamentId) }
        )
        is HomeNavState.InviteCodes -> InviteCodesScreen(
            tournamentId = state.tournamentId,
            tournamentName = state.tournamentName,
            userId = user.id,
            onBack = { navState = HomeNavState.TournamentDetail(state.tournamentId) }
        )
    }
}
