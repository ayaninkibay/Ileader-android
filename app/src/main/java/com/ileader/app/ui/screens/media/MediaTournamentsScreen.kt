package com.ileader.app.ui.screens.media

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.MediaTournamentItem
import com.ileader.app.ui.viewmodels.MediaTournamentsData
import com.ileader.app.ui.viewmodels.MediaTournamentsViewModel

@Composable
fun MediaTournamentsScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    val viewModel: MediaTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> TournamentsContent(user, s.data, viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TournamentsContent(
    user: User,
    data: MediaTournamentsData,
    viewModel: MediaTournamentsViewModel
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
                    filteredTournaments.forEach { item ->
                        TournamentCard(
                            item = item,
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
    onRegister: (String) -> Unit,
    onUnregister: (String) -> Unit
) {
    val t = item.tournament
    val isActive = isTournamentActive(t.status)
    val isAccredited = item.accreditationStatus == "accepted"
    val hasPendingRequest = item.accreditationStatus == "pending"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DarkTheme.CardBg,
        border = DarkTheme.cardBorderStroke
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(t.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                        color = DarkTheme.TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(t.sportName ?: "", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatusBadge(tournamentStatusLabel(t.status),
                        if (isActive) DarkTheme.Accent else DarkTheme.TextMuted)
                    if (isAccredited) {
                        StatusBadge("Аккредитован", DarkTheme.Accent)
                    } else if (hasPendingRequest) {
                        StatusBadge("Ожидает", DarkTheme.TextMuted)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.DateRange, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                    Text(formatShortDate(t.startDate), fontSize = 12.sp, color = DarkTheme.TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                    Text(t.locationName ?: "", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.People, null, Modifier.size(14.dp), DarkTheme.TextSecondary)
                    Text("${t.participantCount} участников", fontSize = 12.sp, color = DarkTheme.TextSecondary)
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
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.TextSecondary),
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
