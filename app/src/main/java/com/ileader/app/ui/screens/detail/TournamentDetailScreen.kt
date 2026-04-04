package com.ileader.app.ui.screens.detail

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
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
    onProfileClick: (String) -> Unit = {},
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
                    onEditTournament = onEditTournament,
                    onProfileClick = onProfileClick
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
    onEditTournament: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {}
) {
    val tournament = data.tournament

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
                        val scope = rememberCoroutineScope()

                        Box(
                            modifier = Modifier
                                .size(40.dp)
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

                    // Status + category pills
                    val pillModifier = @Composable { label: String, bg: Color ->
                        Surface(shape = RoundedCornerShape(50), color = bg) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }

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
                        // Status badge (LIVE pulsing for in_progress)
                        tournament.status?.let { status ->
                            val statusBg = when (status) {
                                "in_progress" -> accentColor.copy(0.9f)
                                "check_in" -> Color(0xFF7C3AED).copy(0.8f)
                                "completed" -> Color(0xFF22C55E).copy(0.7f)
                                else -> Color.White.copy(alpha = 0.2f)
                            }
                            HeroPill(getStatusLabel(status), statusBg)
                        }
                        // Sport
                        tournament.sports?.name?.let { sportName ->
                            Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.15f)) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(sportIcon(sportName), null, tint = Color.White.copy(0.9f), modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(sportName, fontSize = 11.sp, color = Color.White.copy(0.9f), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        // Age category
                        tournament.ageCategory?.let { age ->
                            val label = when (age) { "children" -> "Дети"; "youth" -> "Юноши"; "adult" -> "Взрослые"; else -> age }
                            HeroPill(label)
                        }
                        // Skill level
                        tournament.skillLevel?.let { skill ->
                            val label = when (skill) { "beginner" -> "Начинающие"; "intermediate" -> "Средний"; "pro" -> "Профи"; else -> skill }
                            HeroPill(label)
                        }
                        // Gender
                        tournament.genderCategory?.let { gender ->
                            val label = when (gender) { "male" -> "Мужчины"; "female" -> "Женщины"; "mixed" -> "Смешанный"; else -> gender }
                            HeroPill(label)
                        }
                        // Format
                        tournament.format?.let { fmt ->
                            val label = when (fmt) {
                                "single_elimination" -> "Single Elim"
                                "double_elimination" -> "Double Elim"
                                "round_robin" -> "Round Robin"
                                "groups_knockout" -> "Группы + Плей-офф"
                                "swiss" -> "Швейцарская"
                                else -> fmt
                            }
                            HeroPill(label)
                        }
                        // Private badge
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
                                Icons.Outlined.Business, null,
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
            

            // ── Quick Info Cards ──
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
                    modifier = Modifier.width(110.dp)
                )
                QuickInfoCard(
                    icon = Icons.Default.LocationOn,
                    label = "Место",
                    value = tournament.locations?.name ?: "—",
                    modifier = Modifier.width(110.dp)
                )
                QuickInfoCard(
                    icon = Icons.Default.People,
                    label = "Участники",
                    value = "${data.participants.size}/${tournament.maxParticipants ?: "∞"}",
                    modifier = Modifier.width(110.dp)
                )
                QuickInfoCard(
                    icon = Icons.Default.EmojiEvents,
                    label = "Приз",
                    value = if (!tournament.prize.isNullOrEmpty()) tournament.prize else "—",
                    modifier = Modifier.width(110.dp)
                )
                QuickInfoCard(
                    icon = Icons.Default.AccountTree,
                    label = "Формат",
                    value = formatShortLabel(tournament.format),
                    modifier = Modifier.width(110.dp)
                )
                tournament.entryFee?.let { fee ->
                    if (fee > 0) {
                        QuickInfoCard(
                            icon = Icons.Default.CreditCard,
                            label = "Взнос",
                            value = "${fee.toInt()} ₸",
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }
            }
            

            // ── Organizer Info Card ──
            tournament.profiles?.name?.let { orgName ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .clickable { tournament.organizerId?.let { onProfileClick(it) } },
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(44.dp).background(accentColor.copy(0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Business, null, tint = accentColor, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Организатор", fontSize = 11.sp, color = TextMuted)
                            Text(orgName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
                
                Spacer(Modifier.height(8.dp))
            }

            // ── Description ──
            if (!tournament.description.isNullOrEmpty()) {
                SectionCard(title = "Описание") {
                    Text(
                        text = tournament.description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 21.sp
                    )
                }
                
            }

            // ── Categories ──
            if (!tournament.categories.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
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

            // ── Requirements ──
            if (!tournament.requirements.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
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

            // ── Tournament Details (2-column grid) ──
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

            // ── Participants ──
            if (data.participants.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                ParticipantsSection(data.participants, onProfileClick)
                
            }

            // ── Podium (top 3) ──
            if (data.results.size >= 3 && tournament.status == "completed") {
                Spacer(Modifier.height(8.dp))
                PodiumSection(data.results.take(3), onProfileClick)
                
            }

            // ── Results ──
            if (data.results.isNotEmpty() && tournament.status == "completed") {
                Spacer(Modifier.height(8.dp))
                ResultsSection(data.results, onProfileClick)
                
            }

            // ── Bracket ──
            if (data.bracket.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                BracketSection(data)
                
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
private fun ParticipantsSection(participants: List<ParticipantDto>, onProfileClick: (String) -> Unit = {}) {
    SectionCard(title = "Участники (${participants.size})") {
        participants.take(12).forEachIndexed { idx, p ->
            val avatarUrl = p.profiles?.avatarUrl
            val name = p.profiles?.name ?: "—"

            if (idx > 0) HorizontalDivider(
                thickness = 0.5.dp,
                color = com.ileader.app.ui.theme.LocalAppColors.current.border.copy(0.15f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { p.athleteId?.let { onProfileClick(it) } }
                    .padding(vertical = 6.dp)
            ) {
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

                // Status badge
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
        if (participants.size > 12) {
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Accent.copy(0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Ещё ${participants.size - 12} участников",
                    Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                    fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Accent,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PodiumSection(topResults: List<ResultDto>, onProfileClick: (String) -> Unit = {}) {
    SectionCard(title = "Победители") {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // 2nd place (left)
            if (topResults.size >= 2) {
                PodiumPlace(
                    result = topResults[1],
                    position = 2,
                    height = 80.dp,
                    color = Color(0xFF94A3B8),
                    bgColor = Color(0xFFF1F5F9),
                    label = "Серебро",
                    onProfileClick = onProfileClick
                )
            }
            // 1st place (center, tallest)
            if (topResults.isNotEmpty()) {
                PodiumPlace(
                    result = topResults[0],
                    position = 1,
                    height = 100.dp,
                    color = Color(0xFFCA8A04),
                    bgColor = Color(0xFFFEF9C3),
                    label = "Золото",
                    onProfileClick = onProfileClick
                )
            }
            // 3rd place (right)
            if (topResults.size >= 3) {
                PodiumPlace(
                    result = topResults[2],
                    position = 3,
                    height = 64.dp,
                    color = Color(0xFFB45309),
                    bgColor = Color(0xFFFEF3C7),
                    label = "Бронза",
                    onProfileClick = onProfileClick
                )
            }
        }
    }
}

@Composable
private fun PodiumPlace(
    result: ResultDto,
    position: Int,
    height: androidx.compose.ui.unit.Dp,
    color: Color,
    bgColor: Color,
    label: String,
    onProfileClick: (String) -> Unit
) {
    val isDark = DarkTheme.isDark
    val actualBg = if (isDark) color.copy(alpha = 0.15f) else bgColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable { result.athleteId?.let { onProfileClick(it) } }
    ) {
        // Avatar
        val avatarUrl = result.profiles?.avatarUrl
        val name = result.profiles?.name ?: "—"
        Box(
            modifier = Modifier
                .size(if (position == 1) 56.dp else 44.dp)
                .clip(CircleShape)
                .background(color.copy(0.2f)),
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
        // Podium block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { r.athleteId?.let { onProfileClick(it) } }
                    .padding(vertical = 6.dp)
            ) {
                val posEmoji = when (r.position) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "${r.position}." }
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
                    Surface(shape = RoundedCornerShape(50), color = Accent.copy(0.12f)) {
                        Text(
                            text = "$pts очк.",
                            fontSize = 12.sp,
                            color = Accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
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
    val isDark = DarkTheme.isDark
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = CardBg,
        shadowElevation = 0.dp,
        border = if (isDark) androidx.compose.foundation.BorderStroke(
            1.dp, com.ileader.app.ui.theme.LocalAppColors.current.border.copy(0.2f)
        ) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Accent.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = Accent, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(text = label, fontSize = 11.sp, color = TextMuted)
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
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
            add(Triple(Icons.Outlined.AccountTree, "Формат", formatLabel(it)))
        }
        tournament.matchFormat?.let {
            add(Triple(Icons.Outlined.SportsScore, "Формат матча", matchFormatLabel(it)))
        }
        tournament.seedingType?.let {
            add(Triple(Icons.Outlined.Shuffle, "Посев", seedingLabel(it)))
        }
        tournament.ageCategory?.let {
            add(Triple(Icons.Outlined.Cake, "Возраст", it))
        }
        tournament.discipline?.let {
            add(Triple(Icons.Outlined.FitnessCenter, "Дисциплина", it))
        }
        tournament.region?.let {
            add(Triple(Icons.Outlined.Public, "Регион", it))
        }
        if (tournament.endDate != null && tournament.endDate != tournament.startDate) {
            add(Triple(Icons.Outlined.EventBusy, "Окончание", formatDateShort(tournament.endDate)))
        }
        tournament.registrationDeadline?.let {
            add(Triple(Icons.Outlined.HowToReg, "Дедлайн рег.", formatDateShort(it)))
        }
        tournament.minParticipants?.let {
            add(Triple(Icons.Outlined.GroupRemove, "Мин. участников", it.toString()))
        }
        if (tournament.hasCheckIn == true) {
            val mins = tournament.checkInStartsBefore
            add(Triple(Icons.Outlined.QrCodeScanner, "Check-in", if (mins != null) "За $mins мин" else "Да"))
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
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(12.dp))
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
                Icon(Icons.Outlined.LocationOn, null, tint = TextMuted, modifier = Modifier.size(14.dp))
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
                Icon(Icons.Outlined.People, null, tint = TextMuted, modifier = Modifier.size(14.dp))
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
                Icon(Icons.Outlined.Phone, null, tint = TextMuted, modifier = Modifier.size(14.dp))
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
                val medal = "${index + 1}."
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
