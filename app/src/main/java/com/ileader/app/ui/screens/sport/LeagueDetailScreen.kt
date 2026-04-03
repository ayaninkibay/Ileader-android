package com.ileader.app.ui.screens.sport

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.sportIcon
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
// Mock data (matching website structure)
// ══════════════════════════════════════════════════════════

private data class StandingRow(
    val rank: Int, val name: String, val points: Int,
    val stages: Int, val bestFinish: Int?
)

private data class StageRow(
    val number: Int, val status: String, val title: String?,
    val tournamentName: String?, val date: String?
)

private data class ScoringEntry(val place: Int, val points: Int)

private val mockStandings = listOf(
    StandingRow(1, "Алихан Тлеубаев", 58, 3, 1),
    StandingRow(2, "Марат Касымов", 45, 3, 1),
    StandingRow(3, "Данияр Серикбаев", 40, 3, 2),
    StandingRow(4, "Ерлан Жумабеков", 32, 2, 3),
    StandingRow(5, "Тимур Нурсеитов", 28, 3, 4),
    StandingRow(6, "Арман Бекенов", 25, 2, 5),
    StandingRow(7, "Руслан Ахметов", 20, 2, 6),
    StandingRow(8, "Бауыржан Омаров", 18, 1, 8),
)

private val mockStages = listOf(
    StageRow(1, "completed", "Гран-при Алматы", "Кубок Алматы #1", "15 мар 2026"),
    StageRow(2, "completed", "Гран-при Астана", "Кубок Астана #1", "25 мар 2026"),
    StageRow(3, "completed", "Гран-при Шымкент", "Кубок Шымкент #1", "5 апр 2026"),
    StageRow(4, "upcoming", "Гран-при Караганда", "Кубок Караганда #1", "15 апр 2026"),
    StageRow(5, "upcoming", "Финал — Алматы", null, "30 апр 2026"),
)

private val mockScoring = listOf(
    ScoringEntry(1, 25), ScoringEntry(2, 18), ScoringEntry(3, 15),
    ScoringEntry(4, 12), ScoringEntry(5, 10), ScoringEntry(6, 8),
    ScoringEntry(7, 6), ScoringEntry(8, 4)
)

// ══════════════════════════════════════════════════════════
// Screen
// ══════════════════════════════════════════════════════════

@Composable
fun LeagueDetailScreen(
    leagueName: String,
    sportName: String,
    imageUrl: String?,
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit = {}
) {
    val isDark = DarkTheme.isDark
    // Static values (animations removed)
    val heroAlpha = 1f
    val sec1Alpha = 1f
    val sec1Offset = 0f
    val sec2Alpha = 1f
    val sec2Offset = 0f
    val sec3Alpha = 1f
    val sec3Offset = 0f
    val pulseAlpha = 0.75f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // ════════════════════════════════════
        // HERO (like website hero section)
        // ════════════════════════════════════
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = heroAlpha }
        ) {
            // Background image
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    contentScale = ContentScale.Crop
                )
                Box(
                    Modifier.fillMaxWidth().height(280.dp)
                        .background(Brush.verticalGradient(
                            listOf(Color.Black.copy(0.2f), Color.Black.copy(0.75f))
                        ))
                )
            } else {
                Box(
                    Modifier.fillMaxWidth().height(200.dp)
                        .background(Brush.verticalGradient(
                            listOf(Accent.copy(0.15f), Bg)
                        ))
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 20.dp)
            ) {
                // Back
                Box(
                    Modifier.size(40.dp).clip(CircleShape)
                        .background(if (imageUrl != null) Color.Black.copy(0.3f) else CardBg)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад",
                        tint = if (imageUrl != null) Color.White else TextPrimary,
                        modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.height(if (imageUrl != null) 80.dp else 20.dp))

                // Badges row (like website)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusPill("Идёт", ILeaderColors.Success)
                    Pill(sportName, if (imageUrl != null) Color.White.copy(0.9f) else TextSecondary,
                        if (imageUrl != null) Color.White.copy(0.15f) else TextMuted.copy(0.15f))
                    Pill("Весна 2026", if (imageUrl != null) Color.White.copy(0.7f) else TextMuted,
                        if (imageUrl != null) Color.White.copy(0.1f) else TextMuted.copy(0.15f))
                }

                Spacer(Modifier.height(10.dp))

                // Name
                val textOnImage = imageUrl != null
                Text(
                    leagueName, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (textOnImage) Color.White else TextPrimary,
                    lineHeight = 32.sp, letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Организатор: Турниры Про", fontSize = 13.sp,
                    color = if (textOnImage) Color.White.copy(0.7f) else TextMuted
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Серия турниров по картингу по всему Казахстану. 5 этапов, лучшие набирают очки.",
                    fontSize = 14.sp,
                    color = if (textOnImage) Color.White.copy(0.8f) else TextSecondary,
                    lineHeight = 20.sp
                )

                // Stats row (like website)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    StatLabel(Icons.Outlined.People, "24", "участников", textOnImage)
                    StatLabel(Icons.Outlined.Layers, "3 / 5", "этапов", textOnImage)
                    StatLabel(Icons.Outlined.EmojiEvents, "Лучшие 3", "из 5", textOnImage)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ════════════════════════════════════
        // STANDINGS TABLE (like website)
        // ════════════════════════════════════
        SectionCard(
            title = "Таблица рейтинга",
            icon = Icons.Outlined.WorkspacePremium,
            iconTint = TextMuted,
            modifier = Modifier.graphicsLayer { alpha = sec1Alpha; translationY = sec1Offset }
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text("#", Modifier.width(32.dp), fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Text("Спортсмен", Modifier.weight(1f), fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Text("Очки", Modifier.width(50.dp), fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Этапов", Modifier.width(50.dp), fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Лучшее", Modifier.width(55.dp), fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.3f))

            mockStandings.forEachIndexed { index, s ->
                val alpha = 1f

                val rankBg = when (s.rank) {
                    1 -> if (isDark) Color(0xFFCA8A04).copy(0.2f) else Color(0xFFFEF9C3)
                    2 -> if (isDark) Color(0xFF6B7280).copy(0.2f) else Color(0xFFF1F5F9)
                    3 -> if (isDark) Color(0xFFB45309).copy(0.2f) else Color(0xFFFEF3C7)
                    else -> Color.Transparent
                }
                val rankColor = when (s.rank) {
                    1 -> Color(0xFFCA8A04); 2 -> Color(0xFF94A3B8); 3 -> Color(0xFFB45309); else -> TextMuted
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer { this.alpha = alpha }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank
                    if (s.rank <= 3) {
                        Box(
                            Modifier.size(28.dp).clip(CircleShape).background(rankBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${s.rank}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = rankColor)
                        }
                        Spacer(Modifier.width(4.dp))
                    } else {
                        Text("${s.rank}", Modifier.width(32.dp), fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    }
                    // Name
                    Text(
                        s.name, Modifier.weight(1f), fontSize = 14.sp,
                        fontWeight = if (s.rank <= 3) FontWeight.Bold else FontWeight.Medium,
                        color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    // Points
                    Text("${s.points}", Modifier.width(50.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.Center)
                    // Stages
                    Text("${s.stages}", Modifier.width(50.dp), fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
                    // Best
                    Row(Modifier.width(55.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        if (s.bestFinish != null && s.bestFinish <= 3) {
                            Icon(Icons.Outlined.MilitaryTech, null, tint = Color(0xFFEAB308), modifier = Modifier.size(14.dp))
                        }
                        Text(
                            s.bestFinish?.toString() ?: "—", fontSize = 13.sp, color = TextSecondary
                        )
                    }
                }
                if (index < mockStandings.lastIndex) {
                    HorizontalDivider(
                        Modifier.padding(horizontal = 12.dp),
                        thickness = 0.5.dp,
                        color = Border.copy(0.15f)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ════════════════════════════════════
        // SCORING TABLE (like website)
        // ════════════════════════════════════
        SectionCard(
            title = "Таблица очков",
            icon = Icons.Outlined.Tag,
            iconTint = TextMuted,
            modifier = Modifier.graphicsLayer { alpha = sec2Alpha; translationY = sec2Offset }
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mockScoring.forEach { entry ->
                    val isTop3 = entry.place <= 3
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isTop3) Color(0xFFFEF9C3) else TextMuted.copy(0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isTop3) Color(0xFFFDE68A) else Border.copy(0.2f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "${entry.place}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTop3) Color(0xFFCA8A04) else TextMuted
                            )
                            Text(
                                "${entry.points}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ════════════════════════════════════
        // STAGES (like website sidebar)
        // ════════════════════════════════════
        SectionCard(
            title = "Этапы",
            icon = Icons.Outlined.CalendarMonth,
            iconTint = TextMuted,
            modifier = Modifier.graphicsLayer { alpha = sec3Alpha; translationY = sec3Offset }
        ) {
            mockStages.forEachIndexed { index, stage ->
                val isCompleted = stage.status == "completed"
                val isNext = !isCompleted && (index == 0 || mockStages[index - 1].status == "completed")

                Column(
                    Modifier
                        .fillMaxWidth()
                        .then(
                            if (isNext) Modifier.background(Accent.copy(0.15f), RoundedCornerShape(12.dp))
                            else Modifier
                        )
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Этап ${stage.number}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNext) Accent else TextPrimary
                        )
                        val (statusLabel, statusColor) = when (stage.status) {
                            "completed" -> "Завершён" to ILeaderColors.Success
                            "in_progress" -> "Идёт" to ILeaderColors.Info
                            else -> if (isNext) "Следующий" to Accent else "Будущий" to TextMuted
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(0.2f))
                        ) {
                            Text(
                                statusLabel,
                                Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor
                            )
                        }
                    }

                    stage.title?.let {
                        Spacer(Modifier.height(2.dp))
                        Text(it, fontSize = 13.sp, color = TextSecondary)
                    }
                    stage.tournamentName?.let {
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.EmojiEvents, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(it, fontSize = 12.sp, color = TextMuted)
                        }
                    }
                    stage.date?.let {
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CalendarMonth, null, tint = if (isNext) Accent else TextMuted, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(it, fontSize = 12.sp, color = if (isNext) Accent else TextMuted,
                                fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }

                    if (isNext) {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .graphicsLayer { alpha = pulseAlpha }
                                .background(Accent)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }

                if (index < mockStages.lastIndex) {
                    HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f))
                }
            }
        }

        Spacer(Modifier.height(100.dp))
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
    val isDark = DarkTheme.isDark
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        color = CardBg,
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Column {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            }
            HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.2f))
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.2f))
    ) {
        Text(
            label, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color
        )
    }
}

@Composable
private fun Pill(label: String, textColor: Color, bgColor: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = bgColor) {
        Text(
            label, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor
        )
    }
}

@Composable
private fun StatLabel(icon: ImageVector, value: String, label: String, onImage: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (onImage) Color.White.copy(0.7f) else TextMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (onImage) Color.White else TextPrimary)
        Spacer(Modifier.width(3.dp))
        Text(label, fontSize = 13.sp, color = if (onImage) Color.White.copy(0.7f) else TextMuted)
    }
}
