package com.ileader.app.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.*

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun OrganizerTournamentsScreen(user: User) {
    var screenMode by remember { mutableStateOf("list") }
    var selectedId by remember { mutableStateOf<String?>(null) }

    when (screenMode) {
        "list" -> TournamentsListContent(
            user = user,
            onTournamentClick = { id -> selectedId = id; screenMode = "detail" },
            onCreateClick = { screenMode = "create" }
        )
        "detail" -> {
            val id = selectedId ?: return
            OrganizerTournamentDetailScreen(
                tournamentId = id,
                userId = user.id,
                onBack = { screenMode = "list" },
                onEditClick = { screenMode = "edit" },
                onResultsClick = { screenMode = "results" }
            )
        }
        "edit" -> {
            val id = selectedId ?: return
            OrganizerTournamentEditScreen(
                tournamentId = id,
                userId = user.id,
                onBack = { screenMode = "detail" },
                onSave = { screenMode = "detail" }
            )
        }
        "create" -> OrganizerTournamentCreateScreen(
            userId = user.id,
            onBack = { screenMode = "list" },
            onCreated = { screenMode = "list" }
        )
        "results" -> {
            val id = selectedId ?: return
            OrganizerTournamentResultsScreen(
                tournamentId = id,
                onBack = { screenMode = "detail" }
            )
        }
    }
}

@Composable
private fun TournamentsListContent(
    user: User,
    onTournamentClick: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    val tournamentsVm: OrganizerTournamentsViewModel = viewModel()
    val sportsVm: OrganizerSportsViewModel = viewModel()
    val tournamentsState by tournamentsVm.state.collectAsState()
    val sportsState by sportsVm.state.collectAsState()

    LaunchedEffect(user.id) {
        tournamentsVm.load(user.id)
        sportsVm.load(user.id)
    }

    when (val s = tournamentsState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { tournamentsVm.load(user.id) }
        is UiState.Success -> TournamentsLoadedContent(
            allTournaments = s.data,
            sports = (sportsState as? UiState.Success)?.data?.sports ?: emptyList(),
            onTournamentClick = onTournamentClick,
            onCreateClick = onCreateClick
        )
    }
}

@Composable
private fun TournamentsLoadedContent(
    allTournaments: List<TournamentWithCountsDto>,
    sports: List<SportDto>,
    onTournamentClick: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("all") }
    var selectedSport by remember { mutableStateOf("all") }

    val filteredTournaments = allTournaments.filter { t ->
        val matchesSearch = searchQuery.isEmpty() ||
            t.name.lowercase().contains(searchQuery.lowercase())
        val matchesStatus = selectedStatus == "all" || when (selectedStatus) {
            "draft" -> t.status == "draft"
            "open" -> t.status == "registration_open"
            "progress" -> t.status == "in_progress"
            "completed" -> t.status == "completed"
            "cancelled" -> t.status == "cancelled"
            else -> true
        }
        val matchesSport = selectedSport == "all" || t.sportId == selectedSport
        matchesSearch && matchesStatus && matchesSport
    }

    val totalCount = allTournaments.size
    val activeCount = allTournaments.count {
        it.status == "in_progress" || it.status == "check_in"
    }
    val completedCount = allTournaments.count { it.status == "completed" }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    Box(Modifier.fillMaxSize().background(Bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Header
            FadeIn(visible = started, delayMs = 0) {
                Column {
                    Text("Турниры", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Управление турнирами вашей организации", fontSize = 14.sp, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Mini stats
            FadeIn(visible = started, delayMs = 150) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("Всего", totalCount.toString(), Modifier.weight(1f))
                    MiniStat("Активных", activeCount.toString(), Modifier.weight(1f))
                    MiniStat("Завершённых", completedCount.toString(), Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Search
            FadeIn(visible = started, delayMs = 300) {
                Column {
                    DarkSearchField(searchQuery, { searchQuery = it }, "Поиск турнира...")

                    Spacer(Modifier.height(12.dp))

                    // Status filters
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statusFilters = listOf(
                            "all" to "Все", "draft" to "Черновик", "open" to "Регистрация",
                            "progress" to "В процессе", "completed" to "Завершён", "cancelled" to "Отменён"
                        )
                        statusFilters.forEach { (value, label) ->
                            DarkFilterChip(label, selectedStatus == value, { selectedStatus = value })
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Sport filters
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DarkFilterChip("Все спорты", selectedSport == "all", { selectedSport = "all" })
                        sports.take(6).forEach { sport ->
                            DarkFilterChip(sport.name, selectedSport == sport.id, { selectedSport = sport.id })
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("${filteredTournaments.size} из $totalCount", fontSize = 12.sp, color = TextSecondary)
            Spacer(Modifier.height(12.dp))

            // Tournament cards
            FadeIn(visible = started, delayMs = 450) {
                Column {
                    filteredTournaments.forEach { tournament ->
                        OrgTournamentCard(tournament, onClick = { onTournamentClick(tournament.id) })
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        // FAB
        ExtendedFloatingActionButton(
            onClick = onCreateClick,
            containerColor = Accent,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Создать")
        }
    }
}

@Composable
private fun OrgTournamentCard(tournament: TournamentWithCountsDto, onClick: () -> Unit) {
    val isActive = isActiveStatus(tournament.status)
    val chipColor = if (isActive) Accent else TextMuted

    DarkCard(modifier = Modifier.clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(tournament.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                StatusBadge(statusLabel(tournament.status), chipColor)
            }

            Spacer(Modifier.height(8.dp))

            // Sport badge
            val sportName = tournament.sportName
            if (!sportName.isNullOrEmpty()) {
                StatusBadge(sportName, Accent)
                Spacer(Modifier.height(8.dp))
            }

            val startDate = tournament.startDate
            if (!startDate.isNullOrEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "$startDate${if (tournament.endDate != null) " — ${tournament.endDate}" else ""}",
                        fontSize = 12.sp, color = TextSecondary
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            val locationName = tournament.locationName
            if (!locationName.isNullOrEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text(locationName, fontSize = 12.sp, color = TextSecondary)
                }
            }

            // Progress bar
            val maxP = tournament.maxParticipants ?: 0
            if (maxP > 0) {
                Spacer(Modifier.height(8.dp))
                val progress = tournament.participantCount.toFloat() / maxP
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                    Spacer(Modifier.width(4.dp))
                    Box(Modifier.weight(1f)) {
                        DarkProgressBar(progress)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("${tournament.participantCount}/$maxP", fontSize = 12.sp, color = TextSecondary)
                }
            }

            // Prize
            val prize = tournament.prize
            if (!prize.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Paid, null, modifier = Modifier.size(14.dp), tint = Accent)
                    Spacer(Modifier.width(4.dp))
                    Text(prize, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                }
            }
        }
    }
}
