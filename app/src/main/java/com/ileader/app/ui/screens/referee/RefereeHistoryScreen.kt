package com.ileader.app.ui.screens.referee

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.RefereeHistoryViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun RefereeHistoryScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    val viewModel: RefereeHistoryViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> HistoryContent(s.data)
    }
}

@Composable
private fun HistoryContent(
    data: com.ileader.app.ui.viewmodels.RefereeHistoryData
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf("all") }

    val history = data.history
    val allViolations = data.violations
    val totalTournaments = history.size
    val totalParticipants = history.sumOf { it.participants }
    val totalViolations = allViolations.size

    val filteredHistory = history.filter { t ->
        (searchQuery.isBlank() || t.name.contains(searchQuery, ignoreCase = true)) &&
                (selectedSport == "all" || t.sportId == selectedSport)
    }
    val sports = history.map { it.sportId to it.sport }.distinct()

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
                Text("История судейства", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(4.dp))
                Text("Все завершенные турниры и статистика", fontSize = 14.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(28.dp))

            // ── STATS ──
            FadeIn(visible, 200) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HistoryStatItem(Modifier.weight(1f), Icons.Default.EmojiEvents, "$totalTournaments", "Турниров")
                    HistoryStatItem(Modifier.weight(1f), Icons.Default.People, "$totalParticipants", "Участников")
                    HistoryStatItem(Modifier.weight(1f), Icons.Default.Shield, "$totalViolations", "Нарушений")
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── MONTHLY STATS ──
            FadeIn(visible, 300) {
                MonthlyStatsCard(data.monthlyStats)
            }

            Spacer(Modifier.height(28.dp))

            // ── SEARCH ──
            FadeIn(visible, 400) {
                DarkSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Поиск по названию..."
                )

                Spacer(Modifier.height(12.dp))

                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DarkFilterChip(text = "Все", selected = selectedSport == "all", onClick = { selectedSport = "all" })
                    sports.forEach { (id, name) ->
                        DarkFilterChip(text = name, selected = selectedSport == id, onClick = { selectedSport = id })
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text("Найдено: ${filteredHistory.size}", fontSize = 12.sp, color = TextMuted)
            }

            Spacer(Modifier.height(16.dp))

            // ── LIST ──
            FadeIn(visible, 500) {
                if (filteredHistory.isEmpty()) {
                    EmptyState("Турниры не найдены")
                } else {
                    filteredHistory.forEach { tournament ->
                        HistoryTournamentCard(tournament, allViolations)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HistoryStatItem(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = DarkTheme.CardBg) {
        Column(
            Modifier.border(0.5.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp)).padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SoftIconBox(icon, size = 32.dp, iconSize = 16.dp)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                color = TextPrimary, letterSpacing = (-0.3).sp)
            Text(label, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
private fun MonthlyStatsCard(monthly: List<RefereeMonthlyStats>) {
    DarkCardPadded(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SoftIconBox(Icons.Default.BarChart, size = 36.dp, iconSize = 18.dp)
            Spacer(Modifier.width(10.dp))
            Text("Статистика по месяцам", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                color = TextPrimary, letterSpacing = (-0.3).sp)
        }
        Spacer(Modifier.height(12.dp))

        if (monthly.isEmpty()) {
            Text("Нет данных", fontSize = 13.sp, color = TextMuted)
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                monthly.forEach { stat ->
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                        color = CardBorder.copy(alpha = 0.3f)) {
                        Column(
                            Modifier.border(0.5.dp, CardBorder.copy(alpha = 0.3f), RoundedCornerShape(10.dp)).padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(stat.month.take(3), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            Text("${stat.tournaments}", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                color = TextPrimary, letterSpacing = (-0.3).sp)
                            Text("${stat.violations} нар.", fontSize = 10.sp, color = Accent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTournamentCard(tournament: RefereeTournament, allViolations: List<RefereeViolation>) {
    var expanded by remember { mutableStateOf(false) }
    val tournamentViolations = allViolations.filter { it.tournamentId == tournament.id }

    DarkCardPadded(padding = 14.dp) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                AccentIconBox(Icons.Default.CheckCircle, size = 36.dp, iconSize = 18.dp)
                Spacer(Modifier.width(10.dp))
                Text(tournament.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (tournamentViolations.isNotEmpty()) {
                StatusBadge("${tournamentViolations.size} нар.", Accent)
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, null, Modifier.size(14.dp), TextMuted)
                Spacer(Modifier.width(4.dp))
                Text(tournament.sport, fontSize = 12.sp, color = TextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, Modifier.size(14.dp), TextMuted)
                Spacer(Modifier.width(4.dp))
                Text(tournament.date, fontSize = 12.sp, color = TextSecondary)
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), TextMuted)
            Spacer(Modifier.width(4.dp))
            Text(tournament.location.take(25), fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        tournament.rating?.let { rating ->
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, Modifier.size(16.dp), Accent)
                Spacer(Modifier.width(4.dp))
                Text("$rating", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                tournament.feedback?.let {
                    Spacer(Modifier.width(6.dp))
                    Text("— $it", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        // Expand
        Spacer(Modifier.height(10.dp))
        Surface(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            shape = RoundedCornerShape(8.dp), color = CardBorder.copy(alpha = 0.3f)) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), Arrangement.Center, Alignment.CenterVertically) {
                Text(if (expanded) "Свернуть" else "Детали",
                    fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                Spacer(Modifier.width(4.dp))
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null, Modifier.size(18.dp), TextSecondary)
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = 12.dp)) {
                if (tournament.matchesTotal > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Матчи: ", fontSize = 12.sp, color = TextMuted)
                        Text("${tournament.matchesCompleted}/${tournament.matchesTotal}", fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    }
                    Spacer(Modifier.height(6.dp))
                    DarkProgressBar(1f)
                    Spacer(Modifier.height(10.dp))
                }

                if (tournamentViolations.isNotEmpty()) {
                    val warnings = tournamentViolations.count { it.severity == ViolationSeverity.WARNING }
                    val penalties = tournamentViolations.count { it.severity == ViolationSeverity.PENALTY }
                    val disqualifications = tournamentViolations.count { it.severity == ViolationSeverity.DISQUALIFICATION }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ViolationStatBox("Предупр.", warnings, Modifier.weight(1f))
                        ViolationStatBox("Штрафы", penalties, Modifier.weight(1f))
                        ViolationStatBox("Дискв.", disqualifications, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    tournamentViolations.forEach { v ->
                        ViolationCardCompact(v)
                        Spacer(Modifier.height(6.dp))
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SoftIconBox(Icons.Default.VerifiedUser, size = 28.dp, iconSize = 16.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Без нарушений", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ViolationStatBox(label: String, count: Int, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = Accent.copy(alpha = 0.08f)) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$count", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Accent, letterSpacing = (-0.3).sp)
            Text(label, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun ViolationCardCompact(violation: RefereeViolation) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = CardBorder.copy(alpha = 0.3f)
    ) {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SoftIconBox(Icons.Default.Warning, size = 30.dp, iconSize = 16.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(violation.participantName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(violation.description, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            StatusBadge(violation.severity.label, Accent)
        }
    }
}
