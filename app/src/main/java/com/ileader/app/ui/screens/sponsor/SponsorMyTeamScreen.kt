package com.ileader.app.ui.screens.sponsor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.ui.screens.sponsor.SponsorUtils
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SponsorshipDto
import com.ileader.app.data.remote.dto.TeamMemberDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.SponsorMyTeamViewModel

@Composable
fun SponsorMyTeamScreen(sponsorId: String, teamId: String, onBack: () -> Unit) {
    val viewModel: SponsorMyTeamViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(sponsorId, teamId) { viewModel.load(sponsorId, teamId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(sponsorId, teamId) }
        is UiState.Success -> MyTeamContent(s.data.sponsorship, s.data.members, onBack)
    }
}

@Composable
private fun MyTeamContent(sponsorship: SponsorshipDto, members: List<TeamMemberDto>, onBack: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onBack() }.padding(4.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = DarkTheme.TextSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("Назад", fontSize = 14.sp, color = DarkTheme.TextSecondary)
            }

            Spacer(Modifier.height(16.dp))

            // ── TEAM HEADER ──
            FadeIn(visible, 0) {
                DarkCardPadded(padding = 20.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AccentIconBox(Icons.Default.Groups, size = 56.dp, iconSize = 28.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(sponsorship.teams?.name ?: "", fontSize = 18.sp,
                                fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
                            Spacer(Modifier.height(4.dp))
                            Text(sponsorship.tournaments?.sports?.name ?: "", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DarkTheme.AccentSoft) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachMoney, null, tint = DarkTheme.Accent, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Сумма спонсорства", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                            }
                            Text(SponsorUtils.formatAmount((sponsorship.amount ?: 0.0).toLong()),
                                fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTheme.Accent)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("${formatShortDate(sponsorship.startDate)} — ${if (sponsorship.endDate.isNullOrEmpty()) "Бессрочно" else formatShortDate(sponsorship.endDate)}",
                            fontSize = 13.sp, color = DarkTheme.TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── TEAM STATS ──
            FadeIn(visible, 200) {
                Text("Статистика команды", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TeamStatItem(Modifier.weight(1f), Icons.Default.People, "${members.size}", "Участников")
                    TeamStatItem(Modifier.weight(1f), Icons.Default.EmojiEvents, "0", "Побед")
                    TeamStatItem(Modifier.weight(1f), Icons.Default.Star, "0", "Рейтинг")
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── MEMBERS LIST ──
            FadeIn(visible, 400) {
                Text("Участники команды", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
                Spacer(Modifier.height(12.dp))
                if (members.isEmpty()) {
                    EmptyState("Нет участников")
                } else {
                    members.forEach { member ->
                        MemberCard(member)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TeamStatItem(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = DarkTheme.CardBg) {
        Column(
            Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp)).padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SoftIconBox(icon, size = 36.dp, iconSize = 18.dp)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
            Text(label, fontSize = 11.sp, color = DarkTheme.TextMuted)
        }
    }
}

@Composable
private fun MemberCard(member: TeamMemberDto) {
    DarkCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(
                avatarUrl = member.profiles?.avatarUrl,
                displayName = member.profiles?.name ?: "?",
                size = 44.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(member.profiles?.name ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                Spacer(Modifier.height(3.dp))
                Text(member.role ?: "Участник", fontSize = 12.sp, color = DarkTheme.TextSecondary)
            }
        }
    }
}
