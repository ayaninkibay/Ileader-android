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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bytes = compressImage(context, it)
            if (bytes != null) onImageSelected(bytes)
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
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val scale = minOf(
            maxDimension.toFloat() / original.width,
            maxDimension.toFloat() / original.height,
            1f
        )
        val resized = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                original,
                (original.width * scale).toInt(),
                (original.height * scale).toInt(),
                true
            )
        } else {
            original
        }

        var quality = 85
        var bytes: ByteArray
        do {
            val baos = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            bytes = baos.toByteArray()
            quality -= 10
        } while (bytes.size > maxSizeKb * 1024 && quality > 20)

        if (resized !== original) resized.recycle()
        original.recycle()

        bytes
    } catch (_: Exception) {
        null
    }
}
