package com.ileader.app.ui.components.bracket

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.BracketMatch
import com.ileader.app.data.models.MatchGame
import com.ileader.app.data.models.MatchStatus
import com.ileader.app.ui.components.DarkTheme

@Composable
fun MatchDetailDialog(
    match: BracketMatch,
    canEdit: Boolean = false,
    onDismiss: () -> Unit,
    onSaveResult: ((matchId: String, games: List<MatchGame>, winnerId: String) -> Unit)? = null,
    onRevert: ((matchId: String) -> Unit)? = null
) {
    var editMode by remember { mutableStateOf(false) }
    var editGames by remember { mutableStateOf(match.games.ifEmpty {
        listOf(MatchGame(gameNumber = 1))
    }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkTheme.CardBg,
        title = {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Матч #${match.matchNumber}", fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary)
                Surface(shape = RoundedCornerShape(8.dp), color = when (match.status) {
                    MatchStatus.COMPLETED -> Color(0xFF22C55E).copy(alpha = 0.1f)
                    MatchStatus.IN_PROGRESS -> Color(0xFF3B82F6).copy(alpha = 0.1f)
                    else -> DarkTheme.CardBorder.copy(alpha = 0.3f)
                }) {
                    Text(
                        match.status.displayName,
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = when (match.status) {
                            MatchStatus.COMPLETED -> Color(0xFF22C55E)
                            MatchStatus.IN_PROGRESS -> Color(0xFF3B82F6)
                            else -> DarkTheme.TextMuted
                        }
                    )
                }
            }
        },
        text = {
            Column {
                // VS Section
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                    // Participant 1
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            match.participant1Name ?: "TBD",
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            color = if (match.winnerId == match.participant1Id) Color(0xFF22C55E) else DarkTheme.TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        if (match.participant1Seed != null) {
                            Text("Сид ${match.participant1Seed}", fontSize = 11.sp, color = DarkTheme.TextMuted)
                        }
                    }
                    // Score
                    Text(
                        "${match.participant1Score} : ${match.participant2Score}",
                        fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                        color = DarkTheme.TextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    // Participant 2
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            match.participant2Name ?: "TBD",
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            color = if (match.winnerId == match.participant2Id) Color(0xFF22C55E) else DarkTheme.TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        if (match.participant2Seed != null) {
                            Text("Сид ${match.participant2Seed}", fontSize = 11.sp, color = DarkTheme.TextMuted)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Quick win buttons (BO1 + not completed)
                if (canEdit && match.status != MatchStatus.COMPLETED && match.games.size <= 1 && !editMode) {
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        if (match.participant1Id != null) {
                            Button(
                                onClick = {
                                    val games = listOf(MatchGame(1, 1, 0, match.participant1Id, "completed"))
                                    onSaveResult?.invoke(match.id, games, match.participant1Id)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.EmojiEvents, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    match.participant1Name?.split(" ")?.firstOrNull() ?: "P1",
                                    fontSize = 12.sp, maxLines = 1
                                )
                            }
                        }
                        if (match.participant2Id != null) {
                            Button(
                                onClick = {
                                    val games = listOf(MatchGame(1, 0, 1, match.participant2Id, "completed"))
                                    onSaveResult?.invoke(match.id, games, match.participant2Id)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.EmojiEvents, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    match.participant2Name?.split(" ")?.firstOrNull() ?: "P2",
                                    fontSize = 12.sp, maxLines = 1
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Games table (for BO3/BO5 or edit mode)
                if (editMode || match.games.size > 1 || match.status == MatchStatus.COMPLETED) {
                    Text("Геймы", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextSecondary)
                    Spacer(Modifier.height(8.dp))

                    if (editMode) {
                        editGames.forEachIndexed { index, game ->
                            GameEditRow(
                                gameNumber = game.gameNumber,
                                score1 = game.participant1Score,
                                score2 = game.participant2Score,
                                onScore1Change = { s ->
                                    editGames = editGames.toMutableList().also {
                                        it[index] = it[index].copy(participant1Score = s)
                                    }
                                },
                                onScore2Change = { s ->
                                    editGames = editGames.toMutableList().also {
                                        it[index] = it[index].copy(participant2Score = s)
                                    }
                                }
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    } else {
                        match.games.forEach { game ->
                            GameDisplayRow(game)
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (canEdit) {
                if (editMode) {
                    Button(
                        onClick = {
                            val p1Wins = editGames.count { it.participant1Score > it.participant2Score }
                            val p2Wins = editGames.count { it.participant2Score > it.participant1Score }
                            val winnerId = when {
                                p1Wins > p2Wins -> match.participant1Id
                                p2Wins > p1Wins -> match.participant2Id
                                else -> null
                            }
                            if (winnerId != null) {
                                val completedGames = editGames.map { g ->
                                    val gWinner = when {
                                        g.participant1Score > g.participant2Score -> match.participant1Id
                                        g.participant2Score > g.participant1Score -> match.participant2Id
                                        else -> null
                                    }
                                    g.copy(winnerId = gWinner, status = "completed")
                                }
                                onSaveResult?.invoke(match.id, completedGames, winnerId)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Сохранить")
                    }
                } else if (match.status != MatchStatus.COMPLETED) {
                    Button(
                        onClick = { editMode = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Редактировать")
                    }
                }
            }
        },
        dismissButton = {
            Row {
                if (canEdit && match.status == MatchStatus.COMPLETED && onRevert != null) {
                    TextButton(onClick = { onRevert(match.id); onDismiss() }) {
                        Text("Откатить", color = Color(0xFFEF4444))
                    }
                    Spacer(Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text("Закрыть", color = DarkTheme.TextSecondary)
                }
            }
        }
    )
}

@Composable
private fun GameEditRow(
    gameNumber: Int,
    score1: Int,
    score2: Int,
    onScore1Change: (Int) -> Unit,
    onScore2Change: (Int) -> Unit
) {
    Row(
        Modifier.fillMaxWidth()
            .background(DarkTheme.CardBorder.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Гейм $gameNumber", fontSize = 12.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScoreInput(score1, onScore1Change)
            Text(" : ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextSecondary)
            ScoreInput(score2, onScore2Change)
        }
    }
}

@Composable
private fun ScoreInput(value: Int, onChange: (Int) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    BasicTextField(
        value = text,
        onValueChange = {
            text = it.filter { c -> c.isDigit() }.take(3)
            onChange(text.toIntOrNull() ?: 0)
        },
        textStyle = TextStyle(
            fontSize = 14.sp, fontWeight = FontWeight.Bold,
            color = DarkTheme.TextPrimary, textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.width(36.dp)
            .background(DarkTheme.CardBg, RoundedCornerShape(6.dp))
            .border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(6.dp))
            .padding(vertical = 6.dp),
        singleLine = true
    )
}

@Composable
private fun GameDisplayRow(game: MatchGame) {
    Row(
        Modifier.fillMaxWidth()
            .background(DarkTheme.CardBorder.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Гейм ${game.gameNumber}", fontSize = 12.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${game.participant1Score}",
                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                color = if (game.participant1Score > game.participant2Score) Color(0xFF22C55E) else DarkTheme.TextSecondary
            )
            Text(" : ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextSecondary)
            Text(
                "${game.participant2Score}",
                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                color = if (game.participant2Score > game.participant1Score) Color(0xFF22C55E) else DarkTheme.TextSecondary
            )
        }
        if (game.status == "completed") {
            Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = Color(0xFF22C55E))
        }
    }
}
