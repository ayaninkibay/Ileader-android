package com.ileader.app.ui.screens.sponsor

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.background
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.ui.screens.sponsor.SponsorUtils
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SponsorshipDto
import com.ileader.app.data.remote.dto.TeamWithStatsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.SponsorDashboardViewModel
import com.ileader.app.ui.viewmodels.SponsorTeamsViewModel

@Composable
fun SponsorTeamsScreen(user: User) {
    val dashboardVm: SponsorDashboardViewModel = viewModel()
    val dashState by dashboardVm.state.collectAsState()
    val teamsVm: SponsorTeamsViewModel = viewModel()
    val teamsState by teamsVm.state.collectAsState()

    LaunchedEffect(user.id) {
        dashboardVm.load(user.id)
        teamsVm.load()
    }

    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var selectedTeamId by remember { mutableStateOf<String?>(null) }

    selectedTeamId?.let { teamId ->
        SponsorMyTeamScreen(sponsorId = user.id, teamId = teamId, onBack = { selectedTeamId = null })
        return
    }

    when (val ds = dashState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(ds.message) { dashboardVm.load(user.id) }
        is UiState.Success -> {
            val teamSponsorships = ds.data.sponsorships
                .filter { it.teamId != null }
                .filter { searchQuery.isEmpty() || (it.teams?.name ?: "").contains(searchQuery, ignoreCase = true) }

            val totalAmount = teamSponsorships.sumOf { it.amount ?: 0.0 }.toLong()
            val sportCount = teamSponsorships.mapNotNull { it.tournaments?.sports?.name }.distinct().size

            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    FadeIn(visible, 0) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column {
                                Text("Команды", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                    color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                                Spacer(Modifier.height(4.dp))
                                Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                            }
                            Button(
                                onClick = { showDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Handshake, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Спонсировать", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    FadeIn(visible, 100) {
                        DarkSearchField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = "Поиск команды...")
                    }

                    Spacer(Modifier.height(16.dp))

                    FadeIn(visible, 200) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MiniStat("Команд", "${teamSponsorships.size}", Modifier.weight(1f))
                            MiniStat("Сумма", SponsorUtils.formatAmount(totalAmount), Modifier.weight(1f))
                            MiniStat("Видов спорта", "$sportCount", Modifier.weight(1f))
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    FadeIn(visible, 300) {
                        if (teamSponsorships.isEmpty()) {
                            EmptyState("Нет спонсируемых команд", "Нажмите \"Спонсировать\" чтобы выбрать команду")
                        } else {
                            teamSponsorships.forEach { item ->
                                TeamSponsorshipCard(item) { selectedTeamId = item.teamId }
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }

            if (showDialog) {
                val availableTeams = (teamsState as? UiState.Success)?.data?.teams ?: emptyList()
                SponsorTeamDialog(
                    teams = availableTeams,
                    onSponsor = { teamId -> teamsVm.requestSponsorship(user.id, teamId) },
                    onDismiss = { showDialog = false }
                )
            }
        }
    }
}

@Composable
private fun TeamSponsorshipCard(item: SponsorshipDto, onClick: () -> Unit) {
    DarkCard(modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick() }) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(Icons.Default.Groups)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.teams?.name ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("${item.startDate ?: ""} — ${item.endDate ?: ""}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                }
            }
            Text(SponsorUtils.formatAmount((item.amount ?: 0.0).toLong()),
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTheme.Accent)
        }
    }
}

@Composable
private fun SponsorTeamDialog(teams: List<TeamWithStatsDto>, onSponsor: (String) -> Unit, onDismiss: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var sponsoredIds by remember { mutableStateOf(setOf<String>()) }
    val filteredTeams = teams.filter { it.id !in sponsoredIds }.filter { searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.75f), shape = RoundedCornerShape(20.dp), color = DarkTheme.Bg) {
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Выберите команду", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Закрыть", tint = DarkTheme.TextSecondary) }
                }
                Spacer(Modifier.height(12.dp))
                DarkSearchField(value = searchQuery, onValueChange = { searchQuery = it })
                Spacer(Modifier.height(12.dp))
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    if (filteredTeams.isEmpty()) {
                        Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(52.dp).clip(CircleShape).background(DarkTheme.CardBorder.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SearchOff, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Нет доступных команд", fontSize = 14.sp, color = DarkTheme.TextSecondary)
                        }
                    } else {
                        filteredTeams.forEach { team ->
                            AvailableTeamRow(team) { sponsoredIds = sponsoredIds + team.id; onSponsor(team.id) }
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailableTeamRow(team: TeamWithStatsDto, onSponsor: () -> Unit) {
    var sent by remember { mutableStateOf(false) }
    DarkCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            SoftIconBox(Icons.Default.Groups)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(team.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                Text("${team.sports?.name ?: ""} · ${team.profiles?.name ?: ""} · ${team.memberCount} чел.", fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { sent = true; onSponsor() }, enabled = !sent, shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent, disabledContainerColor = DarkTheme.CardBorder),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                Icon(if (sent) Icons.Default.Check else Icons.Default.Handshake, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (sent) "Отправлено" else "Спонсировать", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
