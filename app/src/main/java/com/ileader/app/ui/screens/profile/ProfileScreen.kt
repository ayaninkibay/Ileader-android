package com.ileader.app.ui.screens.profile

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.AthleteGoal
import com.ileader.app.data.models.GoalStatus
import com.ileader.app.data.models.User
import com.ileader.app.data.models.UserRole
import com.ileader.app.data.preferences.SportPreference
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.ErrorScreen
import com.ileader.app.ui.components.LoadingScreen
import com.ileader.app.ui.components.ThemeSwitcherCard
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.ProfileViewModel
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onSignOut: () -> Unit,
    onEditProfile: () -> Unit,
    onTickets: () -> Unit,
    onNotifications: () -> Unit
) {
    val vm: ProfileViewModel = viewModel()
    val profileState by vm.profile.collectAsState()
    val stats by vm.stats.collectAsState()
    val userSports by vm.userSports.collectAsState()
    val goalsState by vm.goals.collectAsState()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showSportSheet by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(user.id) { vm.load(user.id, user.role) }
    LaunchedEffect(Unit) { started = true }

    // Animations
    val avatarScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.7f,
        animationSpec = tween(500, delayMillis = 100, easing = EaseOutBack),
        label = "avatarScale"
    )
    val avatarAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400, delayMillis = 100),
        label = "avatarAlpha"
    )
    val statsAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400, delayMillis = 250),
        label = "statsAlpha"
    )
    val statsOffset by animateFloatAsState(
        targetValue = if (started) 0f else 40f,
        animationSpec = tween(400, delayMillis = 250, easing = EaseOutBack),
        label = "statsOffset"
    )
    val actionsAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400, delayMillis = 400),
        label = "actionsAlpha"
    )
    val actionsOffset by animateFloatAsState(
        targetValue = if (started) 0f else 40f,
        animationSpec = tween(400, delayMillis = 400, easing = EaseOutBack),
        label = "actionsOffset"
    )
    val bottomAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400, delayMillis = 550),
        label = "bottomAlpha"
    )
    val bottomOffset by animateFloatAsState(
        targetValue = if (started) 0f else 30f,
        animationSpec = tween(400, delayMillis = 550, easing = EaseOutBack),
        label = "bottomOffset"
    )

    val isDark = DarkTheme.isDark

    when (val state = profileState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = state.message, onRetry = { vm.load(user.id, user.role) })
        is UiState.Success -> {
            val profile = state.data

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Bg)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Big gradient header (inzhu-style) ──
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Gradient bg
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        ILeaderColors.DarkRed,
                                        ILeaderColors.PrimaryRed,
                                        Color(0xFFFF8A80)
                                    ),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(
                                        Float.POSITIVE_INFINITY,
                                        Float.POSITIVE_INFINITY
                                    )
                                ),
                                shape = RoundedCornerShape(
                                    bottomStart = 28.dp,
                                    bottomEnd = 28.dp
                                )
                            )
                            .statusBarsPadding()
                    ) {
                        // Top nav: Settings + Notifications
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Settings icon
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable { /* settings */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            // Notification bell
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { onNotifications() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    null,
                                    tint = ILeaderColors.DarkRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Avatar overlapping header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(top = 90.dp)
                            .graphicsLayer {
                                scaleX = avatarScale
                                scaleY = avatarScale
                                alpha = avatarAlpha
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with rings (inzhu style)
                        Box(contentAlignment = Alignment.Center) {
                            // Outer ring
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color.Transparent)
                                    .border(
                                        2.dp,
                                        Color.White.copy(alpha = 0.15f),
                                        CircleShape
                                    )
                            )
                            // Middle ring
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(Color.Transparent)
                                    .border(
                                        1.5.dp,
                                        Color.White.copy(alpha = 0.1f),
                                        CircleShape
                                    )
                            )
                            // Avatar circle
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(CardBg)
                                    .border(3.dp, CardBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (profile.avatarUrl != null) {
                                    AsyncImage(
                                        model = profile.avatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(94.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(94.dp)
                                            .clip(CircleShape)
                                            .background(Accent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (profile.name ?: user.name)
                                                .take(2).uppercase(),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Name
                        Text(
                            text = profile.name ?: user.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(4.dp))

                        // Email
                        Text(
                            text = user.email,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )

                        Spacer(Modifier.height(8.dp))

                        // Role badge + city
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Accent.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = user.role.displayName,
                                    modifier = Modifier.padding(
                                        horizontal = 14.dp,
                                        vertical = 5.dp
                                    ),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Accent
                                )
                            }
                            if (profile.city != null) {
                                Text(
                                    profile.city,
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Stats row (inzhu style — with icons) ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = statsAlpha
                            translationY = statsOffset
                        },
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val primaryStats = stats.firstOrNull()
                    when (user.role) {
                        UserRole.ATHLETE -> {
                            StatCard(
                                icon = Icons.Default.EmojiEvents,
                                value = "${primaryStats?.tournaments ?: 0}",
                                label = "Турниры",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.MilitaryTech,
                                value = "${primaryStats?.wins ?: 0}",
                                label = "Победы",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.Leaderboard,
                                value = "${primaryStats?.rating ?: 1000}",
                                label = "Рейтинг",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        UserRole.TRAINER -> {
                            StatCard(
                                icon = Icons.Default.EmojiEvents,
                                value = "${primaryStats?.tournaments ?: 0}",
                                label = "Турниры",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.Leaderboard,
                                value = "${primaryStats?.rating ?: 1000}",
                                label = "Рейтинг",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        UserRole.REFEREE, UserRole.ORGANIZER -> {
                            StatCard(
                                icon = Icons.Default.EmojiEvents,
                                value = "${primaryStats?.tournaments ?: 0}",
                                label = "Турниры",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        else -> {
                            StatCard(
                                icon = Icons.Default.SportsScore,
                                value = "${userSports.size}",
                                label = "Виды спорта",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ── Goals section (athlete only) ──
                if (user.role == UserRole.ATHLETE && goalsState != null) {
                    Spacer(Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .graphicsLayer {
                                alpha = statsAlpha
                                translationY = statsOffset
                            },
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Мои цели",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        when (val gs = goalsState) {
                            is UiState.Success -> {
                                if (gs.data.isEmpty()) {
                                    Text(
                                        "Нет активных целей",
                                        fontSize = 13.sp,
                                        color = TextMuted
                                    )
                                } else {
                                    gs.data.forEach { goal ->
                                        GoalCard(goal)
                                    }
                                }
                            }
                            is UiState.Error -> {
                                Text(gs.message, fontSize = 13.sp, color = Accent)
                            }
                            else -> {
                                Text("Загрузка целей...", fontSize = 13.sp, color = TextMuted)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Quick Actions (inzhu CustomButtonView style) ──
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = actionsAlpha
                            translationY = actionsOffset
                        },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionCard(
                        Icons.Default.Edit,
                        "Редактировать профиль",
                        onClick = onEditProfile
                    )
                    ActionCard(
                        Icons.Default.ConfirmationNumber,
                        "Мои билеты",
                        onClick = onTickets
                    )
                    ActionCard(
                        Icons.Default.Notifications,
                        "Уведомления",
                        onClick = onNotifications
                    )
                    ActionCard(
                        Icons.Default.SportsScore,
                        "Виды спорта",
                        onClick = { showSportSheet = true }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Theme switcher
                Box(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = bottomAlpha
                            translationY = bottomOffset
                        }
                ) {
                    ThemeSwitcherCard()
                }

                Spacer(Modifier.height(8.dp))

                // Language switcher
                Box(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = bottomAlpha
                            translationY = bottomOffset
                        }
                ) {
                    com.ileader.app.ui.components.LanguageSwitcherCard()
                }

                Spacer(Modifier.height(12.dp))

                // Sign out
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = bottomAlpha
                            translationY = bottomOffset
                        }
                        .clickable { showSignOutDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg,
                    shadowElevation = if (isDark) 0.dp else 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ILeaderColors.Error.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                null,
                                tint = ILeaderColors.Error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            "Выйти",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = ILeaderColors.Error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }

    // ── Dialogs ──
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Выход", color = TextPrimary) },
            text = { Text("Вы уверены, что хотите выйти?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showSignOutDialog = false; onSignOut() }) {
                    Text("Выйти", color = ILeaderColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Отмена", color = TextMuted)
                }
            }
        )
    }

    if (showSportSheet) {
        SportSelectionSheet(userId = user.id, onDismiss = { showSportSheet = false })
    }
}

// ══════════════════════════════════════════════════════════
// Stat Card (inzhu style — individual cards with icons)
// ══════════════════════════════════════════════════════════

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val isDark = DarkTheme.isDark
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon, null,
                tint = Accent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
// Action Card (inzhu CustomButtonView style)
// ══════════════════════════════════════════════════════════

@Composable
private fun ActionCard(icon: ImageVector, label: String, onClick: () -> Unit) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = CardBg,
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(
                label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
// Goal Card
// ══════════════════════════════════════════════════════════

@Composable
private fun GoalCard(goal: AthleteGoal) {
    val isDark = DarkTheme.isDark
    val progress = if (goal.targetValue > 0) {
        (goal.currentValue.toFloat() / goal.targetValue).coerceIn(0f, 1f)
    } else 0f

    val statusColor = when (goal.status) {
        GoalStatus.COMPLETED -> Color(0xFF22C55E)
        GoalStatus.FAILED -> Color(0xFFEF4444)
        GoalStatus.ACTIVE -> Accent
    }

    val progressColor = when (goal.status) {
        GoalStatus.COMPLETED -> Color(0xFF22C55E)
        GoalStatus.FAILED -> Color(0xFFEF4444)
        GoalStatus.ACTIVE -> Color(0xFF3B82F6)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    goal.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        goal.status.displayName,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.12f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "${goal.currentValue}/${goal.targetValue} ${goal.type.displayName.lowercase()}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
// Sport selection bottom sheet
// ══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SportSelectionSheet(userId: String, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sportPref = remember { SportPreference(context) }

    var allSports by remember { mutableStateOf<List<SportDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val selectedIds = remember { mutableStateListOf<String>() }
    val currentIds by sportPref.selectedSportIds.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        try {
            allSports = ViewerRepository().getSports()
            selectedIds.addAll(currentIds)
        } catch (_: Exception) {}
        loading = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                "Виды спорта",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text("Выберите 1-3 вида спорта", fontSize = 13.sp, color = TextMuted)
            Spacer(Modifier.height(16.dp))

            if (loading) {
                LoadingScreen()
            } else {
                allSports.forEach { sport ->
                    val isSelected = selectedIds.contains(sport.id)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                if (isSelected) selectedIds.remove(sport.id)
                                else if (selectedIds.size < 3) selectedIds.add(sport.id)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Accent.copy(alpha = 0.1f)
                        else Color.Transparent
                    ) {
                        Text(
                            sport.name,
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 12.dp
                            ),
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold
                            else FontWeight.Normal,
                            color = if (isSelected) Accent else TextPrimary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = selectedIds.isNotEmpty()) {
                            scope.launch {
                                val selected =
                                    allSports.filter { selectedIds.contains(it.id) }
                                if (selected.isNotEmpty()) {
                                    sportPref.setSports(
                                        selected.map { it.id },
                                        selected.map { it.name })
                                }
                                onDismiss()
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedIds.isNotEmpty()) Accent else TextMuted
                ) {
                    Text(
                        "Сохранить",
                        modifier = Modifier
                            .padding(vertical = 14.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
