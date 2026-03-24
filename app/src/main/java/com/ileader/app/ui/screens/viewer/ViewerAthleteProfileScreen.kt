package com.ileader.app.ui.screens.viewer

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
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
fun ViewerAthleteProfileScreen(
    athleteId: String,
    user: User,
    onBack: () -> Unit = {}
) {
    val viewModel: ViewerPublicProfileViewModel = viewModel()
    val state by viewModel.athleteState.collectAsState()
    LaunchedEffect(athleteId) { viewModel.loadAthlete(athleteId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.loadAthlete(athleteId) }
        is UiState.Success -> {
            val data = s.data
            val profile = data.profile
            val sportName = data.sports.firstOrNull()?.sports?.name ?: ""
            val stats = data.stats.firstOrNull()
            val subtypeLabel = when (profile.athleteSubtype) {
                "pilot" -> "Пилот"; "shooter" -> "Стрелок"; "tennis" -> "Теннисист"
                "football" -> "Футболист"; "boxer" -> "Боксёр"; "general" -> "Спортсмен"
                else -> null
            }

            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            ) {
                // Hero
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
                            InitialsAvatar(name = profile.name ?: "?", gradient = listOf(Accent, AccentDark), size = 72)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(profile.name ?: "", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (sportName.isNotBlank()) {
                                        Surface(shape = RoundedCornerShape(8.dp), color = CardBorder.copy(alpha = 0.5f)) {
                                            Text(sportName, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        }
                                    }
                                    if (subtypeLabel != null) {
                                        Surface(shape = RoundedCornerShape(8.dp), color = AccentSoft) {
                                            Text(subtypeLabel, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(profile.city ?: "", fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Bio
                if (!profile.bio.isNullOrBlank()) {
                    SectionCard(title = "О спортсмене", modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(profile.bio, fontSize = 14.sp, color = TextSecondary, lineHeight = 20.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Stats
                if (stats != null) {
                    SectionCard(title = "Статистика", modifier = Modifier.padding(horizontal = 20.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ProfileStatRow(Icons.Default.EmojiEvents, "Побед", "${stats.wins}")
                            ProfileStatRow(Icons.Default.Flag, "Турниров", "${stats.tournaments}")
                            ProfileStatRow(Icons.Default.WorkspacePremium, "Подиумов", "${stats.podiums}")
                            ProfileStatRow(Icons.Default.BarChart, "Рейтинг", "${stats.rating}")
                        }
                    }

                    // Win rate
                    if (stats.tournaments > 0) {
                        Spacer(Modifier.height(12.dp))
                        SectionCard(title = "Процент побед", modifier = Modifier.padding(horizontal = 20.dp)) {
                            val winRate = (stats.wins.toFloat() / stats.tournaments * 100).toInt()
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                DarkProgressBar(progress = winRate / 100f, modifier = Modifier.weight(1f))
                                Spacer(Modifier.width(12.dp))
                                Text("$winRate%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Accent)
                            }
                        }
                    }
                }

                // Team membership
                if (data.teamMembership != null) {
                    val teamName = data.teamMembership.teams?.name ?: ""
                    if (teamName.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        SectionCard(title = "Команда", modifier = Modifier.padding(horizontal = 20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AccentIconBox(Icons.Default.Groups)
                                Spacer(Modifier.width(12.dp))
                                Text(teamName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ProfileStatRow(icon: ImageVector, label: String, value: String) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = CardBorder.copy(alpha = 0.3f)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(AccentSoft), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(label, fontSize = 14.sp, color = TextSecondary, modifier = Modifier.weight(1f))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}
