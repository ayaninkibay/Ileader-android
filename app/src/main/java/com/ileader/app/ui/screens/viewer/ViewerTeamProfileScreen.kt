package com.ileader.app.ui.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ViewerPublicProfileViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun ViewerTeamProfileScreen(
    teamId: String,
    user: User,
    onBack: () -> Unit = {}
) {
    val viewModel: ViewerPublicProfileViewModel = viewModel()
    val state by viewModel.teamState.collectAsState()
    LaunchedEffect(teamId) { viewModel.loadTeam(teamId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.loadTeam(teamId) }
        is UiState.Success -> {
            val data = s.data
            val team = data.team
            val members = data.members
            val sportName = team.sports?.name ?: ""
            val ownerName = team.profiles?.name ?: ""

            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            ) {
                Box(
                    Modifier.fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Accent.copy(alpha = 0.15f), Bg)))
                        .statusBarsPadding().padding(16.dp)
                ) {
                    Column {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = TextPrimary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InitialsAvatar(name = team.name, gradient = listOf(Accent, AccentDark), isRound = false, size = 72)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(team.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (sportName.isNotBlank()) {
                                        Surface(shape = RoundedCornerShape(8.dp), color = CardBorder.copy(alpha = 0.5f)) {
                                            Text(sportName, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(team.city ?: "", fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Team stats
                SectionCard(title = "Статистика", modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        InlineStat("${members.size}", "Членов")
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Owner
                if (ownerName.isNotBlank()) {
                    SectionCard(title = "Владелец", modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AccentIconBox(icon = Icons.Default.Person, size = 44.dp, iconSize = 22.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(ownerName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Members
                SectionCard(title = "Участники (${members.size})", modifier = Modifier.padding(horizontal = 20.dp)) {
                    if (members.isEmpty()) {
                        Text("Нет участников", fontSize = 13.sp, color = TextSecondary)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            members.forEach { member ->
                                val memberName = member.profiles?.name ?: "—"
                                val memberCity = member.profiles?.city ?: ""
                                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = CardBorder.copy(alpha = 0.3f)) {
                                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        InitialsAvatar(name = memberName, gradient = listOf(Accent, AccentDark), size = 36)
                                        Spacer(Modifier.width(10.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(memberName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                            if (memberCity.isNotBlank()) Text(memberCity, fontSize = 12.sp, color = TextSecondary)
                                        }
                                        member.role?.let { role ->
                                            StatusBadge(role)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
