package com.ileader.app.ui.screens.sponsor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.dto.SponsorStats
import com.ileader.app.data.remote.dto.TournamentSponsorshipDto
import com.ileader.app.data.repository.SponsorRepository
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

@Composable
fun SponsorshipsScreen(
    user: User,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val repo = remember { SponsorRepository() }
    val scope = rememberCoroutineScope()

    var stats by remember { mutableStateOf<SponsorStats?>(null) }
    var items by remember { mutableStateOf<List<TournamentSponsorshipDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun reload() {
        scope.launch {
            loading = true
            error = null
            try {
                items = repo.getMySponsorships(user.id)
                stats = repo.getStats(user.id)
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { reload() }

    Column(
        Modifier.fillMaxSize().background(colors.bg).statusBarsPadding().padding(horizontal = 16.dp)
    ) {
        BackHeader(title = "Мои спонсорства", onBack = onBack) {
            Box(
                Modifier
                    .size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(colors.accent)
                    .clickable { onSearchClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { LoadingScreen() }
            error != null -> ErrorScreen(error!!) { reload() }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                stats?.let { s ->
                    item {
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MiniStat("Всего", "${s.totalSponsored}", Modifier.weight(1f))
                            MiniStat("Активных", "${s.activeSponsorships}", Modifier.weight(1f))
                            MiniStat("Сумма", "${s.totalAmount.toLong()} ₸", Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (items.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Diamond,
                            title = "Нет спонсорств",
                            subtitle = "Найдите турнир и станьте спонсором",
                            actionLabel = "Найти турнир",
                            onAction = onSearchClick
                        )
                    }
                } else {
                    items(items, key = { it.tournamentId }) { item ->
                        SponsorshipCard(
                            item = item,
                            onClick = { onTournamentClick(item.tournamentId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SponsorshipCard(
    item: TournamentSponsorshipDto,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val t = item.tournaments
    val tierLabel = tierDisplay(item.tier)
    val tierColor = tierColor(item.tier)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = colors.cardBg
    ) {
        Column(
            Modifier
                .border(0.5.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(tierColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.WorkspacePremium, null, tint = tierColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        t?.name ?: "Турнир",
                        fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        listOfNotNull(t?.sports?.name, t?.locations?.city).joinToString(" • "),
                        fontSize = 12.sp, color = colors.textMuted,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                    tint = colors.textMuted, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(text = tierLabel, color = tierColor, icon = Icons.Default.Star)
                Spacer(Modifier.width(8.dp))
                StatusBadge(text = "${(item.amount ?: 0.0).toLong()} ₸", color = colors.textSecondary, icon = Icons.Default.Payments)
            }
        }
    }
}

internal fun tierDisplay(tier: String?): String = when (tier) {
    "title" -> "Титульный"
    "gold" -> "Золото"
    "silver" -> "Серебро"
    "bronze" -> "Бронза"
    "media" -> "Медиа"
    else -> tier ?: "—"
}

internal fun tierColor(tier: String?): Color = when (tier) {
    "title" -> Color(0xFFE53535)
    "gold" -> Color(0xFFF59E0B)
    "silver" -> Color(0xFF9CA3AF)
    "bronze" -> Color(0xFFB45309)
    "media" -> Color(0xFF3B82F6)
    else -> Color(0xFF6B7280)
}
