package com.ileader.app.ui.screens.media

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TeamDto
import com.ileader.app.data.remote.dto.TeamMemberDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.MediaTeamData
import com.ileader.app.ui.viewmodels.MediaTeamViewModel

@Composable
fun MediaTeamScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    if (user.teamId == null) {
        // No team - empty state (no ViewModel needed)
        NoTeamContent(user)
    } else {
        val viewModel: MediaTeamViewModel = viewModel()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(user.teamId) { viewModel.load(user.teamId) }

        when (val s = state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(s.message) { user.teamId?.let { viewModel.load(it) } }
            is UiState.Success -> TeamContent(user, s.data)
        }
    }
}

@Composable
private fun NoTeamContent(user: User) {
    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(64.dp).clip(CircleShape)
                    .background(DarkTheme.CardBorder.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Groups, null, Modifier.size(30.dp), DarkTheme.TextMuted)
            }
            Spacer(Modifier.height(16.dp))
            Text("Вы не состоите в команде", fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Создайте команду или присоединитесь к существующей",
                fontSize = 13.sp, color = DarkTheme.TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { /* TODO: будет реализовано с БД */ },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Создать команду", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { /* TODO: будет реализовано с БД */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.TextSecondary),
                    border = DarkTheme.cardBorderStroke
                ) {
                    Text("Присоединиться", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TeamContent(user: User, data: MediaTeamData) {
    val team = data.team
    val members = data.members

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── HEADER ──
            FadeIn(visible, 0) {
                Column {
                    Text("Команда", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(4.dp))
                    Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Team info card
            FadeIn(visible, 150) {
                DarkCard {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(listOf(DarkTheme.Accent, DarkTheme.AccentDark))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(team.name.take(2).uppercase(), fontSize = 20.sp,
                                fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text(team.name, fontSize = 18.sp,
                                fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary)
                            Text("${members.size} участников",
                                fontSize = 13.sp, color = DarkTheme.TextSecondary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Members section
            FadeIn(visible, 300) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween,
                    Alignment.CenterVertically) {
                    Text("Участники", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
                    Button(
                        onClick = { /* TODO: будет реализовано с БД */ },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Пригласить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            FadeIn(visible, 450) {
                members.forEach { member ->
                    TeamMemberItem(member = member, ownerId = team.ownerId)
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TeamMemberItem(member: TeamMemberDto, ownerId: String?) {
    val memberName = member.profiles?.name ?: "Участник"
    val memberRole = when (member.role) {
        "captain" -> "Капитан"
        "member" -> "Участник"
        "reserve" -> "Запасной"
        else -> member.role ?: "Участник"
    }
    val isOwner = member.profiles?.id == ownerId

    Surface(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        color = DarkTheme.CardBg, border = DarkTheme.cardBorderStroke
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(44.dp).clip(CircleShape)
                    .background(DarkTheme.AccentSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(memberName.take(1), fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold, color = DarkTheme.Accent)
            }
            Column(Modifier.weight(1f)) {
                Text(memberName, fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp, color = DarkTheme.TextPrimary)
                Text(memberRole, fontSize = 13.sp, color = DarkTheme.TextSecondary)
            }
            if (isOwner) {
                StatusBadge("Владелец", DarkTheme.Accent)
            }
        }
    }
}
