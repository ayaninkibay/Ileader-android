package com.ileader.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TeamMemberDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.TeamDetailViewModel
import com.ileader.app.ui.viewmodels.SportViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

private val teamColor = Color(0xFF3B82F6)

private fun roleLabel(role: String?): String = when (role) {
    "captain" -> "Капитан"; "member" -> "Участник"; "reserve" -> "Резерв"; else -> role ?: ""
}
private fun roleColor(role: String?): Color = when (role) {
    "captain" -> Color(0xFFE53535); "reserve" -> Color(0xFF6B7280); else -> Color(0xFF3B82F6)
}

// ══════════════════════════════════════════════════════════
// Main Screen
// ══════════════════════════════════════════════════════════

@Composable
fun TeamDetailScreen(
    teamId: String,
    onBack: () -> Unit,
    vm: TeamDetailViewModel = viewModel()
) {
    LaunchedEffect(teamId) { vm.load(teamId) }

    when (val s = vm.state) {
        is UiState.Loading -> { Column(Modifier.fillMaxSize().background(Bg)) { BackHeader("Команда", onBack); LoadingScreen() } }
        is UiState.Error -> { Column(Modifier.fillMaxSize().background(Bg)) { BackHeader("Команда", onBack); ErrorScreen(s.message, onRetry = { vm.load(teamId) }) } }
        is UiState.Success -> TeamContent(data = s.data, onBack = onBack)
    }
}

@Composable
private fun TeamContent(
    data: com.ileader.app.ui.viewmodels.TeamDetailData,
    onBack: () -> Unit
) {
    val team = data.team
    val sportName = team.sports?.name ?: ""
    val city = team.city ?: ""
    val ownerName = team.profiles?.name ?: ""
    val ownerAvatar = team.profiles?.avatarUrl
    val bannerUrl = remember(sportName) { sportImageUrl(sportName) }

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())
    ) {
        // ── HERO (280dp) ──
        Box(modifier = Modifier.fillMaxWidth()) {
            val heroShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            if (bannerUrl != null) {
                AsyncImage(bannerUrl, null, Modifier.fillMaxWidth().height(280.dp).clip(heroShape), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxWidth().height(280.dp).clip(heroShape).background(Color.Black.copy(0.7f)))
            } else {
                Box(Modifier.fillMaxWidth().height(280.dp).clip(heroShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF1a1a1a), Color(0xFF1a1a2d)))))
            }

            // Back
            Box(
                Modifier.statusBarsPadding().padding(16.dp).size(40.dp)
                    .clip(CircleShape).background(Color.White.copy(0.15f)).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Logo + info
            Column(
                Modifier.fillMaxWidth().statusBarsPadding().padding(top = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Team logo circle
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(104.dp).background(Brush.sweepGradient(listOf(teamColor, teamColor.copy(0.5f), Color(0xFF60A5FA), teamColor)), CircleShape))
                    Box(Modifier.size(98.dp).clip(CircleShape).background(Color(0xFF1a1a1a)))
                    Box(Modifier.size(92.dp).clip(CircleShape).background(teamColor.copy(0.15f)), contentAlignment = Alignment.Center) {
                        if (team.logoUrl != null) {
                            AsyncImage(team.logoUrl, null, Modifier.size(92.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Text(team.name.take(1).uppercase(), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = teamColor)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(team.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (sportName.isNotEmpty()) {
                        Surface(shape = RoundedCornerShape(50), color = Color.White.copy(0.15f)) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(sportIcon(sportName), null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(sportName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                    team.foundedYear?.let {
                        Surface(shape = RoundedCornerShape(50), color = Color.White.copy(0.15f)) {
                            Text("с $it", Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
                if (city.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(city, fontSize = 13.sp, color = Color.White.copy(0.7f))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── STATS ──
        Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), color = CardBg) {
            Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                StatColumn("${data.members.size}", "Участников")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn("${data.tournaments.size}", "Турниров")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                val avgRating = if (data.members.isNotEmpty()) {
                    data.members.mapNotNull { it.profiles?.city }.size // placeholder
                } else 0
                StatColumn("${data.members.size}", "Состав")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(team.foundedYear?.toString() ?: "—", "Осн.")
            }
        }

        // ── TRAINER ──
        Spacer(Modifier.height(16.dp))
        SectionCard("Тренер") {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (ownerAvatar != null) {
                    AsyncImage(ownerAvatar, null, Modifier.size(48.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(Accent.copy(0.15f)), contentAlignment = Alignment.Center) {
                        Text(ownerName.take(1), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Accent)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(ownerName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Тренер / Владелец", fontSize = 12.sp, color = TextMuted)
                }
                Surface(shape = RoundedCornerShape(50), color = Accent.copy(0.1f)) {
                    Text("Тренер", Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                }
            }
        }

        // ── MEMBERS ──
        if (data.members.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Состав команды") {
                data.members.forEachIndexed { idx, member ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    MemberRow(member)
                }
            }
        }

        // ── ACTIVE TOURNAMENTS ──
        if (data.activeTournaments.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Активные турниры") {
                data.activeTournaments.forEachIndexed { idx, t ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    TeamTournamentRow(t)
                }
            }
        }

        // ── UPCOMING TOURNAMENTS ──
        if (data.upcomingTournaments.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Предстоящие турниры") {
                data.upcomingTournaments.forEachIndexed { idx, t ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    TeamTournamentRow(t)
                }
            }
        }

        // ── TOURNAMENT HISTORY ──
        if (data.completedTournaments.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            TournamentHistorySection(data.completedTournaments)
        }

        // ── BEST RESULTS ──
        if (data.results.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Лучшие результаты") {
                data.results.forEachIndexed { idx, r ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    ResultRow(
                        name = r.tournaments?.name ?: "Турнир",
                        sport = r.profiles?.name ?: "",
                        position = r.position,
                        points = r.points ?: 0,
                        date = r.tournaments?.startDate ?: ""
                    )
                }
            }
        }

        // ── ABOUT ──
        if (!team.description.isNullOrEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("О клубе") {
                Text(team.description, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    InfoChip("Участников", "${data.members.size}")
                    InfoChip("Турниров", "${data.tournaments.size}")
                    InfoChip("Спорт", sportName)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ══════════════════════════════════════════════════════════
// Member Row
// ══════════════════════════════════════════════════════════

@Composable
private fun MemberRow(member: TeamMemberDto) {
    val name = member.profiles?.name ?: "—"
    val avatarUrl = member.profiles?.avatarUrl
    val city = member.profiles?.city
    val rColor = roleColor(member.role)

    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        UserAvatar(avatarUrl = avatarUrl, name = name, size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(rColor))
                Spacer(Modifier.width(6.dp))
                Text(roleLabel(member.role), fontSize = 12.sp, color = TextMuted)
                city?.let {
                    Text(" · $it", fontSize = 12.sp, color = TextMuted)
                }
            }
        }
        Surface(shape = RoundedCornerShape(50), color = rColor.copy(0.1f)) {
            Text(roleLabel(member.role), Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = rColor)
        }
    }
}

// ══════════════════════════════════════════════════════════
// Tournament Row
// ══════════════════════════════════════════════════════════

@Composable
private fun TeamTournamentRow(t: TournamentWithCountsDto) {
    val statusColor = when (t.status) {
        "in_progress" -> Color(0xFF22C55E); "check_in" -> Color(0xFFF59E0B)
        "registration_open" -> Color(0xFF3B82F6); else -> TextMuted
    }
    val statusLabel = when (t.status) {
        "in_progress" -> "Идёт"; "check_in" -> "Check-in"
        "registration_open" -> "Регистрация"; "registration_closed" -> "Рег. закрыта"; else -> t.status ?: ""
    }

    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(44.dp).background(statusColor.copy(0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.EmojiEvents, null, tint = statusColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                t.sportName?.let { Text(it, fontSize = 12.sp, color = TextMuted) }
                t.locationName?.let {
                    Text("·", fontSize = 12.sp, color = TextMuted)
                    Text(it, fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                Text(statusLabel, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
            }
            t.startDate?.let {
                Spacer(Modifier.height(2.dp))
                Text(formatDateShort(it), fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

private fun formatDateShort(dateStr: String): String {
    return try {
        val parts = dateStr.take(10).split("-")
        if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else dateStr
    } catch (_: Exception) { dateStr }
}

// ══════════════════════════════════════════════════════════
// Tournament History
// ══════════════════════════════════════════════════════════

@Composable
private fun TournamentHistorySection(tournaments: List<TournamentWithCountsDto>) {
    val years = tournaments.mapNotNull { it.startDate?.take(4) }.distinct().sortedDescending()
    var selectedYear by remember { mutableStateOf(years.firstOrNull() ?: "2026") }
    val filtered = tournaments.filter { it.startDate?.startsWith(selectedYear) == true }

    SectionCard("История матчей") {
        // Year tabs
        if (years.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                years.forEach { year ->
                    val sel = year == selectedYear
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (sel) Accent else TextMuted.copy(0.1f),
                        modifier = Modifier.clickable { selectedYear = year }
                    ) {
                        Text(year, Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = if (sel) Color.White else TextMuted)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (filtered.isEmpty()) {
            Text("Нет турниров за $selectedYear", fontSize = 13.sp, color = TextMuted)
        } else {
            filtered.forEachIndexed { idx, t ->
                if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (t.status) {
                        "completed" -> TextMuted; "in_progress" -> Color(0xFF22C55E)
                        "registration_open" -> Color(0xFF3B82F6); else -> TextMuted
                    }
                    Box(
                        Modifier.size(44.dp).background(statusColor.copy(0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, null, tint = statusColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            t.startDate?.let { date ->
                                val parts = date.take(10).split("-")
                                if (parts.size >= 3) {
                                    val months = listOf("", "янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
                                    val d = parts[2].toIntOrNull() ?: 0
                                    val m = parts[1].toIntOrNull() ?: 0
                                    Text("$d ${months.getOrElse(m) { "" }}", fontSize = 12.sp, color = TextMuted)
                                }
                            }
                            t.sportName?.let {
                                Text("·", fontSize = 12.sp, color = TextMuted)
                                Text(it, fontSize = 12.sp, color = TextMuted)
                            }
                        }
                    }
                    val statusLabel = when (t.status) {
                        "completed" -> "Завершён"; "in_progress" -> "Идёт"
                        "registration_open" -> "Регистрация"; "cancelled" -> "Отменён"; else -> t.status ?: ""
                    }
                    val stColor = when (t.status) {
                        "completed" -> TextMuted; "in_progress" -> Color(0xFF22C55E)
                        "registration_open" -> Color(0xFF3B82F6); else -> TextMuted
                    }
                    Surface(shape = RoundedCornerShape(50), color = stColor.copy(0.12f)) {
                        Text(statusLabel, Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = stColor)
                    }
                }
            }
        }
    }
}
