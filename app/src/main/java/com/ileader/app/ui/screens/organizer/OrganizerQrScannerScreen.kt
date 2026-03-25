package com.ileader.app.ui.screens.organizer

import androidx.compose.runtime.Composable
import com.ileader.app.data.models.User
import com.ileader.app.ui.screens.common.QrScannerScreen

/**
 * Organizer QR scanner — delegates to shared QrScannerScreen
 */
@Composable
fun OrganizerQrScannerScreen(
    user: User,
    tournamentId: String,
    tournamentName: String,
    onBack: () -> Unit
) {
    QrScannerScreen(
        tournamentId = tournamentId,
        tournamentName = tournamentName,
        onBack = onBack
    )
}
