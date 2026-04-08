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
import com.ileader.app.data.remote.dto.*
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.TrainerProfileData
import com.ileader.app.ui.viewmodels.TrainerProfileViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

@Composable
fun TrainerProfilePage(
    trainerId: String,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit = {},
    onAthleteClick: (String) -> Unit = {},
    onTeamClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    viewModel: TrainerProfileViewModel = viewModel()
) {
    LaunchedEffect(trainerId) { viewModel.load(trainerId) }

    when (val state = viewModel.state) {
        is UiState.Loading -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Профиль тренера", onBack)
                LoadingScreen()
            }
        }
        is UiState.Error -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Профиль тренера", onBack)
                ErrorScreen(state.message) { viewModel.load(trainerId) }
            }
        }
        is UiState.Success -> {
            TrainerProfileContent(
                data = state.data,
                onBack = onBack,
                onTournamentClick = onTournamentClick,
                onAthleteClick = onAthleteClick,
                onTeamClick = onTeamClick
            )
        }
    }
}

@Composable
private fun TrainerProfileContent(
    data: TrainerProfileData,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit,
    onAthleteClick: (String) -> Unit,
    onTeamClick: (String) -> Unit
) {
    val profile = data.profile
    val sportName = data.primarySportName
    val bannerUrl = remember(sportName) { sportImageUrl(sportName.ifBlank { "Картинг" }) }
    val trainerColor = Color(0xFF059669)

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

            Box(
                Modifier.statusBarsPadding().padding(16.dp).size(40.dp)
                    .clip(CircleShape).background(Color.White.copy(0.15f)).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Column(
                Modifier.fillMaxWidth().statusBarsPadding().padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(104.dp).background(Brush.sweepGradient(listOf(trainerColor, trainerColor.copy(0.5f), Color(0xFF10B981), trainerColor)), CircleShape))
                    Box(Modifier.size(98.dp).clip(CircleShape).background(Color(0xFF1a1a1a)))
                    if (profile.avatarUrl != null) {
                        AsyncImage(profile.avatarUrl, null, Modifier.size(92.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Box(Modifier.size(92.dp).clip(CircleShape).background(trainerColor.copy(0.8f)), contentAlignment = Alignment.Center) {
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
                    RoleBadge(role = com.ileader.app.data.models.UserRole.TRAINER)
                }
                Spacer(Modifier.height(6.dp))
                val location = listOfNotNull(profile.city, profile.country).joinToString(", ")
                if (location.isNotBlank()) {
                    Text(location, fontSize = 13.sp, color = Color.White.copy(0.7f))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── SPORT TAG ──
        if (sportName.isNotBlank()) {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SportTag(sportName)
            }
        }

        // ── STATS ──
        Spacer(Modifier.height(16.dp))
        Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), color = CardBg) {
            Row(Modifier.padding(vertical = 20.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                StatColumn(data.athleteCount.toString(), "Спортсменов")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(data.totalTournaments.toString(), "Турниров")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn(data.totalWins.toString(), "Побед")
                Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                StatColumn("${data.avgRating}", "Ср. рейтинг")
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
        val team = data.team
        if (team != null) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Команда") {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable { onTeamClick(team.id) }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(56.dp).background(trainerColor.copy(0.1f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(team.name.take(1).uppercase(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = trainerColor)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(team.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        val meta = listOfNotNull(
                            team.sportName.ifBlank { null },
                            if (team.foundedYear > 0) "осн. ${team.foundedYear}" else null
                        ).joinToString(" · ")
                        if (meta.isNotBlank()) Text(meta, fontSize = 12.sp, color = TextMuted)
                        if (team.description.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(team.description, fontSize = 12.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }

                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    InfoChip("Турниров", "${data.totalTournaments}")
                    InfoChip("Побед", "${data.totalWins}")
                    InfoChip("Подиумов", "${data.totalPodiums}")
                    InfoChip("Ср. рейтинг", "${data.avgRating}")
                }
            }

            // ── ATHLETES ──
            if (team.members.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                SectionCard("Спортсмены") {
                    team.members.forEachIndexed { idx, athlete ->
                        if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                        val roleLabel = when (athlete.role) { "captain" -> "Капитан"; "member" -> "Участник"; "reserve" -> "Запасной"; else -> athlete.role }
                        val roleColor = when (athlete.role) { "captain" -> Accent; "member" -> Color(0xFF3B82F6); else -> TextMuted }
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .clickable { onAthleteClick(athlete.id) }.padding(vertical = 4.dp),
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
            }
        }

        // ── ACTIVE TOURNAMENTS ──
        if (data.activeTournaments.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Активные турниры") {
                data.activeTournaments.forEachIndexed { idx, t ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    TournamentRow(
                        name = t.name, sport = t.sportName ?: "", location = t.locationName ?: "",
                        date = t.startDate ?: "", status = t.status ?: ""
                    ) { onTournamentClick(t.id) }
                }
            }
        }

        // ── UPCOMING TOURNAMENTS ──
        if (data.upcomingOnly.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionCard("Предстоящие турниры") {
                data.upcomingOnly.forEachIndexed { idx, t ->
                    if (idx > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                    TournamentRow(
                        name = t.name, sport = t.sportName ?: "", location = t.locationName ?: "",
                        date = t.startDate ?: "", status = t.status ?: ""
                    ) { onTournamentClick(t.id) }
                }
            }
        }

        // ── HISTORY (Results) ──
        if (data.results.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
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

        Spacer(Modifier.height(32.dp))
    }
}
