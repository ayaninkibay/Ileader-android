package com.ileader.app.ui.screens.sponsor

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.TournamentDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.SponsorTournamentDetailViewModel

@Composable
fun SponsorTournamentDetailScreen(sponsorId: String, tournamentId: String, onBack: () -> Unit) {
    val viewModel: SponsorTournamentDetailViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val sponsorRequestState by viewModel.sponsorRequestState.collectAsState()

    LaunchedEffect(sponsorId, tournamentId) { viewModel.load(sponsorId, tournamentId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(sponsorId, tournamentId) }
        is UiState.Success -> {
            DetailContent(
                detail = s.data.tournament,
                participantCount = s.data.participantCount,
                refereeCount = s.data.refereeCount,
                sponsorshipAmount = s.data.sponsorship?.amount,
                hasSponsorship = s.data.sponsorship != null,
                onBack = onBack,
                onSponsor = { viewModel.requestSponsorship(sponsorId, tournamentId) }
            )
        }
    }
}

@Composable
private fun DetailContent(
    detail: TournamentDto,
    participantCount: Int,
    refereeCount: Int,
    sponsorshipAmount: Double?,
    hasSponsorship: Boolean,
    onBack: () -> Unit,
    onSponsor: () -> Unit
) {
    var isSponsored by remember(hasSponsorship) { mutableStateOf(hasSponsorship) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onBack() }.padding(4.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = DarkTheme.TextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Назад", fontSize = 14.sp, color = DarkTheme.TextSecondary)
                }

                Spacer(Modifier.height(16.dp))

                // ── HEADER CARD ──
                FadeIn(visible, 0) {
                    val chipColor = SponsorUtils.getStatusColor(detail.status ?: "")

                    DarkCardPadded {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text(detail.name, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                color = DarkTheme.TextPrimary, modifier = Modifier.weight(1f), letterSpacing = (-0.3).sp)
                            Spacer(Modifier.width(8.dp))
                            StatusBadge(SponsorUtils.getStatusLabel(detail.status ?: ""), chipColor)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("${detail.sports?.name ?: ""} · ${detail.profiles?.name ?: ""}", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                        Spacer(Modifier.height(10.dp))
                        Text(detail.description ?: "", fontSize = 14.sp, color = DarkTheme.TextSecondary, lineHeight = 20.sp)

                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("${formatShortDate(detail.startDate)} — ${formatShortDate(detail.endDate)}", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("${detail.locations?.city ?: ""}, ${detail.locations?.name ?: ""}", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("$participantCount/${detail.maxParticipants ?: "?"} участников", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                        }

                        Spacer(Modifier.height(16.dp))

                        if (isSponsored) {
                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DarkTheme.AccentSoft) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, null, tint = DarkTheme.Accent, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Вы спонсор", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.Accent)
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { isSponsored = true; onSponsor() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                            ) {
                                Icon(Icons.Default.Handshake, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Стать спонсором", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // ── SPONSORSHIP DETAILS ──
                if (isSponsored && sponsorshipAmount != null) {
                    Spacer(Modifier.height(16.dp))
                    FadeIn(visible, 200) {
                        DarkCardPadded {
                            Text("Детали спонсорства", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                color = DarkTheme.TextPrimary, letterSpacing = (-0.2).sp)
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DetailInfoBox(Modifier.weight(1f), "Сумма", SponsorUtils.formatAmount(sponsorshipAmount.toLong()))
                                DetailInfoBox(Modifier.weight(1f), "Начало", formatShortDate(detail.startDate))
                                DetailInfoBox(Modifier.weight(1f), "Окончание", detail.endDate ?: "")
                            }
                        }
                    }
                }

                // ── TOURNAMENT INFO ──
                Spacer(Modifier.height(16.dp))
                FadeIn(visible, 400) {
                    DarkCardPadded {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AccentIconBox(Icons.Default.EmojiEvents, size = 36.dp, iconSize = 18.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Информация о турнире", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                color = DarkTheme.TextPrimary, letterSpacing = (-0.2).sp)
                        }
                        Spacer(Modifier.height(12.dp))

                        DetailRow("Вид спорта", detail.sports?.name ?: "")
                        DetailRow("Статус", SponsorUtils.getStatusLabel(detail.status ?: ""))
                        DetailRow("Формат", detail.format ?: "")
                        DetailRow("Участники", "$participantCount/${detail.maxParticipants ?: "?"}")
                        DetailRow("Судьи", "$refereeCount")
                        DetailRow("Локация", "${detail.locations?.city ?: ""}, ${detail.locations?.name ?: ""}")
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DetailInfoBox(modifier: Modifier, label: String, value: String) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = DarkTheme.CardBorder.copy(alpha = 0.3f)) {
        Column(
            Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp)).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = DarkTheme.TextMuted)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = DarkTheme.TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
    }
}
