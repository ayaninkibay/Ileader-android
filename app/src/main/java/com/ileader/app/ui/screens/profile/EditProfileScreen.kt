package com.ileader.app.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ProfileUpdateDto
import com.ileader.app.ui.components.DarkFormField
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.ErrorScreen
import com.ileader.app.ui.components.LoadingScreen
import com.ileader.app.ui.components.sportImageUrl
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.AvatarViewModel
import com.ileader.app.ui.viewmodels.ProfileViewModel

// ── Palette aliases ──
private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

private const val BIO_MAX_LENGTH = 300

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
    val userSports by vm.userSports.collectAsState()

    val context = LocalContext.current
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
    var nameError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user.id) {
        vm.load(user.id, user.role)
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

    val primarySportName = remember(userSports) {
        userSports.firstOrNull()?.sports?.name ?: "картинг"
    }
    val bannerUrl = remember(primarySportName) {
        sportImageUrl(primarySportName) ?: "https://ileader.kz/img/karting/karting-15-1280x853.jpeg"
    }

    fun doSave() {
        // Validate
        if (name.isBlank()) {
            nameError = "Имя не может быть пустым"
            return
        }
        nameError = null
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (profileState) {
                is UiState.Loading -> LoadingScreen()
                is UiState.Error -> ErrorScreen(
                    message = (profileState as? UiState.Error)?.message ?: "Ошибка загрузки",
                    onRetry = { vm.load(user.id, user.role) }
                )
                is UiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ══════════════════════════════════════
                        // HERO BANNER + AVATAR
                        // ══════════════════════════════════════
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Sport background
                            AsyncImage(
                                model = bannerUrl, contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Black.copy(0.3f), Color.Black.copy(0.75f))
                                        )
                                    )
                            )

                            // Back button
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .padding(8.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(0.3f))
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack, "Назад",
                                    tint = Color.White, modifier = Modifier.size(20.dp)
                                )
                            }

                            // Avatar + name + role
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(top = 50.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Avatar with camera button
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    Box(
                                        modifier = Modifier
                                            .size(110.dp)
                                            .clip(CircleShape)
                                            .border(3.dp, Color.White.copy(0.3f), CircleShape)
                                            .clickable { imagePicker.launch("image/*") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (avatarUrl != null) {
                                            AsyncImage(
                                                model = avatarUrl,
                                                contentDescription = "Аватар",
                                                modifier = Modifier
                                                    .size(110.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(110.dp)
                                                    .clip(CircleShape)
                                                    .background(Accent),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = name.take(2).uppercase().ifEmpty { "?" },
                                                    fontSize = 36.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        if (isUploading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(110.dp),
                                                color = Accent,
                                                strokeWidth = 3.dp
                                            )
                                        }
                                    }
                                    // Camera icon
                                    Box(
                                        modifier = Modifier
                                            .offset(x = (-4).dp, y = (-4).dp)
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Accent)
                                            .clickable { imagePicker.launch("image/*") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.CameraAlt, null,
                                            tint = Color.White, modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))
                                Text(
                                    name.ifEmpty { user.name },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(0.2f)
                                ) {
                                    Text(
                                        user.role.displayName,
                                        Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ══════════════════════════════════════
                        // SECTION: Личные данные
                        // ══════════════════════════════════════
                        FormSection(
                            title = "Личные данные",
                            icon = Icons.Outlined.Person
                        ) {
                            IconFormField(
                                icon = Icons.Outlined.Person,
                                label = "Имя",
                                value = name,
                                onValueChange = {
                                    name = it
                                    if (it.isNotBlank()) nameError = null
                                },
                                placeholder = "Ваше имя",
                                error = nameError
                            )
                            Spacer(Modifier.height(12.dp))
                            IconFormField(
                                icon = Icons.Outlined.AlternateEmail,
                                label = "Никнейм",
                                value = nickname,
                                onValueChange = { nickname = it },
                                placeholder = "Ваш никнейм"
                            )
                            Spacer(Modifier.height(12.dp))
                            IconFormField(
                                icon = Icons.Outlined.Info,
                                label = "О себе",
                                value = bio,
                                onValueChange = { if (it.length <= BIO_MAX_LENGTH) bio = it },
                                placeholder = "Расскажите о себе",
                                singleLine = false,
                                minLines = 3
                            )
                            // Character counter
                            Text(
                                "${bio.length}/$BIO_MAX_LENGTH",
                                fontSize = 11.sp,
                                color = if (bio.length > BIO_MAX_LENGTH - 30) Accent else TextMuted,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, end = 4.dp),
                                textAlign = TextAlign.End
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // ══════════════════════════════════════
                        // SECTION: Контакты
                        // ══════════════════════════════════════
                        FormSection(
                            title = "Контакты",
                            icon = Icons.Outlined.Phone
                        ) {
                            IconFormField(
                                icon = Icons.Outlined.Phone,
                                label = "Телефон",
                                value = phone,
                                onValueChange = { phone = it },
                                placeholder = "+7 XXX XXX XX XX",
                                keyboardType = KeyboardType.Phone
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // ══════════════════════════════════════
                        // SECTION: Местоположение
                        // ══════════════════════════════════════
                        FormSection(
                            title = "Местоположение",
                            icon = Icons.Outlined.LocationCity
                        ) {
                            IconFormField(
                                icon = Icons.Outlined.LocationCity,
                                label = "Город",
                                value = city,
                                onValueChange = { city = it },
                                placeholder = "Ваш город"
                            )
                            Spacer(Modifier.height(12.dp))
                            IconFormField(
                                icon = Icons.Outlined.Public,
                                label = "Страна",
                                value = country,
                                onValueChange = { country = it },
                                placeholder = "Ваша страна"
                            )
                        }

                        Spacer(Modifier.height(28.dp))

                        // ══════════════════════════════════════
                        // SAVE BUTTON
                        // ══════════════════════════════════════
                        val enabled = hasChanges && !isSaving
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (enabled) Accent else TextMuted.copy(alpha = 0.3f))
                                .clickable(enabled = enabled) { doSave() }
                                .padding(vertical = 16.dp),
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
                                    fontSize = 16.sp,
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

// ══════════════════════════════════════════
// Form Section Card
// ══════════════════════════════════════════

@Composable
private fun FormSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = CardBg
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

// ══════════════════════════════════════════
// Form Field with Icon
// ══════════════════════════════════════════

@Composable
private fun IconFormField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            icon, null,
            tint = TextMuted,
            modifier = Modifier
                .padding(top = 28.dp)
                .size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        DarkFormField(
            label = label,
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            singleLine = singleLine,
            minLines = minLines,
            error = error,
            keyboardType = keyboardType,
            modifier = Modifier.weight(1f)
        )
    }
}
