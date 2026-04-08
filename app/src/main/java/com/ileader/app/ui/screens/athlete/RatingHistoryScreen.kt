package com.ileader.app.ui.screens.athlete

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.RatingHistoryEntry
import com.ileader.app.data.repository.AthleteRepository
import com.ileader.app.ui.components.*
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun RatingHistoryScreen(userId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val repo = remember { AthleteRepository() }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var entries by remember { mutableStateOf<List<RatingHistoryEntry>>(emptyList()) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                entries = repo.getRatingHistory(userId)
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки"
            } finally { loading = false }
        }
    }
    LaunchedEffect(userId) { load() }

    Column(Modifier.fillMaxSize().background(Bg)) {
        BackHeader("История рейтинга", onBack)
        when {
            loading -> LoadingScreen()
            error != null -> ErrorScreen(error!!) { load() }
            entries.isEmpty() -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                EmptyState(title = "Нет данных", subtitle = "История изменений рейтинга появится после турниров", icon = Icons.Outlined.Timeline)
            }
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { RatingChart(entries) }
                items(entries) { entry -> RatingRow(entry) }
            }
        }
    }
}

@Composable
private fun RatingChart(entries: List<RatingHistoryEntry>) {
    val points = entries.reversed().map { it.rating.toFloat() }
    if (points.size < 2) return
    val minV = points.min()
    val maxV = points.max().coerceAtLeast(minV + 1f)
    val accent = Accent
    val muted = TextMuted
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBg
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Динамика рейтинга", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
            Canvas(Modifier.fillMaxWidth().height(140.dp)) {
                val w = size.width
                val h = size.height
                val stepX = w / (points.size - 1).coerceAtLeast(1)
                val path = Path()
                points.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = h - ((v - minV) / (maxV - minV)) * h
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = accent, style = Stroke(width = 4f, cap = StrokeCap.Round))
                points.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = h - ((v - minV) / (maxV - minV)) * h
                    drawCircle(accent, radius = 5f, center = Offset(x, y))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("${minV.toInt()}", fontSize = 11.sp, color = muted)
                Text("${maxV.toInt()}", fontSize = 11.sp, color = muted)
            }
        }
    }
}

@Composable
private fun RatingRow(entry: RatingHistoryEntry) {
    val (icon, color) = when {
        entry.delta > 0 -> Icons.Default.TrendingUp to Color(0xFF22C55E)
        entry.delta < 0 -> Icons.Default.TrendingDown to Color(0xFFEF4444)
        else -> Icons.Default.TrendingFlat to TextMuted
    }
    val reasonLabel = when (entry.reason) {
        "tournament_result" -> "Результат турнира"
        "manual_adjustment" -> "Корректировка"
        "initial" -> "Стартовый рейтинг"
        else -> entry.reason.ifBlank { "Изменение" }
    }
    DarkCardPadded {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.tournamentName ?: reasonLabel,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary
                )
                val sub = listOfNotNull(entry.sportName, entry.date.takeIf { it.isNotBlank() }).joinToString(" · ")
                if (sub.isNotEmpty()) Text(sub, fontSize = 12.sp, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (entry.delta > 0) "+" else "") + entry.delta,
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color
                )
                Text("${entry.rating}", fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}
