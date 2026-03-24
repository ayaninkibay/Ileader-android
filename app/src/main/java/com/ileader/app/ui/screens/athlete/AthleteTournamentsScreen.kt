package com.ileader.app.ui.screens.athlete

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AthleteTournamentsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

private val BASE_T = "https://ileader.kz/img"

private val UNSPLASH = "https://images.unsplash.com"

private val SPORT_IMAGES_T = mapOf(
    "картинг" to listOf(
        "$BASE_T/karting/karting-04-1280x853.jpeg",
        "$BASE_T/karting/karting-07-1280x853.jpeg",
        "$BASE_T/karting/karting-13-1280x853.jpeg",
        "$BASE_T/karting/karting-15-1280x853.jpeg",
        "$BASE_T/karting/karting-16-1280x853.jpeg",
        "$BASE_T/karting/karting-18-1280x852.jpeg",
        "$BASE_T/karting/karting-20-1280x853.jpeg",
    ),
    "стрельба" to listOf(
        "$BASE_T/shooting/shooting-02-1280x853.jpeg",
        "$BASE_T/shooting/shooting-04-1280x853.jpeg",
        "$BASE_T/shooting/shooting-06-1280x853.jpeg",
        "$BASE_T/shooting/shooting-07-1280x853.jpeg",
        "$BASE_T/shooting/shooting-08-1280x853.jpeg",
        "$BASE_T/shooting/shooting-09-1280x853.jpeg",
        "$BASE_T/shooting/shooting-11-1280x853.jpeg",
    ),
    "лёгкая атлетика" to listOf(
        "$UNSPLASH/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1552674605-db6ffd4facb5?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1571008887538-b36bb32f4571?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1530549387789-4c1017266635?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1541534741688-6078c6bfb5c5?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1476480862126-209bfaa8edc8?w=1280&q=80&fit=crop",
    ),
    "легкая атлетика" to listOf(
        "$UNSPLASH/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1552674605-db6ffd4facb5?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1571008887538-b36bb32f4571?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1530549387789-4c1017266635?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1541534741688-6078c6bfb5c5?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1476480862126-209bfaa8edc8?w=1280&q=80&fit=crop",
    ),
)

private fun tournamentCardImage(tournament: Tournament, seed: Int = 0): String? {
    // Сначала пробуем фото по виду спорта (они правильные)
    val list = SPORT_IMAGES_T[tournament.sportName.lowercase().trim()]
    if (list != null) return list[seed.mod(list.size)]
    // Фолбэк — imageUrl из БД (для спортов без наших фото)
    return tournament.imageUrl.takeIf { !it.isNullOrEmpty() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthleteTournamentsScreen(user: User) {
    val viewModel: AthleteTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    var selectedTournamentId by remember { mutableStateOf<String?>(null) }
    var qrTournamentId by remember { mutableStateOf<String?>(null) }
    var qrTournamentName by remember { mutableStateOf("") }
    var qrCheckedIn by remember { mutableStateOf(false) }

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    // QR-билет поверх всего
    qrTournamentId?.let { tid ->
        AthleteQrTicketScreen(
            user = user,
            tournamentId = tid,
            tournamentName = qrTournamentName,
            isCheckedIn = qrCheckedIn,
            onBack = { qrTournamentId = null }
        )
        return
    }

    selectedTournamentId?.let { id ->
        AthleteTournamentDetailScreen(
            tournamentId = id,
            user = user,
            viewModel = viewModel,
            onBack = { selectedTournamentId = null },
            onShowQrTicket = { name, checkedIn ->
                qrTournamentId = id
                qrTournamentName = name
                qrCheckedIn = checkedIn
            }
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFilterSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
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
                UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
            }
            }

            Spacer(Modifier.height(20.dp))

            // ── STATS 3 в ряд ──
            FadeIn(visible = started, delayMs = 150) {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                TournamentStatCard(Modifier.weight(1f), Icons.Default.EmojiEvents, myCount.toString(), "Мои")
                TournamentStatCard(Modifier.weight(1f), Icons.Default.LockOpen, openCount.toString(), "Открыты")
                TournamentStatCard(Modifier.weight(1f), Icons.Default.PlayArrow, activeCount.toString(), "Активные")
            }
            }

            Spacer(Modifier.height(16.dp))

            // ── SEARCH + FILTER ──
            FadeIn(visible = started, delayMs = 300) {
            val activeFilters = (if (selectedStatus != 0) 1 else 0) + (if (selectedSport != 0) 1 else 0)
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
                                    Text(
                                        "$activeFilters",
                                        fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White
                                    )
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
                        DarkSegmentedControl(statusFilters, selectedStatus, { selectedStatus = it })

                        Spacer(Modifier.height(16.dp))

                        Text("Вид спорта", fontSize = 13.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                            sportFilters.forEachIndexed { index, label ->
                                DarkFilterChip(label, selectedSport == index, { selectedSport = index })
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { selectedStatus = 0; selectedSport = 0 },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder(true).copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(DarkTheme.CardBorder)
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
            FadeIn(visible = started, delayMs = 600) {
            Column {
            if (filteredTournaments.isEmpty()) {
                EmptyState("Турниры не найдены", "Попробуйте изменить фильтры")
            } else {
                filteredTournaments.forEachIndexed { i, tournament ->
                    TournamentListCard(tournament, seed = i, onClick = { onTournamentClick(tournament.id) })
                    Spacer(Modifier.height(10.dp))
                }
            }
            }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Stat card для экрана ──
@Composable
private fun TournamentStatCard(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    val accent = DarkTheme.Accent
    val accentSoft = DarkTheme.AccentSoft
    val cardBg = DarkTheme.CardBg
    val textPrimary = DarkTheme.TextPrimary
    val textSecondary = DarkTheme.TextSecondary

    Surface(modifier.height(80.dp), RoundedCornerShape(16.dp), cardBg) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(accentSoft),
                Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(16.dp), accent)
            }
            Spacer(Modifier.height(5.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = textPrimary, letterSpacing = (-0.3).sp)
            Text(label, fontSize = 12.sp, color = textSecondary)
        }
    }
}

@Composable
private fun TournamentListCard(tournament: Tournament, seed: Int = 0, onClick: () -> Unit) {
    val cardBg = DarkTheme.CardBg
    val cardImage = tournamentCardImage(tournament, seed)
    val hasImage = cardImage != null
    val textPrimary = if (hasImage) Color.White else DarkTheme.TextPrimary
    val textSecondary = if (hasImage) Color.White.copy(alpha = 0.75f) else DarkTheme.TextSecondary
    val accent = DarkTheme.Accent

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        // Фото турнира (своё или по виду спорта) или тёмный фолбэк
        if (cardImage != null) {
            AsyncImage(
                model = cardImage,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
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
                    // Вид спорта badge
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
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Text(
                        tournament.status.displayName,
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Дата и локация
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
                    Text(
                        "${tournament.currentParticipants}/${tournament.maxParticipants}",
                        fontSize = 13.sp, color = textSecondary
                    )
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
                    Text(
                        "Призовой фонд: ${tournament.prize}",
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimary
                    )
                }
            }
        }
    }
}
