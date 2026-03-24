package com.ileader.app.ui.screens.athlete

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
import androidx.compose.ui.graphics.vector.ImageVector
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
    var selectedTournamentId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    // Детали результатов турнира
    selectedTournamentId?.let { id ->
        com.ileader.app.ui.screens.viewer.ViewerTournamentResultsScreen(
            tournamentId = id,
            user = user,
            onBack = { selectedTournamentId = null }
        )
        return
    }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> ResultsContent(user, s.data.results, s.data.sports, onResultClick = { selectedTournamentId = it })
    }
}

@Composable
private fun ResultsContent(
    user: User,
    allResults: List<TournamentResult>,
    sports: List<Pair<String, String>>,
    onResultClick: (String) -> Unit
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
                        "Привет, ${user.displayName.split(" ").firstOrNull() ?: user.displayName}",
                        fontSize = 14.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.Normal
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Результаты", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.8).sp
                    )
                }
                UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
            }
            }

            Spacer(Modifier.height(20.dp))

            // ── STATS 4 в ряд ──
            FadeIn(visible = started, delayMs = 150) {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                ResultStatCard(Modifier.weight(1f), Icons.Default.EmojiEvents, totalParticipations.toString(), "Турниров")
                ResultStatCard(Modifier.weight(1f), Icons.Default.Star, firstPlaces.toString(), "1-е место")
                ResultStatCard(Modifier.weight(1f), Icons.Default.WorkspacePremium, podiums.toString(), "Подиумы")
                ResultStatCard(Modifier.weight(1f), Icons.Default.Bolt, totalPoints.toString(), "Очки")
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
                    ResultCard(result, onClick = { onResultClick(result.tournamentId) })
                    Spacer(Modifier.height(10.dp))
                }
            }
            }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ResultStatCard(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    val accent = DarkTheme.Accent
    val accentSoft = DarkTheme.AccentSoft
    val cardBg = DarkTheme.CardBg
    val cardBorder = DarkTheme.CardBorder
    val textPrimary = DarkTheme.TextPrimary
    val textMuted = DarkTheme.TextMuted

    Surface(modifier.height(80.dp), RoundedCornerShape(16.dp), cardBg) {
        Column(
            Modifier
                .border(0.5.dp, cardBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(accentSoft),
                Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(15.dp), accent)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = textPrimary, letterSpacing = (-0.3).sp)
            Text(label, fontSize = 9.sp, color = textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ResultCard(result: TournamentResult, onClick: () -> Unit = {}) {
    val isTop = result.position <= 3
    val medalColor = when (result.position) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> DarkTheme.CardBorder
    }
    val cardBg = DarkTheme.CardBg
    val cardBorder = DarkTheme.CardBorder
    val textPrimary = DarkTheme.TextPrimary
    val textMuted = DarkTheme.TextMuted
    val accent = DarkTheme.Accent
    val accentSoft = DarkTheme.AccentSoft

    Surface(Modifier.fillMaxWidth().clickable { onClick() }, RoundedCornerShape(20.dp), cardBg) {
        Row(
            Modifier
                .border(0.5.dp, cardBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Позиция — квадрат с medalColor фоном
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isTop) medalColor.copy(alpha = 0.15f)
                        else cardBorder.copy(alpha = 0.3f)
                    ),
                Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "#${result.position}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isTop) medalColor else textMuted
                    )
                    Text("место", fontSize = 9.sp, color = textMuted)
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    result.tournamentName,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(12.dp), textMuted)
                    Text(result.date, fontSize = 11.sp, color = textMuted)
                    Icon(sportIcon(result.sportName), null, Modifier.size(11.dp), textMuted)
                    Text(result.sportName, fontSize = 11.sp, color = textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(Modifier.width(10.dp))

            // Очки — акцентный badge
            Surface(shape = RoundedCornerShape(12.dp), color = accentSoft) {
                Text(
                    "+${result.points}",
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = accent
                )
            }

            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.ChevronRight, null, Modifier.size(20.dp), textMuted)
        }
    }
}
