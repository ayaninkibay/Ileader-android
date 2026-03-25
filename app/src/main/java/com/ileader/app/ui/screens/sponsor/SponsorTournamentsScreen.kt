package com.ileader.app.ui.screens.sponsor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SponsorshipDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.SponsorTournamentsViewModel

private val BASE_URL_S = "https://ileader.kz/img"
private val UNSPLASH_S = "https://images.unsplash.com"
private val SPORT_IMAGES_S = mapOf(
    "картинг" to listOf("$BASE_URL_S/karting/karting-04-1280x853.jpeg", "$BASE_URL_S/karting/karting-07-1280x853.jpeg", "$BASE_URL_S/karting/karting-13-1280x853.jpeg"),
    "стрельба" to listOf("$BASE_URL_S/shooting/shooting-02-1280x853.jpeg", "$BASE_URL_S/shooting/shooting-04-1280x853.jpeg"),
    "теннис" to listOf("$UNSPLASH_S/photo-1554068865-24cecd4e34b8?w=1280&q=80&fit=crop"),
    "футбол" to listOf("$UNSPLASH_S/photo-1431324155629-1a6deb1dec8d?w=1280&q=80&fit=crop"),
    "бокс" to listOf("$UNSPLASH_S/photo-1549719386-74dfcbf7dbed?w=1280&q=80&fit=crop"),
    "плавание" to listOf("$UNSPLASH_S/photo-1519315901367-f34ff9154487?w=1280&q=80&fit=crop"),
    "лёгкая атлетика" to listOf("$UNSPLASH_S/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop"),
    "легкая атлетика" to listOf("$UNSPLASH_S/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop"),
)

private fun tournamentImageUrlS(sportName: String?, imageUrl: String?, seed: Int = 0): String? {
    imageUrl?.takeIf { it.isNotEmpty() }?.let { return it }
    val list = SPORT_IMAGES_S[sportName?.lowercase()?.trim()] ?: return null
    return list[seed.mod(list.size)]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsorTournamentsScreen(user: User) {
    val viewModel: SponsorTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val appliedTournaments by viewModel.appliedTournaments.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTournamentId by remember { mutableStateOf<String?>(null) }
    val tabFilters = listOf("Открытые", "Мои")
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

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

            DarkPullRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh(user.id) }
            ) {
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

                    // ── SEARCH + FILTER ──
                    FadeIn(visible, 100) {
                        val activeFilters = if (selectedTab != 0) 1 else 0
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

                                Text("Категория", fontSize = 13.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(8.dp))
                                DarkSegmentedControl(tabFilters, selectedTab, onSelect = { selectedTab = it })

                                Spacer(Modifier.height(24.dp))

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedButton(
                                        onClick = { selectedTab = 0 },
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

                    FadeIn(visible, 300) {
                        when (selectedTab) {
                            0 -> {
                                if (openTournaments.isEmpty()) {
                                    EmptyState("Турниры не найдены", "Попробуйте изменить параметры поиска")
                                } else {
                                    openTournaments.forEachIndexed { index, tournament ->
                                        OpenTournamentItem(
                                            tournament = tournament,
                                            seed = index,
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
}

@Composable
private fun OpenTournamentItem(tournament: TournamentWithCountsDto, seed: Int = 0, applied: Boolean, onApply: () -> Unit, onClick: () -> Unit) {
    val cardBg = DarkTheme.CardBg
    val cardImage = tournamentImageUrlS(tournament.sportName, tournament.imageUrl, seed)
    val hasImage = cardImage != null
    val textPrimary = if (hasImage) Color.White else DarkTheme.TextPrimary
    val textSecondary = if (hasImage) Color.White.copy(alpha = 0.75f) else DarkTheme.TextSecondary
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
                        SponsorUtils.getStatusLabel(tournament.status ?: ""),
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
                    Text("${formatShortDate(tournament.startDate)} — ${formatShortDate(tournament.endDate)}", fontSize = 13.sp, color = textSecondary)
                }
                if (!tournament.locationName.isNullOrEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(15.dp), textSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(tournament.locationName, fontSize = 13.sp, color = textSecondary)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, null, Modifier.size(15.dp), textSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text("${tournament.participantCount} участников", fontSize = 13.sp, color = textSecondary)
                }
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
