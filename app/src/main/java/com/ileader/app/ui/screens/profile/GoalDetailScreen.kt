package com.ileader.app.ui.screens.profile

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.AthleteGoal
import com.ileader.app.data.models.GoalStatus
import com.ileader.app.data.models.GoalType
import com.ileader.app.data.remote.dto.GoalUpdateDto
import com.ileader.app.data.repository.AthleteRepository
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun GoalDetailScreen(
    goal: AthleteGoal,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val isDark = DarkTheme.isDark
    val scope = rememberCoroutineScope()
    val repo = remember { AthleteRepository() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    val progress = if (goal.targetValue > 0) (goal.currentValue.toFloat() / goal.targetValue).coerceIn(0f, 1f) else 0f
    val statusColor = when (goal.status) {
        GoalStatus.COMPLETED -> Color(0xFF22C55E)
        GoalStatus.FAILED -> Color(0xFFEF4444)
        GoalStatus.ACTIVE -> Accent
    }
    val progressColor = when (goal.status) {
        GoalStatus.COMPLETED -> Color(0xFF22C55E)
        GoalStatus.FAILED -> Color(0xFFEF4444)
        GoalStatus.ACTIVE -> Color(0xFF3B82F6)
    }
    val typeIcon = when (goal.type) {
        GoalType.RATING -> Icons.Default.Leaderboard
        GoalType.TOURNAMENT -> Icons.Default.EmojiEvents
        GoalType.POINTS -> Icons.Default.Stars
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
            }
            Text("Цель", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, null, tint = ILeaderColors.Error)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Hero card
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = CardBg,
            shadowElevation = 0.dp
        ) {
            Column(Modifier.padding(20.dp)) {
                // Type icon + status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(progressColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(typeIcon, null, tint = progressColor, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(goal.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(goal.type.displayName, fontSize = 13.sp, color = TextMuted)
                    }
                    Surface(shape = RoundedCornerShape(50), color = statusColor.copy(alpha = 0.12f)) {
                        Text(
                            goal.status.displayName,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Big progress
                Text("Прогресс", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextMuted)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = progressColor,
                    trackColor = progressColor.copy(alpha = 0.12f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${goal.currentValue}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = progressColor)
                    Text("из ${goal.targetValue}", fontSize = 16.sp, color = TextMuted)
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    "${(progress * 100).toInt()}% выполнено",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Info cards
        if (goal.description.isNotEmpty()) {
            InfoRow(icon = Icons.Default.Description, label = "Описание", value = goal.description)
        }
        if (goal.deadline != null) {
            InfoRow(icon = Icons.Default.CalendarMonth, label = "Дедлайн", value = goal.deadline)
        }
        InfoRow(icon = Icons.Default.AccessTime, label = "Создана", value = goal.createdAt.take(10))

        Spacer(Modifier.height(100.dp))
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить цель", color = TextPrimary) },
            text = { Text("Вы уверены, что хотите удалить \"${goal.title}\"?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleting = true
                        scope.launch {
                            try {
                                repo.deleteGoal(goal.id)
                                onDeleted()
                            } catch (_: Exception) {
                                deleting = false
                            }
                        }
                    },
                    enabled = !deleting
                ) {
                    Text("Удалить", color = ILeaderColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    val isDark = DarkTheme.isDark
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = CardBg,
        shadowElevation = 0.dp
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 12.sp, color = TextMuted)
                Spacer(Modifier.height(2.dp))
                Text(value, fontSize = 14.sp, color = TextPrimary)
            }
        }
    }
}
