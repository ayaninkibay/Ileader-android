package com.ileader.app.ui.screens.organizer

import androidx.compose.runtime.Composable

@Composable
fun OrganizerTournamentCreateScreen(
    userId: String,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    OrganizerTournamentEditScreen(
        tournamentId = null,
        userId = userId,
        onBack = onBack,
        onSave = onCreated
    )
}
