package com.ileader.app.ui.screens.athlete

import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AthleteDashboardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AthleteDashboardScreen(
    user: User,
    onNavigateToTournaments: () -> Unit = {},
    onNavigateToResults: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {}
) {
    val viewModel: AthleteDashboardViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> DashboardContent(user, s.data.stats, s.data.upcoming, s.data.recentResults, s.data.leaderboard, onNavigateToTournaments, onNavigateToResults, onNavigateToGoals)
    }
}

@Composable
private fun DashboardContent(
    user: User,
    stats: AthleteStats,
    upcoming: List<Tournament>,
    recentResults: List<TournamentResult>,
    leaderboard: List<Triple<String, Int, Int>>,
    onNavigateToTournaments: () -> Unit,
    onNavigateToResults: () -> Unit,
    onNavigateToGoals: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val ratingTarget = stats.rating / 2000f
    val ratingProgress by animateFloatAsState(
        if (visible) ratingTarget else 0f,
        tween(1400, delayMillis = 400, easing = EaseOutCubic), label = "rp"
    )

    val accentColor = DarkTheme.Accent
    val cardBorderColor = DarkTheme.CardBorder
    val accentDarkColor = DarkTheme.AccentDark
    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
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
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ILeaderBrandHeader(role = user.role)
                    UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── MAIN PROGRESS CARD ──
            FadeIn(visible, 200) {
                DarkCard {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                            Canvas(Modifier.fillMaxSize()) {
                                val pad = 12.dp.toPx()
                                drawArc(
                                    color = cardBorderColor, startAngle = 0f, sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
                                    topLeft = Offset(pad, pad),
                                    size = Size(size.width - pad * 2, size.height - pad * 2)
                                )
                            }
                            Canvas(Modifier.fillMaxSize()) {
                                val pad = 12.dp.toPx()
                                drawArc(
                                    brush = Brush.sweepGradient(listOf(accentColor, accentDarkColor, accentColor)),
                                    startAngle = -90f, sweepAngle = 360f * ratingProgress,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
                                    topLeft = Offset(pad, pad),
                                    size = Size(size.width - pad * 2, size.height - pad * 2)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${stats.rating}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DarkTheme.TextPrimary)
                                Text("рейтинг", fontSize = 11.sp, color = DarkTheme.TextMuted)
                            }
                        }

                        Spacer(Modifier.width(20.dp))

                        Column(Modifier.weight(1f)) {
                            Text("Общий прогресс", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text("${stats.totalTournaments} турниров сыграно", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                            Spacer(Modifier.height(16.dp))
                            Text("Точность ${stats.accuracy}%", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DarkTheme.TextSecondary)
                            Spacer(Modifier.height(6.dp))
                            DarkProgressBar(stats.accuracy / 100f)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── STAT CARDS 2x2 ──
            FadeIn(visible, 350) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(Modifier.weight(1f), Icons.Default.EmojiEvents, stats.totalTournaments.toString(), "Турниров")
                    StatItem(Modifier.weight(1f), Icons.Default.Star, stats.wins.toString(), "Побед")
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(Modifier.weight(1f), Icons.Default.WorkspacePremium, stats.podiums.toString(), "Подиумов")
                    StatItem(Modifier.weight(1f), Icons.Default.FitnessCenter, "${stats.points}", "Очков")
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── UPCOMING TOURNAMENTS ──
            FadeIn(visible, 500) {
                SectionHeader("Ближайшие турниры", "Все", onNavigateToTournaments)
                Spacer(Modifier.height(12.dp))
                if (upcoming.isEmpty()) {
                    EmptyState("Нет предстоящих турниров")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        upcoming.forEach { t -> TournamentItem(t) }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── LEADERBOARD ──
            FadeIn(visible, 600) {
                SectionHeader("Таблица лидеров")
                Spacer(Modifier.height(12.dp))
                DarkCard {
                    Column(Modifier.padding(vertical = 8.dp)) {
                        leaderboard.forEachIndexed { i, (name, pts, rank) ->
                            LeaderItem(rank, name, pts, isCurrent = rank == 5)
                            if (i < leaderboard.lastIndex) {
                                HorizontalDivider(
                                    Modifier.padding(horizontal = 14.dp),
                                    thickness = 0.5.dp,
                                    color = DarkTheme.CardBorder.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── RECENT RESULTS ──
            FadeIn(visible, 700) {
                SectionHeader("Последние результаты", "Все", onNavigateToResults)
                Spacer(Modifier.height(12.dp))
                if (recentResults.isEmpty()) {
                    EmptyState("Нет результатов")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentResults.forEach { r -> ResultItem(r) }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── QUICK ACTIONS ──
            FadeIn(visible, 800) {
                SectionHeader("Быстрые действия")
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAction(Modifier.weight(1f), Icons.Default.Search, "Турниры", onNavigateToTournaments)
                    QuickAction(Modifier.weight(1f), Icons.Default.Leaderboard, "Результаты", onNavigateToResults)
                    QuickAction(Modifier.weight(1f), Icons.Default.Flag, "Цели", onNavigateToGoals)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ══════════════════════════════════════════════════════
// SCREEN-SPECIFIC COMPONENTS
// ══════════════════════════════════════════════════════

@Composable
private fun TournamentItem(tournament: Tournament) {
    DarkCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(Icons.Default.EmojiEvents)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    tournament.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text("${tournament.startDate} · ${tournament.sportName}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            TournamentStatusBadge(tournament.status)
        }
    }
}

@Composable
private fun LeaderItem(rank: Int, name: String, points: Int, isCurrent: Boolean) {
    val rowBg = if (isCurrent) DarkTheme.AccentSoft else Color.Transparent
    Row(
        Modifier.fillMaxWidth().background(rowBg).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape)
                .background(if (rank <= 3) DarkTheme.Accent.copy(alpha = 0.15f) else DarkTheme.CardBorder.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text("$rank", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = if (rank <= 3) DarkTheme.Accent else DarkTheme.TextMuted)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            name, Modifier.weight(1f), fontSize = 14.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
            color = if (isCurrent) DarkTheme.Accent else DarkTheme.TextPrimary
        )
        Text("$points", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextSecondary)
    }
}

@Composable
private fun ResultItem(result: TournamentResult) {
    val isTop = result.position <= 3
    DarkCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).clip(CircleShape)
                    .background(if (isTop) DarkTheme.AccentSoft else DarkTheme.CardBorder.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text("#${result.position}", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = if (isTop) DarkTheme.Accent else DarkTheme.TextMuted)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    result.tournamentName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text("${result.date} · ${result.sportName}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = DarkTheme.AccentSoft) {
                Text("+${result.points}", Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkTheme.Accent)
            }
        }
    }
}

@Composable
private fun QuickAction(modifier: Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).clickable { onClick() },
        shape = RoundedCornerShape(14.dp), color = DarkTheme.CardBg
    ) {
        Column(
            Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AccentIconBox(icon)
            Spacer(Modifier.height(10.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                color = DarkTheme.TextPrimary, textAlign = TextAlign.Center)
        }
    }
}

/** Public status badge reused by other athlete screens. */
@Composable
fun TournamentStatusBadge(status: TournamentStatus) {
    val isActive = status == TournamentStatus.REGISTRATION_OPEN || status == TournamentStatus.IN_PROGRESS
    StatusBadge(status.displayName, if (isActive) DarkTheme.Accent else DarkTheme.TextMuted)
}
