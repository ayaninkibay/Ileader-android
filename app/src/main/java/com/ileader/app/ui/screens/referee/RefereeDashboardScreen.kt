package com.ileader.app.ui.screens.referee

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.ileader.app.ui.viewmodels.RefereeDashboardViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark

@Composable
fun RefereeDashboardScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    val viewModel: RefereeDashboardViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> DashboardContent(user, s.data, onNavigate)
    }
}

@Composable
private fun DashboardContent(
    user: User,
    data: com.ileader.app.ui.viewmodels.RefereeDashboardData,
    onNavigate: (String) -> Unit
) {
    val stats = data.stats
    val pendingInvites = data.pendingInvites
    val activeTournaments = data.activeTournaments
    val upcomingTournaments = data.upcomingTournaments
    val calendarTournaments = data.calendarTournaments

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val accentColor = Accent
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

            // ── STATS ──
            FadeIn(visible, 200) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(Modifier.weight(1f), Icons.Default.Gavel, "${stats.totalTournaments}", "Турниров")
                    StatItem(Modifier.weight(1f), Icons.Default.SportsScore, "${stats.thisMonth}", "В этом месяце")
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(Modifier.weight(1f), Icons.Default.Shield, "${stats.totalViolations}", "Нарушений")
                    StatItem(Modifier.weight(1f), Icons.Default.Star, "${stats.pendingResults}", "Ожидают")
                }
            }

            // ── INCOMING INVITES ──
            if (pendingInvites.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                FadeIn(visible, 400) {
                    SectionHeader("Входящие приглашения", "Все") { onNavigate("referee/requests") }
                    Spacer(Modifier.height(12.dp))
                    pendingInvites.take(2).forEach { invite ->
                        InviteCard(invite)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // ── CALENDAR ──
            Spacer(Modifier.height(28.dp))
            FadeIn(visible, 600) {
                SectionHeader("Календарь назначений")
                Spacer(Modifier.height(12.dp))
                val upcoming = calendarTournaments.take(3)
                if (upcoming.isEmpty()) {
                    EmptyState("Нет предстоящих назначений")
                } else {
                    upcoming.forEach { t ->
                        CalendarItem(t)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // ── ACTIVE TOURNAMENTS ──
            if (activeTournaments.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                FadeIn(visible, 800) {
                    SectionHeader("Активные турниры", "Все") { onNavigate("referee/tournaments") }
                    Spacer(Modifier.height(12.dp))
                    activeTournaments.take(2).forEach { t ->
                        TournamentCompactCard(t) { onNavigate("referee/tournaments") }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // ── UPCOMING TOURNAMENTS ──
            if (upcomingTournaments.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                FadeIn(visible, 1000) {
                    SectionHeader("Предстоящие турниры")
                    Spacer(Modifier.height(12.dp))
                    upcomingTournaments.take(2).forEach { t ->
                        TournamentCompactCard(t) { onNavigate("referee/tournaments") }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InviteCard(invite: RefereeInvite) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = DarkTheme.cardBorderStroke
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(Icons.Default.Mail)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(invite.tournamentName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(3.dp))
                Text("${invite.sportName} · ${invite.role.label}", fontSize = 12.sp, color = TextSecondary)
            }
            StatusBadge("Новое")
        }
    }
}

@Composable
private fun CalendarItem(tournament: RefereeTournament) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = CardBg) {
        Row(
            Modifier.border(0.5.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SoftIconBox(Icons.Default.DateRange)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tournament.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text("${tournament.date} · ${tournament.location}", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun TournamentCompactCard(tournament: RefereeTournament, onClick: () -> Unit) {
    val isActive = tournament.status == TournamentStatus.IN_PROGRESS
    val chipColor = if (isActive) Accent else TextMuted

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = DarkTheme.cardBorderStroke,
        onClick = onClick
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(Icons.Default.EmojiEvents)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tournament.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text("${tournament.sport} · ${tournament.date}", fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            StatusBadge(tournament.status.displayName, chipColor)
        }
    }
}
