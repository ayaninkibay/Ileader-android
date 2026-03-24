package com.ileader.app.ui.screens.trainer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.TournamentStatus
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.TrainerDashboardViewModel
import com.ileader.app.ui.viewmodels.getTeamStats

@Composable
fun TrainerDashboardScreen(
    user: User,
    onNavigateToTeam: () -> Unit = {},
    onNavigateToTournaments: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {}
) {
    val viewModel: TrainerDashboardViewModel = viewModel()
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
            val upcomingTournaments = data.tournaments.filter {
                it.status == TournamentStatus.REGISTRATION_OPEN || it.status == TournamentStatus.IN_PROGRESS
            }.filter { it.sportId == selectedTeam.sportId }.take(3)

            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            val accentColor = DarkTheme.Accent
            Box(Modifier.fillMaxSize()) {
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

                    // ── HEADER ──
                    FadeIn(visible, 0) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            ILeaderBrandHeader(role = user.role)
                            UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── TEAM SELECTOR ──
                    if (teams.size > 1) {
                        FadeIn(visible, 100) {
                            var expanded by remember { mutableStateOf(false) }
                            DarkCard {
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
                                            text = {
                                                Column {
                                                    Text(team.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                    Text("${team.sportName} · ${team.ageCategory}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                                                }
                                            },
                                            onClick = { selectedTeamIndex = index; expanded = false },
                                            leadingIcon = { if (index == selectedTeamIndex) Icon(Icons.Default.Check, null, tint = DarkTheme.Accent) }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // ── STATS 2x2 ──
                    FadeIn(visible, 200) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatItem(Modifier.weight(1f), Icons.Default.People, teamStats.athleteCount.toString(), "Спортсменов")
                            StatItem(Modifier.weight(1f), Icons.Default.EmojiEvents, teamStats.totalTournaments.toString(), "Турниров")
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatItem(Modifier.weight(1f), Icons.Default.Star, teamStats.totalWins.toString(), "Побед")
                            StatItem(Modifier.weight(1f), Icons.Default.TrendingUp, teamStats.avgRating.toString(), "Ср. рейтинг")
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── TEAM MEMBERS ──
                    FadeIn(visible, 400) {
                        SectionHeader("Состав команды", "Все", onNavigateToTeam)
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            selectedTeam.members.take(3).forEach { athlete ->
                                DarkCard {
                                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            Modifier.size(40.dp).clip(CircleShape).background(DarkTheme.AccentSoft),
                                            Alignment.Center
                                        ) {
                                            Text(athlete.name.first().toString(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.Accent)
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(athlete.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${athlete.tournaments} турн. · ${athlete.wins} побед", fontSize = 12.sp, color = DarkTheme.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        StatusBadge("${athlete.rating}", DarkTheme.Accent)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── UPCOMING TOURNAMENTS ──
                    FadeIn(visible, 600) {
                        SectionHeader("Ближайшие турниры", "Все", onNavigateToTournaments)
                        Spacer(Modifier.height(12.dp))
                        if (upcomingTournaments.isEmpty()) {
                            EmptyState("Нет предстоящих турниров")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                upcomingTournaments.forEach { tournament ->
                                    DarkCard {
                                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                            AccentIconBox(Icons.Default.EmojiEvents)
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(tournament.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Spacer(Modifier.height(3.dp))
                                                Text("${formatShortDate(tournament.startDate)} · ${tournament.sportName}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                                            }
                                            val isActive = tournament.status == TournamentStatus.REGISTRATION_OPEN || tournament.status == TournamentStatus.IN_PROGRESS
                                            StatusBadge(tournament.status.displayName, if (isActive) DarkTheme.Accent else DarkTheme.TextMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── QUICK ACTIONS ──
                    FadeIn(visible, 800) {
                        SectionHeader("Быстрые действия")
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            QuickAction(Modifier.weight(1f), Icons.Default.PersonAdd, "Пригласить", onNavigateToTeam)
                            QuickAction(Modifier.weight(1f), Icons.Default.AppRegistration, "Регистрация", onNavigateToTournaments)
                            QuickAction(Modifier.weight(1f), Icons.Default.BarChart, "Статистика", onNavigateToStatistics)
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickAction(modifier: Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = DarkTheme.CardBg
    ) {
        Column(
            Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp)).padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AccentIconBox(icon)
            Spacer(Modifier.height(10.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
        }
    }
}
