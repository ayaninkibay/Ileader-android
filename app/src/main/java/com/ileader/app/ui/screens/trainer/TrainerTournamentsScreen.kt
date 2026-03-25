package com.ileader.app.ui.screens.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.Tournament
import com.ileader.app.data.models.TournamentStatus
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.screens.athlete.AthleteTournamentDetailScreen
import com.ileader.app.ui.viewmodels.AthleteTournamentsViewModel
import com.ileader.app.ui.viewmodels.TrainerTournamentsViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke

private val BASE_TT = "https://ileader.kz/img"
private val UNSPLASH_TT = "https://images.unsplash.com"

private val SPORT_IMAGES_TT = mapOf(
    "картинг" to listOf(
        "$BASE_TT/karting/karting-04-1280x853.jpeg",
        "$BASE_TT/karting/karting-07-1280x853.jpeg",
        "$BASE_TT/karting/karting-13-1280x853.jpeg",
        "$BASE_TT/karting/karting-15-1280x853.jpeg",
        "$BASE_TT/karting/karting-16-1280x853.jpeg",
    ),
    "стрельба" to listOf(
        "$BASE_TT/shooting/shooting-02-1280x853.jpeg",
        "$BASE_TT/shooting/shooting-04-1280x853.jpeg",
        "$BASE_TT/shooting/shooting-06-1280x853.jpeg",
        "$BASE_TT/shooting/shooting-07-1280x853.jpeg",
    ),
    "лёгкая атлетика" to listOf(
        "$UNSPLASH_TT/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop",
        "$UNSPLASH_TT/photo-1552674605-db6ffd4facb5?w=1280&q=80&fit=crop",
    ),
    "легкая атлетика" to listOf(
        "$UNSPLASH_TT/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop",
        "$UNSPLASH_TT/photo-1552674605-db6ffd4facb5?w=1280&q=80&fit=crop",
    ),
)

private fun ttCardImage(tournament: Tournament, seed: Int = 0): String? {
    val list = SPORT_IMAGES_TT[tournament.sportName.lowercase().trim()]
    if (list != null) return list[seed.mod(list.size)]
    return tournament.imageUrl.takeIf { !it.isNullOrEmpty() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerTournamentsScreen(user: User) {
    val viewModel: TrainerTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    var selectedTournamentId by remember { mutableStateOf<String?>(null) }
    var registerTournamentId by remember { mutableStateOf<String?>(null) }
    val detailViewModel: AthleteTournamentsViewModel = viewModel()

    LaunchedEffect(user.id) { detailViewModel.load(user.id) }

    selectedTournamentId?.let { id ->
        AthleteTournamentDetailScreen(
            tournamentId = id,
            user = user,
            viewModel = detailViewModel,
            onBack = { selectedTournamentId = null },
            onRegisterTeam = { tournamentId ->
                registerTournamentId = tournamentId
                selectedTournamentId = null
            }
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
            var searchQuery by remember { mutableStateOf("") }
            var selectedFilter by remember { mutableIntStateOf(0) }
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

            var showRegisterDialog by remember { mutableStateOf<Tournament?>(null) }

            // Handle registration from detail screen
            LaunchedEffect(registerTournamentId) {
                registerTournamentId?.let { tId ->
                    val tournament = data.tournaments.find { it.id == tId }
                    if (tournament != null) showRegisterDialog = tournament
                    registerTournamentId = null
                }
            }

            var showFilterSheet by remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val scope = rememberCoroutineScope()

            val statusFilters = listOf("Все", "Доступные", "Зарег.", "Завершённые")

            var started by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { started = true }

            DarkPullRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh(user.id) }
            ) {
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
                            Text(
                                "Поиск турниров",
                                fontSize = 14.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.Normal
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Турниры", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                                color = DarkTheme.TextPrimary, letterSpacing = (-0.8).sp
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── TEAM SELECTOR ──
                    if (teams.size > 1) {
                        FadeIn(visible = started, delayMs = 100) {
                            var expanded by remember { mutableStateOf(false) }
                            DarkCard {
                                Row(
                                    Modifier.clickable { expanded = true }.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AccentIconBox(Icons.Default.Groups)
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
                                            text = { Text("${team.name} (${team.sportName})", fontSize = 14.sp) },
                                            onClick = { selectedTeamIndex = index; expanded = false; selectedFilter = 0 },
                                            leadingIcon = { if (index == selectedTeamIndex) Icon(Icons.Default.Check, null, tint = DarkTheme.Accent) }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // ── STATS ──
                    FadeIn(visible = started, delayMs = 150) {
                        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                            TournamentStatCard(Modifier.weight(1f), Icons.Default.EmojiEvents, totalT.toString(), "Всего", selected = selectedFilter == 0) {
                                selectedFilter = 0
                            }
                            TournamentStatCard(Modifier.weight(1f), Icons.Default.AppRegistration, registeredCount.toString(), "Зарег.", selected = selectedFilter == 2) {
                                selectedFilter = if (selectedFilter == 2) 0 else 2
                            }
                            TournamentStatCard(Modifier.weight(1f), Icons.Default.LockOpen, availableCount.toString(), "Доступно", selected = selectedFilter == 1) {
                                selectedFilter = if (selectedFilter == 1) 0 else 1
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── SEARCH + FILTER ──
                    FadeIn(visible = started, delayMs = 300) {
                        val activeFilters = (if (selectedFilter != 0) 1 else 0)
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.weight(1f)) {
                                DarkSearchField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = "Поиск турнира...")
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
                                    Icon(Icons.Default.Tune, "Фильтры", Modifier.size(20.dp), DarkTheme.Accent)
                                    if (activeFilters > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = DarkTheme.Accent,
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
                                Text("Фильтры", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary)
                                Spacer(Modifier.height(20.dp))

                                Text("Статус", fontSize = 13.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(8.dp))
                                DarkSegmentedControl(statusFilters, selectedFilter, { selectedFilter = it })

                                Spacer(Modifier.height(24.dp))

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedButton(
                                        onClick = { selectedFilter = 0 },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = ButtonDefaults.outlinedButtonBorder(true).copy(
                                            brush = SolidColor(DarkTheme.CardBorder)
                                        )
                                    ) { Text("Сбросить", color = DarkTheme.TextSecondary) }
                                    Button(
                                        onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { showFilterSheet = false } },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                                    ) { Text("Применить") }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── TOURNAMENT LIST ──
                    FadeIn(visible = started, delayMs = 450) {
                        if (filteredTournaments.isEmpty()) {
                            EmptyState("Турниров не найдено", "Попробуйте изменить фильтры", actionLabel = "Сбросить фильтры", onAction = { selectedFilter = 0; searchQuery = "" })
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                filteredTournaments.forEachIndexed { i, tournament ->
                                    val isRegistered = tournament.id in registeredIds
                                    TournamentListCard(
                                        tournament = tournament,
                                        seed = i,
                                        isRegistered = isRegistered,
                                        onClick = { selectedTournamentId = tournament.id },
                                        onRegister = { showRegisterDialog = tournament },
                                        onUnregister = { viewModel.unregisterTeam(tournament.id, selectedTeam.id, user.id) }
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
private fun TournamentStatCard(modifier: Modifier, icon: ImageVector, value: String, label: String, selected: Boolean = false, onClick: () -> Unit = {}) {
    val accent = DarkTheme.Accent
    val accentSoft = DarkTheme.AccentSoft
    val cardBg = DarkTheme.CardBg

    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) accentSoft else cardBg,
        border = if (selected) BorderStroke(1.5.dp, accent) else null
    ) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(if (selected) accent else accentSoft),
                Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(16.dp), if (selected) Color.White else accent)
            }
            Spacer(Modifier.height(5.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
            Text(label, fontSize = 12.sp, color = DarkTheme.TextSecondary)
        }
    }
}

@Composable
private fun TournamentListCard(tournament: Tournament, seed: Int = 0, isRegistered: Boolean, onClick: () -> Unit = {}, onRegister: () -> Unit, onUnregister: () -> Unit) {
    val cardBg = DarkTheme.CardBg
    val cardImage = ttCardImage(tournament, seed)
    val hasImage = cardImage != null
    val textPrimary = if (hasImage) Color.White else DarkTheme.TextPrimary
    val textSecondary = if (hasImage) Color.White.copy(alpha = 0.75f) else DarkTheme.TextSecondary
    val statusColor = tournamentStatusColor(tournament.status)

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(sportIcon(tournament.sportName), null, Modifier.size(13.dp), textSecondary)
                        Text(tournament.sportName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textSecondary)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        tournament.status.displayName,
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(15.dp), textSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text(formatShortDate(tournament.startDate), fontSize = 13.sp, color = textSecondary)
                }
                if (tournament.location.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(15.dp), textSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(tournament.location, fontSize = 13.sp, color = textSecondary)
                    }
                }
            }

            if (tournament.maxParticipants > 0) {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, null, Modifier.size(15.dp), textSecondary)
                    Spacer(Modifier.width(6.dp))
                    Text("${tournament.currentParticipants}/${tournament.maxParticipants}", fontSize = 13.sp, color = textSecondary)
                    Spacer(Modifier.width(8.dp))
                    DarkProgressBar(
                        tournament.currentParticipants.toFloat() / tournament.maxParticipants,
                        Modifier.weight(1f).height(4.dp)
                    )
                }
            }

            if (tournament.prize.isNotEmpty()) {
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
                    Text("Призовой фонд: ${tournament.prize}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                }
            }

            if (tournament.status == TournamentStatus.REGISTRATION_OPEN) {
                Spacer(Modifier.height(12.dp))
                if (isRegistered) {
                    OutlinedButton(
                        onClick = onUnregister,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (hasImage) Color.White else DarkTheme.Accent),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = Brush.linearGradient(listOf(
                                if (hasImage) Color.White.copy(alpha = 0.3f) else DarkTheme.Accent.copy(alpha = 0.3f),
                                if (hasImage) Color.White.copy(alpha = 0.3f) else DarkTheme.Accent.copy(alpha = 0.3f)
                            ))
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
