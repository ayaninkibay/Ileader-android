package com.ileader.app.ui.screens.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.LocationReviewDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.LocationDetailViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = DarkTheme.CardBorder

@Composable
fun LocationDetailScreen(
    locationId: String,
    onBack: () -> Unit,
    onWriteReview: () -> Unit
) {
    val vm: LocationDetailViewModel = viewModel()
    val state by vm.state.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    LaunchedEffect(locationId) { vm.load(locationId) }

    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        BackHeader("Локация", onBack)

        when (val s = state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(s.message) { vm.load(locationId) }
            is UiState.Success -> {
                val data = s.data
                val loc = data.location
                Column(
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(4.dp))
                    Text(loc.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    loc.city?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, fontSize = 13.sp, color = TextMuted)
                    }
                    Spacer(Modifier.height(12.dp))

                    // Tabs
                    DarkSegmentedControl(
                        options = listOf("Инфо", "Отзывы (${data.reviews.size})"),
                        selectedIndex = tab,
                        onSelect = { tab = it }
                    )
                    Spacer(Modifier.height(16.dp))

                    if (tab == 0) {
                        DarkCard {
                            Column(Modifier.padding(16.dp)) {
                                if (!loc.description.isNullOrBlank()) {
                                    Text(loc.description, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)
                                    Spacer(Modifier.height(12.dp))
                                }
                                loc.address?.let { InfoRow("Адрес", it) }
                                loc.phone?.let { InfoRow("Телефон", it) }
                                loc.email?.let { InfoRow("Email", it) }
                                loc.capacity?.let { InfoRow("Вместимость", "$it") }
                                loc.rating?.let {
                                    InfoRow("Рейтинг", String.format("%.1f / 5", it))
                                }
                            }
                        }
                    } else {
                        // Write review button
                        Surface(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onWriteReview() },
                            shape = RoundedCornerShape(12.dp),
                            color = Accent.copy(0.1f)
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Star, null, tint = Accent, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Оставить отзыв", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        if (data.reviews.isEmpty()) {
                            EmptyState(
                                icon = Icons.Default.Star,
                                title = "Пока нет отзывов",
                                subtitle = "Будьте первым"
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                data.reviews.forEach { ReviewCard(it) }
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, fontSize = 11.sp, color = TextMuted)
        Text(value, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(thickness = 0.5.dp, color = Border.copy(0.15f))
}

@Composable
private fun ReviewCard(review: LocationReviewDto) {
    DarkCard {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(Accent.copy(0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (review.profiles?.name ?: "?").take(1).uppercase(),
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Accent
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        review.profiles?.name ?: "Пользователь",
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            String.format("%.1f", review.overall),
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary
                        )
                    }
                }
            }
            if (!review.comment.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(review.comment, fontSize = 13.sp, color = TextSecondary, lineHeight = 19.sp)
            }
        }
    }
}
