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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

// ══════════════════════════════════════════════════════════
// Mock data
// ══════════════════════════════════════════════════════════

private data class MockAthleteData(
    val name: String = "Алихан Тлеубаев",
    val nickname: String = "alikh_racer",
    val avatarUrl: String? = null,
    val city: String = "Алматы",
    val country: String = "Казахстан",
    val phone: String = "+7 707 123 45 67",
    val email: String = "alikhan@demo.com",
    val teamName: String = "Red Racers",
    val teamRole: String = "captain",
    val participantNumber: Int = 7,
    val seed: Int = 3,
    val checkInStatus: String = "checked_in",
    val sportName: String = "Картинг",
    val ageCategory: String = "adult",
    val rating: Int = 1450,
    val totalTournaments: Int = 24,
    val wins: Int = 8,
    val podiums: Int = 14,
    val totalPoints: Int = 3200,
    val licenseNumber: String = "KZ-2025-0147",
    val licenseCategory: String = "Профессионал",
    val licenseClass: String = "A",
    val activeTournaments: List<MockTournamentItem> = listOf(
        MockTournamentItem("Кубок Алматы 2026", "Картинг", "in_progress", "2026-04-05", "Almaty Karting Center"),
        MockTournamentItem("Весенний Гран-При", "Картинг", "check_in", "2026-04-12", "Speed Arena")
    ),
    val upcomingTournaments: List<MockTournamentItem> = listOf(
        MockTournamentItem("Чемпионат РК", "Картинг", "registration_open", "2026-05-10", "Astana Motorsport"),
        MockTournamentItem("Summer Cup", "Картинг", "registration_open", "2026-06-15", "Almaty Karting Center")
    ),
    val historyTournaments: List<MockTournamentResult> = listOf(
        MockTournamentResult("Зимний Кубок 2025", "Картинг", 1, 500, "2025-12-20"),
        MockTournamentResult("Осенний Чемпионат", "Картинг", 2, 350, "2025-10-15"),
        MockTournamentResult("Кубок Астаны", "Картинг", 3, 250, "2025-08-22"),
        MockTournamentResult("Летний Гран-При", "Картинг", 1, 500, "2025-07-10"),
        MockTournamentResult("Весенний Кубок", "Картинг", 5, 150, "2025-04-18")
    ),
    val achievements: List<MockAchievement> = listOf(
        MockAchievement("Чемпион сезона", "Победа в чемпионате 2025", "legendary"),
        MockAchievement("Хет-трик", "3 победы подряд", "epic"),
        MockAchievement("На подиуме", "10 подиумов за сезон", "rare")
    ),
    val goals: List<MockGoal> = listOf(
        MockGoal("Рейтинг 1500", 1450, 1500, "active"),
        MockGoal("30 турниров", 24, 30, "active")
    )
)

private data class MockTournamentItem(
    val name: String, val sport: String, val status: String, val date: String, val location: String
)
private data class MockTournamentResult(
    val name: String, val sport: String, val position: Int, val points: Int, val date: String
)
private data class MockAchievement(val title: String, val description: String, val rarity: String)
private data class MockGoal(val title: String, val current: Int, val target: Int, val status: String)

// ══════════════════════════════════════════════════════════
// Main Screen
// ══════════════════════════════════════════════════════════

@Composable
fun AthleteProfilePage(
    athleteId: String,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit = {},
    onTeamClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {}
) {
    val data = remember { MockAthleteData() }
    val bannerUrl = remember { sportImageUrl(data.sportName) }

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())
    ) {
        // ── HERO ──
        Box(modifier = Modifier.fillMaxWidth()) {
            val heroShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            if (bannerUrl != null) {
                AsyncImage(bannerUrl, null, Modifier.fillMaxWidth().height(260.dp).clip(heroShape), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxWidth().height(260.dp).clip(heroShape).background(Color.Black.copy(0.7f)))
            } else {
                Box(Modifier.fillMaxWidth().height(260.dp).clip(heroShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF1a1a1a), Color(0xFF2d1a1a)))))
            }

            // Back
            Box(
                Modifier.statusBarsPadding().padding(16.dp).size(40.dp)
                    .clip(CircleShape).background(Color.White.copy(0.15f)).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Avatar + info
            Column(
                Modifier.fillMaxWidth().statusBarsPadding().padding(top = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(112.dp).background(Brush.sweepGradient(listOf(Accent, Accent.copy(0.5f), Color(0xFF3B82F6), Accent)), CircleShape))
                    Box(Modifier.size(106.dp).clip(CircleShape).background(Color(0xFF1a1a1a)))
                    Box(Modifier.size(100.dp).clip(CircleShape).background(Color(0xFF252525)), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(100.dp).clip(CircleShape).background(Accent), contentAlignment = Alignment.Center) {
                            Text(data.name.take(1).uppercase(), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(data.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text("@${data.nickname}", fontSize = 14.sp, color = Color.White.copy(0.6f))
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoleBadge(role = com.ileader.app.data.models.UserRole.ATHLETE)
                    Text("·", fontSize = 14.sp, color = Color.White.copy(0.7f))
                    Text("${data.city}, ${data.country}", fontSize = 13.sp, color = Color.White.copy(0.7f))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── SPORT TAG ──
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SportTag(data.sportName)
            Surface(shape = RoundedCornerShape(50), color = Color(0xFF3B82F6).copy(0.12f)) {
                Text(data.ageCategory.let { when (it) { "junior" -> "Юниор"; "adult" -> "Взрослый"; "senior" -> "Ветеран"; else -> it } },
                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3B82F6))
            }
        }

        // ── STATS ──
        Spacer(Modifier.height(16.dp))
        Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), color = CardBg) {
            Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                StatColumn(data.totalTournaments.toString(), "Турниров")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(data.wins.toString(), "Побед")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(data.podiums.toString(), "Подиумов")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(data.rating.toString(), "Рейтинг")
            }
        }

        // ── CONTACTS ──
        Spacer(Modifier.height(16.dp))
        SectionCard("Контакты") {
            ContactRow(Icons.Outlined.Phone, "Телефон", data.phone)
            Spacer(Modifier.height(8.dp))
            ContactRow(Icons.Outlined.Email, "Email", data.email)
            Spacer(Modifier.height(8.dp))
            ContactRow(Icons.Outlined.LocationOn, "Город", "${data.city}, ${data.country}")
        }

        // ── TOURNAMENT PARTICIPATION ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Участие в турнире") {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                InfoChip("Номер", "#${data.participantNumber}")
                InfoChip("Seed", "#${data.seed}")
                InfoChip("Check-in", when (data.checkInStatus) { "checked_in" -> "✓"; "pending" -> "⏳"; else -> "—" })
            }
        }

        // ── TEAM ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Команда") {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable { onTeamClick("mock-team-id") }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(48.dp).background(Accent.copy(0.1f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(data.teamName.take(1).uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Accent)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(data.teamName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(when (data.teamRole) { "captain" -> "Капитан"; "member" -> "Участник"; "reserve" -> "Запасной"; else -> data.teamRole },
                        fontSize = 13.sp, color = TextMuted)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }

        // ── LICENSE ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Лицензия") {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column {
                    Text("Номер", fontSize = 12.sp, color = TextMuted)
                    Text(data.licenseNumber, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Категория", fontSize = 12.sp, color = TextMuted)
                    Text(data.licenseCategory, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column {
                    Text("Класс", fontSize = 12.sp, color = TextMuted)
                    Text(data.licenseClass, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Статус", fontSize = 12.sp, color = TextMuted)
                    Surface(shape = RoundedCornerShape(50), color = Color(0xFF22C55E).copy(0.12f)) {
                        Text("Активна", Modifier.padding(horizontal = 10.dp, vertical = 3.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF22C55E))
                    }
                }
            }
        }

        // ── ACTIVE TOURNAMENTS ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Активные турниры") {
            data.activeTournaments.forEachIndexed { idx, t ->
                if (idx > 0) {
                    HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                }
                TournamentRow(t.name, t.sport, t.location, t.date, t.status) { onTournamentClick("mock-id") }
            }
        }

        // ── UPCOMING TOURNAMENTS ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Предстоящие турниры") {
            data.upcomingTournaments.forEachIndexed { idx, t ->
                if (idx > 0) {
                    HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                }
                TournamentRow(t.name, t.sport, t.location, t.date, t.status) { onTournamentClick("mock-id") }
            }
        }

        // ── HISTORY ──
        Spacer(Modifier.height(12.dp))
        SectionCard("История турниров") {
            data.historyTournaments.forEachIndexed { idx, r ->
                if (idx > 0) {
                    HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                }
                ResultRow(r.name, r.sport, r.position, r.points, r.date)
            }
        }

        // ── ACHIEVEMENTS ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Достижения") {
            data.achievements.forEachIndexed { idx, a ->
                if (idx > 0) Spacer(Modifier.height(8.dp))
                val rarityColor = when (a.rarity) { "legendary" -> Color(0xFFCA8A04); "epic" -> Color(0xFF7C3AED); "rare" -> Color(0xFF3B82F6); else -> TextMuted }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).background(rarityColor.copy(0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, null, tint = rarityColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(a.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(a.description, fontSize = 12.sp, color = TextMuted)
                    }
                    Surface(shape = RoundedCornerShape(50), color = rarityColor.copy(0.12f)) {
                        Text(when (a.rarity) { "legendary" -> "Легендарное"; "epic" -> "Эпическое"; "rare" -> "Редкое"; else -> "Обычное" },
                            Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = rarityColor)
                    }
                }
            }
        }

        // ── GOALS ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Цели") {
            data.goals.forEachIndexed { idx, g ->
                if (idx > 0) Spacer(Modifier.height(10.dp))
                val progress = (g.current.toFloat() / g.target).coerceIn(0f, 1f)
                Column {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text(g.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("${g.current}/${g.target}", fontSize = 12.sp, color = TextMuted)
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF3B82F6),
                        trackColor = Color(0xFF3B82F6).copy(0.12f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

// ══════════════════════════════════════════════════════════
// Shared composables for all profile pages
// ══════════════════════════════════════════════════════════

@Composable
internal fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    DarkCardPadded(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
internal fun ContactRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(36.dp).background(LocalAppColors.current.accentSoft, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = DarkTheme.Accent, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = DarkTheme.TextMuted)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkTheme.TextPrimary)
        }
    }
}

@Composable
internal fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DarkTheme.TextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = DarkTheme.TextMuted)
    }
}

@Composable
internal fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkTheme.TextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = DarkTheme.TextMuted)
    }
}

@Composable
internal fun TournamentRow(name: String, sport: String, location: String, date: String, status: String, onClick: () -> Unit) {
    val statusColor = when (status) {
        "in_progress" -> Color(0xFF22C55E); "check_in" -> Color(0xFFF59E0B)
        "registration_open" -> Color(0xFF3B82F6); else -> DarkTheme.TextMuted
    }
    val statusLabel = when (status) {
        "in_progress" -> "Идёт"; "check_in" -> "Check-in"
        "registration_open" -> "Регистрация"; else -> status
    }

    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
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
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(sport, fontSize = 12.sp, color = DarkTheme.TextMuted)
                Text("·", fontSize = 12.sp, color = DarkTheme.TextMuted)
                Text(location, fontSize = 12.sp, color = DarkTheme.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                Text(statusLabel, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
            }
            Spacer(Modifier.height(2.dp))
            Text(date.take(10), fontSize = 11.sp, color = DarkTheme.TextMuted)
        }
    }
}

@Composable
internal fun ResultRow(name: String, sport: String, position: Int, points: Int, date: String) {
    val posEmoji = when (position) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "#$position" }

    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(40.dp).background(
                if (position <= 3) DarkTheme.Accent.copy(0.12f) else LocalAppColors.current.border.copy(0.2f),
                RoundedCornerShape(10.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(posEmoji, fontSize = if (position <= 3) 18.sp else 13.sp, fontWeight = FontWeight.Bold,
                color = if (position <= 3) DarkTheme.Accent else DarkTheme.TextMuted)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(sport, fontSize = 12.sp, color = DarkTheme.TextMuted)
                Text("·", fontSize = 12.sp, color = DarkTheme.TextMuted)
                Text(date.take(10), fontSize = 12.sp, color = DarkTheme.TextMuted)
            }
        }
        if (points > 0) {
            Column(horizontalAlignment = Alignment.End) {
                Text("$points", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTheme.Accent)
                Text("очки", fontSize = 10.sp, color = DarkTheme.TextMuted)
            }
        }
    }
}
