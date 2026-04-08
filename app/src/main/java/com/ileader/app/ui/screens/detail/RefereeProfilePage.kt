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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.RefereeProfileData
import com.ileader.app.ui.viewmodels.RefereeProfileViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

// ══════════════════════════════════════════════════════════
// Main Screen
// ══════════════════════════════════════════════════════════

@Composable
fun RefereeProfilePage(
    refereeId: String,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    viewModel: RefereeProfileViewModel = viewModel()
) {
    LaunchedEffect(refereeId) { viewModel.load(refereeId) }

    when (val state = viewModel.state) {
        is UiState.Loading -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Профиль судьи", onBack)
                LoadingScreen()
            }
        }
        is UiState.Error -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Профиль судьи", onBack)
                ErrorScreen(state.message) { viewModel.load(refereeId) }
            }
        }
        is UiState.Success -> {
            RefereeProfileContent(
                data = state.data,
                onBack = onBack,
                onTournamentClick = onTournamentClick,
                onProfileClick = onProfileClick
            )
        }
    }
}

@Composable
private fun RefereeProfileContent(
    data: RefereeProfileData,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit,
    onProfileClick: (String) -> Unit
) {
    val profile = data.profile
    val sportNames = data.sports.mapNotNull { it.sports?.name }
    val primarySportName = data.primarySportName
    val bannerUrl = remember(primarySportName) { sportImageUrl(primarySportName.ifBlank { "Картинг" }) }
    val refColor = Color(0xFFEF4444)

    // Group stats by sport
    val sportStats = data.stats

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
                    if (profile.avatarUrl != null) {
                        AsyncImage(profile.avatarUrl, null, Modifier.size(92.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Box(Modifier.size(92.dp).clip(CircleShape).background(refColor.copy(0.8f)), contentAlignment = Alignment.Center) {
                            Text((profile.name ?: "?").take(1).uppercase(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(profile.name ?: "Без имени", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleBadge(role = com.ileader.app.data.models.UserRole.REFEREE)
                    val primaryRating = data.stats.firstOrNull()?.rating ?: 1000
                    Surface(shape = RoundedCornerShape(50), color = refColor.copy(0.25f)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gavel, null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Рейтинг $primaryRating", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                val location = listOfNotNull(profile.city, profile.country).joinToString(", ")
                if (location.isNotBlank()) {
                    Text(location, fontSize = 13.sp, color = Color.White.copy(0.7f))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── SPORT TAGS ──
        if (sportNames.isNotEmpty()) {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sportNames.forEach { SportTag(it) }
            }
        }

        // ── STATS ──
        Spacer(Modifier.height(16.dp))
        Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), color = CardBg) {
            Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                StatColumn(data.totalJudged.toString(), "Всего")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(data.completed.toString(), "Отсужено")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(data.active.toString(), "Активных")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn("${data.stats.firstOrNull()?.rating ?: 0}", "Рейтинг")
            }
        }

        // ── CONTACTS ──
        if (!profile.phone.isNullOrBlank() || !profile.email.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))
            SectionCard("Контакты") {
                if (!profile.phone.isNullOrBlank()) {
                    ContactRow(Icons.Outlined.Phone, "Телефон", profile.phone)
                    Spacer(Modifier.height(8.dp))
                }
                if (!profile.email.isNullOrBlank()) {
                    ContactRow(Icons.Outlined.Email, "Email", profile.email)
                    Spacer(Modifier.height(8.dp))
                }
                val loc = listOfNotNull(profile.city, profile.country).joinToString(", ")
                if (loc.isNotBlank()) {
                    ContactRow(Icons.Outlined.LocationOn, "Город", loc)
                }
            }
        }

        // ── SPECIALIZATION BY SPORT ──
        if (sportStats.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Специализация по спорту") {
                sportStats.forEachIndexed { idx, ss ->
                    if (idx > 0) { Spacer(Modifier.height(8.dp)); HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f)); Spacer(Modifier.height(8.dp)) }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Accent.copy(0.1f)), contentAlignment = Alignment.Center) {
                            Icon(sportIcon(ss.sportName ?: ""), null, Modifier.size(20.dp), tint = Accent)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ss.sportName ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("${ss.tournaments} турниров", fontSize = 12.sp, color = TextMuted)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${ss.tournaments}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Accent)
                            Text("турн.", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // ── QUALIFICATION ──
        val license = data.license
        if (license != null) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Квалификация") {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Column {
                        Text("Номер лицензии", fontSize = 12.sp, color = TextMuted)
                        Text(license.number ?: "—", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Категория", fontSize = 12.sp, color = TextMuted)
                        Text(license.category ?: "—", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Column {
                        Text("Федерация", fontSize = 12.sp, color = TextMuted)
                        Text(license.federation ?: "—", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Статус", fontSize = 12.sp, color = TextMuted)
                        val statusActive = license.status == "active"
                        Surface(shape = RoundedCornerShape(50), color = (if (statusActive) Color(0xFF22C55E) else Color(0xFFEF4444)).copy(0.12f)) {
                            Text(
                                if (statusActive) "Активна" else (license.status ?: "—"),
                                Modifier.padding(horizontal = 10.dp, vertical = 3.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = if (statusActive) Color(0xFF22C55E) else Color(0xFFEF4444)
                            )
                        }
                    }
                }
                if (!license.expiryDate.isNullOrBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Column {
                        Text("Действует до", fontSize = 12.sp, color = TextMuted)
                        Text(license.expiryDate, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                }
            }
        }

        // ── ACTIVE TOURNAMENTS ──
        if (data.activeTournaments.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Активные турниры") {
                data.activeTournaments.forEachIndexed { idx, t ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    RefereeTournamentRow(t) { onTournamentClick(t.tournamentId) }
                }
            }
        }

        // ── UPCOMING TOURNAMENTS ──
        if (data.upcomingTournaments.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Предстоящие турниры") {
                data.upcomingTournaments.forEachIndexed { idx, t ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    RefereeTournamentRow(t) { onTournamentClick(t.tournamentId) }
                }
            }
        }

        // ── HISTORY ──
        if (data.historyTournaments.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("История судейства") {
                data.historyTournaments.forEachIndexed { idx, t ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    RefereeTournamentRow(t) { onTournamentClick(t.tournamentId) }
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
private fun RefereeTournamentRow(t: RefereeAssignmentDto, onClick: () -> Unit) {
    val roleColor = when (t.role) { "head" -> Color(0xFFEF4444); "assistant" -> Color(0xFF7C3AED); else -> Color(0xFF3B82F6) }
    val roleLabel = when (t.role) { "head" -> "Главный"; "assistant" -> "Помощник"; "line" -> "Линейный"; else -> "Судья" }
    val status = t.tournaments?.status ?: ""
    val statusColor = when (status) {
        "in_progress" -> Color(0xFF22C55E); "check_in" -> Color(0xFFF59E0B)
        "registration_open" -> Color(0xFF3B82F6); "completed" -> DarkTheme.TextMuted; else -> DarkTheme.TextMuted
    }
    val statusLabel = when (status) {
        "in_progress" -> "Идёт"; "check_in" -> "Check-in"
        "registration_open" -> "Регистрация"; "completed" -> "Завершён"; else -> status
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
            Text(t.tournaments?.name ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(t.tournaments?.sports?.name ?: "", fontSize = 12.sp, color = DarkTheme.TextMuted)
                Text("·", fontSize = 12.sp, color = DarkTheme.TextMuted)
                Text(t.tournaments?.locations?.name ?: "", fontSize = 12.sp, color = DarkTheme.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
