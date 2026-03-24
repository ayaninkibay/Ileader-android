package com.ileader.app.ui.screens.main

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.User
import com.ileader.app.ui.components.SportBackground
import com.ileader.app.ui.navigation.BottomNavItem
import com.ileader.app.ui.navigation.getBottomNavItems
import com.ileader.app.ui.screens.athlete.*
import com.ileader.app.ui.screens.admin.*
import com.ileader.app.data.DeepLinkTarget
import com.ileader.app.data.DeepLinkType
import com.ileader.app.ui.screens.common.ChatListScreen
import com.ileader.app.ui.screens.common.ChatScreen
import com.ileader.app.ui.screens.common.NotificationsScreen
import com.ileader.app.ui.screens.common.PlaceholderScreen
import com.ileader.app.ui.screens.media.*
import com.ileader.app.ui.screens.organizer.*
import com.ileader.app.ui.screens.referee.*
import com.ileader.app.ui.screens.trainer.*
import com.ileader.app.ui.screens.sponsor.*
import com.ileader.app.ui.screens.viewer.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.theme.DarkAppColors
import com.ileader.app.ui.theme.floatingShadow
import com.ileader.app.ui.viewmodels.NotificationsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(
    user: User,
    onSignOut: () -> Unit,
    deepLinkTarget: DeepLinkTarget? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val colors = LocalAppColors.current
    val isDark = colors.bg == DarkAppColors.bg
    val notificationsVm: NotificationsViewModel = viewModel()
    val unreadCount by notificationsVm.unreadCount.collectAsState()

    LaunchedEffect(user.id) { notificationsVm.loadUnreadCount(user.id) }

    val bottomNavItems = getBottomNavItems(user.role, user.teamId, unreadNotifications = unreadCount)
    var selectedIndex by remember { mutableIntStateOf(0) }

    // Чат навигация: null = обычный роутинг, "list" = список чатов, "chat/{id}/{name}" = конкретный чат
    var chatRoute by remember { mutableStateOf<String?>(null) }

    // Deep link обработка — перенаправляем на нужный экран
    // TODO: При реализации TournamentDetailScreen / PublicProfileScreen в Чатах 2-3 —
    // здесь можно направить на конкретный экран. Пока переключаем на вкладку "Турниры" или "Сообщество".
    LaunchedEffect(deepLinkTarget) {
        if (deepLinkTarget != null) {
            chatRoute = null
            when (deepLinkTarget.type) {
                DeepLinkType.TOURNAMENT -> {
                    // Найти индекс вкладки "Турниры"
                    val idx = bottomNavItems.indexOfFirst { it.route.contains("tournaments") }
                    if (idx >= 0) selectedIndex = idx
                }
                DeepLinkType.ATHLETE_PROFILE, DeepLinkType.TEAM_PROFILE -> {
                    // Найти индекс вкладки "Сообщество" или "Профиль"
                    val idx = bottomNavItems.indexOfFirst { it.route.contains("community") }
                    if (idx >= 0) selectedIndex = idx
                }
            }
            onDeepLinkConsumed()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(colors.bg)
    ) {
        // Content area — takes full space, padded at bottom for nav bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = if (chatRoute?.startsWith("chat/") == true) 0.dp else 74.dp)
        ) {
            when {
                chatRoute == "list" -> ChatListScreen(
                    user = user,
                    onOpenChat = { conversationId, otherUserName ->
                        chatRoute = "chat/$conversationId/$otherUserName"
                    }
                )
                chatRoute?.startsWith("chat/") == true -> {
                    val parts = chatRoute!!.removePrefix("chat/").split("/", limit = 2)
                    ChatScreen(
                        user = user,
                        conversationId = parts[0],
                        otherUserName = parts.getOrElse(1) { "Чат" },
                        onBack = { chatRoute = "list" }
                    )
                }
                else -> {
                    val currentRoute = bottomNavItems.getOrNull(selectedIndex)?.route ?: ""
                    RoleScreenRouter(
                        route = currentRoute,
                        user = user,
                        onSignOut = onSignOut,
                        onNavigate = { route ->
                            if (route == "chat") {
                                chatRoute = "list"
                            } else {
                                val index = bottomNavItems.indexOfFirst { it.route == route }
                                if (index >= 0) selectedIndex = index
                            }
                        }
                    )
                }
            }
        }

        // Floating bottom bar (скрыть в чате)
        if (chatRoute?.startsWith("chat/") != true) {
            ILeaderBottomBar(
                items = bottomNavItems,
                selectedIndex = selectedIndex,
                onItemSelected = {
                    chatRoute = null
                    selectedIndex = it
                },
                isDark = isDark,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun ILeaderBottomBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .floatingShadow(isDark),
            color = colors.cardBg,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    val iconAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.55f,
                        animationSpec = tween(250, easing = EaseInOut),
                        label = "iconAlpha$index"
                    )
                    val indicatorAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = tween(250, easing = EaseInOut),
                        label = "indicator$index"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onItemSelected(index) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        BadgedBox(
                            badge = {
                                if (item.badge > 0) {
                                    Badge(
                                        containerColor = ILeaderColors.PrimaryRed,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            item.badge.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier
                                    .size(26.dp)
                                    .alpha(iconAlpha),
                                tint = if (isSelected) colors.accent else colors.textMuted
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // Indicator bar
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(3.dp)
                                .alpha(indicatorAlpha)
                                .clip(RoundedCornerShape(2.dp))
                                .background(colors.accent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleScreenRouter(
    route: String,
    user: User,
    onSignOut: () -> Unit,
    onNavigate: (String) -> Unit
) {
    when {
        // Athlete screens
        route == "athlete/dashboard" -> AthleteDashboardScreen(
            user = user,
            onNavigateToTournaments = { onNavigate("athlete/tournaments") },
            onNavigateToResults = { onNavigate("athlete/results") },
            onNavigateToGoals = { onNavigate("athlete/goals") }
        )
        route == "athlete/tournaments" -> AthleteTournamentsScreen(user = user)
        route == "athlete/results" -> AthleteResultsScreen(user = user)
        route == "athlete/team" -> AthleteTeamScreen(user = user, onBack = { onNavigate("athlete/dashboard") })
        route == "athlete/goals" -> AthleteGoalsScreen(user = user)
        route == "athlete/profile" -> AthleteProfileScreen(user = user, onSignOut = onSignOut)

        // Trainer screens
        route == "trainer/dashboard" -> TrainerDashboardScreen(
            user = user,
            onNavigateToTeam = { onNavigate("trainer/team") },
            onNavigateToTournaments = { onNavigate("trainer/tournaments") },
            onNavigateToStatistics = { onNavigate("trainer/statistics") }
        )
        route == "trainer/team" -> TrainerTeamScreen(user = user)
        route == "trainer/tournaments" -> TrainerTournamentsScreen(user = user)
        route == "trainer/statistics" -> TrainerStatisticsScreen(user = user)
        route == "trainer/profile" -> TrainerProfileScreen(user = user, onSignOut = onSignOut)

        // Organizer screens
        route == "organizer/dashboard" -> OrganizerDashboardScreen(
            user = user,
            onNavigateToTournaments = { onNavigate("organizer/tournaments") },
            onNavigateToNotifications = { onNavigate("organizer/notifications") }
        )
        route == "organizer/tournaments" -> OrganizerTournamentsScreen(user = user)
        route == "organizer/notifications" -> NotificationsScreen(user = user)
        route == "organizer/profile" -> OrganizerProfileScreen(user = user, onSignOut = onSignOut)

        // Referee screens
        route == "referee/dashboard" -> RefereeDashboardScreen(user = user, onNavigate = onNavigate)
        route == "referee/tournaments" -> RefereeTournamentsScreen(user = user, onNavigate = onNavigate)
        route == "referee/requests" -> RefereeRequestsScreen(user = user, onNavigate = onNavigate)
        route == "referee/history" -> RefereeHistoryScreen(user = user, onNavigate = onNavigate)
        route == "referee/profile" -> RefereeProfileScreen(user = user, onSignOut = onSignOut)

        // Sponsor screens
        route == "sponsor/dashboard" -> SponsorDashboardScreen(user = user, onNavigate = onNavigate)
        route == "sponsor/tournaments" -> SponsorTournamentsScreen(user = user)
        route == "sponsor/notifications" -> SponsorNotificationsScreen(user = user)
        route == "sponsor/profile" -> SponsorProfileScreen(user = user, onSignOut = onSignOut)

        // Media screens
        route == "media/dashboard" -> MediaDashboardScreen(user = user, onNavigate = onNavigate)
        route == "media/tournaments" -> MediaTournamentsScreen(user = user, onNavigate = onNavigate)
        route == "media/content" -> MediaContentScreen(user = user)
        route == "media/notifications" -> NotificationsScreen(user = user)
        route == "media/profile" -> MediaProfileScreen(user = user, onSignOut = onSignOut)

        // Admin screens
        route == "admin/dashboard" -> AdminDashboardScreen(user = user)
        route == "admin/users" -> AdminUsersScreen(user = user)
        route == "admin/tournaments" -> AdminTournamentsScreen(user = user)
        route == "admin/requests" -> AdminRequestsScreen(user = user)
        route == "admin/settings" -> AdminSettingsScreen(user = user)

        // Viewer screens
        route == "viewer/home" -> ViewerHomeScreen(
            user = user,
            onNavigateToTournaments = { onNavigate("viewer/tournaments") },
            onNavigateToNews = { onNavigate("viewer/news") }
        )
        route == "viewer/tournaments" -> ViewerTournamentsTab(user = user)
        route == "viewer/news" -> ViewerNewsTab(user = user)
        route == "viewer/community" -> ViewerCommunityTab(user = user)
        route == "viewer/profile" -> ViewerProfileScreen(user = user, onSignOut = onSignOut)

        // Чат (общий для всех ролей — доступен через onNavigate("chat"))
        route == "chat" -> ChatListScreen(
            user = user,
            onOpenChat = { _, _ -> /* handled by MainScreen chatRoute */ }
        )

        else -> PlaceholderScreen("Страница не найдена", user, onSignOut)
    }
}
