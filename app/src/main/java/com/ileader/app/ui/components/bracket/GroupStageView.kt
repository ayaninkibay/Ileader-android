package com.ileader.app.ui.components.bracket

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.GroupParticipant
import com.ileader.app.data.models.TournamentGroup
import com.ileader.app.ui.components.DarkTheme

@Composable
fun GroupStageView(
    groups: List<TournamentGroup>,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        groups.forEach { group ->
            GroupTable(group)
        }
    }
}

@Composable
private fun GroupTable(group: TournamentGroup) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkTheme.CardBg
    ) {
        Column(
            Modifier.border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp))
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth()
                    .background(DarkTheme.AccentSoft)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    group.name,
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary
                )
            }

            // Column headers
            Row(
                Modifier.fillMaxWidth()
                    .background(DarkTheme.CardBorder.copy(alpha = 0.3f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#", fontSize = 11.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(24.dp))
                Text("Участник", fontSize = 11.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("И", fontSize = 11.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
                Text("В", fontSize = 11.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
                Text("Н", fontSize = 11.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
                Text("П", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
                Text("Очки", fontSize = 11.sp, color = DarkTheme.TextMuted, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp))
            }

            // Rows
            group.participants.sortedBy { it.position }.forEachIndexed { index, p ->
                if (index > 0) {
                    HorizontalDivider(
                        Modifier.padding(horizontal = 14.dp),
                        thickness = 0.5.dp, color = DarkTheme.CardBorder.copy(alpha = 0.3f)
                    )
                }
                GroupRow(p)
            }
        }
    }
}

@Composable
private fun GroupRow(participant: GroupParticipant) {
    val rowBg = if (participant.qualified) DarkTheme.AccentSoft.copy(alpha = 0.5f) else Color.Transparent

    Row(
        Modifier.fillMaxWidth().background(rowBg).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position
        if (participant.position == 1) {
            Icon(Icons.Default.EmojiEvents, null, Modifier.size(18.dp).width(24.dp), tint = Color(0xFFEAB308))
        } else {
            Text(
                "${participant.position}",
                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                color = if (participant.qualified) DarkTheme.Accent else DarkTheme.TextMuted,
                modifier = Modifier.width(24.dp)
            )
        }

        // Name
        Column(Modifier.weight(1f)) {
            Text(
                participant.athleteName,
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = DarkTheme.TextPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            if (participant.team != null) {
                Text(participant.team, fontSize = 11.sp, color = DarkTheme.TextMuted, maxLines = 1)
            }
        }

        // Stats
        Text("${participant.gamesPlayed}", fontSize = 12.sp, color = DarkTheme.TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
        Text("${participant.wins}", fontSize = 12.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
        Text("${participant.draws}", fontSize = 12.sp, color = DarkTheme.TextMuted, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
        Text("${participant.losses}", fontSize = 12.sp, color = Color(0xFFEF4444), textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
        Text("${participant.points}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp))
    }
}
