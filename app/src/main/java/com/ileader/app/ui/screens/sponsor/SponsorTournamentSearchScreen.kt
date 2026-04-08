package com.ileader.app.ui.screens.sponsor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.SponsorRepository
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

private val TIERS = listOf("title", "gold", "silver", "bronze", "media")

@Composable
fun SponsorTournamentSearchScreen(
    user: User,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit,
    onCreated: () -> Unit = {}
) {
    val colors = LocalAppColors.current
    val ctx = LocalContext.current
    val repo = remember { SponsorRepository() }
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var items by remember { mutableStateOf<List<TournamentWithCountsDto>>(emptyList()) }
    var sponsoredIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var error by remember { mutableStateOf<String?>(null) }

    var sheetTournament by remember { mutableStateOf<TournamentWithCountsDto?>(null) }

    fun reload() {
        scope.launch {
            loading = true
            error = null
            try {
                items = repo.searchTournaments(query)
                sponsoredIds = repo.getSponsoredTournamentIds(user.id)
            } catch (e: Exception) {
                error = e.message ?: "Ошибка"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { reload() }
    LaunchedEffect(query) {
        kotlinx.coroutines.delay(300)
        reload()
    }

    Column(
        Modifier.fillMaxSize().background(colors.bg).statusBarsPadding().padding(horizontal = 16.dp)
    ) {
        BackHeader(title = "Поиск турниров", onBack = onBack)
        Spacer(Modifier.height(4.dp))
        DarkSearchField(value = query, onValueChange = { query = it }, placeholder = "Название турнира...")
        Spacer(Modifier.height(12.dp))

        when {
            loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { LoadingScreen() }
            error != null -> ErrorScreen(error!!) { reload() }
            items.isEmpty() -> EmptyState(
                icon = Icons.Default.SearchOff,
                title = "Турниры не найдены",
                subtitle = "Попробуйте изменить запрос"
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.id }) { t ->
                    TournamentRow(
                        tournament = t,
                        alreadySponsored = t.id in sponsoredIds,
                        onClick = { onTournamentClick(t.id) },
                        onSponsor = { sheetTournament = t }
                    )
                }
            }
        }
    }

    sheetTournament?.let { t ->
        SponsorshipDialog(
            tournamentName = t.name,
            onDismiss = { sheetTournament = null },
            onConfirm = { tier, amount ->
                scope.launch {
                    try {
                        repo.createSponsorship(user.id, t.id, tier, amount)
                        sheetTournament = null
                        Toast.makeText(ctx, "Заявка отправлена", Toast.LENGTH_SHORT).show()
                        reload()
                        onCreated()
                    } catch (e: Exception) {
                        Toast.makeText(ctx, e.message ?: "Ошибка", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
private fun TournamentRow(
    tournament: TournamentWithCountsDto,
    alreadySponsored: Boolean,
    onClick: () -> Unit,
    onSponsor: () -> Unit
) {
    val colors = LocalAppColors.current
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
                        .background(colors.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.EmojiEvents, null, tint = colors.accent, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        tournament.name,
                        fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        listOfNotNull(tournament.sportName, tournament.locationName, tournament.startDate?.take(10))
                            .joinToString(" • "),
                        fontSize = 12.sp, color = colors.textMuted,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(text = "${tournament.participantCount} участников", color = colors.textSecondary, icon = Icons.Default.People)
                Spacer(Modifier.weight(1f))
                if (alreadySponsored) {
                    StatusBadge(text = "Спонсор", color = Color(0xFF22C55E), icon = Icons.Default.Check)
                } else {
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable { onSponsor() },
                        shape = RoundedCornerShape(20.dp),
                        color = colors.accent
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WorkspacePremium, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Спонсировать", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SponsorshipDialog(
    tournamentName: String,
    onDismiss: () -> Unit,
    onConfirm: (tier: String, amount: Double) -> Unit
) {
    val colors = LocalAppColors.current
    var tier by remember { mutableStateOf("gold") }
    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.cardBg,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Стать спонсором", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(tournamentName, fontSize = 13.sp, color = colors.textMuted)
                Spacer(Modifier.height(16.dp))

                Text("Уровень", fontSize = 13.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TIERS) { t ->
                        DarkFilterChip(
                            text = tierDisplay(t),
                            selected = tier == t,
                            onClick = { tier = t }
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                DarkFormField(
                    label = "Сумма (₸)",
                    value = amount,
                    onValueChange = { amount = it.filter { ch -> ch.isDigit() }; amountError = null },
                    placeholder = "100000",
                    keyboardType = KeyboardType.Number,
                    error = amountError
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton(text = "Отмена", onClick = onDismiss, modifier = Modifier.weight(1f))
                    GradientButton(
                        text = "Отправить",
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                amountError = "Введите сумму"
                            } else {
                                onConfirm(tier, amt)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
