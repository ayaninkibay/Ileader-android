package com.ileader.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.bracket.BracketUtils
import com.ileader.app.data.models.BracketMatch
import com.ileader.app.data.models.User
import com.ileader.app.data.models.UserRole
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ParticipantDto
import com.ileader.app.data.remote.dto.ResultDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.components.bracket.BracketView
import com.ileader.app.ui.components.bracket.MatchDetailDialog
import com.ileader.app.ui.viewmodels.RegistrationState
import com.ileader.app.ui.viewmodels.HomeTournamentDetailData
import com.ileader.app.ui.viewmodels.TournamentDetailViewModel

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
                    onBack = onBack
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
    onBack: () -> Unit
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
            // ── Hero Header (inzhu style) ──
            FadeIn(visible = started, delayMs = 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFC62828),
                                    accentColor,
                                    Color(0xFFFF8A80)
                                ),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(
                                    Float.POSITIVE_INFINITY,
                                    Float.POSITIVE_INFINITY
                                )
                            ),
                            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                        )
                        .statusBarsPadding()
                        .padding(top = 12.dp, bottom = 28.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        // Top row: back + share
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Back button (white circle like inzhu)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { onBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack, "Назад",
                                    tint = accentDarkColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            // Share button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
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
                                    Text(
                                        text = sportEmoji(sportName) + " " + sportName,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
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
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickInfoCard(
                        icon = Icons.Default.CalendarMonth,
                        label = "Дата",
                        value = formatDateShort(tournament.startDate),
                        modifier = Modifier.weight(1f)
                    )
                    QuickInfoCard(
                        icon = Icons.Default.LocationOn,
                        label = "Место",
                        value = tournament.locations?.name ?: "—",
                        modifier = Modifier.weight(1f)
                    )
                    QuickInfoCard(
                        icon = Icons.Default.People,
                        label = "Участники",
                        value = "${data.participants.size}/${tournament.maxParticipants ?: "∞"}",
                        modifier = Modifier.weight(1f)
                    )
                    QuickInfoCard(
                        icon = Icons.Default.EmojiEvents,
                        label = "Приз",
                        value = if (!tournament.prize.isNullOrEmpty()) tournament.prize else "—",
                        modifier = Modifier.weight(1f)
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

            // ── Participants ──
            if (data.participants.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FadeIn(visible = started, delayMs = 450) {
                    ParticipantsSection(data.participants)
                }
            }

            // ── Results ──
            if (data.results.isNotEmpty() && tournament.status == "completed") {
                Spacer(Modifier.height(8.dp))
                FadeIn(visible = started, delayMs = 500) {
                    ResultsSection(data.results)
                }
            }

            // ── Bracket ──
            if (data.bracket.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FadeIn(visible = started, delayMs = 550) {
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
                    text = p.profiles?.name ?: "—",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
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
