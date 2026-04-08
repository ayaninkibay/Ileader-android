package com.ileader.app.ui.screens.sport

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.ErrorScreen
import com.ileader.app.ui.components.LoadingScreen
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.LeagueDetailData
import com.ileader.app.ui.viewmodels.LeagueDetailViewModel
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

// ══════════════════════════════════════════════════════════
// Screen
// ══════════════════════════════════════════════════════════

@Composable
fun LeagueDetailScreen(
    leagueId: String,
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit = {},
    viewModel: LeagueDetailViewModel = viewModel()
) {
    LaunchedEffect(leagueId) { viewModel.load(leagueId) }

    when (val state = viewModel.state) {
        is UiState.Loading -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Лига", onBack)
                LoadingScreen()
            }
        }
        is UiState.Error -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Лига", onBack)
                ErrorScreen(state.message) { viewModel.load(leagueId) }
            }
        }
        is UiState.Success -> {
            LeagueDetailContent(
                data = state.data,
                onBack = onBack,
                onProfileClick = onProfileClick
            )
        }
    }
}

@Composable
private fun LeagueDetailContent(
    data: LeagueDetailData,
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit
) {
    val isDark = DarkTheme.isDark
    val league = data.league
    val imageUrl = league.imageUrl

    // Parse scoring table from JSON
    val scoringEntries = remember(league.scoringTable) {
        try {
            val arr = league.scoringTable as? JsonArray ?: return@remember emptyList()
            arr.mapNotNull { el ->
                val obj = el.jsonObject
                val place = (obj["place"] as? JsonPrimitive)?.int ?: return@mapNotNull null
                val points = (obj["points"] as? JsonPrimitive)?.int ?: return@mapNotNull null
                place to points
            }
        } catch (_: Exception) { emptyList() }
    }

    val statusLabel = when (league.status) {
        "in_progress" -> "Идёт"
        "registration_open" -> "Регистрация"
        "completed" -> "Завершена"
        "draft" -> "Черновик"
        else -> league.status ?: ""
    }
    val statusColor = when (league.status) {
        "in_progress" -> ILeaderColors.Success
        "registration_open" -> Color(0xFF3B82F6)
        "completed" -> TextMuted
        else -> TextMuted
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())
    ) {
        // ════════════════════════════════════
        // HERO
        // ════════════════════════════════════
        Box(modifier = Modifier.fillMaxWidth()) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    contentScale = ContentScale.Crop
                )
                Box(
                    Modifier.fillMaxWidth().height(280.dp)
                        .background(Brush.verticalGradient(
                            listOf(Color.Black.copy(if (isDark) 0.2f else 0.4f), Color.Black.copy(0.75f))
                        ))
                )
            } else {
                Box(
                    Modifier.fillMaxWidth().height(280.dp)
                        .background(Brush.verticalGradient(
                            listOf(Color(0xFF1a1a2e), Color(0xFF16213e), Color(0xFF0f3460))
                        ))
                )
            }

            Column(
                Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 20.dp)
            ) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(Color.Black.copy(0.3f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.height(80.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusPill(statusLabel, statusColor)
                    if (data.sportName.isNotBlank()) {
                        Pill(data.sportName, Color.White.copy(0.9f), Color.White.copy(0.15f))
                    }
                    if (!league.season.isNullOrBlank()) {
                        Pill(league.season, Color.White.copy(0.7f), Color.White.copy(0.1f))
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    league.name, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                    color = Color.White, lineHeight = 32.sp, letterSpacing = (-0.5).sp
                )
            }
        }

        // ════════════════════════════════════
        // INFO BLOCK
        // ════════════════════════════════════
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-20).dp),
            shape = RoundedCornerShape(16.dp), color = CardBg, shadowElevation = 0.dp
        ) {
            Column(Modifier.padding(16.dp)) {
                if (data.organizerName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(36.dp).background(Accent.copy(0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Business, null, tint = Accent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Организатор", fontSize = 11.sp, color = TextMuted)
                            Text(data.organizerName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                if (!league.description.isNullOrBlank()) {
                    Text(league.description, fontSize = 14.sp, color = TextSecondary, lineHeight = 20.sp)
                    Spacer(Modifier.height(14.dp))
                }

                HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.2f))
                Spacer(Modifier.height(14.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem(Icons.Outlined.People, "${data.participantCount}", "участников")
                    StatItem(Icons.Outlined.Layers, "${data.completedStages} / ${league.totalStages}", "этапов")
                    val bestOfLabel = if (league.bestOf != null) "Лучшие ${league.bestOf}" else "${league.totalStages}"
                    StatItem(Icons.Outlined.EmojiEvents, bestOfLabel, "из ${league.totalStages}")
                }
            }
        }

        Spacer(Modifier.height(-4.dp))

        // ════════════════════════════════════
        // STANDINGS TABLE
        // ════════════════════════════════════
        if (data.standings.isNotEmpty()) {
            SectionCard(
                title = "Таблица рейтинга",
                icon = Icons.Outlined.WorkspacePremium,
                iconTint = TextMuted
            ) {
                // Header
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text("#", Modifier.width(30.dp), fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text("Спортсмен", Modifier.weight(1f), fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text("Очки", Modifier.width(44.dp), fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Этапы", Modifier.width(40.dp), fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Лучш.", Modifier.width(42.dp), fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
                HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.3f))

                data.standings.forEachIndexed { index, s ->
                    val rank = index + 1
                    val rankBg = when (rank) {
                        1 -> if (isDark) Color(0xFFCA8A04).copy(0.2f) else Color(0xFFFEF9C3)
                        2 -> if (isDark) Color(0xFF6B7280).copy(0.2f) else Color(0xFFF1F5F9)
                        3 -> if (isDark) Color(0xFFB45309).copy(0.2f) else Color(0xFFFEF3C7)
                        else -> Color.Transparent
                    }
                    val rankColor = when (rank) {
                        1 -> Color(0xFFCA8A04); 2 -> Color(0xFF94A3B8); 3 -> Color(0xFFB45309); else -> TextMuted
                    }

                    Row(
                        Modifier.fillMaxWidth()
                            .clickable { onProfileClick(s.athleteId) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (rank <= 3) {
                            Box(
                                Modifier.size(26.dp).clip(CircleShape).background(rankBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$rank", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = rankColor)
                            }
                            Spacer(Modifier.width(4.dp))
                        } else {
                            Text("$rank", Modifier.width(30.dp), fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                        }
                        Text(
                            s.profiles?.name ?: "—", Modifier.weight(1f), fontSize = 13.sp,
                            fontWeight = if (rank <= 3) FontWeight.Bold else FontWeight.Medium,
                            color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text("${s.totalPoints}", Modifier.width(44.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.Center)
                        Text("${s.stagesParticipated}", Modifier.width(40.dp), fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
                        Row(Modifier.width(42.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            if (s.bestFinish != null && s.bestFinish <= 3) {
                                Icon(Icons.Outlined.MilitaryTech, null, tint = Color(0xFFEAB308), modifier = Modifier.size(13.dp))
                            }
                            Text(s.bestFinish?.toString() ?: "—", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    if (index < data.standings.lastIndex) {
                        HorizontalDivider(Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = Border.copy(0.15f))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        // ════════════════════════════════════
        // SCORING TABLE
        // ════════════════════════════════════
        if (scoringEntries.isNotEmpty()) {
            SectionCard(
                title = "Таблица очков",
                icon = Icons.Outlined.Tag,
                iconTint = TextMuted
            ) {
                scoringEntries.chunked(4).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (place, points) ->
                            val isTop3 = place <= 3
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isTop3) {
                                    if (isDark) Color(0xFFCA8A04).copy(0.15f) else Color(0xFFFEF9C3)
                                } else TextMuted.copy(0.1f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isTop3) {
                                        if (isDark) Color(0xFFCA8A04).copy(0.3f) else Color(0xFFFDE68A)
                                    } else Border.copy(0.2f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$place место", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                        color = if (isTop3) Color(0xFFCA8A04) else TextMuted)
                                    Text("$points", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                                    Text("очк.", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    if (row != scoringEntries.chunked(4).last()) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        // ════════════════════════════════════
        // STAGES
        // ════════════════════════════════════
        if (data.stages.isNotEmpty()) {
            SectionCard(
                title = "Этапы",
                icon = Icons.Outlined.CalendarMonth,
                iconTint = TextMuted
            ) {
                data.stages.forEachIndexed { index, stage ->
                    val isCompleted = stage.status == "completed"
                    val isNext = !isCompleted && (index == 0 || data.stages[index - 1].status == "completed")

                    Column(
                        Modifier.fillMaxWidth()
                            .then(if (isNext) Modifier.background(Accent.copy(0.15f), RoundedCornerShape(12.dp)) else Modifier)
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text(
                                "Этап ${stage.stageNumber}", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                color = if (isNext) Accent else TextPrimary
                            )
                            val (sLabel, sColor) = when (stage.status) {
                                "completed" -> "Завершён" to ILeaderColors.Success
                                "in_progress" -> "Идёт" to ILeaderColors.Info
                                else -> if (isNext) "Следующий" to Accent else "Будущий" to TextMuted
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp), color = sColor.copy(0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, sColor.copy(0.2f))
                            ) {
                                Text(sLabel, Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = sColor)
                            }
                        }

                        stage.title?.let {
                            Spacer(Modifier.height(2.dp))
                            Text(it, fontSize = 13.sp, color = TextSecondary)
                        }
                        stage.tournaments?.name?.let {
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.EmojiEvents, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(it, fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        stage.tournaments?.startDate?.let { date ->
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CalendarMonth, null, tint = if (isNext) Accent else TextMuted, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(date.take(10), fontSize = 12.sp,
                                    color = if (isNext) Accent else TextMuted,
                                    fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Normal)
                            }
                        }

                        if (isNext) {
                            Spacer(Modifier.height(8.dp))
                            Box(Modifier.size(8.dp).clip(CircleShape).background(Accent).align(Alignment.CenterHorizontally))
                        }
                    }

                    if (index < data.stages.lastIndex) {
                        HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f))
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ══════════════════════════════════════════════════════════
// Shared components
// ══════════════════════════════════════════════════════════

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp), color = CardBg, shadowElevation = 0.dp
    ) {
        Column {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            }
            HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.2f))
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) { content() }
        }
    }
}

@Composable
private fun StatusPill(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp), color = color.copy(0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.2f))
    ) {
        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun Pill(label: String, textColor: Color, bgColor: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = bgColor) {
        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun StatItem(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}
