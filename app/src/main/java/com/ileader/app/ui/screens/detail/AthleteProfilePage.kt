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
import com.ileader.app.ui.viewmodels.AthleteProfileData
import com.ileader.app.ui.viewmodels.AthleteProfileViewModel

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
fun AthleteProfilePage(
    athleteId: String,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit = {},
    onTeamClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    viewModel: AthleteProfileViewModel = viewModel()
) {
    LaunchedEffect(athleteId) { viewModel.load(athleteId) }

    when (val state = viewModel.state) {
        is UiState.Loading -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Профиль спортсмена", onBack)
                LoadingScreen()
            }
        }
        is UiState.Error -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Профиль спортсмена", onBack)
                ErrorScreen(state.message) { viewModel.load(athleteId) }
            }
        }
        is UiState.Success -> {
            AthleteProfileContent(
                data = state.data,
                onBack = onBack,
                onTournamentClick = onTournamentClick,
                onTeamClick = onTeamClick,
                onProfileClick = onProfileClick
            )
        }
    }
}

@Composable
private fun AthleteProfileContent(
    data: AthleteProfileData,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit,
    onTeamClick: (String) -> Unit,
    onProfileClick: (String) -> Unit
) {
    val profile = data.profile
    val primarySport = data.sports.firstOrNull { it.isPrimary } ?: data.sports.firstOrNull()
    val sportName = primarySport?.sports?.name ?: ""
    val primaryStats = data.stats.firstOrNull()
    val bannerUrl = remember(sportName) { sportImageUrl(sportName) }
    val athleteColor = Accent

    val subtypeLabel = when (profile.athleteSubtype) {
        "pilot" -> "Пилот"; "shooter" -> "Стрелок"; "tennis" -> "Теннисист"
        "football" -> "Футболист"; "boxer" -> "Боксёр"; "general" -> "Спортсмен"
        else -> profile.athleteSubtype ?: "Спортсмен"
    }

    // Split tournaments by status
    val activeTournaments = data.upcomingTournaments.filter {
        it.status == "in_progress" || it.status == "check_in"
    }
    val upcomingTournaments = data.upcomingTournaments.filter {
        it.status == "registration_open" || it.status == "registration_closed"
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())
    ) {
        // ── HERO ──
        Box(modifier = Modifier.fillMaxWidth()) {
            val heroShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            if (bannerUrl != null) {
                AsyncImage(bannerUrl, null, Modifier.fillMaxWidth().height(350.dp).clip(heroShape), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxWidth().height(350.dp).clip(heroShape).background(Color.Black.copy(0.7f)))
            } else {
                Box(Modifier.fillMaxWidth().height(350.dp).clip(heroShape)
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
                Modifier.fillMaxWidth().statusBarsPadding().padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(104.dp).background(Brush.sweepGradient(listOf(athleteColor, athleteColor.copy(0.5f), Color(0xFF3B82F6), athleteColor)), CircleShape))
                    Box(Modifier.size(98.dp).clip(CircleShape).background(Color(0xFF1a1a1a)))
                    if (profile.avatarUrl != null) {
                        AsyncImage(profile.avatarUrl, null, Modifier.size(92.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Box(Modifier.size(92.dp).clip(CircleShape).background(athleteColor.copy(0.8f)), contentAlignment = Alignment.Center) {
                            Text((profile.name ?: "?").take(1).uppercase(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(profile.name ?: "Без имени", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (!profile.nickname.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("@${profile.nickname}", fontSize = 14.sp, color = Color.White.copy(0.6f))
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleBadge(role = com.ileader.app.data.models.UserRole.ATHLETE)
                    Surface(shape = RoundedCornerShape(50), color = athleteColor.copy(0.25f)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(subtypeLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
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

        // ── SPORT TAG + AGE ──
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (sportName.isNotBlank()) SportTag(sportName)
            val ageCategory = profile.ageCategory
            if (!ageCategory.isNullOrBlank()) {
                Surface(shape = RoundedCornerShape(50), color = Color(0xFF3B82F6).copy(0.12f)) {
                    Text(ageCategory, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3B82F6))
                }
            }
        }

        // ── STATS ──
        if (primaryStats != null) {
            Spacer(Modifier.height(16.dp))
            Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), color = CardBg) {
                Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                    StatColumn(primaryStats.tournaments.toString(), "Турниров")
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    StatColumn(primaryStats.wins.toString(), "Побед")
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    StatColumn(primaryStats.podiums.toString(), "Подиумов")
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    StatColumn("${primaryStats.rating}", "Рейтинг")
                }
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

        // ── TEAM ──
        val team = data.teamDetail
        val membership = data.membership
        if (team != null && membership != null) {
            Spacer(Modifier.height(16.dp))
            SectionCard("Команда") {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable { onTeamClick(team.id) }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(56.dp).background(Accent.copy(0.1f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (team.logoUrl != null) {
                            AsyncImage(team.logoUrl, null, Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
                        } else {
                            Text(team.name.take(1).uppercase(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Accent)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(team.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        val roleLabel = when (membership.role) { "captain" -> "Капитан"; "member" -> "Участник"; "reserve" -> "Запасной"; else -> membership.role ?: "" }
                        val teamCity = team.city ?: ""
                        val foundedYear = team.foundedYear?.let { "осн. $it" } ?: ""
                        val meta = listOfNotNull(roleLabel.ifBlank { null }, teamCity.ifBlank { null }, foundedYear.ifBlank { null }).joinToString(" · ")
                        Text(meta, fontSize = 12.sp, color = TextMuted)
                        if (!team.description.isNullOrBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(team.description, fontSize = 12.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }

                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    InfoChip("Участников", "${data.teamMembers.size}")
                    if (team.sports?.name != null) InfoChip("Спорт", team.sports.name)
                }
            }
        }

        // ── LICENSE ──
        val license = data.license
        if (license != null) {
            Spacer(Modifier.height(16.dp))
            SectionCard("Лицензия") {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Column {
                        Text("Номер", fontSize = 12.sp, color = TextMuted)
                        Text(license.number ?: "—", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Категория", fontSize = 12.sp, color = TextMuted)
                        Text(license.category ?: "—", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Column {
                        Text("Класс", fontSize = 12.sp, color = TextMuted)
                        Text(license.licenseClass ?: "—", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
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
            }
        }

        // ── ACTIVE TOURNAMENTS ──
        if (activeTournaments.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            SectionCard("Активные турниры") {
                activeTournaments.forEachIndexed { idx, t ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    TournamentRow(
                        name = t.name,
                        sport = t.sportName ?: "",
                        location = t.locationName ?: "",
                        date = t.startDate ?: "",
                        status = t.status ?: ""
                    ) { onTournamentClick(t.id) }
                }
            }
        }

        // ── UPCOMING TOURNAMENTS ──
        if (upcomingTournaments.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            SectionCard("Предстоящие турниры") {
                upcomingTournaments.forEachIndexed { idx, t ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    TournamentRow(
                        name = t.name,
                        sport = t.sportName ?: "",
                        location = t.locationName ?: "",
                        date = t.startDate ?: "",
                        status = t.status ?: ""
                    ) { onTournamentClick(t.id) }
                }
            }
        }

        // ── HISTORY (Results) ──
        if (data.results.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            SectionCard("История турниров") {
                data.results.forEachIndexed { idx, r ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    ResultRow(
                        name = r.tournaments?.name ?: "Турнир",
                        sport = r.tournaments?.sports?.name ?: "",
                        position = r.position,
                        points = r.points ?: 0,
                        date = r.tournaments?.startDate ?: ""
                    )
                }
            }
        }

        // ── GOALS ──
        if (data.goals.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            SectionCard("Цели") {
                data.goals.forEachIndexed { idx, g ->
                    if (idx > 0) Spacer(Modifier.height(10.dp))
                    val target = when (g.type) {
                        "rating" -> g.targetRating ?: 0
                        "wins" -> g.targetWins ?: 0
                        "podiums" -> g.targetPodiums ?: 0
                        "points" -> g.targetPoints ?: 0
                        else -> 100
                    }
                    val current = when (g.type) {
                        "rating" -> primaryStats?.rating ?: 0
                        "wins" -> g.currentWins ?: primaryStats?.wins ?: 0
                        "podiums" -> g.currentPodiums ?: primaryStats?.podiums ?: 0
                        "points" -> g.currentPoints ?: primaryStats?.totalPoints ?: 0
                        else -> g.progress
                    }
                    val progress = if (target > 0) (current.toFloat() / target).coerceIn(0f, 1f) else 0f
                    Column {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text(g.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("$current/$target", fontSize = 12.sp, color = TextMuted)
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF3B82F6),
                            trackColor = Color(0xFF3B82F6).copy(0.12f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ══════════════════════════════════════════════════════════
// Shared composables for all profile pages
// ══════════════════════════════════════════════════════════

@Composable
internal fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    DarkCardPadded(modifier = modifier.padding(horizontal = 16.dp)) {
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
