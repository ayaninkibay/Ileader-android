package com.ileader.app.ui.components.bracket

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.BracketMatch
import com.ileader.app.data.models.MatchStatus
import com.ileader.app.ui.components.DarkTheme

@Composable
fun MatchCardView(
    match: BracketMatch,
    onClick: (() -> Unit)? = null,
    compact: Boolean = false,
    highlightParticipantId: String? = null
) {
    val statusColor = when (match.status) {
        MatchStatus.SCHEDULED -> DarkTheme.TextMuted
        MatchStatus.IN_PROGRESS -> Color(0xFF3B82F6) // blue
        MatchStatus.COMPLETED -> Color(0xFF22C55E) // green
        MatchStatus.CANCELLED -> Color(0xFFEF4444) // red
    }

    if (match.isBye) {
        // Bye card - simplified
        Surface(
            modifier = Modifier.width(if (compact) 160.dp else 200.dp),
            shape = RoundedCornerShape(10.dp),
            color = DarkTheme.CardBg.copy(alpha = 0.5f)
        ) {
            Column(
                Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("BYE", fontSize = 11.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    match.participant1Name ?: match.participant2Name ?: "TBD",
                    fontSize = 12.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.SemiBold
                )
                Text("Автопроход", fontSize = 10.sp, color = DarkTheme.TextMuted)
            }
        }
        return
    }

    Surface(
        modifier = Modifier
            .width(if (compact) 160.dp else 200.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(10.dp),
        color = DarkTheme.CardBg
    ) {
        Column(
            Modifier.border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(10.dp))
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth()
                    .background(statusColor.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Матч #${match.matchNumber}",
                    fontSize = 10.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (match.status == MatchStatus.IN_PROGRESS) {
                        Box(
                            Modifier.size(6.dp).clip(CircleShape).background(statusColor)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        match.status.displayName,
                        fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Participant 1
            ParticipantRow(
                name = match.participant1Name,
                seed = match.participant1Seed,
                score = match.participant1Score,
                isWinner = match.winnerId != null && match.winnerId == match.participant1Id,
                isHighlighted = highlightParticipantId != null && match.participant1Id == highlightParticipantId,
                isCompleted = match.status == MatchStatus.COMPLETED
            )

            HorizontalDivider(thickness = 0.5.dp, color = DarkTheme.CardBorder.copy(alpha = 0.5f))

            // Participant 2
            ParticipantRow(
                name = match.participant2Name,
                seed = match.participant2Seed,
                score = match.participant2Score,
                isWinner = match.winnerId != null && match.winnerId == match.participant2Id,
                isHighlighted = highlightParticipantId != null && match.participant2Id == highlightParticipantId,
                isCompleted = match.status == MatchStatus.COMPLETED
            )
        }
    }
}

@Composable
private fun ParticipantRow(
    name: String?,
    seed: Int?,
    score: Int,
    isWinner: Boolean,
    isHighlighted: Boolean,
    isCompleted: Boolean
) {
    val bgColor = when {
        isHighlighted -> DarkTheme.AccentSoft
        isWinner -> Color(0xFF22C55E).copy(alpha = 0.06f)
        else -> Color.Transparent
    }
    val textColor = when {
        isCompleted && !isWinner && name != null -> DarkTheme.TextMuted
        isHighlighted -> DarkTheme.Accent
        else -> DarkTheme.TextPrimary
    }

    Row(
        Modifier.fillMaxWidth().background(bgColor).padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (seed != null) {
            Text(
                "$seed",
                fontSize = 10.sp, color = DarkTheme.TextMuted,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(18.dp)
            )
        }
        Text(
            name ?: "TBD",
            fontSize = 12.sp,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Medium,
            color = if (name == null) DarkTheme.TextMuted else textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (isWinner) {
            Icon(
                Icons.Default.CheckCircle, null,
                Modifier.size(14.dp).padding(end = 4.dp),
                tint = Color(0xFF22C55E)
            )
        }
        Text(
            "$score",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isWinner) Color(0xFF22C55E) else DarkTheme.TextSecondary
        )
    }
}
