package com.ileader.app.ui.screens.sponsor

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SponsorshipDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.SponsorTournamentsViewModel

@Composable
fun SponsorTournamentsScreen(user: User) {
    val viewModel: SponsorTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val appliedTournaments by viewModel.appliedTournaments.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTournamentId by remember { mutableStateOf<String?>(null) }

    selectedTournamentId?.let { tournamentId ->
        SponsorTournamentDetailScreen(
            sponsorId = user.id,
            tournamentId = tournamentId,
            onBack = { selectedTournamentId = null }
        )
        return
    }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> {
            val mySponsorships = s.data.sponsoredTournaments
                .filter { searchQuery.isEmpty() || (it.tournaments?.name ?: "").contains(searchQuery, ignoreCase = true) }

            val openTournaments = s.data.allTournaments
                .filter { it.status == "registration_open" || it.status == "in_progress" }
                .filter { searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) }

            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier.fillMaxSize().statusBarsPadding()
                        .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    FadeIn(visible, 0) {
                        Column {
                            Text("Турниры", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                            Spacer(Modifier.height(4.dp))
                            Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    FadeIn(visible, 100) {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DarkFilterChip("Открытые (${openTournaments.size})", selectedTab == 0, onClick = { selectedTab = 0 })
                            DarkFilterChip("Мои (${mySponsorships.size})", selectedTab == 1, onClick = { selectedTab = 1 })
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (selectedTab == 1 && mySponsorships.isNotEmpty()) {
                        FadeIn(visible, 150) {
                            val totalAmount = mySponsorships.sumOf { it.amount ?: 0.0 }.toLong()
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MiniStat("Турниров", "${mySponsorships.size}", Modifier.weight(1f))
                                MiniStat("Сумма", SponsorUtils.formatAmount(totalAmount), Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    FadeIn(visible, 200) {
                        DarkSearchField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = "Поиск турнира...")
                    }

                    Spacer(Modifier.height(16.dp))

                    FadeIn(visible, 300) {
                        when (selectedTab) {
                            0 -> {
                                if (openTournaments.isEmpty()) {
                                    EmptyState("Турниры не найдены", "Попробуйте изменить параметры поиска")
                                } else {
                                    openTournaments.forEach { tournament ->
                                        OpenTournamentItem(
                                            tournament = tournament,
                                            applied = tournament.id in appliedTournaments,
                                            onApply = { viewModel.requestTournamentSponsorship(user.id, tournament.id) },
                                            onClick = { selectedTournamentId = tournament.id }
                                        )
                                        Spacer(Modifier.height(10.dp))
                                    }
                                }
                            }
                            1 -> {
                                if (mySponsorships.isEmpty()) {
                                    EmptyState("Турниры не найдены", "Попробуйте изменить параметры поиска")
                                } else {
                                    mySponsorships.forEach { sponsorship ->
                                        MySponsorshipItem(sponsorship) { selectedTournamentId = sponsorship.tournamentId }
                                        Spacer(Modifier.height(10.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun OpenTournamentItem(tournament: TournamentWithCountsDto, applied: Boolean, onApply: () -> Unit, onClick: () -> Unit) {
    val chipColor = SponsorUtils.getStatusColor(tournament.status ?: "")

    DarkCard(modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick() }) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    SoftIconBox(Icons.Default.Star, size = 36.dp, iconSize = 18.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(tournament.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                }
                StatusBadge(SponsorUtils.getStatusLabel(tournament.status ?: ""), chipColor)
            }
            Spacer(Modifier.height(3.dp))
            Text(tournament.sportName ?: "", fontSize = 12.sp, color = DarkTheme.TextSecondary, modifier = Modifier.padding(start = 46.dp))

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${formatShortDate(tournament.startDate)} — ${formatShortDate(tournament.endDate)}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                Spacer(Modifier.width(16.dp))
                Icon(Icons.Default.LocationOn, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(tournament.locationName ?: "", fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${tournament.participantCount} участников", fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }

            Spacer(Modifier.height(14.dp))
            Button(onClick = onApply, enabled = !applied, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent, disabledContainerColor = DarkTheme.CardBorder)) {
                Icon(if (applied) Icons.Default.Check else Icons.Default.Handshake, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (applied) "Заявка отправлена" else "Подать заявку на спонсорство", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun MySponsorshipItem(item: SponsorshipDto, onClick: () -> Unit) {
    DarkCard(modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick() }) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(Icons.Default.EmojiEvents)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.tournaments?.name ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                Spacer(Modifier.height(3.dp))
                Text(item.tournaments?.sports?.name ?: "", fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            Text(SponsorUtils.formatAmount((item.amount ?: 0.0).toLong()),
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTheme.Accent)
        }
    }
}
