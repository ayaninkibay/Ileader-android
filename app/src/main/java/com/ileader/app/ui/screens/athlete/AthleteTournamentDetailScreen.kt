package com.ileader.app.ui.screens.athlete

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.bracket.BracketUtils
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.components.bracket.BracketView
import com.ileader.app.ui.components.bracket.MatchDetailDialog
import com.ileader.app.ui.viewmodels.AthleteTournamentsViewModel

@Composable
fun AthleteTournamentDetailScreen(
    tournamentId: String,
    user: User,
    viewModel: AthleteTournamentsViewModel,
    onBack: () -> Unit
) {
    val detailState by viewModel.detailState.collectAsState()
    val isRegistered by viewModel.isRegistered.collectAsState()

    LaunchedEffect(tournamentId) { viewModel.loadDetail(tournamentId, user.id) }

    val bracketData by viewModel.bracketData.collectAsState()

    when (val s = detailState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.loadDetail(tournamentId, user.id) }
        is UiState.Success -> DetailContent(
            tournament = s.data,
            isRegistered = isRegistered,
            onToggleRegistration = { viewModel.toggleRegistration(tournamentId, user.id) },
            onBack = onBack,
            bracketData = bracketData,
            userId = user.id
        )
    }
}

@Composable
private fun DetailContent(
    tournament: Tournament,
    isRegistered: Boolean,
    onToggleRegistration: () -> Unit,
    onBack: () -> Unit,
    bracketData: com.ileader.app.ui.viewmodels.AthleteTournamentBracketData = com.ileader.app.ui.viewmodels.AthleteTournamentBracketData(),
    userId: String = ""
) {
    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── BACK + TITLE ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onBack, shape = CircleShape, color = DarkTheme.CardBg,
                    modifier = Modifier.size(40.dp).border(0.5.dp, DarkTheme.CardBorder, CircleShape)
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", Modifier.size(20.dp), DarkTheme.TextPrimary)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(tournament.name, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(2.dp))
                    Text(tournament.sportName, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                }
                TournamentStatusBadge(tournament.status)
            }

            Spacer(Modifier.height(20.dp))

            // ── INFO CHIPS ──
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                InfoChip(Modifier.weight(1f), Icons.Default.CalendarMonth, tournament.startDate)
                InfoChip(Modifier.weight(1f), Icons.Default.Groups, "${tournament.currentParticipants}/${tournament.maxParticipants}")
                if (tournament.format.isNotEmpty()) {
                    InfoChip(Modifier.weight(1f), Icons.Default.AccountTree, tournament.format.take(12))
                }
            }

            // ── PARTICIPANTS PROGRESS ──
            if (tournament.maxParticipants > 0) {
                Spacer(Modifier.height(16.dp))
                DarkCardPadded {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Участники", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = DarkTheme.TextPrimary)
                        Text("${tournament.currentParticipants} из ${tournament.maxParticipants}",
                            fontSize = 13.sp, color = DarkTheme.TextSecondary)
                    }
                    Spacer(Modifier.height(8.dp))
                    DarkProgressBar(tournament.currentParticipants.toFloat() / tournament.maxParticipants)
                }
            }

            // ── DESCRIPTION ──
            if (tournament.description.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                SectionCard("О турнире", Icons.Default.Info) {
                    Text(tournament.description, fontSize = 14.sp, color = DarkTheme.TextSecondary, lineHeight = 20.sp)
                }
            }

            // ── REQUIREMENTS ──
            if (tournament.requirements.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                SectionCard("Требования", Icons.Default.CheckCircle) {
                    tournament.requirements.forEach { req ->
                        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, null, Modifier.size(18.dp), DarkTheme.Accent)
                            Spacer(Modifier.width(8.dp))
                            Text(req, fontSize = 14.sp, color = DarkTheme.TextPrimary)
                        }
                    }
                }
            }

            // ── SCHEDULE ──
            if (tournament.schedule.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                SectionCard("Расписание", Icons.Default.Schedule) {
                    tournament.schedule.forEach { item ->
                        Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                Modifier.size(36.dp).clip(CircleShape).background(DarkTheme.AccentSoft),
                                Alignment.Center
                            ) {
                                Text(item.time.take(5), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkTheme.Accent)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkTheme.TextPrimary)
                                if (item.description.isNotEmpty()) {
                                    Text(item.description, fontSize = 13.sp, color = DarkTheme.TextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // ── PRIZES ──
            if (tournament.prizes.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                SectionCard("Призы", Icons.Default.EmojiEvents) {
                    tournament.prizes.forEachIndexed { index, prize ->
                        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(28.dp).clip(CircleShape)
                                    .background(if (index < 3) DarkTheme.AccentSoft else DarkTheme.CardBorder.copy(alpha = 0.5f)),
                                Alignment.Center
                            ) {
                                Text("${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    color = if (index < 3) DarkTheme.Accent else DarkTheme.TextMuted)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(prize, fontSize = 14.sp, color = DarkTheme.TextPrimary)
                        }
                    }
                }
            }

            // ── ORGANIZER ──
            if (tournament.organizerName.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                DarkCard {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        SoftIconBox(Icons.Default.Business)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Организатор", fontSize = 12.sp, color = DarkTheme.TextSecondary)
                            Text(tournament.organizerName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                        }
                    }
                }
            }

            // ── BRACKET ──
            if (bracketData.bracket.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                AthleteBracketSection(bracketData, userId)
            }

            Spacer(Modifier.height(20.dp))

            // ── REGISTER BUTTON ──
            if (tournament.status == TournamentStatus.REGISTRATION_OPEN) {
                Button(
                    onClick = onToggleRegistration,
                    Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRegistered) DarkTheme.TextMuted else DarkTheme.Accent
                    )
                ) {
                    Icon(if (isRegistered) Icons.Default.CheckCircle else Icons.Default.PersonAdd, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isRegistered) "Вы зарегистрированы" else "Зарегистрироваться",
                        fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            } else if (tournament.status != TournamentStatus.COMPLETED && tournament.status != TournamentStatus.CANCELLED) {
                Surface(Modifier.fillMaxWidth(), RoundedCornerShape(12.dp), DarkTheme.CardBg) {
                    Box(
                        Modifier.fillMaxWidth().border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp)).padding(16.dp),
                        Alignment.Center
                    ) {
                        Text(
                            if (tournament.status == TournamentStatus.IN_PROGRESS) "Турнир уже начался" else "Регистрация закрыта",
                            fontSize = 15.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Screen-specific composables ──

@Composable
private fun InfoChip(modifier: Modifier = Modifier, icon: ImageVector, text: String) {
    Surface(modifier, RoundedCornerShape(12.dp), DarkTheme.CardBg) {
        Row(
            Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, Modifier.size(16.dp), DarkTheme.Accent)
            Spacer(Modifier.width(6.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DarkTheme.TextPrimary)
        }
    }
}

@Composable
private fun AthleteBracketSection(
    bracketData: com.ileader.app.ui.viewmodels.AthleteTournamentBracketData,
    userId: String
) {
    val matches = BracketUtils.mapDtosToMatches(bracketData.bracket, bracketData.participants)
    val groups = BracketUtils.mapGroupDtos(bracketData.groups)
    var selectedMatch by remember { mutableStateOf<BracketMatch?>(null) }

    SectionCard("Турнирная сетка", Icons.Default.AccountTree) {
        BracketView(
            format = bracketData.format,
            matches = matches,
            groups = groups,
            onMatchClick = { selectedMatch = it },
            highlightParticipantId = userId
        )
    }

    selectedMatch?.let { match ->
        MatchDetailDialog(
            match = match,
            canEdit = false,
            onDismiss = { selectedMatch = null }
        )
    }
}

@Composable
private fun SectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    DarkCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).clip(CircleShape).background(DarkTheme.AccentSoft), Alignment.Center) {
                    Icon(icon, null, Modifier.size(18.dp), DarkTheme.Accent)
                }
                Spacer(Modifier.width(10.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
