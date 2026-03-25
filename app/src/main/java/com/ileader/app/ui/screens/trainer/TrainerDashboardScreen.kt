package com.ileader.app.ui.screens.trainer

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
import com.ileader.app.data.models.Tournament
import com.ileader.app.data.models.TournamentStatus
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.TrainerDashboardViewModel
import com.ileader.app.ui.viewmodels.getTeamStats

private val BASE = "https://ileader.kz/img"
private val UNSPLASH = "https://images.unsplash.com"

private val SPORT_IMAGES_DASH = mapOf(
    "картинг" to listOf(
        "$BASE/karting/karting-01-1280x719.jpeg",
        "$BASE/karting/karting-04-1280x853.jpeg",
        "$BASE/karting/karting-05-1280x719.jpeg",
        "$BASE/karting/karting-07-1280x853.jpeg",
        "$BASE/karting/karting-12-1280x853.jpeg",
    ),
    "стрельба" to listOf(
        "$BASE/shooting/shooting-01-1280x853.jpeg",
        "$BASE/shooting/shooting-02-1280x853.jpeg",
        "$BASE/shooting/shooting-04-1280x853.jpeg",
        "$BASE/shooting/shooting-05-1280x853.jpeg",
    ),
    "лёгкая атлетика" to listOf(
        "$UNSPLASH/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1552674605-db6ffd4facb5?w=1280&q=80&fit=crop",
    ),
    "легкая атлетика" to listOf(
        "$UNSPLASH/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1552674605-db6ffd4facb5?w=1280&q=80&fit=crop",
    ),
)

private fun dashSportImageUrl(sportName: String, seed: Int = 0): String? {
    val key = sportName.lowercase().trim()
    val list = SPORT_IMAGES_DASH[key] ?: return null
    return list[seed.mod(list.size)]
}

private fun dashTournamentImageUrl(tournament: Tournament, seed: Int = 0): String? =
    dashSportImageUrl(tournament.sportName, seed)
        ?: tournament.imageUrl.takeIf { !it.isNullOrEmpty() }

@Composable
fun TrainerDashboardScreen(
    user: User,
    onNavigateToTeam: () -> Unit = {},
    onNavigateToTournaments: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {}
) {
    val viewModel: TrainerDashboardViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> {
            val data = s.data
            val teams = data.teams
            if (teams.isEmpty()) {
                EmptyState("Нет команд")
                return
            }
            var selectedTeamIndex by remember { mutableIntStateOf(0) }
            val selectedTeam = teams[selectedTeamIndex.coerceIn(0, teams.lastIndex)]
            val teamStats = getTeamStats(selectedTeam)
            val upcomingTournaments = data.tournaments.filter {
                it.status == TournamentStatus.REGISTRATION_OPEN || it.status == TournamentStatus.IN_PROGRESS
            }.filter { it.sportId == selectedTeam.sportId }.take(5)

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
                                    "Твоя команда",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DarkTheme.TextPrimary,
                                    letterSpacing = (-0.5).sp
                                )
                            }
                            UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── TEAM SELECTOR ──
                    if (teams.size > 1) {
                        FadeIn(visible, 100) {
                            var expanded by remember { mutableStateOf(false) }
                            DarkCard(Modifier.padding(horizontal = 20.dp)) {
                                Row(
                                    Modifier.clickable { expanded = true }.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AccentIconBox(Icons.Default.Groups)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(selectedTeam.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                                        Text("${selectedTeam.sportName} · ${selectedTeam.ageCategory}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                                    }
                                    Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(20.dp), DarkTheme.TextMuted)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    teams.forEachIndexed { index, team ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(team.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                    Text("${team.sportName} · ${team.ageCategory}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                                                }
                                            },
                                            onClick = { selectedTeamIndex = index; expanded = false },
                                            leadingIcon = { if (index == selectedTeamIndex) Icon(Icons.Default.Check, null, tint = DarkTheme.Accent) }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // ── STATS ROW ──
                    FadeIn(visible, 150) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashStatItem(Modifier.weight(1f), teamStats.athleteCount.toString(), "Спортсмены", Icons.Default.People)
                            DashStatItem(Modifier.weight(1f), teamStats.totalWins.toString(), "Победы", Icons.Default.Star)
                            DashStatItem(Modifier.weight(1f), teamStats.avgRating.toString(), "Рейтинг", Icons.Default.TrendingUp)
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── HERO CARD — ближайший турнир ──
                    FadeIn(visible, 250) {
                        val hero = upcomingTournaments.firstOrNull()
                        if (hero != null) {
                            HeroTournamentCard(
                                tournament = hero,
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
                        if (upcomingTournaments.drop(1).isEmpty()) {
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
                                upcomingTournaments.drop(1).forEachIndexed { i, t ->
                                    TournamentScrollCard(t, seed = i + 1)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── СОСТАВ КОМАНДЫ ──
                    FadeIn(visible, 450) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Состав команды",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTheme.TextPrimary
                            )
                            TextButton(onClick = onNavigateToTeam, contentPadding = PaddingValues(0.dp)) {
                                Text("Все", fontSize = 13.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = DarkTheme.TextSecondary)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Column(
                            Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedTeam.members.take(3).forEach { athlete ->
                                Surface(
                                    Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = DarkTheme.CardBg
                                ) {
                                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(DarkTheme.AccentSoft),
                                            Alignment.Center
                                        ) {
                                            Text(athlete.name.first().toString(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.Accent)
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(athlete.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Spacer(Modifier.height(3.dp))
                                            Text("${athlete.tournaments} турн. · ${athlete.wins} побед", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                                        }
                                        Surface(shape = RoundedCornerShape(10.dp), color = DarkTheme.AccentSoft) {
                                            Text(
                                                "${athlete.rating}",
                                                Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = DarkTheme.Accent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── БЫСТРЫЕ ДЕЙСТВИЯ ──
                    FadeIn(visible, 550) {
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
                            QuickActionCard(Modifier.weight(1f), Icons.Default.PersonAdd, "Пригласить", onNavigateToTeam)
                            QuickActionCard(Modifier.weight(1f), Icons.Default.AppRegistration, "Регистрация", onNavigateToTournaments)
                            QuickActionCard(Modifier.weight(1f), Icons.Default.BarChart, "Статистика", onNavigateToStatistics)
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

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
private fun HeroTournamentCard(tournament: Tournament, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        val heroImage = dashTournamentImageUrl(tournament)
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
private fun TournamentScrollCard(tournament: Tournament, seed: Int = 0) {
    Box(
        Modifier
            .width(180.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        val scrollImage = dashTournamentImageUrl(tournament, seed)
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
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    tournament.status.displayName,
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
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
