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

private data class MockRefSportStat(val sport: String, val tournaments: Int, val asHead: Int, val asAssistant: Int)

private data class MockRefereeProfileData(
    val name: String = "Серик Абдуллаев",
    val avatarUrl: String? = null,
    val city: String = "Астана",
    val country: String = "Казахстан",
    val phone: String = "+7 701 987 65 43",
    val email: String = "serik.referee@demo.com",
    val sports: List<String> = listOf("Картинг", "Стрельба", "Теннис"),
    val totalJudged: Int = 45,
    val completedTournaments: Int = 38,
    val activeTournaments: Int = 3,
    val rating: Int = 1320,
    val licenseNumber: String = "REF-KZ-2024-0032",
    val licenseCategory: String = "Международная",
    val federation: String = "Федерация картинга РК",
    val licenseStatus: String = "active",
    val licenseExpiry: String = "2027-12-31",
    val sportStats: List<MockRefSportStat> = listOf(
        MockRefSportStat("Картинг", 32, 24, 8),
        MockRefSportStat("Стрельба", 8, 5, 3),
        MockRefSportStat("Теннис", 5, 1, 4)
    ),
    val activeTournamentList: List<MockRefTournament> = listOf(
        MockRefTournament("Кубок Алматы 2026", "Картинг", "head", "in_progress", "2026-04-05", "Almaty Karting Center"),
        MockRefTournament("Весенний Гран-При", "Картинг", "assistant", "check_in", "2026-04-12", "Speed Arena"),
        MockRefTournament("Кубок Стрелков", "Стрельба", "head", "registration_open", "2026-04-20", "Shooting Range Astana")
    ),
    val upcomingTournamentList: List<MockRefTournament> = listOf(
        MockRefTournament("Чемпионат РК", "Картинг", "head", "registration_open", "2026-05-10", "Astana Motorsport"),
        MockRefTournament("Летний турнир", "Теннис", "line", "registration_open", "2026-06-01", "Tennis Club Almaty")
    ),
    val historyTournamentList: List<MockRefTournament> = listOf(
        MockRefTournament("Зимний Кубок 2025", "Картинг", "head", "completed", "2025-12-20", "Almaty Karting Center"),
        MockRefTournament("Осенний Чемпионат", "Картинг", "head", "completed", "2025-10-15", "Speed Arena"),
        MockRefTournament("Кубок Астаны", "Стрельба", "assistant", "completed", "2025-08-22", "Shooting Range"),
        MockRefTournament("Летний Гран-При", "Картинг", "head", "completed", "2025-07-10", "Almaty Karting Center"),
        MockRefTournament("Open Cup Tennis", "Теннис", "line", "completed", "2025-05-18", "Tennis Club")
    ),
    val achievements: List<MockRefAchievement> = listOf(
        MockRefAchievement("50 турниров", "Отсудил 50 турниров на платформе", "legendary"),
        MockRefAchievement("Серия главного", "10 турниров подряд как главный судья", "epic"),
        MockRefAchievement("Мультиспорт", "Судейство в 3+ видах спорта", "rare"),
        MockRefAchievement("Без нарушений", "Сезон без спорных решений", "rare")
    ),
    val reviews: List<MockRefereeReview> = listOf(
        MockRefereeReview("Организатор А.", 5.0f, "Отличное судейство, очень профессионально"),
        MockRefereeReview("Организатор Б.", 4.5f, "Чётко и справедливо"),
        MockRefereeReview("Организатор В.", 4.5f, "Рекомендую!")
    )
)

private data class MockRefTournament(
    val name: String, val sport: String, val role: String, val status: String, val date: String, val location: String
)

private data class MockRefereeReview(val author: String, val rating: Float, val comment: String)
private data class MockRefAchievement(val title: String, val description: String, val rarity: String)

// ══════════════════════════════════════════════════════════
// Main Screen
// ══════════════════════════════════════════════════════════

@Composable
fun RefereeProfilePage(
    refereeId: String,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {}
) {
    val data = remember { MockRefereeProfileData() }
    val bannerUrl = remember { sportImageUrl(data.sports.firstOrNull() ?: "Картинг") }
    val refColor = Color(0xFFEF4444)

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())
    ) {
        // ── HERO ──
        Box(modifier = Modifier.fillMaxWidth()) {
            val heroShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            if (bannerUrl != null) {
                AsyncImage(bannerUrl, null, Modifier.fillMaxWidth().height(320.dp).clip(heroShape), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxWidth().height(320.dp).clip(heroShape).background(Color.Black.copy(0.7f)))
            } else {
                Box(Modifier.fillMaxWidth().height(320.dp).clip(heroShape)
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
                Modifier.fillMaxWidth().statusBarsPadding().padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(104.dp).background(Brush.sweepGradient(listOf(refColor, refColor.copy(0.5f), Color(0xFF7C3AED), refColor)), CircleShape))
                    Box(Modifier.size(98.dp).clip(CircleShape).background(Color(0xFF1a1a1a)))
                    Box(Modifier.size(92.dp).clip(CircleShape).background(Color(0xFF252525)), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(92.dp).clip(CircleShape).background(refColor.copy(0.8f)), contentAlignment = Alignment.Center) {
                            Text(data.name.take(1).uppercase(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(data.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleBadge(role = com.ileader.app.data.models.UserRole.REFEREE)
                    Surface(shape = RoundedCornerShape(50), color = refColor.copy(0.25f)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gavel, null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Рейтинг ${data.rating}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("${data.city}, ${data.country}", fontSize = 13.sp, color = Color.White.copy(0.7f))
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── SPORT TAGS ──
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            data.sports.forEach { SportTag(it) }
        }

        // ── STATS ──
        Spacer(Modifier.height(16.dp))
        Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), color = CardBg) {
            Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                StatColumn(data.totalJudged.toString(), "Всего")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(data.completedTournaments.toString(), "Отсужено")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(data.activeTournaments.toString(), "Активных")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn("${data.rating}", "Рейтинг")
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

        // ── SPECIALIZATION BY SPORT ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Специализация по спорту") {
            data.sportStats.forEachIndexed { idx, ss ->
                if (idx > 0) { Spacer(Modifier.height(8.dp)); HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f)); Spacer(Modifier.height(8.dp)) }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Accent.copy(0.1f)), contentAlignment = Alignment.Center) {
                        Icon(sportIcon(ss.sport), null, Modifier.size(20.dp), tint = Accent)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(ss.sport, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("${ss.asHead} главным · ${ss.asAssistant} помощником", fontSize = 12.sp, color = TextMuted)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${ss.tournaments}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Accent)
                        Text("турн.", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }

        // ── QUALIFICATION ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Квалификация") {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column {
                    Text("Номер лицензии", fontSize = 12.sp, color = TextMuted)
                    Text(data.licenseNumber, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Категория", fontSize = 12.sp, color = TextMuted)
                    Text(data.licenseCategory, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column {
                    Text("Федерация", fontSize = 12.sp, color = TextMuted)
                    Text(data.federation, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Статус", fontSize = 12.sp, color = TextMuted)
                    Surface(shape = RoundedCornerShape(50), color = Color(0xFF22C55E).copy(0.12f)) {
                        Text("Активна", Modifier.padding(horizontal = 10.dp, vertical = 3.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF22C55E))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Column {
                Text("Действует до", fontSize = 12.sp, color = TextMuted)
                Text(data.licenseExpiry, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            }
        }

        // ── ACTIVE TOURNAMENTS ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Активные турниры") {
            data.activeTournamentList.forEachIndexed { idx, t ->
                if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                RefereeTournamentRow(t) { onTournamentClick("mock-id") }
            }
        }

        // ── UPCOMING TOURNAMENTS ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Предстоящие турниры") {
            data.upcomingTournamentList.forEachIndexed { idx, t ->
                if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                RefereeTournamentRow(t) { onTournamentClick("mock-id") }
            }
        }

        // ── HISTORY ──
        Spacer(Modifier.height(12.dp))
        SectionCard("История судейства") {
            data.historyTournamentList.forEachIndexed { idx, t ->
                if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                RefereeTournamentRow(t) { onTournamentClick("mock-id") }
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

        // ── REVIEWS ──
        Spacer(Modifier.height(12.dp))
        SectionCard("Отзывы организаторов") {
            data.reviews.forEachIndexed { idx, review ->
                if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.size(40.dp).background(Accent.copy(0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(review.author.take(1).uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Accent)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text(review.author, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("${review.rating}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(review.comment, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ══════════════════════════════════════════════════════════
// Referee tournament row with role badge
// ══════════════════════════════════════════════════════════

@Composable
private fun RefereeTournamentRow(t: MockRefTournament, onClick: () -> Unit) {
    val roleColor = when (t.role) { "head" -> Color(0xFFEF4444); "assistant" -> Color(0xFF7C3AED); else -> Color(0xFF3B82F6) }
    val roleLabel = when (t.role) { "head" -> "Главный"; "assistant" -> "Помощник"; "line" -> "Линейный"; else -> "Судья" }
    val statusColor = when (t.status) {
        "in_progress" -> Color(0xFF22C55E); "check_in" -> Color(0xFFF59E0B)
        "registration_open" -> Color(0xFF3B82F6); "completed" -> DarkTheme.TextMuted; else -> DarkTheme.TextMuted
    }
    val statusLabel = when (t.status) {
        "in_progress" -> "Идёт"; "check_in" -> "Check-in"
        "registration_open" -> "Регистрация"; "completed" -> "Завершён"; else -> t.status
    }

    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(44.dp).background(roleColor.copy(0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Gavel, null, tint = roleColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(t.sport, fontSize = 12.sp, color = DarkTheme.TextMuted)
                Text("·", fontSize = 12.sp, color = DarkTheme.TextMuted)
                Text(t.location, fontSize = 12.sp, color = DarkTheme.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Surface(shape = RoundedCornerShape(50), color = roleColor.copy(0.12f)) {
                Text(roleLabel, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = roleColor)
            }
            Spacer(Modifier.height(3.dp))
            Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                Text(statusLabel, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Medium, color = statusColor)
            }
        }
    }
}
