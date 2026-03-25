package com.ileader.app.ui.screens.sponsor

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SponsorshipDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.screens.athlete.AthleteTournamentDetailScreen
import com.ileader.app.ui.viewmodels.AthleteTournamentsViewModel
import com.ileader.app.ui.viewmodels.SponsorDashboardViewModel

private val BASE_URL = "https://ileader.kz/img"
private val UNSPLASH = "https://images.unsplash.com"
private val SPORT_IMAGES = mapOf(
    "картинг" to listOf("$BASE_URL/karting/karting-04-1280x853.jpeg", "$BASE_URL/karting/karting-07-1280x853.jpeg"),
    "стрельба" to listOf("$BASE_URL/shooting/shooting-02-1280x853.jpeg", "$BASE_URL/shooting/shooting-04-1280x853.jpeg"),
    "теннис" to listOf("$UNSPLASH/photo-1554068865-24cecd4e34b8?w=1280&q=80&fit=crop"),
    "футбол" to listOf("$UNSPLASH/photo-1431324155629-1a6deb1dec8d?w=1280&q=80&fit=crop"),
    "бокс" to listOf("$UNSPLASH/photo-1549719386-74dfcbf7dbed?w=1280&q=80&fit=crop"),
    "плавание" to listOf("$UNSPLASH/photo-1519315901367-f34ff9154487?w=1280&q=80&fit=crop"),
)

private fun tournamentImageUrl(tournament: TournamentWithCountsDto, seed: Int = 0): String? {
    tournament.imageUrl?.takeIf { it.isNotEmpty() }?.let { return it }
    val list = SPORT_IMAGES[tournament.sportName?.lowercase()?.trim()] ?: return null
    return list[seed.mod(list.size)]
}

@Composable
private fun statusColor(status: String?): Color = tournamentStatusColor(status)

@Composable
fun SponsorDashboardScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    val viewModel: SponsorDashboardViewModel = viewModel()
    val detailViewModel: AthleteTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val appliedTournaments by viewModel.appliedTournaments.collectAsState()
    var selectedTournamentId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user.id) {
        viewModel.load(user.id)
        detailViewModel.load(user.id)
    }

    // Детальный экран турнира
    selectedTournamentId?.let { id ->
        AthleteTournamentDetailScreen(
            tournamentId = id,
            user = user,
            viewModel = detailViewModel,
            onBack = { selectedTournamentId = null }
        )
        return
    }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen(LoadingVariant.DASHBOARD)
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> DarkPullRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh(user.id) }
        ) {
            DashboardContent(
                user = user,
                sponsorships = s.data.sponsorships,
                openTournaments = s.data.openTournaments,
                totalInvested = s.data.totalInvested,
                teamCount = s.data.teamCount,
                tournamentCount = s.data.tournamentCount,
                appliedTournaments = appliedTournaments,
                onNavigate = onNavigate,
                onApplySponsorship = { tournamentId -> viewModel.requestTournamentSponsorship(user.id, tournamentId) },
                onTournamentClick = { selectedTournamentId = it }
            )
        }
    }
}

@Composable
private fun DashboardContent(
    user: User,
    sponsorships: List<SponsorshipDto>,
    openTournaments: List<TournamentWithCountsDto>,
    totalInvested: Double,
    teamCount: Int,
    tournamentCount: Int,
    appliedTournaments: Set<String>,
    onNavigate: (String) -> Unit,
    onApplySponsorship: (String) -> Unit,
    onTournamentClick: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // ── HEADER ──
            FadeIn(visible, 0) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Привет, ${user.displayName.split(" ").firstOrNull() ?: user.displayName}",
                            fontSize = 14.sp,
                            color = DarkTheme.TextMuted,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            "Спонсорство",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkTheme.TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── STATS ROW ──
            FadeIn(visible, 150) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SponsorStatItem(Modifier.weight(1f), SponsorUtils.formatAmount(totalInvested.toLong()), "Инвестировано", Icons.Default.AttachMoney)
                    SponsorStatItem(Modifier.weight(1f), "$teamCount", "Команды", Icons.Default.Groups)
                    SponsorStatItem(Modifier.weight(1f), "$tournamentCount", "Турниры", Icons.Default.EmojiEvents)
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── HERO CARD — первый открытый турнир ──
            FadeIn(visible, 250) {
                val hero = openTournaments.firstOrNull()
                if (hero != null) {
                    SponsorHeroCard(
                        tournament = hero,
                        onClick = { onTournamentClick(hero.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── ОТКРЫТЫЕ ТУРНИРЫ — горизонтальный скролл ──
            FadeIn(visible, 350) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Открыты для спонсорства",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary
                    )
                    TextButton(onClick = { onNavigate("sponsor/tournaments") }, contentPadding = PaddingValues(0.dp)) {
                        Text("Все", fontSize = 13.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = DarkTheme.TextSecondary)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (openTournaments.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        EmptyState("Нет доступных турниров")
                    }
                } else {
                    Row(
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        openTournaments.forEachIndexed { i, t ->
                            SponsorTournamentScrollCard(
                                tournament = t,
                                seed = i + 1,
                                applied = t.id in appliedTournaments,
                                onClick = { onTournamentClick(t.id) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── СПОНСИРУЕМЫЕ КОМАНДЫ ──
            FadeIn(visible, 450) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Спонсируемые команды",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary
                    )
                    TextButton(onClick = { onNavigate("sponsor/teams") }, contentPadding = PaddingValues(0.dp)) {
                        Text("Все", fontSize = 13.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = DarkTheme.TextSecondary)
                    }
                }
                Spacer(Modifier.height(12.dp))
                val teams = sponsorships.filter { it.teamId != null }
                if (teams.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        EmptyState("Нет спонсируемых команд")
                    }
                } else {
                    Column(
                        Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        teams.forEach { item ->
                            SponsoredItemCard(item, isTeam = true)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── СПОНСИРУЕМЫЕ ТУРНИРЫ ──
            FadeIn(visible, 550) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Спонсируемые турниры",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary
                    )
                    TextButton(onClick = { onNavigate("sponsor/tournaments") }, contentPadding = PaddingValues(0.dp)) {
                        Text("Все", fontSize = 13.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = DarkTheme.TextSecondary)
                    }
                }
                Spacer(Modifier.height(12.dp))
                val tournaments = sponsorships.filter { it.tournamentId != null }
                if (tournaments.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        EmptyState("Нет спонсируемых турниров")
                    }
                } else {
                    Column(
                        Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tournaments.forEach { item ->
                            SponsoredItemCard(item, isTeam = false)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── БЫСТРЫЕ ДЕЙСТВИЯ ──
            FadeIn(visible, 650) {
                Text(
                    "Быстрые действия",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SponsorQuickAction(Modifier.weight(1f), Icons.Default.Search, "Турниры") { onNavigate("sponsor/tournaments") }
                    SponsorQuickAction(Modifier.weight(1f), Icons.Default.Groups, "Команды") { onNavigate("sponsor/teams") }
                    SponsorQuickAction(Modifier.weight(1f), Icons.Default.Analytics, "Статистика") { onNavigate("sponsor/stats") }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ══════════════════════════════════════════════════════
// COMPONENTS
// ══════════════════════════════════════════════════════

@Composable
private fun SponsorStatItem(modifier: Modifier, value: String, label: String, icon: ImageVector) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = DarkTheme.CardBg
    ) {
        Column(
            Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkTheme.AccentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(18.dp), tint = DarkTheme.Accent)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkTheme.TextPrimary,
                letterSpacing = (-0.5).sp,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize = 12.sp,
                color = DarkTheme.TextSecondary,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SponsorHeroCard(tournament: TournamentWithCountsDto, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1a0a0a))
            .clickable(onClick = onClick)
    ) {
        val heroImage = tournamentImageUrl(tournament)
        if (heroImage != null) {
            AsyncImage(
                model = heroImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize().background(Color(0xFF1a0a0a)))
        }
        // Gradient overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    "Открыт для спонсорства",
                    Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Column {
                Text(
                    tournament.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.3).sp
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.size(15.dp), Color.White.copy(alpha = 0.8f))
                        Spacer(Modifier.width(4.dp))
                        Text(formatShortDate(tournament.startDate), fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    if (!tournament.sportName.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(sportIcon(tournament.sportName), null, Modifier.size(14.dp), Color.White.copy(alpha = 0.8f))
                            Text(tournament.sportName, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SponsorTournamentScrollCard(
    tournament: TournamentWithCountsDto,
    seed: Int = 0,
    applied: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        Modifier
            .width(180.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1a0808))
            .clickable(onClick = onClick)
    ) {
        val scrollImage = tournamentImageUrl(tournament, seed)
        if (scrollImage != null) {
            AsyncImage(
                model = scrollImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize().background(Color(0xFF1a0808)))
        }
        // Gradient overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.72f)
                        )
                    )
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val chipColor = if (applied) Color(0xFF3B82F6) else statusColor(tournament.status)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = chipColor.copy(alpha = 0.2f)
            ) {
                Text(
                    if (applied) "Заявка" else SponsorUtils.getStatusLabel(tournament.status ?: ""),
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = chipColor
                )
            }
            Column {
                Text(
                    tournament.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    formatShortDate(tournament.startDate),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun SponsoredItemCard(item: SponsorshipDto, isTeam: Boolean) {
    val name = if (isTeam) item.teams?.name else item.tournaments?.name
    val sport = item.tournaments?.sports?.name ?: ""

    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DarkTheme.CardBg
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkTheme.AccentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isTeam) Icons.Default.Groups else Icons.Default.EmojiEvents,
                    null, Modifier.size(20.dp), tint = DarkTheme.Accent
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    name ?: "",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTheme.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(sport, fontSize = 13.sp, color = DarkTheme.TextSecondary)
            }
            Surface(shape = RoundedCornerShape(10.dp), color = DarkTheme.AccentSoft) {
                Text(
                    SponsorUtils.formatAmount((item.amount ?: 0.0).toLong()),
                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkTheme.Accent
                )
            }
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp), DarkTheme.TextMuted)
        }
    }
}

@Composable
private fun SponsorQuickAction(modifier: Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = DarkTheme.CardBg
    ) {
        Column(
            Modifier
                .border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(vertical = 18.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(DarkTheme.Accent, DarkTheme.AccentDark))),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(22.dp), tint = Color.White)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTheme.TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}
