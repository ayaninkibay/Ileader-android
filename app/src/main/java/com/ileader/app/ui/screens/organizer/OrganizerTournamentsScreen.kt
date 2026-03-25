package com.ileader.app.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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

private val BASE_URL_O = "https://ileader.kz/img"
private val UNSPLASH_O = "https://images.unsplash.com"
private val SPORT_IMAGES_O = mapOf(
    "картинг" to listOf("$BASE_URL_O/karting/karting-04-1280x853.jpeg", "$BASE_URL_O/karting/karting-07-1280x853.jpeg", "$BASE_URL_O/karting/karting-13-1280x853.jpeg"),
    "стрельба" to listOf("$BASE_URL_O/shooting/shooting-02-1280x853.jpeg", "$BASE_URL_O/shooting/shooting-04-1280x853.jpeg"),
    "теннис" to listOf("$UNSPLASH_O/photo-1554068865-24cecd4e34b8?w=1280&q=80&fit=crop"),
    "футбол" to listOf("$UNSPLASH_O/photo-1431324155629-1a6deb1dec8d?w=1280&q=80&fit=crop"),
    "бокс" to listOf("$UNSPLASH_O/photo-1549719386-74dfcbf7dbed?w=1280&q=80&fit=crop"),
    "плавание" to listOf("$UNSPLASH_O/photo-1519315901367-f34ff9154487?w=1280&q=80&fit=crop"),
    "лёгкая атлетика" to listOf("$UNSPLASH_O/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop"),
    "легкая атлетика" to listOf("$UNSPLASH_O/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop"),
)

private fun tournamentImageUrl(sportName: String?, imageUrl: String?, seed: Int = 0): String? {
    imageUrl?.takeIf { it.isNotEmpty() }?.let { return it }
    val list = SPORT_IMAGES_O[sportName?.lowercase()?.trim()] ?: return null
    return list[seed.mod(list.size)]
}

@Composable
fun OrganizerTournamentsScreen(user: User) {
    var screenMode by remember { mutableStateOf("list") }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var selectedName by remember { mutableStateOf("") }

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
                onResultsClick = { screenMode = "results" },
                onCheckInClick = { name -> selectedName = name; screenMode = "qr_scan" }
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
        "qr_scan" -> {
            val id = selectedId ?: return
            OrganizerQrScannerScreen(
                user = user,
                tournamentId = id,
                tournamentName = selectedName,
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
    val isRefreshing by tournamentsVm.isRefreshing.collectAsState()

    LaunchedEffect(user.id) {
        tournamentsVm.load(user.id)
        sportsVm.load(user.id)
    }

    when (val s = tournamentsState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { tournamentsVm.load(user.id) }
        is UiState.Success -> DarkPullRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { tournamentsVm.refresh(user.id) }
        ) {
            TournamentsLoadedContent(
                allTournaments = s.data,
                sports = (sportsState as? UiState.Success)?.data?.sports ?: emptyList(),
                onTournamentClick = onTournamentClick,
                onCreateClick = onCreateClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFilterSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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

    Box(Modifier.fillMaxSize()) {
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

            // Search + Filter
            FadeIn(visible = started, delayMs = 300) {
                val statusFilters = listOf(
                    "all" to "Все", "draft" to "Черновик", "open" to "Регистрация",
                    "progress" to "В процессе", "completed" to "Завершён", "cancelled" to "Отменён"
                )
                val activeFilters = (if (selectedStatus != "all") 1 else 0) + (if (selectedSport != "all") 1 else 0)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        DarkSearchField(searchQuery, { searchQuery = it }, "Поиск турнира...")
                    }
                    Surface(
                        onClick = { showFilterSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        color = DarkTheme.CardBg
                    ) {
                        Box(
                            Modifier
                                .border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.Tune, "Фильтры", Modifier.size(20.dp), Accent)
                            if (activeFilters > 0) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Accent,
                                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-6).dp).size(16.dp)
                                ) {
                                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                                        Text("$activeFilters", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showFilterSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showFilterSheet = false },
                    sheetState = sheetState,
                    containerColor = DarkTheme.CardBg,
                    dragHandle = {
                        Box(Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                            Box(Modifier.width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(DarkTheme.CardBorder))
                        }
                    }
                ) {
                    Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
                        Text("Фильтры", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(Modifier.height(20.dp))

                        Text("Статус", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        val statusFilters = listOf(
                            "all" to "Все", "draft" to "Черновик", "open" to "Регистрация",
                            "progress" to "В процессе", "completed" to "Завершён", "cancelled" to "Отменён"
                        )
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                            statusFilters.forEach { (value, label) ->
                                DarkFilterChip(label, selectedStatus == value, { selectedStatus = value })
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text("Вид спорта", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                            DarkFilterChip("Все спорты", selectedSport == "all", { selectedSport = "all" })
                            sports.take(6).forEach { sport ->
                                DarkFilterChip(sport.name, selectedSport == sport.id, { selectedSport = sport.id })
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { selectedStatus = "all"; selectedSport = "all" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder(true).copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(DarkTheme.CardBorder)
                                )
                            ) { Text("Сбросить", color = TextSecondary) }
                            Button(
                                onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { showFilterSheet = false } },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Accent)
                            ) { Text("Применить") }
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
                    filteredTournaments.forEachIndexed { index, tournament ->
                        OrgTournamentCard(tournament, seed = index, onClick = { onTournamentClick(tournament.id) })
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
private fun OrgTournamentCard(tournament: TournamentWithCountsDto, seed: Int = 0, onClick: () -> Unit) {
    val cardBg = DarkTheme.CardBg
    val cardImage = tournamentImageUrl(tournament.sportName, tournament.imageUrl, seed)
    val hasImage = cardImage != null
    val textPrimary = if (hasImage) Color.White else TextPrimary
    val textSecondary = if (hasImage) Color.White.copy(alpha = 0.75f) else TextSecondary
    val statusColor = tournamentStatusColor(tournament.status)

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .clickable(onClick = onClick)
    ) {
        if (cardImage != null) {
            AsyncImage(
                model = cardImage,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.55f)))
        } else {
            Box(Modifier.matchParentSize().background(cardBg))
        }

        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        tournament.name,
                        fontSize = 17.sp, fontWeight = FontWeight.Bold, color = textPrimary,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    if (!tournament.sportName.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(sportIcon(tournament.sportName), null, Modifier.size(13.dp), textSecondary)
                            Text(tournament.sportName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textSecondary)
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(
                        statusLabel(tournament.status),
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val startDate = formatShortDate(tournament.startDate)
                if (startDate.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.size(15.dp), textSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$startDate${if (tournament.endDate != null) " — ${formatShortDate(tournament.endDate)}" else ""}",
                            fontSize = 13.sp, color = textSecondary
                        )
                    }
                }
                val locationName = tournament.locationName
                if (!locationName.isNullOrEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(15.dp), textSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(locationName, fontSize = 13.sp, color = textSecondary)
                    }
                }
            }

            val maxP = tournament.maxParticipants ?: 0
            if (maxP > 0) {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, null, Modifier.size(15.dp), textSecondary)
                    Spacer(Modifier.width(6.dp))
                    Text("${tournament.participantCount}/$maxP", fontSize = 13.sp, color = textSecondary)
                    Spacer(Modifier.width(8.dp))
                    DarkProgressBar(
                        tournament.participantCount.toFloat() / maxP,
                        Modifier.weight(1f).height(4.dp)
                    )
                }
            }

            val prize = tournament.prize
            if (!prize.isNullOrEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Paid, null, Modifier.size(15.dp), textSecondary)
                    Spacer(Modifier.width(6.dp))
                    Text("Призовой фонд: $prize", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                }
            }
        }
    }
}
