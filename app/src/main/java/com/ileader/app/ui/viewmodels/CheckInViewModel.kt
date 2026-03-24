package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.dto.ParticipantDto
import com.ileader.app.data.remote.dto.QrPayload
import com.ileader.app.data.repository.CheckInRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

sealed class CheckInScanState {
    object Idle : CheckInScanState()
    object Loading : CheckInScanState()
    data class Found(val participant: ParticipantDto, val alreadyCheckedIn: Boolean) : CheckInScanState()
    object NotFound : CheckInScanState()
    data class Error(val message: String) : CheckInScanState()
    object CheckedIn : CheckInScanState()
}

class CheckInViewModel : ViewModel() {
    private val repo = CheckInRepository()

    private val _scanState = MutableStateFlow<CheckInScanState>(CheckInScanState.Idle)
    val scanState: StateFlow<CheckInScanState> = _scanState

    private var pendingParticipant: ParticipantDto? = null

    fun onQrScanned(jsonPayload: String, expectedTournamentId: String) {
        if (_scanState.value is CheckInScanState.Loading) return
        viewModelScope.launch {
            try {
                val payload = Json.decodeFromString<QrPayload>(jsonPayload)
                if (payload.tid != expectedTournamentId) {
                    _scanState.value = CheckInScanState.Error("QR-код от другого турнира")
                    return@launch
                }
                _scanState.value = CheckInScanState.Loading
                val participant = repo.getParticipant(payload.tid, payload.uid)
                if (participant == null) {
                    _scanState.value = CheckInScanState.NotFound
                } else {
                    pendingParticipant = participant
                    _scanState.value = CheckInScanState.Found(
                        participant = participant,
                        alreadyCheckedIn = participant.checkInStatus == "checked_in"
                    )
                }
            } catch (e: Exception) {
                _scanState.value = CheckInScanState.Error("Неверный QR-код")
            }
        }
    }

    fun confirmCheckIn() {
        val p = pendingParticipant ?: return
        viewModelScope.launch {
            try {
                repo.markCheckIn(p.tournamentId, p.athleteId)
                _scanState.value = CheckInScanState.CheckedIn
            } catch (e: Exception) {
                _scanState.value = CheckInScanState.Error("Ошибка при check-in")
            }
        }
    }

    fun resetScan() {
        pendingParticipant = null
        _scanState.value = CheckInScanState.Idle
    }
}
