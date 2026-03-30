package com.ileader.app.ui.screens.sport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkFilterChip
import com.ileader.app.ui.components.DarkFormField
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.FormDropdown
import com.ileader.app.ui.viewmodels.SportViewModel.SportFilterState
import com.ileader.app.ui.viewmodels.SportViewModel.SportSubTab

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val Accent: Color @Composable get() = DarkTheme.Accent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterPopupScreen(
    activeTab: SportSubTab,
    sports: List<SportDto>,
    filters: SportFilterState,
    onApply: (SportFilterState) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var localFilters by remember { mutableStateOf(filters) }

    val sportItems = listOf("" to "Все виды спорта") + sports.map { it.id to it.name }
    val selectedSportName = sports.find { it.id == localFilters.sportId }?.name ?: ""

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg)
        ) {
            BackHeader(title = "Фильтры", onBack = onDismiss)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // ── Sport filter (common for all tabs) ──
                FilterSectionTitle("Вид спорта")
                FormDropdown(
                    label = "",
                    selectedValue = selectedSportName,
                    placeholder = "Все виды спорта",
                    items = sportItems,
                    onItemSelected = { id ->
                        localFilters = localFilters.copy(sportId = id.ifEmpty { null })
                    }
                )

                Spacer(Modifier.height(16.dp))

                // ── Tab-specific filters ──
                when (activeTab) {
                    SportSubTab.TOURNAMENTS -> TournamentFilters(
                        filters = localFilters,
                        onUpdate = { localFilters = it }
                    )
                    SportSubTab.PEOPLE -> PeopleFilters(
                        filters = localFilters,
                        onUpdate = { localFilters = it }
                    )
                    SportSubTab.NEWS -> NewsFilters(
                        filters = localFilters,
                        onUpdate = { localFilters = it }
                    )
                }
            }

            // ── Bottom buttons ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Сбросить", color = TextPrimary)
                }
                Button(
                    onClick = { onApply(localFilters) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text("Применить", color = Color.White)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// Tournament Filters
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TournamentFilters(
    filters: SportFilterState,
    onUpdate: (SportFilterState) -> Unit
) {
    // Status
    FilterSectionTitle("Статус")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            "registration_open" to "Регистрация",
            "in_progress" to "Идёт",
            "completed" to "Завершён"
        ).forEach { (value, label) ->
            DarkFilterChip(
                text = label,
                selected = filters.status == value,
                onClick = {
                    onUpdate(filters.copy(status = if (filters.status == value) null else value))
                }
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    // City
    FilterSectionTitle("Город")
    DarkFormField(
        label = "",
        value = filters.city ?: "",
        onValueChange = { onUpdate(filters.copy(city = it.ifEmpty { null })) },
        placeholder = "Введите город"
    )

    Spacer(Modifier.height(16.dp))

    // Age category
    FilterSectionTitle("Возрастная категория")
    val ageItems = listOf(
        "" to "Все",
        "children" to "Дети (6-12)",
        "youth" to "Юноши (12-17)",
        "adult" to "Взрослые (18+)"
    )
    val selectedAgeName = ageItems.find { it.first == filters.ageCategory }?.second ?: ""
    FormDropdown(
        label = "",
        selectedValue = selectedAgeName,
        placeholder = "Все",
        items = ageItems,
        onItemSelected = { onUpdate(filters.copy(ageCategory = it.ifEmpty { null })) }
    )
}

// ═══════════════════════════════════════════════════
// People Filters
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeopleFilters(
    filters: SportFilterState,
    onUpdate: (SportFilterState) -> Unit
) {
    // City
    FilterSectionTitle("Город")
    DarkFormField(
        label = "",
        value = filters.city ?: "",
        onValueChange = { onUpdate(filters.copy(city = it.ifEmpty { null })) },
        placeholder = "Введите город"
    )

    Spacer(Modifier.height(16.dp))

    // Role
    FilterSectionTitle("Роль")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            "athlete" to "Спортсмены",
            "trainer" to "Тренеры",
            "referee" to "Судьи"
        ).forEach { (value, label) ->
            DarkFilterChip(
                text = label,
                selected = filters.role == value,
                onClick = {
                    onUpdate(filters.copy(role = if (filters.role == value) null else value))
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════
// News Filters
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewsFilters(
    filters: SportFilterState,
    onUpdate: (SportFilterState) -> Unit
) {
    FilterSectionTitle("Категория")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            "news" to "Новости",
            "report" to "Отчёт",
            "interview" to "Интервью",
            "analytics" to "Аналитика",
            "review" to "Обзор"
        ).forEach { (value, label) ->
            DarkFilterChip(
                text = label,
                selected = filters.category == value,
                onClick = {
                    onUpdate(filters.copy(category = if (filters.category == value) null else value))
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════
// Shared
// ═══════════════════════════════════════════════════

@Composable
private fun FilterSectionTitle(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
