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
import com.ileader.app.data.DeepLinkTarget
import com.ileader.app.data.DeepLinkType
import com.ileader.app.ui.navigation.BottomNavItem
import com.ileader.app.ui.navigation.getBottomNavItems
import com.ileader.app.ui.screens.home.HomeTab
import com.ileader.app.ui.screens.sport.SportTab
import com.ileader.app.ui.screens.mytournaments.MyTournamentsTab
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

    // Deep link handling
    LaunchedEffect(deepLinkTarget) {
        if (deepLinkTarget != null) {
            when (deepLinkTarget.type) {
                DeepLinkType.TOURNAMENT -> selectedRoute = "my_tournaments"
                DeepLinkType.ATHLETE_PROFILE, DeepLinkType.TEAM_PROFILE -> selectedRoute = "profile"
            }
            onDeepLinkConsumed()
        }
    }

    CompositionLocalProvider(LocalSnackbarHost provides snackbarHostState) {
        Box(
            modifier = Modifier.fillMaxSize().background(colors.bg)
        ) {
            // Content area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(bottom = 74.dp)
            ) {
                when (selectedRoute) {
                    "home" -> HomeTab(user = user)
                    "sport" -> SportTab(user = user)
                    "my_tournaments" -> MyTournamentsTab(user = user, onSignOut = onSignOut)
                    "profile" -> ProfileTab(user = user, onSignOut = onSignOut)
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

