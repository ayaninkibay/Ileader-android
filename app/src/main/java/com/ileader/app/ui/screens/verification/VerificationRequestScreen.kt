package com.ileader.app.ui.screens.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
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
import com.ileader.app.data.models.User
import com.ileader.app.data.repository.AdminRepository
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
fun VerificationRequestScreen(
    user: User,
    onBack: () -> Unit,
    onSubmitted: () -> Unit
) {
    val repo = remember { AdminRepository() }
    val scope = rememberCoroutineScope()
    var description by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().background(Bg).statusBarsPadding()
    ) {
        BackHeader("Верификация", onBack)

        Column(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Surface(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Accent.copy(alpha = 0.08f)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.VerifiedUser, null, tint = Accent, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Подача заявки на верификацию",
                            fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                        )
                        Text(
                            "Роль: ${user.role.displayName}",
                            fontSize = 12.sp, color = TextSecondary
                        )
                    }
                }
            }

            Text(
                "Расскажите о своём опыте, прикрепите ссылки на документы или подтверждения. Заявка будет рассмотрена администратором.",
                fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp
            )

            DarkFormField(
                label = "Описание / документы",
                value = description,
                onValueChange = { description = it },
                placeholder = "Например: ссылки на свидетельства, лицензии, опыт работы…",
                singleLine = false,
                minLines = 5
            )

            error?.let {
                Text(it, fontSize = 12.sp, color = Color(0xFFEF4444))
            }

            Surface(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = !submitting) {
                        scope.launch {
                            submitting = true
                            error = null
                            try {
                                repo.submitVerificationRequest(user.id, description.takeIf { it.isNotBlank() })
                                onSubmitted()
                            } catch (e: Exception) {
                                error = e.message ?: "Ошибка отправки"
                            } finally {
                                submitting = false
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                color = if (submitting) Accent.copy(alpha = 0.5f) else Accent
            ) {
                Box(Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
                    if (submitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Подать заявку", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
