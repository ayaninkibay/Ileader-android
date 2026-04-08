package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
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
import coil.compose.AsyncImage
import com.ileader.app.data.remote.dto.AdminUserDto
import com.ileader.app.data.repository.AdminRepository
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkTheme
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun AdminVerificationsScreen(onBack: () -> Unit) {
    val repo = remember { AdminRepository() }
    val scope = rememberCoroutineScope()

    var requests by remember { mutableStateOf<List<AdminUserDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                requests = repo.getPendingVerifications()
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(
        Modifier.fillMaxSize().background(Bg).statusBarsPadding()
    ) {
        BackHeader("Заявки на верификацию", onBack)

        when {
            loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(error ?: "", color = TextMuted)
            }
            requests.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Нет заявок на рассмотрение", color = TextMuted)
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(requests, key = { it.id }) { user ->
                    VerificationCard(
                        user = user,
                        onApprove = {
                            scope.launch {
                                try {
                                    repo.approveVerification(user.id); load()
                                } catch (_: Exception) {}
                            }
                        },
                        onReject = {
                            scope.launch {
                                try {
                                    repo.rejectVerification(user.id); load()
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun VerificationCard(
    user: AdminUserDto,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBg
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (user.avatarUrl != null) {
                    AsyncImage(
                        model = user.avatarUrl, contentDescription = null,
                        modifier = Modifier.size(44.dp).clip(CircleShape)
                    )
                } else {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(Accent.copy(0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, null, tint = Accent, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(user.name ?: "—", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(user.email ?: "", fontSize = 11.sp, color = TextMuted)
                    user.roles?.name?.let {
                        Text("Роль: $it", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { onApprove() },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF22C55E).copy(alpha = 0.15f)
                ) {
                    Row(
                        Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Check, null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Одобрить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF22C55E))
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { onReject() },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.15f)
                ) {
                    Row(
                        Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Close, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Отклонить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}
