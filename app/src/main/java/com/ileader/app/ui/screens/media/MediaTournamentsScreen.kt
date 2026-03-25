package com.ileader.app.ui.screens.media

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.screens.athlete.AthleteTournamentDetailScreen
import com.ileader.app.ui.viewmodels.AthleteTournamentsViewModel
import com.ileader.app.ui.viewmodels.MediaTournamentItem
import com.ileader.app.ui.viewmodels.MediaTournamentsData
import com.ileader.app.ui.viewmodels.MediaTournamentsViewModel

private val BASE_URL_M = "https://ileader.kz/img"
private val UNSPLASH_M = "https://images.unsplash.com"
private val SPORT_IMAGES_M = mapOf(
    "картинг" to listOf("$BASE_URL_M/karting/karting-04-1280x853.jpeg", "$BASE_URL_M/karting/karting-07-1280x853.jpeg", "$BASE_URL_M/karting/karting-13-1280x853.jpeg"),
    "стрельба" to listOf("$BASE_URL_M/shooting/shooting-02-1280x853.jpeg", "$BASE_URL_M/shooting/shooting-04-1280x853.jpeg"),
    "теннис" to listOf("$UNSPLASH_M/photo-1554068865-24cecd4e34b8?w=1280&q=80&fit=crop"),
    "футбол" to listOf("$UNSPLASH_M/photo-1431324155629-1a6deb1dec8d?w=1280&q=80&fit=crop"),
    "бокс" to listOf("$UNSPLASH_M/photo-1549719386-74dfcbf7dbed?w=1280&q=80&fit=crop"),
    "плавание" to listOf("$UNSPLASH_M/photo-1519315901367-f34ff9154487?w=1280&q=80&fit=crop"),
    "лёгкая атлетика" to listOf("$UNSPLASH_M/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop"),
    "легкая атлетика" to listOf("$UNSPLASH_M/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop"),
)

private fun tournamentImageUrlM(sportName: String?, imageUrl: String?, seed: Int = 0): String? {
    imageUrl?.takeIf { it.isNotEmpty() }?.let { return it }
    val list = SPORT_IMAGES_M[sportName?.lowercase()?.trim()] ?: return null
    return list[seed.mod(list.size)]
}

@Composable
fun MediaTournamentsScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    val viewModel: MediaTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    var selectedTournamentId by remember { mutableStateOf<String?>(null) }
    val detailViewModel: AthleteTournamentsViewModel = viewModel()
    LaunchedEffect(user.id) { detailViewModel.load(user.id) }

    selectedTournamentId?.let { id ->
        AthleteTournamentDetailScreen(
            tournamentId = id,
            user = user,
            viewModel = detailViewModel,
            onBack = { selectedTournamentId = null }
        )
        return
    }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> DarkPullRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh(user.id) }
        ) {
            TournamentsContent(user, s.data, viewModel, onTournamentClick = { selectedTournamentId = it })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TournamentsContent(
    user: User,
    data: MediaTournamentsData,
    viewModel: MediaTournamentsViewModel,
    onTournamentClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) }
    val filters = listOf("Все", "Мои", "Регистрация", "Активные", "Завершённые")
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val filteredTournaments = data.tournaments.filter { item ->
        val t = item.tournament
        val matchesSearch = searchQuery.isEmpty() ||
                t.name.lowercase().contains(searchQuery.lowercase())
        val matchesFilter = when (selectedFilter) {
            1 -> item.accreditationStatus == "accepted"
            2 -> t.status == "registration_open"
            3 -> t.status == "in_progress"
            4 -> t.status == "completed"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

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
            FadeIn(visible, 0) {
                Column {
                    Text("Турниры", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(4.dp))
                    Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── SEARCH + FILTER ──
            FadeIn(visible, 150) {
                val activeFilters = if (selectedFilter != 0) 1 else 0
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        DarkSearchField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = "Поиск турниров...")
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
                        DarkSegmentedControl(filters, selectedFilter, onSelect = { selectedFilter = it })

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

            Spacer(Modifier.height(20.dp))

            // ── TOURNAMENT LIST ──
            FadeIn(visible, 450) {
                if (filteredTournaments.isEmpty()) {
                    EmptyState("Турниры не найдены")
                } else {
                    filteredTournaments.forEachIndexed { index, item ->
                        TournamentCard(
                            item = item,
                            seed = index,
                            onClick = { onTournamentClick(item.tournament.id) },
                            onRegister = { viewModel.requestAccreditation(it) },
                            onUnregister = { viewModel.cancelAccreditation(it) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TournamentCard(
    item: MediaTournamentItem,
    seed: Int = 0,
    onClick: () -> Unit = {},
    onRegister: (String) -> Unit,
    onUnregister: (String) -> Unit
) {
    val t = item.tournament
    val isActive = isTournamentActive(t.status)
    val isAccredited = item.accreditationStatus == "accepted"
    val hasPendingRequest = item.accreditationStatus == "pending"
    val cardBg = DarkTheme.CardBg
    val cardImage = tournamentImageUrlM(t.sportName, t.imageUrl, seed)
    val hasImage = cardImage != null
    val textPrimary = if (hasImage) Color.White else DarkTheme.TextPrimary
    val textSecondary = if (hasImage) Color.White.copy(alpha = 0.75f) else DarkTheme.TextSecondary
    val statusColor = tournamentStatusColor(t.status)

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
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        t.name,
                        fontSize = 17.sp, fontWeight = FontWeight.Bold, color = textPrimary,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    if (!t.sportName.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(sportIcon(t.sportName), null, Modifier.size(13.dp), textSecondary)
                            Text(t.sportName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textSecondary)
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.15f)) {
                        Text(
                            tournamentStatusLabel(t.status),
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor
                        )
                    }
                    if (isAccredited) {
                        Surface(shape = RoundedCornerShape(8.dp), color = DarkTheme.Accent.copy(alpha = 0.15f)) {
                            Text("Аккредитован", Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.Accent)
                        }
                    } else if (hasPendingRequest) {
                        Surface(shape = RoundedCornerShape(8.dp), color = DarkTheme.TextMuted.copy(alpha = 0.15f)) {
                            Text("Ожидает", Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (hasImage) Color.White.copy(alpha = 0.7f) else DarkTheme.TextMuted)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(15.dp), textSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text(formatShortDate(t.startDate), fontSize = 13.sp, color = textSecondary)
                }
                if (!t.locationName.isNullOrEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(15.dp), textSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(t.locationName, fontSize = 13.sp, color = textSecondary)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, null, Modifier.size(15.dp), textSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text("${t.participantCount} участников", fontSize = 13.sp, color = textSecondary)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (isAccredited) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Опубликовать", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    if (isActive) {
                        OutlinedButton(
                            onClick = { onUnregister(t.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (hasImage) Color.White.copy(alpha = 0.8f) else DarkTheme.TextSecondary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Отменить", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            } else if (!hasPendingRequest) {
                Button(
                    onClick = { onRegister(t.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Зарегистрироваться", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
