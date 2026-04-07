package com.ileader.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.GoalType
import com.ileader.app.data.remote.dto.GoalInsertDto
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
private val Border: Color @Composable get() = LocalAppColors.current.border

@Composable
fun GoalCreateScreen(
    userId: String,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    val repo = remember { AthleteRepository() }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(GoalType.RATING) }
    var targetValue by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
            }
            Text("Новая цель", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Spacer(Modifier.height(8.dp))

        // Type selector
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("Тип цели", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GoalType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    Surface(
                        modifier = Modifier.weight(1f).clickable { selectedType = type },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Accent.copy(alpha = 0.12f) else CardBg,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Accent) else null,
                        shadowElevation = 0.dp
                    ) {
                        Column(
                            Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                when (type) {
                                    GoalType.RATING -> Icons.Default.Leaderboard
                                    GoalType.TOURNAMENT -> Icons.Default.EmojiEvents
                                    GoalType.POINTS -> Icons.Default.Stars
                                },
                                null,
                                tint = if (isSelected) Accent else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                type.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Accent else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Title field
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("Название", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Например: Достичь рейтинга 1500", color = TextMuted) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Accent
                ),
                singleLine = true
            )
        }

        Spacer(Modifier.height(16.dp))

        // Target value
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("Целевое значение", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = targetValue,
                onValueChange = { targetValue = it.filter { c -> c.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Например: 1500", color = TextMuted) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Accent
                ),
                singleLine = true
            )
        }

        Spacer(Modifier.height(16.dp))

        // Description
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("Описание (опционально)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                placeholder = { Text("Описание цели...", color = TextMuted) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Accent
                )
            )
        }

        // Error
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = ILeaderColors.Error, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp))
        }

        Spacer(Modifier.height(24.dp))

        // Save button
        val canSave = title.isNotBlank() && targetValue.isNotBlank() && !saving
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(enabled = canSave) {
                    saving = true
                    error = null
                    scope.launch {
                        try {
                            val target = targetValue.toIntOrNull() ?: 0
                            val dto = GoalInsertDto(
                                athleteId = userId,
                                type = selectedType.name.lowercase(),
                                title = title.trim(),
                                description = description.trim().ifEmpty { null },
                                createdById = userId,
                                targetRating = if (selectedType == GoalType.RATING) target else null,
                                targetWins = if (selectedType == GoalType.TOURNAMENT) target else null,
                                targetPoints = if (selectedType == GoalType.POINTS) target else null
                            )
                            repo.createGoal(dto)
                            onCreated()
                        } catch (e: Exception) {
                            error = e.message ?: "Ошибка создания цели"
                            saving = false
                        }
                    }
                },
            shape = RoundedCornerShape(14.dp),
            color = if (canSave) Accent else TextMuted.copy(alpha = 0.3f)
        ) {
            Row(
                Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (saving) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    if (saving) "Сохранение..." else "Создать цель",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
