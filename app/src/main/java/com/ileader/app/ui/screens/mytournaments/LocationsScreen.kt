package com.ileader.app.ui.screens.mytournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.remote.dto.LocationDto
import com.ileader.app.data.remote.dto.LocationInsertDto
import com.ileader.app.data.repository.OrganizerRepository
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkFormField
import com.ileader.app.ui.components.DarkTheme
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun LocationsScreen(
    userId: String,
    onBack: () -> Unit
) {
    val repo = remember { OrganizerRepository() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var locations by remember { mutableStateOf<List<LocationDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var editingLocation by remember { mutableStateOf<LocationDto?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }

    fun loadLocations() {
        scope.launch {
            loading = true
            try {
                locations = repo.getMyLocations(userId)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.message ?: "Ошибка загрузки")
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(userId) { loadLocations() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(padding)
        ) {
            BackHeader("Мои локации", onBack)

            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(12.dp))

                // ── Create button ──
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable { showCreateDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = Accent.copy(alpha = 0.1f)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, null, tint = Accent, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Добавить локацию", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Ваши локации (${locations.size})",
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))

                if (loading) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Accent, modifier = Modifier.size(28.dp))
                    }
                } else if (locations.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Локаций нет", fontSize = 13.sp, color = TextMuted)
                    }
                } else {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        locations.forEach { loc ->
                            LocationCard(
                                location = loc,
                                onEdit = { editingLocation = loc },
                                onDelete = { deleteConfirmId = loc.id }
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // ── Create / Edit dialog ──
    if (showCreateDialog || editingLocation != null) {
        LocationFormDialog(
            initial = editingLocation,
            onDismiss = {
                showCreateDialog = false
                editingLocation = null
            },
            onSave = { data ->
                scope.launch {
                    try {
                        if (editingLocation != null) {
                            repo.updateLocation(editingLocation!!.id ?: "", data)
                            snackbarHostState.showSnackbar("Локация обновлена")
                        } else {
                            repo.createLocation(data.copy(ownerId = userId))
                            snackbarHostState.showSnackbar("Локация создана")
                        }
                        showCreateDialog = false
                        editingLocation = null
                        loadLocations()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(e.message ?: "Ошибка")
                    }
                }
            }
        )
    }

    // ── Delete confirmation ──
    deleteConfirmId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            containerColor = CardBg,
            title = { Text("Удалить локацию?", fontWeight = FontWeight.SemiBold, color = TextPrimary) },
            text = { Text("Это действие необратимо.", fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            repo.deleteLocation(id)
                            snackbarHostState.showSnackbar("Локация удалена")
                            loadLocations()
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(e.message ?: "Ошибка")
                        } finally {
                            deleteConfirmId = null
                        }
                    }
                }) { Text("Удалить", color = Color(0xFFEF4444)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) { Text("Отмена", color = TextMuted) }
            }
        )
    }
}

@Composable
private fun LocationCard(
    location: LocationDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBg
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                        .background(Accent.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.LocationOn, null, tint = Accent, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(location.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    location.city?.let {
                        Text(it, fontSize = 12.sp, color = TextMuted)
                    }
                }
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onEdit() },
                    color = Accent.copy(0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Edit, null, tint = Accent, modifier = Modifier.size(20.dp).padding(4.dp))
                }
                Spacer(Modifier.width(6.dp))
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onDelete() },
                    color = Color(0xFFEF4444).copy(0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp).padding(4.dp))
                }
            }
            location.address?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, fontSize = 12.sp, color = TextSecondary)
            }
            location.capacity?.let {
                Spacer(Modifier.height(4.dp))
                Text("Вместимость: $it", fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun LocationFormDialog(
    initial: LocationDto?,
    onDismiss: () -> Unit,
    onSave: (LocationInsertDto) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "venue") }
    var city by remember { mutableStateOf(initial?.city ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var capacity by remember { mutableStateOf(initial?.capacity?.toString() ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text(
                if (initial == null) "Новая локация" else "Редактирование",
                fontWeight = FontWeight.SemiBold, color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DarkFormField(label = "Название *", value = name, onValueChange = { name = it }, placeholder = "Стадион Астана")
                DarkFormField(label = "Город", value = city, onValueChange = { city = it }, placeholder = "Алматы")
                DarkFormField(label = "Адрес", value = address, onValueChange = { address = it }, placeholder = "ул. Абая 1")
                DarkFormField(
                    label = "Вместимость",
                    value = capacity,
                    onValueChange = { capacity = it },
                    placeholder = "1000",
                    keyboardType = KeyboardType.Number
                )
                DarkFormField(label = "Телефон", value = phone, onValueChange = { phone = it }, placeholder = "+7...")
                DarkFormField(
                    label = "Описание",
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Краткое описание",
                    singleLine = false,
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            LocationInsertDto(
                                name = name,
                                type = type.ifBlank { "venue" },
                                city = city.ifBlank { null },
                                address = address.ifBlank { null },
                                capacity = capacity.toIntOrNull(),
                                description = description.ifBlank { null },
                                phone = phone.ifBlank { null },
                                ownerId = "" // will be set by caller
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) { Text("Сохранить", color = Accent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = TextMuted) }
        }
    )
}
