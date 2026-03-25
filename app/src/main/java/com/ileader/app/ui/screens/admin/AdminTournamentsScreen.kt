package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.mock.AdminMockData
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.screens.athlete.AthleteTournamentDetailScreen
import com.ileader.app.ui.viewmodels.AdminTournamentsViewModel
import com.ileader.app.ui.viewmodels.AthleteTournamentsViewModel

private val BASE_URL_A = "https://ileader.kz/img"
private val UNSPLASH_A = "https://images.unsplash.com"
private val SPORT_IMAGES_A = mapOf(
    "картинг" to listOf("$BASE_URL_A/karting/karting-04-1280x853.jpeg", "$BASE_URL_A/karting/karting-07-1280x853.jpeg", "$BASE_URL_A/karting/karting-13-1280x853.jpeg"),
    "стрельба" to listOf("$BASE_URL_A/shooting/shooting-02-1280x853.jpeg", "$BASE_URL_A/shooting/shooting-04-1280x853.jpeg"),
    "теннис" to listOf("$UNSPLASH_A/photo-1554068865-24cecd4e34b8?w=1280&q=80&fit=crop"),
    "футбол" to listOf("$UNSPLASH_A/photo-1431324155629-1a6deb1dec8d?w=1280&q=80&fit=crop"),
    "бокс" to listOf("$UNSPLASH_A/photo-1549719386-74dfcbf7dbed?w=1280&q=80&fit=crop"),
    "плавание" to listOf("$UNSPLASH_A/photo-1519315901367-f34ff9154487?w=1280&q=80&fit=crop"),
    "лёгкая атлетика" to listOf("$UNSPLASH_A/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop"),
    "легкая атлетика" to listOf("$UNSPLASH_A/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop"),
)

private fun tournamentImageUrlA(sportName: String?, imageUrl: String?, seed: Int = 0): String? {
    imageUrl?.takeIf { it.isNotEmpty() }?.let { return it }
    val list = SPORT_IMAGES_A[sportName?.lowercase()?.trim()] ?: return null
    return list[seed.mod(list.size)]
}

@Composable
fun AdminTournamentsScreen(user: User) {
    var subScreen by remember { mutableStateOf<String?>(null) }
    val detailViewModel: AthleteTournamentsViewModel = viewModel()
    LaunchedEffect(user.id) { detailViewModel.load(user.id) }

    when {
        subScreen == null -> TournamentsListContent(
            onEditTournament = { subScreen = "edit:$it" },
            onTournamentClick = { subScreen = "detail:$it" }
        )
        subScreen?.startsWith("detail:") == true -> {
            val id = subScreen?.removePrefix("detail:") ?: return
            AthleteTournamentDetailScreen(
                tournamentId = id,
                user = user,
                viewModel = detailViewModel,
                onBack = { subScreen = null }
            )
        }
        subScreen?.startsWith("edit:") == true -> {
            val id = subScreen?.removePrefix("edit:") ?: return
            AdminTournamentEditScreen(tournamentId = id, onBack = { subScreen = null })
        }
    }
}

@Composable
private fun TournamentsListContent(onEditTournament: (String) -> Unit, onTournamentClick: (String) -> Unit = {}) {
    val viewModel: AdminTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = s.message, onRetry = { viewModel.load() })
        is UiState.Success -> DarkPullRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        ) {
            TournamentsSuccessContent(
                tournaments = s.data,
                onEditTournament = onEditTournament,
                onDeleteTournament = { viewModel.deleteTournament(it) },
                onTournamentClick = onTournamentClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TournamentsSuccessContent(
    tournaments: List<TournamentWithCountsDto>,
    onEditTournament: (String) -> Unit,
    onDeleteTournament: (String) -> Unit,
    onTournamentClick: (String) -> Unit = {}
) {
    var searchTerm by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("all") }
    var selectedSport by remember { mutableStateOf("all") }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val statuses = listOf(
        "all" to "Все", "draft" to "Черновик", "registration_open" to "Регистрация",
        "in_progress" to "Идёт", "completed" to "Завершён", "cancelled" to "Отменён"
    )
    val sportFilters = remember(tournaments) {
        listOf("all" to "Все виды") +
                tournaments.mapNotNull { it.sportName }.distinct().sorted().map { it to it }
    }

    val regOpenCount = tournaments.count { it.status == "registration_open" }
    val inProgressCount = tournaments.count { it.status == "in_progress" }
    val completedCount = tournaments.count { it.status == "completed" }

    val filteredTournaments = tournaments.filter { t ->
        val matchSearch = searchTerm.isEmpty() || t.name.contains(searchTerm, ignoreCase = true)
        val matchStatus = selectedStatus == "all" || t.status == selectedStatus
        val matchSport = selectedSport == "all" || t.sportName == selectedSport
        matchSearch && matchStatus && matchSport
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Турниры", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = TextPrimary, letterSpacing = (-0.3).sp)
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FadeIn(visible = started, delayMs = 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("Всего", "${tournaments.size}", modifier = Modifier.weight(1f))
                    MiniStat("Регистрация", "$regOpenCount", modifier = Modifier.weight(1f))
                    MiniStat("Активных", "$inProgressCount", modifier = Modifier.weight(1f))
                    MiniStat("Заверш.", "$completedCount", modifier = Modifier.weight(1f))
                }
            }

            FadeIn(visible = started, delayMs = 150) {
                val activeFilters = (if (selectedStatus != "all") 1 else 0) + (if (selectedSport != "all") 1 else 0)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        DarkSearchField(value = searchTerm, onValueChange = { searchTerm = it }, placeholder = "Поиск по названию")
                    }
                    Surface(
                        onClick = { showFilterSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        color = CardBg
                    ) {
                        Box(
                            Modifier
                                .border(0.5.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
                    containerColor = CardBg,
                    dragHandle = {
                        Box(Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                            Box(Modifier.width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(CardBorder))
                        }
                    }
                ) {
                    Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
                        Text("Фильтры", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(Modifier.height(20.dp))

                        Text("Статус", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        val statusIndex = statuses.indexOfFirst { it.first == selectedStatus }.coerceAtLeast(0)
                        DarkSegmentedControl(statuses.map { it.second }, statusIndex, onSelect = { selectedStatus = statuses[it].first })

                        Spacer(Modifier.height(16.dp))

                        Text("Вид спорта", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                            sportFilters.forEachIndexed { index, (key, label) ->
                                DarkFilterChip(label, selectedSport == key, onClick = { selectedSport = key })
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { selectedStatus = "all"; selectedSport = "all" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder(true).copy(brush = SolidColor(CardBorder))
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

            FadeIn(visible = started, delayMs = 300) {
                Text("Показано ${filteredTournaments.size} из ${tournaments.size}",
                    fontSize = 13.sp, color = TextMuted)
            }

            FadeIn(visible = started, delayMs = 600) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredTournaments.forEachIndexed { index, t ->
                        TournamentCard(tournament = t, seed = index, onClick = { onTournamentClick(t.id) }, onEdit = { onEditTournament(t.id) }, onDelete = { showDeleteDialog = t.id })
                    }

                    if (filteredTournaments.isEmpty()) {
                        EmptyState("Турниры не найдены")
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { tournamentId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = CardBg,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Удалить турнир?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTournament(tournamentId)
                    showDeleteDialog = null
                }) { Text("Удалить", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Отмена", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun TournamentCard(
    tournament: TournamentWithCountsDto,
    seed: Int = 0,
    onClick: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardBg = DarkTheme.CardBg
    val cardImage = tournamentImageUrlA(tournament.sportName, tournament.imageUrl, seed)
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
                        AdminMockData.statusLabel(tournament.status ?: "draft"),
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(15.dp), textSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${formatShortDate(tournament.startDate)} — ${formatShortDate(tournament.endDate)}",
                        fontSize = 13.sp, color = textSecondary
                    )
                }
                if (!tournament.locationName.isNullOrEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(15.dp), textSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(tournament.locationName, fontSize = 13.sp, color = textSecondary)
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

            if (!tournament.organizerName.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Орг: ${tournament.organizerName}", fontSize = 11.sp,
                    color = if (hasImage) Color.White.copy(alpha = 0.6f) else TextMuted,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Редактировать", fontSize = 13.sp)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = if (hasImage) Color.White.copy(alpha = 0.7f) else TextMuted)
                }
            }
        }
    }
}
