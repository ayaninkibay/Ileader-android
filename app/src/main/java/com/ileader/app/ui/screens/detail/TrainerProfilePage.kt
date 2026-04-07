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

private data class MockTrainerProfileData(
    val name: String = "Ержан Каримов",
    val avatarUrl: String? = null,
    val city: String = "Алматы",
    val country: String = "Казахстан",
    val phone: String = "+7 702 555 33 11",
    val email: String = "erzhan.trainer@demo.com",
    val sportName: String = "Картинг",
    val yearsExperience: Int = 12,
    val totalAthletesTrainedCareer: Int = 87,
    val teamName: String = "Red Racers",
    val teamLogoUrl: String? = null,
    val teamCity: String = "Алматы",
    val teamFoundedYear: Int = 2019,
    val teamDescription: String = "Профессиональная картинг-команда, многократные чемпионы Казахстана",
    val athletes: List<MockTrainerAthlete> = listOf(
        MockTrainerAthlete("Алихан Тлеубаев", "captain", 1450, null),
        MockTrainerAthlete("Марат Касымов", "member", 1280, null),
        MockTrainerAthlete("Данияр Серикбаев", "member", 1190, null),
        MockTrainerAthlete("Тимур Нурланов", "reserve", 980, null)
    ),
    val teamTournaments: Int = 18,
    val teamWins: Int = 6,
    val teamPodiums: Int = 12,
    val avgTeamRating: Int = 1225,
    val activeTournaments: List<MockTrainerTournament> = listOf(
        MockTrainerTournament("Кубок Алматы 2026", "Картинг", "in_progress", "2026-04-05", "Almaty Karting Center", 3),
        MockTrainerTournament("Весенний Гран-При", "Картинг", "check_in", "2026-04-12", "Speed Arena", 2)
    ),
    val upcomingTournaments: List<MockTrainerTournament> = listOf(
        MockTrainerTournament("Чемпионат РК", "Картинг", "registration_open", "2026-05-10", "Astana Motorsport", 4),
        MockTrainerTournament("Summer Cup", "Картинг", "registration_open", "2026-06-15", "Almaty Karting Center", 3)
    ),
    val historyTournaments: List<MockTrainerTournamentResult> = listOf(
        MockTrainerTournamentResult("Зимний Кубок 2025", "Картинг", 1, "2025-12-20", "Алихан Тлеубаев — 🥇"),
        MockTrainerTournamentResult("Осенний Чемпионат", "Картинг", 2, "2025-10-15", "Марат Касымов — 🥈"),
        MockTrainerTournamentResult("Кубок Астаны", "Картинг", 3, "2025-08-22", "Данияр Серикбаев — 🥉"),
        MockTrainerTournamentResult("Летний Гран-При", "Картинг", 1, "2025-07-10", "Алихан Тлеубаев — 🥇"),
        MockTrainerTournamentResult("Весенний Кубок", "Картинг", 4, "2025-04-18", "Тимур Нурланов — #4")
    ),
    val teamAchievements: List<MockTeamAchievement> = listOf(
        MockTeamAchievement("Команда года 2025", "Лучшая команда сезона 2025", "legendary"),
        MockTeamAchievement("Серия побед", "5 побед подряд командой", "epic"),
        MockTeamAchievement("Полный подиум", "Все 3 призовых места — наши спортсмены", "rare")
    )
)

private data class MockTrainerAthlete(val name: String, val role: String, val rating: Int, val avatarUrl: String?)
private data class MockTrainerTournament(val name: String, val sport: String, val status: String, val date: String, val location: String, val athleteCount: Int)
private data class MockTrainerTournamentResult(val name: String, val sport: String, val bestPosition: Int, val date: String, val bestResult: String)
private data class MockTeamAchievement(val title: String, val description: String, val rarity: String)

// ══════════════════════════════════════════════════════════
// Main Screen
// ══════════════════════════════════════════════════════════

@Composable
fun TrainerProfilePage(
    trainerId: String,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit = {},
    onAthleteClick: (String) -> Unit = {},
    onTeamClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {}
) {
    val data = remember { MockTrainerProfileData() }
    val bannerUrl = remember { sportImageUrl(data.sportName) }
    val trainerColor = Color(0xFF059669)

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
                    .background(Brush.verticalGradient(listOf(Color(0xFF1a1a1a), Color(0xFF1a2d1a)))))
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
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(112.dp).background(Brush.sweepGradient(listOf(trainerColor, trainerColor.copy(0.5f), Color(0xFF10B981), trainerColor)), CircleShape))
                    Box(Modifier.size(106.dp).clip(CircleShape).background(Color(0xFF1a1a1a)))
                    Box(Modifier.size(100.dp).clip(CircleShape).background(Color(0xFF252525)), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(100.dp).clip(CircleShape).background(trainerColor.copy(0.8f)), contentAlignment = Alignment.Center) {
                            Text(data.name.take(1).uppercase(), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(data.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoleBadge(role = com.ileader.app.data.models.UserRole.TRAINER)
                    Text("·", fontSize = 14.sp, color = Color.White.copy(0.7f))
                    Text("${data.city}, ${data.country}", fontSize = 13.sp, color = Color.White.copy(0.7f))
                }
                Spacer(Modifier.height(6.dp))
                Surface(shape = RoundedCornerShape(50), color = trainerColor.copy(0.25f)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("${data.yearsExperience} лет опыта", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── SPORT TAG ──
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SportTag(data.sportName)
        }

        // ── STATS ──
        Spacer(Modifier.height(16.dp))
        Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), color = CardBg) {
            Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                StatColumn(data.athletes.size.toString(), "Спортсменов")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(data.teamTournaments.toString(), "Турниров")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(data.teamWins.toString(), "Побед")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn("${data.totalAthletesTrainedCareer}", "За карьеру")
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

        // ── TEAM ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Команда") {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable { onTeamClick("mock-team-id") }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(56.dp).background(trainerColor.copy(0.1f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(data.teamName.take(1).uppercase(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = trainerColor)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(data.teamName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${data.teamCity} · осн. ${data.teamFoundedYear}", fontSize = 12.sp, color = TextMuted)
                    Spacer(Modifier.height(2.dp))
                    Text(data.teamDescription, fontSize = 12.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(16.dp))
            }

            Spacer(Modifier.height(12.dp))

            // Team stats
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                InfoChip("Турниров", "${data.teamTournaments}")
                InfoChip("Побед", "${data.teamWins}")
                InfoChip("Подиумов", "${data.teamPodiums}")
                InfoChip("Ср. рейтинг", "${data.avgTeamRating}")
            }
        }

        // ── ATHLETES ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Спортсмены") {
            data.athletes.forEachIndexed { idx, athlete ->
                if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                val roleLabel = when (athlete.role) { "captain" -> "Капитан"; "member" -> "Участник"; "reserve" -> "Запасной"; else -> athlete.role }
                val roleColor = when (athlete.role) { "captain" -> Accent; "member" -> Color(0xFF3B82F6); else -> TextMuted }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .clickable { onAthleteClick("mock-athlete-$idx") }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(avatarUrl = athlete.avatarUrl, name = athlete.name, size = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(athlete.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(roleColor))
                            Spacer(Modifier.width(6.dp))
                            Text(roleLabel, fontSize = 12.sp, color = TextMuted)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${athlete.rating}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("рейтинг", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }

        // ── ACTIVE TOURNAMENTS ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Активные турниры") {
            data.activeTournaments.forEachIndexed { idx, t ->
                if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                TrainerTournamentRow(t) { onTournamentClick("mock-id") }
            }
        }

        // ── UPCOMING TOURNAMENTS ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Предстоящие турниры") {
            data.upcomingTournaments.forEachIndexed { idx, t ->
                if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                TrainerTournamentRow(t) { onTournamentClick("mock-id") }
            }
        }

        // ── HISTORY ──
        Spacer(Modifier.height(12.dp))
        SectionCard("История турниров") {
            data.historyTournaments.forEachIndexed { idx, r ->
                if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                val posEmoji = when (r.bestPosition) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "#${r.bestPosition}" }
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).background(
                            if (r.bestPosition <= 3) Accent.copy(0.12f) else Border.copy(0.2f),
                            RoundedCornerShape(10.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(posEmoji, fontSize = if (r.bestPosition <= 3) 18.sp else 13.sp, fontWeight = FontWeight.Bold,
                            color = if (r.bestPosition <= 3) Accent else TextMuted)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(r.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(r.bestResult, fontSize = 12.sp, color = TextMuted)
                    }
                    Text(r.date.take(10), fontSize = 11.sp, color = TextMuted)
                }
            }
        }

        // ── TEAM ACHIEVEMENTS ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Достижения команды") {
            data.teamAchievements.forEachIndexed { idx, a ->
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

        Spacer(Modifier.height(100.dp))
    }
}

// ══════════════════════════════════════════════════════════
// Trainer tournament row with athlete count
// ══════════════════════════════════════════════════════════

@Composable
private fun TrainerTournamentRow(t: MockTrainerTournament, onClick: () -> Unit) {
    val statusColor = when (t.status) {
        "in_progress" -> Color(0xFF22C55E); "check_in" -> Color(0xFFF59E0B)
        "registration_open" -> Color(0xFF3B82F6); else -> DarkTheme.TextMuted
    }
    val statusLabel = when (t.status) {
        "in_progress" -> "Идёт"; "check_in" -> "Check-in"
        "registration_open" -> "Регистрация"; else -> t.status
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
            Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(t.sport, fontSize = 12.sp, color = DarkTheme.TextMuted)
                Text("·", fontSize = 12.sp, color = DarkTheme.TextMuted)
                Text("${t.athleteCount} спортсм.", fontSize = 12.sp, color = DarkTheme.TextMuted)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                Text(statusLabel, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
            }
            Spacer(Modifier.height(2.dp))
            Text(t.date.take(10), fontSize = 11.sp, color = DarkTheme.TextMuted)
        }
    }
}
