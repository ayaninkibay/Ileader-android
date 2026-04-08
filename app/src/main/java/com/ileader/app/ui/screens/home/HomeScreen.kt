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
import com.ileader.app.data.models.UserRole
import com.ileader.app.data.preferences.SportPreference
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.CommunityProfileDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.AdminDashboardState
import com.ileader.app.ui.viewmodels.AthleteDashboardState
import com.ileader.app.ui.viewmodels.HomeViewModel
import com.ileader.app.ui.viewmodels.MediaDashboardState
import com.ileader.app.ui.viewmodels.OrganizerDashboardState
import com.ileader.app.ui.viewmodels.RefereeDashboardState
import com.ileader.app.ui.viewmodels.SponsorDashboardState
import com.ileader.app.ui.viewmodels.TrainerDashboardState

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
    onNotificationsClick: () -> Unit = {},
    onAllNewsClick: () -> Unit = {},
    onAllTournamentsClick: () -> Unit = {},
    onAllPeopleClick: () -> Unit = {},
    onAdminUsersClick: () -> Unit = {},
    onAdminVerificationsClick: () -> Unit = {},
    onAdminSportRequestsClick: () -> Unit = {},
    onAdminSettingsClick: () -> Unit = {},
    onLeaguesClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.loadUnreadCount(user.id)
        when (user.role) {
            UserRole.ORGANIZER -> viewModel.loadOrganizerDashboard(user.id)
            UserRole.REFEREE -> viewModel.loadRefereeDashboard(user.id)
            UserRole.TRAINER -> viewModel.loadTrainerDashboard(user.id)
            UserRole.ATHLETE -> viewModel.loadAthleteDashboard(user.id)
            UserRole.SPONSOR -> viewModel.loadSponsorDashboard(user.id)
            UserRole.MEDIA -> viewModel.loadMediaDashboard(user.id)
            UserRole.ADMIN -> viewModel.loadAdminDashboard()
            else -> {}
        }
    }

    val state = viewModel.state
    val orgDashboard = viewModel.organizerDashboard
    val refDashboard = viewModel.refereeDashboard
    val trnDashboard = viewModel.trainerDashboard
    val athDashboard = viewModel.athleteDashboard
    val spDashboard = viewModel.sponsorDashboard
    val mdDashboard = viewModel.mediaDashboard
    val adDashboard = viewModel.adminDashboard
    val isRefreshing = state.tournaments is UiState.Loading && state.news is UiState.Loading

    val colors = LocalAppColors.current

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.load() },
        modifier = Modifier.fillMaxSize().background(Bg)
    ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                    Box(contentAlignment = Alignment.TopEnd) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CardBg)
                                .border(
                                    1.dp,
                                    Border.copy(alpha = 0.3f),
                                    CircleShape
                                )
                                .clickable { onNotificationsClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        if (viewModel.unreadNotifications > 0) {
                            Box(
                                modifier = Modifier
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .size(if (viewModel.unreadNotifications > 9) 18.dp else 16.dp)
                                    .clip(CircleShape)
                                    .background(Accent)
                                    .border(2.dp, Bg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (viewModel.unreadNotifications > 9) "9+" else "${viewModel.unreadNotifications}",
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Sport calendar strip
                SportWeekCalendar(tournaments = state.tournaments)
            }
        }


        // ── Organizer Dashboard ──
        if (user.role == UserRole.ORGANIZER && orgDashboard.isLoaded) {
            item {
                OrganizerDashboardSection(
                    dashboard = orgDashboard,
                    onTournamentClick = onTournamentClick
                )
            }
        }

        // ── Referee Dashboard ──
        if (user.role == UserRole.REFEREE && refDashboard.isLoaded) {
            item {
                RefereeDashboardSection(
                    dashboard = refDashboard,
                    onTournamentClick = onTournamentClick
                )
            }
        }

        // ── Trainer Dashboard ──
        if (user.role == UserRole.TRAINER && trnDashboard.isLoaded) {
            item {
                TrainerDashboardSection(
                    dashboard = trnDashboard,
                    onTournamentClick = onTournamentClick
                )
            }
        }

        // ── Athlete Dashboard ──
        if (user.role == UserRole.ATHLETE && athDashboard.isLoaded) {
            item {
                AthleteDashboardSection(
                    dashboard = athDashboard,
                    onTournamentClick = onTournamentClick,
                    onRankingsClick = onRankingsClick
                )
            }
        }

        // ── Admin Dashboard ──
        if (user.role == UserRole.ADMIN && adDashboard.isLoaded) {
            item {
                AdminDashboardSection(
                    dashboard = adDashboard,
                    onUsersClick = onAdminUsersClick,
                    onVerificationsClick = onAdminVerificationsClick,
                    onSportRequestsClick = onAdminSportRequestsClick,
                    onSettingsClick = onAdminSettingsClick
                )
            }
        }

        // ── Sponsor Dashboard ──
        if (user.role == UserRole.SPONSOR && spDashboard.isLoaded) {
            item {
                SponsorDashboardSection(
                    dashboard = spDashboard,
                    onTournamentClick = onTournamentClick
                )
            }
        }

        // ── Media Dashboard ──
        if (user.role == UserRole.MEDIA && mdDashboard.isLoaded) {
            item {
                MediaDashboardSection(
                    dashboard = mdDashboard,
                    onArticleClick = onArticleClick
                )
            }
        }

        // ── Новости (overlay card style) ──
        item {
            Spacer(Modifier.height(20.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Новости", action = "Все", onAction = onAllNewsClick)
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

        // ── Leagues entry ──
        item {
            Spacer(Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onLeaguesClick() },
                shape = RoundedCornerShape(14.dp),
                color = CardBg
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape)
                            .background(Accent.copy(0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.MilitaryTech, null, tint = Accent, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Лиги", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Серии турниров и общие зачёты", fontSize = 12.sp, color = TextMuted)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                }
            }
        }

        // ── Турниры ──
        item {
            Spacer(Modifier.height(20.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Ближайшие турниры", action = "Все", onAction = onAllTournamentsClick)
                Spacer(Modifier.height(12.dp))
            }
            
        }
        item {
            TournamentsContent(
                state = state.tournaments,
                onTournamentClick = onTournamentClick
            )
            
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
    } // PullToRefreshBox
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
                if (nearestTournament != null && daysUntilNearest != null) {
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
                                    SportTag(sport)
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
        shadowElevation = 0.dp
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
                            SportTag(sport, onImage = true)
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
                            SportTag(sport)
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

// ══════════════════════════════════════════════════════════
// Organizer Dashboard Section
// ══════════════════════════════════════════════════════════

@Composable
private fun OrganizerDashboardSection(
    dashboard: OrganizerDashboardState,
    onTournamentClick: (String) -> Unit
) {
    val stats = dashboard.stats ?: return

    Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(20.dp))

        // ── Stats Row ──
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrgStatCard(
                value = stats.totalTournaments,
                label = "Турниров",
                icon = Icons.Filled.EmojiEvents,
                color = Accent,
                modifier = Modifier.weight(1f)
            )
            OrgStatCard(
                value = stats.activeTournaments,
                label = "Активных",
                icon = Icons.Filled.PlayCircle,
                color = Color(0xFF22C55E),
                modifier = Modifier.weight(1f)
            )
            OrgStatCard(
                value = stats.totalParticipants,
                label = "Участников",
                icon = Icons.Filled.People,
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
        }

        // ── Upcoming tournaments ──
        if (dashboard.upcomingTournaments.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Ближайшие мои турниры",
                fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))

            dashboard.upcomingTournaments.take(3).forEach { t ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTournamentClick(t.id) },
                    shape = RoundedCornerShape(12.dp),
                    color = CardBg
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp)
                                .background(Accent.copy(0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.EmojiEvents, null, tint = Accent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                formatDateShort(t.startDate) + " • " + getStatusLabel(t.status ?: ""),
                                fontSize = 12.sp, color = TextMuted
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                            tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // ── Recent registrations ──
        if (dashboard.recentRegistrations.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Последние регистрации",
                fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), color = CardBg
            ) {
                Column(Modifier.padding(12.dp)) {
                    dashboard.recentRegistrations.take(5).forEachIndexed { idx, reg ->
                        if (idx > 0) {
                            Spacer(Modifier.height(1.dp))
                            Box(Modifier.fillMaxWidth().height(0.5.dp).background(Border.copy(0.15f)))
                            Spacer(Modifier.height(1.dp))
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(28.dp).clip(CircleShape)
                                    .background(Color(0xFF22C55E).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    (reg.profiles?.name ?: "?").take(1).uppercase(),
                                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    color = Color(0xFF22C55E)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    reg.profiles?.name ?: "—",
                                    fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary
                                )
                                reg.tournaments?.name?.let { tName ->
                                    Text(tName, fontSize = 11.sp, color = TextMuted, maxLines = 1,
                                        overflow = TextOverflow.Ellipsis)
                                }
                            }
                            val statusColor = when (reg.status) {
                                "pending" -> Color(0xFFF59E0B)
                                "confirmed" -> Color(0xFF22C55E)
                                else -> TextMuted
                            }
                            val statusLabel = when (reg.status) {
                                "pending" -> "Ожидает"
                                "confirmed" -> "Подтв."
                                else -> reg.status ?: ""
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusColor.copy(alpha = 0.1f)
                            ) {
                                Text(statusLabel,
                                    Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrgStatCard(
    value: Int,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = CardBg) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text("$value", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(label, fontSize = 11.sp, color = TextMuted)
        }
    }
}

// ══════════════════════════════════════════════════════════
// Referee Dashboard Section
// ══════════════════════════════════════════════════════════

@Composable
private fun RefereeDashboardSection(
    dashboard: RefereeDashboardState,
    onTournamentClick: (String) -> Unit
) {
    val stats = dashboard.stats ?: return

    Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(20.dp))

        // ── Stats Row ──
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrgStatCard(
                value = stats.totalTournaments,
                label = "Турниров",
                icon = Icons.Filled.SportsKabaddi,
                color = Accent,
                modifier = Modifier.weight(1f)
            )
            OrgStatCard(
                value = stats.thisMonth,
                label = "В месяце",
                icon = Icons.Filled.CalendarMonth,
                color = Color(0xFF22C55E),
                modifier = Modifier.weight(1f)
            )
            OrgStatCard(
                value = stats.pendingResults,
                label = "Активных",
                icon = Icons.Filled.PlayCircle,
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }

        // ── Active tournaments ──
        if (dashboard.activeTournaments.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Активные турниры",
                fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))

            dashboard.activeTournaments.forEach { t ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTournamentClick(t.id) },
                    shape = RoundedCornerShape(12.dp),
                    color = CardBg
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp)
                                .background(Color(0xFFF59E0B).copy(0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.SportsKabaddi, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${t.sport} • ${formatDateShort(t.date)}",
                                fontSize = 12.sp, color = TextMuted
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                            tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Trainer Dashboard Section
// ══════════════════════════════════════════════════════════

@Composable
private fun TrainerDashboardSection(
    dashboard: TrainerDashboardState,
    onTournamentClick: (String) -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(20.dp))

        // ── Stats Row ──
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrgStatCard(
                value = dashboard.teams.size,
                label = "Команд",
                icon = Icons.Filled.Groups,
                color = Accent,
                modifier = Modifier.weight(1f)
            )
            OrgStatCard(
                value = dashboard.totalAthletes,
                label = "Атлетов",
                icon = Icons.Filled.People,
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
            OrgStatCard(
                value = dashboard.upcomingTournaments.size,
                label = "Турниров",
                icon = Icons.Filled.EmojiEvents,
                color = Color(0xFF22C55E),
                modifier = Modifier.weight(1f)
            )
        }

        // ── Teams summary ──
        if (dashboard.teams.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Мои команды", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            dashboard.teams.take(3).forEach { team ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    shape = RoundedCornerShape(12.dp), color = CardBg
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp)
                                .background(Accent.copy(0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Groups, null, tint = Accent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(team.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${team.sportName} • ${team.members.size} атлетов",
                                fontSize = 12.sp, color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // ── Upcoming tournaments for trainer's teams ──
        if (dashboard.upcomingTournaments.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Ближайшие турниры команды", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            dashboard.upcomingTournaments.forEach { t ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTournamentClick(t.id) },
                    shape = RoundedCornerShape(12.dp), color = CardBg
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp)
                                .background(Color(0xFF22C55E).copy(0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.EmojiEvents, null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                formatDateShort(t.startDate ?: "") + " • " + getStatusLabel(t.status ?: ""),
                                fontSize = 12.sp, color = TextMuted
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                            tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Athlete Dashboard Section
// ══════════════════════════════════════════════════════════

@Composable
private fun AthleteDashboardSection(
    dashboard: AthleteDashboardState,
    onTournamentClick: (String) -> Unit,
    onRankingsClick: () -> Unit
) {
    val stats = dashboard.stats ?: return

    Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(20.dp))

        // ── Rating hero card ──
        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .clickable { onRankingsClick() },
            shape = RoundedCornerShape(16.dp),
            color = CardBg
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(56.dp).clip(CircleShape)
                        .background(Accent.copy(0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Star, null, tint = Accent, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Ваш рейтинг", fontSize = 12.sp, color = TextMuted)
                    Text("${stats.rating}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Stats Row ──
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrgStatCard(
                value = stats.totalTournaments,
                label = "Турниров",
                icon = Icons.Filled.EmojiEvents,
                color = Accent,
                modifier = Modifier.weight(1f)
            )
            OrgStatCard(
                value = stats.wins,
                label = "Побед",
                icon = Icons.Filled.MilitaryTech,
                color = Color(0xFF22C55E),
                modifier = Modifier.weight(1f)
            )
            OrgStatCard(
                value = stats.podiums,
                label = "Подиумов",
                icon = Icons.Filled.WorkspacePremium,
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }

        // ── Active goals ──
        if (dashboard.goals.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Активные цели", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            dashboard.goals.forEach { goal ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    shape = RoundedCornerShape(12.dp), color = CardBg
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                when (goal.type) {
                                    com.ileader.app.data.models.GoalType.RATING -> Icons.Filled.Star
                                    com.ileader.app.data.models.GoalType.TOURNAMENT -> Icons.Filled.EmojiEvents
                                    com.ileader.app.data.models.GoalType.POINTS -> Icons.Filled.TrendingUp
                                },
                                null, tint = Accent, modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(goal.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                color = TextPrimary, modifier = Modifier.weight(1f))
                            Text("${goal.currentValue}/${goal.targetValue}",
                                fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                        }
                        if (goal.targetValue > 0) {
                            Spacer(Modifier.height(8.dp))
                            val progress = (goal.currentValue.toFloat() / goal.targetValue).coerceIn(0f, 1f)
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Accent,
                                trackColor = Border.copy(0.15f)
                            )
                        }
                    }
                }
            }
        }

        // ── Upcoming tournaments ──
        if (dashboard.upcomingTournaments.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Мои ближайшие турниры", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            dashboard.upcomingTournaments.forEach { t ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTournamentClick(t.id) },
                    shape = RoundedCornerShape(12.dp), color = CardBg
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp)
                                .background(Accent.copy(0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.EmojiEvents, null, tint = Accent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                formatDateShort(t.startDate) + " • " + getStatusLabel(t.status.name.lowercase()),
                                fontSize = 12.sp, color = TextMuted
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                            tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Sponsor Dashboard Section
// ══════════════════════════════════════════════════════════

@Composable
private fun SponsorDashboardSection(
    dashboard: SponsorDashboardState,
    onTournamentClick: (String) -> Unit
) {
    val stats = dashboard.stats ?: return
    Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrgStatCard(
                value = stats.totalSponsored,
                label = "Спонсорств",
                icon = Icons.Filled.WorkspacePremium,
                color = Accent,
                modifier = Modifier.weight(1f)
            )
            OrgStatCard(
                value = stats.activeSponsorships,
                label = "Активных",
                icon = Icons.Filled.PlayCircle,
                color = Color(0xFF22C55E),
                modifier = Modifier.weight(1f)
            )
            OrgStatCard(
                value = (stats.totalAmount / 1000.0).toInt(),
                label = "Тыс. ₸",
                icon = Icons.Filled.Payments,
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }

        if (dashboard.sponsorships.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Мои спонсорства", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            dashboard.sponsorships.forEach { sp ->
                val t = sp.tournaments
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTournamentClick(sp.tournamentId) },
                    shape = RoundedCornerShape(12.dp), color = CardBg
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp)
                                .background(Accent.copy(0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.WorkspacePremium, null, tint = Accent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t?.name ?: "Турнир", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${sp.tier ?: "—"} • ${(sp.amount ?: 0.0).toLong()} ₸",
                                fontSize = 12.sp, color = TextMuted
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                            tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Media Dashboard Section
// ══════════════════════════════════════════════════════════

@Composable
private fun MediaDashboardSection(
    dashboard: MediaDashboardState,
    onArticleClick: (String) -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrgStatCard(
                value = dashboard.articleStats?.published ?: 0,
                label = "Статей",
                icon = Icons.Filled.Article,
                color = Accent,
                modifier = Modifier.weight(1f)
            )
            OrgStatCard(
                value = dashboard.accreditationsAccepted,
                label = "Аккредитаций",
                icon = Icons.Filled.Badge,
                color = Color(0xFF22C55E),
                modifier = Modifier.weight(1f)
            )
            OrgStatCard(
                value = dashboard.interviewsTotal,
                label = "Интервью",
                icon = Icons.Filled.Mic,
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
        }

        if (dashboard.recentArticles.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Последние публикации", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            dashboard.recentArticles.forEach { a ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onArticleClick(a.id) },
                    shape = RoundedCornerShape(12.dp), color = CardBg
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp)
                                .background(Accent.copy(0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Article, null, tint = Accent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(a.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${a.status ?: "draft"} • ${a.views} просмотров",
                                fontSize = 12.sp, color = TextMuted
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                            tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Admin Dashboard Section
// ══════════════════════════════════════════════════════════

@Composable
private fun AdminDashboardSection(
    dashboard: AdminDashboardState,
    onUsersClick: () -> Unit,
    onVerificationsClick: () -> Unit,
    onSportRequestsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val stats = dashboard.stats ?: return

    Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(20.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminStatCard(
                value = stats.totalUsers,
                label = "Пользователей",
                icon = Icons.Filled.People,
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f),
                onClick = onUsersClick
            )
            AdminStatCard(
                value = stats.totalTournaments,
                label = "Турниров",
                icon = Icons.Filled.EmojiEvents,
                color = Accent,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminStatCard(
                value = stats.activeTournaments,
                label = "Активных",
                icon = Icons.Filled.PlayCircle,
                color = Color(0xFF22C55E),
                modifier = Modifier.weight(1f),
                onClick = {}
            )
            AdminStatCard(
                value = stats.pendingVerifications,
                label = "На верификации",
                icon = Icons.Filled.VerifiedUser,
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f),
                onClick = onVerificationsClick
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Управление",
            fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))

        AdminActionRow(
            icon = Icons.Filled.People,
            title = "Пользователи",
            subtitle = "Список, роли, блокировки",
            onClick = onUsersClick
        )
        Spacer(Modifier.height(6.dp))
        AdminActionRow(
            icon = Icons.Filled.VerifiedUser,
            title = "Верификации",
            subtitle = "${stats.pendingVerifications} на рассмотрении",
            onClick = onVerificationsClick
        )
        Spacer(Modifier.height(6.dp))
        AdminActionRow(
            icon = Icons.Filled.SportsScore,
            title = "Заявки на виды спорта",
            subtitle = "Одобрить или отклонить",
            onClick = onSportRequestsClick
        )
        Spacer(Modifier.height(6.dp))
        AdminActionRow(
            icon = Icons.Filled.Settings,
            title = "Настройки платформы",
            subtitle = "Параметры системы",
            onClick = onSettingsClick
        )
    }
}

@Composable
private fun AdminStatCard(
    value: Int,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = CardBg
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text("$value", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(label, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
private fun AdminActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = CardBg
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(40.dp).background(Accent.copy(0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(subtitle, fontSize = 12.sp, color = TextMuted)
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                tint = TextMuted, modifier = Modifier.size(14.dp)
            )
        }
    }
}
