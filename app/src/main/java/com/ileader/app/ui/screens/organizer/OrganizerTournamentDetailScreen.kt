package com.ileader.app.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.ileader.app.data.models.MatchGame
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.ui.components.*
import com.ileader.app.ui.components.bracket.BracketView
import com.ileader.app.ui.components.bracket.MatchDetailDialog
import com.ileader.app.ui.viewmodels.*

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun OrganizerTournamentDetailScreen(
    tournamentId: String,
    userId: String,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onResultsClick: () -> Unit,
    onCheckInClick: (tournamentName: String) -> Unit = {}
) {
    val vm: OrganizerTournamentDetailViewModel = viewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(tournamentId) { vm.load(tournamentId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.load(tournamentId) }
        is UiState.Success -> DetailContent(s.data, vm, tournamentId, onBack, onEditClick, onResultsClick, onCheckInClick)
    }
}

@Composable
private fun DetailContent(
    data: TournamentDetailData,
    vm: OrganizerTournamentDetailViewModel,
    tournamentId: String,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onResultsClick: () -> Unit,
    onCheckInClick: (String) -> Unit = {}
) {
    val tournament = data.tournament
    val participants = data.participants
    var selectedTab by remember { mutableIntStateOf(0) }
    val generating by vm.generating.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Spacer(Modifier.height(8.dp))

            // Top bar
            BackHeader(tournament.name, onBack)

            // Subtitle
            Text(
                "${tournament.sports?.name ?: ""} • ${tournament.locations?.name ?: ""}",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Status + dates
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isActive = isActiveStatus(tournament.status)
                StatusBadge(statusLabel(tournament.status), if (isActive) Accent else TextMuted)
                if (tournament.visibility == "private") {
                    StatusBadge("Приватный", TextSecondary)
                }
                Spacer(Modifier.weight(1f))
                val endDate = tournament.endDate
                Text(
                    "${formatShortDate(tournament.startDate)}${if (endDate != null) " — ${formatShortDate(endDate)}" else ""}",
                    color = TextSecondary, fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Редактировать")
                }
                OutlinedButton(
                    onClick = onResultsClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = DarkTheme.cardBorderStroke,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Leaderboard, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Результаты")
                }
            }

            // QR Check-in button
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onCheckInClick(tournament.name) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("QR Check-in")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Accent, height = 3.dp
                        )
                    }
                },
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text("Обзор") }, selectedContentColor = Accent, unselectedContentColor = TextSecondary)
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text("Участники (${participants.size})") }, selectedContentColor = Accent, unselectedContentColor = TextSecondary)
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 },
                    text = { Text("Сетка") }, selectedContentColor = Accent, unselectedContentColor = TextSecondary)
            }

            // Tab content
            when (selectedTab) {
                2 -> {
                    // Bracket tab — not wrapped in verticalScroll since BracketView handles its own scroll
                    BracketTab(data, vm, tournamentId, generating)
                }
                else -> {
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
                    ) {
                        Spacer(Modifier.height(16.dp))
                        when (selectedTab) {
                            0 -> OverviewTab(tournament, participants.size)
                            1 -> ParticipantsTab(participants, vm, tournamentId)
                        }
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BracketTab(
    data: TournamentDetailData,
    vm: OrganizerTournamentDetailViewModel,
    tournamentId: String,
    generating: Boolean
) {
    val bracketDtos = data.bracket
    val matches = BracketUtils.mapDtosToMatches(bracketDtos, data.participants)
    val groups = BracketUtils.mapGroupDtos(data.groups)
    val format = data.tournament.format ?: "single_elimination"

    var selectedMatch by remember { mutableStateOf<BracketMatch?>(null) }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 8.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        if (matches.isEmpty()) {
            // No bracket yet — show generate button
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(40.dp))
                Icon(
                    Icons.Default.AccountTree, null,
                    modifier = Modifier.size(64.dp),
                    tint = TextMuted.copy(alpha = 0.4f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Турнирная сетка ещё не создана",
                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                val confirmed = data.participants.count { it.status == "confirmed" }
                Text(
                    "Подтверждённых участников: $confirmed",
                    fontSize = 13.sp, color = TextMuted
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { vm.generateBracket(tournamentId) },
                    enabled = !generating && confirmed >= 2,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                ) {
                    if (generating) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Генерация...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Сгенерировать сетку", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            // Bracket exists — show it with regenerate option
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val completed = matches.count { it.status == com.ileader.app.data.models.MatchStatus.COMPLETED }
                Text(
                    "Матчей: ${matches.size} (завершено: $completed)",
                    fontSize = 12.sp, color = TextMuted
                )
                TextButton(
                    onClick = { vm.generateBracket(tournamentId) },
                    enabled = !generating
                ) {
                    Icon(Icons.Default.Refresh, null, Modifier.size(16.dp), tint = Accent)
                    Spacer(Modifier.width(4.dp))
                    Text("Пересоздать", fontSize = 12.sp, color = Accent)
                }
            }

            BracketView(
                format = format,
                matches = matches,
                groups = groups,
                onMatchClick = { selectedMatch = it }
            )
        }
    }

    // Match detail dialog
    selectedMatch?.let { match ->
        MatchDetailDialog(
            match = match,
            canEdit = true,
            onDismiss = { selectedMatch = null },
            onSaveResult = { matchId, games, winnerId ->
                val p1Score = games.sumOf { it.participant1Score }
                val p2Score = games.sumOf { it.participant2Score }
                val gameDtos = games.map { g ->
                    MatchGameDto(
                        gameNumber = g.gameNumber,
                        participant1Score = g.participant1Score,
                        participant2Score = g.participant2Score,
                        winnerId = g.winnerId,
                        status = g.status
                    )
                }
                val loserId = if (winnerId == match.participant1Id) match.participant2Id else match.participant1Id
                vm.updateMatchResult(matchId, MatchResultUpdateDto(
                    participant1Score = p1Score,
                    participant2Score = p2Score,
                    games = gameDtos,
                    winnerId = winnerId,
                    loserId = loserId,
                    status = "completed"
                ))
            }
        )
    }
}

@Composable
private fun OverviewTab(tournament: TournamentDto, participantCount: Int) {
    val maxP = tournament.maxParticipants ?: 0
    val participantsText = if (maxP > 0) "$participantCount/$maxP" else participantCount.toString()

    // Info chips
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoChip(Icons.Default.SportsSoccer, tournament.sports?.name ?: "")
        InfoChip(Icons.Default.Groups, participantsText)
        val format = tournament.format
        if (!format.isNullOrEmpty()) InfoChip(Icons.Default.AccountTree, format)
    }

    Spacer(Modifier.height(28.dp))

    val description = tournament.description
    if (!description.isNullOrEmpty()) {
        SectionCard("О турнире") {
            Text(description, fontSize = 14.sp, color = TextSecondary, lineHeight = 20.sp)
        }
        Spacer(Modifier.height(28.dp))
    }

    val prize = tournament.prize
    if (!prize.isNullOrEmpty()) {
        SectionCard("Призовой фонд") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SoftIconBox(Icons.Default.Paid)
                Spacer(Modifier.width(12.dp))
                Text(prize, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
        Spacer(Modifier.height(28.dp))
    }

    val requirements = tournament.requirements
    if (!requirements.isNullOrEmpty()) {
        SectionCard("Требования") {
            requirements.forEach { req ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Icon(Icons.Default.CheckCircle, null, tint = Accent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(req, fontSize = 13.sp, color = TextSecondary)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }

    SectionCard("Статус турнира") {
        StatusPipeline(tournament.status)
    }
}

@Composable
private fun ParticipantsTab(
    participants: List<ParticipantDto>,
    vm: OrganizerTournamentDetailViewModel,
    tournamentId: String
) {
    val confirmed = participants.count { it.status == "confirmed" }
    val pending = participants.count { it.status == "pending" }
    val cancelled = participants.count { it.status == "cancelled" }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MiniStat("Подтверждено", confirmed.toString(), Modifier.weight(1f))
        MiniStat("Ожидает", pending.toString(), Modifier.weight(1f))
        MiniStat("Отменено", cancelled.toString(), Modifier.weight(1f))
    }

    Spacer(Modifier.height(20.dp))

    participants.forEach { participant ->
        ParticipantCard(participant) { action ->
            if (action == "approve") {
                vm.approveParticipant(tournamentId, participant.athleteId)
            } else {
                vm.declineParticipant(tournamentId, participant.athleteId)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ParticipantCard(
    participant: ParticipantDto,
    onAction: (String) -> Unit
) {
    val name = participant.profiles?.name?.takeIf { it.isNotBlank() } ?: "Участник"
    val team = participant.teams?.name ?: ""
    val isActive = participant.status == "confirmed"
    val chipColor = if (isActive) Accent else TextMuted

    DarkCard {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(AccentSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(name.first().toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Accent)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                if (team.isNotEmpty()) {
                    Text(team, fontSize = 12.sp, color = TextSecondary)
                }
            }
            StatusBadge(
                when (participant.status) {
                    "confirmed" -> "Подтверждён"
                    "pending" -> "Ожидает"
                    "cancelled" -> "Отменён"
                    else -> participant.status ?: "—"
                },
                chipColor
            )
            if (participant.status == "pending") {
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = { onAction("approve") }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Check, "Одобрить", tint = Accent, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { onAction("decline") }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, "Отклонить", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusPipeline(currentStatus: String?) {
    val steps = listOf(
        "draft" to "Черновик",
        "registration_open" to "Регистрация",
        "registration_closed" to "Рег. закрыта",
        "check_in" to "Check-in",
        "in_progress" to "Идёт",
        "completed" to "Завершён"
    )
    val currentIndex = steps.indexOfFirst { it.first == currentStatus }.coerceAtLeast(0)

    Column {
        steps.forEachIndexed { index, (_, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape)
                        .background(
                            if (index <= currentIndex)
                                Brush.linearGradient(listOf(Accent, AccentDark))
                            else
                                Brush.linearGradient(listOf(TextMuted.copy(alpha = 0.3f), TextMuted.copy(alpha = 0.3f)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < currentIndex) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else if (index == currentIndex) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    label, fontSize = 13.sp,
                    fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal,
                    color = if (index <= currentIndex) TextPrimary else TextMuted
                )
            }
            if (index < steps.lastIndex) {
                Box(
                    Modifier.padding(start = 11.dp).width(2.dp).height(16.dp)
                        .background(if (index < currentIndex) Accent else TextMuted.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    DarkCardPadded {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = AccentSoft,
        border = DarkTheme.cardBorderStroke
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, fontSize = 12.sp, color = Accent, fontWeight = FontWeight.Medium)
        }
    }
}
