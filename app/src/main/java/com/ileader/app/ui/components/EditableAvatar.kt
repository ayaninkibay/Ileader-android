package com.ileader.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@Composable
fun EditableAvatar(
    avatarUrl: String?,
    displayName: String,
    size: Dp = 80.dp,
    isUploading: Boolean = false,
    onImageSelected: (ByteArray) -> Unit
) {
    val context = LocalContext.current
    // Декод/ресайз/JPEG-сжатие — десятки/сотни мс на телефонах поскромнее.
    // Раньше блокировало main → дроп фреймов и риск ANR на больших фотках.
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch(Dispatchers.IO) {
                val bytes = compressImage(context, selectedUri)
                if (bytes != null) {
                    withContext(Dispatchers.Main) { onImageSelected(bytes) }
                }
            }
        }
    }

    Box(contentAlignment = Alignment.Center) {
        UserAvatar(
            avatarUrl = avatarUrl,
            displayName = displayName,
            size = size
        )

        // Camera overlay
        Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = if (isUploading) 0.5f else 0.3f))
                .clickable(enabled = !isUploading) { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Изменить фото",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(if (size >= 72.dp) 28.dp else 20.dp)
                )
            }
        }
    }
}

private fun compressImage(
    context: Context,
    uri: Uri,
    maxDimension: Int = 512,
    maxSizeKb: Int = 500
): ByteArray? {
    return try {
        // Pass 1: read только размеры, без аллокации пикселей.
        // BitmapFactory.decodeStream(stream) на 12MP-фотке = ~48MB в памяти
        // → OOM на low-end девайсах. inSampleSize даунсемплит при декоде.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        // Степень двойки — это требование inSampleSize. Целимся в 2× maxDimension,
        // дальше точный resize даст финальный размер.
        var sample = 1
        while (bounds.outWidth / sample > maxDimension * 2 ||
               bounds.outHeight / sample > maxDimension * 2) {
            sample *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)
        } ?: return null

        val scale = minOf(
            maxDimension.toFloat() / decoded.width,
            maxDimension.toFloat() / decoded.height,
            1f
        )
        val resized = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * scale).toInt(),
                (decoded.height * scale).toInt(),
                true
            )
        } else {
            decoded
        }

        var quality = 85
        var bytes: ByteArray
        do {
            val baos = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            bytes = baos.toByteArray()
            quality -= 10
        } while (bytes.size > maxSizeKb * 1024 && quality > 20)

        if (resized !== decoded) resized.recycle()
        decoded.recycle()

        bytes
    } catch (_: Exception) {
        null
    }
}
