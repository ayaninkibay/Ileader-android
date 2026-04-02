package com.ileader.app.ui.screens.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.InterviewDto
import com.ileader.app.data.remote.dto.InterviewStatsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.MediaViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted

private val InterviewColor = Color(0xFFE11D48)
private val InterviewColorLight = Color(0xFFFB7185)

private enum class InterviewFilter(val label: String) {
    ALL("Все"), SCHEDULED("Запланированные"), COMPLETED("Завершённые"), PUBLISHED("Опубликованные")
}

@Composable
fun MediaInterviewsScreen(
    user: User,
    onBack: () -> Unit,
    onInterviewClick: (String) -> Unit,
    onCreateInterview: () -> Unit,
    vm: MediaViewModel = viewModel()
) {
    val interviews by vm.interviews.collectAsState()
    val stats by vm.interviewStats.collectAsState()
    val actionState by vm.actionState.collectAsState()

    var started by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(InterviewFilter.ALL) }
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }

    val snackbar = LocalSnackbarHost.current

    LaunchedEffect(user.id) {
        vm.loadInterviews(user.id)
        started = true
    }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is UiState.Success -> {
                snackbar.showSnackbar(s.data)
                vm.clearAction()
                deleteConfirmId = null
            }
            is UiState.Error -> {
                snackbar.showSnackbar(s.message)
                vm.clearAction()
            }
            else -> {}
        }
    }

    val allInterviews = if (interviews is UiState.Success) (interviews as UiState.Success).data else emptyList()
    val scheduled = allInterviews.filter { it.status == "scheduled" }
    val completed = allInterviews.filter { it.status == "completed" }
    val published = allInterviews.filter { it.status == "published" }
    val filtered = when (selectedFilter) {
        InterviewFilter.ALL -> allInterviews
        InterviewFilter.SCHEDULED -> scheduled
        InterviewFilter.COMPLETED -> completed
        InterviewFilter.PUBLISHED -> published
    }

    // Nearest scheduled interview
    val today = remember { java.time.LocalDate.now().toString() }
    val nearest = remember(scheduled) {
        scheduled.filter { (it.scheduledDate ?: "") >= today }
            .minByOrNull { it.scheduledDate ?: "9999" }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Bg),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ── 1. Hero ──
        item {
            FadeIn(visible = started, delayMs = 0) {
                InterviewHero(stats = stats, onBack = onBack)
            }
        }

        // ── 2. Nearest interview countdown ──
        if (nearest != null) {
            item {
                Spacer(Modifier.height(16.dp))
                FadeIn(visible = started, delayMs = 60) {
                    NextInterviewCard(
                        interview = nearest,
                        onClick = { onInterviewClick(nearest.id) }
                    )
                }
            }
        }

        // ── 3. Create button ──
        item {
            Spacer(Modifier.height(16.dp))
            FadeIn(visible = started, delayMs = 100) {
                CreateInterviewButton(onClick = onCreateInterview)
            }
        }

        // ── 4. Filter chips ──
        item {
            Spacer(Modifier.height(16.dp))
            FadeIn(visible = started, delayMs = 120) {
                InterviewFilterChips(
                    selected = selectedFilter,
                    onSelect = { selectedFilter = it },
                    counts = mapOf(
                        InterviewFilter.ALL to allInterviews.size,
                        InterviewFilter.SCHEDULED to scheduled.size,
                        InterviewFilter.COMPLETED to completed.size,
                        InterviewFilter.PUBLISHED to published.size
                    )
                )
            }
        }

        when (interviews) {
            is UiState.Loading -> {
                item {
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        LoadingScreen()
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.padding(16.dp)) {
                        EmptyState(
                            title = "Ошибка загрузки",
                            subtitle = (interviews as UiState.Error).message
                        )
                    }
                }
            }
            is UiState.Success -> {
                if (filtered.isEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        EmptyState(
                            title = if (allInterviews.isEmpty()) "Нет интервью" else "Нет интервью",
                            subtitle = if (allInterviews.isEmpty()) "Запланируйте интервью со спортсменом"
                            else "В этой категории пока нет интервью",
                            icon = Icons.Default.Videocam
                        )
                    }
                } else {
                    item { Spacer(Modifier.height(8.dp)) }
                    itemsIndexed(filtered, key = { _, it -> it.id }) { index, interview ->
                        val delay = (160 + index * 50).coerceAtMost(500)
                        FadeIn(visible = started, delayMs = delay) {
                            InterviewCard(
                                interview = interview,
                                onClick = { onInterviewClick(interview.id) },
                                onDelete = { deleteConfirmId = interview.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation
    if (deleteConfirmId != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            containerColor = CardBg,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Удалить интервью?", color = TextPrimary) },
            text = { Text("Это действие нельзя отменить", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { deleteConfirmId?.let { vm.deleteInterview(it, user.id) } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) {
                    Text("Отмена", color = TextMuted)
                }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════
// Hero
// ══════════════════════════════════════════════════════════

@Composable
private fun InterviewHero(stats: InterviewStatsDto, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        Box(
            Modifier.matchParentSize().background(
                Brush.linearGradient(listOf(Color(0xFF9F1239), InterviewColor, InterviewColorLight))
            )
        )
        Column(
            Modifier.statusBarsPadding().padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 20.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Videocam, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Text("Видеоинтервью", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.5).sp)
            }
            Spacer(Modifier.height(4.dp))
            Text("Интервью со спортсменами", fontSize = 14.sp, color = Color.White.copy(0.7f))

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroPill(Icons.Default.Videocam, "${stats.total}", "Всего", Color.White.copy(0.2f))
                if (stats.scheduled > 0) HeroPill(Icons.Default.Schedule, "${stats.scheduled}", "Запл.", Color(0xFFF59E0B).copy(0.4f))
                if (stats.completed > 0) HeroPill(Icons.Default.CheckCircle, "${stats.completed}", "Заверш.", Color(0xFF3B82F6).copy(0.4f))
                if (stats.published > 0) HeroPill(Icons.Default.Public, "${stats.published}", "Опубл.", Color(0xFF10B981).copy(0.4f))
            }

            // Progress bar
            if (stats.total > 0) {
                Spacer(Modifier.height(14.dp))
                val progress = (stats.completed + stats.published).toFloat() / stats.total
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Процесс завершения", fontSize = 11.sp, color = Color.White.copy(0.7f))
                        Text("${((stats.completed + stats.published).toFloat() / stats.total * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroPill(icon: ImageVector, value: String, label: String, bg: Color) {
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(12.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 10.sp, color = Color.White.copy(0.7f))
        }
    }
}

// ══════════════════════════════════════════════════════════
// Next Interview Card (countdown-style)
// ══════════════════════════════════════════════════════════

@Composable
private fun NextInterviewCard(interview: InterviewDto, onClick: () -> Unit) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current

    val today = remember { java.time.LocalDate.now() }
    val daysUntil = remember(interview.scheduledDate) {
        interview.scheduledDate?.take(10)?.let {
            try { java.time.temporal.ChronoUnit.DAYS.between(today, java.time.LocalDate.parse(it)).toInt() }
            catch (_: Exception) { null }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, InterviewColor.copy(0.3f)),
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Countdown circle
            Box(
                Modifier.size(56.dp).clip(CircleShape).background(
                    Brush.linearGradient(listOf(InterviewColor, InterviewColorLight))
                ),
                contentAlignment = Alignment.Center
            ) {
                if (daysUntil != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            when (daysUntil) { 0 -> "!" ; else -> "$daysUntil" },
                            fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                        )
                        if (daysUntil > 0) {
                            Text("дн.", fontSize = 10.sp, color = Color.White.copy(0.8f), fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    Icon(Icons.Default.Videocam, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    when (daysUntil) { 0 -> "Сегодня!"; 1 -> "Завтра"; null -> "Ближайшее"; else -> "Через $daysUntil дн." },
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = InterviewColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    interview.title,
                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    interview.athlete?.name?.let { name ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(name, fontSize = 12.sp, color = TextMuted)
                        }
                    }
                    interview.scheduledDate?.let { date ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(formatDateShort(date), fontSize = 12.sp, color = TextMuted)
                        }
                    }
                    interview.time?.let { time ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(time, fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Create Interview Button
// ══════════════════════════════════════════════════════════

@Composable
private fun CreateInterviewButton(onClick: () -> Unit) {
    val isDark = DarkTheme.isDark

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, InterviewColor.copy(0.3f)),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(
                    Brush.linearGradient(listOf(InterviewColor, InterviewColorLight))
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Запланировать интервью", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("Выберите спортсмена и назначьте дату", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Filter Chips
// ══════════════════════════════════════════════════════════

@Composable
private fun InterviewFilterChips(
    selected: InterviewFilter,
    onSelect: (InterviewFilter) -> Unit,
    counts: Map<InterviewFilter, Int>
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(InterviewFilter.entries.toList()) { filter ->
            val isSelected = selected == filter
            val count = counts[filter] ?: 0
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) InterviewColor else CardBg,
                border = if (!isSelected && DarkTheme.isDark) DarkTheme.cardBorderStroke else null,
                modifier = Modifier.clickable { onSelect(filter) }
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        filter.label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                    if (count > 0) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.White.copy(0.25f) else TextMuted.copy(0.15f),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    "$count", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Interview Card
// ══════════════════════════════════════════════════════════

@Composable
private fun InterviewCard(
    interview: InterviewDto,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current
    val statusColor = getInterviewStatusColor(interview.status)
    val statusLabel = getInterviewStatusLabel(interview.status)

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke
        else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(0.3f)),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Row(Modifier.padding(12.dp)) {
            // Avatar or icon
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
            ) {
                val avatarUrl = interview.athlete?.avatarUrl
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.linearGradient(listOf(InterviewColor.copy(0.7f), InterviewColorLight.copy(0.4f)))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(26.dp))
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                // Title
                Text(
                    interview.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Athlete name
                interview.athlete?.name?.let { name ->
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = InterviewColor.copy(0.7f), modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(name, fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Meta row: status + topic + date
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(0.15f)) {
                        Text(
                            statusLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                    interview.topic?.let { topic ->
                        Text(topic, fontSize = 11.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Date, time, location, tournament
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        interview.scheduledDate?.let { date ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(formatDateShort(date), fontSize = 11.sp, color = TextMuted)
                            }
                        }
                        interview.time?.let { time ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(time, fontSize = 11.sp, color = TextMuted)
                            }
                        }
                        interview.location?.let { loc ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                                Icon(Icons.Default.LocationOn, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(loc, fontSize = 11.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, tint = TextMuted.copy(0.5f), modifier = Modifier.size(16.dp))
                    }
                }

                // Tournament tag
                interview.tournaments?.name?.let { tName ->
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF3B82F6).copy(0.1f)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(tName, fontSize = 10.sp, color = Color(0xFF3B82F6), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Helpers
// ══════════════════════════════════════════════════════════

private fun formatDateShort(dateStr: String): String {
    val parts = dateStr.split("T")[0].split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2].toIntOrNull() ?: return dateStr
    val m = listOf("", "янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
    val month = parts[1].toIntOrNull() ?: return dateStr
    return "$day ${m.getOrElse(month) { "" }}"
}

private fun getInterviewStatusColor(status: String?): Color = when (status) {
    "scheduled" -> Color(0xFFF59E0B)
    "completed" -> Color(0xFF3B82F6)
    "published" -> Color(0xFF10B981)
    "cancelled" -> Color(0xFFEF4444)
    else -> Color(0xFF8E8E93)
}

private fun getInterviewStatusLabel(status: String?): String = when (status) {
    "scheduled" -> "Запланировано"
    "completed" -> "Завершено"
    "published" -> "Опубликовано"
    "cancelled" -> "Отменено"
    else -> "Неизвестно"
}
