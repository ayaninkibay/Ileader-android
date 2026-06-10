package com.ileader.app.ui.screens.auth

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.ui.components.ILeaderButton
import com.ileader.app.ui.components.pressableClick
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

private const val CODE_LEN = 6

/**
 * Экран подтверждения email после регистрации — 6-значный код из письма.
 * Зеркало `/verify-code` страницы веб-сайта ileader.kz.
 *
 * State приходит из [AuthViewModel] — там же хранится email/password из
 * предыдущего signUp шага, чтобы после verify сразу залогинить юзера без
 * повторного ввода пароля.
 */
@Composable
fun VerifyCodeScreen(
    state: AuthState,
    onVerify: (String) -> Unit,
    onResend: () -> Unit,
    onTickCooldown: () -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit
) {
    val colors = LocalAppColors.current
    val focusManager = LocalFocusManager.current

    // Один input — Compose с native-keyboard и автопереходом по символам.
    // 6 box'ов отображаются визуально, но текстфилд один.
    var codeText by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Авто-фокус при входе на экран.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // Авто-отправка когда введены все 6 цифр.
    LaunchedEffect(codeText) {
        if (codeText.length == CODE_LEN && !state.isLoading) {
            focusManager.clearFocus()
            onVerify(codeText)
        }
    }

    // Тикаем cooldown каждую секунду пока он > 0.
    LaunchedEffect(state.otpResendCooldown) {
        if (state.otpResendCooldown > 0) {
            delay(1000)
            onTickCooldown()
        }
    }

    // Если в state есть ошибка — чистим введённый код для следующей попытки.
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            codeText = ""
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Top bar with back ──
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .pressableClick {
                        onClearError()
                        onBack()
                    }
                    .background(colors.cardBg)
                    .border(0.5.dp, colors.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(40.dp))

        // ── Icon ──
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ILeaderColors.PrimaryRed.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.MailOutline,
                contentDescription = null,
                tint = ILeaderColors.PrimaryRed,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Подтвердите email",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Мы отправили 6-значный код на",
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        state.pendingEmail?.let {
            Spacer(Modifier.height(2.dp))
            Text(
                it,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        Spacer(Modifier.height(32.dp))

        // ── 6 OTP boxes ──
        Box(modifier = Modifier.fillMaxWidth()) {
            // Скрытый BasicTextField принимает фокус и ввод; ниже — визуальные боксы.
            BasicTextField(
                value = codeText,
                onValueChange = { v ->
                    val digits = v.filter { it.isDigit() }.take(CODE_LEN)
                    codeText = digits
                    if (state.errorMessage != null) onClearError()
                },
                modifier = Modifier
                    .matchParentSize()
                    .focusRequester(focusRequester),
                singleLine = true,
                cursorBrush = SolidColor(Color.Transparent),
                textStyle = TextStyle(color = Color.Transparent),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(CODE_LEN) { i ->
                    val ch = codeText.getOrNull(i)?.toString() ?: ""
                    val filled = ch.isNotEmpty()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.cardBg)
                            .border(
                                width = if (filled) 1.5.dp else 0.5.dp,
                                color = if (filled) ILeaderColors.PrimaryRed else colors.border,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            ch,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }

        // ── Dev hint ──
        state.devOtpCode?.let {
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFEF3C7)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Resend не настроен", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Письмо не отправлено. Тестовый код: $it",
                        fontSize = 12.sp,
                        color = Color(0xFF92400E)
                    )
                }
            }
        }

        // ── Error ──
        state.errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                it,
                fontSize = 13.sp,
                color = ILeaderColors.Error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Verify button ──
        ILeaderButton(
            text = "Подтвердить",
            onClick = {
                if (codeText.length == CODE_LEN) {
                    focusManager.clearFocus()
                    onVerify(codeText)
                }
            },
            enabled = codeText.length == CODE_LEN,
            isLoading = state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // ── Resend ──
        if (state.otpResendCooldown > 0) {
            Text(
                "Отправить новый код через ${state.otpResendCooldown} сек.",
                fontSize = 13.sp,
                color = colors.textMuted
            )
        } else {
            Text(
                "Отправить код повторно",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ILeaderColors.PrimaryRed,
                modifier = Modifier.pressableClick { onResend() }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
