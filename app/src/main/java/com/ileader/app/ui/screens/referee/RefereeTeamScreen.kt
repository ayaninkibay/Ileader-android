package com.ileader.app.ui.screens.referee

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
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
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.RefereeTeamViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun RefereeTeamScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    val hasTeam = user.teamId != null

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

            FadeIn(visible, 0) {
                Text("Команда", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(4.dp))
                Text(user.displayName, fontSize = 14.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(28.dp))

            FadeIn(visible, 200) {
                val teamId = user.teamId
                if (teamId == null) {
                    NoTeamState()
                } else {
                    TeamWithData(teamId)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TeamWithData(teamId: String) {
    val viewModel: RefereeTeamViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(teamId) { viewModel.load(teamId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(teamId) }
        is UiState.Success -> TeamContent(s.data.team)
    }
}

@Composable
private fun NoTeamState() {
    DarkCardPadded(padding = 20.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(CardBorder.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Groups, null, tint = TextMuted, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Вы пока не состоите в команде", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Создайте команду или присоединитесь", fontSize = 13.sp, color = TextSecondary)

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Создать команду", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.Search, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Найти команду", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun TeamContent(team: Team) {
    // Team info
    DarkCardPadded(padding = 20.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier.size(80.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Accent, AccentDark))),
                contentAlignment = Alignment.Center
            ) {
                Text(team.name.take(2).uppercase(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(12.dp))
            Text(team.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(team.sportName.ifEmpty { "Мультиспорт" }, fontSize = 13.sp, color = TextSecondary)

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Владелец", fontSize = 11.sp, color = TextMuted)
                    Text(team.trainerName.ifEmpty { "—" }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Вид спорта", fontSize = 11.sp, color = TextMuted)
                    Text(team.sportName.ifEmpty { "—" }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Основана", fontSize = 11.sp, color = TextMuted)
                    Text(team.foundedDate.ifEmpty { "—" }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Пригласить", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Assignment, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Заявки", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // Team stats
    DarkCard {
        Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
            TeamStatItem("Участники", "${team.members.size}")
            TeamStatItem("Турниры", "—")
            TeamStatItem("Побед", "—")
        }
    }

    Spacer(Modifier.height(12.dp))

    // Members
    if (team.members.isNotEmpty()) {
        DarkCardPadded(padding = 20.dp) {
            Text("Участники команды", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                color = TextPrimary, letterSpacing = (-0.3).sp)
            Spacer(Modifier.height(12.dp))

            team.members.forEach { member ->
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    color = CardBorder.copy(alpha = 0.3f)) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(
                                avatarUrl = member.avatarUrl,
                                displayName = member.name,
                                size = 40.dp,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(member.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text(member.role, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun TeamStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
            color = TextPrimary, letterSpacing = (-0.3).sp)
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}
