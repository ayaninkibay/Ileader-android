package com.ileader.app.ui.screens.athlete

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
import coil.compose.AsyncImage
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AthleteDashboardViewModel
import com.ileader.app.ui.viewmodels.AthleteTournamentsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

private val BASE = "https://ileader.kz/img"
private val UNSPLASH = "https://images.unsplash.com"

private val SPORT_IMAGES_LIST = mapOf(
    "картинг" to listOf(
        "$BASE/karting/karting-01-1280x719.jpeg",
        "$BASE/karting/karting-04-1280x853.jpeg",
        "$BASE/karting/karting-05-1280x719.jpeg",
        "$BASE/karting/karting-07-1280x853.jpeg",
        "$BASE/karting/karting-12-1280x853.jpeg",
        "$BASE/karting/karting-13-1280x853.jpeg",
        "$BASE/karting/karting-15-1280x853.jpeg",
        "$BASE/karting/karting-16-1280x853.jpeg",
        "$BASE/karting/karting-18-1280x852.jpeg",
        "$BASE/karting/karting-20-1280x853.jpeg",
    ),
    "стрельба" to listOf(
        "$BASE/shooting/shooting-01-1280x853.jpeg",
        "$BASE/shooting/shooting-02-1280x853.jpeg",
        "$BASE/shooting/shooting-04-1280x853.jpeg",
        "$BASE/shooting/shooting-05-1280x853.jpeg",
        "$BASE/shooting/shooting-06-1280x853.jpeg",
        "$BASE/shooting/shooting-07-1280x853.jpeg",
        "$BASE/shooting/shooting-08-1280x853.jpeg",
        "$BASE/shooting/shooting-09-1280x853.jpeg",
        "$BASE/shooting/shooting-11-1280x853.jpeg",
    ),
    "лёгкая атлетика" to listOf(
        "$UNSPLASH/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1552674605-db6ffd4facb5?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1571008887538-b36bb32f4571?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1530549387789-4c1017266635?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1541534741688-6078c6bfb5c5?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1476480862126-209bfaa8edc8?w=1280&q=80&fit=crop",
    ),
    "легкая атлетика" to listOf(
        "$UNSPLASH/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1552674605-db6ffd4facb5?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1571008887538-b36bb32f4571?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1530549387789-4c1017266635?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1541534741688-6078c6bfb5c5?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1476480862126-209bfaa8edc8?w=1280&q=80&fit=crop",
    ),
)

private fun sportImageUrl(sportName: String, seed: Int = 0): String? {
    val key = sportName.lowercase().trim()
    val list = SPORT_IMAGES_LIST[key] ?: return null
    return list[seed.mod(list.size)]
}

private fun tournamentImageUrl(tournament: Tournament, seed: Int = 0): String? =
    sportImageUrl(tournament.sportName, seed)
        ?: tournament.imageUrl.takeIf { !it.isNullOrEmpty() }

@Composable
fun AthleteDashboardScreen(
    user: User,
    onNavigateToTournaments: () -> Unit = {},
    onNavigateToResults: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {}
) {
    val viewModel: AthleteDashboardViewModel = viewModel()
    val tournamentsViewModel: AthleteTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    var selectedTournamentId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    // Детальный экран турнира
    selectedTournamentId?.let { id ->
        AthleteTournamentDetailScreen(
            tournamentId = id,
            user = user,
            viewModel = tournamentsViewModel,
            onBack = { selectedTournamentId = null },
            onShowQrTicket = { _, _ -> }
        )
        return
    }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen(LoadingVariant.DASHBOARD)
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> DashboardContent(
            user, s.data.stats, s.data.upcoming, s.data.recentResults,
            s.data.leaderboard, onNavigateToTournaments, onNavigateToResults, onNavigateToGoals,
            onTournamentClick = { selectedTournamentId = it }
        )
    }
}

@Composable
private fun DashboardContent(
    user: User,
    stats: AthleteStats,
    upcoming: List<Tournament>,
    recentResults: List<TournamentResult>,
    leaderboard: List<Triple<String, Int, Int>>,
    onNavigateToTournaments: () -> Unit,
    onNavigateToResults: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onTournamentClick: (String) -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val Bg = DarkTheme.Bg
    val Accent = DarkTheme.Accent
    val AccentDark = DarkTheme.AccentDark

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
                            "Твои турниры",
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
                    DashStatItem(Modifier.weight(1f), stats.rating.toString(), "Рейтинг", Icons.Default.Star)
                    DashStatItem(Modifier.weight(1f), stats.wins.toString(), "Победы", Icons.Default.EmojiEvents)
                    DashStatItem(Modifier.weight(1f), stats.totalTournaments.toString(), "Турниры", Icons.Default.SportsSoccer)
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── HERO CARD — ближайший турнир ──
            FadeIn(visible, 250) {
                val hero = upcoming.firstOrNull()
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

            // ── БЛИЖАЙШИЕ ТУРНИРЫ — горизонтальный скролл ──
            FadeIn(visible, 350) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Предстоящие",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary
                    )
                    TextButton(onClick = onNavigateToTournaments, contentPadding = PaddingValues(0.dp)) {
                        Text("Все", fontSize = 13.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = DarkTheme.TextSecondary)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (upcoming.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        EmptyState("Нет предстоящих турниров")
                    }
                } else {
                    Row(
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        upcoming.forEachIndexed { i, t -> TournamentScrollCard(t, seed = i + 1, onClick = { onTournamentClick(t.id) }) }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── ПОСЛЕДНИЕ РЕЗУЛЬТАТЫ ──
            FadeIn(visible, 450) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Последние результаты",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary
                    )
                    TextButton(onClick = onNavigateToResults, contentPadding = PaddingValues(0.dp)) {
                        Text("Все", fontSize = 13.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = DarkTheme.TextSecondary)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (recentResults.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        EmptyState("Нет результатов")
                    }
                } else {
                    Column(
                        Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentResults.take(3).forEach { r ->
                            ResultCard(r, onResultClick = { onTournamentClick(r.tournamentId) })
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── ТАБЛИЦА ЛИДЕРОВ ──
            FadeIn(visible, 550) {
                Text(
                    "Таблица лидеров",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = DarkTheme.CardBg
                ) {
                    Column(
                        Modifier.padding(vertical = 8.dp)
                    ) {
                        leaderboard.forEachIndexed { i, (name, pts, rank) ->
                            LeaderRow(rank, name, pts, isCurrent = name == user.name || name == user.displayName)
                            if (i < leaderboard.lastIndex) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .height(0.5.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(DarkTheme.CardBorder.copy(alpha = 0.5f))
                                )
                            }
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
                    QuickActionCard(Modifier.weight(1f), Icons.Default.Search, "Турниры", onNavigateToTournaments)
                    QuickActionCard(Modifier.weight(1f), Icons.Default.Leaderboard, "Результаты", onNavigateToResults)
                    QuickActionCard(Modifier.weight(1f), Icons.Default.Flag, "Цели", onNavigateToGoals)
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
private fun HeroTournamentCard(tournament: Tournament, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1a0a0a))
            .clickable(onClick = onClick)
    ) {
        // Фото турнира (своё или по виду спорта) или тёмный фолбэк
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
        // Тёмный оверлей для читаемости текста
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
                    "Следующий турнир",
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(sportIcon(tournament.sportName), null, Modifier.size(14.dp), Color.White.copy(alpha = 0.8f))
                        Text(tournament.sportName, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentScrollCard(tournament: Tournament, seed: Int = 0, onClick: () -> Unit = {}) {
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
        // Тёмный оверлей
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
            val scrollStatusColor = when (tournament.status) {
                TournamentStatus.REGISTRATION_OPEN -> Color(0xFF22C55E)
                TournamentStatus.IN_PROGRESS -> Color(0xFFF97316)
                TournamentStatus.CHECK_IN -> Color(0xFF3B82F6)
                TournamentStatus.COMPLETED -> Color(0xFFE53535)
                else -> Color.White
            }
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
                    formatShortDate(tournament.startDate),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun ResultCard(result: TournamentResult, onResultClick: () -> Unit = {}) {
    val isTop = result.position <= 3
    Surface(
        Modifier.fillMaxWidth().clickable { onResultClick() },
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
                    .background(if (isTop) DarkTheme.AccentSoft else DarkTheme.CardBorder.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#${result.position}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isTop) DarkTheme.Accent else DarkTheme.TextSecondary
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    result.tournamentName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTheme.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "${formatShortDate(result.date)} · ${result.sportName}",
                    fontSize = 13.sp,
                    color = DarkTheme.TextSecondary
                )
            }
            Surface(shape = RoundedCornerShape(10.dp), color = DarkTheme.AccentSoft) {
                Text(
                    "+${result.points}",
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
private fun LeaderRow(rank: Int, name: String, points: Int, isCurrent: Boolean) {
    val medalEmoji = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> null
    }

    Row(
        Modifier
            .fillMaxWidth()
            .background(if (isCurrent) DarkTheme.AccentSoft else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (medalEmoji != null) {
            Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                Text(medalEmoji, fontSize = 22.sp)
            }
        } else {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(DarkTheme.CardBorder.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$rank",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkTheme.TextMuted
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                name,
                fontSize = 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = if (isCurrent) DarkTheme.Accent else DarkTheme.TextPrimary,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (isCurrent) {
                Spacer(Modifier.width(6.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = DarkTheme.Accent) {
                    Text(
                        "Вы",
                        Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        Text(
            "$points",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCurrent) DarkTheme.Accent else DarkTheme.TextSecondary
        )
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

@Composable
fun TournamentStatusBadge(status: TournamentStatus) {
    val color = when (status) {
        TournamentStatus.REGISTRATION_OPEN -> Color(0xFF22C55E)
        TournamentStatus.IN_PROGRESS -> Color(0xFFF97316)
        TournamentStatus.CHECK_IN -> Color(0xFF3B82F6)
        TournamentStatus.COMPLETED -> Color(0xFFE53535)
        else -> DarkTheme.TextMuted
    }
    StatusBadge(status.displayName, color)
}
