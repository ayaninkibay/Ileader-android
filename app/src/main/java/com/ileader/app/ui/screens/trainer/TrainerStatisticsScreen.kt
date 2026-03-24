package com.ileader.app.ui.screens.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.TrainerStatisticsViewModel
import com.ileader.app.ui.viewmodels.getTeamStats

@Composable
fun TrainerStatisticsScreen(user: User) {
    val viewModel: TrainerStatisticsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> {
            val data = s.data
            val teams = data.teams
            if (teams.isEmpty()) {
                EmptyState("Нет команд")
                return
            }
            var selectedTeamIndex by remember { mutableIntStateOf(0) }
            val selectedTeam = teams[selectedTeamIndex.coerceIn(0, teams.lastIndex)]
            val teamStats = getTeamStats(selectedTeam)
            val ratingProgress = data.ratingProgressByTeam[selectedTeam.id] ?: emptyList()
            val distribution = data.resultsDistributionByTeam[selectedTeam.id] ?: emptyList()
            val topAthletes = selectedTeam.members.sortedByDescending { it.rating }.take(3)

            var started by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { started = true }

            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    // ── HEADER ──
                    FadeIn(visible = started, delayMs = 0) {
                    Column {
                        Text("Статистика", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                        Spacer(Modifier.height(4.dp))
                        Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                    }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── TEAM SELECTOR ──
                    if (teams.size > 1) {
                        var expanded by remember { mutableStateOf(false) }
                        DarkCard {
                            Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SoftIconBox(Icons.Default.Groups, size = 36.dp, iconSize = 18.dp)
                                Spacer(Modifier.width(10.dp))
                                Text(selectedTeam.name, Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                                IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(20.dp), DarkTheme.TextMuted)
                                }
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                teams.forEachIndexed { index, team ->
                                    DropdownMenuItem(
                                        text = { Text("${team.name} (${team.sportName})", fontSize = 14.sp) },
                                        onClick = { selectedTeamIndex = index; expanded = false },
                                        leadingIcon = { if (index == selectedTeamIndex) Icon(Icons.Default.Check, null, tint = DarkTheme.Accent) }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    // ── SUMMARY STATS ──
                    FadeIn(visible = started, delayMs = 150) {
                    Column {
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                        StatItem(Modifier.weight(1f), Icons.Default.People, teamStats.athleteCount.toString(), "Спортсменов")
                        StatItem(Modifier.weight(1f), Icons.Default.Star, teamStats.totalWins.toString(), "Побед")
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                        StatItem(Modifier.weight(1f), Icons.Default.WorkspacePremium, teamStats.totalPodiums.toString(), "Подиумов")
                        StatItem(Modifier.weight(1f), Icons.Default.TrendingUp, teamStats.avgRating.toString(), "Ср. рейтинг")
                    }
                    }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── RATING PROGRESSION ──
                    FadeIn(visible = started, delayMs = 300) {
                    Column {
                    SectionHeader("Динамика рейтинга команды")
                    Spacer(Modifier.height(12.dp))
                    DarkCardPadded {
                        DarkBarChart(data = ratingProgress)
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── RESULTS DISTRIBUTION ──
                    SectionHeader("Распределение результатов")
                    Spacer(Modifier.height(12.dp))
                    DarkCard {
                        Column(Modifier.padding(16.dp)) {
                            val totalResults = distribution.sumOf { it.value }.coerceAtLeast(1)
                            distribution.forEach { item ->
                                val fraction = item.value.toFloat() / totalResults
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier.size(12.dp).clip(CircleShape).background(DarkTheme.Accent))
                                    Spacer(Modifier.width(10.dp))
                                    Text(item.label, Modifier.width(70.dp), fontSize = 13.sp, color = DarkTheme.TextPrimary, fontWeight = FontWeight.Medium)
                                    DarkProgressBar(fraction, Modifier.weight(1f))
                                    Spacer(Modifier.width(10.dp))
                                    Text("${item.value}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                                }
                            }
                        }
                    }
                    }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── TOP 3 ATHLETES ──
                    FadeIn(visible = started, delayMs = 450) {
                    Column {
                    SectionHeader("Лучшие спортсмены")
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        topAthletes.forEachIndexed { index, athlete ->
                            DarkCard {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(40.dp).clip(CircleShape)
                                            .background(if (index == 0) DarkTheme.AccentSoft else DarkTheme.CardBorder.copy(alpha = 0.5f)),
                                        Alignment.Center
                                    ) {
                                        Text("${index + 1}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (index == 0) DarkTheme.Accent else DarkTheme.TextMuted)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(athlete.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                                        Text("${athlete.wins} побед · ${athlete.podiums} подиумов", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                                    }
                                    StatusBadge("${athlete.rating}", DarkTheme.Accent)
                                }
                            }
                        }
                    }
                    }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── KEY METRICS ──
                    FadeIn(visible = started, delayMs = 600) {
                    Column {
                    SectionHeader("Ключевые метрики")
                    Spacer(Modifier.height(12.dp))
                    DarkCard {
                        Column(Modifier.padding(16.dp)) {
                            DarkMetricRow("Процент побед", "%.1f%%".format(teamStats.winRate))
                            Spacer(Modifier.height(12.dp))
                            val podiumRate = if (teamStats.totalTournaments > 0) teamStats.totalPodiums.toFloat() / teamStats.totalTournaments * 100 else 0f
                            DarkMetricRow("Процент подиумов", "%.1f%%".format(podiumRate))
                            Spacer(Modifier.height(12.dp))
                            DarkMetricRow("Средний рейтинг", teamStats.avgRating.toString())
                        }
                    }
                    }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun DarkMetricRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = DarkTheme.TextSecondary)
        StatusBadge(value, DarkTheme.Accent)
    }
}
