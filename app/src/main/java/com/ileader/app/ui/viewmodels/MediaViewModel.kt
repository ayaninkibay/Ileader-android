package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.repository.AccreditationStats
import com.ileader.app.data.repository.MediaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MediaViewModel : ViewModel() {
    private val repo = MediaRepository()

    // ── Accreditations ──
    private val _invites = MutableStateFlow<UiState<List<MediaInviteFullDto>>>(UiState.Loading)
    val invites: StateFlow<UiState<List<MediaInviteFullDto>>> = _invites

    private val _accreditationStats = MutableStateFlow(AccreditationStats(0, 0, 0))
    val accreditationStats: StateFlow<AccreditationStats> = _accreditationStats

    private val _accreditationMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val accreditationMap: StateFlow<Map<String, String>> = _accreditationMap

    // ── Tournaments (for requesting accreditation) ──
    private val _upcomingTournaments = MutableStateFlow<UiState<List<TournamentWithCountsDto>>>(UiState.Loading)
    val upcomingTournaments: StateFlow<UiState<List<TournamentWithCountsDto>>> = _upcomingTournaments

    // ── Articles ──
    private val _articles = MutableStateFlow<UiState<List<ArticleDto>>>(UiState.Loading)
    val articles: StateFlow<UiState<List<ArticleDto>>> = _articles

    private val _articleStats = MutableStateFlow(ArticleStatsDto())
    val articleStats: StateFlow<ArticleStatsDto> = _articleStats

    private val _topArticles = MutableStateFlow<List<ArticleDto>>(emptyList())
    val topArticles: StateFlow<List<ArticleDto>> = _topArticles

    // ── Single article for editor ──
    private val _currentArticle = MutableStateFlow<UiState<ArticleDto?>>(UiState.Success(null))
    val currentArticle: StateFlow<UiState<ArticleDto?>> = _currentArticle

    // ── Interviews ──
    private val _interviews = MutableStateFlow<UiState<List<InterviewDto>>>(UiState.Loading)
    val interviews: StateFlow<UiState<List<InterviewDto>>> = _interviews

    private val _interviewStats = MutableStateFlow(InterviewStatsDto())
    val interviewStats: StateFlow<InterviewStatsDto> = _interviewStats

    private val _currentInterview = MutableStateFlow<UiState<InterviewDto?>>(UiState.Success(null))
    val currentInterview: StateFlow<UiState<InterviewDto?>> = _currentInterview

    // ── Athlete search (for interview editor) ──
    private val _athleteSearch = MutableStateFlow<List<ProfileMinimalDto>>(emptyList())
    val athleteSearch: StateFlow<List<ProfileMinimalDto>> = _athleteSearch

    // ── Action state ──
    private val _actionState = MutableStateFlow<UiState<String>?>(null)
    val actionState: StateFlow<UiState<String>?> = _actionState

    fun clearAction() { _actionState.value = null }

    // ══════════════════════════════════════════════════════════
    // LOAD
    // ══════════════════════════════════════════════════════════

    fun loadAccreditations(userId: String) {
        viewModelScope.launch {
            _invites.value = UiState.Loading

            val invitesDeferred = async {
                try {
                    UiState.Success(repo.getMediaInvites(userId))
                } catch (e: Exception) {
                    UiState.Error(e.message ?: "Ошибка загрузки аккредитаций")
                }
            }

            val statsDeferred = async {
                try { repo.getAccreditationStats(userId) }
                catch (_: Exception) { AccreditationStats(0, 0, 0) }
            }

            val mapDeferred = async {
                try { repo.getAccreditationMap(userId) }
                catch (_: Exception) { emptyMap() }
            }

            _invites.value = invitesDeferred.await()
            _accreditationStats.value = statsDeferred.await()
            _accreditationMap.value = mapDeferred.await()
        }
    }

    fun loadUpcomingTournaments() {
        viewModelScope.launch {
            _upcomingTournaments.value = UiState.Loading
            try {
                val list = repo.getUpcomingTournaments(20)
                _upcomingTournaments.value = UiState.Success(list)
            } catch (e: Exception) {
                _upcomingTournaments.value = UiState.Error(e.message ?: "Ошибка загрузки турниров")
            }
        }
    }

    fun loadArticles(userId: String) {
        viewModelScope.launch {
            _articles.value = UiState.Loading

            val articlesDeferred = async {
                try {
                    UiState.Success(repo.getMyArticles(userId))
                } catch (e: Exception) {
                    UiState.Error(e.message ?: "Ошибка загрузки статей")
                }
            }

            val statsDeferred = async {
                try { repo.getArticleStats(userId) }
                catch (_: Exception) { ArticleStatsDto() }
            }

            val topDeferred = async {
                try { repo.getTopArticlesByViews(userId) }
                catch (_: Exception) { emptyList() }
            }

            _articles.value = articlesDeferred.await()
            _articleStats.value = statsDeferred.await()
            _topArticles.value = topDeferred.await()
        }
    }

    fun loadArticle(articleId: String) {
        viewModelScope.launch {
            _currentArticle.value = UiState.Loading
            try {
                val article = repo.getArticleById(articleId)
                _currentArticle.value = UiState.Success(article)
            } catch (e: Exception) {
                _currentArticle.value = UiState.Error(e.message ?: "Ошибка загрузки статьи")
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // ACCREDITATION ACTIONS
    // ══════════════════════════════════════════════════════════

    fun requestAccreditation(userId: String, tournamentId: String, message: String? = null) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.requestAccreditation(userId, tournamentId, message)
                _actionState.value = UiState.Success("Заявка на аккредитацию отправлена")
                loadAccreditations(userId)
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Ошибка отправки заявки")
            }
        }
    }

    fun cancelAccreditation(userId: String, tournamentId: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.cancelAccreditation(userId, tournamentId)
                _actionState.value = UiState.Success("Аккредитация отменена")
                loadAccreditations(userId)
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Ошибка отмены аккредитации")
            }
        }
    }

    fun acceptInvite(inviteId: String, userId: String, contactPhone: String, message: String?) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.acceptInvite(inviteId, contactPhone, message)
                _actionState.value = UiState.Success("Приглашение принято")
                loadAccreditations(userId)
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Ошибка принятия приглашения")
            }
        }
    }

    fun declineInvite(inviteId: String, userId: String, reason: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.declineInvite(inviteId, reason)
                _actionState.value = UiState.Success("Приглашение отклонено")
                loadAccreditations(userId)
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Ошибка отклонения")
            }
        }
    }

    fun joinByInviteCode(code: String, userId: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.joinByInviteCode(code, userId)
                _actionState.value = UiState.Success("Аккредитация по инвайт-коду создана")
                loadAccreditations(userId)
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Недействительный инвайт-код")
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // ARTICLE ACTIONS
    // ══════════════════════════════════════════════════════════

    fun createArticle(userId: String, data: ArticleInsertDto) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.createArticle(data)
                _actionState.value = UiState.Success("Статья создана")
                loadArticles(userId)
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Ошибка создания статьи")
            }
        }
    }

    fun updateArticle(articleId: String, userId: String, data: ArticleUpdateDto) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.updateArticle(articleId, data)
                _actionState.value = UiState.Success("Статья обновлена")
                loadArticles(userId)
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Ошибка обновления статьи")
            }
        }
    }

    fun deleteArticle(articleId: String, userId: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.deleteArticle(articleId)
                _actionState.value = UiState.Success("Статья удалена")
                loadArticles(userId)
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Ошибка удаления статьи")
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // INTERVIEW LOAD
    // ══════════════════════════════════════════════════════════

    fun loadInterviews(userId: String) {
        viewModelScope.launch {
            _interviews.value = UiState.Loading

            val listDeferred = async {
                try {
                    UiState.Success(repo.getMyInterviews(userId))
                } catch (e: Exception) {
                    UiState.Error(e.message ?: "Ошибка загрузки интервью")
                }
            }

            val statsDeferred = async {
                try { repo.getInterviewStats(userId) }
                catch (_: Exception) { InterviewStatsDto() }
            }

            _interviews.value = listDeferred.await()
            _interviewStats.value = statsDeferred.await()
        }
    }

    fun loadInterview(interviewId: String) {
        viewModelScope.launch {
            _currentInterview.value = UiState.Loading
            try {
                val interview = repo.getInterviewById(interviewId)
                _currentInterview.value = UiState.Success(interview)
            } catch (e: Exception) {
                _currentInterview.value = UiState.Error(e.message ?: "Ошибка загрузки интервью")
            }
        }
    }

    fun searchAthletes(query: String) {
        viewModelScope.launch {
            try {
                _athleteSearch.value = repo.searchAthletes(query)
            } catch (_: Exception) {
                _athleteSearch.value = emptyList()
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // INTERVIEW ACTIONS
    // ══════════════════════════════════════════════════════════

    fun createInterview(userId: String, data: InterviewInsertDto) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.createInterview(data)
                _actionState.value = UiState.Success("Интервью создано")
                loadInterviews(userId)
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Ошибка создания интервью")
            }
        }
    }

    fun updateInterview(interviewId: String, userId: String, data: InterviewUpdateDto) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.updateInterview(interviewId, data)
                _actionState.value = UiState.Success("Интервью обновлено")
                loadInterviews(userId)
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Ошибка обновления интервью")
            }
        }
    }

    fun deleteInterview(interviewId: String, userId: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.deleteInterview(interviewId)
                _actionState.value = UiState.Success("Интервью удалено")
                loadInterviews(userId)
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Ошибка удаления интервью")
            }
        }
    }
}
