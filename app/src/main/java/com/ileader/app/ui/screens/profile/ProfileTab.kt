package com.ileader.app.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ileader.app.data.models.AthleteGoal
import com.ileader.app.data.models.User
import com.ileader.app.data.models.UserRole
import com.ileader.app.ui.screens.common.MyTicketsScreen
import com.ileader.app.ui.screens.common.NotificationsScreen
import com.ileader.app.ui.screens.detail.TeamDetailScreen
import com.ileader.app.ui.screens.common.CoursesListScreen
import com.ileader.app.ui.screens.common.CourseDetailScreen
import com.ileader.app.ui.screens.media.MediaArticlesScreen
import com.ileader.app.ui.screens.media.MediaArticleEditorScreen
import com.ileader.app.ui.screens.athlete.AchievementsScreen
import com.ileader.app.ui.screens.athlete.LapTimesScreen
import com.ileader.app.ui.screens.athlete.RacingLicenseScreen
import com.ileader.app.ui.screens.athlete.RatingHistoryScreen
import com.ileader.app.ui.screens.athlete.ResultsHistoryScreen
import com.ileader.app.ui.screens.chat.ChatScreen
import com.ileader.app.ui.screens.chat.ConversationsListScreen
import com.ileader.app.ui.screens.detail.PublicProfileScreen
import com.ileader.app.ui.screens.verification.VerificationRequestScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.viewmodels.StartConversationViewModel

private sealed class ProfileNavState {
    data object Main : ProfileNavState()
    data object Edit : ProfileNavState()
    data object Tickets : ProfileNavState()
    data object Notifications : ProfileNavState()
    data class GoalDetail(val goal: AthleteGoal) : ProfileNavState()
    data object GoalCreate : ProfileNavState()
    data object Articles : ProfileNavState()
    data class ArticleEditor(val articleId: String? = null) : ProfileNavState()
    data class TeamDetail(val teamId: String) : ProfileNavState()
    data object Academy : ProfileNavState()
    data class CourseDetail(val courseId: String) : ProfileNavState()
    data object Family : ProfileNavState()
    data object RatingHistory : ProfileNavState()
    data object ResultsHistory : ProfileNavState()
    data object Achievements : ProfileNavState()
    data object RacingLicense : ProfileNavState()
    data object LapTimes : ProfileNavState()
    data object Verification : ProfileNavState()
    data object Conversations : ProfileNavState()
    data class Chat(val conversationId: String, val otherName: String) : ProfileNavState()
    data class StartChat(val otherUserId: String) : ProfileNavState()
    data class PublicProfile(val userId: String) : ProfileNavState()
}

@Composable
fun ProfileTab(user: User, onSignOut: () -> Unit) {
    var navState by remember { mutableStateOf<ProfileNavState>(ProfileNavState.Main) }

    when (val state = navState) {
        is ProfileNavState.Main -> {
            ProfileScreen(
                user = user,
                onSignOut = onSignOut,
                onEditProfile = { navState = ProfileNavState.Edit },
                onTickets = { navState = ProfileNavState.Tickets },
                onNotifications = { navState = ProfileNavState.Notifications },
                onGoalClick = { goal -> navState = ProfileNavState.GoalDetail(goal) },
                onGoalCreate = { navState = ProfileNavState.GoalCreate },
                onArticles = { navState = ProfileNavState.Articles },
                onTeamClick = { navState = ProfileNavState.TeamDetail(it) },
                onAcademy = { navState = ProfileNavState.Academy },
                onFamily = { navState = ProfileNavState.Family },
                onRatingHistory = { navState = ProfileNavState.RatingHistory },
                onResultsHistory = { navState = ProfileNavState.ResultsHistory },
                onAchievements = { navState = ProfileNavState.Achievements },
                onRacingLicense = { navState = ProfileNavState.RacingLicense },
                onLapTimes = { navState = ProfileNavState.LapTimes },
                onVerification = { navState = ProfileNavState.Verification },
                onConversations = { navState = ProfileNavState.Conversations }
            )
        }
        is ProfileNavState.Edit -> {
            EditProfileScreen(user = user, onBack = { navState = ProfileNavState.Main })
        }
        is ProfileNavState.Tickets -> {
            MyTicketsScreen(user = user, onBack = { navState = ProfileNavState.Main })
        }
        is ProfileNavState.Notifications -> {
            NotificationsScreen(user = user, onBack = { navState = ProfileNavState.Main })
        }
        is ProfileNavState.GoalDetail -> {
            GoalDetailScreen(goal = state.goal, onBack = { navState = ProfileNavState.Main }, onDeleted = { navState = ProfileNavState.Main })
        }
        is ProfileNavState.GoalCreate -> {
            GoalCreateScreen(userId = user.id, onBack = { navState = ProfileNavState.Main }, onCreated = { navState = ProfileNavState.Main })
        }
        is ProfileNavState.Articles -> {
            MediaArticlesScreen(
                user = user,
                onBack = { navState = ProfileNavState.Main },
                onArticleClick = { id -> navState = ProfileNavState.ArticleEditor(id) },
                onCreateArticle = { navState = ProfileNavState.ArticleEditor(null) }
            )
        }
        is ProfileNavState.ArticleEditor -> {
            MediaArticleEditorScreen(
                userId = user.id,
                articleId = state.articleId,
                onBack = { navState = ProfileNavState.Articles }
            )
        }
        is ProfileNavState.TeamDetail -> {
            TeamDetailScreen(
                teamId = state.teamId,
                onBack = { navState = ProfileNavState.Main }
            )
        }
        is ProfileNavState.Academy -> {
            CoursesListScreen(
                user = user,
                onNavigateToDetail = { navState = ProfileNavState.CourseDetail(it) },
                onBack = { navState = ProfileNavState.Main }
            )
        }
        is ProfileNavState.CourseDetail -> {
            CourseDetailScreen(
                courseId = state.courseId,
                user = user,
                onBack = { navState = ProfileNavState.Academy }
            )
        }
        is ProfileNavState.Family -> {
            FamilyScreen(
                user = user,
                onBack = { navState = ProfileNavState.Main }
            )
        }
        is ProfileNavState.RatingHistory -> RatingHistoryScreen(user.id) { navState = ProfileNavState.Main }
        is ProfileNavState.ResultsHistory -> ResultsHistoryScreen(user.id) { navState = ProfileNavState.Main }
        is ProfileNavState.Achievements -> AchievementsScreen(user.id) { navState = ProfileNavState.Main }
        is ProfileNavState.RacingLicense -> RacingLicenseScreen(user.id) { navState = ProfileNavState.Main }
        is ProfileNavState.LapTimes -> LapTimesScreen(user.id) { navState = ProfileNavState.Main }
        is ProfileNavState.Verification -> VerificationRequestScreen(
            user = user,
            onBack = { navState = ProfileNavState.Main },
            onSubmitted = { navState = ProfileNavState.Main }
        )
        is ProfileNavState.Conversations -> ConversationsListScreen(
            myUserId = user.id,
            onBack = { navState = ProfileNavState.Main },
            onOpenConversation = { id, name -> navState = ProfileNavState.Chat(id, name) }
        )
        is ProfileNavState.Chat -> ChatScreen(
            conversationId = state.conversationId,
            myUserId = user.id,
            title = state.otherName,
            onBack = { navState = ProfileNavState.Conversations }
        )
        is ProfileNavState.StartChat -> {
            // Use StartConversationViewModel to create or fetch conversation, then navigate to Chat
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
                        navState = ProfileNavState.Chat(s.data, "Диалог")
                    }
                    com.ileader.app.ui.components.LoadingScreen()
                }
            }
        }
        is ProfileNavState.PublicProfile -> PublicProfileScreen(
            userId = state.userId,
            onBack = { navState = ProfileNavState.Main },
            onStartChat = { otherId -> navState = ProfileNavState.StartChat(otherId) }
        )
    }
}
