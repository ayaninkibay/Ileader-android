package com.ileader.app.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.User
import com.ileader.app.data.DeepLinkTarget
import com.ileader.app.data.DeepLinkType
import com.ileader.app.ui.navigation.BottomNavItem
import com.ileader.app.ui.navigation.getBottomNavItems
import com.ileader.app.data.models.UserRole
import com.ileader.app.ui.screens.home.HomeTab
import com.ileader.app.ui.screens.sport.SportTab
import com.ileader.app.ui.screens.mytournaments.MyTournamentsTab
import com.ileader.app.ui.screens.media.MediaTab
import com.ileader.app.ui.screens.sponsor.SponsorTab
import com.ileader.app.ui.screens.profile.ProfileTab
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.theme.DarkAppColors
import com.ileader.app.ui.theme.floatingShadow
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.LocalSnackbarHost

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

    val bottomNavItems = getBottomNavItems()

    var selectedRoute by remember { mutableStateOf(bottomNavItems.firstOrNull()?.route ?: "") }

    val selectedIndex = bottomNavItems.indexOfFirst { it.route == selectedRoute }
        .coerceAtLeast(0)

    // Push «message» → открыть диалог внутри ProfileTab.
    var pendingChatConversationId by remember { mutableStateOf<String?>(null) }
    // Прочие пуши → открыть экран уведомлений внутри ProfileTab.
    var pendingOpenNotifications by remember { mutableStateOf(false) }

    // Deep link handling
    LaunchedEffect(deepLinkTarget) {
        if (deepLinkTarget != null) {
            when (deepLinkTarget.type) {
                DeepLinkType.TOURNAMENT -> selectedRoute = "my_tournaments"
                DeepLinkType.ATHLETE_PROFILE, DeepLinkType.TEAM_PROFILE -> selectedRoute = "profile"
                DeepLinkType.CONVERSATION -> {
                    pendingChatConversationId = deepLinkTarget.id
                    selectedRoute = "profile"
                }
                DeepLinkType.NOTIFICATIONS -> {
                    pendingOpenNotifications = true
                    selectedRoute = "profile"
                }
            }
            onDeepLinkConsumed()
        }
    }

    CompositionLocalProvider(LocalSnackbarHost provides snackbarHostState) {
        Box(
            modifier = Modifier.fillMaxSize().background(colors.bg)
        ) {
            // Content area — crossfade between tabs for a softer feel than a hard swap.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(bottom = 74.dp)
            ) {
                androidx.compose.animation.Crossfade(
                    targetState = selectedRoute,
                    animationSpec = tween(durationMillis = 200),
                    label = "tabContent"
                ) { route ->
                    when (route) {
                        "home" -> HomeTab(
                            user = user,
                            onNavigateToSport = { selectedRoute = "sport" }
                        )
                        "sport" -> SportTab(user = user)
                        "my_tournaments" -> {
                            if (user.role == UserRole.MEDIA) {
                                MediaTab(user = user)
                            } else if (user.role == UserRole.SPONSOR) {
                                SponsorTab(user = user)
                            } else {
                                MyTournamentsTab(user = user, onSignOut = onSignOut)
                            }
                        }
                        "profile" -> ProfileTab(
                            user = user,
                            onSignOut = onSignOut,
                            pendingChatConversationId = pendingChatConversationId,
                            onPendingChatConsumed = { pendingChatConversationId = null },
                            pendingOpenNotifications = pendingOpenNotifications,
                            onPendingNotificationsConsumed = { pendingOpenNotifications = false }
                        )
                    }
                }
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

    // Pill colors
    val pillBg = if (isDark) Color.White else Color(0xFF1C1C1E)
    val pillContentColor = if (isDark) Color(0xFF1C1C1E) else Color.White
    val unselectedColor = if (isDark) colors.textMuted else Color(0xFF8E8E93)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .floatingShadow(isDark),
            color = colors.cardBg,
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index

                    BottomBarItem(
                        item = item,
                        isSelected = isSelected,
                        pillBg = pillBg,
                        pillContentColor = pillContentColor,
                        unselectedColor = unselectedColor,
                        onClick = { onItemSelected(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    item: BottomNavItem,
    isSelected: Boolean,
    pillBg: Color,
    pillContentColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animate the unselected icon's opacity for a soft cross-fade with the pill.
    val unselectedIconAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0f else 0.7f,
        animationSpec = tween(durationMillis = 220),
        label = "unselectedIconAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Pill grows in with a springy scale + fades on selection change.
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(
                initialScale = 0.5f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(tween(180)),
            exit = scaleOut(targetScale = 0.7f, animationSpec = tween(150)) + fadeOut(tween(120))
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(pillBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (item.badge > 0) {
                            Badge(
                                containerColor = ILeaderColors.PrimaryRed,
                                contentColor = Color.White
                            ) {
                                Text(item.badge.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp),
                        tint = pillContentColor
                    )
                }
            }
        }

        // Unselected icon — fades out as the pill comes in (no hard snap).
        if (unselectedIconAlpha > 0.01f) {
            BadgedBox(
                badge = {
                    if (item.badge > 0) {
                        Badge(
                            containerColor = ILeaderColors.PrimaryRed,
                            contentColor = Color.White
                        ) {
                            Text(item.badge.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                modifier = Modifier.graphicsLayer { alpha = unselectedIconAlpha }
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    modifier = Modifier.size(24.dp),
                    tint = unselectedColor
                )
            }
        }
    }
}

