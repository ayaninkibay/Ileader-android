package com.ileader.app.ui.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ViewerTournamentDetailViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun ViewerTournamentResultsScreen(
    tournamentId: String,
    user: User,
    onBack: () -> Unit = {}
) {
    val viewModel: ViewerTournamentDetailViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(tournamentId) { viewModel.load(tournamentId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(tournamentId) }
        is UiState.Success -> {
            val data = s.data
            val tournament = data.tournament
            val results = data.results.sortedBy { it.position }
            val top3 = results.filter { it.position <= 3 }
            val locationName = tournament.locations?.name ?: ""
            val locationCity = tournament.locations?.city ?: tournament.region ?: ""

            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding()
            ) {
                // Header
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = TextPrimary)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Результаты", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
                        Text(tournament.name, fontSize = 13.sp, color = TextSecondary, maxLines = 1)
                    }
                }

                Text(
                    buildString {
                        if (locationName.isNotBlank()) append("$locationName, ")
                        if (locationCity.isNotBlank()) append("$locationCity · ")
                        append(formatShortDate(tournament.startDate))
                    },
                    fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(Modifier.height(20.dp))

                // Podium
                if (top3.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        top3.forEach { result ->
                            val posLabel = when (result.position) { 1 -> "1 место"; 2 -> "2 место"; 3 -> "3 место"; else -> "${result.position} место" }
                            val athleteName = result.profiles?.name ?: "—"

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = CardBg
                            ) {
                                Column(
                                    Modifier.border(
                                        if (result.position == 1) 2.dp else 0.5.dp,
                                        if (result.position == 1) Accent.copy(alpha = 0.5f) else CardBorder.copy(alpha = 0.5f),
                                        RoundedCornerShape(16.dp)
                                    ).padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        Modifier.size(32.dp).clip(CircleShape)
                                            .background(if (result.position == 1) AccentSoft else CardBorder.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            when (result.position) { 1 -> Icons.Default.EmojiEvents; 2 -> Icons.Default.MilitaryTech; else -> Icons.Default.WorkspacePremium },
                                            null, tint = if (result.position == 1) Accent else TextMuted, modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(posLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(Modifier.height(2.dp))
                                    Text(athleteName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.Center, maxLines = 2)
                                    Spacer(Modifier.height(2.dp))
                                    val resultText = result.time ?: ""
                                    if (resultText.isNotEmpty()) {
                                        Text(resultText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }

                // Full results table
                if (results.isNotEmpty()) {
                    Surface(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp), color = CardBg
                    ) {
                        Column(
                            Modifier.border(0.5.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SoftIconBox(icon = Icons.Default.EmojiEvents, size = 28.dp, iconSize = 16.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Полная таблица результатов", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                Modifier.fillMaxWidth().background(CardBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("Место", Modifier.width(48.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                                Text("Участник", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                                Text("Время/Очки", Modifier.width(80.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, textAlign = TextAlign.End)
                            }

                            results.forEach { result ->
                                val athleteName = result.profiles?.name ?: "—"
                                Spacer(Modifier.height(1.dp))
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(Modifier.width(48.dp), verticalAlignment = Alignment.CenterVertically) {
                                        if (result.position in 1..3) {
                                            Icon(
                                                when (result.position) { 1 -> Icons.Default.EmojiEvents; 2 -> Icons.Default.MilitaryTech; else -> Icons.Default.WorkspacePremium },
                                                null, tint = Accent, modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(2.dp))
                                        }
                                        Text("${result.position}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text(athleteName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        result.category?.let { Text(it, fontSize = 12.sp, color = TextMuted) }
                                    }
                                    Column(Modifier.width(80.dp), horizontalAlignment = Alignment.End) {
                                        result.time?.let { Text(it, fontSize = 13.sp, color = TextSecondary) }
                                        val pts = result.points ?: 0
                                        if (pts > 0) { Text("$pts очк.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Accent) }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        EmptyState("Результаты не опубликованы", "Данные появятся позже")
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
