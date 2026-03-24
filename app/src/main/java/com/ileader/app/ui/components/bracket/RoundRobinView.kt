package com.ileader.app.ui.components.bracket

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.BracketMatch
import com.ileader.app.data.models.TournamentGroup
import com.ileader.app.ui.components.DarkTheme

@Composable
fun RoundRobinView(
    matches: List<BracketMatch>,
    groups: List<TournamentGroup>,
    onMatchClick: ((BracketMatch) -> Unit)? = null,
    highlightParticipantId: String? = null
) {
    Column(Modifier.padding(16.dp)) {
        // Standings table
        if (groups.isNotEmpty()) {
            GroupStageView(groups, Modifier.padding(0.dp))
            Spacer(Modifier.height(20.dp))
        }

        // Match list
        Text(
            "Матчи",
            fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary
        )
        Spacer(Modifier.height(12.dp))

        // Grid of matches in rows of 2
        val sortedMatches = matches.sortedBy { it.matchNumber }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in sortedMatches.indices step 2) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MatchCardView(
                        match = sortedMatches[i],
                        onClick = onMatchClick?.let { { it(sortedMatches[i]) } },
                        compact = true,
                        highlightParticipantId = highlightParticipantId
                    )
                    if (i + 1 < sortedMatches.size) {
                        MatchCardView(
                            match = sortedMatches[i + 1],
                            onClick = onMatchClick?.let { { it(sortedMatches[i + 1]) } },
                            compact = true,
                            highlightParticipantId = highlightParticipantId
                        )
                    }
                }
            }
        }
    }
}
