package com.ileader.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.PublicProfileData
import com.ileader.app.ui.viewmodels.PublicProfileViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

@Composable
fun PublicProfileScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: PublicProfileViewModel = viewModel()
) {
    LaunchedEffect(userId) { viewModel.load(userId) }

    when (val state = viewModel.state) {
        is UiState.Loading -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Профиль", onBack)
                LoadingScreen()
            }
        }
        is UiState.Error -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Профиль", onBack)
                ErrorScreen(state.message, onRetry = { viewModel.load(userId) })
            }
        }
        is UiState.Success -> ProfileContent(data = state.data, onBack = onBack)
    }
}

@Composable
private fun ProfileContent(data: PublicProfileData, onBack: () -> Unit) {
    val profile = data.profile
    val user = profile.toDomain()
    val sports = data.sports
    val stats = data.stats
    val results = data.results
    val membership = data.membership

    val primarySportName = remember(sports) {
        sports.firstOrNull()?.sports?.name ?: ""
    }
    val bannerUrl = remember(primarySportName) {
        if (primarySportName.isNotEmpty()) sportImageUrl(primarySportName) else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // ══════════════════════════════════════
        // HERO BANNER
        // ══════════════════════════════════════
        Box(modifier = Modifier.fillMaxWidth()) {
            if (bannerUrl != null) {
                AsyncImage(
                    model = bannerUrl, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(240.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )
                Box(
                    Modifier.fillMaxWidth().height(240.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(Brush.verticalGradient(listOf(Color.Black.copy(0.3f), Color.Black.copy(0.7f))))
                )
            } else {
                Box(
                    Modifier.fillMaxWidth().height(240.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(Brush.verticalGradient(listOf(Accent.copy(0.9f), Accent.copy(0.4f))))
                )
            }

            // Back button
            Box(
                Modifier.statusBarsPadding().padding(16.dp).size(40.dp)
                    .clip(CircleShape).background(Color.Black.copy(0.3f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Avatar + name overlay
            Column(
                Modifier.fillMaxWidth().statusBarsPadding().padding(top = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with gradient border
                val sColor = if (primarySportName.isNotEmpty()) sportColor(primarySportName) else Accent
                val borderColors = listOf(sColor, sColor.copy(0.5f), Accent, sColor)
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(112.dp).background(Brush.sweepGradient(borderColors), CircleShape))
                    Box(Modifier.size(106.dp).clip(CircleShape).background(Bg))
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(CardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profile.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(profile.avatarUrl, null, Modifier.size(100.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Box(Modifier.size(100.dp).clip(CircleShape).background(Accent), contentAlignment = Alignment.Center) {
                                Text((profile.name ?: "?").take(1).uppercase(), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    profile.name ?: "Пользователь", fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (bannerUrl != null) Color.White else TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleBadge(role = user.role)
                    if (!profile.city.isNullOrEmpty()) {
                        Text("·", fontSize = 14.sp, color = if (bannerUrl != null) Color.White.copy(0.7f) else TextMuted)
                        Text(profile.city, fontSize = 13.sp, color = if (bannerUrl != null) Color.White.copy(0.7f) else TextMuted)
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // SPORT CHIPS
        // ══════════════════════════════════════
        if (sports.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            FadeIn(visible = true, delayMs = 100) {
                Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    sports.forEach { sport ->
                        SportTag(sport.sports?.name ?: "")
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // STAT CARDS
        // ══════════════════════════════════════
        if (stats.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            FadeIn(visible = true, delayMs = 200) {
                val primaryStats = stats.firstOrNull()
                val totalTournaments = stats.sumOf { it.tournaments }
                val totalWins = stats.sumOf { it.wins }
                val topRating = stats.maxOf { it.rating }

                Surface(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp), color = CardBg, shadowElevation = 0.dp
                ) {
                    Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                        StatItem(Icons.Outlined.EmojiEvents, totalTournaments, "Турниры", Modifier.weight(1f))
                        Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                        StatItem(Icons.Outlined.MilitaryTech, totalWins, "Победы", Modifier.weight(1f))
                        Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                        StatItem(Icons.Outlined.Leaderboard, topRating, "Рейтинг", Modifier.weight(1f))
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // BIO
        // ══════════════════════════════════════
        if (!profile.bio.isNullOrEmpty()) {
            Spacer(Modifier.height(16.dp))
            FadeIn(visible = true, delayMs = 250) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp), color = CardBg, shadowElevation = 0.dp
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("О себе", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text(profile.bio, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // RESULTS
        // ══════════════════════════════════════
        Spacer(Modifier.height(20.dp))
        FadeIn(visible = true, delayMs = 350) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("Результаты")
                Spacer(Modifier.height(10.dp))
                if (results.isEmpty()) {
                    EmptyCard("Нет результатов", Icons.Outlined.Scoreboard)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        results.take(5).forEach { r -> ResultCard(r) }
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // RATING BY SPORT
        // ══════════════════════════════════════
        if (stats.size > 0) {
            Spacer(Modifier.height(20.dp))
            FadeIn(visible = true, delayMs = 400) {
                Column {
                    Row(Modifier.padding(horizontal = 16.dp)) {
                        SectionHeader("Рейтинг по спорту")
                    }
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(stats) { sportStat ->
                            SportRatingCard(sportStat)
                        }
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // TEAM
        // ══════════════════════════════════════
        if (membership != null) {
            Spacer(Modifier.height(20.dp))
            FadeIn(visible = true, delayMs = 450) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader("Команда")
                    Spacer(Modifier.height(10.dp))
                    TeamCard(membership)
                }
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

// ═══════════════════════════════════════════════════════════
// STAT ITEM
// ═══════════════════════════════════════════════════════════

@Composable
private fun StatItem(icon: ImageVector, value: Int, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(6.dp))
        Text("$value", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}

// ═══════════════════════════════════════════════════════════
// RESULT CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun ResultCard(r: ResultDto) {
    val posEmoji = when (r.position) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "#${r.position}" }
    val sportName = r.tournaments?.sports?.name ?: ""

    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), CardBg, shadowElevation = 0.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (r.position <= 3) Accent.copy(0.15f) else Border.copy(0.2f)),
                Alignment.Center
            ) {
                Text(posEmoji, fontSize = if (r.position <= 3) 20.sp else 14.sp, fontWeight = FontWeight.Bold,
                    color = if (r.position <= 3) Accent else TextMuted)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(r.tournaments?.name ?: "Турнир", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sportName.isNotEmpty()) {
                        Text(sportName, fontSize = 12.sp, color = TextMuted)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(formatShortDate(r.tournaments?.startDate), fontSize = 12.sp, color = TextMuted)
                }
            }
            if (r.points != null && r.points > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("${r.points}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Accent)
                    Text("очки", fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SPORT RATING CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun SportRatingCard(stat: UserSportStatsDto) {
    val name = stat.sportName ?: ""
    Surface(Modifier.width(160.dp), RoundedCornerShape(16.dp), CardBg, shadowElevation = 0.dp) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(TextMuted.copy(0.08f)), contentAlignment = Alignment.Center) {
                    Icon(sportIcon(name), null, Modifier.size(18.dp), tint = TextMuted)
                }
                Spacer(Modifier.width(10.dp))
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Text("${stat.rating}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${stat.tournaments} турн.", fontSize = 12.sp, color = TextMuted)
                Spacer(Modifier.width(8.dp))
                Text("${stat.wins} поб.", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// TEAM CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun TeamCard(membership: TeamMembershipDto) {
    val team = membership.teams ?: return
    val sportName = team.sports?.name ?: ""
    val roleName = when (membership.role) { "captain" -> "Капитан"; "member" -> "Участник"; "reserve" -> "Запасной"; else -> membership.role ?: "" }

    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), CardBg, shadowElevation = 0.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).clip(RoundedCornerShape(14.dp)).background(TextMuted.copy(0.08f)), contentAlignment = Alignment.Center) {
                Icon(sportIcon(sportName), null, Modifier.size(24.dp), tint = TextMuted)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(team.name ?: "Команда", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sportName.isNotEmpty()) {
                        Text(sportName, fontSize = 12.sp, color = TextMuted)
                        Spacer(Modifier.width(8.dp))
                    }
                    if (team.city != null) Text(team.city, fontSize = 12.sp, color = TextMuted)
                }
            }
            if (roleName.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(50), color = Accent.copy(0.18f)) {
                    Text(roleName, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// EMPTY STATE CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun EmptyCard(text: String, icon: ImageVector) {
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), CardBg, shadowElevation = 0.dp) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TextMuted, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, fontSize = 14.sp, color = TextMuted)
        }
    }
}
