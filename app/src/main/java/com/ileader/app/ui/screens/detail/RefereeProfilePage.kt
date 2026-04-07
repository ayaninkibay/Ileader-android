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
import com.ileader.app.data.remote.dto.LicenseDto
import com.ileader.app.data.remote.dto.RefereeAssignmentDto
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

@Composable
fun RefereeProfilePage(
    refereeId: String,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    vm: RefereeProfileViewModel = viewModel()
) {
    LaunchedEffect(refereeId) { vm.load(refereeId) }

    when (val s = vm.state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = s.message, onRetry = { vm.load(refereeId) })
        is UiState.Success -> RefereeProfileContent(
            data = s.data,
            onBack = onBack,
            onTournamentClick = onTournamentClick
        )
    }
}

@Composable
private fun RefereeProfileContent(
    data: RefereeProfileData,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit
) {
    val profile = data.profile
    val bannerUrl = remember(data.primarySportName) { sportImageUrl(data.primarySportName) }

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
                // Avatar
                val refColor = Color(0xFFEF4444)
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(112.dp).background(Brush.sweepGradient(listOf(refColor, refColor.copy(0.5f), Color(0xFF7C3AED), refColor)), CircleShape))
                    Box(Modifier.size(106.dp).clip(CircleShape).background(Color(0xFF1a1a1a)))
                    if (profile.avatarUrl != null) {
                        AsyncImage(
                            profile.avatarUrl, null,
                            Modifier.size(100.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(Modifier.size(100.dp).clip(CircleShape).background(refColor.copy(0.8f)), contentAlignment = Alignment.Center) {
                            Text((profile.name ?: "?").take(1).uppercase(), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(profile.name ?: "Судья", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoleBadge(role = com.ileader.app.data.models.UserRole.REFEREE)
                    val location = buildString {
                        profile.city?.let { append(it) }
                        profile.country?.let { if (isNotEmpty()) append(", "); append(it) }
                    }
                    if (location.isNotEmpty()) {
                        Text("·", fontSize = 14.sp, color = Color.White.copy(0.7f))
                        Text(location, fontSize = 13.sp, color = Color.White.copy(0.7f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── SPORT CHIPS ──
        if (data.sports.isNotEmpty()) {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                data.sports.forEach { s ->
                    SportTag(s.sports?.name ?: "")
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── STATS ──
        Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), color = CardBg) {
            Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                StatColumn(data.totalJudged.toString(), "Всего")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(data.completed.toString(), "Отсужено")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(data.active.toString(), "Активных")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                val rating = data.stats.firstOrNull()?.rating ?: 0
                StatColumn(rating.toString(), "Рейтинг")
            }
        }

        // ── CONTACTS ──
        val hasContacts = !profile.phone.isNullOrEmpty() || !profile.email.isNullOrEmpty() || !profile.city.isNullOrEmpty()
        if (hasContacts) {
            Spacer(Modifier.height(16.dp))
            SectionCard("Контакты") {
                profile.phone?.takeIf { it.isNotEmpty() }?.let {
                    ContactRow(Icons.Outlined.Phone, "Телефон", it)
                    Spacer(Modifier.height(8.dp))
                }
                profile.email?.takeIf { it.isNotEmpty() }?.let {
                    ContactRow(Icons.Outlined.Email, "Email", it)
                    Spacer(Modifier.height(8.dp))
                }
                val loc = buildString {
                    profile.city?.let { append(it) }
                    profile.country?.let { if (isNotEmpty()) append(", "); append(it) }
                }
                if (loc.isNotEmpty()) {
                    ContactRow(Icons.Outlined.LocationOn, "Город", loc)
                }
            }
        }

        // ── QUALIFICATION (License) ──
        data.license?.let { lic ->
            Spacer(Modifier.height(12.dp))
            LicenseSection(lic)
        }

        // ── ACTIVE TOURNAMENTS ──
        if (data.activeTournaments.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Активные турниры") {
                data.activeTournaments.forEachIndexed { idx, a ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    RefereeAssignmentRow(a, onTournamentClick)
                }
            }
        }

        // ── UPCOMING TOURNAMENTS ──
        val upcoming = data.assignments.filter { it.tournaments?.status == "registration_open" }
        if (upcoming.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Предстоящие турниры") {
                upcoming.forEachIndexed { idx, a ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    RefereeAssignmentRow(a, onTournamentClick)
                }
            }
        }

        // ── HISTORY ──
        if (data.historyTournaments.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("История судейства") {
                data.historyTournaments.forEachIndexed { idx, a ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    RefereeAssignmentRow(a, onTournamentClick)
                }
            }
        }

        // ── EMPTY STATE ──
        if (data.assignments.isEmpty() && data.license == null) {
            Spacer(Modifier.height(24.dp))
            EmptyState(
                icon = Icons.Outlined.Gavel,
                title = "Нет данных",
                subtitle = "У судьи пока нет назначений на турниры"
            )
        }

        Spacer(Modifier.height(100.dp))
    }
}

// ══════════════════════════════════════════════════════════
// License section
// ══════════════════════════════════════════════════════════

@Composable
private fun LicenseSection(lic: LicenseDto) {
    SectionCard("Квалификация") {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Column {
                Text("Номер лицензии", fontSize = 12.sp, color = TextMuted)
                Text(lic.number ?: "—", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Категория", fontSize = 12.sp, color = TextMuted)
                Text(lic.category ?: "—", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Column {
                Text("Федерация", fontSize = 12.sp, color = TextMuted)
                Text(lic.federation ?: "—", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Статус", fontSize = 12.sp, color = TextMuted)
                val statusColor = when (lic.status) {
                    "active" -> Color(0xFF22C55E)
                    "expired" -> Color(0xFFEF4444)
                    "suspended" -> Color(0xFFF59E0B)
                    else -> TextMuted
                }
                val statusLabel = when (lic.status) {
                    "active" -> "Активна"
                    "expired" -> "Истекла"
                    "suspended" -> "Приостановлена"
                    else -> lic.status ?: "—"
                }
                Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                    Text(statusLabel, Modifier.padding(horizontal = 10.dp, vertical = 3.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                }
            }
        }
        if (lic.expiryDate != null) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column {
                    Text("Действует до", fontSize = 12.sp, color = TextMuted)
                    Text(formatDateShort(lic.expiryDate), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Tournament assignment row
// ══════════════════════════════════════════════════════════

@Composable
private fun RefereeAssignmentRow(a: RefereeAssignmentDto, onTournamentClick: (String) -> Unit) {
    val role = a.role ?: "referee"
    val roleColor = when (role) { "head" -> Color(0xFFEF4444); "assistant" -> Color(0xFF7C3AED); else -> Color(0xFF3B82F6) }
    val roleLabel = when (role) { "head" -> "Главный"; "assistant" -> "Помощник"; "line" -> "Линейный"; else -> "Судья" }

    val t = a.tournaments
    val status = t?.status ?: ""
    val statusColor = when (status) {
        "in_progress" -> Color(0xFF22C55E); "check_in" -> Color(0xFFF59E0B)
        "registration_open" -> Color(0xFF3B82F6); "completed" -> TextMuted; else -> TextMuted
    }
    val statusLabel = when (status) {
        "in_progress" -> "Идёт"; "check_in" -> "Check-in"
        "registration_open" -> "Регистрация"; "completed" -> "Завершён"; else -> status
    }

    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onTournamentClick(a.tournamentId) }.padding(vertical = 4.dp),
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
            Text(t?.name ?: "Турнир", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                val sportName = t?.sports?.name ?: ""
                if (sportName.isNotEmpty()) Text(sportName, fontSize = 12.sp, color = TextMuted)
                val locName = t?.locations?.name ?: t?.locations?.city
                if (locName != null) {
                    Text("·", fontSize = 12.sp, color = TextMuted)
                    Text(locName, fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
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

// ══════════════════════════════════════════════════════════
// Date formatter
// ══════════════════════════════════════════════════════════

private fun formatDateShort(dateStr: String?): String {
    if (dateStr == null) return "—"
    return try {
        val parts = dateStr.take(10).split("-")
        if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else dateStr
    } catch (_: Exception) { dateStr }
}
