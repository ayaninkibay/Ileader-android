package com.ileader.app.ui.screens.athlete

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AthleteGoalsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AthleteGoalsScreen(user: User) {
    val viewModel: AthleteGoalsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> GoalsContent(user, s.data.goals, s.data.stats, viewModel)
    }
}

@Composable
private fun GoalsContent(
    user: User,
    allGoals: List<AthleteGoal>,
    stats: AthleteStats,
    viewModel: AthleteGoalsViewModel
) {
    var selectedStatus by remember { mutableIntStateOf(0) }
    var selectedType by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val statusFilters = listOf("Все", "Активные", "Выполнены", "Просрочены")
    val typeFilters = listOf("Все типы", "Рейтинг", "Турниры", "Очки")

    val filteredGoals = allGoals.filter { g ->
        val matchesStatus = selectedStatus == 0 || when (selectedStatus) {
            1 -> g.status == GoalStatus.ACTIVE; 2 -> g.status == GoalStatus.COMPLETED; 3 -> g.status == GoalStatus.FAILED; else -> true
        }
        val matchesType = selectedType == 0 || when (selectedType) {
            1 -> g.type == GoalType.RATING; 2 -> g.type == GoalType.TOURNAMENT; 3 -> g.type == GoalType.POINTS; else -> true
        }
        matchesStatus && matchesType
    }

    val activeCount = allGoals.count { it.status == GoalStatus.ACTIVE }
    val completedCount = allGoals.count { it.status == GoalStatus.COMPLETED }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── HEADER ──
            FadeIn(visible = started, delayMs = 0) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Цели и план", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(4.dp))
                    Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                }
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Добавить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            }

            Spacer(Modifier.height(20.dp))

            // ── STATS 2x2 ──
            FadeIn(visible = started, delayMs = 150) {
            Column {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                StatItem(Modifier.weight(1f), Icons.Default.GpsFixed, activeCount.toString(), "Активные")
                StatItem(Modifier.weight(1f), Icons.Default.CheckCircle, completedCount.toString(), "Выполнены")
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                StatItem(Modifier.weight(1f), Icons.Default.TrendingUp, "#${stats.rating}", "Рейтинг")
                StatItem(Modifier.weight(1f), Icons.Default.Bolt, stats.points.toString(), "Очки")
            }
            }
            }

            Spacer(Modifier.height(16.dp))

            // ── STATUS FILTERS ──
            FadeIn(visible = started, delayMs = 300) {
            Column {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                statusFilters.forEachIndexed { index, label ->
                    DarkFilterChip(label, selectedStatus == index, { selectedStatus = index })
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── TYPE FILTERS ──
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                typeFilters.forEachIndexed { index, label ->
                    DarkFilterChip(label, selectedType == index, onClick = { selectedType = index })
                }
            }
            }
            }

            Spacer(Modifier.height(16.dp))

            // ── GOALS LIST ──
            FadeIn(visible = started, delayMs = 450) {
            Column {
            if (filteredGoals.isEmpty()) {
                EmptyState("Нет целей", "Создайте свою первую цель!")
            } else {
                filteredGoals.forEach { goal ->
                    GoalCard(goal)
                    Spacer(Modifier.height(10.dp))
                }
            }
            }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showAddDialog) AddGoalDialog(
        onDismiss = { showAddDialog = false },
        onCreate = { type, title, desc, target ->
            viewModel.createGoal(type, title, desc, target)
            showAddDialog = false
        }
    )
}

@Composable
private fun GoalCard(goal: AthleteGoal) {
    val isActive = goal.status == GoalStatus.ACTIVE
    val chipColor = if (isActive) DarkTheme.Accent else DarkTheme.TextMuted
    val statusIcon = when (goal.status) { GoalStatus.ACTIVE -> Icons.Default.PlayArrow; GoalStatus.COMPLETED -> Icons.Default.CheckCircle; GoalStatus.FAILED -> Icons.Default.Cancel }
    val typeIcon = when (goal.type) { GoalType.RATING -> Icons.Default.TrendingUp; GoalType.TOURNAMENT -> Icons.Default.EmojiEvents; GoalType.POINTS -> Icons.Default.Bolt }
    val progress = if (goal.targetValue > 0) (goal.currentValue.toFloat() / goal.targetValue).coerceIn(0f, 1f) else 0f

    DarkCard {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SoftIconBox(typeIcon)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(goal.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                        Text(goal.type.displayName, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = chipColor.copy(alpha = 0.10f)) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(statusIcon, null, Modifier.size(14.dp), chipColor)
                        Spacer(Modifier.width(4.dp))
                        Text(goal.status.displayName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = chipColor)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(goal.description, fontSize = 14.sp, color = DarkTheme.TextSecondary, lineHeight = 18.sp)

            if (goal.targetValue > 0) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Прогресс: ${goal.currentValue}/${goal.targetValue}", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                    Text("${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.Accent)
                }
                Spacer(Modifier.height(6.dp))
                DarkProgressBar(progress)
            }

            if (goal.deadline != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(14.dp), DarkTheme.TextMuted)
                    Spacer(Modifier.width(4.dp))
                    Text("Дедлайн: ${goal.deadline}", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onCreate: (GoalType, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(GoalType.RATING) }
    var targetValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkTheme.CardBg,
        titleContentColor = DarkTheme.TextPrimary,
        textContentColor = DarkTheme.TextSecondary,
        title = { Text("Новая цель", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DialogInput("Название цели", title) { title = it }
                DialogInput("Описание", description) { description = it }
                Text("Тип цели", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkTheme.TextPrimary)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoalType.entries.forEach { type ->
                        DarkFilterChip(type.displayName, selectedType == type, onClick = { selectedType = type })
                    }
                }
                DialogInput("Целевое значение", targetValue) { targetValue = it.filter { c -> c.isDigit() } }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(selectedType, title, description, targetValue.toIntOrNull() ?: 0) },
                colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = DarkTheme.TextSecondary) }
        }
    )
}

@Composable
private fun DialogInput(placeholder: String, value: String, onValueChange: (String) -> Unit) {
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(12.dp), DarkTheme.CardBg) {
        Row(
            Modifier.border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) Text(placeholder, fontSize = 14.sp, color = DarkTheme.TextMuted)
                BasicTextField(value, onValueChange,
                    textStyle = TextStyle(fontSize = 14.sp, color = DarkTheme.TextPrimary),
                    singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
