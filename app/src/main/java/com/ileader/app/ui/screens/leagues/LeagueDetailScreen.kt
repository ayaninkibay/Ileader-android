package com.ileader.app.ui.screens.leagues

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.LeagueStageDto
import com.ileader.app.data.remote.dto.LeagueStandingDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.LeagueDetailViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = DarkTheme.CardBorder

@Composable
fun LeagueDetailScreen(
    leagueId: String,
    onBack: () -> Unit
) {
    val vm: LeagueDetailViewModel = viewModel()
    LaunchedEffect(leagueId) { vm.load(leagueId) }

    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        BackHeader("Лига", onBack)
        when (val s = vm.state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(s.message) { vm.load(leagueId) }
            is UiState.Success -> {
                val data = s.data
                val league = data.league

                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(4.dp))

                    // Cover
                    if (!league.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = league.imageUrl,
                            contentDescription = league.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    Text(
                        league.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (data.sportName.isNotEmpty()) {
                            StatusBadge(text = data.sportName, color = TextSecondary)
                        }
                        league.season?.let {
                            StatusBadge(text = "Сезон $it", color = TextSecondary)
                        }
                    }

                    if (!league.description.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            league.description,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 21.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Stats card
                    DarkCard {
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatCol("${league.totalStages}", "Этапов")
                            Divider()
                            StatCol("${data.completedStages}", "Завершено")
                            Divider()
                            StatCol("${data.participantCount}", "Участников")
                        }
                    }

                    // Stages
                    Spacer(Modifier.height(20.dp))
                    SectionHeader(title = "Этапы")
                    Spacer(Modifier.height(10.dp))
                    if (data.stages.isEmpty()) {
                        Text("Этапы пока не добавлены", fontSize = 13.sp, color = TextMuted)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            data.stages.forEach { stage -> StageRow(stage) }
                        }
                    }

                    // Standings
                    Spacer(Modifier.height(20.dp))
                    SectionHeader(title = "Таблица")
                    Spacer(Modifier.height(10.dp))
                    if (data.standings.isEmpty()) {
                        Text("Нет данных", fontSize = 13.sp, color = TextMuted)
                    } else {
                        StandingsTable(data.standings)
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun StatCol(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .width(1.dp)
            .height(32.dp)
            .background(Border.copy(0.3f))
    )
}

@Composable
private fun StageRow(stage: LeagueStageDto) {
    DarkCard {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(Accent.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${stage.stageNumber}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Accent
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stage.title ?: stage.tournaments?.name ?: "Этап ${stage.stageNumber}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                stage.tournaments?.startDate?.let {
                    Text(it, fontSize = 12.sp, color = TextMuted)
                }
            }
            val statusColor = when (stage.status) {
                "completed" -> Color(0xFF22C55E)
                "in_progress" -> Color(0xFF3B82F6)
                else -> TextMuted
            }
            val statusLabel = when (stage.status) {
                "completed" -> "Завершён"
                "in_progress" -> "Идёт"
                "upcoming" -> "Скоро"
                else -> stage.status
            }
            Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                Text(
                    statusLabel,
                    Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun StandingsTable(standings: List<LeagueStandingDto>) {
    DarkCard {
        Column(Modifier.padding(vertical = 8.dp)) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#", fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(28.dp))
                Text("Участник", fontSize = 11.sp, color = TextMuted, modifier = Modifier.weight(1f))
                Text("Этап.", fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(48.dp))
                Text("Очки", fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(48.dp))
            }
            HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.2f))
            standings.forEachIndexed { index, s ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${index + 1}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (index < 3) Accent else TextPrimary,
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        s.profiles?.name ?: "—",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${s.stagesParticipated}",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.width(48.dp)
                    )
                    Text(
                        "${s.totalPoints}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.width(48.dp)
                    )
                }
                if (index < standings.size - 1) {
                    HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f))
                }
            }
        }
    }
}
