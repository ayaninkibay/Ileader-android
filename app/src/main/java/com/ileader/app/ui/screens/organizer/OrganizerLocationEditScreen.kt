package com.ileader.app.ui.screens.organizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.LocationInsertDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.OrganizerLocationDetailViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun OrganizerLocationEditScreen(
    locationId: String?,
    userId: String,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val vm: OrganizerLocationDetailViewModel = viewModel()
    val state by vm.state.collectAsState()
    val saveState by vm.saveState.collectAsState()

    LaunchedEffect(locationId) { vm.loadForEdit(locationId) }

    // Navigate back on successful save
    LaunchedEffect(saveState) {
        if (saveState is UiState.Success) {
            onSave()
        }
    }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.loadForEdit(locationId) }
        is UiState.Success -> LocationEditContent(
            locationId = locationId,
            userId = userId,
            initialLocation = s.data.location,
            vm = vm,
            saveState = saveState,
            onBack = onBack
        )
    }
}

@Composable
private fun LocationEditContent(
    locationId: String?,
    userId: String,
    initialLocation: com.ileader.app.data.remote.dto.LocationDto,
    vm: OrganizerLocationDetailViewModel,
    saveState: UiState<Boolean>?,
    onBack: () -> Unit
) {
    val isCreate = locationId == null
    val title = if (isCreate) "Добавить локацию" else "Редактирование локации"

    var name by remember { mutableStateOf(initialLocation.name) }
    var type by remember { mutableStateOf(initialLocation.type ?: "karting") }
    var address by remember { mutableStateOf(initialLocation.address ?: "") }
    var city by remember { mutableStateOf(initialLocation.city ?: "") }
    var capacity by remember { mutableStateOf(initialLocation.capacity?.toString() ?: "") }
    var description by remember { mutableStateOf(initialLocation.description ?: "") }
    var phone by remember { mutableStateOf(initialLocation.phone ?: "") }
    var email by remember { mutableStateOf(initialLocation.email ?: "") }
    var website by remember { mutableStateOf(initialLocation.website ?: "") }
    var selectedFacilities by remember {
        mutableStateOf(initialLocation.facilities?.toSet() ?: emptySet())
    }

    val allFacilities = listOf(
        "Парковка", "Кафе", "Трибуны", "Раздевалки", "Медпункт",
        "Wi-Fi", "Боксы", "Прокат", "Пресс-центр", "VIP-ложи",
        "Душевые", "Бассейн", "Оружейная", "Класс"
    )

    val locationTypes = listOf(
        "karting" to "Картодром", "shooting" to "Тир",
        "stadium" to "Стадион", "arena" to "Арена", "other" to "Другое"
    )

    val isSaving = saveState is UiState.Loading

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            BackHeader(title, onBack)

            Spacer(Modifier.height(20.dp))

            // Main info card
            DarkCardPadded {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SoftIconBox(Icons.Default.Info)
                    Spacer(Modifier.width(12.dp))
                    Text("Основная информация", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
                }
                Spacer(Modifier.height(16.dp))

                DarkFormField("Название", name, { name = it }, "Введите название")

                Spacer(Modifier.height(14.dp))

                // Type selector
                Text("Тип локации", fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    locationTypes.forEach { (value, label) ->
                        DarkFilterChip(label, type == value, { type = value })
                    }
                }

                Spacer(Modifier.height(14.dp))

                DarkFormField("Описание", description, { description = it }, "Введите описание", singleLine = false, minLines = 3)
            }

            Spacer(Modifier.height(28.dp))

            // Address card
            DarkCardPadded {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SoftIconBox(Icons.Default.LocationOn)
                    Spacer(Modifier.width(12.dp))
                    Text("Адрес", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
                }
                Spacer(Modifier.height(16.dp))

                DarkFormField("Адрес", address, { address = it }, "Введите адрес")

                Spacer(Modifier.height(14.dp))

                // City selector
                Text("Город", fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                val cities = listOf("Алматы", "Астана", "Шымкент", "Караганда", "Актау", "Атырау")
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cities.take(3).forEach { c ->
                        DarkFilterChip(c, city == c, { city = c })
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cities.drop(3).forEach { c ->
                        DarkFilterChip(c, city == c, { city = c })
                    }
                }

                Spacer(Modifier.height(14.dp))

                DarkFormField("Вместимость", capacity, { capacity = it }, "Введите вместимость")
            }

            Spacer(Modifier.height(28.dp))

            // Facilities card
            DarkCardPadded {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SoftIconBox(Icons.Default.CheckCircle)
                    Spacer(Modifier.width(12.dp))
                    Text("Удобства и оборудование", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
                }
                Spacer(Modifier.height(12.dp))
                allFacilities.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { facility ->
                            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = facility in selectedFacilities,
                                    onCheckedChange = { checked ->
                                        selectedFacilities = if (checked) selectedFacilities + facility
                                        else selectedFacilities - facility
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Accent,
                                        uncheckedColor = TextMuted,
                                        checkmarkColor = Color.White
                                    )
                                )
                                Text(facility, fontSize = 13.sp, color = TextPrimary)
                            }
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Contacts card
            DarkCardPadded {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SoftIconBox(Icons.Default.ContactPhone)
                    Spacer(Modifier.width(12.dp))
                    Text("Контактная информация", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
                }
                Spacer(Modifier.height(16.dp))

                DarkFormField("Телефон", phone, { phone = it }, "+7 (___) ___-__-__")
                Spacer(Modifier.height(14.dp))
                DarkFormField("Email", email, { email = it }, "email@example.com")
                Spacer(Modifier.height(14.dp))
                DarkFormField("Веб-сайт", website, { website = it }, "https://")
            }

            Spacer(Modifier.height(28.dp))

            // Error message
            if (saveState is UiState.Error) {
                Text(
                    saveState.message,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Save button
            Button(
                onClick = {
                    val data = LocationInsertDto(
                        name = name,
                        type = type,
                        address = address.ifEmpty { null },
                        city = city.ifEmpty { null },
                        capacity = capacity.toIntOrNull(),
                        description = description.ifEmpty { null },
                        ownerId = userId,
                        phone = phone.ifEmpty { null },
                        email = email.ifEmpty { null },
                        website = website.ifEmpty { null },
                        facilities = selectedFacilities.toList().ifEmpty { null }
                    )
                    vm.saveLocation(locationId, data)
                },
                enabled = name.isNotBlank() && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (isCreate) "Создать локацию" else "Сохранить изменения",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
