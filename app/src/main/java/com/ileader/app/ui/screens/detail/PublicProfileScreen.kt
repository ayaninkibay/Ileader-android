package com.ileader.app.ui.screens.detail

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ResultDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.PublicProfileData
import com.ileader.app.ui.viewmodels.PublicProfileViewModel
import kotlin.math.roundToInt

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft
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
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Профиль", onBack)
                LoadingScreen()
            }
        }
        is UiState.Error -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Профиль", onBack)
                ErrorScreen(state.message, onRetry = { viewModel.load(userId) })
            }
        }
        is UiState.Success -> ProfileContent(data = state.data, onBack = onBack)
    }
}

@Composable
private fun ProfileContent(data: PublicProfileData, onBack: () -> Unit) {
    val profile = data.profile
    val user = profile.toDomain()

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val isDark = DarkTheme.isDark
    val primarySportName = data.sports.firstOrNull()?.sports?.name ?: "картинг"
    val sColor = sportColor(primarySportName)
    val bannerUrl = sportImageUrl(primarySportName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // ══════════════════════════════════════
        // HERO with sport background
        // ══════════════════════════════════════
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background image or gradient
            if (bannerUrl != null) {
                AsyncImage(
                    model = bannerUrl, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )
                Box(
                    Modifier.fillMaxWidth().height(200.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(Brush.verticalGradient(listOf(Color.Black.copy(0.3f), Color.Black.copy(0.7f))))
                )
            } else {
                Box(
                    Modifier.fillMaxWidth().height(200.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(Brush.horizontalGradient(listOf(sColor.copy(0.9f), sColor.copy(0.5f))))
                )
            }

            // Back button
            Box(
                Modifier.statusBarsPadding().padding(16.dp).size(40.dp)
                    .clip(CircleShape).background(Color.Black.copy(0.3f))
                    .then(Modifier.padding(0.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            // Avatar + name overlay
            Column(
                Modifier.fillMaxWidth().statusBarsPadding().padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with sport-color gradient border
                val borderColors = listOf(sColor, sColor.copy(0.5f), Accent, sColor)
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(112.dp).background(Brush.sweepGradient(borderColors), CircleShape))
                    Box(Modifier.size(106.dp).clip(CircleShape).background(Bg))
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(CardBg),
                        contentAlignment = Alignment.Center
                    ) {
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
                Text(profile.name ?: "Пользователь", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleBadge(role = user.role)
                    if (!profile.city.isNullOrEmpty()) {
                        Text("·", fontSize = 14.sp, color = TextMuted)
                        Text(profile.city, fontSize = 13.sp, color = TextMuted)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ══════════════════════════════════════
        // SPORT CHIPS
        // ══════════════════════════════════════
        if (data.sports.isNotEmpty()) {
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                data.sports.forEach { sport ->
                    val name = sport.sports?.name ?: ""
                    val emoji = sportEmoji(name)
                    Surface(shape = RoundedCornerShape(50), color = TextMuted.copy(0.1f)) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(emoji, fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextMuted)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
        }

        // ══════════════════════════════════════
        // ANIMATED STATS
        // ══════════════════════════════════════
        if (data.stats.isNotEmpty()) {
            val s = data.stats.first()
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = CardBg, shadowElevation = if (isDark) 0.dp else 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimStat(Icons.Outlined.EmojiEvents, s.tournaments, "Турниры", started)
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    AnimStat(Icons.Outlined.MilitaryTech, s.wins, "Победы", started)
                    Box(Modifier.width(1.dp).height(40.dp).background(Border.copy(0.3f)))
                    AnimStat(Icons.Outlined.Leaderboard, s.rating, "Рейтинг", started)
                }
            }
            
            Spacer(Modifier.height(12.dp))
        }

        // ══════════════════════════════════════
        // SPORTS with ratings
        // ══════════════════════════════════════
        if (data.sports.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = CardBg, shadowElevation = if (isDark) 0.dp else 2.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Виды спорта", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(Modifier.height(10.dp))
                    data.sports.forEach { sport ->
                        val name = sport.sports?.name ?: ""
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                    .background(TextMuted.copy(0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(sportEmoji(name), fontSize = 18.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(name, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Surface(shape = RoundedCornerShape(8.dp), color = Accent.copy(0.12f)) {
                                Text(
                                    "${sport.rating}",
                                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Accent
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
        }

        // ══════════════════════════════════════
        // BIO
        // ══════════════════════════════════════
        if (!profile.bio.isNullOrEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = CardBg, shadowElevation = if (isDark) 0.dp else 2.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("О себе", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(profile.bio, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)
                }
            }
            
            Spacer(Modifier.height(12.dp))
        }

        // ══════════════════════════════════════
        // RESULTS
        // ══════════════════════════════════════
        if (data.results.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = CardBg, shadowElevation = if (isDark) 0.dp else 2.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.EmojiEvents, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Результаты", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Spacer(Modifier.height(12.dp))
                    data.results.take(10).forEachIndexed { i, result ->
                        if (i > 0) HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.2f), modifier = Modifier.padding(vertical = 6.dp))
                        ResultRow(result)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
        }

        // ══════════════════════════════════════
        // TEAM
        // ══════════════════════════════════════
        data.membership?.let { m ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = CardBg, shadowElevation = if (isDark) 0.dp else 2.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Groups, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Команда", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        val sportName = m.teams?.sports?.name ?: ""
                        Box(
                            Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                                .background(TextMuted.copy(0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(sportEmoji(sportName), fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(m.teams?.name ?: "—", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            if (sportName.isNotEmpty()) {
                                Text(sportName, fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = AccentSoft) {
                            Text(
                                when (m.role) { "captain" -> "Капитан"; "member" -> "Участник"; "reserve" -> "Резерв"; else -> m.role ?: "" },
                                Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Accent
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ══════════════════════════════════════════
// Animated Stat
// ══════════════════════════════════════════

@Composable
private fun AnimStat(icon: ImageVector, target: Int, label: String, started: Boolean) {
    val v by animateFloatAsState(
        if (started) target.toFloat() else 0f,
        tween(800, 300, FastOutSlowInEasing), label = "ps_$label"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text("${v.roundToInt()}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}

// ══════════════════════════════════════════
// Result Row
// ══════════════════════════════════════════

@Composable
private fun ResultRow(result: ResultDto) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // Position badge
        val posColor = when (result.position) { 1 -> Color(0xFFCA8A04); 2 -> Color(0xFF6B7280); 3 -> Color(0xFFB45309); else -> TextMuted }
        val posBg = when (result.position) {
            1 -> if (DarkTheme.isDark) Color(0xFFCA8A04).copy(0.15f) else Color(0xFFFEF9C3)
            2 -> if (DarkTheme.isDark) Color(0xFF6B7280).copy(0.15f) else Color(0xFFF1F5F9)
            3 -> if (DarkTheme.isDark) Color(0xFFB45309).copy(0.15f) else Color(0xFFFEF3C7)
            else -> Color.Transparent
        }
        Box(
            Modifier.size(32.dp).then(
                if (result.position <= 3) Modifier.background(posBg, RoundedCornerShape(8.dp))
                else Modifier
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (result.position <= 3) "${result.position}" else "${result.position}.",
                fontSize = if (result.position <= 3) 14.sp else 13.sp,
                fontWeight = if (result.position <= 3) FontWeight.ExtraBold else FontWeight.Medium,
                color = posColor
            )
        }
        Spacer(Modifier.width(10.dp))

        // Tournament info
        Column(Modifier.weight(1f)) {
            val sportName = result.tournaments?.sports?.name ?: ""
            Text(
                result.tournaments?.name ?: "—", fontSize = 14.sp,
                fontWeight = FontWeight.Medium, color = TextPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (sportName.isNotEmpty()) {
                    Text("${sportEmoji(sportName)} $sportName", fontSize = 11.sp, color = TextMuted)
                    Spacer(Modifier.width(6.dp))
                }
                result.tournaments?.startDate?.let {
                    Text(formatDateCompact(it), fontSize = 11.sp, color = TextMuted)
                }
            }
        }

        // Points
        result.points?.let { pts ->
            Surface(shape = RoundedCornerShape(8.dp), color = Accent.copy(alpha = 0.12f)) {
                Text(
                    "$pts", Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Accent
                )
            }
        }
    }
}

private fun formatDateCompact(dateStr: String): String {
    val parts = dateStr.take(10).split("-")
    if (parts.size < 3) return dateStr
    return "${parts[2]}.${parts[1]}.${parts[0]}"
}
