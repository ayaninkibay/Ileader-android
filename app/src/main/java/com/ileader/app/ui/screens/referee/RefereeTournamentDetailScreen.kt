package com.ileader.app.ui.screens.referee

import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.ileader.app.data.bracket.BracketUtils
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.components.bracket.BracketView
import com.ileader.app.ui.components.bracket.MatchDetailDialog
import com.ileader.app.ui.viewmodels.RefereeTournamentDetailViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun RefereeTournamentDetailScreen(
    user: User,
    tournamentId: String,
    onBack: () -> Unit = {},
    onNavigateToResults: () -> Unit = {}
) {
    val viewModel: RefereeTournamentDetailViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(tournamentId, user.id) { viewModel.load(tournamentId, user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(tournamentId, user.id) }
        is UiState.Success -> DetailContent(s.data, onBack, onNavigateToResults)
    }
}

@Composable
private fun DetailContent(
    data: com.ileader.app.ui.viewmodels.RefereeTournamentDetailData,
    onBack: () -> Unit,
    onNavigateToResults: () -> Unit
) {
    val tournament = data.tournament
    val matches = data.matches
    val participants = data.participants
    val violations = data.violations

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(Bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Back
            Row(Modifier.clickable { onBack() }, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(20.dp), TextSecondary)
                Spacer(Modifier.width(8.dp))
                Text("Назад", fontSize = 13.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 0) {
                Text(tournament.name, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(4.dp))
                Text("${tournament.sport} · ${tournament.refereeRole.label}", fontSize = 14.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(28.dp))

            // ── PROGRESS ──
            FadeIn(visible, 200) {
                val progress = if (tournament.matchesTotal > 0) tournament.matchesCompleted.toFloat() / tournament.matchesTotal else 0f
                DarkCardPadded(padding = 14.dp) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Прогресс", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("${tournament.matchesCompleted}/${tournament.matchesTotal}", fontSize = 12.sp, color = TextSecondary)
                    }
                    Spacer(Modifier.height(10.dp))
                    DarkProgressBar(progress)
                    Spacer(Modifier.height(8.dp))
                    Text("${(progress * 100).toInt()}% выполнено", fontSize = 12.sp, color = TextMuted)
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── INFO ──
            FadeIn(visible, 300) {
                DarkCardPadded(padding = 14.dp) {
                    Text("Информация", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = TextPrimary, letterSpacing = (-0.3).sp)
                    Spacer(Modifier.height(12.dp))
                    InfoRow(Icons.Default.DateRange, "Дата", tournament.date)
                    Spacer(Modifier.height(10.dp))
                    InfoRow(Icons.Default.LocationOn, "Локация", tournament.location)
                    Spacer(Modifier.height(10.dp))
                    InfoRow(Icons.Default.People, "Участников", "${tournament.participants}")
                    Spacer(Modifier.height(10.dp))
                    InfoRow(Icons.Default.Gavel, "Роль", tournament.refereeRole.label)

                    if (tournament.status == TournamentStatus.IN_PROGRESS) {
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = onNavigateToResults,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Начать судейство", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── BRACKET / MATCHES ──
            FadeIn(visible, 400) {
                if (data.bracket.isNotEmpty()) {
                    RefereeBracketSection(data)
                } else {
                    DarkCardPadded(padding = 14.dp) {
                        Text("Матчи (${matches.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = TextPrimary, letterSpacing = (-0.3).sp)
                        Spacer(Modifier.height(12.dp))
                        matches.forEach { match ->
                            val isActive = match.status == RefereeMatchStatus.IN_PROGRESS
                            val chipColor = if (isActive) Accent else TextMuted

                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                color = CardBorder.copy(alpha = 0.3f)) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    SoftIconBox(Icons.Default.SportsScore, size = 36.dp, iconSize = 18.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Матч #${match.number}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        Text("${match.category} · ${match.time}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    StatusBadge(match.status.label, chipColor)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── PARTICIPANTS ──
            FadeIn(visible, 500) {
                DarkCardPadded(padding = 14.dp) {
                    Text("Участники (${participants.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = TextPrimary, letterSpacing = (-0.3).sp)
                    Spacer(Modifier.height(12.dp))
                    participants.forEach { p ->
                        val pViolations = violations.filter { it.participantId == p.id }
                        var expanded by remember { mutableStateOf(false) }

                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            color = CardBorder.copy(alpha = 0.3f)) {
                            Column(Modifier.padding(12.dp)) {
                                Row(
                                    Modifier.fillMaxWidth().clickable { if (pViolations.isNotEmpty()) expanded = !expanded },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AccentSoft),
                                        contentAlignment = Alignment.Center) {
                                        Text("#${p.number}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Accent)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(p.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        Text(p.team, fontSize = 11.sp, color = TextSecondary)
                                    }
                                    if (pViolations.isNotEmpty()) {
                                        StatusBadge("${pViolations.size} нар.", Accent)
                                    }
                                }
                                AnimatedVisibility(visible = expanded) {
                                    Column(Modifier.padding(top = 10.dp)) {
                                        pViolations.forEach { v ->
                                            ViolationItem(v)
                                            Spacer(Modifier.height(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }

            // ── VIOLATIONS ──
            if (violations.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                FadeIn(visible, 600) {
                    DarkCardPadded(padding = 14.dp) {
                        Text("Нарушения (${violations.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = TextPrimary, letterSpacing = (-0.3).sp)
                        Spacer(Modifier.height(12.dp))
                        val warnings = violations.count { it.severity == ViolationSeverity.WARNING }
                        val penalties = violations.count { it.severity == ViolationSeverity.PENALTY }
                        val disqualifications = violations.count { it.severity == ViolationSeverity.DISQUALIFICATION }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ViolationStat("Предупр.", warnings, Modifier.weight(1f))
                            ViolationStat("Штрафы", penalties, Modifier.weight(1f))
                            ViolationStat("Дискв.", disqualifications, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        violations.forEach { v ->
                            ViolationItem(v)
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SoftIconBox(icon, size = 32.dp, iconSize = 16.dp)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextMuted)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

@Composable
private fun ViolationItem(violation: RefereeViolation) {
    Surface(shape = RoundedCornerShape(14.dp), color = Accent.copy(alpha = 0.08f)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            SoftIconBox(Icons.Default.Shield, size = 32.dp, iconSize = 16.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(violation.description, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text("${violation.participantName} · ${violation.severity.label}", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun ViolationStat(label: String, count: Int, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = Accent.copy(alpha = 0.08f)) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$count", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Accent, letterSpacing = (-0.3).sp)
            Text(label, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun RefereeBracketSection(data: com.ileader.app.ui.viewmodels.RefereeTournamentDetailData) {
    val matches = BracketUtils.mapDtosToMatches(data.bracket, data.bracketParticipants)
    val groups = BracketUtils.mapGroupDtos(data.groups)
    var selectedMatch by remember { mutableStateOf<BracketMatch?>(null) }

    DarkCardPadded(padding = 14.dp) {
        Text("Турнирная сетка", fontSize = 16.sp, fontWeight = FontWeight.Bold,
            color = TextPrimary, letterSpacing = (-0.3).sp)
        Spacer(Modifier.height(12.dp))

        BracketView(
            format = data.format,
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
