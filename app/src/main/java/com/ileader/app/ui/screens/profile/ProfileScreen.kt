package com.ileader.app.ui.screens.profile

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.ProfileViewModel
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    var showPrivacySheet by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(user.id) { vm.load(user.id, user.role) }
    LaunchedEffect(Unit) { started = true }

    val isDark = DarkTheme.isDark

    // Primary sport for banner image
    val primarySportName = remember(userSports) {
        userSports.firstOrNull()?.sports?.name ?: "картинг"
    }
    val bannerUrl = remember(primarySportName) {
        sportImageUrl(primarySportName) ?: "https://ileader.kz/img/karting/karting-15-1280x853.jpeg"
    }

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
                // ═══════════════════════════════════════
                // HERO BANNER with sport photo
                // ═══════════════════════════════════════
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Sport photo background
                    AsyncImage(
                        model = bannerUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Dark gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )
                    // Top buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { showSportSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { onNotifications() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, null, tint = ILeaderColors.DarkRed, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Avatar + name overlapping banner
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(top = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Animated glow ring avatar
                        GlowAvatar(
                            avatarUrl = profile.avatarUrl,
                            initials = (profile.name ?: user.name).take(2).uppercase(),
                            started = started
                        )

                        Spacer(Modifier.height(12.dp))

                        // Name on banner
                        Text(
                            text = profile.name ?: user.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(user.email, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))

                        Spacer(Modifier.height(8.dp))

                        // Role + city
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    user.role.displayName,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            if (profile.city != null) {
                                Text(profile.city, fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════
                // SPORT CHIPS
                // ═══════════════════════════════════════
                if (userSports.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    FadeIn(visible = started, delayMs = 100) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            userSports.forEach { sport ->
                                val name = sport.sports?.name ?: ""
                                val emoji = sportEmoji(name)
                                val color = sportColor(name)
                                Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.1f)) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(emoji, fontSize = 14.sp)
                                        Spacer(Modifier.width(6.dp))
                                        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ═══════════════════════════════════════
                // ANIMATED STATS
                // ═══════════════════════════════════════
                FadeIn(visible = started, delayMs = 200) {
                    val primaryStats = stats.firstOrNull()
                    val statItems = when (user.role) {
                        UserRole.ATHLETE -> listOf(
                            Triple(Icons.Default.EmojiEvents, primaryStats?.tournaments ?: 0, "Турниры"),
                            Triple(Icons.Default.MilitaryTech, primaryStats?.wins ?: 0, "Победы"),
                            Triple(Icons.Default.Leaderboard, primaryStats?.rating ?: 1000, "Рейтинг")
                        )
                        UserRole.TRAINER -> listOf(
                            Triple(Icons.Default.EmojiEvents, primaryStats?.tournaments ?: 0, "Турниры"),
                            Triple(Icons.Default.Leaderboard, primaryStats?.rating ?: 1000, "Рейтинг"),
                            Triple(Icons.Default.People, userSports.size, "Виды спорта")
                        )
                        UserRole.REFEREE, UserRole.ORGANIZER -> listOf(
                            Triple(Icons.Default.EmojiEvents, primaryStats?.tournaments ?: 0, "Турниры"),
                            Triple(Icons.Default.Gavel, userSports.size, "Виды спорта"),
                            Triple(Icons.Default.Leaderboard, primaryStats?.rating ?: 1000, "Рейтинг")
                        )
                        else -> listOf(
                            Triple(Icons.Default.SportsScore, userSports.size, "Виды спорта"),
                            Triple(Icons.Default.EmojiEvents, primaryStats?.tournaments ?: 0, "Турниры"),
                            Triple(Icons.Default.People, 0, "Команды")
                        )
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = CardBg,
                        shadowElevation = if (isDark) 0.dp else 3.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            statItems.forEachIndexed { idx, (icon, target, label) ->
                                if (idx > 0) {
                                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(alpha = 0.3f)))
                                }
                                AnimatedStatItem(
                                    icon = icon,
                                    targetValue = target,
                                    label = label,
                                    started = started,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════
                // GOALS (athlete only)
                // ═══════════════════════════════════════
                if (user.role == UserRole.ATHLETE && goalsState != null) {
                    Spacer(Modifier.height(20.dp))
                    FadeIn(visible = started, delayMs = 300) {
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            SectionHeader(title = "Мои цели")
                            Spacer(Modifier.height(10.dp))
                            when (val gs = goalsState) {
                                is UiState.Success -> {
                                    if (gs.data.isEmpty()) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            color = CardBg,
                                            shadowElevation = if (isDark) 0.dp else 2.dp
                                        ) {
                                            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Flag, null, tint = TextMuted, modifier = Modifier.size(24.dp))
                                                Spacer(Modifier.width(12.dp))
                                                Text("Нет активных целей", fontSize = 14.sp, color = TextMuted)
                                            }
                                        }
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            gs.data.forEach { goal -> GoalCard(goal) }
                                        }
                                    }
                                }
                                is UiState.Error -> Text(gs.message, fontSize = 13.sp, color = Accent)
                                else -> Text("Загрузка...", fontSize = 13.sp, color = TextMuted)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ═══════════════════════════════════════
                // QUICK ACTIONS — 2x2 grid
                // ═══════════════════════════════════════
                FadeIn(visible = started, delayMs = 400) {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        SectionHeader(title = "Действия")
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ActionTile(Icons.Default.Edit, "Редактировать\nпрофиль", onEditProfile, Modifier.weight(1f))
                            ActionTile(Icons.Default.ConfirmationNumber, "Мои\nбилеты", onTickets, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ActionTile(Icons.Default.Notifications, "Уведомления", onNotifications, Modifier.weight(1f))
                            ActionTile(Icons.Default.SportsScore, "Виды\nспорта", { showSportSheet = true }, Modifier.weight(1f))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ═══════════════════════════════════════
                // SETTINGS
                // ═══════════════════════════════════════
                FadeIn(visible = started, delayMs = 500) {
                    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(title = "Настройки")
                        Spacer(Modifier.height(2.dp))
                        ThemeSwitcherCard()
                        LanguageSwitcherCard()
                        SettingsRow(Icons.Default.Shield, "Конфиденциальность и условия") { showPrivacySheet = true }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Sign out
                FadeIn(visible = started, delayMs = 600) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { showSignOutDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        color = CardBg,
                        shadowElevation = if (isDark) 0.dp else 2.dp
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                    .background(ILeaderColors.Error.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = ILeaderColors.Error, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Text("Выйти", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = ILeaderColors.Error, modifier = Modifier.weight(1f))
                        }
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
    if (showPrivacySheet) {
        PrivacySheet(onDismiss = { showPrivacySheet = false })
    }
}

// ═══════════════════════════════════════════════════════════
// GLOW AVATAR — animated gradient ring
// ═══════════════════════════════════════════════════════════

@Composable
private fun GlowAvatar(avatarUrl: String?, initials: String, started: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "glowAngle"
    )
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.6f,
        animationSpec = tween(600, delayMillis = 100, easing = EaseOutBack),
        label = "avatarScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 100),
        label = "avatarAlpha"
    )

    val glowColors = listOf(
        ILeaderColors.PrimaryRed,
        Color(0xFFFF6B6B),
        Color(0xFFFF8A65),
        ILeaderColors.PrimaryRed
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
    ) {
        // Glow ring
        Box(
            modifier = Modifier
                .size(116.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = glowColors,
                            center = Offset(size.width / 2, size.height / 2)
                        ),
                        radius = size.width / 2,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
        )
        // White separator ring
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(Bg)
        )
        // Avatar
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(CardBg),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.size(104.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(104.dp).clip(CircleShape).background(Accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// ANIMATED STAT ITEM — count-up numbers
// ═══════════════════════════════════════════════════════════

@Composable
private fun AnimatedStatItem(
    icon: ImageVector,
    targetValue: Int,
    label: String,
    started: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = if (started) targetValue.toFloat() else 0f,
        animationSpec = tween(durationMillis = 900, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "stat_$label"
    )

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(6.dp))
        Text(
            "${animatedValue.roundToInt()}",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}

// ═══════════════════════════════════════════════════════════
// ACTION TILE — 2x2 grid card
// ═══════════════════════════════════════════════════════════

@Composable
private fun ActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SETTINGS ROW
// ═══════════════════════════════════════════════════════════

@Composable
private fun SettingsRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = CardBg,
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(colors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// GOAL CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun GoalCard(goal: AthleteGoal) {
    val isDark = DarkTheme.isDark
    val progress = if (goal.targetValue > 0) (goal.currentValue.toFloat() / goal.targetValue).coerceIn(0f, 1f) else 0f

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
                Text(goal.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(50), color = statusColor.copy(alpha = 0.12f)) {
                    Text(goal.status.displayName, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                }
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.12f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Spacer(Modifier.height(8.dp))
            Text("${goal.currentValue}/${goal.targetValue} ${goal.type.displayName.lowercase()}", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// PRIVACY SHEET
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacySheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Конфиденциальность и условия", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(16.dp))
            Text("Содержимое будет добавлено позже.", fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SPORT SELECTION SHEET
// ═══════════════════════════════════════════════════════════

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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text("Виды спорта", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Выберите 1-3 вида спорта", fontSize = 13.sp, color = TextMuted)
            Spacer(Modifier.height(16.dp))

            if (loading) {
                LoadingScreen()
            } else {
                allSports.forEach { sport ->
                    val isSelected = selectedIds.contains(sport.id)
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable {
                            if (isSelected) selectedIds.remove(sport.id)
                            else if (selectedIds.size < 3) selectedIds.add(sport.id)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Accent.copy(alpha = 0.1f) else Color.Transparent
                    ) {
                        Text(
                            sport.name,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Accent else TextPrimary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(enabled = selectedIds.isNotEmpty()) {
                        scope.launch {
                            val selected = allSports.filter { selectedIds.contains(it.id) }
                            if (selected.isNotEmpty()) {
                                sportPref.setSports(selected.map { it.id }, selected.map { it.name })
                            }
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedIds.isNotEmpty()) Accent else TextMuted
                ) {
                    Text(
                        "Сохранить",
                        modifier = Modifier.padding(vertical = 14.dp).fillMaxWidth(),
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
