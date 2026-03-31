package com.ileader.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.ileader.app.data.remote.dto.ResultDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.PublicProfileData
import com.ileader.app.ui.viewmodels.PublicProfileViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft
private val Border: Color @Composable get() = com.ileader.app.ui.theme.LocalAppColors.current.border

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Gradient header with avatar ──
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFE53535), Color(0xFFFF6B6B))
                        ),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
            )

            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier.statusBarsPadding().padding(4.dp)
            ) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, null,
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp).size(20.dp)
                    )
                }
            }

            // Avatar + name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(CardBg)
                        .border(3.dp, CardBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profile.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = profile.avatarUrl, contentDescription = null,
                            modifier = Modifier.size(88.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(88.dp).clip(CircleShape).background(Accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                (profile.name ?: "?").take(1).uppercase(),
                                fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    profile.name ?: "Пользователь",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleBadge(role = user.role)
                    if (!profile.city.isNullOrEmpty()) {
                        Text(profile.city, fontSize = 13.sp, color = TextMuted)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Stats ──
        if (data.stats.isNotEmpty()) {
            val s = data.stats.first()
            FadeIn(visible = started, delayMs = 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg, shadowElevation = if (DarkTheme.isDark) 0.dp else 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatColumn("Турниры", "${s.tournaments}")
                        StatDivider()
                        StatColumn("Победы", "${s.wins}")
                        StatDivider()
                        StatColumn("Рейтинг", "${s.rating}")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // ── Sports ──
        if (data.sports.isNotEmpty()) {
            FadeIn(visible = started, delayMs = 100) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg, shadowElevation = if (DarkTheme.isDark) 0.dp else 2.dp
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SectionLabel("Виды спорта")
                        Spacer(Modifier.height(8.dp))
                        data.sports.forEach { sport ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${sportEmoji(sport.sports?.name ?: "")} ${sport.sports?.name ?: ""}",
                                    fontSize = 14.sp, color = TextSecondary, modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Accent.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        "${sport.rating}",
                                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Accent
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // ── Bio ──
        if (!profile.bio.isNullOrEmpty()) {
            FadeIn(visible = started, delayMs = 200) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg, shadowElevation = if (DarkTheme.isDark) 0.dp else 2.dp
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SectionLabel("О себе")
                        Spacer(Modifier.height(8.dp))
                        Text(profile.bio, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // ── Results ──
        if (data.results.isNotEmpty()) {
            FadeIn(visible = started, delayMs = 300) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg, shadowElevation = if (DarkTheme.isDark) 0.dp else 2.dp
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, null, tint = Accent, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            SectionLabel("Результаты")
                        }
                        Spacer(Modifier.height(10.dp))
                        data.results.take(10).forEachIndexed { i, result ->
                            if (i > 0) HorizontalDivider(thickness = 0.5.dp, color = Border, modifier = Modifier.padding(vertical = 6.dp))
                            ResultRow(result)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // ── Team ──
        data.membership?.let { m ->
            FadeIn(visible = started, delayMs = 400) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg, shadowElevation = if (DarkTheme.isDark) 0.dp else 2.dp
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Groups, null, tint = Accent, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            SectionLabel("Команда")
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(m.teams?.name ?: "—", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                m.teams?.sports?.name?.let {
                                    Text("${sportEmoji(it)} $it", fontSize = 12.sp, color = TextMuted)
                                }
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = AccentSoft) {
                                Text(
                                    when (m.role) { "captain" -> "Капитан"; "member" -> "Участник"; "reserve" -> "Резерв"; else -> m.role ?: "" },
                                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Accent
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 12.sp, color = TextMuted)
    }
}

@Composable
private fun StatDivider() {
    Box(Modifier.width(1.dp).height(36.dp).background(Border))
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
}

@Composable
private fun ResultRow(result: ResultDto) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        val posEmoji = when (result.position) {
            1 -> "\uD83E\uDD47"; 2 -> "\uD83E\uDD48"; 3 -> "\uD83E\uDD49"
            else -> "${result.position}."
        }
        Text(posEmoji, fontSize = 16.sp, modifier = Modifier.width(32.dp))
        Column(Modifier.weight(1f)) {
            Text(
                result.tournaments?.name ?: "—", fontSize = 14.sp,
                fontWeight = FontWeight.Medium, color = TextPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            result.tournaments?.startDate?.let {
                Text(formatDateCompact(it), fontSize = 11.sp, color = TextMuted)
            }
        }
        result.points?.let { pts ->
            Surface(shape = RoundedCornerShape(6.dp), color = Accent.copy(alpha = 0.1f)) {
                Text(
                    "$pts очк.", Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Accent
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
