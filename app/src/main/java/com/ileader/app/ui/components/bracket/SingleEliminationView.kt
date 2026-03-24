package com.ileader.app.ui.components.bracket

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.BracketMatch
import com.ileader.app.data.models.BracketType
import com.ileader.app.data.bracket.BracketUtils
import com.ileader.app.ui.components.DarkTheme

@Composable
fun SingleEliminationView(
    matches: List<BracketMatch>,
    onMatchClick: ((BracketMatch) -> Unit)? = null,
    highlightParticipantId: String? = null
) {
    val upperMatches = matches.filter { it.bracketType == BracketType.UPPER }
    val thirdPlaceMatch = BracketUtils.getThirdPlaceMatch(matches)
    val maxRound = BracketUtils.getMaxRound(upperMatches)
    val matchesByRound = upperMatches.groupBy { it.round }.toSortedMap()

    Column {
        // Bracket grid - horizontal scroll
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            matchesByRound.forEach { (round, roundMatches) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.defaultMinSize(minHeight = (roundMatches.size * 80 + (roundMatches.size - 1) * 16).coerceAtLeast(80).dp)
                ) {
                    // Round header
                    Text(
                        BracketUtils.getRoundName(round, maxRound),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = DarkTheme.TextMuted
                    )
                    Spacer(Modifier.height(8.dp))

                    roundMatches.forEach { match ->
                        MatchCardView(
                            match = match,
                            onClick = onMatchClick?.let { { it(match) } },
                            compact = roundMatches.size > 4,
                            highlightParticipantId = highlightParticipantId
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        // Third place match
        if (thirdPlaceMatch != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Матч за 3-е место",
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = DarkTheme.TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            Box(Modifier.padding(horizontal = 16.dp)) {
                MatchCardView(
                    match = thirdPlaceMatch,
                    onClick = onMatchClick?.let { { it(thirdPlaceMatch) } },
                    highlightParticipantId = highlightParticipantId
                )
            }
        }
    }
}
