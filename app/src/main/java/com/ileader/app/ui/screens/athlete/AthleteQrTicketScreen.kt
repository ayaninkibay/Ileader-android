package com.ileader.app.ui.screens.athlete

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.dto.QrPayload
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkTheme
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun AthleteQrTicketScreen(
    user: User,
    tournamentId: String,
    tournamentName: String,
    isCheckedIn: Boolean = false,
    onBack: () -> Unit
) {
    val qrBitmap = remember(user.id, tournamentId) {
        generateQrBitmap(
            QrPayload(uid = user.id, tid = tournamentId, ts = System.currentTimeMillis() / 1000)
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        BackHeader("Мой билет", onBack)

        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                tournamentName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTheme.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)

            Spacer(Modifier.height(32.dp))

            // QR-код на белом фоне
            Box(
                modifier = Modifier
                    .size(256.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR билет",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.QrCode2,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (isCheckedIn) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = DarkTheme.Accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Вы уже отмечены на входе",
                        fontSize = 14.sp,
                        color = DarkTheme.Accent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    "Покажите этот код организатору на входе",
                    fontSize = 13.sp,
                    color = DarkTheme.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun generateQrBitmap(payload: QrPayload, size: Int = 512): Bitmap? {
    return try {
        val json = Json.encodeToString(payload)
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bits = QRCodeWriter().encode(json, BarcodeFormat.QR_CODE, size, size, hints)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        null
    }
}
