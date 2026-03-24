package com.ileader.app.ui.screens.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.TrainerAthleteDetailViewModel

@Composable
fun TrainerAthleteDetailScreen(
    user: User,
    athleteId: String,
    onBack: () -> Unit = {}
) {
    val viewModel: TrainerAthleteDetailViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id, athleteId) { viewModel.load(user.id, athleteId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> NotFoundScreen("Спортсмен не найден", onBack)
        is UiState.Success -> {
            val data = s.data
            val athlete = data.athlete
            val teamName = data.teamName
            val results = data.results
            val ratingHistory = data.ratingHistory
            val goals = data.goals
            var trainerNote by remember { mutableStateOf("Хорошо прогрессирует в последних турнирах. Нужно поработать над стартовой позицией.") }
            var isEditingNote by remember { mutableStateOf(false) }
            var showGoalDialog by remember { mutableStateOf(false) }

            Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    // ── BACK + TITLE ──
                    BackHeader("Профиль спортсмена", onBack)

                    Spacer(Modifier.height(24.dp))

                    // ── ATHLETE HEADER CARD ──
                    DarkCard {
                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(72.dp).clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(DarkTheme.Accent, DarkTheme.AccentDark))),
                                Alignment.Center
                            ) {
                                Text(athlete.name.first().toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(athlete.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary)
                            Text(athlete.email, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            Text(teamName, fontSize = 12.sp, color = DarkTheme.TextMuted)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatusBadge("С ${athlete.joinedDate}", DarkTheme.Accent)
                            }
                            if (athlete.bio.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text(athlete.bio, fontSize = 13.sp, color = DarkTheme.TextSecondary, lineHeight = 20.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── STATS GRID ──
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                        DetailStatCard(Modifier.weight(1f), Icons.Default.EmojiEvents, athlete.tournaments.toString(), "Турниров")
                        DetailStatCard(Modifier.weight(1f), Icons.Default.Star, athlete.wins.toString(), "Побед")
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                        DetailStatCard(Modifier.weight(1f), Icons.Default.WorkspacePremium, athlete.podiums.toString(), "Подиумов")
                        DetailStatCard(Modifier.weight(1f), Icons.Default.TrendingUp, athlete.rating.toString(), "Рейтинг")
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── RATING HISTORY ──
                    SectionHeader("Динамика рейтинга")
                    Spacer(Modifier.height(12.dp))
                    DarkCardPadded {
                        DarkBarChart(data = ratingHistory)
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── RECENT RESULTS ──
                    SectionHeader("Последние результаты")
                    Spacer(Modifier.height(12.dp))
                    if (results.isEmpty()) {
                        EmptyState("Нет результатов")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            results.take(5).forEach { result ->
                                val isTop = result.position <= 3
                                DarkCard {
                                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            Modifier.size(38.dp).clip(CircleShape)
                                                .background(if (isTop) DarkTheme.AccentSoft else DarkTheme.CardBorder.copy(alpha = 0.5f)),
                                            Alignment.Center
                                        ) {
                                            Text("#${result.position}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isTop) DarkTheme.Accent else DarkTheme.TextMuted)
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(result.tournamentName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                                            Text("${result.date} · ${result.sportName}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                                        }
                                        StatusBadge("+${result.points}", DarkTheme.Accent)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── GOALS ──
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        SectionHeader("Цели спортсмена")
                        Button(
                            onClick = { showGoalDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Добавить", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (goals.isEmpty()) {
                        EmptyState("Нет целей")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            goals.forEach { goal ->
                                val statusColor = when (goal.status) {
                                    "completed" -> ILeaderColors.Success
                                    "failed" -> DarkTheme.Accent
                                    "paused" -> ILeaderColors.Warning
                                    else -> ILeaderColors.Info
                                }
                                val statusLabel = when (goal.status) {
                                    "active" -> "Активная"
                                    "completed" -> "Выполнена"
                                    "failed" -> "Не выполнена"
                                    "paused" -> "Пауза"
                                    else -> goal.status
                                }
                                DarkCard {
                                    Column(Modifier.padding(14.dp)) {
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                            Text(goal.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, modifier = Modifier.weight(1f))
                                            StatusBadge(statusLabel, statusColor)
                                        }
                                        if (!goal.description.isNullOrBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(goal.description, fontSize = 12.sp, color = DarkTheme.TextSecondary)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        DarkProgressBar(goal.progress / 100f, Modifier.fillMaxWidth())
                                        Spacer(Modifier.height(4.dp))
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                            Text("${goal.progress}%", fontSize = 11.sp, color = DarkTheme.TextMuted)
                                            if (goal.deadline != null) {
                                                Text("До ${goal.deadline}", fontSize = 11.sp, color = DarkTheme.TextMuted)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── TRAINER NOTES ──
                    SectionHeader("Заметки тренера")
                    Spacer(Modifier.height(12.dp))
                    DarkCard {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Заметка", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                                Button(
                                    onClick = { isEditingNote = !isEditingNote },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(if (isEditingNote) Icons.Default.Check else Icons.Default.Edit, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isEditingNote) "Сохранить" else "Редактировать", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            if (isEditingNote) {
                                Surface(Modifier.fillMaxWidth(), RoundedCornerShape(12.dp), DarkTheme.CardBg) {
                                    Box(
                                        Modifier.border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp)).padding(14.dp).heightIn(min = 80.dp)
                                    ) {
                                        BasicTextField(
                                            trainerNote, { trainerNote = it },
                                            textStyle = TextStyle(fontSize = 14.sp, color = DarkTheme.TextPrimary, lineHeight = 20.sp),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            } else {
                                Text(trainerNote, fontSize = 14.sp, color = DarkTheme.TextSecondary, lineHeight = 20.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }

            // ── CREATE GOAL DIALOG ──
            if (showGoalDialog) {
                var goalTitle by remember { mutableStateOf("") }
                var goalDescription by remember { mutableStateOf("") }
                var goalType by remember { mutableStateOf("rating") }
                var goalTarget by remember { mutableStateOf("") }
                var goalDeadline by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showGoalDialog = false },
                    containerColor = DarkTheme.CardBg,
                    titleContentColor = DarkTheme.TextPrimary,
                    textContentColor = DarkTheme.TextSecondary,
                    title = { Text("Создать цель", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            DarkFormField("Название", goalTitle, { goalTitle = it }, placeholder = "Достичь рейтинга 1500")
                            Spacer(Modifier.height(12.dp))
                            DarkFormField("Описание", goalDescription, { goalDescription = it }, placeholder = "Описание цели (необязательно)")
                            Spacer(Modifier.height(12.dp))

                            Text("Тип цели", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("rating" to "Рейтинг", "tournament" to "Турниры", "points" to "Очки").forEach { (type, label) ->
                                    DarkFilterChip(label, goalType == type, onClick = { goalType = type })
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            DarkFormField(
                                when (goalType) {
                                    "rating" -> "Целевой рейтинг"
                                    "tournament" -> "Кол-во побед"
                                    else -> "Целевые очки"
                                },
                                goalTarget, { goalTarget = it }, placeholder = "1500"
                            )
                            Spacer(Modifier.height(12.dp))
                            DarkFormField("Дедлайн (ГГГГ-ММ-ДД)", goalDeadline, { goalDeadline = it }, placeholder = "2026-12-31")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val target = goalTarget.toIntOrNull()
                                viewModel.createGoal(
                                    athleteId = athleteId,
                                    trainerId = user.id,
                                    type = goalType,
                                    title = goalTitle,
                                    description = goalDescription.ifBlank { null },
                                    targetRating = if (goalType == "rating") target else null,
                                    targetWins = if (goalType == "tournament") target else null,
                                    targetPodiums = null,
                                    deadline = goalDeadline.ifBlank { null },
                                    sportId = null
                                )
                                showGoalDialog = false
                            },
                            enabled = goalTitle.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                        ) { Text("Создать") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showGoalDialog = false }) { Text("Отмена", color = DarkTheme.TextSecondary) }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailStatCard(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = DarkTheme.CardBg) {
        Column(
            Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp)).padding(14.dp)
        ) {
            SoftIconBox(icon)
            Spacer(Modifier.height(10.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
            Text(label, fontSize = 11.sp, color = DarkTheme.TextMuted)
        }
    }
}

@Composable
internal fun DarkBarChart(data: List<Pair<String, Int>>) {
    val maxValue = data.maxOfOrNull { it.second } ?: 1
    val minValue = data.minOfOrNull { it.second } ?: 0
    val range = (maxValue - minValue).coerceAtLeast(1)

    Row(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, value) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(value.toString(), fontSize = 10.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                val barHeight = ((value - minValue).toFloat() / range * 80 + 20).dp
                Box(
                    Modifier
                        .width(28.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(Brush.verticalGradient(listOf(DarkTheme.Accent, DarkTheme.AccentDark)))
                )
                Spacer(Modifier.height(4.dp))
                Text(label, fontSize = 11.sp, color = DarkTheme.TextMuted)
            }
        }
    }
}
