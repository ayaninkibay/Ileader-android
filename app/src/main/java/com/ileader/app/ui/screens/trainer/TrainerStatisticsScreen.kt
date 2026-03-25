package com.ileader.app.ui.screens.trainer

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
                ) {
                    Spacer(Modifier.height(16.dp))

                    // ── HEADER ──
                    FadeIn(visible = started, delayMs = 0) {
                        Column(Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                user.displayName,
                                fontSize = 14.sp,
                                color = DarkTheme.TextMuted,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Статистика",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DarkTheme.TextPrimary,
                                letterSpacing = (-0.8).sp
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── TEAM SELECTOR ──
                    if (teams.size > 1) {
                        FadeIn(visible = started, delayMs = 100) {
                            var expanded by remember { mutableStateOf(false) }
                            DarkCard(Modifier.padding(horizontal = 20.dp)) {
                                Row(
                                    Modifier.clickable { expanded = true }.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AccentIconBox(Icons.Default.Groups)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(selectedTeam.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                                        Text("${selectedTeam.sportName} · ${selectedTeam.ageCategory}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                                    }
                                    Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(20.dp), DarkTheme.TextMuted)
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
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    // ── SUMMARY STATS ──
                    FadeIn(visible = started, delayMs = 150) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashStatItem(Modifier.weight(1f), Icons.Default.People, teamStats.athleteCount.toString(), "Спортсмены")
                            DashStatItem(Modifier.weight(1f), Icons.Default.Star, teamStats.totalWins.toString(), "Победы")
                            DashStatItem(Modifier.weight(1f), Icons.Default.TrendingUp, teamStats.avgRating.toString(), "Рейтинг")
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── RATING PROGRESSION ──
                    FadeIn(visible = started, delayMs = 300) {
                        Column(Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                "Динамика рейтинга команды",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTheme.TextPrimary
                            )
                            Spacer(Modifier.height(12.dp))
                            DarkCardPadded {
                                DarkBarChart(data = ratingProgress)
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── RESULTS DISTRIBUTION ──
                    FadeIn(visible = started, delayMs = 450) {
                        Column(Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                "Распределение результатов",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTheme.TextPrimary
                            )
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = DarkTheme.CardBg
                            ) {
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
                    FadeIn(visible = started, delayMs = 550) {
                        Column(Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                "Лучшие спортсмены",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTheme.TextPrimary
                            )
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = DarkTheme.CardBg
                            ) {
                                Column(Modifier.padding(vertical = 8.dp)) {
                                    topAthletes.forEachIndexed { index, athlete ->
                                        val medalEmoji = when (index) {
                                            0 -> "🥇"
                                            1 -> "🥈"
                                            2 -> "🥉"
                                            else -> null
                                        }
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .background(if (index == 0) DarkTheme.AccentSoft else androidx.compose.ui.graphics.Color.Transparent)
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (medalEmoji != null) {
                                                Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                                                    Text(medalEmoji, fontSize = 22.sp)
                                                }
                                            } else {
                                                Box(
                                                    Modifier.size(32.dp).clip(CircleShape).background(DarkTheme.CardBorder.copy(alpha = 0.4f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("${index + 1}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = DarkTheme.TextMuted)
                                                }
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(athlete.name, fontSize = 14.sp, fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium, color = if (index == 0) DarkTheme.Accent else DarkTheme.TextPrimary)
                                                Text("${athlete.wins} побед · ${athlete.podiums} подиумов", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                                            }
                                            Text(
                                                "${athlete.rating}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (index == 0) DarkTheme.Accent else DarkTheme.TextSecondary
                                            )
                                        }
                                        if (index < topAthletes.lastIndex) {
                                            Box(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 20.dp)
                                                    .height(0.5.dp)
                                                    .clip(RoundedCornerShape(1.dp))
                                                    .background(DarkTheme.CardBorder.copy(alpha = 0.5f))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── KEY METRICS ──
                    FadeIn(visible = started, delayMs = 650) {
                        Column(Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                "Ключевые метрики",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTheme.TextPrimary
                            )
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = DarkTheme.CardBg
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    MetricRow("Процент побед", "%.1f%%".format(teamStats.winRate))
                                    Spacer(Modifier.height(12.dp))
                                    val podiumRate = if (teamStats.totalTournaments > 0) teamStats.totalPodiums.toFloat() / teamStats.totalTournaments * 100 else 0f
                                    MetricRow("Процент подиумов", "%.1f%%".format(podiumRate))
                                    Spacer(Modifier.height(12.dp))
                                    MetricRow("Средний рейтинг", teamStats.avgRating.toString())
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
private fun DashStatItem(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = DarkTheme.CardBg
    ) {
        Column(
            Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkTheme.AccentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(18.dp), tint = DarkTheme.Accent)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkTheme.TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize = 12.sp,
                color = DarkTheme.TextSecondary,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = DarkTheme.TextSecondary)
        Surface(shape = RoundedCornerShape(10.dp), color = DarkTheme.AccentSoft) {
            Text(
                value,
                Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkTheme.Accent
            )
        }
    }
}
