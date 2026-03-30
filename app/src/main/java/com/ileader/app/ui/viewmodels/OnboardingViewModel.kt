package com.ileader.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.preferences.SportPreference
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.repository.ViewerRepository
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
private data class UserSportInsert(
    val user_id: String,
    val sport_id: String,
    val is_primary: Boolean = false
)

class OnboardingViewModel : ViewModel() {

    private val repo = ViewerRepository()

    private val _sportsState = MutableStateFlow<UiState<List<SportDto>>>(UiState.Loading)
    val sportsState: StateFlow<UiState<List<SportDto>>> = _sportsState

    private val _selectedSportIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedSportIds: StateFlow<Set<String>> = _selectedSportIds

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    init {
        loadSports()
    }

    private fun loadSports() {
        viewModelScope.launch {
            _sportsState.value = UiState.Loading
            try {
                val sports = repo.getSports()
                _sportsState.value = UiState.Success(sports)
            } catch (e: Exception) {
                _sportsState.value = UiState.Error(e.message ?: "Ошибка загрузки спортов")
            }
        }
    }

    fun toggleSport(sportId: String) {
        val current = _selectedSportIds.value
        _selectedSportIds.value = if (sportId in current) {
            current - sportId
        } else {
            if (current.size < 3) current + sportId else current
        }
    }

    fun saveSports(userId: String, context: Context, onComplete: () -> Unit) {
        if (_isSaving.value) return
        val selected = _selectedSportIds.value
        if (selected.isEmpty()) return

        val sports = (_sportsState.value as? UiState.Success)?.data ?: return

        viewModelScope.launch {
            _isSaving.value = true
            try {
                // Insert into user_sports table
                val inserts = selected.mapIndexed { index, sportId ->
                    UserSportInsert(
                        user_id = userId,
                        sport_id = sportId,
                        is_primary = index == 0
                    )
                }
                SupabaseModule.client.from("user_sports").insert(inserts)

                // Save to local DataStore
                val selectedSports = sports.filter { it.id in selected }
                val pref = SportPreference(context)
                pref.setSports(
                    ids = selectedSports.map { it.id },
                    names = selectedSports.map { it.name }
                )

                onComplete()
            } catch (e: Exception) {
                _sportsState.value = UiState.Error(e.message ?: "Ошибка сохранения")
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun retry() {
        loadSports()
    }
}
