package com.ileader.app.ui.screens.viewer

import androidx.compose.runtime.*
import com.ileader.app.data.models.User
import com.ileader.app.ui.screens.common.CoursesListScreen
import com.ileader.app.ui.screens.common.CourseDetailScreen

/**
 * Tab wrappers that manage internal sub-navigation within bottom nav tabs.
 * Each wrapper maintains a stack so the user can navigate to detail screens
 * and come back to the list without switching the bottom nav tab.
 */

// --- Tournaments Tab ---

private sealed class TournamentsNavState {
    data object List : TournamentsNavState()
    data class Detail(val tournamentId: String) : TournamentsNavState()
    data class Results(val tournamentId: String) : TournamentsNavState()
}

@Composable
fun ViewerTournamentsTab(user: User) {
    var navState by remember { mutableStateOf<TournamentsNavState>(TournamentsNavState.List) }

    when (val state = navState) {
        is TournamentsNavState.List -> ViewerTournamentsScreen(
            user = user,
            onNavigateToDetail = { id -> navState = TournamentsNavState.Detail(id) }
        )
        is TournamentsNavState.Detail -> ViewerTournamentDetailScreen(
            tournamentId = state.tournamentId,
            user = user,
            onBack = { navState = TournamentsNavState.List },
            onNavigateToResults = { id -> navState = TournamentsNavState.Results(id) }
        )
        is TournamentsNavState.Results -> ViewerTournamentResultsScreen(
            tournamentId = state.tournamentId,
            user = user,
            onBack = { navState = TournamentsNavState.Detail(state.tournamentId) }
        )
    }
}

// --- News Tab ---

private sealed class NewsNavState {
    data object List : NewsNavState()
    data class Detail(val articleId: String) : NewsNavState()
}

@Composable
fun ViewerNewsTab(user: User) {
    var navState by remember { mutableStateOf<NewsNavState>(NewsNavState.List) }

    when (val state = navState) {
        is NewsNavState.List -> ViewerNewsScreen(
            user = user,
            onNavigateToDetail = { id -> navState = NewsNavState.Detail(id) }
        )
        is NewsNavState.Detail -> ViewerNewsDetailScreen(
            articleId = state.articleId,
            user = user,
            onBack = { navState = NewsNavState.List }
        )
    }
}

// --- Community Tab ---

private sealed class CommunityNavState {
    data object List : CommunityNavState()
    data class AthleteProfile(val athleteId: String) : CommunityNavState()
    data class TrainerProfile(val trainerId: String) : CommunityNavState()
    data class RefereeProfile(val refereeId: String) : CommunityNavState()
    data class TeamProfile(val teamId: String) : CommunityNavState()
}

@Composable
fun ViewerCommunityTab(user: User) {
    var navState by remember { mutableStateOf<CommunityNavState>(CommunityNavState.List) }

    when (val state = navState) {
        is CommunityNavState.List -> ViewerCommunityScreen(
            user = user,
            onNavigateToAthleteProfile = { id -> navState = CommunityNavState.AthleteProfile(id) },
            onNavigateToTrainerProfile = { id -> navState = CommunityNavState.TrainerProfile(id) },
            onNavigateToRefereeProfile = { id -> navState = CommunityNavState.RefereeProfile(id) },
            onNavigateToTeamProfile = { id -> navState = CommunityNavState.TeamProfile(id) }
        )
        is CommunityNavState.AthleteProfile -> ViewerAthleteProfileScreen(
            athleteId = state.athleteId,
            user = user,
            onBack = { navState = CommunityNavState.List }
        )
        is CommunityNavState.TrainerProfile -> ViewerTrainerProfileScreen(
            trainerId = state.trainerId,
            user = user,
            onBack = { navState = CommunityNavState.List }
        )
        is CommunityNavState.RefereeProfile -> ViewerRefereeProfileScreen(
            refereeId = state.refereeId,
            user = user,
            onBack = { navState = CommunityNavState.List }
        )
        is CommunityNavState.TeamProfile -> ViewerTeamProfileScreen(
            teamId = state.teamId,
            user = user,
            onBack = { navState = CommunityNavState.List }
        )
    }
}

// --- Courses/Academy Tab ---

private sealed class CoursesNavState {
    data object List : CoursesNavState()
    data class Detail(val courseId: String) : CoursesNavState()
}

@Composable
fun ViewerCoursesTab(user: User) {
    var navState by remember { mutableStateOf<CoursesNavState>(CoursesNavState.List) }

    when (val state = navState) {
        is CoursesNavState.List -> CoursesListScreen(
            user = user,
            onNavigateToDetail = { id -> navState = CoursesNavState.Detail(id) }
        )
        is CoursesNavState.Detail -> CourseDetailScreen(
            courseId = state.courseId,
            user = user,
            onBack = { navState = CoursesNavState.List }
        )
    }
}
