package com.ileader.app.ui.screens.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.LocationDto
import com.ileader.app.data.remote.dto.ParticipantDto
import com.ileader.app.data.remote.dto.RefereeAssignmentDto
import com.ileader.app.data.remote.dto.ResultDto
import com.ileader.app.data.remote.dto.ScheduleItemDto
import com.ileader.app.data.remote.dto.TournamentDto
import com.ileader.app.data.remote.dto.TournamentSponsorshipDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.components.bracket.BracketView
import com.ileader.app.ui.components.bracket.MatchDetailDialog
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.RegistrationState
import com.ileader.app.ui.viewmodels.HomeTournamentDetailData
import com.ileader.app.ui.viewmodels.SportViewModel
import com.ileader.app.ui.viewmodels.TournamentDetailViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft
private val Border: Color @Composable get() = LocalAppColors.current.border

@Composable
fun TournamentDetailScreen(
    tournamentId: String,
    user: User,
    onBack: () -> Unit,
    onEditTournament: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onAthleteProfileClick: (String) -> Unit = {},
    onRefereeProfileClick: (String) -> Unit = {},
    onTrainerProfileClick: (String) -> Unit = {},
    onTeamClick: (String) -> Unit = {},
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
                TournamentContent(
                    data = state.data,
                    user = user,
                    viewModel = viewModel,
                    onBack = onBack,
                    onEditTournament = onEditTournament,
                    onProfileClick = onProfileClick,
                    onAthleteProfileClick = onAthleteProfileClick,
                    onRefereeProfileClick = onRefereeProfileClick,
                    onTrainerProfileClick = onTrainerProfileClick,
                    onTeamClick = onTeamClick
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Main Content with Tabs
// ══════════════════════════════════════════════════════════

@Composable
private fun TournamentContent(
    data: HomeTournamentDetailData,
    user: User,
    viewModel: TournamentDetailViewModel,
    onBack: () -> Unit,
    onEditTournament: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onAthleteProfileClick: (String) -> Unit = {},
    onRefereeProfileClick: (String) -> Unit = {},
    onTrainerProfileClick: (String) -> Unit = {},
    onTeamClick: (String) -> Unit = {}
) {
    val tournament = data.tournament
    val accentColor = Accent
    val accentDarkColor = AccentDark

    // Tab state
    val tabs = remember(data) { buildTabList(data) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ══════════════════════════════════════
            // HERO HEADER
            // ══════════════════════════════════════
            HeroHeader(tournament, data, onBack, accentColor)

            // ══════════════════════════════════════
            // STICKY TAB BAR
            // ══════════════════════════════════════
            TabBar(tabs, selectedTab) { selectedTab = it }

            Spacer(Modifier.height(8.dp))

            // ══════════════════════════════════════
            // TAB CONTENT
            // ══════════════════════════════════════
            when (tabs.getOrNull(selectedTab)?.type) {
                TabType.OVERVIEW -> OverviewTab(data, tournament, onProfileClick, onAthleteProfileClick, onRefereeProfileClick, onTrainerProfileClick, onTeamClick)
                TabType.PARTICIPANTS -> ParticipantsTab(data.participants, tournament, onProfileClick)
                TabType.BRACKET -> BracketTab(data)
                TabType.RESULTS -> ResultsTab(data, onProfileClick)
                TabType.NEWS -> NewsTab(data.articles)
                else -> OverviewTab(data, tournament, onProfileClick, onAthleteProfileClick, onRefereeProfileClick, onTrainerProfileClick, onTeamClick)
            }

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
// HERO HEADER (Premium)
// ══════════════════════════════════════════════════════════

@Composable
private fun HeroHeader(
    tournament: TournamentDto,
    data: HomeTournamentDetailData,
    onBack: () -> Unit,
    accentColor: Color
) {
    val sportName = tournament.sports?.name ?: ""
    val heroColor = if (sportName.isNotEmpty()) sportColor(sportName) else accentColor
    val heroImage = tournament.imageUrl
        ?: tournament.sports?.let { SportViewModel.getFallbackImage(it) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
    ) {
        // Background
        if (heroImage != null) {
            AsyncImage(
                model = heroImage, contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.8f))
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
                .padding(top = 12.dp, bottom = 20.dp)
        ) {
            // Top row: back + fav + share
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.weight(1f))

                // Favorite
                val favContext = androidx.compose.ui.platform.LocalContext.current
                val favPref = remember { com.ileader.app.data.preferences.FavoritesPreference(favContext) }
                val favIds by favPref.favoriteTournamentIds.collectAsState(initial = emptyList())
                val isFav = tournament.id in favIds
                val scope = rememberCoroutineScope()

                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(if (isFav) Accent.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.3f))
                        .clickable { scope.launch { favPref.toggleFavorite(tournament.id) } },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Status pills
            @Composable
            fun HeroPill(text: String, bg: Color = Color.White.copy(alpha = 0.15f)) {
                Surface(shape = RoundedCornerShape(50), color = bg) {
                    Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Live pulsing badge
                tournament.status?.let { status ->
                    if (status == "in_progress") {
                        LiveBadge()
                    } else {
                        val statusBg = when (status) {
                            "check_in" -> Color(0xFF7C3AED).copy(0.8f)
                            "completed" -> Color(0xFF22C55E).copy(0.7f)
                            else -> Color.White.copy(alpha = 0.2f)
                        }
                        HeroPill(getStatusLabel(status), statusBg)
                    }
                }
                tournament.sports?.name?.let { name ->
                    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.15f)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(sportIcon(name), null, tint = Color.White.copy(0.9f), modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(name, fontSize = 11.sp, color = Color.White.copy(0.9f), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                tournament.format?.let { fmt ->
                    HeroPill(formatLabel(fmt))
                }
                if (tournament.visibility == "private") {
                    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(0.2f)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Lock, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Приватный", fontSize = 11.sp, color = Color.White.copy(0.8f), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Date badge
            tournament.startDate?.let { start ->
                val dateText = buildString {
                    append(formatDateFull(start))
                    tournament.endDate?.let { end ->
                        if (end != start) append(" — ${formatDateFull(end)}")
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.CalendarMonth, null, tint = Color.White.copy(0.9f), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(dateText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(0.95f))
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Tournament name
            Text(
                text = tournament.name,
                fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                color = Color.White, lineHeight = 32.sp, letterSpacing = (-0.5).sp
            )

            // Organizer
            tournament.profiles?.name?.let { org ->
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Business, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(org, fontSize = 13.sp, color = Color.White.copy(0.8f))
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Prize + Countdown + Participants row ──
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Prize pool (big)
                val prizeText = tournament.prize
                if (!prizeText.isNullOrEmpty()) {
                    Column {
                        Text("Призовой фонд", fontSize = 10.sp, color = Color.White.copy(0.5f), fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(prizeText, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
                // Countdown
                val countdown = rememberCountdown(tournament.startDate)
                if (countdown != null && tournament.status in listOf("registration_open", "registration_closed", "check_in", "draft")) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("До старта", fontSize = 10.sp, color = Color.White.copy(0.5f), fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(countdown, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // ── Registration progress bar ──
            val maxP = tournament.maxParticipants
            val curP = data.participants.size
            if (maxP != null && maxP > 0 && tournament.status in listOf("registration_open", "check_in")) {
                Spacer(Modifier.height(14.dp))
                val progress = (curP.toFloat() / maxP).coerceIn(0f, 1f)
                val remaining = maxP - curP
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$curP / $maxP участников", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        if (remaining > 0) {
                            Text("осталось $remaining мест", fontSize = 11.sp, color = Color.White.copy(0.6f))
                        } else {
                            Text("мест нет", fontSize = 11.sp, color = Color(0xFFFCA5A5))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (progress >= 1f) Color(0xFFFCA5A5) else Color.White,
                        trackColor = Color.White.copy(0.15f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// LIVE BADGE (pulsing)
// ══════════════════════════════════════════════════════════

@Composable
private fun LiveBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse"
    )

    Surface(shape = RoundedCornerShape(50), color = Color(0xFFEF4444).copy(alpha = 0.9f)) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(8.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = alpha))
            )
            Spacer(Modifier.width(5.dp))
            Text("LIVE", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
        }
    }
}

// ══════════════════════════════════════════════════════════
// COUNTDOWN
// ══════════════════════════════════════════════════════════

@Composable
private fun rememberCountdown(startDate: String?): String? {
    if (startDate == null) return null
    var text by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(startDate) {
        while (true) {
            try {
                val target = if (startDate.contains("T")) {
                    LocalDateTime.parse(startDate.take(19), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } else {
                    LocalDate.parse(startDate.take(10)).atStartOfDay()
                }
                val now = LocalDateTime.now()
                val totalMinutes = ChronoUnit.MINUTES.between(now, target)
                if (totalMinutes <= 0) {
                    text = null
                } else {
                    val days = totalMinutes / (60 * 24)
                    val hours = (totalMinutes % (60 * 24)) / 60
                    val mins = totalMinutes % 60
                    text = when {
                        days > 0 -> "${days}д ${hours}ч"
                        hours > 0 -> "${hours}ч ${mins}м"
                        else -> "${mins}м"
                    }
                }
            } catch (_: Exception) {
                text = null
            }
            delay(60_000) // update every minute
        }
    }
    return text
}

// ══════════════════════════════════════════════════════════
// TAB BAR (pill-style, scrollable)
// ══════════════════════════════════════════════════════════

private enum class TabType { OVERVIEW, PARTICIPANTS, BRACKET, RESULTS, NEWS }
private data class TabItem(val label: String, val type: TabType, val count: Int? = null)

private fun buildTabList(data: HomeTournamentDetailData): List<TabItem> = buildList {
    add(TabItem("Обзор", TabType.OVERVIEW))
    if (data.participants.isNotEmpty()) {
        add(TabItem("Участники", TabType.PARTICIPANTS, data.participants.size))
    }
    if (data.bracket.isNotEmpty()) {
        add(TabItem("Сетка", TabType.BRACKET))
    }
    if (data.results.isNotEmpty() && data.tournament.status == "completed") {
        add(TabItem("Результаты", TabType.RESULTS, data.results.size))
    }
    if (data.articles.isNotEmpty()) {
        add(TabItem("Новости", TabType.NEWS, data.articles.size))
    }
}

@Composable
private fun TabBar(tabs: List<TabItem>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { idx, tab ->
            val isSelected = idx == selectedIndex
            val bgColor by animateColorAsState(
                if (isSelected) Accent else CardBg, label = "tabBg"
            )
            val textColor by animateColorAsState(
                if (isSelected) Color.White else TextMuted, label = "tabText"
            )
            Surface(
                modifier = Modifier.clickable { onSelect(idx) },
                shape = RoundedCornerShape(50),
                color = bgColor,
                border = if (!isSelected) BorderStroke(1.dp, Border.copy(0.3f)) else null
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tab.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                    tab.count?.let { count ->
                        Spacer(Modifier.width(5.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) Color.White.copy(0.2f) else Border.copy(0.2f)
                        ) {
                            Text(
                                "$count",
                                Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White.copy(0.9f) else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// TAB: OVERVIEW
// ══════════════════════════════════════════════════════════

@Composable
private fun OverviewTab(
    data: HomeTournamentDetailData,
    tournament: TournamentDto,
    onProfileClick: (String) -> Unit,
    onAthleteProfileClick: (String) -> Unit = {},
    onRefereeProfileClick: (String) -> Unit = {},
    onTrainerProfileClick: (String) -> Unit = {},
    onTeamClick: (String) -> Unit = {}
) {
    Column {
        // ── Organizer ──
        tournament.profiles?.name?.let { orgName ->
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clickable { tournament.organizerId?.let { onProfileClick(it) } },
                shape = RoundedCornerShape(16.dp), color = CardBg, shadowElevation = 0.dp
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).background(Accent.copy(0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Business, null, tint = Accent, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Организатор", fontSize = 11.sp, color = TextMuted)
                        Text(orgName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                }
            }
        }

        // ── Description ──
        if (!tournament.description.isNullOrEmpty()) {
            Spacer(Modifier.height(8.dp))
            SectionCard(title = "Описание") {
                Text(tournament.description, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)
            }
        }

        // ── Categories ──
        if (!tournament.categories.isNullOrEmpty()) {
            Spacer(Modifier.height(8.dp))
            SectionCard(title = "Категории") {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tournament.categories.forEach { cat ->
                        Surface(shape = RoundedCornerShape(8.dp), color = AccentSoft) {
                            Text(cat, fontSize = 12.sp, color = Accent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }

        // ── Requirements ──
        if (!tournament.requirements.isNullOrEmpty()) {
            Spacer(Modifier.height(8.dp))
            SectionCard(title = "Требования") {
                tournament.requirements.forEach { req ->
                    Text("• $req", fontSize = 13.sp, color = TextSecondary,
                        modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }

        // ── Tournament Details ──
        Spacer(Modifier.height(8.dp))
        TournamentDetailsSection(tournament)

        // ── Location ──
        if (tournament.locations != null) {
            Spacer(Modifier.height(8.dp))
            LocationSection(tournament.locations)
        }

        // ── Prizes ──
        if (!tournament.prizes.isNullOrEmpty()) {
            Spacer(Modifier.height(8.dp))
            PrizesSection(tournament.prizes)
        }

        // ── Schedule ──
        if (tournament.schedule != null) {
            Spacer(Modifier.height(8.dp))
            ScheduleSection(tournament.schedule)
        }

        // ── Teams ──
        val teams = remember(data.participants) {
            val real = data.participants.filter { it.teams?.name != null }
                .groupBy { it.teamId }
                .mapNotNull { (teamId, members) ->
                    val teamName = members.firstOrNull()?.teams?.name ?: return@mapNotNull null
                    Triple(teamId ?: "", teamName, members.map { (it.profiles?.name ?: "?") to it.profiles?.avatarUrl })
                }
            real.ifEmpty {
                // Mock teams
                listOf(
                    Triple("1", "Red Racers", listOf("Алихан Т." to null, "Марат К." to null, "Данияр С." to null, "Тимур Н." to null)),
                    Triple("2", "Storm Eagles", listOf("Аян Б." to null, "Ерлан Ж." to null, "Нурлан А." to null))
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        MockTeamsSection(teams, onAthleteProfileClick, onTeamClick)

        // ── Referees ──
        val referees = remember(data.referees) {
            data.referees.ifEmpty {
                // Mock referees
                listOf(
                    MockReferee("Серик Абдуллаев", "head", null),
                    MockReferee("Кайрат Жумабеков", "assistant", null),
                    MockReferee("Бауыржан Тулеев", "line", null)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        MockRefereesSection(referees, data.referees.isEmpty(), onRefereeProfileClick)

        // ── Trainers ──
        val mockTrainers = remember {
            listOf(
                MockTrainer("Ержан Каримов", "Red Racers", null),
                MockTrainer("Алмас Сулейменов", "Storm Eagles", null)
            )
        }
        Spacer(Modifier.height(8.dp))
        MockTrainersSection(mockTrainers, onTrainerProfileClick)

        // ── Sponsors ──
        if (data.sponsors.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            SponsorsSection(data.sponsors, onProfileClick)
        }

        // ── FAQ ──
        Spacer(Modifier.height(8.dp))
        FaqSection()
    }
}

// ══════════════════════════════════════════════════════════
// AT A GLANCE GRID (2x3)
// ══════════════════════════════════════════════════════════

@Composable
private fun AtAGlanceGrid(tournament: TournamentDto, data: HomeTournamentDetailData) {
    val items = buildList {
        add(Triple(Icons.Outlined.CalendarMonth, "Дата", formatDateShort(tournament.startDate)))
        add(Triple(Icons.Outlined.LocationOn, "Место", tournament.locations?.name ?: "—"))
        add(Triple(Icons.Outlined.People, "Участники", "${data.participants.size}/${tournament.maxParticipants ?: "∞"}"))
        add(Triple(Icons.Outlined.AccountTree, "Формат", tournament.format?.let { formatLabel(it) } ?: "—"))
        val fee = tournament.entryFee
        if (fee != null && fee > 0) {
            add(Triple(Icons.Outlined.CreditCard, "Взнос", "${fee.toInt()} ₸"))
        }
        tournament.registrationDeadline?.let {
            add(Triple(Icons.Outlined.HowToReg, "Дедлайн", formatDateShort(it)))
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp), color = CardBg, shadowElevation = 0.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            items.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    row.forEach { (icon, label, value) ->
                        GlanceCell(icon, label, value, Modifier.weight(1f))
                    }
                    // Fill empty cells if row has less than 3
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
                if (row != items.chunked(3).last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        thickness = 0.5.dp, color = Border.copy(0.15f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlanceCell(icon: ImageVector, label: String, value: String, modifier: Modifier) {
    Column(modifier.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(32.dp).background(AccentSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(
            value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
        )
    }
}

// ══════════════════════════════════════════════════════════
// TAB: PARTICIPANTS
// ══════════════════════════════════════════════════════════

@Composable
private fun ParticipantsTab(
    participants: List<ParticipantDto>,
    tournament: TournamentDto,
    onProfileClick: (String) -> Unit
) {
    Column {
        // Progress header
        val maxP = tournament.maxParticipants
        val curP = participants.size
        if (maxP != null && maxP > 0) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp), color = CardBg, shadowElevation = 0.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    val progress = (curP.toFloat() / maxP).coerceIn(0f, 1f)
                    val remaining = maxP - curP
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$curP / $maxP зарегистрировано", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        if (remaining > 0) {
                            Text("$remaining мест", fontSize = 13.sp, color = Accent, fontWeight = FontWeight.Medium)
                        } else {
                            Text("Мест нет", fontSize = 13.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (progress >= 1f) Color(0xFFEF4444) else Accent,
                        trackColor = Border.copy(0.15f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Participants list
        SectionCard(title = "Все участники") {
            participants.forEachIndexed { idx, p ->
                val avatarUrl = p.profiles?.avatarUrl
                val name = p.profiles?.name ?: "—"

                if (idx > 0) HorizontalDivider(
                    thickness = 0.5.dp, color = Border.copy(0.15f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .clickable { p.athleteId?.let { onProfileClick(it) } }
                        .padding(vertical = 6.dp)
                ) {
                    // Number
                    Text(
                        "${idx + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = TextMuted, modifier = Modifier.width(28.dp),
                        textAlign = TextAlign.Center
                    )

                    // Avatar
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl, contentDescription = null,
                            modifier = Modifier.size(36.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(AccentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name.take(1).uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Accent)
                        }
                    }
                    Spacer(Modifier.width(10.dp))

                    // Seed badge
                    p.seed?.let { seed ->
                        Surface(shape = RoundedCornerShape(6.dp), color = Accent.copy(0.1f)) {
                            Text("#$seed", Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Accent)
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    // Name + city
                    Column(Modifier.weight(1f)) {
                        Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        p.profiles?.city?.let { city ->
                            Text(city, fontSize = 12.sp, color = TextMuted)
                        }
                    }

                    // Status
                    val status = p.status
                    if (status != null && status != "registered" && status != "confirmed") {
                        val (statusLabel, statusColor) = when (status) {
                            "pending" -> "Ожидание" to Color(0xFFF59E0B)
                            "checked_in" -> "Check-in ✓" to Color(0xFF22C55E)
                            "declined" -> "Отклонён" to Color(0xFFEF4444)
                            "withdrawn" -> "Снялся" to Color(0xFF6B7280)
                            else -> status to TextMuted
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.12f)) {
                            Text(statusLabel, Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// TAB: BRACKET
// ══════════════════════════════════════════════════════════

@Composable
private fun BracketTab(data: HomeTournamentDetailData) {
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
        MatchDetailDialog(match = match, canEdit = false, onDismiss = { selectedMatch = null })
    }
}

// ══════════════════════════════════════════════════════════
// TAB: RESULTS
// ══════════════════════════════════════════════════════════

@Composable
private fun ResultsTab(data: HomeTournamentDetailData, onProfileClick: (String) -> Unit) {
    Column {
        // Podium
        if (data.results.size >= 3) {
            PodiumSection(data.results.take(3), onProfileClick)
            Spacer(Modifier.height(8.dp))
        }
        // Full results
        ResultsSection(data.results, onProfileClick)
    }
}

// ══════════════════════════════════════════════════════════
// TAB: NEWS
// ══════════════════════════════════════════════════════════

@Composable
private fun NewsTab(articles: List<ArticleDto>) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        articles.forEach { article -> NewsCard(article) }
    }
}

@Composable
private fun NewsCard(article: ArticleDto) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp), color = CardBg, shadowElevation = 0.dp
    ) {
        Column {
            if (article.coverImageUrl != null) {
                AsyncImage(
                    model = article.coverImageUrl, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.padding(16.dp)) {
                Text(
                    article.title, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary, maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis, lineHeight = 22.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    article.category?.let { cat ->
                        Surface(shape = RoundedCornerShape(6.dp), color = AccentSoft) {
                            Text(cat, Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 11.sp, color = Accent, fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Icon(Icons.Outlined.CalendarMonth, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(formatDateShort(article.publishedAt ?: article.createdAt), fontSize = 12.sp, color = TextMuted)
                    if (article.views > 0) {
                        Spacer(Modifier.width(10.dp))
                        Icon(Icons.Outlined.Visibility, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${article.views}", fontSize = 12.sp, color = TextMuted)
                    }
                }
                if (expanded && !article.excerpt.isNullOrEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f))
                    Spacer(Modifier.height(12.dp))
                    Text(article.excerpt, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Section Composables (shared)
// ══════════════════════════════════════════════════════════

@Composable
private fun PodiumSection(topResults: List<ResultDto>, onProfileClick: (String) -> Unit = {}) {
    SectionCard(title = "Победители") {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            if (topResults.size >= 2) PodiumPlace(topResults[1], 2, 80.dp, Color(0xFF94A3B8), Color(0xFFF1F5F9), "Серебро", onProfileClick)
            if (topResults.isNotEmpty()) PodiumPlace(topResults[0], 1, 100.dp, Color(0xFFCA8A04), Color(0xFFFEF9C3), "Золото", onProfileClick)
            if (topResults.size >= 3) PodiumPlace(topResults[2], 3, 64.dp, Color(0xFFB45309), Color(0xFFFEF3C7), "Бронза", onProfileClick)
        }
    }
}

@Composable
private fun PodiumPlace(
    result: ResultDto, position: Int, height: androidx.compose.ui.unit.Dp,
    color: Color, bgColor: Color, label: String, onProfileClick: (String) -> Unit
) {
    val isDark = DarkTheme.isDark
    val actualBg = if (isDark) color.copy(alpha = 0.15f) else bgColor
    val avatarUrl = result.profiles?.avatarUrl
    val name = result.profiles?.name ?: "—"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp).clickable { result.athleteId?.let { onProfileClick(it) } }
    ) {
        Box(
            modifier = Modifier.size(if (position == 1) 56.dp else 44.dp).clip(CircleShape).background(color.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                AsyncImage(avatarUrl, null, Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            } else {
                Text(name.take(1).uppercase(), fontSize = if (position == 1) 20.sp else 16.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(actualBg),
            contentAlignment = Alignment.Center
        ) {
            Text("$position", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun ResultsSection(results: List<ResultDto>, onProfileClick: (String) -> Unit = {}) {
    SectionCard(title = "Результаты") {
        results.forEach { r ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .clickable { r.athleteId?.let { onProfileClick(it) } }
                    .padding(vertical = 6.dp)
            ) {
                val posEmoji = when (r.position) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "${r.position}." }
                Text(posEmoji, fontSize = 14.sp, modifier = Modifier.width(32.dp))
                Text(
                    r.profiles?.name ?: "—", fontSize = 14.sp,
                    fontWeight = if (r.position <= 3) FontWeight.SemiBold else FontWeight.Normal,
                    color = TextPrimary, modifier = Modifier.weight(1f)
                )
                r.points?.let { pts ->
                    Surface(shape = RoundedCornerShape(50), color = Accent.copy(0.12f)) {
                        Text("$pts очк.", fontSize = 12.sp, color = Accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Mock data classes
// ══════════════════════════════════════════════════════════

private data class MockReferee(val name: String, val role: String, val avatarUrl: String?)
private data class MockTrainer(val name: String, val teamName: String, val avatarUrl: String?)

// ══════════════════════════════════════════════════════════
// TEAMS SECTION (mock-compatible)
// ══════════════════════════════════════════════════════════

@Composable
private fun MockTeamsSection(
    teams: List<Triple<String, String, List<Pair<String, String?>>>>,
    onProfileClick: (String) -> Unit,
    onTeamClick: (String) -> Unit = {}
) {
    SectionCard(title = "Команды") {
        teams.forEachIndexed { idx, (teamId, teamName, members) ->
            if (idx > 0) {
                HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 10.dp))
            }

            // Whole team block clickable
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onTeamClick(teamId) }
                    .padding(vertical = 4.dp)
            ) {
                // Team header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).background(Accent.copy(0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            teamName.take(1).uppercase(),
                            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Accent
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(teamName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${members.size} участников", fontSize = 12.sp, color = TextMuted)
                    }
                    Icon(
                        Icons.Outlined.ChevronRight, null, tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Team members avatars
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                    members.take(5).forEach { (name, avatar) ->
                        UserAvatar(avatarUrl = avatar, name = name, size = 34.dp)
                    }
                    if (members.size > 5) {
                        Box(
                            Modifier.size(34.dp).background(TextMuted.copy(0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+${members.size - 5}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// REFEREES SECTION (mock-compatible)
// ══════════════════════════════════════════════════════════

@Composable
private fun MockRefereesSection(
    referees: Any, // List<RefereeAssignmentDto> or List<MockReferee>
    isMock: Boolean,
    onProfileClick: (String) -> Unit
) {
    SectionCard(title = "Судьи") {
        @Suppress("UNCHECKED_CAST")
        if (isMock) {
            val mocks = referees as List<MockReferee>
            mocks.forEachIndexed { idx, ref ->
                if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 6.dp))
                RefereeRow(name = ref.name, role = ref.role, avatarUrl = ref.avatarUrl, onClick = { onProfileClick("mock-referee-$idx") })
            }
        } else {
            val real = referees as List<RefereeAssignmentDto>
            real.forEachIndexed { idx, ref ->
                if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 6.dp))
                RefereeRow(
                    name = ref.profiles?.name ?: "Судья",
                    role = ref.role ?: "referee",
                    avatarUrl = ref.profiles?.avatarUrl,
                    onClick = { ref.refereeId?.let { onProfileClick(it) } }
                )
            }
        }
    }
}

@Composable
private fun RefereeRow(name: String, role: String, avatarUrl: String?, onClick: () -> Unit) {
    val roleLabel = when (role) {
        "head" -> "Главный судья"
        "assistant" -> "Помощник судьи"
        "line" -> "Линейный судья"
        else -> "Судья"
    }
    val roleColor = when (role) {
        "head" -> Color(0xFFEF4444)
        "assistant" -> Color(0xFF7C3AED)
        else -> Color(0xFF3B82F6)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        UserAvatar(avatarUrl = avatarUrl, name = name, size = 42.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(roleColor))
                Spacer(Modifier.width(6.dp))
                Text(roleLabel, fontSize = 12.sp, color = TextMuted)
            }
        }
        Box(
            Modifier.size(32.dp).background(roleColor.copy(0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Gavel, null, tint = roleColor, modifier = Modifier.size(16.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════
// TRAINERS SECTION (mock)
// ══════════════════════════════════════════════════════════

@Composable
private fun MockTrainersSection(
    trainers: List<MockTrainer>,
    onProfileClick: (String) -> Unit
) {
    SectionCard(title = "Тренера") {
        trainers.forEachIndexed { idx, trainer ->
            if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onProfileClick("mock-trainer-$idx") }
                    .padding(vertical = 4.dp)
            ) {
                UserAvatar(avatarUrl = trainer.avatarUrl, name = trainer.name, size = 42.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(trainer.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.People, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(trainer.teamName, fontSize = 12.sp, color = TextMuted)
                    }
                }
                Box(
                    Modifier.size(32.dp).background(Color(0xFF059669).copy(0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.School, null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SponsorsSection(sponsors: List<TournamentSponsorshipDto>, onProfileClick: (String) -> Unit = {}) {
    SectionCard(title = "Партнёры") {
        val sortedSponsors = sponsors.sortedBy { tierOrder(it.tier) }
        sortedSponsors.forEachIndexed { idx, sponsor ->
            if (idx > 0) HorizontalDivider(
                thickness = 0.5.dp, color = Border.copy(0.15f),
                modifier = Modifier.padding(vertical = 6.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .clickable { onProfileClick(sponsor.sponsorId) }
                    .padding(vertical = 4.dp)
            ) {
                val avatarUrl = sponsor.profiles?.avatarUrl
                val name = sponsor.profiles?.name ?: "Спонсор"
                if (avatarUrl != null) {
                    AsyncImage(model = avatarUrl, contentDescription = null,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop)
                } else {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(AccentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(name.take(1).uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Accent)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    sponsor.amount?.let { amount ->
                        if (amount > 0) Text("${amount.toInt()} ₸", fontSize = 12.sp, color = TextMuted)
                    }
                }
                val tierLabel = tierDisplayName(sponsor.tier)
                val tierClr = tierColor(sponsor.tier)
                Surface(shape = RoundedCornerShape(50), color = tierClr.copy(0.12f)) {
                    Text(tierLabel, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = tierClr)
                }
            }
        }
    }
}

@Composable
private fun FaqSection() {
    val faqItems = listOf(
        "Как зарегистрироваться на турнир?" to "Нажмите кнопку «Участвовать» внизу экрана. Для участия необходимо иметь аккаунт спортсмена. Если регистрация закрыта или достигнут лимит участников, кнопка будет недоступна.",
        "Какие правила турнира?" to "Правила зависят от вида спорта и формата турнира. Ознакомьтесь с секцией «Требования» выше. Общие правила платформы доступны на сайте ileader.kz.",
        "Как узнать результаты?" to "Результаты публикуются после завершения турнира в секциях «Победители» и «Результаты». Также вы получите уведомление.",
        "Можно ли отменить регистрацию?" to "Да, вы можете отменить участие до начала турнира через кнопку «Отменить регистрацию» на странице турнира.",
        "Как связаться с организатором?" to "Нажмите на карточку организатора, чтобы перейти на его профиль и найти контактную информацию."
    )
    SectionCard(title = "Вопросы и ответы") {
        faqItems.forEachIndexed { idx, (question, answer) ->
            if (idx > 0) Spacer(Modifier.height(4.dp))
            FaqItem(question, answer)
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        color = if (expanded) AccentSoft else Color.Transparent, shadowElevation = 0.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = Accent, modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(question, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.weight(1f))
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(answer, fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp, modifier = Modifier.padding(start = 28.dp))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Detail Sub-sections
// ══════════════════════════════════════════════════════════

@Composable
private fun TournamentDetailsSection(tournament: TournamentDto) {
    data class DetailItem(val icon: ImageVector, val label: String, val value: String, val accent: Boolean = false)

    val items = buildList {
        tournament.format?.let { add(DetailItem(Icons.Outlined.AccountTree, "Формат", formatLabel(it))) }
        tournament.matchFormat?.let { add(DetailItem(Icons.Outlined.SportsScore, "Формат матча", matchFormatLabel(it))) }
        tournament.seedingType?.let { add(DetailItem(Icons.Outlined.Shuffle, "Посев", seedingLabel(it))) }
        if (tournament.endDate != null && tournament.endDate != tournament.startDate) {
            add(DetailItem(Icons.Outlined.EventBusy, "Окончание", formatDateShort(tournament.endDate)))
        }
        tournament.registrationDeadline?.let { add(DetailItem(Icons.Outlined.HowToReg, "Дедлайн регистрации", formatDateShort(it), accent = true)) }
        if (tournament.hasCheckIn == true) {
            val mins = tournament.checkInStartsBefore
            add(DetailItem(Icons.Outlined.QrCodeScanner, "Check-in", if (mins != null) "За $mins мин" else "Да"))
        }
        tournament.ageCategory?.let { add(DetailItem(Icons.Outlined.Cake, "Возраст", ageCategoryLabel(it))) }
        tournament.skillLevel?.let { add(DetailItem(Icons.Outlined.TrendingUp, "Уровень", skillLevelLabel(it))) }
        tournament.genderCategory?.let { add(DetailItem(Icons.Outlined.Wc, "Пол", genderLabel(it))) }
        tournament.discipline?.let { add(DetailItem(Icons.Outlined.FitnessCenter, "Дисциплина", it)) }
        tournament.region?.let { add(DetailItem(Icons.Outlined.Public, "Регион", regionLabel(it))) }
        tournament.minParticipants?.let { add(DetailItem(Icons.Outlined.GroupRemove, "Мин. участников", "$it чел.")) }
    }
    if (items.isEmpty()) return

    SectionCard(title = "Детали турнира") {
        items.forEachIndexed { idx, item ->
            if (idx > 0) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Border.copy(0.1f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    item.icon, null,
                    tint = if (item.accent) Accent else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    item.label, fontSize = 14.sp, color = TextMuted,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    item.value, fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.accent) Accent else TextPrimary
                )
            }
        }
    }
}

@Composable
private fun LocationSection(location: LocationDto) {
    SectionCard(title = "Место проведения") {
        location.imageUrls?.firstOrNull()?.let { url ->
            AsyncImage(
                model = url, contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(12.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(location.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
            location.rating?.let { rating ->
                Text("★ ${"%.1f".format(rating)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF59E0B))
            }
        }
        val addressParts = listOfNotNull(location.address, location.city).filter { it.isNotEmpty() }
        if (addressParts.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(addressParts.joinToString(", "), fontSize = 13.sp, color = TextSecondary)
            }
        }
        location.type?.let { type ->
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(6.dp), color = AccentSoft) {
                Text(locationTypeLabel(type), fontSize = 11.sp, color = Accent, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
        }
        location.capacity?.let { cap ->
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.People, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Вместимость: $cap", fontSize = 12.sp, color = TextSecondary)
            }
        }
        if (!location.facilities.isNullOrEmpty()) {
            Spacer(Modifier.height(8.dp))
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                location.facilities.forEach { facility ->
                    Surface(shape = RoundedCornerShape(6.dp), color = CardBorder.copy(alpha = 0.5f)) {
                        Text(facility, fontSize = 11.sp, color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }
        }
        location.phone?.let { phone ->
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Phone, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(phone, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun PrizesSection(prizes: List<String>) {
    SectionCard(title = "Призы") {
        prizes.forEachIndexed { index, prize ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                Text("${index + 1}.", fontSize = 14.sp, modifier = Modifier.width(32.dp))
                Text(prize, fontSize = 14.sp, color = TextPrimary,
                    fontWeight = if (index < 3) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun ScheduleSection(scheduleJson: kotlinx.serialization.json.JsonElement) {
    val jsonParser = remember { Json { ignoreUnknownKeys = true } }
    val items = remember(scheduleJson) {
        try { jsonParser.decodeFromJsonElement<List<ScheduleItemDto>>(scheduleJson) }
        catch (_: Exception) { emptyList() }
    }
    if (items.isEmpty()) return

    val accentColor = Accent
    val accentFaded = Accent.copy(alpha = 0.2f)

    SectionCard(title = "Расписание") {
        items.forEachIndexed { index, item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(item.time, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = accentColor, modifier = Modifier.width(52.dp))
                Box(
                    modifier = Modifier.width(20.dp).height(IntrinsicSize.Min),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (index < items.size - 1) {
                        Box(Modifier.width(2.dp).fillMaxHeight().background(accentFaded))
                    }
                    Box(Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                }
                Spacer(Modifier.width(8.dp))
                Text(item.title, fontSize = 13.sp, color = TextPrimary,
                    modifier = Modifier.weight(1f).padding(bottom = if (index < items.size - 1) 16.dp else 0.dp))
            }
        }
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

    val buttonConfig = remember(user.role, status, regState) {
        getButtonConfig(user, status, regState, tournament.organizerId)
    }

    if (buttonConfig != null) {
        Box(
            modifier = modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (buttonConfig.isDestructive) Color.Transparent else Accent,
                    contentColor = if (buttonConfig.isDestructive) com.ileader.app.ui.theme.ILeaderColors.Error else Color.White
                ),
                border = if (buttonConfig.isDestructive) BorderStroke(1.dp, com.ileader.app.ui.theme.ILeaderColors.Error) else null
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                        color = if (buttonConfig.isDestructive) Color(0xFFEF4444) else Color.White
                    )
                } else {
                    Text(text = buttonConfig.label, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private enum class ButtonAction { REGISTER_PARTICIPANT, UNREGISTER, REGISTER_SPECTATOR, EDIT, REFEREE }

private data class ButtonConfig(val label: String, val action: ButtonAction, val isDestructive: Boolean = false)

private fun getButtonConfig(user: User, status: String, regState: RegistrationState, organizerId: String?): ButtonConfig? {
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
private fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    DarkCardPadded(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

// ══════════════════════════════════════════════════════════
// Tier Helpers
// ══════════════════════════════════════════════════════════

private fun tierOrder(tier: String?): Int = when (tier) {
    "title" -> 0; "gold" -> 1; "silver" -> 2; "bronze" -> 3; "media" -> 4; else -> 5
}

private fun tierDisplayName(tier: String?): String = when (tier) {
    "title" -> "Титульный"; "gold" -> "Золото"; "silver" -> "Серебро"; "bronze" -> "Бронза"; "media" -> "Медиа"; else -> "Партнёр"
}

@Composable
private fun tierColor(tier: String?): Color = when (tier) {
    "title" -> Accent; "gold" -> Color(0xFFCA8A04); "silver" -> Color(0xFF94A3B8)
    "bronze" -> Color(0xFFB45309); "media" -> Color(0xFF8B5CF6); else -> TextMuted
}

// ══════════════════════════════════════════════════════════
// Helpers
// ══════════════════════════════════════════════════════════

private fun formatDateShort(dateStr: String?): String {
    if (dateStr == null) return "—"
    val parts = dateStr.take(10).split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2].toIntOrNull() ?: return dateStr
    val monthNames = listOf("", "янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
    val month = parts[1].toIntOrNull() ?: return dateStr
    return "$day ${monthNames.getOrElse(month) { "" }}"
}

private fun formatDateFull(dateStr: String?): String {
    if (dateStr == null) return "—"
    val parts = dateStr.take(10).split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2].toIntOrNull() ?: return dateStr
    val monthNames = listOf("", "января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря")
    val month = parts[1].toIntOrNull() ?: return dateStr
    val year = parts[0]
    return "$day ${monthNames.getOrElse(month) { "" }} $year"
}

private fun getStatusLabel(status: String): String = when (status) {
    "in_progress" -> "Идёт сейчас"; "registration_open" -> "Регистрация"; "registration_closed" -> "Рег. закрыта"
    "check_in" -> "Check-in"; "completed" -> "Завершён"; "cancelled" -> "Отменён"; "draft" -> "Черновик"; else -> status
}

private fun formatLabel(format: String): String = when (format) {
    "single_elimination" -> "Single Elim"; "double_elimination" -> "Double Elim"
    "round_robin" -> "Round Robin"; "group_stage" -> "Групповой"
    "groups_knockout" -> "Группы + Плей-офф"; "swiss" -> "Швейцарская"; else -> format
}

private fun matchFormatLabel(mf: String): String = when (mf) {
    "bo1", "best_of_1" -> "Bo1 (1 игра)"
    "bo3", "best_of_3" -> "Bo3 (до 2 побед)"
    "bo5", "best_of_5" -> "Bo5 (до 3 побед)"
    "bo7", "best_of_7" -> "Bo7 (до 4 побед)"
    else -> mf
}

private fun seedingLabel(st: String): String = when (st) {
    "random" -> "Случайное"; "manual" -> "Ручное"; "rating" -> "По рейтингу"; else -> st
}

private fun ageCategoryLabel(age: String): String = when (age) {
    "children" -> "Дети (до 12)"
    "youth" -> "Юноши (13-17)"
    "adult" -> "Взрослые (18+)"
    "senior" -> "Ветераны"
    "open" -> "Без ограничений"
    else -> age
}

private fun skillLevelLabel(skill: String): String = when (skill) {
    "beginner" -> "Начинающий"
    "intermediate" -> "Средний"
    "advanced" -> "Продвинутый"
    "pro" -> "Профессионал"
    "open" -> "Любой уровень"
    else -> skill
}

private fun genderLabel(gender: String): String = when (gender) {
    "male" -> "Мужчины"
    "female" -> "Женщины"
    "mixed" -> "Смешанный"
    "open" -> "Без ограничений"
    else -> gender
}

private fun regionLabel(region: String): String = when (region.lowercase()) {
    "kz" -> "Казахстан"
    "ru" -> "Россия"
    "almaty" -> "Алматы"
    "astana", "nursultan" -> "Астана"
    "shymkent" -> "Шымкент"
    "karaganda" -> "Караганда"
    "aktau" -> "Актау"
    "atyrau" -> "Атырау"
    "cis" -> "СНГ"
    "international" -> "Международный"
    "online" -> "Онлайн"
    else -> region
}

private fun locationTypeLabel(type: String): String = when (type) {
    "track" -> "Трасса"; "stadium" -> "Стадион"; "arena" -> "Арена"; "court" -> "Корт"
    "pool" -> "Бассейн"; "gym" -> "Зал"; "field" -> "Поле"; "range" -> "Стрельбище"; "water" -> "Водоём"; else -> type
}
