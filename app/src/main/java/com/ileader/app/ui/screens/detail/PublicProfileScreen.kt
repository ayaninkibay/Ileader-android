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
import androidx.compose.ui.graphics.StrokeCap
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
import com.ileader.app.ui.viewmodels.SportViewModel

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
            Column(Modifier.fillMaxSize().background(Bg)) { BackHeader("Профиль", onBack); LoadingScreen() }
        }
        is UiState.Error -> {
            Column(Modifier.fillMaxSize().background(Bg)) { BackHeader("Профиль", onBack); ErrorScreen(state.message, onRetry = { viewModel.load(userId) }) }
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
    val isReferee = data.refereeAssignments.isNotEmpty() && results.isEmpty()

    val primarySportName = remember(sports) { sports.firstOrNull()?.sports?.name ?: "" }
    val bannerUrl = remember(primarySportName) {
        if (primarySportName.isNotEmpty()) sportImageUrl(primarySportName) else null
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())
    ) {
        // ══════════════════════════════════════
        // HERO BANNER
        // ══════════════════════════════════════
        Box(modifier = Modifier.fillMaxWidth()) {
            // Hero background — always dark so white text is readable in both themes
            val heroShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            if (bannerUrl != null) {
                AsyncImage(bannerUrl, null, Modifier.fillMaxWidth().height(240.dp)
                    .clip(heroShape), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxWidth().height(240.dp).clip(heroShape)
                    .background(Color.Black.copy(0.7f)))
            } else {
                Box(Modifier.fillMaxWidth().height(240.dp).clip(heroShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF1a1a1a), Color(0xFF2d1a1a)))))
            }

            // Back button
            Box(
                Modifier.statusBarsPadding().padding(16.dp).size(40.dp)
                    .clip(CircleShape).background(Color.White.copy(0.15f)).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Avatar + name — always white text on dark hero
            Column(
                Modifier.fillMaxWidth().statusBarsPadding().padding(top = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val sColor = if (primarySportName.isNotEmpty()) sportColor(primarySportName) else Accent
                val borderColors = listOf(sColor, sColor.copy(0.5f), Accent, sColor)
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(112.dp).background(Brush.sweepGradient(borderColors), CircleShape))
                    Box(Modifier.size(106.dp).clip(CircleShape).background(Color(0xFF1a1a1a)))
                    Box(Modifier.size(100.dp).clip(CircleShape).background(Color(0xFF252525)), contentAlignment = Alignment.Center) {
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
                Text(profile.name ?: "Пользователь", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = Color.White)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoleBadge(role = user.role)
                    if (!profile.city.isNullOrEmpty()) {
                        Text("·", fontSize = 14.sp, color = Color.White.copy(0.7f))
                        Text(profile.city, fontSize = 13.sp, color = Color.White.copy(0.7f))
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // SPORT CHIPS
        // ══════════════════════════════════════
        if (sports.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sports.forEach { sport -> SportTag(sport.sports?.name ?: "") }
            }
        }

        // ══════════════════════════════════════
        // STATS
        // ══════════════════════════════════════
        Spacer(Modifier.height(16.dp))
        if (isReferee) {
            // Referee stats
            val totalJudged = data.refereeAssignments.size
            val completed = data.refereeAssignments.count { it.tournaments?.status == "completed" }
            val active = data.refereeAssignments.count { it.tournaments?.status in listOf("registration_open", "in_progress", "check_in") }

            Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp), color = CardBg) {
                Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                    StatItem(Icons.Outlined.Gavel, totalJudged, "Турниров", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    StatItem(Icons.Outlined.CheckCircle, completed, "Отсужено", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    StatItem(Icons.Outlined.PlayArrow, active, "Активных", Modifier.weight(1f))
                }
            }
        } else if (stats.isNotEmpty()) {
            // Athlete stats
            val totalTournaments = stats.sumOf { it.tournaments }
            val totalWins = stats.sumOf { it.wins }
            val topRating = stats.maxOf { it.rating }

            Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp), color = CardBg) {
                Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                    StatItem(Icons.Outlined.EmojiEvents, totalTournaments, "Турниры", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    StatItem(Icons.Outlined.MilitaryTech, totalWins, "Победы", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    StatItem(Icons.Outlined.Leaderboard, topRating, "Рейтинг", Modifier.weight(1f))
                }
            }
        }

        // ══════════════════════════════════════
        // BIO
        // ══════════════════════════════════════
        if (!profile.bio.isNullOrEmpty()) {
            Spacer(Modifier.height(16.dp))
            Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp), color = CardBg) {
                Column(Modifier.padding(16.dp)) {
                    Text("О себе", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(profile.bio, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)
                }
            }
        }

        // ══════════════════════════════════════
        // REFEREE TOURNAMENTS (right after bio for referees)
        // ══════════════════════════════════════
        if (isReferee && data.refereeAssignments.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("Назначенные турниры")
                Spacer(Modifier.height(10.dp))
                data.refereeAssignments.sortedByDescending { it.tournaments?.startDate }.forEach { a ->
                    val t = a.tournaments
                    Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp), RoundedCornerShape(14.dp), CardBg) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Sport icon
                            Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(TextMuted.copy(0.08f)), contentAlignment = Alignment.Center) {
                                Icon(sportIcon(t?.sports?.name ?: ""), null, Modifier.size(20.dp), tint = TextMuted)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t?.name ?: "—", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    t?.sports?.name?.let { Text(it, fontSize = 12.sp, color = TextMuted) }
                                    t?.startDate?.let { date ->
                                        val parts = date.take(10).split("-")
                                        if (parts.size >= 3) {
                                            val months = listOf("", "янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
                                            Text(" · ${parts[2].toIntOrNull() ?: 0} ${months.getOrElse(parts[1].toIntOrNull() ?: 0) { "" }}", fontSize = 12.sp, color = TextMuted)
                                        }
                                    }
                                }
                            }
                            // Role + status
                            Column(horizontalAlignment = Alignment.End) {
                                val rl = when (a.role) { "head_referee" -> "Главный"; "assistant" -> "Помощник"; else -> "Судья" }
                                Surface(shape = RoundedCornerShape(50), color = Accent.copy(0.1f)) {
                                    Text(rl, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                                }
                                Spacer(Modifier.height(2.dp))
                                val statusLabel = when (t?.status) { "completed" -> "Завершён"; "in_progress" -> "Идёт"; "registration_open" -> "Регистрация"; else -> "" }
                                if (statusLabel.isNotEmpty()) {
                                    Text(statusLabel, fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // UPCOMING TOURNAMENTS (athlete only)
        // ══════════════════════════════════════
        if (!isReferee && data.upcomingTournaments.isNotEmpty()) {
            val upcoming = data.upcomingTournaments.filter { it.status in listOf("registration_open", "in_progress", "check_in") }
            if (upcoming.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Column {
                    Row(Modifier.padding(horizontal = 16.dp)) { SectionHeader("Ближайшие турниры") }
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(upcoming.take(5)) { t ->
                            UpcomingTournamentCard(t)
                        }
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // RESULTS (athlete only)
        // ══════════════════════════════════════
        if (!isReferee) {
            Spacer(Modifier.height(20.dp))
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
        // RATING BY SPORT (athlete only)
        // ══════════════════════════════════════
        if (!isReferee && stats.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Column {
                Row(Modifier.padding(horizontal = 16.dp)) { SectionHeader("Рейтинг по спорту") }
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stats) { sportStat -> SportRatingCard(sportStat) }
                }
            }
        }

        // ══════════════════════════════════════
        // TEAM
        // ══════════════════════════════════════
        if (membership != null) {
            Spacer(Modifier.height(20.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("Команда")
                Spacer(Modifier.height(10.dp))
                TeamCard(membership)
            }
        }

        // ══════════════════════════════════════
        // MESSAGE BUTTON (placeholder)
        // ══════════════════════════════════════
        Spacer(Modifier.height(20.dp))
        Surface(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp), color = CardBg
        ) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.ChatBubbleOutline, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Написать", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

// ═══════════════════════════════════════════════════════════
// COMPONENTS
// ═══════════════════════════════════════════════════════════

@Composable
private fun StatItem(icon: ImageVector, value: Int, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text("$value", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text(label, fontSize = 10.sp, color = TextMuted)
    }
}

@Composable
private fun UpcomingTournamentCard(t: TournamentWithCountsDto) {
    val sportName = t.sportName ?: ""
    val imgUrl = t.imageUrl ?: SportViewModel.getFallbackImage(SportDto(id = "", name = sportName))

    Surface(shape = RoundedCornerShape(16.dp), color = CardBg, modifier = Modifier.width(220.dp)) {
        Column {
            Box(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))) {
                if (imgUrl != null) {
                    AsyncImage(imgUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.5f)))))
                } else {
                    Box(Modifier.fillMaxSize().background(Accent.copy(0.3f)))
                }
                // Status badge
                val statusColor = when (t.status) {
                    "in_progress" -> Color(0xFF3B82F6); "registration_open" -> Color(0xFF22C55E)
                    "check_in" -> Color(0xFF8B5CF6); else -> Color(0xFF6B7280)
                }
                val statusLabel = when (t.status) {
                    "in_progress" -> "Идёт"; "registration_open" -> "Регистрация"
                    "check_in" -> "Check-in"; else -> t.status ?: ""
                }
                Surface(
                    Modifier.align(Alignment.TopEnd).padding(8.dp),
                    shape = RoundedCornerShape(50), color = statusColor.copy(0.85f)
                ) {
                    Text(statusLabel, Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 2, lineHeight = 18.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(formatShortDate(t.startDate), fontSize = 12.sp, color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun ResultCard(r: ResultDto) {
    val posEmoji = when (r.position) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "#${r.position}" }
    val sportName = r.tournaments?.sports?.name ?: ""

    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), CardBg) {
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
                    if (sportName.isNotEmpty()) { Text(sportName, fontSize = 12.sp, color = TextMuted); Spacer(Modifier.width(8.dp)) }
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

@Composable
private fun SportRatingCard(stat: UserSportStatsDto) {
    val name = stat.sportName ?: ""
    val progress = (stat.rating.toFloat() / 2000f).coerceIn(0f, 1f)

    Surface(Modifier.width(170.dp), RoundedCornerShape(16.dp), CardBg) {
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
            Spacer(Modifier.height(6.dp))
            // Rating progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = Accent, trackColor = Border.copy(0.2f), strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${stat.tournaments} турн.", fontSize = 12.sp, color = TextMuted)
                Spacer(Modifier.width(8.dp))
                Text("${stat.wins} поб.", fontSize = 12.sp, color = TextMuted)
                if (stat.podiums > 0) {
                    Spacer(Modifier.width(8.dp))
                    Text("${stat.podiums} под.", fontSize = 12.sp, color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun TeamCard(membership: TeamMembershipDto) {
    val team = membership.teams ?: return
    val sportName = team.sports?.name ?: ""
    val roleName = when (membership.role) { "captain" -> "Капитан"; "member" -> "Участник"; "reserve" -> "Запасной"; else -> membership.role ?: "" }

    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), CardBg) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).clip(RoundedCornerShape(14.dp)).background(TextMuted.copy(0.08f)), contentAlignment = Alignment.Center) {
                Icon(sportIcon(sportName), null, Modifier.size(24.dp), tint = TextMuted)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(team.name ?: "Команда", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sportName.isNotEmpty()) { Text(sportName, fontSize = 12.sp, color = TextMuted); Spacer(Modifier.width(8.dp)) }
                    team.city?.let { Text(it, fontSize = 12.sp, color = TextMuted) }
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

@Composable
private fun EmptyCard(text: String, icon: ImageVector) {
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), CardBg) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TextMuted, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, fontSize = 14.sp, color = TextMuted)
        }
    }
}

private fun formatShortDate(dateStr: String?): String {
    if (dateStr == null) return ""
    val parts = dateStr.take(10).split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2].toIntOrNull() ?: return dateStr
    val months = listOf("", "янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
    val month = parts[1].toIntOrNull() ?: return dateStr
    return "$day ${months.getOrElse(month) { "" }}"
}
