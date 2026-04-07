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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.UserRole
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
    val license = data.license
    val goals = data.goals
    val isReferee = user.role == UserRole.REFEREE

    val primarySportName = remember(sports) { sports.firstOrNull()?.sports?.name ?: "" }
    val bannerUrl = remember(primarySportName) {
        if (primarySportName.isNotEmpty()) sportImageUrl(primarySportName) else null
    }

    // Aggregate stats
    val totalTournaments = stats.sumOf { it.tournaments }
    val totalWins = stats.sumOf { it.wins }
    val totalPodiums = stats.sumOf { it.podiums }
    val topRating = if (stats.isNotEmpty()) stats.maxOf { it.rating } else 0

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())
    ) {
        // ══════════════════════════════════════
        // HERO BANNER (260dp like AthleteProfilePage)
        // ══════════════════════════════════════
        Box(modifier = Modifier.fillMaxWidth()) {
            val heroShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            if (bannerUrl != null) {
                AsyncImage(bannerUrl, null, Modifier.fillMaxWidth().height(260.dp).clip(heroShape), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxWidth().height(260.dp).clip(heroShape).background(Color.Black.copy(0.7f)))
            } else {
                Box(Modifier.fillMaxWidth().height(260.dp).clip(heroShape)
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

            // Avatar + name + nickname
            Column(
                Modifier.fillMaxWidth().statusBarsPadding().padding(top = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with sweep gradient border
                val sColor = if (primarySportName.isNotEmpty()) sportColor(primarySportName) else Accent
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(112.dp).background(Brush.sweepGradient(listOf(sColor, sColor.copy(0.5f), Color(0xFF3B82F6), sColor)), CircleShape))
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
                Text(profile.name ?: "Пользователь", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (!profile.nickname.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("@${profile.nickname}", fontSize = 14.sp, color = Color.White.copy(0.6f))
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoleBadge(role = user.role)
                    if (!profile.city.isNullOrEmpty()) {
                        Text("·", fontSize = 14.sp, color = Color.White.copy(0.7f))
                        val location = buildString {
                            append(profile.city)
                            if (!profile.country.isNullOrEmpty()) append(", ${profile.country}")
                        }
                        Text(location, fontSize = 13.sp, color = Color.White.copy(0.7f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ══════════════════════════════════════
        // SPORT TAGS + AGE CATEGORY
        // ══════════════════════════════════════
        if (sports.isNotEmpty() || profile.ageCategory != null) {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sports.forEach { sport -> SportTag(sport.sports?.name ?: "") }
                profile.ageCategory?.let { age ->
                    val ageLabel = when (age) { "junior" -> "Юниор"; "adult" -> "Взрослый"; "senior" -> "Ветеран"; else -> age }
                    Surface(shape = RoundedCornerShape(50), color = Color(0xFF3B82F6).copy(0.12f)) {
                        Text(ageLabel, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3B82F6))
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // STATS (4 columns like AthleteProfilePage)
        // ══════════════════════════════════════
        Spacer(Modifier.height(16.dp))
        if (isReferee) {
            val totalJudged = data.refereeAssignments.size
            val completed = data.refereeAssignments.count { it.tournaments?.status == "completed" }
            val active = data.refereeAssignments.count { it.tournaments?.status in listOf("registration_open", "in_progress", "check_in") }
            Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), color = CardBg) {
                Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                    StatColumn(totalJudged.toString(), "Всего")
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    StatColumn(completed.toString(), "Отсужено")
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    StatColumn(active.toString(), "Активных")
                }
            }
        } else if (stats.isNotEmpty()) {
            Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), color = CardBg) {
                Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                    StatColumn(totalTournaments.toString(), "Турниров")
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    StatColumn(totalWins.toString(), "Побед")
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    StatColumn(totalPodiums.toString(), "Подиумов")
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    StatColumn(topRating.toString(), "Рейтинг")
                }
            }
        }

        // ══════════════════════════════════════
        // BIO
        // ══════════════════════════════════════
        if (!profile.bio.isNullOrEmpty()) {
            Spacer(Modifier.height(16.dp))
            SectionCard("О себе") {
                Text(profile.bio, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)
            }
        }

        // ══════════════════════════════════════
        // CONTACTS
        // ══════════════════════════════════════
        if (!profile.phone.isNullOrEmpty() || !profile.email.isNullOrEmpty() || !profile.city.isNullOrEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Контакты") {
                var hasItem = false
                profile.phone?.let { phone ->
                    if (phone.isNotEmpty()) {
                        ContactRow(Icons.Outlined.Phone, "Телефон", phone)
                        hasItem = true
                    }
                }
                profile.email?.let { email ->
                    if (email.isNotEmpty()) {
                        if (hasItem) Spacer(Modifier.height(8.dp))
                        ContactRow(Icons.Outlined.Email, "Email", email)
                        hasItem = true
                    }
                }
                if (!profile.city.isNullOrEmpty()) {
                    if (hasItem) Spacer(Modifier.height(8.dp))
                    val location = buildString {
                        append(profile.city)
                        if (!profile.country.isNullOrEmpty()) append(", ${profile.country}")
                    }
                    ContactRow(Icons.Outlined.LocationOn, "Город", location)
                }
            }
        }

        // ══════════════════════════════════════
        // TEAM
        // ══════════════════════════════════════
        if (membership != null) {
            val team = membership.teams
            if (team != null) {
                Spacer(Modifier.height(12.dp))
                SectionCard("Команда") {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(48.dp).background(Accent.copy(0.1f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text((team.name ?: "?").take(1).uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Accent)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(team.name ?: "Команда", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            val roleName = when (membership.role) { "captain" -> "Капитан"; "member" -> "Участник"; "reserve" -> "Запасной"; else -> membership.role ?: "" }
                            if (roleName.isNotEmpty()) {
                                Text(roleName, fontSize = 13.sp, color = TextMuted)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // LICENSE
        // ══════════════════════════════════════
        if (license != null) {
            Spacer(Modifier.height(12.dp))
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
                        val statusColor = when (license.status) { "active" -> Color(0xFF22C55E); "expired" -> Color(0xFFEF4444); else -> TextMuted }
                        val statusLabel = when (license.status) { "active" -> "Активна"; "expired" -> "Истекла"; "suspended" -> "Приостановлена"; else -> license.status ?: "—" }
                        Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                            Text(statusLabel, Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                        }
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // UPCOMING TOURNAMENTS (active + upcoming)
        // ══════════════════════════════════════
        if (!isReferee && data.upcomingTournaments.isNotEmpty()) {
            val active = data.upcomingTournaments.filter { it.status in listOf("in_progress", "check_in") }
            val upcoming = data.upcomingTournaments.filter { it.status in listOf("registration_open") }

            if (active.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                SectionCard("Активные турниры") {
                    active.forEachIndexed { idx, t ->
                        if (idx > 0) {
                            HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                        }
                        TournamentRow(
                            name = t.name,
                            sport = t.sportName ?: "",
                            location = t.locationName ?: "",
                            date = t.startDate ?: "",
                            status = t.status ?: "",
                            onClick = {}
                        )
                    }
                }
            }

            if (upcoming.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                SectionCard("Предстоящие турниры") {
                    upcoming.forEachIndexed { idx, t ->
                        if (idx > 0) {
                            HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                        }
                        TournamentRow(
                            name = t.name,
                            sport = t.sportName ?: "",
                            location = t.locationName ?: "",
                            date = t.startDate ?: "",
                            status = t.status ?: "",
                            onClick = {}
                        )
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // REFEREE TOURNAMENTS
        // ══════════════════════════════════════
        if (isReferee && data.refereeAssignments.isNotEmpty()) {
            val activeRef = data.refereeAssignments.filter { it.tournaments?.status in listOf("in_progress", "check_in", "registration_open") }
            val completedRef = data.refereeAssignments.filter { it.tournaments?.status == "completed" }

            if (activeRef.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                SectionCard("Активные турниры") {
                    activeRef.forEachIndexed { idx, a ->
                        if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                        TournamentRow(
                            name = a.tournaments?.name ?: "—",
                            sport = a.tournaments?.sports?.name ?: "",
                            location = "",
                            date = a.tournaments?.startDate ?: "",
                            status = a.tournaments?.status ?: "",
                            onClick = {}
                        )
                    }
                }
            }

            if (completedRef.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                SectionCard("История судейства") {
                    completedRef.take(5).forEachIndexed { idx, a ->
                        if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                        TournamentRow(
                            name = a.tournaments?.name ?: "—",
                            sport = a.tournaments?.sports?.name ?: "",
                            location = "",
                            date = a.tournaments?.startDate ?: "",
                            status = a.tournaments?.status ?: "",
                            onClick = {}
                        )
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // RESULTS (history)
        // ══════════════════════════════════════
        if (!isReferee && results.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("История турниров") {
                results.forEachIndexed { idx, r ->
                    if (idx > 0) {
                        HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    }
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

        // ══════════════════════════════════════
        // GOALS
        // ══════════════════════════════════════
        if (goals.isNotEmpty()) {
            val activeGoals = goals.filter { it.status == "active" }
            if (activeGoals.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                SectionCard("Цели") {
                    activeGoals.forEachIndexed { idx, g ->
                        if (idx > 0) Spacer(Modifier.height(10.dp))
                        val target = g.targetRating ?: g.targetWins ?: g.targetPoints ?: g.targetPodiums ?: 100
                        val current = when {
                            g.targetRating != null -> topRating
                            g.targetWins != null -> g.currentWins ?: 0
                            g.targetPoints != null -> g.currentPoints ?: 0
                            g.targetPodiums != null -> g.currentPodiums ?: 0
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
        }

        // ══════════════════════════════════════
        // RATING BY SPORT (keep — useful extra)
        // ══════════════════════════════════════
        if (!isReferee && stats.size > 1) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Рейтинг по спорту") {
                stats.forEachIndexed { idx, sportStat ->
                    if (idx > 0) Spacer(Modifier.height(10.dp))
                    val name = sportStat.sportName ?: ""
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(TextMuted.copy(0.08f)), contentAlignment = Alignment.Center) {
                            Icon(sportIcon(name), null, Modifier.size(18.dp), tint = TextMuted)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("${sportStat.tournaments} турн. · ${sportStat.wins} поб.", fontSize = 12.sp, color = TextMuted)
                        }
                        Text("${sportStat.rating}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Accent)
                    }
                }
            }
        }

        // ══════════════════════════════════════
        // MESSAGE BUTTON
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
