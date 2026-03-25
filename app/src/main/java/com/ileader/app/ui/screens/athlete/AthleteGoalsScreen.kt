package com.ileader.app.ui.screens.athlete

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
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
        is UiState.Loading -> LoadingScreen(LoadingVariant.LIST)
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> GoalsContent(user, s.data.goals, s.data.stats, viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var editingGoal by remember { mutableStateOf<AthleteGoal?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFilterSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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

    Box(Modifier.fillMaxSize()) {
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
                    Text(
                        "Привет, ${user.displayName.split(" ").firstOrNull() ?: user.displayName}",
                        fontSize = 14.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.Normal
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Цели и план", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.8).sp
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Добавить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            }

            Spacer(Modifier.height(20.dp))

            // ── STATS 4 в ряд ──
            FadeIn(visible = started, delayMs = 150) {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                GoalStatCard(Modifier.weight(1f), Icons.Default.GpsFixed, activeCount.toString(), "Активные")
                GoalStatCard(Modifier.weight(1f), Icons.Default.CheckCircle, completedCount.toString(), "Выполнены")
                GoalStatCard(Modifier.weight(1f), Icons.Default.TrendingUp, "#${stats.rating}", "Рейтинг")
                GoalStatCard(Modifier.weight(1f), Icons.Default.Bolt, stats.points.toString(), "Очки")
            }
            }

            Spacer(Modifier.height(16.dp))

            // ── FILTER BUTTON ──
            FadeIn(visible = started, delayMs = 300) {
            val activeFilters = (if (selectedStatus != 0) 1 else 0) + (if (selectedType != 0) 1 else 0)
            Surface(
                onClick = { showFilterSheet = true },
                shape = RoundedCornerShape(12.dp),
                color = DarkTheme.CardBg
            ) {
                Row(
                    Modifier
                        .border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Tune, null, Modifier.size(18.dp), DarkTheme.Accent)
                    Text("Фильтры", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkTheme.TextPrimary)
                    if (activeFilters > 0) {
                        Spacer(Modifier.width(2.dp))
                        Surface(shape = RoundedCornerShape(20.dp), color = DarkTheme.Accent) {
                            Text(
                                "$activeFilters",
                                Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(18.dp), DarkTheme.TextMuted)
                }
            }
            }

            Spacer(Modifier.height(16.dp))

            // ── GOALS LIST ──
            FadeIn(visible = started, delayMs = 450) {
            Column {
            if (filteredGoals.isEmpty()) {
                EmptyState("Нет целей", "Создайте свою первую цель!", actionLabel = "Создать цель", onAction = { showAddDialog = true })
            } else {
                filteredGoals.forEach { goal ->
                    GoalCard(goal, onEdit = { editingGoal = goal })
                    Spacer(Modifier.height(10.dp))
                }
            }
            }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = DarkTheme.CardBg,
            dragHandle = {
                Box(Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                    Box(Modifier.width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(DarkTheme.CardBorder))
                }
            }
        ) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
                Text("Фильтры", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary)
                Spacer(Modifier.height(20.dp))

                Text("Статус", fontSize = 13.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                DarkSegmentedControl(statusFilters, selectedStatus, { selectedStatus = it })

                Spacer(Modifier.height(16.dp))

                Text("Тип цели", fontSize = 13.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                DarkSegmentedControl(typeFilters, selectedType, { selectedType = it })

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { selectedStatus = 0; selectedType = 0 },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder(true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(DarkTheme.CardBorder)
                        )
                    ) { Text("Сбросить", color = DarkTheme.TextSecondary) }
                    Button(
                        onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { showFilterSheet = false } },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                    ) { Text("Применить") }
                }
            }
        }
    }

    if (showAddDialog) AddGoalDialog(
        onDismiss = { showAddDialog = false },
        onCreate = { type, title, desc, target ->
            viewModel.createGoal(type, title, desc, target)
            showAddDialog = false
        }
    )

    editingGoal?.let { goal ->
        EditGoalDialog(
            goal = goal,
            onDismiss = { editingGoal = null },
            onSave = { type, title, desc, target, status ->
                viewModel.updateGoal(goal.id, type, title, desc, target, status)
                editingGoal = null
            },
            onDelete = {
                viewModel.deleteGoal(goal.id)
                editingGoal = null
            }
        )
    }
}

@Composable
private fun GoalStatCard(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    val accent = DarkTheme.Accent
    val accentSoft = DarkTheme.AccentSoft
    val cardBg = DarkTheme.CardBg
    val textPrimary = DarkTheme.TextPrimary
    val textSecondary = DarkTheme.TextSecondary

    Surface(modifier.height(80.dp), RoundedCornerShape(16.dp), cardBg) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(accentSoft),
                Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(15.dp), accent)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = textPrimary, letterSpacing = (-0.3).sp)
            Text(label, fontSize = 11.sp, color = textSecondary, maxLines = 1)
        }
    }
}

@Composable
private fun GoalCard(goal: AthleteGoal, onEdit: () -> Unit) {
    val isActive = goal.status == GoalStatus.ACTIVE
    val chipColor = if (isActive) DarkTheme.Accent else DarkTheme.TextMuted
    val statusIcon = when (goal.status) {
        GoalStatus.ACTIVE -> Icons.Default.PlayArrow
        GoalStatus.COMPLETED -> Icons.Default.CheckCircle
        GoalStatus.FAILED -> Icons.Default.Cancel
    }
    val typeIcon = when (goal.type) {
        GoalType.RATING -> Icons.Default.TrendingUp
        GoalType.TOURNAMENT -> Icons.Default.EmojiEvents
        GoalType.POINTS -> Icons.Default.Bolt
    }
    val progress = if (goal.targetValue > 0) (goal.currentValue.toFloat() / goal.targetValue).coerceIn(0f, 1f) else 0f

    val cardBg = DarkTheme.CardBg
    val textPrimary = DarkTheme.TextPrimary
    val textSecondary = DarkTheme.TextSecondary
    val textMuted = DarkTheme.TextMuted
    val accent = DarkTheme.Accent

    Surface(Modifier.fillMaxWidth().clickable { onEdit() }, RoundedCornerShape(20.dp), cardBg) {
        Column(
            Modifier.padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Иконка типа в скруглённом квадрате
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(DarkTheme.AccentSoft),
                        Alignment.Center
                    ) {
                        Icon(typeIcon, null, Modifier.size(22.dp), accent)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(goal.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                        Text(goal.type.displayName, fontSize = 12.sp, color = textSecondary)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(10.dp), color = chipColor.copy(alpha = 0.12f)) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(statusIcon, null, Modifier.size(13.dp), chipColor)
                            Spacer(Modifier.width(4.dp))
                            Text(goal.status.displayName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = chipColor)
                        }
                    }
                    Icon(Icons.Default.Edit, "Редактировать", Modifier.size(18.dp), textMuted)
                }
            }

            if (goal.description.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(goal.description, fontSize = 13.sp, color = textSecondary, lineHeight = 18.sp)
            }

            if (goal.targetValue > 0) {
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Прогресс: ${goal.currentValue}/${goal.targetValue}", fontSize = 12.sp, color = textSecondary)
                    Surface(shape = RoundedCornerShape(8.dp), color = DarkTheme.AccentSoft) {
                        Text(
                            "${(progress * 100).toInt()}%",
                            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accent
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                DarkProgressBar(progress, Modifier.fillMaxWidth().height(6.dp))
            }

            if (goal.deadline != null) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(13.dp), textMuted)
                    Spacer(Modifier.width(5.dp))
                    Text("Дедлайн: ${goal.deadline}", fontSize = 12.sp, color = textMuted)
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
private fun EditGoalDialog(
    goal: AthleteGoal,
    onDismiss: () -> Unit,
    onSave: (GoalType, String, String, Int, GoalStatus) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(goal.title) }
    var description by remember { mutableStateOf(goal.description) }
    var selectedType by remember { mutableStateOf(goal.type) }
    var targetValue by remember { mutableStateOf(goal.targetValue.toString()) }
    var selectedStatus by remember { mutableStateOf(goal.status) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = DarkTheme.CardBg,
            titleContentColor = DarkTheme.TextPrimary,
            textContentColor = DarkTheme.TextSecondary,
            title = { Text("Удалить цель?", fontWeight = FontWeight.Bold) },
            text = { Text("Цель «${goal.title}» будет удалена безвозвратно.") },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена", color = DarkTheme.TextSecondary) }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkTheme.CardBg,
        titleContentColor = DarkTheme.TextPrimary,
        textContentColor = DarkTheme.TextSecondary,
        title = {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Редактировать цель", fontWeight = FontWeight.Bold)
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, "Удалить", tint = DarkTheme.Accent, modifier = Modifier.size(20.dp))
                }
            }
        },
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

                Text("Статус", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkTheme.TextPrimary)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoalStatus.entries.forEach { status ->
                        DarkFilterChip(status.displayName, selectedStatus == status, onClick = { selectedStatus = status })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedType, title, description, targetValue.toIntOrNull() ?: 0, selectedStatus) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
            ) { Text("Сохранить") }
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
