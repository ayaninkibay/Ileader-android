package com.ileader.app.ui.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Article
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
import com.ileader.app.data.remote.dto.*
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
    onNotifications: () -> Unit,
    onGoalClick: (AthleteGoal) -> Unit = {},
    onGoalCreate: () -> Unit = {},
    onSettings: () -> Unit = {},
    onTournamentClick: (String) -> Unit = {},
    onArticles: () -> Unit = {}
) {
    val vm: ProfileViewModel = viewModel()
    val profileState by vm.profile.collectAsState()
    val stats by vm.stats.collectAsState()
    val userSports by vm.userSports.collectAsState()
    val goalsState by vm.goals.collectAsState()
    val myTournaments by vm.myTournaments.collectAsState()
    val myResults by vm.myResults.collectAsState()
    val myTeam by vm.myTeam.collectAsState()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showSportSheet by remember { mutableStateOf(false) }
    var showPrivacySheet by remember { mutableStateOf(false) }
    var showLegalSheet by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(user.id) { vm.load(user.id, user.role) }
    LaunchedEffect(Unit) { started = true }

    val isDark = DarkTheme.isDark

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

            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Bg),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // ═══════════════════════════════════════
                // HERO BANNER
                // ═══════════════════════════════════════
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = bannerUrl, contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(360.dp)
                                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            Modifier.fillMaxWidth().height(360.dp)
                                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                                .background(Brush.verticalGradient(listOf(Color.Black.copy(0.3f), Color.Black.copy(0.75f))))
                        )
                        Row(
                            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(Color.Black.copy(0.3f)).clickable { showPrivacySheet = true }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Settings, null, tint = Color.White.copy(0.9f), modifier = Modifier.size(20.dp))
                            }
                            Box(Modifier.size(40.dp).clip(CircleShape).background(Color.Black.copy(0.3f)).clickable { onNotifications() }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Notifications, null, tint = Color.White.copy(0.9f), modifier = Modifier.size(20.dp))
                            }
                        }
                        Column(
                            Modifier.fillMaxWidth().statusBarsPadding().padding(top = 80.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProfileAvatar(profile.avatarUrl, (profile.name ?: user.name).take(2).uppercase(), primarySportName)
                            Spacer(Modifier.height(12.dp))
                            Text(profile.name ?: user.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                            Text(user.email, fontSize = 13.sp, color = Color.White.copy(0.7f))
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(0.2f)) {
                                    Text(user.role.displayName, Modifier.padding(horizontal = 14.dp, vertical = 5.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                                if (profile.city != null) Text(profile.city, fontSize = 13.sp, color = Color.White.copy(0.6f))
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════
                // SPORT CHIPS
                // ═══════════════════════════════════════
                if (userSports.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        FadeIn(visible = started, delayMs = 100) {
                            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                userSports.forEach { sport ->
                                    val name = sport.sports?.name ?: ""
                                    val emoji = sportEmoji(name)
                                    val color = sportColor(name)
                                    Surface(shape = RoundedCornerShape(50), color = color.copy(0.1f)) {
                                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(emoji, fontSize = 14.sp)
                                            Spacer(Modifier.width(6.dp))
                                            Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════
                // ANIMATED STATS
                // ═══════════════════════════════════════
                item {
                    Spacer(Modifier.height(20.dp))
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
                            else -> listOf(
                                Triple(Icons.Default.SportsScore, userSports.size, "Виды спорта"),
                                Triple(Icons.Default.EmojiEvents, primaryStats?.tournaments ?: 0, "Турниры"),
                                Triple(Icons.Default.Leaderboard, primaryStats?.rating ?: 1000, "Рейтинг")
                            )
                        }
                        Surface(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp), color = CardBg,
                            shadowElevation = if (isDark) 0.dp else 3.dp
                        ) {
                            Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                                statItems.forEachIndexed { idx, (icon, target, label) ->
                                    if (idx > 0) Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                                    AnimatedStatItem(icon, target, label, started, Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════
                // EDIT PROFILE BUTTON
                // ═══════════════════════════════════════
                item {
                    Spacer(Modifier.height(14.dp))
                    FadeIn(visible = started, delayMs = 250) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onEditProfile),
                            shape = RoundedCornerShape(14.dp),
                            color = CardBg,
                            shadowElevation = if (isDark) 0.dp else 2.dp
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, null, tint = Accent, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Редактировать профиль", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════
                // GOALS (athlete only)
                // ═══════════════════════════════════════
                if (user.role == UserRole.ATHLETE && goalsState != null) {
                    item {
                        Spacer(Modifier.height(20.dp))
                        FadeIn(visible = started, delayMs = 300) {
                            Column(Modifier.padding(horizontal = 16.dp)) {
                                SectionHeader("Мои цели", action = "+", onAction = onGoalCreate)
                                Spacer(Modifier.height(10.dp))
                                when (val gs = goalsState) {
                                    is UiState.Success -> {
                                        if (gs.data.isEmpty()) {
                                            Surface(Modifier.fillMaxWidth().clickable(onClick = onGoalCreate), RoundedCornerShape(14.dp), CardBg, shadowElevation = if (isDark) 0.dp else 2.dp) {
                                                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Add, null, tint = Accent, modifier = Modifier.size(24.dp))
                                                    Spacer(Modifier.width(12.dp))
                                                    Column {
                                                        Text("Добавить цель", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                                        Text("Отслеживайте свой прогресс", fontSize = 12.sp, color = TextMuted)
                                                    }
                                                }
                                            }
                                        } else {
                                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                gs.data.forEach { goal -> GoalCard(goal) { onGoalClick(goal) } }
                                            }
                                        }
                                    }
                                    is UiState.Error -> Text(gs.message, fontSize = 13.sp, color = Accent)
                                    else -> {}
                                }
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════
                // RESULTS
                // ═══════════════════════════════════════
                item {
                    Spacer(Modifier.height(20.dp))
                    FadeIn(visible = started, delayMs = 400) {
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            SectionHeader("Результаты")
                            Spacer(Modifier.height(10.dp))
                            when (val rs = myResults) {
                                is UiState.Success -> {
                                    if (rs.data.isEmpty()) {
                                        EmptyCard("Нет результатов", Icons.Default.Scoreboard)
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            rs.data.take(5).forEach { r -> ResultCard(r) }
                                        }
                                    }
                                }
                                is UiState.Error -> EmptyCard(rs.message, Icons.Default.Error)
                                is UiState.Loading -> {}
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════
                // RATING BY SPORT
                // ═══════════════════════════════════════
                // Filter stats to only user's sports
                val userSportIds = userSports.mapNotNull { it.sportId }
                val filteredStats = stats.filter { it.sportId in userSportIds || userSportIds.isEmpty() }
                if (filteredStats.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(20.dp))
                        FadeIn(visible = started, delayMs = 450) {
                            Column {
                                Row(Modifier.padding(horizontal = 16.dp)) {
                                    SectionHeader("Рейтинг по спорту")
                                }
                                Spacer(Modifier.height(10.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(filteredStats) { sportStat ->
                                        SportRatingCard(sportStat)
                                    }
                                }
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════
                // TEAM (if any)
                // ═══════════════════════════════════════
                val team = myTeam
                if (team != null) {
                    item {
                        Spacer(Modifier.height(20.dp))
                        FadeIn(visible = started, delayMs = 500) {
                            Column(Modifier.padding(horizontal = 16.dp)) {
                                SectionHeader("Команда")
                                Spacer(Modifier.height(10.dp))
                                TeamCard(team)
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════
                // NAVIGATION MENU
                // ═══════════════════════════════════════
                item {
                    Spacer(Modifier.height(24.dp))
                    FadeIn(visible = started, delayMs = 520) {
                        Surface(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp), color = CardBg,
                            shadowElevation = if (isDark) 0.dp else 2.dp
                        ) {
                            Column {
                                // My Tickets (viewer only)
                                if (user.role == UserRole.USER) {
                                    MenuRow(icon = Icons.Default.ConfirmationNumber, label = "Мои билеты", onClick = onTickets)
                                    MenuDivider()
                                }
                                // My Articles (media only)
                                if (user.role == UserRole.MEDIA) {
                                    MenuRow(icon = Icons.AutoMirrored.Filled.Article, label = "Мои статьи", onClick = onArticles)
                                    MenuDivider()
                                }
                                // Notifications
                                MenuRow(icon = Icons.Default.Notifications, label = "Уведомления", onClick = onNotifications)
                                MenuDivider()
                                // Settings
                                MenuRow(icon = Icons.Default.Settings, label = "Настройки", onClick = { showPrivacySheet = true })
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════
                // LEGAL + SIGN OUT
                // ═══════════════════════════════════════
                item {
                    Spacer(Modifier.height(12.dp))
                    FadeIn(visible = started, delayMs = 560) {
                        Surface(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp), color = CardBg,
                            shadowElevation = if (isDark) 0.dp else 2.dp
                        ) {
                            Column {
                                MenuRow(icon = Icons.Default.Shield, label = "Конфиденциальность", onClick = { showLegalSheet = true })
                                MenuDivider()
                                MenuRow(icon = Icons.Default.Info, label = "О приложении", onClick = { showLegalSheet = true })
                                MenuDivider()
                                // Sign out (red)
                                Row(
                                    Modifier.fillMaxWidth().clickable { showSignOutDialog = true }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = ILeaderColors.Error, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(14.dp))
                                    Text("Выйти", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = ILeaderColors.Error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Выход", color = TextPrimary) },
            text = { Text("Вы уверены, что хотите выйти?", color = TextSecondary) },
            confirmButton = { TextButton(onClick = { showSignOutDialog = false; onSignOut() }) { Text("Выйти", color = ILeaderColors.Error) } },
            dismissButton = { TextButton(onClick = { showSignOutDialog = false }) { Text("Отмена", color = TextMuted) } }
        )
    }
    if (showSportSheet) SportSelectionSheet(user.id) { showSportSheet = false }
    if (showPrivacySheet) SettingsSheet { showPrivacySheet = false }
    if (showLegalSheet) LegalSheet { showLegalSheet = false }
}

// ═══════════════════════════════════════════════════════════
// STATIC AVATAR with gradient border
// ═══════════════════════════════════════════════════════════

@Composable
private fun ProfileAvatar(avatarUrl: String?, initials: String, sportName: String) {
    val sColor = sportColor(sportName)
    val borderColors = listOf(sColor, sColor.copy(0.5f), Accent, sColor)

    Box(contentAlignment = Alignment.Center) {
        // Gradient border ring
        Box(
            Modifier
                .size(112.dp)
                .background(Brush.sweepGradient(borderColors), CircleShape)
        )
        // Background gap
        Box(Modifier.size(106.dp).clip(CircleShape).background(Bg))
        // Avatar
        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape).background(CardBg),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                AsyncImage(avatarUrl, null, Modifier.size(100.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.size(100.dp).clip(CircleShape).background(Accent), contentAlignment = Alignment.Center) {
                    Text(initials, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// ANIMATED STAT
// ═══════════════════════════════════════════════════════════

@Composable
private fun AnimatedStatItem(icon: ImageVector, targetValue: Int, label: String, started: Boolean, modifier: Modifier = Modifier) {
    val v by animateFloatAsState(if (started) targetValue.toFloat() else 0f, tween(900, 400, FastOutSlowInEasing), label = "s_$label")
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(6.dp))
        Text("${v.roundToInt()}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}

// ═══════════════════════════════════════════════════════════
// TOURNAMENT CARD (horizontal carousel)
// ═══════════════════════════════════════════════════════════

@Composable
private fun TournamentCard(t: TournamentWithCountsDto, onClick: () -> Unit) {
    val isDark = DarkTheme.isDark
    val sportName = t.sportName ?: ""
    val imgUrl = sportImageUrl(sportName)

    Surface(
        Modifier.width(220.dp).clickable(onClick = onClick),
        RoundedCornerShape(16.dp), CardBg,
        shadowElevation = if (isDark) 0.dp else 3.dp
    ) {
        Column {
            // Image header
            Box(Modifier.fillMaxWidth().height(90.dp)) {
                if (imgUrl != null) {
                    AsyncImage(imgUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.4f)))
                } else {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(sportColor(sportName).copy(0.8f), sportColor(sportName).copy(0.4f)))))
                }
                // Sport badge
                Surface(
                    Modifier.align(Alignment.TopStart).padding(8.dp),
                    RoundedCornerShape(50), Color.White.copy(0.2f)
                ) {
                    Text("${sportEmoji(sportName)} $sportName", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                // Status
                val statusText = when (t.status) {
                    "registration_open" -> "Регистрация"
                    "in_progress" -> "Идёт"
                    "completed" -> "Завершён"
                    else -> t.status ?: ""
                }
                if (statusText.isNotEmpty()) {
                    Surface(
                        Modifier.align(Alignment.TopEnd).padding(8.dp),
                        RoundedCornerShape(50), Color.Black.copy(0.5f)
                    ) {
                        Text(statusText, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, color = Color.White)
                    }
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(formatShortDate(t.startDate), fontSize = 12.sp, color = TextMuted)
                }
                if (t.locationName != null) {
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(t.locationName, fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// RESULT CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun ResultCard(r: ResultDto) {
    val isDark = DarkTheme.isDark
    val posEmoji = when (r.position) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "#${r.position}" }
    val sportName = r.tournaments?.sports?.name ?: ""

    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), CardBg, shadowElevation = if (isDark) 0.dp else 2.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Position
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (r.position <= 3) Accent.copy(0.15f) else Border.copy(0.2f)),
                Alignment.Center
            ) {
                Text(posEmoji, fontSize = if (r.position <= 3) 20.sp else 14.sp, fontWeight = FontWeight.Bold,
                    color = if (r.position <= 3) Accent else TextMuted)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(r.tournaments?.name ?: "Турнир", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sportName.isNotEmpty()) {
                        Text("${sportEmoji(sportName)} $sportName", fontSize = 12.sp, color = TextMuted)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(formatShortDate(r.tournaments?.startDate), fontSize = 12.sp, color = TextMuted)
                }
            }
            if (r.points != null && r.points > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("${r.points}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Accent)
                    Text("очки", fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SPORT RATING CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun SportRatingCard(stat: UserSportStatsDto) {
    val isDark = DarkTheme.isDark
    val name = stat.sportName ?: ""
    val color = sportColor(name)

    Surface(Modifier.width(160.dp), RoundedCornerShape(16.dp), CardBg, shadowElevation = if (isDark) 0.dp else 2.dp) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(0.12f)), contentAlignment = Alignment.Center) {
                    Text(sportEmoji(name), fontSize = 18.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Text("${stat.rating}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${stat.tournaments} турн.", fontSize = 12.sp, color = TextMuted)
                Spacer(Modifier.width(8.dp))
                Text("${stat.wins} поб.", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// TEAM CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun TeamCard(membership: TeamMembershipDto) {
    val isDark = DarkTheme.isDark
    val team = membership.teams ?: return
    val sportName = team.sports?.name ?: ""
    val roleName = when (membership.role) { "captain" -> "Капитан"; "member" -> "Участник"; "reserve" -> "Запасной"; else -> membership.role ?: "" }

    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), CardBg, shadowElevation = if (isDark) 0.dp else 2.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).clip(RoundedCornerShape(14.dp)).background(sportColor(sportName).copy(0.1f)), contentAlignment = Alignment.Center) {
                Text(sportEmoji(sportName), fontSize = 24.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(team.name ?: "Команда", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sportName.isNotEmpty()) {
                        Text(sportName, fontSize = 12.sp, color = TextMuted)
                        Spacer(Modifier.width(8.dp))
                    }
                    if (team.city != null) Text(team.city, fontSize = 12.sp, color = TextMuted)
                }
            }
            Surface(shape = RoundedCornerShape(50), color = Accent.copy(0.18f)) {
                Text(roleName, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Accent)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// MENU ROW (iOS-style, grey icons)
// ═══════════════════════════════════════════════════════════

@Composable
private fun MenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(
        color = Border.copy(0.2f), thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

// ═══════════════════════════════════════════════════════════
// COMPACT ACTION BUTTON
// ═══════════════════════════════════════════════════════════

@Composable
private fun CompactActionButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current
    Surface(modifier.clickable(onClick = onClick), RoundedCornerShape(14.dp), CardBg, shadowElevation = if (isDark) 0.dp else 2.dp) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(colors.accentSoft), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// EMPTY STATE CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun EmptyCard(text: String, icon: ImageVector) {
    val isDark = DarkTheme.isDark
    Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), RoundedCornerShape(14.dp), CardBg, shadowElevation = if (isDark) 0.dp else 2.dp) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TextMuted, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, fontSize = 14.sp, color = TextMuted)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// GOAL CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun GoalCard(goal: AthleteGoal, onClick: () -> Unit = {}) {
    val isDark = DarkTheme.isDark
    val progress = if (goal.targetValue > 0) (goal.currentValue.toFloat() / goal.targetValue).coerceIn(0f, 1f) else 0f
    val statusColor = when (goal.status) { GoalStatus.COMPLETED -> Color(0xFF22C55E); GoalStatus.FAILED -> Color(0xFFEF4444); GoalStatus.ACTIVE -> Accent }
    val progressColor = when (goal.status) { GoalStatus.COMPLETED -> Color(0xFF22C55E); GoalStatus.FAILED -> Color(0xFFEF4444); GoalStatus.ACTIVE -> Color(0xFF3B82F6) }

    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(16.dp), CardBg, shadowElevation = if (isDark) 0.dp else 2.dp) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(goal.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                    Text(goal.status.displayName, Modifier.padding(horizontal = 10.dp, vertical = 3.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                }
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress }, Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = progressColor, trackColor = progressColor.copy(0.12f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Spacer(Modifier.height(8.dp))
            Text("${goal.currentValue}/${goal.targetValue} ${goal.type.displayName.lowercase()}", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SETTINGS SHEET (Theme + Language + Privacy)
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(onDismiss: () -> Unit) {
    var pages by remember { mutableStateOf<List<LegalPageDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showLegal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try { pages = ViewerRepository().getLegalPages().filter { it.enabled } } catch (_: Exception) {}
        loading = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!showLegal) {
                Text("Настройки", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                ThemeSwitcherCard()
                LanguageSwitcherCard()
                SettingsRow(Icons.Default.Shield, "Конфиденциальность и условия") { showLegal = true }
            } else {
                // Legal content
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { showLegal = false }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Конфиденциальность и условия", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(Modifier.height(12.dp))
                if (loading) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Accent, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    }
                } else if (pages.isEmpty()) {
                    Text("Содержимое будет добавлено позже.", fontSize = 14.sp, color = TextMuted)
                } else {
                    pages.forEach { page ->
                        Text(page.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        RenderLegalContent(page.content)
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(14.dp), CardBg, shadowElevation = if (isDark) 0.dp else 2.dp) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(colors.accentSoft), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalSheet(onDismiss: () -> Unit) {
    var pages by remember { mutableStateOf<List<LegalPageDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try { pages = ViewerRepository().getLegalPages().filter { it.enabled } } catch (_: Exception) {}
        loading = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())
        ) {
            Text("Конфиденциальность и условия", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(16.dp))
            if (loading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                }
            } else if (pages.isEmpty()) {
                Text("Содержимое будет добавлено позже.", fontSize = 14.sp, color = TextMuted)
            } else {
                pages.forEach { page ->
                    Text(page.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    RenderLegalContent(page.content)
                    Spacer(Modifier.height(24.dp))
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RenderLegalContent(content: String) {
    content.split("\n").forEach { line ->
        val trimmed = line.trim()
        when {
            trimmed.isEmpty() -> Spacer(Modifier.height(8.dp))
            trimmed.startsWith("## ") -> { Spacer(Modifier.height(12.dp)); Text(trimmed.removePrefix("## "), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Spacer(Modifier.height(4.dp)) }
            trimmed.startsWith("### ") -> { Spacer(Modifier.height(8.dp)); Text(trimmed.removePrefix("### "), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary); Spacer(Modifier.height(2.dp)) }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> Row(Modifier.padding(start = 12.dp)) { Text("•", fontSize = 14.sp, color = TextSecondary); Spacer(Modifier.width(8.dp)); Text(trimmed.removePrefix("- ").removePrefix("* "), fontSize = 14.sp, color = TextSecondary, lineHeight = 20.sp) }
            else -> Text(trimmed, fontSize = 14.sp, color = TextSecondary, lineHeight = 20.sp)
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
        try { allSports = ViewerRepository().getSports(); selectedIds.addAll(currentIds) } catch (_: Exception) {}
        loading = false
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = CardBg, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Виды спорта", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Выберите 1-3 вида спорта", fontSize = 13.sp, color = TextMuted)
            Spacer(Modifier.height(16.dp))
            if (loading) { LoadingScreen() } else {
                allSports.forEach { sport ->
                    val sel = selectedIds.contains(sport.id)
                    Surface(Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { if (sel) selectedIds.remove(sport.id) else if (selectedIds.size < 3) selectedIds.add(sport.id) }, RoundedCornerShape(12.dp), if (sel) Accent.copy(0.15f) else Color.Transparent) {
                        Text(sport.name, Modifier.padding(horizontal = 14.dp, vertical = 12.dp), fontSize = 15.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal, color = if (sel) Accent else TextPrimary)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Surface(Modifier.fillMaxWidth().clickable(enabled = selectedIds.isNotEmpty()) {
                    scope.launch { val s = allSports.filter { selectedIds.contains(it.id) }; if (s.isNotEmpty()) sportPref.setSports(s.map { it.id }, s.map { it.name }); onDismiss() }
                }, RoundedCornerShape(12.dp), if (selectedIds.isNotEmpty()) Accent else TextMuted) {
                    Text("Сохранить", Modifier.padding(vertical = 14.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
