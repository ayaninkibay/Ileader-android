package com.ileader.app.ui.screens.sponsor

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SponsorshipDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.SponsorDashboardViewModel

@Composable
fun SponsorDashboardScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    val viewModel: SponsorDashboardViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val appliedTournaments by viewModel.appliedTournaments.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> {
            DashboardContent(user, s.data.sponsorships, s.data.openTournaments,
                s.data.totalInvested, s.data.teamCount, s.data.tournamentCount,
                appliedTournaments, onNavigate,
                onApplySponsorship = { tournamentId -> viewModel.requestTournamentSponsorship(user.id, tournamentId) })
        }
    }
}

@Composable
private fun DashboardContent(
    user: User,
    sponsorships: List<SponsorshipDto>,
    openTournaments: List<TournamentWithCountsDto>,
    totalInvested: Double,
    teamCount: Int,
    tournamentCount: Int,
    appliedTournaments: Set<String>,
    onNavigate: (String) -> Unit,
    onApplySponsorship: (String) -> Unit
) {
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

            // ── STATS 2x2 ──
            FadeIn(visible, 200) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(Modifier.weight(1f), Icons.Default.AttachMoney,
                        SponsorUtils.formatAmount(totalInvested.toLong()), "Инвестировано")
                    StatItem(Modifier.weight(1f), Icons.Default.TrendingUp,
                        "${sponsorships.size}", "Спонсорств")
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigate("sponsor/teams") },
                        Icons.Default.Groups, "$teamCount", "Команды")
                    StatItem(Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigate("sponsor/tournaments") },
                        Icons.Default.EmojiEvents, "$tournamentCount", "Турниры")
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── OPEN FOR SPONSORSHIP ──
            FadeIn(visible, 400) {
                SectionHeader("Открыты для спонсорства")
                Spacer(Modifier.height(12.dp))
                openTournaments.forEach { tournament ->
                    OpenTournamentCard(tournament,
                        applied = tournament.id in appliedTournaments,
                        onApply = { onApplySponsorship(tournament.id) })
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── SPONSORED TEAMS ──
            FadeIn(visible, 600) {
                SectionHeader("Спонсируемые команды", "Все") { onNavigate("sponsor/teams") }
                Spacer(Modifier.height(12.dp))
                val teams = sponsorships.filter { it.teamId != null }
                if (teams.isEmpty()) {
                    EmptyState("Нет спонсируемых команд", "Перейдите в раздел \"Команды\"")
                } else {
                    teams.forEach { item ->
                        SponsoredItemCard(item, isTeam = true)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── SPONSORED TOURNAMENTS ──
            FadeIn(visible, 800) {
                SectionHeader("Спонсируемые турниры", "Все") { onNavigate("sponsor/tournaments") }
                Spacer(Modifier.height(12.dp))
                val tournaments = sponsorships.filter { it.tournamentId != null }
                if (tournaments.isEmpty()) {
                    EmptyState("Нет спонсируемых турниров", "Перейдите в раздел \"Турниры\"")
                } else {
                    tournaments.forEach { item ->
                        SponsoredItemCard(item, isTeam = false)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OpenTournamentCard(tournament: TournamentWithCountsDto, applied: Boolean, onApply: () -> Unit) {
    val chipColor = SponsorUtils.getStatusColor(tournament.status ?: "")

    DarkCard {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(tournament.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = DarkTheme.TextPrimary, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                StatusBadge(SponsorUtils.getStatusLabel(tournament.status ?: ""), chipColor)
            }
            Spacer(Modifier.height(3.dp))
            Text(tournament.sportName ?: "", fontSize = 12.sp, color = DarkTheme.TextSecondary)

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${formatShortDate(tournament.startDate)} — ${formatShortDate(tournament.endDate)}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(tournament.locationName ?: "", fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${tournament.participantCount}/${tournament.maxParticipants ?: "?"} участников", fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }

            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onApply,
                enabled = !applied,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkTheme.Accent,
                    disabledContainerColor = DarkTheme.CardBorder
                )
            ) {
                Icon(
                    if (applied) Icons.Default.Check else Icons.Default.Handshake,
                    null, modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (applied) "Заявка отправлена" else "Подать заявку",
                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SponsoredItemCard(item: SponsorshipDto, isTeam: Boolean) {
    val name = if (isTeam) item.teams?.name else item.tournaments?.name
    val sport = item.tournaments?.sports?.name ?: ""

    DarkCard {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccentIconBox(
                if (isTeam) Icons.Default.Groups else Icons.Default.EmojiEvents
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                Spacer(Modifier.height(3.dp))
                Text(sport, fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            Text(
                SponsorUtils.formatAmount((item.amount ?: 0.0).toLong()),
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTheme.Accent
            )
        }
    }
}
