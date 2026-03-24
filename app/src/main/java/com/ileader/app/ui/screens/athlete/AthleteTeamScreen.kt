package com.ileader.app.ui.screens.athlete

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.Team
import com.ileader.app.data.models.TeamMember
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AthleteTeamViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AthleteTeamScreen(
    user: User,
    onBack: () -> Unit
) {
    val viewModel: AthleteTeamViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.teamId) {
        user.teamId?.let { viewModel.load(it) }
    }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { user.teamId?.let { viewModel.load(it) } }
        is UiState.Success -> TeamContent(user, s.data, onBack)
    }
}

@Composable
private fun TeamContent(user: User, team: Team, onBack: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── BACK + TITLE ──
            FadeIn(visible = started, delayMs = 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onBack, shape = CircleShape, color = DarkTheme.CardBg,
                    modifier = Modifier.size(40.dp).border(0.5.dp, DarkTheme.CardBorder, CircleShape)
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", Modifier.size(20.dp), DarkTheme.TextPrimary)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Команда", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(2.dp))
                    Text(team.name, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                }
            }
            }

            Spacer(Modifier.height(20.dp))

            // ── TEAM HEADER CARD ──
            FadeIn(visible = started, delayMs = 150) {
            DarkCard {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(64.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(DarkTheme.Accent, DarkTheme.AccentDark))),
                        Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, null, Modifier.size(32.dp), Color.White)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(team.name, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
                    Text(team.sportName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                }
            }
            }

            Spacer(Modifier.height(12.dp))

            // ── STATS ──
            FadeIn(visible = started, delayMs = 300) {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                val totalTournaments = team.members.sumOf { it.tournaments }
                val totalWins = team.members.sumOf { it.wins }

                TeamStatItem(Modifier.weight(1f), Icons.Default.People, team.members.size.toString(), "Участников")
                TeamStatItem(Modifier.weight(1f), Icons.Default.EmojiEvents, totalTournaments.toString(), "Турниров")
                TeamStatItem(Modifier.weight(1f), Icons.Default.Star, totalWins.toString(), "Побед")
            }
            }

            Spacer(Modifier.height(16.dp))

            // ── TEAM INFO ──
            FadeIn(visible = started, delayMs = 450) {
            DarkCardPadded {
                Text("О команде", fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
                Spacer(Modifier.height(10.dp))
                Text(team.description, fontSize = 14.sp, color = DarkTheme.TextSecondary, lineHeight = 20.sp)

                Spacer(Modifier.height(14.dp))

                TeamInfoRow(Icons.Default.Person, "Тренер", team.trainerName)
                TeamInfoRow(Icons.Default.SportsSoccer, "Вид спорта", team.sportName)
                TeamInfoRow(Icons.Default.CalendarMonth, "Основана", team.foundedDate)
                if (team.sponsorName != null) {
                    TeamInfoRow(Icons.Default.Handshake, "Спонсор", team.sponsorName)
                }
            }
            }

            Spacer(Modifier.height(16.dp))

            // ── MEMBERS ──
            FadeIn(visible = started, delayMs = 600) {
            Column {
            Text("Участники команды", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
            Spacer(Modifier.height(10.dp))

            team.members.forEach { member ->
                MemberCard(member = member, isCurrentUser = member.id == user.id)
                Spacer(Modifier.height(8.dp))
            }
            }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Screen-specific composables ──

@Composable
private fun TeamStatItem(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    Surface(modifier, RoundedCornerShape(14.dp), DarkTheme.CardBg) {
        Column(
            Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp)).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(DarkTheme.AccentSoft),
                Alignment.Center
            ) { Icon(icon, null, Modifier.size(18.dp), DarkTheme.Accent) }
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
            Text(label, fontSize = 11.sp, color = DarkTheme.TextMuted)
        }
    }
}

@Composable
private fun TeamInfoRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(DarkTheme.CardBorder.copy(alpha = 0.5f)), Alignment.Center) {
            Icon(icon, null, Modifier.size(16.dp), DarkTheme.TextMuted)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 12.sp, color = DarkTheme.TextMuted)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkTheme.TextPrimary)
        }
    }
}

@Composable
private fun MemberCard(member: TeamMember, isCurrentUser: Boolean) {
    Surface(
        Modifier.fillMaxWidth(), RoundedCornerShape(16.dp),
        if (isCurrentUser) DarkTheme.Accent.copy(alpha = 0.06f) else DarkTheme.CardBg,
        border = ButtonDefaults.outlinedButtonBorder(true).copy(
            brush = Brush.linearGradient(
                if (isCurrentUser) listOf(DarkTheme.Accent.copy(alpha = 0.3f), DarkTheme.Accent.copy(alpha = 0.1f))
                else listOf(DarkTheme.CardBorder.copy(alpha = 0.6f), DarkTheme.CardBorder.copy(alpha = 0.2f))
            )
        )
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(
                    if (isCurrentUser) DarkTheme.AccentSoft else DarkTheme.CardBorder.copy(alpha = 0.5f)
                ),
                Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, Modifier.size(24.dp),
                    if (isCurrentUser) DarkTheme.Accent else DarkTheme.TextMuted)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(member.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = if (isCurrentUser) DarkTheme.Accent else DarkTheme.TextPrimary)
                    if (isCurrentUser) {
                        Spacer(Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = DarkTheme.AccentSoft) {
                            Text("Вы", Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp, color = DarkTheme.Accent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(member.role, fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${member.wins} побед", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkTheme.TextPrimary)
                Text("${member.tournaments} турн.", fontSize = 11.sp, color = DarkTheme.TextMuted)
            }
        }
    }
}
