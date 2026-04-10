package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.dto.ParticipantDto
import com.ileader.app.data.remote.dto.QrPayload
import com.ileader.app.data.remote.dto.RefereeCheckInDto
import com.ileader.app.data.remote.dto.SpectatorDto
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.repository.CheckInRepository
import com.ileader.app.data.repository.HelperRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

sealed class CheckInScanState {
    object Idle : CheckInScanState()
    object Loading : CheckInScanState()

    data class FoundAthlete(
        val participant: ParticipantDto,
        val alreadyCheckedIn: Boolean
    ) : CheckInScanState()

    data class FoundSpectator(
        val spectator: SpectatorDto,
        val alreadyCheckedIn: Boolean
    ) : CheckInScanState()

    data class FoundReferee(
        val referee: RefereeCheckInDto,
        val alreadyCheckedIn: Boolean
    ) : CheckInScanState()

    // Keep for backwards compat with UI
    data class Found(
        val participant: ParticipantDto,
        val alreadyCheckedIn: Boolean
    ) : CheckInScanState()

    object NotFound : CheckInScanState()
    data class Error(val message: String) : CheckInScanState()
    object CheckedIn : CheckInScanState()
}

sealed class CheckInAccessState {
    object Checking : CheckInAccessState()
    object Allowed : CheckInAccessState()
    data class Denied(val reason: String) : CheckInAccessState()
}

@Serializable
private data class OrganizerIdDto(@SerialName("organizer_id") val organizerId: String? = null)

class CheckInViewModel : ViewModel() {
    private val repo = CheckInRepository()
    private val helperRepo = HelperRepository()
    private val client = SupabaseModule.client
    private val json = Json { ignoreUnknownKeys = true }

    private val _accessState = MutableStateFlow<CheckInAccessState>(CheckInAccessState.Checking)
    val accessState: StateFlow<CheckInAccessState> = _accessState

    /**
     * Verify that the user is allowed to run check-ins for this tournament.
     * Access is granted if the user is the tournament organizer OR an assigned helper.
     * Call this once on screen mount before exposing scanner / manual UI.
     */
    fun verifyAccess(userId: String, tournamentId: String) {
        viewModelScope.launch {
            _accessState.value = CheckInAccessState.Checking
            try {
                val isOrganizer = try {
                    val row = client.from("tournaments")
                        .select(Columns.raw("organizer_id")) {
                            filter { eq("id", tournamentId) }
                        }
                        .decodeSingleOrNull<OrganizerIdDto>()
                    row?.organizerId == userId
                } catch (_: Exception) { false }

                if (isOrganizer) {
                    _accessState.value = CheckInAccessState.Allowed
                    return@launch
                }

                val isHelper = try {
                    helperRepo.isHelperForTournament(userId, tournamentId)
                } catch (_: Exception) { false }

                _accessState.value = if (isHelper) {
                    CheckInAccessState.Allowed
                } else {
                    CheckInAccessState.Denied("У вас нет прав на check-in этого турнира")
                }
            } catch (e: Exception) {
                _accessState.value = CheckInAccessState.Denied(e.message ?: "Ошибка проверки доступа")
            }
        }
    }

    private val _scanState = MutableStateFlow<CheckInScanState>(CheckInScanState.Idle)
    val scanState: StateFlow<CheckInScanState> = _scanState

    private var pendingType: String = "athlete"
    private var pendingParticipant: ParticipantDto? = null
    private var pendingSpectator: SpectatorDto? = null
    private var pendingReferee: RefereeCheckInDto? = null

    fun onQrScanned(jsonPayload: String, expectedTournamentId: String) {
        if (_scanState.value is CheckInScanState.Loading) return
        viewModelScope.launch {
            try {
                val payload = json.decodeFromString<QrPayload>(jsonPayload)
                if (payload.tid != expectedTournamentId) {
                    _scanState.value = CheckInScanState.Error("QR-код от другого турнира")
                    return@launch
                }
                _scanState.value = CheckInScanState.Loading
                pendingType = payload.type

                when (payload.type) {
                    "referee" -> {
                        val referee = repo.getReferee(payload.tid, payload.uid)
                        if (referee == null) {
                            _scanState.value = CheckInScanState.NotFound
                        } else {
                            pendingReferee = referee
                            _scanState.value = CheckInScanState.FoundReferee(
                                referee = referee,
                                alreadyCheckedIn = referee.checkInStatus == "checked_in"
                            )
                        }
                    }
                    "spectator" -> {
                        val spectator = repo.getSpectator(payload.tid, payload.uid)
                        if (spectator == null) {
                            _scanState.value = CheckInScanState.NotFound
                        } else {
                            pendingSpectator = spectator
                            _scanState.value = CheckInScanState.FoundSpectator(
                                spectator = spectator,
                                alreadyCheckedIn = spectator.checkInStatus == "checked_in"
                            )
                        }
                    }
                    else -> {
                        // "athlete" or old QR without type
                        val participant = repo.getParticipant(payload.tid, payload.uid)
                        if (participant == null) {
                            _scanState.value = CheckInScanState.NotFound
                        } else {
                            pendingParticipant = participant
                            _scanState.value = CheckInScanState.FoundAthlete(
                                participant = participant,
                                alreadyCheckedIn = participant.checkInStatus == "checked_in"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _scanState.value = CheckInScanState.Error("Неверный QR-код")
            }
        }
    }

    fun confirmCheckIn() {
        viewModelScope.launch {
            try {
                when (pendingType) {
                    "referee" -> {
                        val r = pendingReferee ?: return@launch
                        repo.markRefereeCheckIn(r.tournamentId, r.refereeId)
                    }
                    "spectator" -> {
                        val s = pendingSpectator ?: return@launch
                        repo.markSpectatorCheckIn(s.tournamentId, s.userId)
                    }
                    else -> {
                        val p = pendingParticipant ?: return@launch
                        repo.markParticipantCheckIn(p.tournamentId, p.athleteId)
                    }
                }
                _scanState.value = CheckInScanState.CheckedIn
            } catch (e: Exception) {
                _scanState.value = CheckInScanState.Error("Ошибка при check-in")
            }
        }
    }

    fun resetScan() {
        pendingParticipant = null
        pendingSpectator = null
        pendingReferee = null
        pendingType = "athlete"
        _scanState.value = CheckInScanState.Idle
    }
}
