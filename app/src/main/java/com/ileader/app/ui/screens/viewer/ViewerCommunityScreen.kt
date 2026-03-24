package com.ileader.app.ui.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.CommunityProfileDto
import com.ileader.app.data.remote.dto.TeamWithStatsDto
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ViewerCommunityViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

private enum class CommunityTab(val label: String) {
    TEAMS("Команды"), ATHLETES("Спортсмены"), TRAINERS("Тренеры"), REFEREES("Судьи")
}

@Composable
fun ViewerCommunityScreen(
    user: User,
    onNavigateToAthleteProfile: (String) -> Unit = {},
    onNavigateToTrainerProfile: (String) -> Unit = {},
    onNavigateToRefereeProfile: (String) -> Unit = {},
    onNavigateToTeamProfile: (String) -> Unit = {}
) {
    val viewModel: ViewerCommunityViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load() }
        is UiState.Success -> {
            val data = s.data
            var activeTab by remember { mutableStateOf(CommunityTab.TEAMS) }
            var searchQuery by remember { mutableStateOf("") }
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            Column(
                Modifier.fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                FadeIn(visible, 0) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Сообщество", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Команды, тренеры, судьи и звёзды спорта", fontSize = 14.sp, color = TextSecondary)
                        }
                        UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
                    }
                }

                Spacer(Modifier.height(20.dp))

                FadeIn(visible, 200) {
                    ScrollableTabRow(
                        selectedTabIndex = CommunityTab.entries.indexOf(activeTab),
                        containerColor = Color.Transparent, edgePadding = 0.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[CommunityTab.entries.indexOf(activeTab)]),
                                color = Accent, height = 3.dp
                            )
                        },
                        divider = {}
                    ) {
                        CommunityTab.entries.forEach { tab ->
                            Tab(
                                selected = activeTab == tab, onClick = { activeTab = tab },
                                text = { Text(tab.label, fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp) },
                                selectedContentColor = Accent, unselectedContentColor = TextSecondary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                FadeIn(visible, 300) {
                    DarkSearchField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Поиск по имени, городу..."
                    )
                }

                Spacer(Modifier.height(16.dp))

                FadeIn(visible, 400) {
                    when (activeTab) {
                        CommunityTab.TEAMS -> {
                            val filtered = data.teams.filter { searchQuery.isBlank() || it.name.contains(searchQuery, true) || (it.city ?: "").contains(searchQuery, true) }
                            if (filtered.isEmpty()) EmptyState("Команды не найдены", "Попробуйте изменить поиск")
                            else Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { filtered.forEach { TeamCard(it) { onNavigateToTeamProfile(it.id) } } }
                        }
                        CommunityTab.ATHLETES -> {
                            val filtered = data.athletes.filter { searchQuery.isBlank() || (it.name ?: "").contains(searchQuery, true) || (it.city ?: "").contains(searchQuery, true) }
                            if (filtered.isEmpty()) EmptyState("Спортсмены не найдены", "Попробуйте изменить поиск")
                            else Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { filtered.forEach { AthleteCard(it) { onNavigateToAthleteProfile(it.id) } } }
                        }
                        CommunityTab.TRAINERS -> {
                            val filtered = data.trainers.filter { searchQuery.isBlank() || (it.name ?: "").contains(searchQuery, true) || (it.city ?: "").contains(searchQuery, true) }
                            if (filtered.isEmpty()) EmptyState("Тренеры не найдены", "Попробуйте изменить поиск")
                            else Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { filtered.forEach { TrainerCard(it) { onNavigateToTrainerProfile(it.id) } } }
                        }
                        CommunityTab.REFEREES -> {
                            val filtered = data.referees.filter { searchQuery.isBlank() || (it.name ?: "").contains(searchQuery, true) || (it.city ?: "").contains(searchQuery, true) }
                            if (filtered.isEmpty()) EmptyState("Судьи не найдены", "Попробуйте изменить поиск")
                            else Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { filtered.forEach { RefereeCard(it) { onNavigateToRefereeProfile(it.id) } } }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Card composables ────────────────────────────────────────────────

@Composable
private fun TeamCard(team: TeamWithStatsDto, onClick: () -> Unit) {
    DarkCard(Modifier.clickable { onClick() }) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(name = team.name, gradient = listOf(Accent, AccentDark), isRound = false)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(team.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("${team.city ?: ""} · ${team.sportName}", fontSize = 12.sp, color = TextSecondary)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                InlineStat("${team.memberCount}", "Членов")
            }
            if (team.ownerName.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = CardBorder.copy(alpha = 0.3f)) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(24.dp).clip(CircleShape).background(AccentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Accent, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("Владелец: ", fontSize = 12.sp, color = TextSecondary)
                        Text(team.ownerName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun AthleteCard(profile: CommunityProfileDto, onClick: () -> Unit) {
    DarkCard(Modifier.clickable { onClick() }) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(name = profile.name ?: "?", gradient = listOf(Accent, AccentDark))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(profile.name ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (profile.subtypeLabel != null) {
                            Text("${profile.subtypeLabel} · ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                        }
                        Text(profile.city ?: "", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                InlineStat("${profile.primaryRating}", "Рейтинг", valueColor = Accent)
            }
            if (profile.primarySportName.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = AccentSoft) {
                    Row(
                        Modifier.fillMaxWidth().padding(10.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Text(profile.primarySportName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Профиль", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                            Icon(Icons.Default.ChevronRight, null, tint = Accent, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainerCard(profile: CommunityProfileDto, onClick: () -> Unit) {
    DarkCard(Modifier.clickable { onClick() }) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(name = profile.name ?: "?", gradient = listOf(Accent, AccentDark))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(profile.name ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("${profile.city ?: ""} · ${profile.primarySportName}", fontSize = 12.sp, color = TextSecondary)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                InlineStat("${profile.primaryRating}", "Рейтинг", valueColor = Accent)
            }
        }
    }
}

@Composable
private fun RefereeCard(profile: CommunityProfileDto, onClick: () -> Unit) {
    DarkCard(Modifier.clickable { onClick() }) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(name = profile.name ?: "?", gradient = listOf(Accent, AccentDark))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(profile.name ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("${profile.city ?: ""} · ${profile.primarySportName}", fontSize = 12.sp, color = TextSecondary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = AccentSoft) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Shield, null, tint = Accent, modifier = Modifier.size(14.dp))
                    Text("Судья", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                }
            }
        }
    }
}

// ── Shared helpers ──────────────────────────────────────────────────

@Composable
internal fun InitialsAvatar(name: String, gradient: List<Color>, isRound: Boolean = true, size: Int = 48) {
    val initials = name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifEmpty { "?" }
    Box(
        Modifier.size(size.dp).clip(if (isRound) CircleShape else RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, fontSize = (size / 3).sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}

@Composable
internal fun InlineStat(value: String, label: String, valueColor: Color = TextPrimary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}
