package com.ileader.app.ui.screens.organizer

import androidx.compose.runtime.Composable

@Composable
fun OrganizerLocationCreateScreen(
    userId: String,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    OrganizerLocationEditScreen(
        locationId = null,
        userId = userId,
        onBack = onBack,
        onSave = onCreated
    )
}
