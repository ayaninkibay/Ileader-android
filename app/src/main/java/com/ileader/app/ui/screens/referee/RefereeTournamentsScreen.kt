package com.ileader.app.ui.screens.referee

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.RefereeTournamentsViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

private val BASE_URL_R = "https://ileader.kz/img"
private val UNSPLASH_R = "https://images.unsplash.com"
private val SPORT_IMAGES_R = mapOf(
    "картинг" to listOf("$BASE_URL_R/karting/karting-04-1280x853.jpeg", "$BASE_URL_R/karting/karting-07-1280x853.jpeg", "$BASE_URL_R/karting/karting-13-1280x853.jpeg"),
    "стрельба" to listOf("$BASE_URL_R/shooting/shooting-02-1280x853.jpeg", "$BASE_URL_R/shooting/shooting-04-1280x853.jpeg"),
    "теннис" to listOf("$UNSPLASH_R/photo-1554068865-24cecd4e34b8?w=1280&q=80&fit=crop"),
    "футбол" to listOf("$UNSPLASH_R/photo-1431324155629-1a6deb1dec8d?w=1280&q=80&fit=crop"),
    "бокс" to listOf("$UNSPLASH_R/photo-1549719386-74dfcbf7dbed?w=1280&q=80&fit=crop"),
    "плавание" to listOf("$UNSPLASH_R/photo-1519315901367-f34ff9154487?w=1280&q=80&fit=crop"),
    "лёгкая атлетика" to listOf("$UNSPLASH_R/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop"),
    "легкая атлетика" to listOf("$UNSPLASH_R/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop"),
)

private fun refereeTournamentImageUrl(sportName: String?, seed: Int = 0): String? {
    val list = SPORT_IMAGES_R[sportName?.lowercase()?.trim()] ?: return null
    return list[seed.mod(list.size)]
}

@Composable
fun RefereeTournamentsScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    // Internal navigation state
    var subScreen by remember { mutableStateOf<String?>(null) }

    when {
        subScreen?.startsWith("detail:") == true -> {
            val tournamentId = subScreen?.removePrefix("detail:") ?: return
            RefereeTournamentDetailScreen(
                user = user,
                tournamentId = tournamentId,
                onBack = { subScreen = null },
                onNavigateToResults = { subScreen = "results:$tournamentId" }
            )
            return
        }
        subScreen?.startsWith("results:") == true -> {
            val tournamentId = subScreen?.removePrefix("results:") ?: return
            RefereeTournamentResultsScreen(
                user = user,
                tournamentId = tournamentId,
                onBack = { subScreen = "detail:$tournamentId" }
            )
            return
        }
    }

    val viewModel: RefereeTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> DarkPullRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh(user.id) }
        ) {
            TournamentsListContent(s.data.all) { subScreen = "detail:$it" }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TournamentsListContent(
    allTournaments: List<RefereeTournament>,
    onTournamentClick: (String) -> Unit
) {
    var selectedFilter by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("Все", "Активные", "Предстоящие", "Завершённые")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFilterSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filtered = allTournaments.filter { t ->
        val matchesFilter = when (selectedFilter) {
            1 -> t.status == TournamentStatus.IN_PROGRESS
            2 -> t.status in listOf(TournamentStatus.REGISTRATION_OPEN, TournamentStatus.REGISTRATION_CLOSED, TournamentStatus.CHECK_IN)
            3 -> t.status == TournamentStatus.COMPLETED
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() || t.name.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val accentColor = Accent
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(accentColor.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.03f),
                    radius = 280.dp.toPx()
                ),
                radius = 280.dp.toPx(),
                center = Offset(size.width * 0.85f, size.height * 0.03f)
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 0) {
                Text("Мои турниры", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(4.dp))
                Text("Назначенные и прошедшие", fontSize = 14.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(28.dp))

            // ── SEARCH + FILTER ──
            FadeIn(visible, 200) {
                val activeFilters = if (selectedFilter != 0) 1 else 0
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        DarkSearchField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Поиск турниров..."
                        )
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
                        DarkSegmentedControl(filters, selectedFilter, { selectedFilter = it })

                        Spacer(Modifier.height(24.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { selectedFilter = 0 },
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

            Spacer(Modifier.height(4.dp))
            Text("Найдено: ${filtered.size}", fontSize = 12.sp, color = TextMuted)

            Spacer(Modifier.height(16.dp))

            // ── LIST ──
            FadeIn(visible, 400) {
                if (filtered.isEmpty()) {
                    EmptyState("Турниры не найдены")
                } else {
                    filtered.forEachIndexed { index, tournament ->
                        TournamentListCard(tournament, seed = index) { onTournamentClick(tournament.id) }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TournamentListCard(tournament: RefereeTournament, seed: Int = 0, onClick: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }
    val cardBg = DarkTheme.CardBg
    val cardImage = refereeTournamentImageUrl(tournament.sport, seed)
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
                        maxLines = 2
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(sportIcon(tournament.sport), null, Modifier.size(13.dp), textSecondary)
                        Text(tournament.sport, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textSecondary)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(
                        tournament.status.displayName,
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(8.dp), color = if (hasImage) Color.White.copy(alpha = 0.15f) else AccentSoft) {
                    Text(tournament.refereeRole.label, Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (hasImage) Color.White else Accent)
                }
            }

            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(15.dp), textSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text(tournament.date, fontSize = 13.sp, color = textSecondary)
                }
                if (tournament.location.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(15.dp), textSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(tournament.location, fontSize = 13.sp, color = textSecondary)
                    }
                }
            }

            // Progress
            if (tournament.matchesTotal > 0 && tournament.status != TournamentStatus.REGISTRATION_OPEN) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Матчи: ", fontSize = 12.sp, color = textSecondary)
                    Text("${tournament.matchesCompleted}/${tournament.matchesTotal}", fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold, color = textPrimary)
                }
                Spacer(Modifier.height(6.dp))
                val progress = if (tournament.matchesTotal > 0) tournament.matchesCompleted.toFloat() / tournament.matchesTotal else 0f
                DarkProgressBar(progress)
            }

            // Expand
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                shape = RoundedCornerShape(8.dp),
                color = if (hasImage) Color.White.copy(alpha = 0.12f) else CardBorder.copy(alpha = 0.3f)
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), Arrangement.Center, Alignment.CenterVertically) {
                    Text(if (expanded) "Свернуть" else "Подробнее",
                        fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textSecondary)
                    Spacer(Modifier.width(4.dp))
                    Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        null, Modifier.size(18.dp), textSecondary)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, null, Modifier.size(14.dp), textSecondary)
                            Spacer(Modifier.width(4.dp))
                            Text("${tournament.participants} участников", fontSize = 12.sp, color = textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, Modifier.size(14.dp), textSecondary)
                            Spacer(Modifier.width(4.dp))
                            Text(tournament.date, fontSize = 12.sp, color = textSecondary)
                        }
                    }
                }
            }
        }
    }
}
