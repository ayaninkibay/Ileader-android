package com.ileader.app.ui.screens.trainer

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.InviteResult
import com.ileader.app.ui.viewmodels.TrainerTeamViewModel
import com.ileader.app.ui.viewmodels.getTeamStats

@Composable
fun TrainerTeamScreen(user: User) {
    val viewModel: TrainerTeamViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val inviteResult by viewModel.inviteResult.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    // Internal navigation state
    var selectedAthleteId by remember { mutableStateOf<String?>(null) }

    selectedAthleteId?.let { id ->
        TrainerAthleteDetailScreen(
            user = user,
            athleteId = id,
            onBack = { selectedAthleteId = null }
        )
        return
    }

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
            val pendingInvites = data.pendingInvites.filter { it.teamId == selectedTeam.id }
            var showInviteDialog by remember { mutableStateOf(false) }

            var started by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { started = true }

            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    // ── HEADER ──
                    FadeIn(visible = started, delayMs = 0) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column {
                            Text("Моя команда", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                            Spacer(Modifier.height(4.dp))
                            Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                        }
                        Button(
                            onClick = { showInviteDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Пригласить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── TEAM SELECTOR ──
                    if (teams.size > 1) {
                        var expanded by remember { mutableStateOf(false) }
                        DarkCard {
                            Row(
                                Modifier.clickable { expanded = true }.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SoftIconBox(Icons.Default.Groups)
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
                        Spacer(Modifier.height(16.dp))
                    }

                    // ── TEAM INFO CARD ──
                    FadeIn(visible = started, delayMs = 150) {
                    DarkCard {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(56.dp).clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(DarkTheme.Accent, DarkTheme.AccentDark))),
                                    Alignment.Center
                                ) {
                                    Icon(Icons.Default.Shield, null, Modifier.size(28.dp), Color.White)
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text(selectedTeam.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary)
                                    Text("${selectedTeam.sportName} · Осн. ${selectedTeam.foundedYear}", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(selectedTeam.description, fontSize = 13.sp, color = DarkTheme.TextSecondary, lineHeight = 20.sp)
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                                TeamInfoStat("Спортсменов", teamStats.athleteCount.toString())
                                TeamInfoStat("Турниров", teamStats.totalTournaments.toString())
                                TeamInfoStat("Побед", teamStats.totalWins.toString())
                                TeamInfoStat("Рейтинг", teamStats.avgRating.toString())
                            }
                        }
                    }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── PENDING INVITES ──
                    FadeIn(visible = started, delayMs = 300) {
                    Column {
                    if (pendingInvites.isNotEmpty()) {
                        SectionHeader("Ожидающие приглашения")
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            pendingInvites.forEach { invite ->
                                DarkCard {
                                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        SoftIconBox(Icons.Default.Schedule)
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(invite.athleteName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                                            Text("Отправлено ${invite.sentAt}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                                        }
                                        StatusBadge("Ожидает", DarkTheme.Accent)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(28.dp))
                    }
                    }
                    }

                    // ── ATHLETES LIST ──
                    FadeIn(visible = started, delayMs = 450) {
                    Column {
                    SectionHeader("Спортсмены (${selectedTeam.members.size})")
                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedTeam.members.forEach { athlete ->
                            DarkCard(Modifier.clickable { selectedAthleteId = athlete.id }) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Аватар с бейджем спорта
                                    Box {
                                        UserAvatar(
                                            avatarUrl = athlete.avatarUrl,
                                            displayName = athlete.name,
                                            size = 44.dp
                                        )
                                        Box(
                                            Modifier
                                                .align(Alignment.BottomEnd)
                                                .offset(x = 3.dp, y = 3.dp)
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(DarkTheme.CardBg)
                                                .border(1.5.dp, DarkTheme.CardBorder, CircleShape),
                                            Alignment.Center
                                        ) {
                                            Icon(sportIcon(selectedTeam.sportName), null, Modifier.size(10.dp), DarkTheme.TextMuted)
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(athlete.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                                        Spacer(Modifier.height(2.dp))
                                        Text(athlete.email, fontSize = 12.sp, color = DarkTheme.TextSecondary)
                                        Spacer(Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            SmallIconStat(Icons.Default.EmojiEvents, "${athlete.tournaments}")
                                            SmallIconStat(Icons.Default.Star, "${athlete.wins}")
                                            SmallIconStat(Icons.Default.TrendingUp, "${athlete.rating}")
                                        }
                                    }
                                    Icon(Icons.Default.ChevronRight, null, Modifier.size(20.dp), DarkTheme.TextMuted)
                                }
                            }
                        }
                    }
                    }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }

            // ── INVITE DIALOG ──
            if (showInviteDialog) {
                var email by remember { mutableStateOf("") }
                var inviteError by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(inviteResult) {
                    when (val r = inviteResult) {
                        is InviteResult.Success -> {
                            showInviteDialog = false
                            viewModel.clearInviteResult()
                        }
                        is InviteResult.Error -> {
                            inviteError = r.message
                            viewModel.clearInviteResult()
                        }
                        null -> {}
                    }
                }

                AlertDialog(
                    onDismissRequest = { showInviteDialog = false },
                    containerColor = DarkTheme.CardBg,
                    titleContentColor = DarkTheme.TextPrimary,
                    textContentColor = DarkTheme.TextSecondary,
                    title = { Text("Пригласить спортсмена", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Введите email спортсмена для отправки приглашения в команду «${selectedTeam.name}»", fontSize = 14.sp)
                            Spacer(Modifier.height(16.dp))
                            DarkFormField("Email", email, { email = it; inviteError = null }, placeholder = "email@example.com")
                            if (inviteError != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(inviteError.orEmpty(), fontSize = 12.sp, color = DarkTheme.Accent)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (email.isNotBlank()) {
                                    viewModel.inviteAthlete(selectedTeam.id, email.trim(), user.id)
                                }
                            },
                            enabled = email.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                        ) { Text("Отправить") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showInviteDialog = false }) { Text("Отмена", color = DarkTheme.TextSecondary) }
                    }
                )
            }
        }
    }
}

@Composable
private fun TeamInfoStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary)
        Text(label, fontSize = 11.sp, color = DarkTheme.TextMuted)
    }
}

@Composable
private fun SmallIconStat(icon: ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(14.dp), DarkTheme.TextMuted)
        Spacer(Modifier.width(2.dp))
        Text(value, fontSize = 11.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.Medium)
    }
}
