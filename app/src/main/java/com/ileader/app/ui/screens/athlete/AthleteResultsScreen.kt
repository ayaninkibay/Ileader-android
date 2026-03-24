package com.ileader.app.ui.screens.athlete

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.TournamentResult
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AthleteResultsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AthleteResultsScreen(user: User) {
    val viewModel: AthleteResultsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> ResultsContent(user, s.data.results, s.data.sports)
    }
}

@Composable
private fun ResultsContent(
    user: User,
    allResults: List<TournamentResult>,
    sports: List<Pair<String, String>>
) {
    var selectedSport by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableIntStateOf(0) }
    var sortAscending by remember { mutableStateOf(false) }

    val sportFilters = listOf("Все виды") + sports.map { it.first }
    val sortOptions = listOf("По дате", "По месту", "По очкам")

    val filteredResults = allResults
        .filter { r ->
            val matchesSport = selectedSport == 0 || r.sportName == sportFilters[selectedSport]
            val matchesSearch = searchQuery.isEmpty() || r.tournamentName.lowercase().contains(searchQuery.lowercase())
            matchesSport && matchesSearch
        }
        .let { list ->
            when (sortBy) {
                1 -> if (sortAscending) list.sortedBy { it.position } else list.sortedByDescending { it.position }
                2 -> if (sortAscending) list.sortedBy { it.points } else list.sortedByDescending { it.points }
                else -> list
            }
        }

    val totalParticipations = allResults.size
    val firstPlaces = allResults.count { it.position == 1 }
    val podiums = allResults.count { it.position in 1..3 }
    val totalPoints = allResults.sumOf { it.points }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
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
                    Text("Результаты", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(4.dp))
                    Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                }
                UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
            }
            }

            Spacer(Modifier.height(20.dp))

            // ── STATS 2x2 ──
            FadeIn(visible = started, delayMs = 150) {
            Column {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                StatItem(Modifier.weight(1f), Icons.Default.EmojiEvents, totalParticipations.toString(), "Турниров")
                StatItem(Modifier.weight(1f), Icons.Default.Star, firstPlaces.toString(), "1-е место")
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                StatItem(Modifier.weight(1f), Icons.Default.WorkspacePremium, podiums.toString(), "Подиумы")
                StatItem(Modifier.weight(1f), Icons.Default.FitnessCenter, totalPoints.toString(), "Очки")
            }
            }
            }

            Spacer(Modifier.height(16.dp))

            // ── SEARCH ──
            FadeIn(visible = started, delayMs = 300) {
            DarkSearchField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = "Поиск по турниру...")
            }

            Spacer(Modifier.height(12.dp))

            // ── SPORT FILTERS + SORT ──
            FadeIn(visible = started, delayMs = 450) {
            Column {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                sportFilters.forEachIndexed { index, label ->
                    DarkFilterChip(label, selectedSport == index, { selectedSport = index })
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── SORT ──
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
                Icon(Icons.Default.Sort, null, Modifier.size(18.dp), DarkTheme.TextMuted)
                sortOptions.forEachIndexed { index, label ->
                    Surface(
                        Modifier.clickable { sortBy = index }, RoundedCornerShape(8.dp),
                        if (sortBy == index) DarkTheme.AccentSoft else Color.Transparent
                    ) {
                        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 12.sp,
                            fontWeight = if (sortBy == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (sortBy == index) DarkTheme.Accent else DarkTheme.TextSecondary)
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton({ sortAscending = !sortAscending }, Modifier.size(32.dp)) {
                    Icon(if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        "Порядок", Modifier.size(18.dp), DarkTheme.Accent)
                }
            }
            }
            }

            Spacer(Modifier.height(16.dp))

            // ── RESULTS LIST ──
            FadeIn(visible = started, delayMs = 600) {
            Column {
            if (filteredResults.isEmpty()) {
                EmptyState("Результаты не найдены", "Попробуйте изменить фильтры")
            } else {
                filteredResults.forEach { result ->
                    ResultCard(result)
                    Spacer(Modifier.height(8.dp))
                }
            }
            }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ResultCard(result: TournamentResult) {
    val isTop = result.position <= 3

    DarkCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).clip(CircleShape)
                    .background(if (isTop) DarkTheme.AccentSoft else DarkTheme.CardBorder.copy(alpha = 0.5f)),
                Alignment.Center
            ) {
                Text("#${result.position}", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = if (isTop) DarkTheme.Accent else DarkTheme.TextMuted)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(result.tournamentName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text("${result.date} · ${result.sportName}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = DarkTheme.AccentSoft) {
                Text("+${result.points}", Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkTheme.Accent)
            }
        }
    }
}
