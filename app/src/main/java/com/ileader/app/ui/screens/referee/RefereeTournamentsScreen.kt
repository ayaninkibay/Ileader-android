package com.ileader.app.ui.screens.referee

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.RefereeTournamentsViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun RefereeTournamentsScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    // Internal navigation state
    var subScreen by remember { mutableStateOf<String?>(null) }

    when {
        subScreen?.startsWith("detail:") == true -> {
            val tournamentId = subScreen?.removePrefix("detail:") ?: return
            RefereeTournamentDetailScreen(
                user = user,
                tournamentId = tournamentId,
                onBack = { subScreen = null },
                onNavigateToResults = { subScreen = "results:$tournamentId" }
            )
            return
        }
        subScreen?.startsWith("results:") == true -> {
            val tournamentId = subScreen?.removePrefix("results:") ?: return
            RefereeTournamentResultsScreen(
                user = user,
                tournamentId = tournamentId,
                onBack = { subScreen = "detail:$tournamentId" }
            )
            return
        }
    }

    val viewModel: RefereeTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> TournamentsListContent(s.data.all) { subScreen = "detail:$it" }
    }
}

@Composable
private fun TournamentsListContent(
    allTournaments: List<RefereeTournament>,
    onTournamentClick: (String) -> Unit
) {
    var selectedFilter by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("Все", "Активные", "Предстоящие", "Завершённые")

    val filtered = allTournaments.filter { t ->
        val matchesFilter = when (selectedFilter) {
            1 -> t.status == TournamentStatus.IN_PROGRESS
            2 -> t.status in listOf(TournamentStatus.REGISTRATION_OPEN, TournamentStatus.REGISTRATION_CLOSED, TournamentStatus.CHECK_IN)
            3 -> t.status == TournamentStatus.COMPLETED
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() || t.name.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val accentColor = Accent
    Box(Modifier.fillMaxSize().background(Bg)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(accentColor.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.03f),
                    radius = 280.dp.toPx()
                ),
                radius = 280.dp.toPx(),
                center = Offset(size.width * 0.85f, size.height * 0.03f)
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 0) {
                Text("Мои турниры", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(4.dp))
                Text("Назначенные и прошедшие", fontSize = 14.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(28.dp))

            // ── SEARCH ──
            FadeIn(visible, 200) {
                DarkSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Поиск турниров..."
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── FILTER CHIPS ──
            FadeIn(visible, 300) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEachIndexed { index, filter ->
                        DarkFilterChip(filter, selectedFilter == index, { selectedFilter = index })
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Найдено: ${filtered.size}", fontSize = 12.sp, color = TextMuted)
            }

            Spacer(Modifier.height(16.dp))

            // ── LIST ──
            FadeIn(visible, 400) {
                if (filtered.isEmpty()) {
                    EmptyState("Турниры не найдены")
                } else {
                    filtered.forEach { tournament ->
                        TournamentListCard(tournament) { onTournamentClick(tournament.id) }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TournamentListCard(tournament: RefereeTournament, onClick: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }
    val isActive = tournament.status == TournamentStatus.IN_PROGRESS
    val chipColor = if (isActive) Accent else TextMuted

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = DarkTheme.cardBorderStroke
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AccentIconBox(Icons.Default.EmojiEvents)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(tournament.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(Modifier.height(3.dp))
                    Text("${tournament.sport} · ${tournament.date}", fontSize = 12.sp, color = TextSecondary)
                }
                StatusBadge(tournament.status.displayName, chipColor)
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(8.dp), color = AccentSoft) {
                    Text(tournament.refereeRole.label, Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), TextMuted)
                    Spacer(Modifier.width(4.dp))
                    Text(tournament.location.take(20), fontSize = 12.sp, color = TextSecondary)
                }
            }

            // Progress
            if (tournament.matchesTotal > 0 && tournament.status != TournamentStatus.REGISTRATION_OPEN) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Матчи: ", fontSize = 12.sp, color = TextMuted)
                    Text("${tournament.matchesCompleted}/${tournament.matchesTotal}", fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold, color = TextSecondary)
                }
                Spacer(Modifier.height(6.dp))
                val progress = if (tournament.matchesTotal > 0) tournament.matchesCompleted.toFloat() / tournament.matchesTotal else 0f
                DarkProgressBar(progress)
            }

            // Expand
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                shape = RoundedCornerShape(8.dp),
                color = CardBorder.copy(alpha = 0.3f)
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), Arrangement.Center, Alignment.CenterVertically) {
                    Text(if (expanded) "Свернуть" else "Подробнее",
                        fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    Spacer(Modifier.width(4.dp))
                    Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        null, Modifier.size(18.dp), TextSecondary)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, null, Modifier.size(14.dp), TextMuted)
                            Spacer(Modifier.width(4.dp))
                            Text("${tournament.participants} участников", fontSize = 12.sp, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, Modifier.size(14.dp), TextMuted)
                            Spacer(Modifier.width(4.dp))
                            Text(tournament.date, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
