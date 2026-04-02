package com.ileader.app.ui.screens.sport

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.CommunityProfileDto
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.SportViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

@Composable
fun SportScreen(
    user: User,
    onTournamentClick: (String) -> Unit,
    onArticleClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onLeagueClick: (String, String, String?) -> Unit = { _, _, _ -> },
    onTeamClick: (String, String, String) -> Unit = { _, _, _ -> },
    onRankingsClick: () -> Unit = {},
    viewModel: SportViewModel = viewModel()
) {
    val s = viewModel.state
    val isDark = DarkTheme.isDark

    if (s.sports.isEmpty() && s.tournaments is UiState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Accent, strokeWidth = 2.dp)
        }
        return
    }

    // Mock leagues
    val mockLeagues = remember {
        listOf(
            MockLeague("Лига Картинга Казахстан 2026", "Картинг", 5, 3, "in_progress", 24,
                "https://ileader.kz/img/karting/karting-04-1280x853.jpeg", "15 апреля",
                listOf(MockLeader("Алихан Т.", 58), MockLeader("Марат К.", 45), MockLeader("Данияр С.", 40))),
            MockLeague("Чемпионат по Стрельбе", "Стрельба", 4, 0, "registration_open", 12,
                "https://ileader.kz/img/shooting/shooting-01-1280x853.jpeg", "20 апреля", emptyList()),
            MockLeague("Теннисная Лига Алматы", "Теннис", 8, 1, "in_progress", 32,
                "https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=800&q=80", "12 апреля",
                listOf(MockLeader("Аян Б.", 30), MockLeader("Тимур Н.", 20), MockLeader("Ерлан Ж.", 15)))
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ── Title ──
        item {
            Text(
                "Спорт", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold,
                color = TextPrimary, letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // ── Hero sport card (full width, featured sport) ──
        item {
            val featuredIdx = s.selectedIndices.firstOrNull() ?: 0
            val featured = s.sports.getOrNull(featuredIdx) ?: s.sports.firstOrNull()
            if (featured != null) {
                SportHeroCard(
                    sport = featured,
                    tournamentsCount = when (val t = s.tournaments) { is UiState.Success -> t.data.size; else -> 0 },
                    athletesCount = when (val p = s.people) { is UiState.Success -> p.data.size; else -> 0 },
                    onClick = { viewModel.toggleSport(s.sports.indexOf(featured)) }
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // ── Sport dropdown filter ──
        item {
            var dropdownExpanded by remember { mutableStateOf(false) }
            val selectedSport = s.selectedIndices.firstOrNull()?.let { s.sports.getOrNull(it) }
            Box(Modifier.padding(horizontal = 16.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp), color = CardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(0.3f)),
                    modifier = Modifier.fillMaxWidth().clickable { dropdownExpanded = true }
                ) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (selectedSport != null) sportIcon(selectedSport.name) else Icons.Default.Apps,
                            null, tint = if (selectedSport != null) Accent else TextSecondary, modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(selectedSport?.name ?: "Все виды спорта", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
                        Icon(if (dropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = TextMuted, modifier = Modifier.size(22.dp))
                    }
                }
                DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                    DropdownMenuItem(
                        text = { Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Apps, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Все виды спорта", fontSize = 14.sp, color = TextPrimary)
                            if (s.selectedIndices.isEmpty()) { Spacer(Modifier.weight(1f)); Icon(Icons.Default.Check, null, tint = Accent, modifier = Modifier.size(18.dp)) }
                        }},
                        onClick = { if (s.selectedIndices.isNotEmpty()) viewModel.toggleSport(s.selectedIndices.first()); dropdownExpanded = false }
                    )
                    s.sports.forEachIndexed { idx, sport ->
                        DropdownMenuItem(
                            text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(sportIcon(sport.name), null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(sport.name, fontSize = 14.sp, color = TextPrimary)
                                if (idx in s.selectedIndices) { Spacer(Modifier.weight(1f)); Icon(Icons.Default.Check, null, tint = Accent, modifier = Modifier.size(18.dp)) }
                            }},
                            onClick = { viewModel.toggleSport(idx); dropdownExpanded = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Description (when sport selected) ──
        val selectedSports = s.selectedSports
        if (selectedSports.isNotEmpty()) {
            item {
                SectionTitle("О спорте")
                selectedSports.forEach { sp ->
                    Surface(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp), color = CardBg,
                        shadowElevation = if (isDark) 0.dp else 2.dp
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(sportIcon(sp.name), null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(sp.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    sp.athleteLabel?.let { Text("Участник: $it", color = TextMuted, fontSize = 12.sp) }
                                }
                            }
                            sp.description?.let {
                                Spacer(Modifier.height(12.dp))
                                Text(it, color = TextSecondary, fontSize = 14.sp, lineHeight = 21.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        // ── Турниры ──
        item {
            SectionTitle(title = "Турниры", action = "Все", onAction = {})
            SportSection(state = s.tournaments) { list ->
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list, key = { it.id }) { t ->
                        TournamentMiniCard(t, onClick = { onTournamentClick(t.id) })
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Спортсмены ──
        item {
            SectionTitle(title = "Спортсмены", action = "Все", onAction = onRankingsClick)
            SportSection(state = s.people) { list ->
                val athletes = list.take(10)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(athletes, key = { it.id }) { p ->
                        PersonMiniCard(p, onClick = { onProfileClick(p.id) })
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── СМИ ──
        item {
            SectionTitle(title = "СМИ", action = "Все", onAction = {})
            SportSection(state = s.news) { list ->
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list, key = { it.id }) { a ->
                        ArticleMiniCard(a, onClick = { onArticleClick(a.id) })
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Лиги ──
        item {
            SectionTitle(title = "Лиги", action = "Все", onAction = {})
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mockLeagues.size) { i ->
                    val league = mockLeagues[i]
                    LeagueMiniCard(league) { onLeagueClick(league.name, league.sportName, league.imageUrl) }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Судьи ──
        item {
            SectionTitle("Судьи")
            SportSection(state = s.people) { list ->
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list.takeLast(5), key = { "ref_${it.id}" }) { p ->
                        PersonMiniCard(p, onClick = { onProfileClick(p.id) })
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Тренера ──
        item {
            SectionTitle("Тренера")
            SportSection(state = s.people) { list ->
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list.drop(3).take(5), key = { "tr_${it.id}" }) { p ->
                        PersonMiniCard(p, onClick = { onProfileClick(p.id) })
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Команды ──
        item {
            SectionTitle(title = "Команды", action = "Все", onAction = {})
            val mockTeams = remember {
                listOf(
                    MockTeam("Astana Racing", "Картинг", "Астана", 8),
                    MockTeam("Almaty Shooters", "Стрельба", "Алматы", 6),
                    MockTeam("Tennis Pro KZ", "Теннис", "Шымкент", 4),
                    MockTeam("Boxing Club Elite", "Бокс", "Караганда", 10)
                )
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mockTeams.size) { i ->
                        val team = mockTeams[i]
                        TeamMiniCard(team, onClick = { onTeamClick(team.name, team.sportName, team.city) })
                    }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

// ═══════════════════════════════════════════════════
// Sport Hero Card (full-width featured sport)
// ═══════════════════════════════════════════════════

@Composable
private fun SportHeroCard(
    sport: SportDto,
    tournamentsCount: Int,
    athletesCount: Int,
    onClick: () -> Unit
) {
    val imgUrl = SportViewModel.getFallbackImage(sport)
    val sColor = sportColor(sport.name)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        // Background
        if (imgUrl != null) {
            AsyncImage(model = imgUrl, contentDescription = sport.name,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(sColor.copy(0.8f), sColor.copy(0.3f)))))
        }

        // Gradient overlay
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Black.copy(0.25f), Color.Black.copy(0.75f)))
        ))

        // Content
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            // Sport icon + name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(sportIcon(sport.name), null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(sport.name, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                    color = Color.White, letterSpacing = (-0.3).sp)
            }

            // Description
            sport.description?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, fontSize = 12.sp, color = Color.White.copy(0.75f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            // Stats
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$tournamentsCount турниров", fontSize = 12.sp, color = Color.White.copy(0.8f), fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$athletesCount атлетов", fontSize = 12.sp, color = Color.White.copy(0.8f), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// Sport Chip (filter pill)
// ═══════════════════════════════════════════════════

@Composable
private fun SportChip(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = DarkTheme.isDark
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isSelected) Accent else CardBg,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Border.copy(0.3f)) else null,
        shadowElevation = if (isDark || isSelected) 0.dp else 1.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (isSelected) Color.White else TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else TextPrimary
            )
        }
    }
}

// ═══════════════════════════════════════════════════
// Section helpers
// ═══════════════════════════════════════════════════

@Composable
private fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        if (action != null && onAction != null) {
            Text(action, fontSize = 13.sp, color = Accent, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onAction() })
        }
    }
}

@Composable
private fun <T> SportSection(state: UiState<List<T>>, content: @Composable (List<T>) -> Unit) {
    when (state) {
        is UiState.Loading -> Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Accent, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
        }
        is UiState.Error -> Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            EmptyState(title = "Ошибка", subtitle = state.message)
        }
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    EmptyState(title = "Пусто", subtitle = "Данные появятся позже")
                }
            } else {
                content(state.data)
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// Tournament Mini Card
// ═══════════════════════════════════════════════════

@Composable
private fun TournamentMiniCard(t: TournamentWithCountsDto, onClick: () -> Unit) {
    val isDark = DarkTheme.isDark
    Surface(
        shape = RoundedCornerShape(18.dp), color = CardBg,
        shadowElevation = if (isDark) 0.dp else 4.dp,
        modifier = Modifier.width(260.dp).clickable(onClick = onClick)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(120.dp)) {
                if (t.imageUrl != null) {
                    AsyncImage(model = t.imageUrl, contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                        contentScale = ContentScale.Crop)
                } else {
                    val sColor = if (t.sportName != null) sportColor(t.sportName) else Accent
                    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .background(Brush.linearGradient(listOf(sColor.copy(0.8f), sColor.copy(0.4f)))))
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.5f)))))
                Row(Modifier.padding(10.dp).align(Alignment.TopStart), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    t.sportName?.let { sport ->
                        Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(0.4f)) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(sportIcon(sport), null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(sport, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            Column(Modifier.padding(14.dp)) {
                Text(t.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2,
                    overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    InfoChip(icon = Icons.Default.CalendarMonth, text = formatShortDate(t.startDate))
                    t.locationName?.let { InfoChip(icon = Icons.Default.LocationOn, text = it) }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${t.participantCount} участников", fontSize = 12.sp, color = TextMuted)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// Person Mini Card
// ═══════════════════════════════════════════════════

@Composable
private fun PersonMiniCard(p: CommunityProfileDto, onClick: () -> Unit) {
    val isDark = DarkTheme.isDark
    Surface(
        shape = RoundedCornerShape(16.dp), color = CardBg,
        shadowElevation = if (isDark) 0.dp else 4.dp,
        modifier = Modifier.width(110.dp).clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            UserAvatar(avatarUrl = p.avatarUrl, name = p.name ?: "?", size = 48.dp, showGradientBorder = p.primaryRating > 0)
            Spacer(Modifier.height(8.dp))
            Text(p.name ?: "", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (p.primarySportName.isNotEmpty()) {
                Text(p.primarySportName, fontSize = 11.sp, color = TextMuted, maxLines = 1)
            }
            if (p.primaryRating > 0) {
                Spacer(Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(50), color = Accent.copy(0.15f)) {
                    Text("${p.primaryRating}", Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Accent)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// Article Mini Card
// ═══════════════════════════════════════════════════

@Composable
private fun ArticleMiniCard(a: ArticleDto, onClick: () -> Unit) {
    val isDark = DarkTheme.isDark
    Surface(
        shape = RoundedCornerShape(18.dp), color = CardBg,
        shadowElevation = if (isDark) 0.dp else 4.dp,
        modifier = Modifier.width(220.dp).clickable(onClick = onClick)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(120.dp)) {
                if (a.coverImageUrl != null) {
                    AsyncImage(model = a.coverImageUrl, contentDescription = a.title,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                        contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .background(TextMuted.copy(0.15f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Article, null, tint = TextMuted, modifier = Modifier.size(32.dp))
                    }
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.4f)))))
            }
            Column(Modifier.padding(14.dp)) {
                Text(a.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 19.sp)
                Spacer(Modifier.height(6.dp))
                Text(formatShortDate(a.publishedAt ?: a.createdAt), color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// League Mini Card
// ═══════════════════════════════════════════════════

private data class MockLeague(
    val name: String, val sportName: String, val totalStages: Int,
    val completedStages: Int, val status: String, val participants: Int,
    val imageUrl: String?, val nextStageDate: String?, val leaders: List<MockLeader>
)
private data class MockLeader(val name: String, val points: Int)

@Composable
private fun LeagueMiniCard(league: MockLeague, onClick: () -> Unit = {}) {
    val isDark = DarkTheme.isDark
    Surface(
        shape = RoundedCornerShape(18.dp), color = CardBg,
        shadowElevation = if (isDark) 0.dp else 4.dp,
        modifier = Modifier.width(280.dp).clickable(onClick = onClick)
    ) {
        Column {
            // Hero
            Box(Modifier.fillMaxWidth().height(110.dp)) {
                if (league.imageUrl != null) {
                    AsyncImage(model = league.imageUrl, contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                        contentScale = ContentScale.Crop)
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.2f), Color.Black.copy(0.7f)))))
                Row(Modifier.padding(10.dp).align(Alignment.TopStart), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(0.4f)) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(sportIcon(league.sportName), null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(league.sportName, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                    val (statusLabel, statusColor) = when (league.status) {
                        "in_progress" -> "Идёт" to ILeaderColors.Success
                        "registration_open" -> "Регистрация" to ILeaderColors.Info
                        else -> league.status to TextMuted
                    }
                    Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.9f)) {
                        Text(statusLabel, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
                Text(league.name, Modifier.align(Alignment.BottomStart).padding(12.dp), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2, lineHeight = 22.sp)
            }

            Column(Modifier.padding(14.dp)) {
                // Stage timeline
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Этапы", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Text("${league.completedStages}/${league.totalStages}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Accent)
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..league.totalStages) {
                        val done = i <= league.completedStages; val current = i == league.completedStages + 1
                        Box(
                            Modifier.size(if (current) 20.dp else 14.dp).clip(CircleShape)
                                .background(when { done -> Accent; current -> Accent.copy(0.3f); else -> TextMuted.copy(0.18f) })
                                .then(if (current) Modifier.border(2.dp, Accent, CircleShape) else Modifier),
                            contentAlignment = Alignment.Center
                        ) { if (done) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(10.dp)) }
                        if (i < league.totalStages) Box(Modifier.weight(1f).height(2.dp).padding(horizontal = 2.dp)
                            .background(if (i < league.completedStages + 1) Accent.copy(0.5f) else TextMuted.copy(0.18f), RoundedCornerShape(1.dp)))
                    }
                }
                Spacer(Modifier.height(14.dp))

                // Leaders
                if (league.leaders.isNotEmpty()) {
                    Text("Лидеры", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    league.leaders.forEachIndexed { idx, leader ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(when (idx) { 0 -> "🥇"; 1 -> "🥈"; else -> "🥉" }, fontSize = 14.sp, modifier = Modifier.width(24.dp))
                            Box(Modifier.size(24.dp).clip(CircleShape).background(TextMuted.copy(0.18f)), contentAlignment = Alignment.Center) {
                                Text(leader.name.take(1), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(leader.name, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${leader.points} очк.", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                        }
                    }
                }

                // Next stage
                league.nextStageDate?.let { date ->
                    Spacer(Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = Accent.copy(0.15f), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, null, tint = Accent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Следующий этап: $date", fontSize = 12.sp, color = Accent, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// Team Mini Card
// ═══════════════════════════════════════════════════

private data class MockTeam(
    val name: String, val sportName: String, val city: String, val members: Int
)

@Composable
private fun TeamMiniCard(team: MockTeam, onClick: () -> Unit = {}) {
    val isDark = DarkTheme.isDark
    val sColor = sportColor(team.sportName)
    Surface(
        shape = RoundedCornerShape(16.dp), color = CardBg,
        shadowElevation = if (isDark) 0.dp else 4.dp,
        modifier = Modifier.width(160.dp).height(160.dp).clickable(onClick = onClick)
    ) {
        Column(
            Modifier.padding(14.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(sColor.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(team.name.take(1), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = sColor)
            }
            Spacer(Modifier.height(10.dp))
            Text(team.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(sportIcon(team.sportName), null, tint = TextMuted, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(team.sportName, fontSize = 11.sp, color = TextMuted)
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(team.city, fontSize = 11.sp, color = TextMuted)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("${team.members}", fontSize = 11.sp, color = TextMuted)
                }
            }
        }
    }
}
