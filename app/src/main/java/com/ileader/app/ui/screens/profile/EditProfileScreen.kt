package com.ileader.app.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ProfileUpdateDto
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkFormField
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.ErrorScreen
import com.ileader.app.ui.components.LoadingScreen
import com.ileader.app.ui.viewmodels.AvatarViewModel
import com.ileader.app.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.launch

// ── Palette aliases ──
private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun EditProfileScreen(
    user: User,
    onBack: () -> Unit
) {
    val vm: ProfileViewModel = viewModel()
    val avatarVm: AvatarViewModel = viewModel()
    val profileState by vm.profile.collectAsState()
    val saveState by vm.saveState.collectAsState()
    val isUploading by avatarVm.isUploading.collectAsState()
    val uploadedUrl by avatarVm.uploadedUrl.collectAsState()
    val avatarError by avatarVm.error.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(user.id) {
        vm.load(user.id)
    }

    // Initialize form from loaded profile
    LaunchedEffect(profileState) {
        val state = profileState
        if (state is UiState.Success && !initialized) {
            val p = state.data
            name = p.name ?: ""
            nickname = p.nickname ?: ""
            phone = p.phone ?: ""
            city = p.city ?: ""
            country = p.country ?: ""
            bio = p.bio ?: ""
            avatarUrl = p.avatarUrl
            initialized = true
        }
    }

    // Handle avatar upload result
    LaunchedEffect(uploadedUrl) {
        if (uploadedUrl != null) {
            avatarUrl = uploadedUrl
            avatarVm.clearUploadedUrl()
        }
    }

    // Handle save result
    LaunchedEffect(saveState) {
        when (saveState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Профиль обновлён")
                vm.clearSaveState()
                onBack()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((saveState as? UiState.Error)?.message ?: "Ошибка сохранения")
                vm.clearSaveState()
            }
            else -> {}
        }
    }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                avatarVm.uploadAvatar(user.id, bytes)
            }
        }
    }

    val isSaving = saveState is UiState.Loading

    // Check if anything changed
    val hasChanges = remember(name, nickname, phone, city, country, bio, avatarUrl, profileState) {
        val state = profileState
        if (state is UiState.Success) {
            val p = state.data
            name != (p.name ?: "") ||
                nickname != (p.nickname ?: "") ||
                phone != (p.phone ?: "") ||
                city != (p.city ?: "") ||
                country != (p.country ?: "") ||
                bio != (p.bio ?: "") ||
                avatarUrl != p.avatarUrl
        } else false
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BackHeader("Редактирование профиля", onBack)

            when (profileState) {
                is UiState.Loading -> LoadingScreen()
                is UiState.Error -> ErrorScreen(
                    message = (profileState as? UiState.Error)?.message ?: "Ошибка загрузки",
                    onRetry = { vm.load(user.id) }
                )
                is UiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(20.dp))

                        // ── Avatar section ──
                        Box(contentAlignment = Alignment.Center) {
                            if (avatarUrl != null) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Аватар",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(Accent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name.take(2).uppercase().ifEmpty { "?" },
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            if (isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(100.dp),
                                    color = Accent,
                                    strokeWidth = 3.dp
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Изменить фото",
                            fontSize = 14.sp,
                            color = Accent,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                imagePicker.launch("image/*")
                            }
                        )

                        Spacer(Modifier.height(24.dp))

                        // ── Form fields ──
                        DarkFormField(
                            label = "Имя",
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Ваше имя"
                        )
                        Spacer(Modifier.height(12.dp))

                        DarkFormField(
                            label = "Никнейм",
                            value = nickname,
                            onValueChange = { nickname = it },
                            placeholder = "Ваш никнейм"
                        )
                        Spacer(Modifier.height(12.dp))

                        DarkFormField(
                            label = "Телефон",
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = "+7 ...",
                            keyboardType = KeyboardType.Phone
                        )
                        Spacer(Modifier.height(12.dp))

                        DarkFormField(
                            label = "Город",
                            value = city,
                            onValueChange = { city = it },
                            placeholder = "Ваш город"
                        )
                        Spacer(Modifier.height(12.dp))

                        DarkFormField(
                            label = "Страна",
                            value = country,
                            onValueChange = { country = it },
                            placeholder = "Ваша страна"
                        )
                        Spacer(Modifier.height(12.dp))

                        DarkFormField(
                            label = "О себе",
                            value = bio,
                            onValueChange = { bio = it },
                            placeholder = "Расскажите о себе",
                            singleLine = false,
                            minLines = 3
                        )

                        Spacer(Modifier.height(24.dp))

                        // ── Save button ──
                        val enabled = hasChanges && !isSaving
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (enabled) Accent else TextMuted.copy(alpha = 0.3f))
                                .clickable(enabled = enabled) {
                                    vm.updateProfile(
                                        user.id,
                                        ProfileUpdateDto(
                                            name = name.ifBlank { null },
                                            nickname = nickname.ifBlank { null },
                                            phone = phone.ifBlank { null },
                                            city = city.ifBlank { null },
                                            country = country.ifBlank { null },
                                            bio = bio.ifBlank { null },
                                            avatarUrl = avatarUrl
                                        )
                                    )
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Сохранить",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}
