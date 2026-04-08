package com.ileader.app.ui.screens.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.LocationReviewFormViewModel
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

// 11 criteria as per location_reviews schema
private val CRITERIA = listOf(
    "cleanliness" to "Чистота",
    "equipment" to "Оборудование",
    "staff" to "Персонал",
    "accessibility" to "Доступность",
    "parking" to "Парковка",
    "facilities" to "Удобства",
    "safety" to "Безопасность",
    "comfort" to "Комфорт",
    "lighting" to "Освещение",
    "ventilation" to "Вентиляция",
    "value" to "Цена/качество"
)

@Composable
fun LocationReviewFormScreen(
    locationId: String,
    userId: String,
    onBack: () -> Unit,
    onSubmitted: () -> Unit
) {
    val vm: LocationReviewFormViewModel = viewModel()
    val submit by vm.submit.collectAsState()

    var overall by remember { mutableIntStateOf(5) }
    val criteriaRatings = remember { mutableStateMapOf<String, Int>().apply {
        CRITERIA.forEach { put(it.first, 5) }
    } }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(submit) {
        if (submit is UiState.Success) {
            vm.reset()
            onSubmitted()
        }
    }

    Column(
        Modifier.fillMaxSize().background(Bg).statusBarsPadding()
    ) {
        BackHeader("Новый отзыв", onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Overall rating
            DarkCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Общая оценка", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(10.dp))
                    StarRow(value = overall, onValueChange = { overall = it })
                    Spacer(Modifier.height(4.dp))
                    Text("$overall / 5", fontSize = 13.sp, color = TextMuted)
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader(title = "Критерии")
            Spacer(Modifier.height(10.dp))

            DarkCard {
                Column(Modifier.padding(16.dp)) {
                    CRITERIA.forEachIndexed { index, (key, label) ->
                        if (index > 0) Spacer(Modifier.height(14.dp))
                        Column {
                            Text(label, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(6.dp))
                            StarRow(
                                value = criteriaRatings[key] ?: 5,
                                onValueChange = { criteriaRatings[key] = it }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            DarkFormField(
                label = "Комментарий",
                value = comment,
                onValueChange = { comment = it },
                placeholder = "Поделитесь впечатлениями...",
                singleLine = false,
                minLines = 3
            )

            Spacer(Modifier.height(20.dp))

            val isSubmitting = submit is UiState.Loading
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSubmitting) {
                        val criteriaJson = JsonObject(
                            criteriaRatings.mapValues { JsonPrimitive(it.value) }
                        )
                        vm.submit(
                            locationId = locationId,
                            userId = userId,
                            overall = overall.toDouble(),
                            criteria = criteriaJson,
                            comment = comment.ifBlank { null }
                        )
                    },
                shape = RoundedCornerShape(12.dp),
                color = if (isSubmitting) Accent.copy(0.5f) else Accent
            ) {
                Text(
                    if (isSubmitting) "Отправка..." else "Отправить отзыв",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            (submit as? UiState.Error)?.let {
                Spacer(Modifier.height(8.dp))
                Text(it.message, fontSize = 13.sp, color = Accent)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StarRow(value: Int, onValueChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        (1..5).forEach { star ->
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = if (star <= value) Color(0xFFFBBF24) else TextMuted.copy(0.3f),
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onValueChange(star) }
            )
        }
    }
}
