package com.ileader.app.ui.screens.viewer

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ViewerTournamentsViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerTournamentsScreen(
    user: User,
    onNavigateToDetail: (String) -> Unit = {}
) {
    val viewModel: ViewerTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load() }
        is UiState.Success -> {
            val data = s.data
            var searchQuery by remember { mutableStateOf("") }
            var statusFilter by remember { mutableIntStateOf(0) }
            var sportFilter by remember { mutableIntStateOf(0) }
            var visible by remember { mutableStateOf(false) }
            var showFilterSheet by remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) { visible = true }

            val statuses = listOf("all" to "Все", "registration_open" to "Регистрация", "in_progress" to "Идёт", "completed" to "Завершён")
            val sportFilters = remember(data.sports) {
                buildList {
                    add("all" to "Все виды")
                    data.sports.forEach { add(it.id to it.name) }
                }
            }

            val filteredTournaments = remember(searchQuery, statusFilter, sportFilter, data.tournaments) {
                val statusValue = statuses.getOrNull(statusFilter)?.first ?: "all"
                val sportValue = sportFilters.getOrNull(sportFilter)?.first ?: "all"
                data.tournaments.filter { t ->
                    (statusValue == "all" || t.status == statusValue) &&
                    (sportValue == "all" || t.sportId == sportValue) &&
                    (searchQuery.isBlank() || (t.name.contains(searchQuery, true)) || (t.region ?: "").contains(searchQuery, true))
                }
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
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Турниры", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Все соревнования в одном месте", fontSize = 14.sp, color = TextSecondary)
                        }
                        UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
                    }
                }

                Spacer(Modifier.height(20.dp))

                FadeIn(visible, 200) {
                    val activeFilters = (if (statusFilter != 0) 1 else 0) + (if (sportFilter != 0) 1 else 0)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.weight(1f)) {
                            DarkSearchField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = "Поиск по названию, региону...")
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
                            DarkSegmentedControl(statuses.map { it.second }, statusFilter, onSelect = { statusFilter = it })

                            Spacer(Modifier.height(16.dp))

                            Text("Вид спорта", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                                sportFilters.forEachIndexed { index, (_, label) ->
                                    DarkFilterChip(label, sportFilter == index, onClick = { sportFilter = index })
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { statusFilter = 0; sportFilter = 0 },
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

                Spacer(Modifier.height(12.dp))

                FadeIn(visible, 400) {
                    Column {
                        Text("Найдено: ${filteredTournaments.size} турниров", fontSize = 13.sp, color = TextMuted)

                        Spacer(Modifier.height(12.dp))

                        if (filteredTournaments.isEmpty()) {
                            DarkCard {
                                Column(
                                    Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(Modifier.size(52.dp).clip(CircleShape).background(CardBorder.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.SearchOff, null, tint = TextMuted, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text("Турниры не найдены", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Попробуйте изменить фильтры", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(16.dp))
                                    Button(
                                        onClick = { statusFilter = 0; sportFilter = 0; searchQuery = "" },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text("Сбросить фильтры", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                filteredTournaments.forEach { tournament ->
                                    TournamentListCard(tournament) { onNavigateToDetail(tournament.id) }
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

@Composable
private fun TournamentListCard(tournament: TournamentWithCountsDto, onClick: () -> Unit) {
    val status = tournament.status ?: ""
    val chipColor = when (status) {
        "registration_open" -> Color(0xFF22C55E)
        "in_progress" -> Color(0xFFF59E0B)
        else -> TextMuted
    }

    DarkCard(Modifier.clickable { onClick() }) {
        Column(Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(getStatusLabel(status), chipColor)
                Surface(shape = RoundedCornerShape(8.dp), color = CardBorder.copy(alpha = 0.5f)) {
                    Text(tournament.sportName ?: "", Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(tournament.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(formatShortDate(tournament.startDate), fontSize = 12.sp, color = TextSecondary)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("${tournament.region ?: ""} · ${tournament.locationName ?: ""}", fontSize = 12.sp, color = TextSecondary)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("${tournament.participantCount} / ${tournament.maxParticipants ?: 0} участников", fontSize = 12.sp, color = TextSecondary)
            }

            if (tournament.prize != null) {
                Spacer(Modifier.height(12.dp))
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = CardBorder.copy(alpha = 0.3f)) {
                    Row(
                        Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Призовой фонд: ", fontSize = 12.sp, color = TextSecondary)
                            Text(tournament.prize, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Подробнее", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
