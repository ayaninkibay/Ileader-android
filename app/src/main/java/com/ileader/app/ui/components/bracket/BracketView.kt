package com.ileader.app.ui.components.bracket

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.BracketMatch
import com.ileader.app.data.models.TournamentGroup
import com.ileader.app.data.bracket.BracketUtils
import com.ileader.app.ui.components.DarkTheme

@Composable
fun BracketView(
    format: String,
    matches: List<BracketMatch>,
    groups: List<TournamentGroup> = emptyList(),
    onMatchClick: ((BracketMatch) -> Unit)? = null,
    highlightParticipantId: String? = null
) {
    val hasGroups = format == "groups_single_elim" || format == "groups_double_elim"
    val isRoundRobin = format == "round_robin"

    when {
        isRoundRobin -> {
            RoundRobinView(
                matches = matches,
                groups = groups,
                onMatchClick = onMatchClick,
                highlightParticipantId = highlightParticipantId
            )
        }
        hasGroups -> {
            var selectedTab by remember { mutableIntStateOf(0) }
            val tabs = listOf("Группы", "Плей-офф")
            val groupMatches = matches.filter { it.groupId != null }
            val playoffMatches = matches.filter { it.groupId == null }

            Column {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkTheme.CardBg,
                    contentColor = DarkTheme.Accent,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = DarkTheme.Accent
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) DarkTheme.Accent else DarkTheme.TextMuted
                                )
                            }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> {
                        GroupStageView(groups = groups)
                        if (groupMatches.isNotEmpty()) {
                            Text(
                                "Матчи в группах",
                                fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                color = DarkTheme.TextPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Column(
                                Modifier.padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                groupMatches.sortedBy { it.matchNumber }.forEach { match ->
                                    MatchCardView(
                                        match = match,
                                        onClick = onMatchClick?.let { { it(match) } },
                                        compact = true,
                                        highlightParticipantId = highlightParticipantId
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        if (format == "groups_double_elim") {
                            DoubleEliminationView(
                                matches = playoffMatches,
                                onMatchClick = onMatchClick,
                                highlightParticipantId = highlightParticipantId
                            )
                        } else {
                            SingleEliminationView(
                                matches = playoffMatches,
                                onMatchClick = onMatchClick,
                                highlightParticipantId = highlightParticipantId
                            )
                        }
                    }
                }
            }
        }
        format == "double_elimination" -> {
            DoubleEliminationView(
                matches = matches,
                onMatchClick = onMatchClick,
                highlightParticipantId = highlightParticipantId
            )
        }
        else -> {
            SingleEliminationView(
                matches = matches,
                onMatchClick = onMatchClick,
                highlightParticipantId = highlightParticipantId
            )
        }
    }
}
