package com.ileader.app.ui.screens.athlete

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.TournamentResult
import com.ileader.app.data.repository.AthleteRepository
import com.ileader.app.ui.components.*
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted

@Composable
fun ResultsHistoryScreen(userId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val repo = remember { AthleteRepository() }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<List<TournamentResult>>(emptyList()) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                results = repo.getMyResults(userId).sortedByDescending { it.date }
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки"
            } finally { loading = false }
        }
    }
    LaunchedEffect(userId) { load() }

    Column(Modifier.fillMaxSize().background(Bg)) {
        BackHeader("История результатов", onBack)
        when {
            loading -> LoadingScreen()
            error != null -> ErrorScreen(error!!) { load() }
            results.isEmpty() -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                EmptyState(title = "Нет завершённых турниров", subtitle = "Здесь появятся результаты ваших турниров", icon = Icons.Outlined.EmojiEvents)
            }
            else -> {
                val grouped = results.groupBy { it.sportName.ifBlank { "Без вида спорта" } }
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (sport, list) ->
                        item {
                            Text(sport, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                        }
                        items(list) { ResultRow(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(r: TournamentResult) {
    val medalColor = when (r.position) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> TextMuted
    }
    DarkCardPadded {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(50), color = medalColor.copy(0.15f)) {
                Text("#${r.position}", Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = medalColor)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(r.tournamentName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(r.date.take(10), fontSize = 12.sp, color = TextMuted)
            }
            Text("${r.points} очк.", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}
