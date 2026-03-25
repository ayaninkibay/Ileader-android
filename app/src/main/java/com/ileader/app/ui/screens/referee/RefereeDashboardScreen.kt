package com.ileader.app.ui.screens.referee

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
import com.ileader.app.ui.components.*
import com.ileader.app.ui.screens.athlete.AthleteTournamentDetailScreen
import com.ileader.app.ui.viewmodels.AthleteTournamentsViewModel
import com.ileader.app.ui.viewmodels.RefereeDashboardViewModel

// ── Sport images ──

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

private fun tournamentImageUrl(tournament: RefereeTournament, seed: Int = 0): String? {
    val key = tournament.sport.lowercase().trim()
    val list = SPORT_IMAGES[key] ?: return null
    return list[seed.mod(list.size)]
}

@Composable
private fun statusColor(status: TournamentStatus): Color = tournamentStatusColor(status)

@Composable
fun RefereeDashboardScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    val viewModel: RefereeDashboardViewModel = viewModel()
    val detailViewModel: AthleteTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var selectedTournamentId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user.id) {
        viewModel.load(user.id)
        detailViewModel.load(user.id)
    }

    // Tournament detail screen
    selectedTournamentId?.let { id ->
        AthleteTournamentDetailScreen(
            tournamentId = id,
            user = user,
            viewModel = detailViewModel,
            onBack = { selectedTournamentId = null },
            onShowQrTicket = { _, _ -> }
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
                data = s.data,
                onNavigate = onNavigate,
                onTournamentClick = { selectedTournamentId = it }
            )
        }
    }
}

@Composable
private fun DashboardContent(
    user: User,
    data: com.ileader.app.ui.viewmodels.RefereeDashboardData,
    onNavigate: (String) -> Unit,
    onTournamentClick: (String) -> Unit = {}
) {
    val stats = data.stats
    val pendingInvites = data.pendingInvites
    val activeTournaments = data.activeTournaments
    val upcomingTournaments = data.upcomingTournaments
    val calendarTournaments = data.calendarTournaments
    val allTournaments = activeTournaments + upcomingTournaments

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
                            "Твои назначения",
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
                    DashStatItem(Modifier.weight(1f), "${stats.totalTournaments}", "Турниров", Icons.Default.Gavel)
                    DashStatItem(Modifier.weight(1f), "${stats.thisMonth}", "В месяце", Icons.Default.SportsScore)
                    DashStatItem(Modifier.weight(1f), "${stats.totalViolations}", "Нарушений", Icons.Default.Shield)
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── HERO CARD — next assignment ──
            FadeIn(visible, 250) {
                val hero = calendarTournaments.firstOrNull()
                if (hero != null) {
                    HeroTournamentCard(
                        tournament = hero,
                        onClick = { onTournamentClick(hero.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── ASSIGNED TOURNAMENTS — horizontal scroll ──
            FadeIn(visible, 350) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Назначения",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary
                    )
                    TextButton(onClick = { onNavigate("referee/tournaments") }, contentPadding = PaddingValues(0.dp)) {
                        Text("Все", fontSize = 13.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = DarkTheme.TextSecondary)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (allTournaments.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        EmptyState("Нет назначений")
                    }
                } else {
                    Row(
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        allTournaments.forEachIndexed { i, t ->
                            TournamentScrollCard(t, seed = i + 1, onClick = { onTournamentClick(t.id) })
                        }
                    }
                }
            }

            // ── INCOMING INVITES ──
            if (pendingInvites.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                FadeIn(visible, 450) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Входящие приглашения",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTheme.TextPrimary
                        )
                        TextButton(onClick = { onNavigate("referee/requests") }, contentPadding = PaddingValues(0.dp)) {
                            Text("Все", fontSize = 13.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = DarkTheme.TextSecondary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Column(
                        Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pendingInvites.take(2).forEach { invite ->
                            InviteCard(invite)
                        }
                    }
                }
            }

            // ── CALENDAR ──
            Spacer(Modifier.height(28.dp))
            FadeIn(visible, 550) {
                Text(
                    "Календарь назначений",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(12.dp))
                val upcoming = calendarTournaments.take(3)
                if (upcoming.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        EmptyState("Нет предстоящих назначений")
                    }
                } else {
                    Column(
                        Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        upcoming.forEach { t ->
                            CalendarItem(t, onClick = { onTournamentClick(t.id) })
                        }
                    }
                }
            }

            // ── QUICK ACTIONS ──
            Spacer(Modifier.height(28.dp))
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
                    QuickActionCard(Modifier.weight(1f), Icons.Default.EmojiEvents, "Турниры") { onNavigate("referee/tournaments") }
                    QuickActionCard(Modifier.weight(1f), Icons.Default.Mail, "Заявки") { onNavigate("referee/requests") }
                    QuickActionCard(Modifier.weight(1f), Icons.Default.Shield, "Нарушения") { onNavigate("referee/violations") }
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
private fun DashStatItem(modifier: Modifier, value: String, label: String, icon: ImageVector) {
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
                letterSpacing = (-0.5).sp
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
private fun HeroTournamentCard(tournament: RefereeTournament, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
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
        // Dark gradient overlay
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

        // Content
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
                    "Следующее назначение",
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
                        Text(tournament.date, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.SportsSoccer, null, Modifier.size(14.dp), Color.White.copy(alpha = 0.8f))
                        Text(tournament.sport, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentScrollCard(tournament: RefereeTournament, seed: Int = 0, onClick: () -> Unit = {}) {
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
        // Dark overlay
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
            val scrollStatusColor = statusColor(tournament.status)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = scrollStatusColor.copy(alpha = 0.2f)
            ) {
                Text(
                    tournament.status.displayName,
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = scrollStatusColor
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
                    tournament.date,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun InviteCard(invite: RefereeInvite) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DarkTheme.CardBg,
        border = DarkTheme.cardBorderStroke
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(Icons.Default.Mail)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(invite.tournamentName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                Spacer(Modifier.height(3.dp))
                Text("${invite.sportName} \u00b7 ${invite.role.label}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            StatusBadge("Новое")
        }
    }
}

@Composable
private fun CalendarItem(tournament: RefereeTournament, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DarkTheme.CardBg,
        border = DarkTheme.cardBorderStroke,
        onClick = onClick
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SoftIconBox(Icons.Default.DateRange)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tournament.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text("${tournament.date} \u00b7 ${tournament.location}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            val chipColor = statusColor(tournament.status)
            StatusBadge(tournament.status.displayName, chipColor)
        }
    }
}

@Composable
private fun QuickActionCard(modifier: Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
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
