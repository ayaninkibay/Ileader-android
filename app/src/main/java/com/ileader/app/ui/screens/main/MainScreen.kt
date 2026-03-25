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
import com.ileader.app.data.DeepLinkTarget
import com.ileader.app.data.DeepLinkType
import com.ileader.app.ui.screens.common.NotificationsScreen
import com.ileader.app.ui.screens.common.PlaceholderScreen
import com.ileader.app.ui.screens.media.*
import com.ileader.app.ui.screens.organizer.*
import com.ileader.app.ui.screens.referee.*
import com.ileader.app.ui.screens.trainer.*
import com.ileader.app.ui.screens.common.MyTicketsScreen
import com.ileader.app.ui.screens.helper.HelperDashboardScreen
import com.ileader.app.ui.screens.viewer.*
import com.ileader.app.ui.viewmodels.HelperViewModel
import com.ileader.app.ui.viewmodels.TicketsViewModel
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.theme.DarkAppColors
import com.ileader.app.ui.theme.floatingShadow
import com.ileader.app.ui.components.AnimatedBackground
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.LocalSnackbarHost
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
    val snackbarHostState = remember { SnackbarHostState() }
    val notificationsVm: NotificationsViewModel = viewModel()
    val unreadCount by notificationsVm.unreadCount.collectAsState()

    val helperVm: HelperViewModel = viewModel()
    val helperState by helperVm.state.collectAsState()
    val ticketsVm: TicketsViewModel = viewModel()
    val hasTickets by ticketsVm.hasTickets.collectAsState()

    LaunchedEffect(user.id) {
        notificationsVm.loadUnreadCount(user.id)
        helperVm.loadAssignments(user.id)
        ticketsVm.checkHasTickets(user.id)
    }

    // Check if user has active helper assignments
    val hasHelperAssignments = helperState is com.ileader.app.data.remote.UiState.Success &&
            (helperState as com.ileader.app.data.remote.UiState.Success).data.isNotEmpty()

    val bottomNavItems = getBottomNavItems(
        role = user.role,
        teamId = user.teamId,
        unreadNotifications = unreadCount,
        isHelper = hasHelperAssignments,
        hasTickets = hasTickets
    )

    // Navigate by route instead of index to avoid shifting when helper tab appears
    var selectedRoute by remember { mutableStateOf(bottomNavItems.firstOrNull()?.route ?: "") }

    // Ensure selectedRoute is valid when items change
    val selectedIndex = bottomNavItems.indexOfFirst { it.route == selectedRoute }
        .coerceAtLeast(0)

    // Deep link обработка — перенаправляем на нужный экран
    LaunchedEffect(deepLinkTarget) {
        if (deepLinkTarget != null) {
            when (deepLinkTarget.type) {
                DeepLinkType.TOURNAMENT -> {
                    val route = bottomNavItems.firstOrNull { it.route.contains("tournaments") }?.route
                    if (route != null) selectedRoute = route
                }
                DeepLinkType.ATHLETE_PROFILE, DeepLinkType.TEAM_PROFILE -> {
                    val route = bottomNavItems.firstOrNull { it.route.contains("community") }?.route
                    if (route != null) selectedRoute = route
                }
            }
            onDeepLinkConsumed()
        }
    }

    CompositionLocalProvider(LocalSnackbarHost provides snackbarHostState) {
        Box(
            modifier = Modifier.fillMaxSize().background(colors.bg)
        ) {
            // Content area — takes full space, padded at bottom for nav bar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(bottom = 74.dp)
            ) {
                RoleScreenRouter(
                    route = selectedRoute,
                    user = user,
                    onSignOut = onSignOut,
                    onNavigate = { route ->
                        val navItem = bottomNavItems.firstOrNull { it.route == route }
                        if (navItem != null) selectedRoute = route
                    }
                )
            }

            // Snackbar
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
                snackbar = { Snackbar(it, containerColor = DarkTheme.CardBg, contentColor = DarkTheme.TextPrimary) }
            )

            // Floating bottom bar
            ILeaderBottomBar(
                items = bottomNavItems,
                selectedIndex = selectedIndex,
                onItemSelected = { index ->
                    bottomNavItems.getOrNull(index)?.route?.let { selectedRoute = it }
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
        route == "organizer/notifications" -> NotificationsScreen(user = user)
        route == "organizer/profile" -> OrganizerProfileScreen(user = user, onSignOut = onSignOut)

        // Referee screens
        route == "referee/dashboard" -> RefereeDashboardScreen(user = user, onNavigate = onNavigate)
        route == "referee/tournaments" -> RefereeTournamentsScreen(user = user, onNavigate = onNavigate)
        route == "referee/requests" -> RefereeRequestsScreen(user = user, onNavigate = onNavigate)
        route == "referee/history" -> RefereeHistoryScreen(user = user, onNavigate = onNavigate)
        route == "referee/profile" -> RefereeProfileScreen(user = user, onSignOut = onSignOut)

        // Media screens
        route == "media/dashboard" -> MediaDashboardScreen(user = user, onNavigate = onNavigate)
        route == "media/tournaments" -> MediaTournamentsScreen(user = user, onNavigate = onNavigate)
        route == "media/content" -> MediaContentScreen(user = user, onNavigate = onNavigate)
        route == "media/analytics" -> MediaAnalyticsScreen(user = user, onNavigate = onNavigate)
        route == "media/content/new" -> MediaContentEditScreen(
            user = user, articleId = null,
            onBack = { onNavigate("media/content") },
            onSave = { onNavigate("media/content") }
        )
        route.startsWith("media/content/edit/") -> {
            val id = route.removePrefix("media/content/edit/")
            MediaContentEditScreen(
                user = user, articleId = id,
                onBack = { onNavigate("media/content") },
                onSave = { onNavigate("media/content") }
            )
        }
        route.startsWith("media/content/detail/") -> {
            val id = route.removePrefix("media/content/detail/")
            MediaContentDetailScreen(
                user = user, articleId = id,
                onBack = { onNavigate("media/content") },
                onEdit = { articleId -> onNavigate("media/content/edit/$articleId") }
            )
        }
        route == "media/notifications" -> NotificationsScreen(user = user)
        route == "media/profile" -> MediaProfileScreen(user = user, onSignOut = onSignOut)

        // Tickets screen (available for any role with active registrations)
        route == "my/tickets" -> MyTicketsScreen(user = user)

        // Helper screen (available for any role with active assignments)
        route == "helper/dashboard" -> HelperDashboardScreen(user = user)

        // Viewer screens
        route == "viewer/home" -> ViewerHomeScreen(
            user = user,
            onNavigateToTournaments = { onNavigate("viewer/tournaments") },
            onNavigateToNews = { onNavigate("viewer/news") }
        )
        route == "viewer/tournaments" -> ViewerTournamentsTab(user = user)
        route == "viewer/news" -> ViewerNewsTab(user = user)
        route == "viewer/community" -> ViewerCommunityTab(user = user)
        route == "viewer/courses" -> ViewerCoursesTab(user = user)
        route == "viewer/profile" -> ViewerProfileScreen(user = user, onSignOut = onSignOut)

        else -> PlaceholderScreen("Страница не найдена", user, onSignOut)
    }
}
