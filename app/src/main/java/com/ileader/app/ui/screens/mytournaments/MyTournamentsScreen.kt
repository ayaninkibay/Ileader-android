package com.ileader.app.ui.screens.mytournaments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.AthleteGoal
import com.ileader.app.data.models.GoalStatus
import com.ileader.app.data.models.RefereeTournament
import com.ileader.app.data.models.Tournament
import com.ileader.app.data.models.TournamentStatus
import com.ileader.app.data.models.User
import com.ileader.app.data.models.UserRole
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.MediaInviteFullDto
import com.ileader.app.data.remote.dto.SpectatorDto
import com.ileader.app.data.remote.dto.TournamentHelperDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.TrainerRepository
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.EmptyState
import com.ileader.app.ui.components.ErrorScreen
import com.ileader.app.ui.components.FadeIn
import com.ileader.app.ui.components.LoadingScreen
import com.ileader.app.ui.components.StatusBadge
import com.ileader.app.ui.components.sportEmoji
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.MyTournamentsViewModel

// ── Palette aliases ──
private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val Border: Color @Composable get() = LocalAppColors.current.border

@Composable
fun MyTournamentsScreen(
    user: User,
    onTournamentClick: (String) -> Unit,
    onQrScan: (String, String) -> Unit,
    onManualCheckIn: (String, String) -> Unit,
    onEditTournament: (String) -> Unit = {},
    onHelperManagement: (String, String) -> Unit = { _, _ -> }
) {
    val vm: MyTournamentsViewModel = viewModel()
    val roleTournaments by vm.roleTournaments.collectAsState()
    val goals by vm.goals.collectAsState()
    val helperAssignments by vm.helperAssignments.collectAsState()

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(user.id) {
        vm.load(user.id, user.role)
        started = true
    }

    // Scale animation for the gradient header
    val headerScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.95f,
        animationSpec = tween(500, easing = EaseOutBack),
        label = "headerScale"
    )
    val headerAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400),
        label = "headerAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        when (val state = roleTournaments) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(
                message = state.message,
                onRetry = { vm.load(user.id, user.role) }
            )
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Gradient header ──
                    item {
                        Box(
                            modifier = Modifier.graphicsLayer {
                                scaleX = headerScale
                                scaleY = headerScale
                                alpha = headerAlpha
                            }
                        ) {
                            GradientHeader(user = user)
                        }
                    }

                    // ── Role-specific section ──
                    item {
                        FadeIn(visible = started, delayMs = 100) {
                            Column(Modifier.padding(horizontal = 20.dp)) {
                                RoleSection(
                                    user = user,
                                    data = state.data,
                                    onTournamentClick = onTournamentClick,
                                    onQrScan = onQrScan,
                                    onManualCheckIn = onManualCheckIn,
                                    onEditTournament = onEditTournament,
                                    onHelperManagement = onHelperManagement
                                )
                            }
                        }
                    }

                    // ── Goals section (athlete only) ──
                    if (user.role == UserRole.ATHLETE && goals != null) {
                        item {
                            FadeIn(visible = started, delayMs = 250) {
                                Column(Modifier.padding(horizontal = 20.dp)) {
                                    GoalsSection(goalsState = goals ?: UiState.Loading)
                                }
                            }
                        }
                    }

                    // ── Helper section (all roles) ──
                    val helpersState = helperAssignments
                    if (helpersState is UiState.Success && helpersState.data.isNotEmpty()) {
                        item {
                            FadeIn(visible = started, delayMs = 400) {
                                Column(Modifier.padding(horizontal = 20.dp)) {
                                    HelperSection(
                                        assignments = helpersState.data,
                                        onQrScan = onQrScan,
                                        onManualCheckIn = onManualCheckIn
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Gradient header
// ══════════════════════════════════════════════════════════

@Composable
private fun GradientHeader(user: User) {
    val subtitle = when (user.role) {
        UserRole.ATHLETE -> "Ваши турниры и цели"
        UserRole.TRAINER -> "Турниры вашей команды"
        UserRole.ORGANIZER -> "Управление турнирами"
        UserRole.REFEREE -> "Назначенные турниры"
        UserRole.MEDIA -> "Аккредитации и события"
        UserRole.SPONSOR -> "Спонсируемые турниры"
        UserRole.ADMIN -> "Все турниры платформы"
        UserRole.CONTENT_MANAGER -> "Турниры и контент"
        UserRole.USER -> "Турниры зрителя"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFE53535),
                        Color(0xFFFF6B6B)
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Text(
                "Мои турниры",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
// Role section dispatcher
// ══════════════════════════════════════════════════════════

@Composable
private fun RoleSection(
    user: User,
    data: List<Any>,
    onTournamentClick: (String) -> Unit,
    onQrScan: (String, String) -> Unit,
    onManualCheckIn: (String, String) -> Unit,
    onEditTournament: (String) -> Unit = {},
    onHelperManagement: (String, String) -> Unit = { _, _ -> }
) {
    when (user.role) {
        UserRole.USER, UserRole.SPONSOR, UserRole.ADMIN, UserRole.CONTENT_MANAGER -> {
            SpectatorSection(data)
        }
        UserRole.ATHLETE -> {
            @Suppress("UNCHECKED_CAST")
            AthleteTournamentsSection(
                tournaments = data as? List<Tournament> ?: emptyList(),
                onTournamentClick = onTournamentClick
            )
        }
        UserRole.TRAINER -> {
            TrainerSection(data)
        }
        UserRole.ORGANIZER -> {
            @Suppress("UNCHECKED_CAST")
            OrganizerSection(
                tournaments = data as? List<TournamentWithCountsDto> ?: emptyList(),
                onTournamentClick = onTournamentClick,
                onQrScan = onQrScan,
                onManualCheckIn = onManualCheckIn,
                onEditTournament = onEditTournament,
                onHelperManagement = onHelperManagement
            )
        }
        UserRole.REFEREE -> {
            @Suppress("UNCHECKED_CAST")
            RefereeSection(
                tournaments = data as? List<RefereeTournament> ?: emptyList(),
                onTournamentClick = onTournamentClick
            )
        }
        UserRole.MEDIA -> {
            @Suppress("UNCHECKED_CAST")
            MediaSection(invites = data as? List<MediaInviteFullDto> ?: emptyList())
        }
    }
}

// ══════════════════════════════════════════════════════════
// Pill badge composable
// ══════════════════════════════════════════════════════════

@Composable
private fun PillBadge(
    text: String,
    bgColor: Color,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun SportPill(sportName: String) {
    val emoji = sportEmoji(sportName)
    PillBadge(
        text = "$emoji $sportName",
        bgColor = Accent.copy(alpha = 0.12f),
        textColor = Accent
    )
}

@Composable
private fun StatusPill(status: String) {
    val label = getStatusLabel(status)
    val color = getStatusColor(status)
    PillBadge(
        text = label,
        bgColor = color.copy(alpha = 0.15f),
        textColor = color
    )
}

// ══════════════════════════════════════════════════════════
// USER / SPONSOR / ADMIN / CONTENT_MANAGER — Spectator
// ══════════════════════════════════════════════════════════

@Composable
private fun SpectatorSection(data: List<Any>) {
    @Suppress("UNCHECKED_CAST")
    val registrations = data as? List<SpectatorDto> ?: emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Регистрации", Icons.Default.EmojiEvents)

        if (registrations.isEmpty()) {
            EmptyState(
                title = "Нет регистраций",
                subtitle = "Вы пока не зарегистрированы как зритель"
            )
        } else {
            registrations.forEach { reg ->
                SpectatorCard(reg)
            }
        }
    }
}

@Composable
private fun SpectatorCard(spectator: SpectatorDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Accent, AccentDark))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Groups,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Турнир",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                val checkInLabel = when (spectator.checkInStatus) {
                    "checked_in" -> "Check-in выполнен"
                    "pending" -> "Ожидает check-in"
                    else -> "Зарегистрирован"
                }
                val checkInColor = when (spectator.checkInStatus) {
                    "checked_in" -> Color(0xFF22C55E)
                    "pending" -> Color(0xFFF59E0B)
                    else -> TextMuted
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PillBadge(
                        text = spectator.ticketType,
                        bgColor = Accent.copy(alpha = 0.12f),
                        textColor = Accent
                    )
                    Spacer(Modifier.width(8.dp))
                    PillBadge(
                        text = checkInLabel,
                        bgColor = checkInColor.copy(alpha = 0.12f),
                        textColor = checkInColor
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// ATHLETE
// ══════════════════════════════════════════════════════════

@Composable
private fun AthleteTournamentsSection(
    tournaments: List<Tournament>,
    onTournamentClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Турниры", Icons.Default.EmojiEvents)

        if (tournaments.isEmpty()) {
            EmptyState(
                title = "Нет турниров",
                subtitle = "Вы пока не зарегистрированы на турниры"
            )
        } else {
            tournaments.forEachIndexed { index, tournament ->
                val delay = (index * 60).coerceAtMost(500)
                var itemVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(delay.toLong())
                    itemVisible = true
                }
                val itemAlpha by animateFloatAsState(
                    targetValue = if (itemVisible) 1f else 0f,
                    animationSpec = tween(350),
                    label = "athTournAlpha$index"
                )
                val itemOffset by animateFloatAsState(
                    targetValue = if (itemVisible) 0f else 40f,
                    animationSpec = tween(350, easing = EaseOutBack),
                    label = "athTournOffset$index"
                )
                Box(
                    modifier = Modifier.graphicsLayer {
                        alpha = itemAlpha
                        translationY = itemOffset
                    }
                ) {
                    TournamentCard(
                        name = tournament.name,
                        sport = tournament.sportName,
                        date = tournament.startDate,
                        location = tournament.location,
                        status = tournament.status.name.lowercase(),
                        onClick = { onTournamentClick(tournament.id) }
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// TRAINER
// ══════════════════════════════════════════════════════════

@Composable
private fun TrainerSection(data: List<Any>) {
    @Suppress("UNCHECKED_CAST")
    val teams = data as? List<com.ileader.app.data.repository.TrainerTeamData> ?: emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Турниры команды", Icons.Default.People)

        if (teams.isEmpty()) {
            EmptyState(
                title = "Нет команд",
                subtitle = "У вас пока нет команд"
            )
        } else {
            teams.forEach { team ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg,
                    shadowElevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(Accent, AccentDark))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                team.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SportPill(team.sportName)
                                PillBadge(
                                    text = "${team.members.size} участников",
                                    bgColor = Color(0xFF3B82F6).copy(alpha = 0.12f),
                                    textColor = Color(0xFF3B82F6)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// ORGANIZER
// ══════════════════════════════════════════════════════════

@Composable
private fun OrganizerSection(
    tournaments: List<TournamentWithCountsDto>,
    onTournamentClick: (String) -> Unit,
    onQrScan: (String, String) -> Unit,
    onManualCheckIn: (String, String) -> Unit,
    onEditTournament: (String) -> Unit = {},
    onHelperManagement: (String, String) -> Unit = { _, _ -> }
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Организованные", Icons.Default.EmojiEvents)

        if (tournaments.isEmpty()) {
            EmptyState(
                title = "Нет турниров",
                subtitle = "У вас пока нет организованных турниров"
            )
        } else {
            tournaments.forEachIndexed { index, t ->
                val delay = (index * 60).coerceAtMost(500)
                var itemVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(delay.toLong())
                    itemVisible = true
                }
                val itemAlpha by animateFloatAsState(
                    targetValue = if (itemVisible) 1f else 0f,
                    animationSpec = tween(350),
                    label = "orgTournAlpha$index"
                )
                val itemOffset by animateFloatAsState(
                    targetValue = if (itemVisible) 0f else 40f,
                    animationSpec = tween(350, easing = EaseOutBack),
                    label = "orgTournOffset$index"
                )
                Box(
                    modifier = Modifier.graphicsLayer {
                        alpha = itemAlpha
                        translationY = itemOffset
                    }
                ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTournamentClick(t.id) },
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg,
                    shadowElevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        // Top row: pills + action icons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (t.sportName != null) {
                                    SportPill(t.sportName)
                                }
                                StatusPill(t.status ?: "")
                            }
                            IconButton(
                                onClick = { onEditTournament(t.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Редактировать",
                                    tint = Accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onManualCheckIn(t.id, t.name) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Ручной check-in",
                                    tint = Accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onQrScan(t.id, t.name) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = "QR",
                                    tint = Accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onHelperManagement(t.id, t.name) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = "Помощники",
                                    tint = Accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Title
                        Text(
                            t.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(10.dp))

                        // Info rows
                        InfoRow(
                            icon = Icons.Default.CalendarMonth,
                            text = formatDateRu(t.startDate ?: "")
                        )
                        Spacer(Modifier.height(4.dp))
                        if (t.locationName != null) {
                            InfoRow(
                                icon = Icons.Default.LocationOn,
                                text = t.locationName
                            )
                            Spacer(Modifier.height(4.dp))
                        }

                        // Participant count bar
                        Spacer(Modifier.height(8.dp))
                        val maxP = t.maxParticipants ?: 0
                        val progress = if (maxP > 0) {
                            (t.participantCount.toFloat() / maxP).coerceIn(0f, 1f)
                        } else 0f

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${t.participantCount}/${t.maxParticipants ?: "∞"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            if (maxP > 0) {
                                Spacer(Modifier.width(10.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Accent,
                                    trackColor = Accent.copy(alpha = 0.12f),
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
                } // close Box wrapper
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// REFEREE
// ══════════════════════════════════════════════════════════

@Composable
private fun RefereeSection(
    tournaments: List<RefereeTournament>,
    onTournamentClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Назначения", Icons.Default.Gavel)

        if (tournaments.isEmpty()) {
            EmptyState(
                title = "Нет назначений",
                subtitle = "Вы пока не назначены на турниры"
            )
        } else {
            tournaments.forEachIndexed { index, t ->
                val delay = (index * 60).coerceAtMost(500)
                var itemVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(delay.toLong())
                    itemVisible = true
                }
                val itemAlpha by animateFloatAsState(
                    targetValue = if (itemVisible) 1f else 0f,
                    animationSpec = tween(350),
                    label = "refTournAlpha$index"
                )
                val itemOffset by animateFloatAsState(
                    targetValue = if (itemVisible) 0f else 40f,
                    animationSpec = tween(350, easing = EaseOutBack),
                    label = "refTournOffset$index"
                )
                Box(
                    modifier = Modifier.graphicsLayer {
                        alpha = itemAlpha
                        translationY = itemOffset
                    }
                ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTournamentClick(t.id) },
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg,
                    shadowElevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        // Pills row
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SportPill(t.sport)
                            StatusPill(t.status.name.lowercase())
                        }

                        Spacer(Modifier.height(10.dp))

                        // Title
                        Text(
                            t.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(10.dp))

                        // Info
                        InfoRow(
                            icon = Icons.Default.CalendarMonth,
                            text = formatDateRu(t.date)
                        )
                        Spacer(Modifier.height(4.dp))
                        if (t.location.isNotEmpty()) {
                            InfoRow(
                                icon = Icons.Default.LocationOn,
                                text = t.location
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        InfoRow(
                            icon = Icons.Default.SportsScore,
                            text = t.refereeRole.label
                        )

                        // Matches progress
                        if (t.matchesTotal > 0) {
                            Spacer(Modifier.height(10.dp))
                            val matchProgress = (t.matchesCompleted.toFloat() / t.matchesTotal).coerceIn(0f, 1f)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Матчи: ${t.matchesCompleted}/${t.matchesTotal}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Spacer(Modifier.width(10.dp))
                                LinearProgressIndicator(
                                    progress = { matchProgress },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF3B82F6),
                                    trackColor = Color(0xFF3B82F6).copy(alpha = 0.12f),
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
                } // close Box wrapper
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// MEDIA
// ══════════════════════════════════════════════════════════

@Composable
private fun MediaSection(invites: List<MediaInviteFullDto>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Аккредитации", Icons.Default.CalendarMonth)

        if (invites.isEmpty()) {
            EmptyState(
                title = "Нет аккредитаций",
                subtitle = "У вас пока нет аккредитаций на турниры"
            )
        } else {
            invites.forEach { invite ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg,
                    shadowElevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(Accent, AccentDark))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                invite.tournaments?.name ?: "Турнир",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            val statusLabel = when (invite.status) {
                                "accepted" -> "Одобрено"
                                "declined" -> "Отклонено"
                                "pending" -> "На рассмотрении"
                                else -> invite.status ?: ""
                            }
                            val statusColor = when (invite.status) {
                                "accepted" -> Color(0xFF22C55E)
                                "declined" -> Color(0xFFEF4444)
                                "pending" -> Color(0xFFF59E0B)
                                else -> TextMuted
                            }
                            PillBadge(
                                text = statusLabel,
                                bgColor = statusColor.copy(alpha = 0.12f),
                                textColor = statusColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// GOALS (Athlete)
// ══════════════════════════════════════════════════════════

@Composable
private fun GoalsSection(goalsState: UiState<List<AthleteGoal>>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Мои цели", Icons.Default.Flag)

        when (goalsState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Загрузка целей...", color = TextMuted, fontSize = 13.sp)
                }
            }
            is UiState.Error -> {
                Text(goalsState.message, color = Accent, fontSize = 13.sp)
            }
            is UiState.Success -> {
                if (goalsState.data.isEmpty()) {
                    EmptyState(
                        title = "Нет целей",
                        subtitle = "Установите цели для отслеживания прогресса",
                        icon = Icons.Default.Flag
                    )
                } else {
                    goalsState.data.forEach { goal ->
                        GoalCard(goal)
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(goal: AthleteGoal) {
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
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    goal.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                PillBadge(
                    text = goal.status.displayName,
                    bgColor = statusColor.copy(alpha = 0.12f),
                    textColor = statusColor
                )
            }

            Spacer(Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.12f),
                strokeCap = StrokeCap.Round
            )

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${goal.currentValue}/${goal.targetValue} ${goal.type.displayName.lowercase()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                if (goal.deadline != null) {
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "до ${formatDateRu(goal.deadline)}",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// HELPER section
// ══════════════════════════════════════════════════════════

@Composable
private fun HelperSection(
    assignments: List<TournamentHelperDto>,
    onQrScan: (String, String) -> Unit,
    onManualCheckIn: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Помощник", Icons.Default.Groups)

        assignments.forEach { helper ->
            val tournament = helper.tournaments
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardBg,
                shadowElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Column(Modifier.padding(16.dp)) {
                    // Sport pill
                    if (tournament?.sports?.name != null) {
                        SportPill(tournament.sports.name)
                        Spacer(Modifier.height(10.dp))
                    }

                    // Title
                    Text(
                        tournament?.name ?: "Турнир",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (tournament?.startDate != null) {
                        Spacer(Modifier.height(8.dp))
                        InfoRow(
                            icon = Icons.Default.CalendarMonth,
                            text = formatDateRu(tournament.startDate)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        HelperActionButton(
                            text = "QR Check-in",
                            icon = Icons.Default.QrCodeScanner,
                            onClick = { onQrScan(helper.tournamentId, tournament?.name ?: "") },
                            modifier = Modifier.weight(1f)
                        )
                        HelperActionButton(
                            text = "Вручную",
                            icon = Icons.Default.Edit,
                            onClick = { onManualCheckIn(helper.tournamentId, tournament?.name ?: "") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HelperActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Accent.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Accent
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
// Shared tournament card
// ══════════════════════════════════════════════════════════

@Composable
private fun TournamentCard(
    name: String,
    sport: String,
    date: String,
    location: String = "",
    status: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Pills row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SportPill(sport)
                StatusPill(status)
            }

            Spacer(Modifier.height(10.dp))

            // Title
            Text(
                name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            // Info rows
            InfoRow(
                icon = Icons.Default.CalendarMonth,
                text = formatDateRu(date)
            )
            if (location.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    text = location
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Shared components
// ══════════════════════════════════════════════════════════

@Composable
private fun SectionTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

// ══════════════════════════════════════════════════════════
// Helpers
// ══════════════════════════════════════════════════════════

private fun formatDateRu(dateStr: String): String {
    val parts = dateStr.split("T")[0].split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2].toIntOrNull() ?: return dateStr
    val monthNames = listOf(
        "", "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    )
    val month = parts[1].toIntOrNull() ?: return dateStr
    return "$day ${monthNames.getOrElse(month) { "" }}"
}

private fun formatShortDate(dateStr: String): String {
    val parts = dateStr.split("T")[0].split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2]
    val monthShort = listOf(
        "", "янв", "фев", "мар", "апр", "май", "июн",
        "июл", "авг", "сен", "окт", "ноя", "дек"
    )
    val month = parts[1].toIntOrNull() ?: return dateStr
    return "$day\n${monthShort.getOrElse(month) { "" }}"
}

private fun getStatusLabel(status: String): String = when (status) {
    "in_progress" -> "Идёт"
    "registration_open" -> "Регистрация"
    "registration_closed" -> "Рег. закрыта"
    "check_in" -> "Check-in"
    "completed" -> "Завершён"
    "cancelled" -> "Отменён"
    "draft" -> "Черновик"
    else -> status
}

@Composable
private fun getStatusColor(status: String): Color = when (status) {
    "registration_open" -> Color(0xFF22C55E)
    "in_progress" -> Color(0xFF3B82F6)
    "check_in" -> Color(0xFFF59E0B)
    "completed" -> TextSecondary
    "cancelled" -> Color(0xFFEF4444)
    "draft" -> Color(0xFF8B5CF6)
    else -> TextMuted
}
