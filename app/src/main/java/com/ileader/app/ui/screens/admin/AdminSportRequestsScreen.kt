package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.ileader.app.data.remote.dto.SportRequestDto
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
fun AdminSportRequestsScreen(onBack: () -> Unit) {
    val repo = remember { AdminRepository() }
    val scope = rememberCoroutineScope()

    var requests by remember { mutableStateOf<List<SportRequestDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                requests = repo.getSportRequests()
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
        BackHeader("Заявки на виды спорта", onBack)

        when {
            loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(error ?: "", color = TextMuted)
            }
            requests.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Заявок нет", color = TextMuted)
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(requests, key = { it.id }) { req ->
                    SportRequestCard(
                        req = req,
                        onApprove = {
                            scope.launch {
                                try { repo.approveSportRequest(req.id); load() } catch (_: Exception) {}
                            }
                        },
                        onReject = {
                            scope.launch {
                                try { repo.rejectSportRequest(req.id); load() } catch (_: Exception) {}
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
private fun SportRequestCard(
    req: SportRequestDto,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val isPending = req.status == null || req.status == "pending"
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBg
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(req.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                StatusPill(req.status ?: "pending")
            }
            req.description?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, fontSize = 12.sp, color = TextMuted)
            }
            req.profiles?.name?.let {
                Spacer(Modifier.height(4.dp))
                Text("От: $it", fontSize = 11.sp, color = TextMuted)
            }
            if (isPending) {
                Spacer(Modifier.height(10.dp))
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
                            Text("Создать спорт", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF22C55E))
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
                            Text("Отклонить", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: String) {
    val (label, color) = when (status) {
        "approved" -> "Одобрено" to Color(0xFF22C55E)
        "rejected" -> "Отклонено" to Color(0xFFEF4444)
        else -> "Ожидает" to Color(0xFFF59E0B)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.12f)) {
        Text(
            label,
            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold
        )
    }
}
