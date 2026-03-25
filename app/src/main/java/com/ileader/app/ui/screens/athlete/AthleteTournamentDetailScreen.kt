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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ileader.app.data.bracket.BracketUtils
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.components.bracket.BracketView
import com.ileader.app.ui.components.bracket.MatchDetailDialog
import com.ileader.app.ui.components.tournamentImageUrl
import com.ileader.app.ui.components.LocalSnackbarHost
import com.ileader.app.ui.viewmodels.AthleteTournamentsViewModel
@Composable
fun AthleteTournamentDetailScreen(
    tournamentId: String,
    user: User,
    viewModel: AthleteTournamentsViewModel,
    onBack: () -> Unit,
    onShowQrTicket: (tournamentName: String, isCheckedIn: Boolean) -> Unit = { _, _ -> }
) {
    val detailState by viewModel.detailState.collectAsState()
    val isRegistered by viewModel.isRegistered.collectAsState()

    val snackbarHost = LocalSnackbarHost.current
    val snackbarEvent by viewModel.snackbarEvent.collectAsState()
    LaunchedEffect(snackbarEvent) {
        snackbarEvent?.let { msg ->
            snackbarHost.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    LaunchedEffect(tournamentId) { viewModel.loadDetail(tournamentId, user.id) }

    val bracketData by viewModel.bracketData.collectAsState()

    when (val s = detailState) {
        is UiState.Loading -> LoadingScreen(LoadingVariant.DETAIL)
        is UiState.Error -> ErrorScreen(s.message) { viewModel.loadDetail(tournamentId, user.id) }
        is UiState.Success -> DetailContent(
            tournament = s.data,
            isRegistered = isRegistered,
            onToggleRegistration = { viewModel.toggleRegistration(tournamentId, user.id) },
            onBack = onBack,
            bracketData = bracketData,
            userId = user.id,
            onShowQrTicket = onShowQrTicket
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
    userId: String = "",
    onShowQrTicket: (String, Boolean) -> Unit = { _, _ -> }
) {
    val bgColor = DarkTheme.Bg

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── PHOTO HEADER ──
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(DarkTheme.CardBg)
            ) {
                val headerImageUrl = tournamentImageUrl(tournament.sportName, tournament.imageUrl)
                if (headerImageUrl != null) {
                    AsyncImage(
                        model = headerImageUrl,
                        contentDescription = tournament.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(DarkTheme.CardBg))
                }
                // Dark gradient overlay
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
                // Back button (top-left)
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = DarkTheme.CardBg.copy(alpha = 0.7f),
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 16.dp, top = 8.dp)
                        .size(40.dp)
                        .align(Alignment.TopStart)
                        .border(0.5.dp, DarkTheme.CardBorder, CircleShape)
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", Modifier.size(20.dp), Color.White)
                    }
                }
                // Title + sport name (bottom-left)
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 16.dp, end = 80.dp)
                ) {
                    Text(
                        tournament.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(tournament.sportName, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                }
                // Status badge (bottom-right)
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 16.dp)
                ) {
                    TournamentStatusBadge(tournament.status)
                }
            }

            // ── Content with horizontal padding ──
            Column(Modifier.padding(horizontal = 20.dp)) {

            Spacer(Modifier.height(16.dp))

            // ── INFO CHIPS ──
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), Arrangement.spacedBy(10.dp)) {
                InfoChip(Modifier.weight(1f).fillMaxHeight(), Icons.Default.CalendarMonth, formatShortDate(tournament.startDate))
                InfoChip(Modifier.weight(1f).fillMaxHeight(), Icons.Default.Groups, "${tournament.currentParticipants}/${tournament.maxParticipants}")
                if (tournament.format.isNotEmpty()) {
                    InfoChip(Modifier.weight(1f).fillMaxHeight(), Icons.Default.AccountTree, tournament.format)
                }
            }

            // ── ДНЕЙ ДО НАЧАЛА ──
            run {
                Spacer(Modifier.height(16.dp))
                val daysLeft = try {
                    val clean = tournament.startDate.substringBefore("+").substringBefore("Z")
                    val startDate = if (clean.contains("T"))
                        java.time.LocalDateTime.parse(clean).toLocalDate()
                    else
                        java.time.LocalDate.parse(clean)
                    java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), startDate)
                } catch (_: Exception) { null }

                if (daysLeft != null) {
                    DarkCardPadded {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, null, Modifier.size(18.dp), DarkTheme.TextSecondary)
                                Spacer(Modifier.width(8.dp))
                                Text("До начала", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = DarkTheme.TextPrimary)
                            }
                            Text(
                                when {
                                    daysLeft > 0 -> "$daysLeft дн."
                                    daysLeft == 0L -> "Сегодня"
                                    else -> "Завершён"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    daysLeft > 3 -> DarkTheme.TextPrimary
                                    daysLeft in 1..3 -> DarkTheme.Accent
                                    daysLeft == 0L -> DarkTheme.Accent
                                    else -> DarkTheme.TextMuted
                                }
                            )
                        }
                    }
                }
            }

            // ── УЧАСТНИКИ ──
            if (bracketData.participants.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                SectionCard("Участники (${bracketData.participants.size})", Icons.Default.Groups) {
                    bracketData.participants.take(10).forEach { p ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                avatarUrl = p.profiles?.avatarUrl,
                                displayName = p.profiles?.name ?: "Участник",
                                size = 32.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                p.profiles?.name ?: "Участник",
                                fontSize = 14.sp,
                                color = DarkTheme.TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (bracketData.participants.size > 10) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "и ещё ${bracketData.participants.size - 10} участников",
                            fontSize = 13.sp,
                            color = DarkTheme.TextMuted
                        )
                    }
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
            run {
                val canRegister = tournament.status == TournamentStatus.REGISTRATION_OPEN

                if (canRegister) {
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
                    if (isRegistered) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onShowQrTicket(tournament.name, false) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.TextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkTheme.CardBorder)
                        ) {
                            Icon(Icons.Default.QrCode2, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Мой QR-билет", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else if (tournament.status == TournamentStatus.CHECK_IN && isRegistered) {
                    Button(
                        onClick = { onShowQrTicket(tournament.name, false) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                    ) {
                        Icon(Icons.Default.QrCode2, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Показать QR-билет", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(12.dp), DarkTheme.CardBg) {
                        Box(
                            Modifier.fillMaxWidth().padding(16.dp),
                            Alignment.Center
                        ) {
                            Text(
                                when (tournament.status) {
                                    TournamentStatus.COMPLETED -> "Турнир завершён"
                                    TournamentStatus.CANCELLED -> "Турнир отменён"
                                    TournamentStatus.IN_PROGRESS -> "Турнир уже начался"
                                    else -> "Регистрация закрыта"
                                },
                                fontSize = 15.sp, color = DarkTheme.TextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            } // end inner Column (horizontal padding)
        }
    }
}

// ── Screen-specific composables ──

@Composable
private fun InfoChip(modifier: Modifier = Modifier, icon: ImageVector, text: String) {
    Surface(modifier, RoundedCornerShape(12.dp), DarkTheme.CardBg) {
        Row(
            Modifier
                .fillMaxSize()
                .border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, Modifier.size(14.dp), DarkTheme.Accent)
            Spacer(Modifier.width(4.dp))
            Text(
                text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = DarkTheme.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
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
