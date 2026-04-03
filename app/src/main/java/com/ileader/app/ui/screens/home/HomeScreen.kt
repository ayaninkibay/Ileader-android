package com.ileader.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.User
import com.ileader.app.data.preferences.SportPreference
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.CommunityProfileDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.HomeViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

@Composable
fun HomeScreen(
    user: User,
    onArticleClick: (String) -> Unit,
    onTournamentClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onRankingsClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.load()
    }

    val state = viewModel.state

    val colors = LocalAppColors.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Header ──
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                // Top row: greeting + bell
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Привет, ${user.name.split(" ").firstOrNull() ?: ""}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = user.role.displayName,
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    }

                    // Notification bell
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CardBg)
                            .border(
                                1.dp,
                                Border.copy(alpha = 0.3f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Sport calendar strip
                SportWeekCalendar(tournaments = state.tournaments)
            }
        }


        // ── Новости (overlay card style) ──
        item {
            Spacer(Modifier.height(20.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Новости", action = "Все", onAction = {})
                Spacer(Modifier.height(12.dp))
            }
            
        }
        item {
            NewsContent(state = state.news, onArticleClick = onArticleClick)
            
        }

        // ── Promo: Rating card ──
        item {
            Spacer(Modifier.height(20.dp))
            RatingPromoCard(onClick = onRankingsClick)
            
        }

        // ── Турниры ──
        item {
            Spacer(Modifier.height(20.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Ближайшие турниры", action = "Все", onAction = {})
                Spacer(Modifier.height(12.dp))
            }
            
        }
        item {
            TournamentsContent(
                state = state.tournaments,
                onTournamentClick = onTournamentClick
            )
            
        }

        // ── Люди ──
        item {
            Spacer(Modifier.height(24.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Сообщество", action = "Все", onAction = {})
                Spacer(Modifier.height(12.dp))
            }
            
        }
        item {
            PeopleContent(state = state.people, onProfileClick = onProfileClick)
            
        }
    }
}

// ══════════════════════════════════════════════════════════
// Sport Week Calendar
// ══════════════════════════════════════════════════════════

@Composable
private fun SportWeekCalendar(tournaments: UiState<List<TournamentWithCountsDto>>) {
    val today = remember { java.time.LocalDate.now() }
    val startOfWeek = remember { today.minusDays(today.dayOfWeek.value.toLong() - 1) }
    var selectedDate by remember { mutableStateOf<java.time.LocalDate?>(null) }

    // Tournament data by date
    val tournamentList = remember(tournaments) {
        when (tournaments) {
            is UiState.Success -> tournaments.data
            else -> emptyList()
        }
    }
    val tournamentsByDate = remember(tournamentList) {
        tournamentList.groupBy { it.startDate?.take(10) ?: "" }
    }
    val weekTournamentCount = remember(tournamentsByDate, startOfWeek) {
        (0..6).sumOf { i ->
            val dateStr = startOfWeek.plusDays(i.toLong()).toString()
            tournamentsByDate[dateStr]?.size ?: 0
        }
    }

    // Nearest upcoming tournament
    val nearestTournament = remember(tournamentList, today) {
        tournamentList
            .filter { t -> t.startDate?.take(10)?.let { it >= today.toString() } == true }
            .minByOrNull { it.startDate ?: "" }
    }
    val daysUntilNearest = remember(nearestTournament, today) {
        nearestTournament?.startDate?.take(10)?.let {
            try {
                java.time.temporal.ChronoUnit.DAYS.between(today, java.time.LocalDate.parse(it)).toInt()
            } catch (_: Exception) { null }
        }
    }

    // Tournaments for selected day
    val selectedDayTournaments = remember(selectedDate, tournamentsByDate) {
        selectedDate?.let { tournamentsByDate[it.toString()] } ?: emptyList()
    }

    // Background image
    val calendarBgUrl = remember(tournaments) {
        when (tournaments) {
            is UiState.Success -> {
                val firstSport = tournaments.data.firstOrNull()?.sportName
                firstSport?.let { sportImageUrl(it) }
                    ?: "https://ileader.kz/img/karting/karting-15-1280x853.jpeg"
            }
            else -> "https://ileader.kz/img/karting/karting-15-1280x853.jpeg"
        }
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
        ) {
            AsyncImage(
                model = calendarBgUrl,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.55f))
                    )
                )
            )

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth, null,
                            tint = ILeaderColors.PrimaryRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Расписание",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    if (weekTournamentCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ILeaderColors.PrimaryRed.copy(alpha = 0.8f)
                        ) {
                            Text(
                                "$weekTournamentCount ${pluralTournament(weekTournamentCount)}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    } else {
                        Text(
                            "${today.dayOfMonth}.${String.format("%02d", today.monthValue)}.${today.year}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Days row
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                    for (i in 0..6) {
                        val date = startOfWeek.plusDays(i.toLong())
                        val isToday = date == today
                        val isSelected = date == selectedDate
                        val dateStr = date.toString()
                        val dayTournaments = tournamentsByDate[dateStr] ?: emptyList()

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                selectedDate = if (selectedDate == date) null else date
                            }
                        ) {
                            Text(
                                dayNames[i],
                                fontSize = 11.sp,
                                color = if (isToday) ILeaderColors.PrimaryRed else Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (isSelected && !isToday) Modifier.border(
                                            1.5.dp, ILeaderColors.PrimaryRed, CircleShape
                                        ) else Modifier
                                    )
                                    .background(
                                        when {
                                            isToday -> ILeaderColors.PrimaryRed
                                            else -> Color.White.copy(alpha = 0.1f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${date.dayOfMonth}",
                                    fontSize = 14.sp,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                if (dayTournaments.isNotEmpty()) {
                                    dayTournaments.take(3).forEach { t ->
                                        Box(
                                            Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    t.sportName?.let { sportColor(it) }
                                                        ?: ILeaderColors.PrimaryRed
                                                )
                                        )
                                    }
                                } else {
                                    Box(Modifier.size(5.dp))
                                }
                            }
                        }
                    }
                }

                // Nearest tournament teaser (when no day selected)
                if (selectedDate == null && nearestTournament != null && daysUntilNearest != null) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents, null,
                            tint = ILeaderColors.PrimaryRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                nearestTournament.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            nearestTournament.sportName?.let { sport ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(sportIcon(sport), null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(sport, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                                }
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (daysUntilNearest == 0) ILeaderColors.PrimaryRed
                            else Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                when (daysUntilNearest) {
                                    0 -> "Сегодня"
                                    1 -> "Завтра"
                                    else -> "через $daysUntilNearest дн."
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Selected day tournaments
        if (selectedDate != null && selectedDayTournaments.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            selectedDayTournaments.forEach { t ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = CardBg,
                    border = if (DarkTheme.isDark) DarkTheme.cardBorderStroke else null
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sport color indicator
                        Box(
                            Modifier
                                .width(4.dp)
                                .height(36.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(t.sportName?.let { sportColor(it) } ?: Accent)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                t.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                t.sportName?.let { sport ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(sportIcon(sport), null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text(sport, fontSize = 12.sp, color = TextMuted)
                                    }
                                }
                                t.locationName?.let { loc ->
                                    Text("· $loc", fontSize = 12.sp, color = TextMuted, maxLines = 1)
                                }
                            }
                        }
                        Text(
                            "${t.participantCount} уч.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        } else if (selectedDate != null && selectedDayTournaments.isEmpty()) {
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет турниров в этот день", color = TextMuted, fontSize = 13.sp)
            }
        }
    }
}

private fun pluralTournament(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod100 in 11..14 -> "турниров"
        mod10 == 1 -> "турнир"
        mod10 in 2..4 -> "турнира"
        else -> "турниров"
    }
}

// ══════════════════════════════════════════════════════════
// Rating Promo Card (gradient accent)
// ══════════════════════════════════════════════════════════

@Composable
private fun RatingPromoCard(onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(ILeaderColors.PrimaryRed, ILeaderColors.DarkRed)
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(18.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Рейтинг спортсменов",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Узнайте свою позицию",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Leaderboard,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// News (overlay style cards like inzhu)
// ══════════════════════════════════════════════════════════

@Composable
private fun NewsContent(
    state: UiState<List<ArticleDto>>,
    onArticleClick: (String) -> Unit
) {
    when (state) {
        is UiState.Loading -> Box(
            Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) { LoadingScreen() }

        is UiState.Error -> Box(Modifier.padding(horizontal = 16.dp)) {
            EmptyState(title = "Ошибка загрузки", subtitle = state.message)
        }

        is UiState.Success -> {
            if (state.data.isEmpty()) {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    EmptyState(title = "Нет новостей", subtitle = "Новости появятся позже")
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(state.data, key = { _, it -> it.id }) { index, article ->
                        NewsCardOverlay(
                            article = article,
                            onClick = { onArticleClick(article.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsCardOverlay(article: ArticleDto, onClick: () -> Unit) {
    // Overlay-style card: image fills the whole card, text on top
    Box(
        modifier = Modifier
            .width(180.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        // Background image
        AsyncImage(
            model = article.coverImageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        // Play icon (top right)
        Box(
            modifier = Modifier
                .padding(10.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
                .align(Alignment.TopEnd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // Text content (bottom)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = article.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = article.excerpt?.take(60) ?: "",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.75f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
// Tournaments
// ══════════════════════════════════════════════════════════

@Composable
private fun TournamentsContent(
    state: UiState<List<TournamentWithCountsDto>>,
    onTournamentClick: (String) -> Unit
) {
    when (state) {
        is UiState.Loading -> Box(
            Modifier.fillMaxWidth().height(180.dp),
            contentAlignment = Alignment.Center
        ) { LoadingScreen() }

        is UiState.Error -> Box(Modifier.padding(horizontal = 16.dp)) {
            EmptyState(title = "Ошибка загрузки", subtitle = state.message)
        }

        is UiState.Success -> {
            if (state.data.isEmpty()) {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    EmptyState(title = "Нет турниров", subtitle = "Турниры появятся позже")
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        state.data,
                        key = { _, it -> it.id }) { index, tournament ->
                        TournamentCard(
                            tournament = tournament,
                            onClick = { onTournamentClick(tournament.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentCard(tournament: TournamentWithCountsDto, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val isDark = DarkTheme.isDark

    Surface(
        modifier = Modifier
            .width(260.dp),
        shape = RoundedCornerShape(18.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke
        else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(alpha = 0.3f)),
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.clickable { onClick() }
        ) {
            // Cover image
            if (tournament.imageUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    AsyncImage(
                        model = tournament.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.4f)
                                    )
                                )
                            )
                    )
                    // Status + sport on image
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopStart),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tournament.sportName?.let { sport ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.Black.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        sportIcon(sport), null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(sport, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        tournament.status?.let { status ->
                            if (status == "in_progress") {
                                LiveIndicator()
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = getStatusColor(status).copy(alpha = 0.9f)
                                ) {
                                    Text(
                                        getStatusLabel(status),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // No image — color strip
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    getStatusColor(tournament.status ?: ""),
                                    getStatusColor(tournament.status ?: "").copy(alpha = 0.5f)
                                )
                            ),
                            RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                        )
                )
            }

            Column(modifier = Modifier.padding(14.dp)) {
                // Sport + status (only when no image)
                if (tournament.imageUrl == null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tournament.sportName?.let { sport ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = TextMuted.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(sportIcon(sport), null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(sport, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                                }
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        tournament.status?.let { status ->
                            val statusColor = getStatusColor(status)
                            if (status == "in_progress") LiveIndicator()
                            else StatusBadge(text = getStatusLabel(status), color = statusColor)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                Text(
                    text = tournament.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                    letterSpacing = (-0.2).sp
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip(
                        icon = Icons.Default.CalendarMonth,
                        text = formatDateShort(tournament.startDate)
                    )
                    if (!tournament.locationName.isNullOrEmpty()) {
                        InfoChip(
                            icon = Icons.Default.LocationOn,
                            text = tournament.locationName,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.People, null, modifier = Modifier.size(15.dp), tint = TextMuted)
                        Text(
                            "${tournament.participantCount} участников",
                            fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                            tint = Accent, modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// People
// ══════════════════════════════════════════════════════════

@Composable
private fun PeopleContent(
    state: UiState<List<CommunityProfileDto>>,
    onProfileClick: (String) -> Unit
) {
    when (state) {
        is UiState.Loading -> Box(
            Modifier.fillMaxWidth().height(150.dp),
            contentAlignment = Alignment.Center
        ) { LoadingScreen() }

        is UiState.Error -> Box(Modifier.padding(horizontal = 16.dp)) {
            EmptyState(title = "Ошибка загрузки", subtitle = state.message)
        }

        is UiState.Success -> {
            if (state.data.isEmpty()) {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    EmptyState(title = "Нет людей", subtitle = "Данные появятся позже")
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        state.data,
                        key = { _, it -> it.id }) { index, profile ->
                        PersonCard(
                            profile = profile,
                            onClick = { onProfileClick(profile.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonCard(profile: CommunityProfileDto, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val isDark = DarkTheme.isDark

    Surface(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke
        else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(alpha = 0.3f)),
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            UserAvatar(
                avatarUrl = profile.avatarUrl,
                name = profile.name ?: "?",
                size = 56.dp,
                showGradientBorder = profile.primaryRating > 0
            )

            Spacer(Modifier.height(10.dp))

            // Name
            Text(
                text = profile.name ?: "",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Subtype or sport
            val subtitle = profile.subtypeLabel ?: profile.primarySportName.ifEmpty { null }
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (profile.primarySportName.isNotEmpty()) {
                        Icon(
                            sportIcon(profile.primarySportName), null,
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                    }
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // City
            if (!profile.city.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn, null,
                        tint = TextMuted.copy(alpha = 0.7f),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = profile.city,
                        fontSize = 10.sp,
                        color = TextMuted.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }

            // Rating badge
            if (profile.primaryRating > 0) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Accent.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star, null,
                            tint = Accent,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${profile.primaryRating}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Accent
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Utils
// ══════════════════════════════════════════════════════════

private fun formatDateShort(dateStr: String?): String {
    if (dateStr == null) return ""
    val parts = dateStr.take(10).split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2].toIntOrNull() ?: return dateStr
    val monthNames = listOf(
        "", "янв", "фев", "мар", "апр", "мая",
        "июн", "июл", "авг", "сен", "окт", "ноя", "дек"
    )
    val month = parts[1].toIntOrNull() ?: return dateStr
    return "$day ${monthNames.getOrElse(month) { "" }}"
}

private fun getStatusLabel(status: String): String = when (status) {
    "in_progress" -> "Идёт"
    "registration_open" -> "Регистрация"
    "registration_closed" -> "Рег. закрыта"
    "check_in" -> "Check-in"
    "completed" -> "Завершён"
    "cancelled" -> "Отменён"
    else -> status
}

private fun getStatusColor(status: String): Color = when (status) {
    "registration_open" -> ILeaderColors.Success
    "in_progress" -> ILeaderColors.Info
    "check_in" -> ILeaderColors.Warning
    "completed" -> Color(0xFF8E8E93)
    else -> Color(0xFF8E8E93)
}

private fun getCategoryLabel(category: String): String = when (category) {
    "news" -> "Новости"
    "report" -> "Отчёт"
    "interview" -> "Интервью"
    "analytics" -> "Аналитика"
    "review" -> "Обзор"
    "announcement" -> "Анонс"
    "highlight" -> "Главное"
    else -> category.replaceFirstChar { it.uppercase() }
}
