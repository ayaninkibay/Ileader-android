package com.ileader.app.ui.components.bracket

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.BracketMatch
import com.ileader.app.data.models.BracketType
import com.ileader.app.data.bracket.BracketUtils
import com.ileader.app.ui.components.DarkTheme

@Composable
fun DoubleEliminationView(
    matches: List<BracketMatch>,
    onMatchClick: ((BracketMatch) -> Unit)? = null,
    highlightParticipantId: String? = null
) {
    val upperMatches = BracketUtils.getUpperBracketMatches(matches)
    val lowerMatches = BracketUtils.getLowerBracketMatches(matches)
    val grandFinal = BracketUtils.getGrandFinalMatch(matches)

    Column(Modifier.padding(16.dp)) {
        // Upper Bracket
        BracketSection(
            title = "Верхняя сетка",
            accentColor = Color(0xFF22C55E),
            matches = upperMatches,
            onMatchClick = onMatchClick,
            highlightParticipantId = highlightParticipantId
        )

        Spacer(Modifier.height(24.dp))

        // Lower Bracket
        if (lowerMatches.isNotEmpty()) {
            BracketSection(
                title = "Нижняя сетка",
                accentColor = Color(0xFFEF4444),
                matches = lowerMatches,
                onMatchClick = onMatchClick,
                highlightParticipantId = highlightParticipantId
            )
            Spacer(Modifier.height(24.dp))
        }

        // Grand Final
        if (grandFinal != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.width(3.dp).height(20.dp).clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFEAB308))
                )
                Spacer(Modifier.width(8.dp))
                Text("Гранд Финал", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEAB308))
            }
            Spacer(Modifier.height(12.dp))
            MatchCardView(
                match = grandFinal,
                onClick = onMatchClick?.let { { it(grandFinal) } },
                highlightParticipantId = highlightParticipantId
            )
        }
    }
}

@Composable
private fun BracketSection(
    title: String,
    accentColor: Color,
    matches: List<BracketMatch>,
    onMatchClick: ((BracketMatch) -> Unit)?,
    highlightParticipantId: String?
) {
    val matchesByRound = matches.groupBy { it.round }.toSortedMap()
    val maxRound = matches.maxOfOrNull { it.round } ?: 0

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.width(3.dp).height(20.dp).clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = accentColor)
    }
    Spacer(Modifier.height(12.dp))

    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        matchesByRound.forEach { (round, roundMatches) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.defaultMinSize(minHeight = (roundMatches.size * 80).coerceAtLeast(80).dp)
            ) {
                Text(
                    "Раунд $round",
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextMuted
                )
                Spacer(Modifier.height(8.dp))
                roundMatches.forEach { match ->
                    MatchCardView(
                        match = match,
                        onClick = onMatchClick?.let { { it(match) } },
                        compact = true,
                        highlightParticipantId = highlightParticipantId
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
