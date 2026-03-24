package com.ileader.app.ui.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.bracket.BracketUtils
import com.ileader.app.data.models.BracketMatch
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.*
import com.ileader.app.ui.components.bracket.BracketView
import com.ileader.app.ui.components.bracket.MatchDetailDialog
import com.ileader.app.ui.viewmodels.ViewerTournamentDetailViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ViewerTournamentDetailScreen(
    tournamentId: String,
    user: User,
    onBack: () -> Unit = {},
    onNavigateToResults: (String) -> Unit = {}
) {
    val viewModel: ViewerTournamentDetailViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(tournamentId) { viewModel.load(tournamentId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(tournamentId) }
        is UiState.Success -> {
            val data = s.data
            val tournament = data.tournament
            val status = tournament.status ?: ""
            val isCompleted = status == "completed"
            var isFavorite by remember { mutableStateOf(false) }
            var started by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { started = true }
            val sportName = tournament.sports?.name ?: ""
            val locationName = tournament.locations?.name ?: ""
            val locationCity = tournament.locations?.city ?: tournament.region ?: ""
            val organizerName = tournament.profiles?.name ?: ""

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Bg)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero header
                FadeIn(visible = started, delayMs = 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Accent, AccentDark)))
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                            }
                            Row {
                                IconButton(onClick = { isFavorite = !isFavorite }) {
                                    Icon(
                                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        "Избранное",
                                        tint = if (isFavorite) Color.White else Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                IconButton(onClick = { }) {
                                    Icon(Icons.Default.Share, "Поделиться", tint = Color.White.copy(alpha = 0.7f))
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val isActive = status == "registration_open" || status == "in_progress"
                            Surface(shape = RoundedCornerShape(8.dp), color = if (isActive) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f)) {
                                Text(getStatusLabel(status), Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                            if (sportName.isNotBlank()) {
                                Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.15f)) {
                                    Text(sportName, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(tournament.name, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White, lineHeight = 30.sp, letterSpacing = (-0.5).sp)
                        if (organizerName.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(organizerName, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
                }

                Spacer(Modifier.height(20.dp))

                // Quick info cards
                FadeIn(visible = started, delayMs = 150) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickInfoCard(Icons.Default.CalendarMonth, "Дата", formatDateShort(tournament.startDate ?: ""), Modifier.weight(1f))
                    QuickInfoCard(Icons.Default.LocationOn, "Место", locationCity, Modifier.weight(1f))
                    QuickInfoCard(Icons.Default.People, "Участники", "${data.participants.size}/${tournament.maxParticipants ?: 0}", Modifier.weight(1f))
                    QuickInfoCard(Icons.Default.EmojiEvents, "Призы", tournament.prize ?: "—", Modifier.weight(1f))
                }
                }

                Spacer(Modifier.height(20.dp))

                // Results banner
                FadeIn(visible = started, delayMs = 300) {
                Column {
                if (isCompleted && data.results.isNotEmpty()) {
                    DarkCard(
                        modifier = Modifier.padding(horizontal = 20.dp).clickable { onNavigateToResults(tournament.id) }
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            AccentIconBox(Icons.Default.EmojiEvents)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Результаты турнира", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Посмотреть таблицу результатов", fontSize = 12.sp, color = TextSecondary)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = TextMuted)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // Description
                SectionCard(title = "О турнире", modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text((tournament.description ?: "Описание турнира отсутствует."),
                        fontSize = 14.sp, color = TextSecondary, lineHeight = 20.sp)
                }

                }
                }

                Spacer(Modifier.height(12.dp))

                // Categories
                FadeIn(visible = started, delayMs = 450) {
                Column {
                val categories = tournament.categories ?: emptyList()
                if (categories.isNotEmpty()) {
                    SectionCard(title = "Категории", modifier = Modifier.padding(horizontal = 20.dp)) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            categories.forEach { cat ->
                                Surface(shape = RoundedCornerShape(8.dp), color = CardBorder.copy(alpha = 0.5f)) {
                                    Text(cat, Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Prize pool
                if (!tournament.prize.isNullOrBlank()) {
                    SectionCard(title = "Призовой фонд", modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AccentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.EmojiEvents, null, tint = Accent, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(tournament.prize, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Requirements
                if (!tournament.requirements.isNullOrEmpty()) {
                    SectionCard(title = "Требования к участникам", modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(tournament.requirements.joinToString("\n"), fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Location card
                if (locationName.isNotBlank() || locationCity.isNotBlank()) {
                    SectionCard(title = "Место проведения", modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AccentIconBox(Icons.Default.LocationOn)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                if (locationName.isNotBlank()) Text(locationName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                if (locationCity.isNotBlank()) Text(locationCity, fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }
                }
                }

                // ── BRACKET ──
                if (data.bracket.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    FadeIn(visible = started, delayMs = 600) {
                        BracketSection(data)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun BracketSection(data: com.ileader.app.ui.viewmodels.ViewerTournamentDetailData) {
    val matches = BracketUtils.mapDtosToMatches(data.bracket, data.participants)
    val groups = BracketUtils.mapGroupDtos(data.groups)
    val format = data.tournament.format ?: "single_elimination"
    var selectedMatch by remember { mutableStateOf<BracketMatch?>(null) }

    SectionCard(title = "Турнирная сетка", modifier = Modifier.padding(horizontal = 4.dp)) {
        BracketView(
            format = format,
            matches = matches,
            groups = groups,
            onMatchClick = { selectedMatch = it }
        )
    }

    selectedMatch?.let { match ->
        MatchDetailDialog(
            match = match,
            canEdit = false,
            onDismiss = { selectedMatch = null }
        )
    }
}

// ── Shared SectionCard (used by all viewer screens) ──

@Composable
internal fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DarkCardPadded(modifier = modifier) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

// ── Private helpers ──

@Composable
private fun QuickInfoCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    DarkCardPadded(modifier = modifier, padding = 10.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(AccentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 10.sp, color = TextMuted)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

private fun formatDateShort(dateStr: String): String {
    val parts = dateStr.split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2]
    val monthNames = listOf("", "янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
    val month = parts[1].toIntOrNull() ?: return dateStr
    return "$day ${monthNames.getOrElse(month) { "" }}"
}
