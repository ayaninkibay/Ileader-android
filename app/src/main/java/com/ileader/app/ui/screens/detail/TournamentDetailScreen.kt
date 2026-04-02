package com.ileader.app.ui.screens.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.bracket.BracketUtils
import com.ileader.app.data.models.BracketMatch
import com.ileader.app.data.models.User
import com.ileader.app.data.models.UserRole
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.LocationDto
import com.ileader.app.data.remote.dto.ParticipantDto
import com.ileader.app.data.remote.dto.ResultDto
import com.ileader.app.data.remote.dto.ScheduleItemDto
import com.ileader.app.data.remote.dto.TournamentDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.components.bracket.BracketView
import com.ileader.app.ui.components.bracket.MatchDetailDialog
import com.ileader.app.ui.viewmodels.RegistrationState
import com.ileader.app.ui.viewmodels.HomeTournamentDetailData
import com.ileader.app.ui.viewmodels.SportViewModel
import com.ileader.app.ui.viewmodels.TournamentDetailViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun TournamentDetailScreen(
    tournamentId: String,
    user: User,
    onBack: () -> Unit,
    onEditTournament: (String) -> Unit = {},
    viewModel: TournamentDetailViewModel = viewModel()
) {
    LaunchedEffect(tournamentId) {
        viewModel.load(tournamentId)
        val roleName = user.role.name.lowercase()
        viewModel.checkRegistration(tournamentId, user.id, roleName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        when (val state = viewModel.state) {
            is UiState.Loading -> {
                BackHeader(title = "Турнир", onBack = onBack)
                LoadingScreen()
            }
            is UiState.Error -> {
                BackHeader(title = "Турнир", onBack = onBack)
                ErrorScreen(state.message, onRetry = { viewModel.load(tournamentId) })
            }
            is UiState.Success -> {
                val data = state.data
                val tournament = data.tournament

                TournamentContent(
                    data = data,
                    user = user,
                    viewModel = viewModel,
                    onBack = onBack,
                    onEditTournament = onEditTournament
                )
            }
        }
    }
}

@Composable
private fun TournamentContent(
    data: HomeTournamentDetailData,
    user: User,
    viewModel: TournamentDetailViewModel,
    onBack: () -> Unit,
    onEditTournament: (String) -> Unit = {}
) {
    val tournament = data.tournament
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    // Hoist colors for non-composable scopes
    val accentColor = Accent
    val accentDarkColor = AccentDark

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero Header with photo ──
            FadeIn(visible = started, delayMs = 0) {
                val sportName = tournament.sports?.name ?: ""
                val heroColor = if (sportName.isNotEmpty()) sportColor(sportName) else accentColor
                val heroImage = tournament.imageUrl
                    ?: tournament.sports?.let { SportViewModel.getFallbackImage(it) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                ) {
                    // Background: photo or sport gradient
                    if (heroImage != null) {
                        AsyncImage(
                            model = heroImage,
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            Modifier.matchParentSize().background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.7f))
                                )
                            )
                        )
                    } else {
                        Box(
                            Modifier.matchParentSize().background(
                                Brush.linearGradient(
                                    listOf(heroColor.copy(alpha = 0.9f), heroColor.copy(alpha = 0.5f))
                                )
                            )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp)
                            .padding(top = 12.dp, bottom = 28.dp)
                    ) {
                        // Top row: back + share
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .clickable { onBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack, "Назад",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.weight(1f))

                            // Favorite button
                            val favContext = androidx.compose.ui.platform.LocalContext.current
                            val favPref = remember { com.ileader.app.data.preferences.FavoritesPreference(favContext) }
                            val favIds by favPref.favoriteTournamentIds.collectAsState(initial = emptyList())
                            val isFav = tournament.id in favIds
                            val favScale by animateFloatAsState(
                                targetValue = if (isFav) 1f else 0.9f,
                                animationSpec = tween(200), label = "favScale"
                            )
                            val scope = rememberCoroutineScope()

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .scale(favScale)
                                    .clip(CircleShape)
                                    .background(if (isFav) Accent.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.3f))
                                    .clickable { scope.launch { favPref.toggleFavorite(tournament.id) } },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isFav) "Убрать из избранного" else "В избранное",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            // Share button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Share, null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Status + sport pills
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tournament.status?.let { status ->
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = getStatusLabel(status),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }
                            tournament.sports?.name?.let { sportName ->
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(sportIcon(sportName), null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(sportName, fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Tournament name
                        Text(
                            text = tournament.name,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 32.sp,
                            letterSpacing = (-0.5).sp
                        )

                        // Organizer
                        tournament.profiles?.name?.let { org ->
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Business, null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = org,
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Quick Info Cards ──
            FadeIn(visible = started, delayMs = 150) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickInfoCard(
                        icon = Icons.Default.CalendarMonth,
                        label = "Дата",
                        value = formatDateShort(tournament.startDate),
                        modifier = Modifier.width(100.dp)
                    )
                    QuickInfoCard(
                        icon = Icons.Default.LocationOn,
                        label = "Место",
                        value = tournament.locations?.name ?: "—",
                        modifier = Modifier.width(100.dp)
                    )
                    QuickInfoCard(
                        icon = Icons.Default.People,
                        label = "Участники",
                        value = "${data.participants.size}/${tournament.maxParticipants ?: "∞"}",
                        modifier = Modifier.width(100.dp)
                    )
                    QuickInfoCard(
                        icon = Icons.Default.EmojiEvents,
                        label = "Приз",
                        value = if (!tournament.prize.isNullOrEmpty()) tournament.prize else "—",
                        modifier = Modifier.width(100.dp)
                    )
                    QuickInfoCard(
                        icon = Icons.Default.AccountTree,
                        label = "Формат",
                        value = formatShortLabel(tournament.format),
                        modifier = Modifier.width(100.dp)
                    )
                }
            }

            // ── Description ──
            if (!tournament.description.isNullOrEmpty()) {
                FadeIn(visible = started, delayMs = 300) {
                    SectionCard(title = "Описание") {
                        Text(
                            text = tournament.description,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 21.sp
                        )
                    }
                }
            }

            // ── Categories ──
            if (!tournament.categories.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
                FadeIn(visible = started, delayMs = 350) {
                    SectionCard(title = "Категории") {
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            tournament.categories.forEach { cat ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AccentSoft
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        color = Accent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Requirements ──
            if (!tournament.requirements.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
                FadeIn(visible = started, delayMs = 400) {
                    SectionCard(title = "Требования") {
                        tournament.requirements.forEach { req ->
                            Text(
                                text = "• $req",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // ── Tournament Details (2-column grid) ──
            Spacer(Modifier.height(8.dp))
            FadeIn(visible = started, delayMs = 420) {
                TournamentDetailsSection(tournament)
            }

            // ── Location ──
            if (tournament.locations != null) {
                Spacer(Modifier.height(8.dp))
                FadeIn(visible = started, delayMs = 470) {
                    LocationSection(tournament.locations)
                }
            }

            // ── Prizes ──
            if (!tournament.prizes.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
                FadeIn(visible = started, delayMs = 520) {
                    PrizesSection(tournament.prizes)
                }
            }

            // ── Schedule ──
            if (tournament.schedule != null) {
                Spacer(Modifier.height(8.dp))
                FadeIn(visible = started, delayMs = 570) {
                    ScheduleSection(tournament.schedule)
                }
            }

            // ── Participants ──
            if (data.participants.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FadeIn(visible = started, delayMs = 620) {
                    ParticipantsSection(data.participants)
                }
            }

            // ── Results ──
            if (data.results.isNotEmpty() && tournament.status == "completed") {
                Spacer(Modifier.height(8.dp))
                FadeIn(visible = started, delayMs = 670) {
                    ResultsSection(data.results)
                }
            }

            // ── Bracket ──
            if (data.bracket.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FadeIn(visible = started, delayMs = 720) {
                    BracketSection(data)
                }
            }

            // Bottom spacing for action button
            Spacer(Modifier.height(80.dp))
        }

        // ── Action Button ──
        ActionButton(
            user = user,
            data = data,
            viewModel = viewModel,
            onEditTournament = onEditTournament,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ══════════════════════════════════════════════════════════
// Sections
// ══════════════════════════════════════════════════════════

@Composable
private fun ParticipantsSection(participants: List<ParticipantDto>) {
    SectionCard(title = "Участники (${participants.size})") {
        participants.take(10).forEach { p ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Avatar
                val avatarUrl = p.profiles?.avatarUrl
                val name = p.profiles?.name ?: "—"
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AccentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.take(1).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Accent
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))

                p.seed?.let { seed ->
                    Text(
                        text = "#$seed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Accent,
                        modifier = Modifier.width(32.dp)
                    )
                }
                Text(
                    text = name,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Status badge
                val status = p.status
                if (status != null && status != "registered") {
                    val (statusLabel, statusColor) = when (status) {
                        "pending" -> "Ожидание" to Color(0xFFF59E0B)
                        "checked_in" -> "Check-in" to Color(0xFF22C55E)
                        "declined" -> "Отклонён" to Color(0xFFEF4444)
                        "withdrawn" -> "Снялся" to Color(0xFF6B7280)
                        else -> status to TextMuted
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 10.sp,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                }

                p.profiles?.city?.let { city ->
                    Text(
                        text = city,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        }
        if (participants.size > 10) {
            Text(
                text = "и ещё ${participants.size - 10}...",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ResultsSection(results: List<ResultDto>) {
    SectionCard(title = "Результаты") {
        results.forEach { r ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                val posEmoji = when (r.position) {
                    1 -> "\uD83E\uDD47"
                    2 -> "\uD83E\uDD48"
                    3 -> "\uD83E\uDD49"
                    else -> "${r.position}."
                }
                Text(
                    text = posEmoji,
                    fontSize = 14.sp,
                    modifier = Modifier.width(32.dp)
                )
                Text(
                    text = r.profiles?.name ?: "—",
                    fontSize = 14.sp,
                    fontWeight = if (r.position <= 3) FontWeight.SemiBold else FontWeight.Normal,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                r.points?.let { pts ->
                    Text(
                        text = "$pts очк.",
                        fontSize = 12.sp,
                        color = Accent
                    )
                }
            }
        }
    }
}

@Composable
private fun BracketSection(data: HomeTournamentDetailData) {
    val matches = BracketUtils.mapDtosToMatches(data.bracket, data.participants)
    val groups = BracketUtils.mapGroupDtos(data.groups)
    val format = data.tournament.format ?: "single_elimination"
    var selectedMatch by remember { mutableStateOf<BracketMatch?>(null) }

    SectionCard(title = "Турнирная сетка", modifier = Modifier.padding(horizontal = 4.dp)) {
        BracketView(
            format = format,
            matches = matches,
            groups = groups,
            onMatchClick = { selectedMatch = it }
        )
    }

    selectedMatch?.let { match ->
        MatchDetailDialog(
            match = match,
            canEdit = false,
            onDismiss = { selectedMatch = null }
        )
    }
}

// ══════════════════════════════════════════════════════════
// Action Button (role-based)
// ══════════════════════════════════════════════════════════

@Composable
private fun ActionButton(
    user: User,
    data: HomeTournamentDetailData,
    viewModel: TournamentDetailViewModel,
    onEditTournament: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tournament = data.tournament
    val status = tournament.status ?: return
    val regState = viewModel.registrationState
    val loading = viewModel.actionLoading

    // Determine button config
    val buttonConfig = remember(user.role, status, regState) {
        getButtonConfig(user, status, regState, tournament.organizerId)
    }

    if (buttonConfig != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Bg.copy(alpha = 0.95f))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = {
                    when (buttonConfig.action) {
                        ButtonAction.REGISTER_PARTICIPANT -> viewModel.registerAsParticipant(tournament.id, user.id)
                        ButtonAction.UNREGISTER -> viewModel.unregister(tournament.id, user.id)
                        ButtonAction.REGISTER_SPECTATOR -> viewModel.registerAsSpectator(tournament.id, user.id)
                        ButtonAction.EDIT -> onEditTournament(tournament.id)
                        else -> {}
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (buttonConfig.isDestructive) Color.Transparent else Accent,
                    contentColor = if (buttonConfig.isDestructive) com.ileader.app.ui.theme.ILeaderColors.Error else Color.White
                ),
                border = if (buttonConfig.isDestructive) {
                    BorderStroke(1.dp, com.ileader.app.ui.theme.ILeaderColors.Error)
                } else null
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = if (buttonConfig.isDestructive) Color(0xFFEF4444) else Color.White
                    )
                } else {
                    Text(text = buttonConfig.label, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private enum class ButtonAction {
    REGISTER_PARTICIPANT, UNREGISTER, REGISTER_SPECTATOR, EDIT, REFEREE
}

private data class ButtonConfig(
    val label: String,
    val action: ButtonAction,
    val isDestructive: Boolean = false
)

private fun getButtonConfig(
    user: User,
    status: String,
    regState: RegistrationState,
    organizerId: String?
): ButtonConfig? {
    return when (user.role) {
        UserRole.ATHLETE -> when {
            status == "registration_open" && regState is RegistrationState.RegisteredAsParticipant ->
                ButtonConfig("Отменить регистрацию", ButtonAction.UNREGISTER, isDestructive = true)
            status == "registration_open" && regState is RegistrationState.NotRegistered ->
                ButtonConfig("Зарегистрироваться", ButtonAction.REGISTER_PARTICIPANT)
            else -> null
        }
        UserRole.USER -> when {
            status == "registration_open" && regState is RegistrationState.NotRegistered ->
                ButtonConfig("Зарегистрироваться как зритель", ButtonAction.REGISTER_SPECTATOR)
            status == "registration_open" && regState is RegistrationState.RegisteredAsSpectator -> null
            else -> null
        }
        UserRole.ORGANIZER -> when {
            organizerId == user.id -> ButtonConfig("Редактировать", ButtonAction.EDIT)
            else -> null
        }
        UserRole.REFEREE -> when {
            status == "in_progress" -> ButtonConfig("Судейство", ButtonAction.REFEREE)
            else -> null
        }
        else -> null
    }
}

// ══════════════════════════════════════════════════════════
// Shared Components
// ══════════════════════════════════════════════════════════

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DarkCardPadded(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.3).sp
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun QuickInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    DarkCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = Accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(text = label, fontSize = 10.sp, color = TextMuted)
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
// New Detail Sections
// ══════════════════════════════════════════════════════════

@Composable
private fun TournamentDetailsSection(tournament: TournamentDto) {
    val items = buildList<Triple<ImageVector, String, String>> {
        tournament.format?.let {
            add(Triple(Icons.Default.AccountTree, "Формат", formatLabel(it)))
        }
        tournament.matchFormat?.let {
            add(Triple(Icons.Default.SportsScore, "Формат матча", matchFormatLabel(it)))
        }
        tournament.seedingType?.let {
            add(Triple(Icons.Default.Shuffle, "Посев", seedingLabel(it)))
        }
        tournament.ageCategory?.let {
            add(Triple(Icons.Default.Cake, "Возраст", it))
        }
        tournament.discipline?.let {
            add(Triple(Icons.Default.FitnessCenter, "Дисциплина", it))
        }
        tournament.region?.let {
            add(Triple(Icons.Default.Public, "Регион", it))
        }
        if (tournament.endDate != null && tournament.endDate != tournament.startDate) {
            add(Triple(Icons.Default.EventBusy, "Окончание", formatDateShort(tournament.endDate)))
        }
        tournament.registrationDeadline?.let {
            add(Triple(Icons.Default.HowToReg, "Дедлайн рег.", formatDateShort(it)))
        }
        tournament.minParticipants?.let {
            add(Triple(Icons.Default.GroupRemove, "Мин. участников", it.toString()))
        }
        if (tournament.hasCheckIn == true) {
            val mins = tournament.checkInStartsBefore
            add(Triple(Icons.Default.QrCodeScanner, "Check-in", if (mins != null) "За $mins мин" else "Да"))
        }
    }

    if (items.isEmpty()) return

    SectionCard(title = "Детали турнира") {
        Row(modifier = Modifier.fillMaxWidth()) {
            val left = items.filterIndexed { i, _ -> i % 2 == 0 }
            val right = items.filterIndexed { i, _ -> i % 2 == 1 }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                left.forEach { (icon, label, value) ->
                    DetailGridItem(icon, label, value)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                right.forEach { (icon, label, value) ->
                    DetailGridItem(icon, label, value)
                }
            }
        }
    }
}

@Composable
private fun DetailGridItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = TextMuted)
            Text(text = value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LocationSection(location: LocationDto) {
    SectionCard(title = "Место проведения") {
        // Location image
        location.imageUrls?.firstOrNull()?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(10.dp))
        }

        // Name + rating
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = location.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            location.rating?.let { rating ->
                Text(
                    text = "★ ${"%.1f".format(rating)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFF59E0B)
                )
            }
        }

        // Address + city
        val addressParts = listOfNotNull(location.address, location.city).filter { it.isNotEmpty() }
        if (addressParts.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = addressParts.joinToString(", "),
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // Type badge
        location.type?.let { type ->
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = AccentSoft
            ) {
                Text(
                    text = locationTypeLabel(type),
                    fontSize = 11.sp,
                    color = Accent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Capacity
        location.capacity?.let { cap ->
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(text = "Вместимость: $cap", fontSize = 12.sp, color = TextSecondary)
            }
        }

        // Facilities chips
        if (!location.facilities.isNullOrEmpty()) {
            Spacer(Modifier.height(8.dp))
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                location.facilities.forEach { facility ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CardBorder.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = facility,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Phone
        location.phone?.let { phone ->
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(text = phone, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun PrizesSection(prizes: List<String>) {
    SectionCard(title = "Призы") {
        prizes.forEachIndexed { index, prize ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 3.dp)
            ) {
                val medal = when (index) {
                    0 -> "\uD83E\uDD47"
                    1 -> "\uD83E\uDD48"
                    2 -> "\uD83E\uDD49"
                    else -> "${index + 1}."
                }
                Text(
                    text = medal,
                    fontSize = 14.sp,
                    modifier = Modifier.width(32.dp)
                )
                Text(
                    text = prize,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    fontWeight = if (index < 3) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ScheduleSection(scheduleJson: kotlinx.serialization.json.JsonElement) {
    val jsonParser = remember { Json { ignoreUnknownKeys = true } }
    val items = remember(scheduleJson) {
        try {
            jsonParser.decodeFromJsonElement<List<ScheduleItemDto>>(scheduleJson)
        } catch (_: Exception) {
            emptyList()
        }
    }
    if (items.isEmpty()) return

    val accentColor = Accent
    val accentFaded = Accent.copy(alpha = 0.2f)

    SectionCard(title = "Расписание") {
        items.forEachIndexed { index, item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // Time column
                Text(
                    text = item.time,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    modifier = Modifier.width(52.dp)
                )

                // Timeline dot + line
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(IntrinsicSize.Min),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (index < items.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(accentFaded)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Title
                Text(
                    text = item.title,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = if (index < items.size - 1) 16.dp else 0.dp)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Helpers
// ══════════════════════════════════════════════════════════

private fun formatDateShort(dateStr: String?): String {
    if (dateStr == null) return "—"
    val parts = dateStr.take(10).split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2].toIntOrNull() ?: return dateStr
    val monthNames = listOf(
        "", "янв", "фев", "мар", "апр", "мая",
        "июн", "июл", "авг", "сен", "окт", "ноя", "дек"
    )
    val month = parts[1].toIntOrNull() ?: return dateStr
    return "$day ${monthNames.getOrElse(month) { "" }}"
}

private fun getStatusLabel(status: String): String = when (status) {
    "in_progress" -> "Идёт сейчас"
    "registration_open" -> "Регистрация"
    "registration_closed" -> "Рег. закрыта"
    "check_in" -> "Check-in"
    "completed" -> "Завершён"
    "cancelled" -> "Отменён"
    "draft" -> "Черновик"
    else -> status
}

private fun formatShortLabel(format: String?): String = when (format) {
    "single_elimination" -> "SE"
    "double_elimination" -> "DE"
    "round_robin" -> "RR"
    "group_stage" -> "GR"
    else -> format?.take(2)?.uppercase() ?: "—"
}

private fun formatLabel(format: String): String = when (format) {
    "single_elimination" -> "Одиночная элиминация"
    "double_elimination" -> "Двойная элиминация"
    "round_robin" -> "Круговой"
    "group_stage" -> "Групповой"
    else -> format
}

private fun matchFormatLabel(mf: String): String = when (mf) {
    "bo1" -> "До 1 победы"
    "bo3" -> "До 2 побед"
    "bo5" -> "До 3 побед"
    else -> mf
}

private fun seedingLabel(st: String): String = when (st) {
    "random" -> "Случайное"
    "manual" -> "Ручное"
    "rating" -> "По рейтингу"
    else -> st
}

private fun locationTypeLabel(type: String): String = when (type) {
    "track" -> "Трасса"
    "stadium" -> "Стадион"
    "arena" -> "Арена"
    "court" -> "Корт"
    "pool" -> "Бассейн"
    "gym" -> "Зал"
    "field" -> "Поле"
    "range" -> "Стрельбище"
    "water" -> "Водоём"
    else -> type
}
