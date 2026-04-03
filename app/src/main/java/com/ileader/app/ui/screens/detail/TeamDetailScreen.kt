package com.ileader.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ileader.app.data.remote.dto.TeamDto
import com.ileader.app.data.remote.dto.TeamMemberDto
import com.ileader.app.data.repository.ViewerRepository
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.theme.cardShadow
import com.ileader.app.ui.viewmodels.SportViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

private fun roleLabel(role: String?): String = when (role) {
    "captain" -> "Капитан"; "member" -> "Участник"; "reserve" -> "Резерв"; else -> role ?: ""
}
private fun roleColor(role: String?): Color = when (role) {
    "captain" -> Color(0xFFE53535); "reserve" -> Color(0xFF6B7280); else -> Color(0xFF3B82F6)
}

@Composable
fun TeamDetailScreen(
    teamId: String,
    onBack: () -> Unit
) {
    val isDark = DarkTheme.isDark
    var team by remember { mutableStateOf<TeamDto?>(null) }
    var members by remember { mutableStateOf<List<TeamMemberDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(teamId) {
        try {
            val repo = ViewerRepository()
            team = repo.getTeamDetail(teamId)
            members = repo.getTeamMembers(teamId)
        } catch (e: Exception) {
            error = e.message ?: "Ошибка загрузки"
        }
        loading = false
    }

    Column(
        Modifier.fillMaxSize().background(Bg).statusBarsPadding()
    ) {
        if (loading) {
            BackHeader("Команда", onBack)
            LoadingScreen()
            return@Column
        }
        if (error != null || team == null) {
            BackHeader("Команда", onBack)
            ErrorScreen(error ?: "Команда не найдена", onRetry = {})
            return@Column
        }

        val t = team!!
        val sportName = t.sports?.name ?: ""
        val city = t.city ?: ""
        val ownerName = t.profiles?.name ?: ""
        val imgUrl = SportViewModel.getFallbackImage(
            com.ileader.app.data.remote.dto.SportDto(id = t.sportId ?: "", name = sportName)
        )

        Column(Modifier.verticalScroll(rememberScrollState())) {
            // ── Hero ──
            Box(Modifier.fillMaxWidth().height(200.dp)) {
                if (imgUrl != null) {
                    AsyncImage(imgUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Accent.copy(0.8f), Accent.copy(0.3f)))))
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.25f), Color.Black.copy(0.75f)))))

                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart).size(36.dp)
                        .clip(CircleShape).background(Color.Black.copy(0.3f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(0.4f)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(sportIcon(sportName), null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(sportName, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(t.name, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.3).sp, maxLines = 2)
                    if (city.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(city, fontSize = 13.sp, color = Color.White.copy(0.8f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Stats ──
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Участников", "${members.size}", Icons.Outlined.People, Modifier.weight(1f), isDark)
                StatCard("Тренер", ownerName.split(" ").firstOrNull() ?: "—", Icons.Outlined.Person, Modifier.weight(1f), isDark)
            }

            Spacer(Modifier.height(20.dp))

            // ── Состав ──
            if (members.isNotEmpty()) {
                SectionCard("Состав команды", Icons.Outlined.Groups, Modifier.padding(horizontal = 16.dp), isDark) {
                    members.forEachIndexed { idx, member ->
                        if (idx > 0) HorizontalDivider(color = Border.copy(0.15f), modifier = Modifier.padding(vertical = 8.dp))
                        val name = member.profiles?.name ?: "—"
                        val avatarUrl = member.profiles?.avatarUrl
                        val mCity = member.profiles?.city
                        val rColor = roleColor(member.role)

                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (avatarUrl != null) {
                                AsyncImage(avatarUrl, null, Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            } else {
                                Box(Modifier.size(40.dp).clip(CircleShape).background(rColor.copy(0.15f)), contentAlignment = Alignment.Center) {
                                    Text(name.take(1), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = rColor)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                mCity?.let { Text(it, fontSize = 12.sp, color = TextMuted) }
                            }
                            Surface(shape = RoundedCornerShape(50), color = rColor.copy(0.1f)) {
                                Text(roleLabel(member.role), Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = rColor)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── О команде ──
            if (!t.description.isNullOrEmpty()) {
                SectionCard("О команде", Icons.Outlined.Info, Modifier.padding(horizontal = 16.dp), isDark) {
                    Text(t.description, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)
                }
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier, isDark: Boolean) {
    Surface(modifier = modifier.cardShadow(isDark), shape = RoundedCornerShape(14.dp), color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke else null) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = TextMuted, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
private fun SectionCard(title: String, icon: ImageVector, modifier: Modifier, isDark: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier.fillMaxWidth().cardShadow(isDark), shape = RoundedCornerShape(16.dp), color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke else null) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}
