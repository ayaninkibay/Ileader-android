package com.ileader.app.ui.screens.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.sportIcon
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.theme.cardShadow

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

private data class MockMember(val name: String, val role: String)

private val mockMembers = listOf(
    MockMember("Алихан Тлеубаев", "captain"),
    MockMember("Марат Касымов", "member"),
    MockMember("Данияр Серикбаев", "member"),
    MockMember("Ерлан Жумабеков", "reserve")
)

private fun sportImageUrl(sportName: String): String? = when (sportName.lowercase().trim()) {
    "картинг", "karting" -> "https://ileader.kz/img/karting/karting-04-1280x853.jpeg"
    "стрельба", "shooting" -> "https://ileader.kz/img/shooting/shooting-01-1280x853.jpeg"
    "теннис", "tennis" -> "https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=800&q=80"
    "футбол", "football", "soccer" -> "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800&q=80"
    "бокс", "boxing" -> "https://images.unsplash.com/photo-1549719386-74dfcbf7dbed?w=800&q=80"
    "плавание", "swimming" -> "https://images.unsplash.com/photo-1530549387789-4c1017266635?w=800&q=80"
    "атлетика", "лёгкая атлетика", "легкая атлетика", "athletics" -> "https://images.unsplash.com/photo-1532444458054-01a7dd3e9fca?w=800&q=80"
    "гребля", "rowing" -> "https://images.unsplash.com/photo-1541746972996-4e0b0f43e02a?w=800&q=80"
    else -> null
}

private fun roleLabel(role: String): String = when (role) {
    "captain" -> "Капитан"
    "member" -> "Участник"
    "reserve" -> "Резерв"
    else -> role
}

private fun roleColor(role: String): Color = when (role) {
    "captain" -> Color(0xFFE53535)
    "reserve" -> Color(0xFF6B7280)
    else -> Color(0xFF3B82F6)
}

@Composable
fun TeamDetailScreen(
    teamName: String,
    sportName: String,
    city: String,
    onBack: () -> Unit
) {
    val isDark = DarkTheme.isDark
    val scrollState = rememberScrollState()

    // Staggered fade-in animations
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val a0 by animateFloatAsState(if (visible) 1f else 0f, tween(400, 0), label = "a0")
    val a1 by animateFloatAsState(if (visible) 1f else 0f, tween(400, 100), label = "a1")
    val a2 by animateFloatAsState(if (visible) 1f else 0f, tween(400, 200), label = "a2")
    val a3 by animateFloatAsState(if (visible) 1f else 0f, tween(400, 300), label = "a3")
    val a4 by animateFloatAsState(if (visible) 1f else 0f, tween(400, 400), label = "a4")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .verticalScroll(scrollState)
    ) {
        // ── Hero header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .graphicsLayer { alpha = a0 }
        ) {
            val imgUrl = sportImageUrl(sportName)
            if (imgUrl != null) {
                AsyncImage(
                    model = imgUrl,
                    contentDescription = sportName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(Accent.copy(0.8f), Accent.copy(0.3f))
                            )
                        )
                )
            }

            // Gradient overlay
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(0.25f), Color.Black.copy(0.75f))
                        )
                    )
            )

            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.2f))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад",
                    tint = Color.White, modifier = Modifier.size(20.dp)
                )
            }

            // Team name, sport badge, city
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                // Sport badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(0.4f)
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            sportIcon(sportName), null,
                            tint = Color.White, modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            sportName, fontSize = 12.sp,
                            color = Color.White, fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    teamName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                    color = Color.White, letterSpacing = (-0.3).sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn, null,
                        tint = Color.White.copy(0.7f), modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        city, fontSize = 13.sp,
                        color = Color.White.copy(0.8f), fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Stats row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .graphicsLayer { alpha = a1 },
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Участников", "4", Icons.Default.People, Modifier.weight(1f), isDark)
            StatCard("Турниров", "7", Icons.Default.EmojiEvents, Modifier.weight(1f), isDark)
            StatCard("Побед", "3", Icons.Default.MilitaryTech, Modifier.weight(1f), isDark)
        }

        Spacer(Modifier.height(20.dp))

        // ── Состав команды ──
        SectionCard(
            title = "Состав команды",
            icon = Icons.Default.Groups,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .graphicsLayer { alpha = a2 },
            isDark = isDark
        ) {
            mockMembers.forEachIndexed { idx, member ->
                if (idx > 0) {
                    HorizontalDivider(
                        color = Border.copy(0.15f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar placeholder
                    val avatarColor = roleColor(member.role)
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(avatarColor.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            member.name.take(1),
                            fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = avatarColor
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            member.name,
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                            color = TextPrimary, maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            city, fontSize = 12.sp, color = TextMuted
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = roleColor(member.role).copy(0.1f)
                    ) {
                        Text(
                            roleLabel(member.role),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = roleColor(member.role)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── О команде ──
        SectionCard(
            title = "О команде",
            icon = Icons.Default.Info,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .graphicsLayer { alpha = a3 },
            isDark = isDark
        ) {
            Text(
                "Профессиональная команда по дисциплине $sportName из города $city. " +
                        "Команда активно участвует в региональных и национальных турнирах, " +
                        "показывая стабильные результаты на протяжении сезона. " +
                        "Тренировки проходят 4 раза в неделю под руководством опытного тренера.",
                fontSize = 14.sp, color = TextSecondary,
                lineHeight = 21.sp
            )
        }

        Spacer(Modifier.height(100.dp))
    }
}

// ═══════════════════════════════════════════════════
// Helper composables
// ═══════════════════════════════════════════════════

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    Surface(
        modifier = modifier.cardShadow(isDark),
        shape = RoundedCornerShape(14.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke else null
    ) {
        Column(
            Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                value, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                label, fontSize = 11.sp, color = TextMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    isDark: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .cardShadow(isDark),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke else null
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    title, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}
