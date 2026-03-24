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
import androidx.compose.ui.text.style.TextOverflow
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

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // ── HERO BANNER ──
            FadeIn(visible = started, delayMs = 0) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {

                // Логотип + название по центру
                Column(
                    Modifier.fillMaxSize().padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkTheme.AccentSoft)
                            .border(1.dp, DarkTheme.CardBorder, RoundedCornerShape(20.dp)),
                        Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, null, Modifier.size(36.dp), DarkTheme.Accent)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        team.name,
                        fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = DarkTheme.TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(sportIcon(team.sportName), null, Modifier.size(13.dp), DarkTheme.TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(team.sportName, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                    }
                }
            }
            }

            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(16.dp))

                // ── STATS ──
                FadeIn(visible = started, delayMs = 200) {
                val totalTournaments = team.members.sumOf { it.tournaments }
                val totalWins = team.members.sumOf { it.wins }

                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                    TeamStatItem(Modifier.weight(1f), Icons.Default.People, team.members.size.toString(), "Участников")
                    TeamStatItem(Modifier.weight(1f), Icons.Default.EmojiEvents, totalTournaments.toString(), "Турниров")
                    TeamStatItem(Modifier.weight(1f), Icons.Default.Star, totalWins.toString(), "Побед")
                }
                }

                Spacer(Modifier.height(16.dp))

                // ── TEAM INFO ──
                FadeIn(visible = started, delayMs = 350) {
                Surface(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), DarkTheme.CardBg) {
                    Column(
                        Modifier
                            .border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            "О команде", fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                            color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(team.description, fontSize = 14.sp, color = DarkTheme.TextSecondary, lineHeight = 20.sp)

                        Spacer(Modifier.height(16.dp))

                        // Разделитель
                        Box(Modifier.fillMaxWidth().height(0.5.dp).background(DarkTheme.CardBorder.copy(alpha = 0.5f)))

                        Spacer(Modifier.height(14.dp))

                        TeamInfoRow(Icons.Default.Person, "Тренер", team.trainerName)
                        TeamInfoRow(Icons.Default.SportsSoccer, "Вид спорта", team.sportName)
                        TeamInfoRow(Icons.Default.CalendarMonth, "Основана", team.foundedDate)
                        if (team.sponsorName != null) {
                            TeamInfoRow(Icons.Default.Handshake, "Спонсор", team.sponsorName)
                        }
                    }
                }
                }

                Spacer(Modifier.height(20.dp))

                // ── MEMBERS ──
                FadeIn(visible = started, delayMs = 500) {
                Column {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(
                        "Участники команды", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp
                    )
                    Surface(shape = RoundedCornerShape(10.dp), color = DarkTheme.AccentSoft) {
                        Text(
                            "${team.members.size} чел.",
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.Accent
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                team.members.forEach { member ->
                    MemberCard(member = member, isCurrentUser = member.id == user.id, sportName = team.sportName)
                    Spacer(Modifier.height(8.dp))
                }
                }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Screen-specific composables ──

@Composable
private fun TeamStatItem(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    val accent = DarkTheme.Accent
    val accentSoft = DarkTheme.AccentSoft
    val cardBg = DarkTheme.CardBg
    val cardBorder = DarkTheme.CardBorder
    val textPrimary = DarkTheme.TextPrimary
    val textMuted = DarkTheme.TextMuted

    Surface(modifier.height(80.dp), RoundedCornerShape(16.dp), cardBg) {
        Column(
            Modifier
                .border(0.5.dp, cardBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(accentSoft),
                Alignment.Center
            ) { Icon(icon, null, Modifier.size(16.dp), accent) }
            Spacer(Modifier.height(5.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = textPrimary, letterSpacing = (-0.3).sp)
            Text(label, fontSize = 10.sp, color = textMuted)
        }
    }
}

@Composable
private fun TeamInfoRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(DarkTheme.CardBorder.copy(alpha = 0.5f)),
            Alignment.Center
        ) {
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
private fun MemberCard(member: TeamMember, isCurrentUser: Boolean, sportName: String) {
    val accent = DarkTheme.Accent
    val accentSoft = DarkTheme.AccentSoft
    val cardBg = DarkTheme.CardBg
    val cardBorder = DarkTheme.CardBorder
    val textPrimary = DarkTheme.TextPrimary
    val textMuted = DarkTheme.TextMuted

    Surface(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(18.dp),
        if (isCurrentUser) accent.copy(alpha = 0.07f) else cardBg,
        border = ButtonDefaults.outlinedButtonBorder(true).copy(
            brush = Brush.linearGradient(
                if (isCurrentUser) listOf(accent.copy(alpha = 0.35f), accent.copy(alpha = 0.1f))
                else listOf(cardBorder.copy(alpha = 0.6f), cardBorder.copy(alpha = 0.2f))
            )
        )
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Аватар участника с бейджем спорта
            Box {
                UserAvatar(
                    avatarUrl = member.avatarUrl,
                    displayName = member.name,
                    size = 48.dp
                )
                // Спортивная иконка-бейдж
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isCurrentUser) accent else cardBg)
                        .border(1.5.dp, if (isCurrentUser) accent.copy(alpha = 0.3f) else cardBorder, CircleShape),
                    Alignment.Center
                ) {
                    Icon(sportIcon(sportName), null, Modifier.size(11.dp), if (isCurrentUser) Color.White else textMuted)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        member.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = if (isCurrentUser) accent else textPrimary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isCurrentUser) {
                        Spacer(Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = accentSoft) {
                            Text(
                                "Вы", Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp, color = accent, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(member.role, fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(shape = RoundedCornerShape(8.dp), color = if (isCurrentUser) accentSoft else cardBorder.copy(alpha = 0.3f)) {
                    Text(
                        "${member.wins} побед",
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = if (isCurrentUser) accent else textPrimary
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text("${member.tournaments} турн.", fontSize = 11.sp, color = textMuted)
            }
        }
    }
}
