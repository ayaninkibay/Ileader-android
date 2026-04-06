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
import com.ileader.app.data.remote.dto.TeamMemberDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.theme.cardShadow
import com.ileader.app.ui.viewmodels.TeamDetailViewModel
import com.ileader.app.ui.viewmodels.SportViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

private fun roleLabel(role: String?): String = when (role) {
    "captain" -> "Капитан"; "member" -> "Участник"; "reserve" -> "Резерв"; else -> role ?: ""
}
private fun roleColor(role: String?): Color = when (role) {
    "captain" -> Color(0xFFE53535); "reserve" -> Color(0xFF6B7280); else -> Color(0xFF3B82F6)
}

@Composable
fun TeamDetailScreen(
    teamId: String,
    onBack: () -> Unit,
    vm: TeamDetailViewModel = viewModel()
) {
    val isDark = DarkTheme.isDark

    LaunchedEffect(teamId) { vm.load(teamId) }

    Column(Modifier.fillMaxSize().background(Bg)) {
        when (val s = vm.state) {
            is UiState.Loading -> { BackHeader("Команда", onBack); LoadingScreen() }
            is UiState.Error -> { BackHeader("Команда", onBack); ErrorScreen(s.message, onRetry = { vm.load(teamId) }) }
            is UiState.Success -> {
                val data = s.data
                val team = data.team
                val sportName = team.sports?.name ?: ""
                val city = team.city ?: ""
                val ownerName = team.profiles?.name ?: ""
                val ownerAvatar = team.profiles?.avatarUrl
                val imgUrl = SportViewModel.getFallbackImage(
                    com.ileader.app.data.remote.dto.SportDto(id = team.sportId ?: "", name = sportName)
                )

                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    // ══════════════════════════════════════
                    // HERO
                    // ══════════════════════════════════════
                    Box(Modifier.fillMaxWidth().height(200.dp)) {
                        if (imgUrl != null) {
                            AsyncImage(imgUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Accent.copy(0.8f), Accent.copy(0.3f)))))
                        }
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.25f), Color.Black.copy(0.75f)))))

                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.statusBarsPadding().padding(12.dp).align(Alignment.TopStart)
                                .size(36.dp).clip(CircleShape).background(Color.Black.copy(0.3f))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                            Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(0.4f)) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(sportIcon(sportName), null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text(sportName, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(team.name, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.3).sp, maxLines = 2)
                            if (city.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.LocationOn, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(city, fontSize = 13.sp, color = Color.White.copy(0.8f))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ══════════════════════════════════════
                    // 1. ТРЕНЕР + СОСТАВ
                    // ══════════════════════════════════════
                    SectionCard("Тренер и состав", Icons.Outlined.Groups, Modifier.padding(horizontal = 16.dp), isDark) {
                        // Trainer (owner)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (ownerAvatar != null) {
                                AsyncImage(ownerAvatar, null, Modifier.size(44.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            } else {
                                Box(Modifier.size(44.dp).clip(CircleShape).background(Accent.copy(0.15f)), contentAlignment = Alignment.Center) {
                                    Text(ownerName.take(1), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Accent)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(ownerName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Тренер / Владелец", fontSize = 12.sp, color = TextMuted)
                            }
                            Surface(shape = RoundedCornerShape(50), color = Accent.copy(0.1f)) {
                                Text("Тренер", Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                            }
                        }

                        if (data.members.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = Border.copy(0.15f))
                            Spacer(Modifier.height(8.dp))

                            data.members.forEachIndexed { idx, member ->
                                if (idx > 0) {
                                    HorizontalDivider(color = Border.copy(0.1f), modifier = Modifier.padding(vertical = 6.dp))
                                }
                                MemberRow(member)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ══════════════════════════════════════
                    // 2. ИСТОРИЯ МАТЧЕЙ
                    // ══════════════════════════════════════
                    if (data.tournaments.isNotEmpty()) {
                        TournamentHistorySection(data.tournaments, isDark)
                        Spacer(Modifier.height(12.dp))
                    }

                    // ══════════════════════════════════════
                    // 3. О КЛУБЕ
                    // ══════════════════════════════════════
                    SectionCard("О клубе", Icons.Outlined.Info, Modifier.padding(horizontal = 16.dp), isDark) {
                        if (!team.description.isNullOrEmpty()) {
                            Text(team.description, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)
                            Spacer(Modifier.height(12.dp))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            team.foundedYear?.let {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.CalendarMonth, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Основана в $it", fontSize = 13.sp, color = TextMuted)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.People, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("${data.members.size} участников", fontSize = 13.sp, color = TextMuted)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.EmojiEvents, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("${data.tournaments.size} турниров", fontSize = 13.sp, color = TextMuted)
                            }
                        }
                    }

                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// Member Row
// ══════════════════════════════════════════

@Composable
private fun MemberRow(member: TeamMemberDto) {
    val name = member.profiles?.name ?: "—"
    val avatarUrl = member.profiles?.avatarUrl
    val city = member.profiles?.city
    val rColor = roleColor(member.role)

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (avatarUrl != null) {
            AsyncImage(avatarUrl, null, Modifier.size(36.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        } else {
            Box(Modifier.size(36.dp).clip(CircleShape).background(rColor.copy(0.12f)), contentAlignment = Alignment.Center) {
                Text(name.take(1), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = rColor)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            city?.let { Text(it, fontSize = 12.sp, color = TextMuted) }
        }
        Surface(shape = RoundedCornerShape(50), color = rColor.copy(0.1f)) {
            Text(roleLabel(member.role), Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = rColor)
        }
    }
}

// ══════════════════════════════════════════
// Tournament History (with year tabs)
// ══════════════════════════════════════════

@Composable
private fun TournamentHistorySection(tournaments: List<TournamentWithCountsDto>, isDark: Boolean) {
    val years = tournaments.mapNotNull { it.startDate?.take(4) }.distinct().sortedDescending()
    var selectedYear by remember { mutableStateOf(years.firstOrNull() ?: "2026") }
    val filtered = tournaments.filter { it.startDate?.startsWith(selectedYear) == true }

    SectionCard("История матчей", Icons.Outlined.EmojiEvents, Modifier.padding(horizontal = 16.dp), isDark) {
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
                if (idx > 0) HorizontalDivider(color = Border.copy(0.1f), modifier = Modifier.padding(vertical = 6.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            t.startDate?.let { date ->
                                val parts = date.take(10).split("-")
                                if (parts.size >= 3) {
                                    val months = listOf("", "янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
                                    val d = parts[2].toIntOrNull() ?: 0
                                    val m = parts[1].toIntOrNull() ?: 0
                                    Text("$d ${months.getOrElse(m) { "" }}", fontSize = 12.sp, color = TextMuted)
                                    Spacer(Modifier.width(8.dp))
                                }
                            }
                            t.sportName?.let { Text(it, fontSize = 12.sp, color = TextMuted) }
                        }
                    }
                    // Status badge
                    val statusColor = when (t.status) {
                        "completed" -> Color(0xFF6B7280)
                        "in_progress" -> Color(0xFF3B82F6)
                        "registration_open" -> Color(0xFF22C55E)
                        else -> Color(0xFF6B7280)
                    }
                    val statusLabel = when (t.status) {
                        "completed" -> "Завершён"
                        "in_progress" -> "Идёт"
                        "registration_open" -> "Регистрация"
                        "cancelled" -> "Отменён"
                        else -> t.status ?: ""
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(0.12f)) {
                        Text(statusLabel, Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// Section Card
// ══════════════════════════════════════════

@Composable
private fun SectionCard(title: String, icon: ImageVector, modifier: Modifier, isDark: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier.fillMaxWidth().cardShadow(isDark), shape = RoundedCornerShape(16.dp), color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke else null) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}
