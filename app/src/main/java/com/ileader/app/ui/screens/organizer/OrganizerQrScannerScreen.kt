package com.ileader.app.ui.screens.organizer

import android.Manifest
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.dto.ParticipantDto
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.viewmodels.CheckInScanState
import com.ileader.app.ui.viewmodels.CheckInViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OrganizerQrScannerScreen(
    user: User,
    tournamentId: String,
    tournamentName: String,
    onBack: () -> Unit
) {
    val vm: CheckInViewModel = viewModel()
    val scanState by vm.scanState.collectAsState()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {

        when {
            !cameraPermission.status.isGranted -> {
                // Нет разрешения
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(DarkTheme.Bg)
                        .statusBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BackHeader(tournamentName, onBack)
                    Spacer(Modifier.height(48.dp))
                    Text(
                        "Нужен доступ к камере\nдля сканирования QR-кодов",
                        fontSize = 16.sp,
                        color = DarkTheme.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { cameraPermission.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Разрешить доступ")
                    }
                }
            }

            scanState == CheckInScanState.Idle -> {
                // Камера активна
                QrCameraPreview(onQrDetected = { vm.onQrScanned(it, tournamentId) })

                // Полупрозрачный оверлей сверху
                Column(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.55f))
                            .statusBarsPadding()
                    ) {
                        BackHeader(tournamentName, onBack)
                    }

                    // Прозрачная зона прицела
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Затемнение по бокам
                        Row(Modifier.fillMaxSize()) {
                            Box(Modifier.weight(1f).fillMaxHeight().background(Color.Black.copy(alpha = 0.4f)))
                            Box(Modifier.size(260.dp))
                            Box(Modifier.weight(1f).fillMaxHeight().background(Color.Black.copy(alpha = 0.4f)))
                        }
                        // Рамка прицела
                        Box(
                            Modifier
                                .size(260.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            // Уголки рамки
                            ScannerCorners()
                        }
                    }

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Наведите камеру на QR-код участника",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            scanState == CheckInScanState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DarkTheme.Accent)
                }
            }
        }

        // Результаты сканирования (bottom sheet поверх всего)
        when (val s = scanState) {
            is CheckInScanState.Found -> CheckInResultSheet(
                participant = s.participant,
                alreadyCheckedIn = s.alreadyCheckedIn,
                onConfirm = { vm.confirmCheckIn() },
                onDismiss = { vm.resetScan() }
            )
            is CheckInScanState.CheckedIn -> CheckInSuccessSheet(onDismiss = { vm.resetScan() })
            is CheckInScanState.NotFound -> CheckInErrorSheet(
                message = "Участник не найден в этом турнире",
                onDismiss = { vm.resetScan() }
            )
            is CheckInScanState.Error -> CheckInErrorSheet(
                message = s.message,
                onDismiss = { vm.resetScan() }
            )
            else -> {}
        }
    }
}

@Composable
private fun ScannerCorners() {
    val cornerColor = Color.White
    val cornerSize = 24.dp
    val thickness = 3.dp
    Box(Modifier.fillMaxSize()) {
        // Верхний левый
        Column(Modifier.align(Alignment.TopStart)) {
            Box(Modifier.width(cornerSize).height(thickness).background(cornerColor))
            Box(Modifier.width(thickness).height(cornerSize).background(cornerColor))
        }
        // Верхний правый
        Column(Modifier.align(Alignment.TopEnd)) {
            Box(Modifier.width(cornerSize).height(thickness).background(cornerColor))
            Box(Modifier.width(thickness).height(cornerSize).background(cornerColor).align(Alignment.End))
        }
        // Нижний левый
        Column(Modifier.align(Alignment.BottomStart)) {
            Box(Modifier.width(thickness).height(cornerSize).background(cornerColor))
            Box(Modifier.width(cornerSize).height(thickness).background(cornerColor))
        }
        // Нижний правый
        Column(Modifier.align(Alignment.BottomEnd)) {
            Box(Modifier.width(thickness).height(cornerSize).background(cornerColor).align(Alignment.End))
            Box(Modifier.width(cornerSize).height(thickness).background(cornerColor))
        }
    }
}

@Composable
private fun QrCameraPreview(onQrDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember { BarcodeScanning.getClient() }
    var lastScanned by remember { mutableStateOf("") }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage, imageProxy.imageInfo.rotationDegrees
                        )
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                barcodes
                                    .firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                                    ?.rawValue
                                    ?.let { value ->
                                        if (value != lastScanned) {
                                            lastScanned = value
                                            onQrDetected(value)
                                        }
                                    }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        imageProxy.close()
                    }
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                } catch (e: Exception) { /* ignore */ }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun CheckInResultSheet(
    participant: ParticipantDto,
    alreadyCheckedIn: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = DarkTheme.CardBg
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                Box(
                    Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(DarkTheme.CardBorder)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))

                Text(
                    participant.profiles?.name ?: "Участник",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary
                )
                if (participant.profiles?.city != null) {
                    Text(participant.profiles.city, fontSize = 13.sp, color = DarkTheme.TextMuted)
                }
                if (participant.teams != null) {
                    Spacer(Modifier.height(4.dp))
                    Text("Команда: ${participant.teams.name}", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                }
                Spacer(Modifier.height(8.dp))

                val statusText = when (participant.status) {
                    "confirmed" -> "Подтверждён"
                    "pending" -> "Ожидает подтверждения"
                    else -> participant.status ?: "—"
                }
                val statusColor = if (participant.status == "confirmed") DarkTheme.Accent else DarkTheme.TextMuted
                Text("Статус заявки: $statusText", fontSize = 13.sp, color = statusColor)

                Spacer(Modifier.height(20.dp))

                if (alreadyCheckedIn) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = DarkTheme.Accent, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Уже отмечен на входе", fontSize = 14.sp, color = DarkTheme.Accent, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.TextMuted),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Закрыть") }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Отмена", color = DarkTheme.TextSecondary) }
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Отметить") }
                    }
                }
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun CheckInSuccessSheet(onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = DarkTheme.CardBg
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = DarkTheme.Accent, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(12.dp))
                Text("Check-in выполнен!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text("Участник успешно отмечен на входе", fontSize = 14.sp, color = DarkTheme.TextMuted)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Сканировать следующего") }
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun CheckInErrorSheet(message: String, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = DarkTheme.CardBg
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ErrorOutline, null, tint = DarkTheme.TextMuted, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text(message, fontSize = 16.sp, color = DarkTheme.TextPrimary, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Попробовать снова") }
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}
