package com.ileader.app.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.SportsKabaddi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.MatchGameDto
import com.ileader.app.data.remote.dto.MatchResultUpdateDto
import com.ileader.app.data.repository.RefereeRepository
import com.ileader.app.ui.components.*
import com.ileader.app.ui.components.bracket.MatchDetailDialog
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

class RefereeMatchesViewModel : ViewModel() {
    private val repo = RefereeRepository()

    var state by mutableStateOf<UiState<List<RefereeMyMatch>>>(UiState.Loading)
        private set

    var snackbarMessage by mutableStateOf<String?>(null)
        private set

    fun clearSnackbar() { snackbarMessage = null }

    fun load(userId: String) {
        viewModelScope.launch {
            state = UiState.Loading
            try {
                state = UiState.Success(repo.getMyMatches(userId))
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Ошибка загрузки матчей")
            }
        }
    }

    fun saveResult(userId: String, matchId: String, games: List<MatchGame>, winnerId: String) {
        viewModelScope.launch {
            try {
                val currentMatches = (state as? UiState.Success)?.data ?: return@launch
                val match = currentMatches.find { it.matchId == matchId } ?: return@launch
                val p1Wins = games.count { it.participant1Score > it.participant2Score }
                val p2Wins = games.count { it.participant2Score > it.participant1Score }
                val loserId = if (winnerId == match.participant1Id) match.participant2Id else match.participant1Id

                repo.updateMatchResult(
                    matchId,
                    MatchResultUpdateDto(
                        participant1Score = p1Wins,
                        participant2Score = p2Wins,
                        games = games.map { g ->
                            MatchGameDto(g.gameNumber, g.participant1Score, g.participant2Score, g.winnerId, g.status)
                        },
                        winnerId = winnerId,
                        loserId = loserId,
                        status = "completed"
                    )
                )
                // Auto-advance: fetch fresh bracket for this tournament to find next/loser matches
                // Simpler: rely on organizer to regenerate; skip auto-advance here
                snackbarMessage = "Результат сохранён"
                load(userId)
            } catch (e: Exception) {
                snackbarMessage = e.message ?: "Ошибка сохранения"
            }
        }
    }

    fun revertResult(userId: String, matchId: String) {
        viewModelScope.launch {
            try {
                repo.updateMatchResult(
                    matchId,
                    MatchResultUpdateDto(
                        participant1Score = 0,
                        participant2Score = 0,
                        games = null,
                        winnerId = null,
                        loserId = null,
                        status = "scheduled"
                    )
                )
                snackbarMessage = "Результат отменён"
                load(userId)
            } catch (e: Exception) {
                snackbarMessage = e.message ?: "Ошибка отката"
            }
        }
    }
}

private enum class MatchFilter(val label: String, val matches: (RefereeMyMatch) -> Boolean) {
    PENDING("Ожидают", { it.status == "scheduled" || it.status == "in_progress" }),
    COMPLETED("Завершены", { it.status == "completed" }),
    ALL("Все", { true })
}

@Composable
fun RefereeMatchesScreen(
    userId: String,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit = {}
) {
    val vm: RefereeMatchesViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    var filter by remember { mutableStateOf(MatchFilter.PENDING) }
    var selectedMatch by remember { mutableStateOf<BracketMatch?>(null) }

    LaunchedEffect(userId) { vm.load(userId) }

    LaunchedEffect(vm.snackbarMessage) {
        vm.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSnackbar()
        }
    }

    Scaffold(
        containerColor = Bg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(Bg)) {
            BackHeader("Мои матчи", onBack)

            // ── Filter tabs ──
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MatchFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f.label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Accent,
                            selectedLabelColor = Color.White,
                            containerColor = CardBg,
                            labelColor = TextSecondary
                        ),
                        border = null
                    )
                }
            }

            when (val s = vm.state) {
                is UiState.Loading -> LoadingScreen()
                is UiState.Error -> ErrorScreen(s.message) { vm.load(userId) }
                is UiState.Success -> {
                    val filtered = s.data.filter(filter.matches)
                    if (filtered.isEmpty()) {
                        EmptyState(
                            icon = Icons.Outlined.SportsKabaddi,
                            title = "Нет матчей",
                            subtitle = when (filter) {
                                MatchFilter.PENDING -> "Все назначенные матчи уже завершены"
                                MatchFilter.COMPLETED -> "Пока нет завершённых матчей"
                                MatchFilter.ALL -> "Вы не назначены ни на один матч"
                            }
                        )
                    } else {
                        // Group by tournament for readability
                        val grouped = filtered.groupBy { it.tournamentId }
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            grouped.forEach { (tId, matches) ->
                                val first = matches.first()
                                item(key = "header-$tId") {
                                    TournamentHeader(
                                        name = first.tournamentName,
                                        sport = first.sportName,
                                        pending = matches.count { it.status != "completed" },
                                        total = matches.size,
                                        onClick = { onTournamentClick(tId) }
                                    )
                                }
                                items(matches, key = { it.matchId }) { m ->
                                    MatchRow(match = m, onClick = {
                                        selectedMatch = m.toBracketMatch()
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedMatch?.let { match ->
        MatchDetailDialog(
            match = match,
            canEdit = true,
            onDismiss = { selectedMatch = null },
            onSaveResult = { matchId, games, winnerId ->
                vm.saveResult(userId, matchId, games, winnerId)
                selectedMatch = null
            },
            onRevert = { matchId ->
                vm.revertResult(userId, matchId)
                selectedMatch = null
            }
        )
    }
}

@Composable
private fun TournamentHeader(
    name: String,
    sport: String,
    pending: Int,
    total: Int,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardBg,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.EmojiEvents, null, tint = Accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(sport, fontSize = 12.sp, color = TextMuted)
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = if (pending > 0) Accent.copy(0.12f) else Color(0xFF22C55E).copy(0.12f)
            ) {
                Text(
                    if (pending > 0) "$pending / $total" else "✓ $total",
                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (pending > 0) Accent else Color(0xFF22C55E)
                )
            }
        }
    }
}

@Composable
private fun MatchRow(match: RefereeMyMatch, onClick: () -> Unit) {
    val (statusText, statusColor) = when (match.status) {
        "completed" -> "Завершён" to Color(0xFF22C55E)
        "in_progress" -> "Идёт" to Color(0xFFF59E0B)
        else -> "Ожидает" to TextMuted
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardBg,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Матч №${match.matchNumber}",
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted
                )
                Spacer(Modifier.width(8.dp))
                if (match.groupId != null) {
                    Surface(shape = RoundedCornerShape(50), color = Color(0xFF3B82F6).copy(0.12f)) {
                        Text(
                            "Группа",
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3B82F6)
                        )
                    }
                } else {
                    Surface(shape = RoundedCornerShape(50), color = Accent.copy(0.12f)) {
                        Text(
                            "Плей-офф",
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Accent
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                    Text(
                        statusText,
                        Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        match.participant1Name ?: "—",
                        fontSize = 14.sp,
                        fontWeight = if (match.winnerId == match.participant1Id) FontWeight.Bold else FontWeight.Medium,
                        color = if (match.winnerId == match.participant1Id) Accent else TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        match.participant2Name ?: "—",
                        fontSize = 14.sp,
                        fontWeight = if (match.winnerId == match.participant2Id) FontWeight.Bold else FontWeight.Medium,
                        color = if (match.winnerId == match.participant2Id) Accent else TextPrimary
                    )
                }
                if (match.status == "completed") {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${match.participant1Score}",
                            fontSize = 20.sp, fontWeight = FontWeight.Bold,
                            color = if (match.winnerId == match.participant1Id) Accent else TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${match.participant2Score}",
                            fontSize = 20.sp, fontWeight = FontWeight.Bold,
                            color = if (match.winnerId == match.participant2Id) Accent else TextPrimary
                        )
                    }
                } else {
                    Icon(
                        Icons.Filled.Edit, null,
                        tint = TextMuted, modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/** Adapt the domain model to the shared [BracketMatch] used by MatchDetailDialog. */
private fun RefereeMyMatch.toBracketMatch(): BracketMatch = BracketMatch(
    id = matchId,
    tournamentId = tournamentId,
    round = round,
    matchNumber = matchNumber,
    bracketType = BracketType.fromString(bracketType),
    participant1Id = participant1Id,
    participant2Id = participant2Id,
    participant1Name = participant1Name,
    participant2Name = participant2Name,
    participant1Score = participant1Score,
    participant2Score = participant2Score,
    games = games,
    winnerId = winnerId,
    status = MatchStatus.fromString(status),
    groupId = groupId,
    isBye = isBye
)
