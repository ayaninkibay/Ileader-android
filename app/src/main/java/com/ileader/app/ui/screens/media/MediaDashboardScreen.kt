package com.ileader.app.ui.screens.media

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.screens.athlete.AthleteTournamentDetailScreen
import com.ileader.app.ui.viewmodels.AthleteTournamentsViewModel
import com.ileader.app.ui.viewmodels.MediaDashboardData
import com.ileader.app.ui.viewmodels.MediaDashboardViewModel

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
    val sportKey = tournament.sportName?.lowercase()?.trim() ?: return null
    val list = SPORT_IMAGES[sportKey] ?: return null
    return list[seed.mod(list.size)]
}

private fun statusColor(status: String?): Color = when (status) {
    "registration_open" -> Color(0xFF22C55E)
    "in_progress" -> Color(0xFFF97316)
    "check_in" -> Color(0xFF3B82F6)
    "completed" -> Color(0xFFE53535)
    else -> Color.White.copy(alpha = 0.6f)
}

@Composable
fun MediaDashboardScreen(
    user: User,
    onNavigate: (String) -> Unit = {}
) {
    val viewModel: MediaDashboardViewModel = viewModel()
    val detailViewModel: AthleteTournamentsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    var selectedTournamentId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user.id) {
        viewModel.load(user.id)
        detailViewModel.load(user.id)
    }

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
        is UiState.Success -> DashboardContent(
            user, s.data, onNavigate,
            onTournamentClick = { selectedTournamentId = it }
        )
    }
}

@Composable
private fun DashboardContent(
    user: User,
    data: MediaDashboardData,
    onNavigate: (String) -> Unit,
    onTournamentClick: (String) -> Unit = {}
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
            // -- HEADER --
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
                            "Медиа-панель",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkTheme.TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // -- STATS ROW --
            FadeIn(visible, 150) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MediaStatItem(Modifier.weight(1f), "${data.accreditationStats.accepted}", "Аккредитаций", Icons.Default.Badge)
                    MediaStatItem(Modifier.weight(1f), "${data.articleStats.published}", "Статей", Icons.AutoMirrored.Filled.Article)
                    MediaStatItem(Modifier.weight(1f), "${data.articleStats.totalViews}", "Просмотров", Icons.Default.Visibility)
                }
            }

            Spacer(Modifier.height(28.dp))

            // -- HERO CARD -- ближайший турнир --
            FadeIn(visible, 250) {
                val hero = data.upcomingTournaments.firstOrNull()
                if (hero != null) {
                    MediaHeroTournamentCard(
                        tournament = hero,
                        onClick = { onTournamentClick(hero.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // -- ПРЕДСТОЯЩИЕ ТУРНИРЫ -- горизонтальный скролл --
            FadeIn(visible, 350) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Предстоящие турниры",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary
                    )
                    TextButton(onClick = { onNavigate("media/tournaments") }, contentPadding = PaddingValues(0.dp)) {
                        Text("Все", fontSize = 13.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = DarkTheme.TextSecondary)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (data.upcomingTournaments.isEmpty()) {
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
                        data.upcomingTournaments.forEachIndexed { i, t ->
                            MediaTournamentScrollCard(t, seed = i + 1, onClick = { onTournamentClick(t.id) })
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // -- ПУБЛИКАЦИИ --
            FadeIn(visible, 500) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Публикации",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary
                    )
                    TextButton(onClick = { onNavigate("media/content") }, contentPadding = PaddingValues(0.dp)) {
                        Text("Все", fontSize = 13.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = DarkTheme.TextSecondary)
                    }
                }
                Spacer(Modifier.height(12.dp))

                if (data.recentArticles.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        EmptyState("Нет статей", "Создавайте статьи на сайте ileader.kz")
                    }
                } else {
                    Column(
                        Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        data.recentArticles.forEach { article ->
                            ArticlePreviewItem(article)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // -- СТАТИСТИКА КОНТЕНТА --
            FadeIn(visible, 650) {
                Text(
                    "Статистика контента",
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
                    Row(
                        Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ContentStatColumn("${data.articleStats.totalViews}", "Просмотров", Modifier.weight(1f))
                        ContentStatColumn("${data.articleStats.published}", "Опубликовано", Modifier.weight(1f))
                        ContentStatColumn("${data.articleStats.drafts}", "Черновиков", Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // -- БЫСТРЫЕ ДЕЙСТВИЯ --
            FadeIn(visible, 750) {
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
                    MediaQuickActionCard(Modifier.weight(1f), Icons.AutoMirrored.Filled.Article, "Мои статьи") { onNavigate("media/content") }
                    MediaQuickActionCard(Modifier.weight(1f), Icons.Default.Newspaper, "Аккредитация") { onNavigate("media/tournaments") }
                    MediaQuickActionCard(Modifier.weight(1f), Icons.Default.EmojiEvents, "Турниры") { onNavigate("media/tournaments") }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ======================================================
// COMPONENTS
// ======================================================

@Composable
private fun MediaStatItem(modifier: Modifier, value: String, label: String, icon: ImageVector) {
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
private fun MediaHeroTournamentCard(tournament: TournamentWithCountsDto, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
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
                    "Ближайший турнир",
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
private fun MediaTournamentScrollCard(tournament: TournamentWithCountsDto, seed: Int = 0, onClick: () -> Unit = {}) {
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
        // Dark gradient overlay
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
                    tournamentStatusLabel(tournament.status),
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
private fun ArticlePreviewItem(article: ArticleDto) {
    DarkCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AccentIconBox(Icons.AutoMirrored.Filled.Article)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(article.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text("${article.category ?: "Статья"} · ${article.views} просм.",
                    fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp), DarkTheme.TextMuted)
        }
    }
}

@Composable
private fun ContentStatColumn(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DarkTheme.TextPrimary,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = DarkTheme.TextSecondary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MediaQuickActionCard(modifier: Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
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
