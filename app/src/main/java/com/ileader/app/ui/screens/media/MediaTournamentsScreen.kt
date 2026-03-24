package com.ileader.app.ui.screens.media

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
private fun TournamentsContent(
    user: User,
    data: MediaTournamentsData,
    viewModel: MediaTournamentsViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) }
    val filters = listOf("Все", "Мои", "Регистрация", "Активные", "Завершённые")

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

            // ── SEARCH ──
            FadeIn(visible, 150) {
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DarkTheme.CardBg) {
                    Row(
                        Modifier.border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 14.sp, color = DarkTheme.TextPrimary),
                            singleLine = true,
                            cursorBrush = SolidColor(DarkTheme.Accent),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) Text("Поиск турниров...", fontSize = 14.sp, color = DarkTheme.TextMuted)
                                inner()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, null, Modifier.size(16.dp), DarkTheme.TextMuted)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── FILTERS ──
            FadeIn(visible, 300) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEachIndexed { index, filter ->
                        DarkFilterChip(filter, selectedFilter == index, { selectedFilter = index })
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
