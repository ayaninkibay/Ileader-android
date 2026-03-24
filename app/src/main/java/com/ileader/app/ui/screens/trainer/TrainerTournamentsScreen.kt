package com.ileader.app.ui.screens.trainer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.Tournament
import com.ileader.app.data.models.TournamentStatus
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.TrainerTournamentsViewModel

@Composable
fun TrainerTournamentsScreen(user: User) {
    val viewModel: TrainerTournamentsViewModel = viewModel()
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
            var searchQuery by remember { mutableStateOf("") }
            var selectedFilter by remember { mutableIntStateOf(0) }
            val filters = listOf("Все", "Доступные", "Зарег.", "Завершённые")
            val registeredIds = data.registeredTournamentIds[selectedTeam.id] ?: emptyList()

            val filteredTournaments = data.tournaments
                .filter { it.sportId == selectedTeam.sportId || selectedFilter == 0 }
                .filter { t ->
                    when (selectedFilter) {
                        1 -> t.status == TournamentStatus.REGISTRATION_OPEN && t.id !in registeredIds
                        2 -> t.id in registeredIds
                        3 -> t.status == TournamentStatus.COMPLETED
                        else -> true
                    }
                }
                .filter { searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) }

            val totalT = data.tournaments.count { it.sportId == selectedTeam.sportId }
            val registeredCount = registeredIds.size
            val availableCount = data.tournaments.count { it.sportId == selectedTeam.sportId && it.status == TournamentStatus.REGISTRATION_OPEN && it.id !in registeredIds }
            val completedCount = data.tournaments.count { it.sportId == selectedTeam.sportId && it.status == TournamentStatus.COMPLETED }

            var showRegisterDialog by remember { mutableStateOf<Tournament?>(null) }

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
                    Column {
                        Text("Турниры", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                        Spacer(Modifier.height(4.dp))
                        Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                    }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── TEAM SELECTOR ──
                    if (teams.size > 1) {
                        var expanded by remember { mutableStateOf(false) }
                        DarkCard {
                            Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SoftIconBox(Icons.Default.Groups, size = 36.dp, iconSize = 18.dp)
                                Spacer(Modifier.width(10.dp))
                                Text(selectedTeam.name, Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                                IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(20.dp), DarkTheme.TextMuted)
                                }
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                teams.forEachIndexed { index, team ->
                                    DropdownMenuItem(
                                        text = { Text("${team.name} (${team.sportName})", fontSize = 14.sp) },
                                        onClick = { selectedTeamIndex = index; expanded = false; selectedFilter = 0 },
                                        leadingIcon = { if (index == selectedTeamIndex) Icon(Icons.Default.Check, null, tint = DarkTheme.Accent) }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // ── STATS ──
                    FadeIn(visible = started, delayMs = 150) {
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        MiniStat("Всего", totalT.toString(), Modifier.weight(1f))
                        MiniStat("Зарег.", registeredCount.toString(), Modifier.weight(1f))
                        MiniStat("Доступно", availableCount.toString(), Modifier.weight(1f))
                        MiniStat("Заверш.", completedCount.toString(), Modifier.weight(1f))
                    }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── SEARCH ──
                    FadeIn(visible = started, delayMs = 300) {
                    Column {
                    DarkSearchField(searchQuery, { searchQuery = it }, "Поиск турнира...")

                    Spacer(Modifier.height(12.dp))

                    // ── FILTERS ──
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        filters.forEachIndexed { index, filter ->
                            DarkFilterChip(filter, selectedFilter == index, { selectedFilter = index })
                        }
                    }
                    }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── TOURNAMENT LIST ──
                    FadeIn(visible = started, delayMs = 450) {
                    if (filteredTournaments.isEmpty()) {
                        EmptyState("Турниров не найдено")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            filteredTournaments.forEach { tournament ->
                                val isRegistered = tournament.id in registeredIds
                                TournamentCard(
                                    tournament, isRegistered,
                                    { showRegisterDialog = tournament },
                                    {
                                        viewModel.unregisterTeam(tournament.id, selectedTeam.id, user.id)
                                    }
                                )
                            }
                        }
                    }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }

            // ── REGISTER DIALOG ──
            showRegisterDialog?.let { tournament ->
                AlertDialog(
                    onDismissRequest = { showRegisterDialog = null },
                    containerColor = DarkTheme.CardBg,
                    titleContentColor = DarkTheme.TextPrimary,
                    textContentColor = DarkTheme.TextSecondary,
                    title = { Text("Регистрация на турнир", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Зарегистрировать команду «${selectedTeam.name}» на турнир:", fontSize = 14.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(tournament.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                            Text("${formatShortDate(tournament.startDate)} · ${tournament.location}", fontSize = 13.sp)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.registerTeam(
                                    tournament.id,
                                    selectedTeam.id,
                                    selectedTeam.members.map { it.id },
                                    user.id
                                )
                                showRegisterDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                        ) { Text("Зарегистрировать") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRegisterDialog = null }) { Text("Отмена", color = DarkTheme.TextSecondary) }
                    }
                )
            }
        }
    }
}

@Composable
private fun TournamentCard(tournament: Tournament, isRegistered: Boolean, onRegister: () -> Unit, onUnregister: () -> Unit) {
    DarkCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AccentIconBox(Icons.Default.EmojiEvents)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(tournament.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${tournament.sportName} · ${tournament.ageCategory ?: ""}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                }
                val isActive = tournament.status == TournamentStatus.REGISTRATION_OPEN || tournament.status == TournamentStatus.IN_PROGRESS
                StatusBadge(tournament.status.displayName, if (isActive) DarkTheme.Accent else DarkTheme.TextMuted)
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Дата", fontSize = 11.sp, color = DarkTheme.TextMuted)
                    Text(formatShortDate(tournament.startDate), fontSize = 13.sp, color = DarkTheme.TextPrimary, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Место", fontSize = 11.sp, color = DarkTheme.TextMuted)
                    Text(tournament.location, fontSize = 13.sp, color = DarkTheme.TextPrimary, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${tournament.currentParticipants}/${tournament.maxParticipants}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                Spacer(Modifier.width(8.dp))
                val fraction = if (tournament.maxParticipants > 0) tournament.currentParticipants.toFloat() / tournament.maxParticipants else 0f
                DarkProgressBar(fraction, Modifier.weight(1f))
                if (tournament.prize.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Text(tournament.prize, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.Accent)
                }
            }

            if (tournament.status == TournamentStatus.REGISTRATION_OPEN) {
                Spacer(Modifier.height(12.dp))
                if (isRegistered) {
                    OutlinedButton(
                        onClick = onUnregister,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.Accent),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = Brush.linearGradient(listOf(DarkTheme.Accent.copy(alpha = 0.3f), DarkTheme.Accent.copy(alpha = 0.3f)))
                        )
                    ) {
                        Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Отменить регистрацию", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = onRegister,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                    ) {
                        Icon(Icons.Default.AppRegistration, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Зарегистрировать команду", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
