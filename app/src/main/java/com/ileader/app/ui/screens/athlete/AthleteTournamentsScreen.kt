package com.ileader.app.ui.screens.athlete

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AthleteTournamentsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AthleteTournamentsScreen(user: User) {
    val viewModel: AthleteTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    var selectedTournamentId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    selectedTournamentId?.let { id ->
        AthleteTournamentDetailScreen(
            tournamentId = id,
            user = user,
            viewModel = viewModel,
            onBack = { selectedTournamentId = null }
        )
        return
    }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> TournamentsListContent(
            user = user,
            allTournaments = s.data.availableTournaments,
            sports = s.data.sports,
            myTournaments = s.data.myTournaments,
            onTournamentClick = { selectedTournamentId = it }
        )
    }
}

@Composable
private fun TournamentsListContent(
    user: User,
    allTournaments: List<Tournament>,
    sports: List<Pair<String, String>>,
    myTournaments: List<Tournament>,
    onTournamentClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableIntStateOf(0) }
    var selectedSport by remember { mutableIntStateOf(0) }

    val statusFilters = listOf("Все", "Открыты", "Активные", "Завершённые")
    val sportFilters = listOf("Все виды") + sports.map { it.first }

    val filteredTournaments = allTournaments.filter { t ->
        val matchesSearch = searchQuery.isEmpty() ||
            t.name.lowercase().contains(searchQuery.lowercase())
        val matchesStatus = selectedStatus == 0 || when (selectedStatus) {
            1 -> t.status == TournamentStatus.REGISTRATION_OPEN
            2 -> t.status == TournamentStatus.IN_PROGRESS
            3 -> t.status == TournamentStatus.COMPLETED
            else -> true
        }
        val matchesSport = selectedSport == 0 || t.sportName == sportFilters[selectedSport]
        matchesSearch && matchesStatus && matchesSport
    }

    val myCount = myTournaments.size
    val openCount = allTournaments.count { it.status == TournamentStatus.REGISTRATION_OPEN }
    val activeCount = allTournaments.count { it.status == TournamentStatus.IN_PROGRESS }

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

            // ── HEADER ──
            FadeIn(visible = started, delayMs = 0) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Турниры", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(4.dp))
                    Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                }
                UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
            }
            }

            Spacer(Modifier.height(20.dp))

            // ── STATS ──
            FadeIn(visible = started, delayMs = 150) {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                MiniStatAccent(Modifier.weight(1f), "Мои", myCount.toString())
                MiniStatAccent(Modifier.weight(1f), "Открыты", openCount.toString())
                MiniStatAccent(Modifier.weight(1f), "Активные", activeCount.toString())
            }
            }

            Spacer(Modifier.height(16.dp))

            // ── SEARCH ──
            FadeIn(visible = started, delayMs = 300) {
            DarkSearchField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = "Поиск турниров...")
            }

            Spacer(Modifier.height(12.dp))

            // ── STATUS FILTERS ──
            FadeIn(visible = started, delayMs = 450) {
            Column {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                statusFilters.forEachIndexed { index, label ->
                    DarkFilterChip(label, selectedStatus == index, { selectedStatus = index })
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── SPORT FILTERS ──
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                sportFilters.forEachIndexed { index, label ->
                    DarkFilterChip(label, selectedSport == index, { selectedSport = index })
                }
            }
            }
            }

            Spacer(Modifier.height(16.dp))

            // ── TOURNAMENT LIST ──
            FadeIn(visible = started, delayMs = 600) {
            Column {
            if (filteredTournaments.isEmpty()) {
                EmptyState("Турниры не найдены", "Попробуйте изменить фильтры")
            } else {
                filteredTournaments.forEach { tournament ->
                    TournamentListCard(tournament, onClick = { onTournamentClick(tournament.id) })
                    Spacer(Modifier.height(10.dp))
                }
            }
            }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Screen-specific: mini stat with accent-colored value ──
@Composable
private fun MiniStatAccent(modifier: Modifier, label: String, value: String) {
    Surface(modifier, RoundedCornerShape(12.dp), DarkTheme.CardBg) {
        Column(
            Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkTheme.Accent, letterSpacing = (-0.3).sp)
            Text(label, fontSize = 12.sp, color = DarkTheme.TextMuted)
        }
    }
}

@Composable
private fun TournamentListCard(tournament: Tournament, onClick: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        RoundedCornerShape(16.dp), DarkTheme.CardBg,
        border = DarkTheme.cardBorderStroke
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    AccentIconBox(Icons.Default.EmojiEvents)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(tournament.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(3.dp))
                        Text("${tournament.startDate} · ${tournament.sportName}",
                            fontSize = 12.sp, color = DarkTheme.TextSecondary)
                    }
                }
                Spacer(Modifier.width(8.dp))
                TournamentStatusBadge(tournament.status)
            }

            if (tournament.location.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp), DarkTheme.TextMuted)
                    Spacer(Modifier.width(6.dp))
                    Text(tournament.location, fontSize = 12.sp, color = DarkTheme.TextSecondary)
                }
            }

            if (tournament.maxParticipants > 0) {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${tournament.currentParticipants}/${tournament.maxParticipants}",
                        fontSize = 12.sp, color = DarkTheme.TextSecondary)
                    Spacer(Modifier.width(8.dp))
                    DarkProgressBar(
                        tournament.currentParticipants.toFloat() / tournament.maxParticipants,
                        Modifier.weight(1f).height(4.dp)
                    )
                }
            }

            if (tournament.prize.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Paid, null, Modifier.size(16.dp), DarkTheme.Accent)
                    Spacer(Modifier.width(6.dp))
                    Text("Призовой фонд: ${tournament.prize}", fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, color = DarkTheme.TextPrimary)
                }
            }
        }
    }
}
