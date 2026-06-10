package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.repository.AccreditationStats
import com.ileader.app.data.repository.MediaRepository
import com.ileader.app.data.util.Alerts
import com.ileader.app.data.util.AppLogger
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
                    AppLogger.e("MediaVM.loadAccreditations invites failed", e)
                    UiState.Error(e.message ?: "Ошибка загрузки аккредитаций")
                }
            }

            val statsDeferred = async {
                try { repo.getAccreditationStats(userId) }
                catch (e: Exception) {
                    AppLogger.w("MediaVM.loadAccreditations stats: ${e.message}", e); AccreditationStats(0, 0, 0)
                }
            }

            val mapDeferred = async {
                try { repo.getAccreditationMap(userId) }
                catch (e: Exception) {
                    AppLogger.w("MediaVM.loadAccreditations map: ${e.message}", e); emptyMap()
                }
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
                AppLogger.e("MediaVM.loadUpcomingTournaments failed", e)
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
                    AppLogger.e("MediaVM.loadArticles failed", e)
                    UiState.Error(e.message ?: "Ошибка загрузки статей")
                }
            }

            val statsDeferred = async {
                try { repo.getArticleStats(userId) }
                catch (e: Exception) {
                    AppLogger.w("MediaVM.loadArticles stats: ${e.message}", e); ArticleStatsDto()
                }
            }

            val topDeferred = async {
                try { repo.getTopArticlesByViews(userId) }
                catch (e: Exception) {
                    AppLogger.w("MediaVM.loadArticles top: ${e.message}", e); emptyList()
                }
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
                AppLogger.e("MediaVM.loadArticle failed", e)
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
                Alerts.success("Заявка на аккредитацию отправлена")
                _actionState.value = UiState.Success("Заявка на аккредитацию отправлена")
                loadAccreditations(userId)
            } catch (e: Exception) {
                AppLogger.e("MediaVM.requestAccreditation failed", e)
                Alerts.error("Не удалось отправить заявку на аккредитацию")
                _actionState.value = UiState.Error(e.message ?: "Ошибка отправки заявки")
            }
        }
    }

    fun cancelAccreditation(userId: String, tournamentId: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.cancelAccreditation(userId, tournamentId)
                Alerts.success("Аккредитация отменена")
                _actionState.value = UiState.Success("Аккредитация отменена")
                loadAccreditations(userId)
            } catch (e: Exception) {
                AppLogger.e("MediaVM.cancelAccreditation failed", e)
                Alerts.error("Не удалось отменить аккредитацию")
                _actionState.value = UiState.Error(e.message ?: "Ошибка отмены аккредитации")
            }
        }
    }

    fun acceptInvite(inviteId: String, userId: String, contactPhone: String, message: String?) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.acceptInvite(inviteId, contactPhone, message)
                Alerts.success("Приглашение принято")
                _actionState.value = UiState.Success("Приглашение принято")
                loadAccreditations(userId)
            } catch (e: Exception) {
                AppLogger.e("MediaVM.acceptInvite failed", e)
                Alerts.error("Не удалось обработать приглашение")
                _actionState.value = UiState.Error(e.message ?: "Ошибка принятия приглашения")
            }
        }
    }

    fun declineInvite(inviteId: String, userId: String, reason: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.declineInvite(inviteId, reason)
                Alerts.success("Приглашение отклонено")
                _actionState.value = UiState.Success("Приглашение отклонено")
                loadAccreditations(userId)
            } catch (e: Exception) {
                AppLogger.e("MediaVM.declineInvite failed", e)
                Alerts.error("Не удалось обработать приглашение")
                _actionState.value = UiState.Error(e.message ?: "Ошибка отклонения")
            }
        }
    }

    fun joinByInviteCode(code: String, userId: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.joinByInviteCode(code, userId)
                Alerts.success("Аккредитация по инвайт-коду создана")
                _actionState.value = UiState.Success("Аккредитация по инвайт-коду создана")
                loadAccreditations(userId)
            } catch (e: Exception) {
                AppLogger.e("MediaVM.joinByInviteCode failed", e)
                Alerts.error("Недействительный инвайт-код")
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
                Alerts.success("Статья создана")
                _actionState.value = UiState.Success("Статья создана")
                loadArticles(userId)
            } catch (e: Exception) {
                AppLogger.e("MediaVM.createArticle failed", e)
                Alerts.error("Не удалось создать статью")
                _actionState.value = UiState.Error(e.message ?: "Ошибка создания статьи")
            }
        }
    }

    fun updateArticle(articleId: String, userId: String, data: ArticleUpdateDto) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.updateArticle(articleId, data)
                Alerts.success("Статья обновлена")
                _actionState.value = UiState.Success("Статья обновлена")
                loadArticles(userId)
            } catch (e: Exception) {
                AppLogger.e("MediaVM.updateArticle failed", e)
                Alerts.error("Не удалось обновить статью")
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
                AppLogger.e("MediaVM.deleteArticle failed", e)
                Alerts.error("Не удалось удалить статью")
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
                    AppLogger.e("MediaVM.loadInterviews failed", e)
                    UiState.Error(e.message ?: "Ошибка загрузки интервью")
                }
            }

            val statsDeferred = async {
                try { repo.getInterviewStats(userId) }
                catch (e: Exception) {
                    AppLogger.w("MediaVM.loadInterviews stats: ${e.message}", e); InterviewStatsDto()
                }
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
                AppLogger.e("MediaVM.loadInterview failed", e)
                _currentInterview.value = UiState.Error(e.message ?: "Ошибка загрузки интервью")
            }
        }
    }

    fun searchAthletes(query: String) {
        viewModelScope.launch {
            try {
                _athleteSearch.value = repo.searchAthletes(query)
            } catch (e: Exception) {
                AppLogger.w("MediaVM.searchAthletes: ${e.message}", e)
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
                Alerts.success("Интервью создано")
                _actionState.value = UiState.Success("Интервью создано")
                loadInterviews(userId)
            } catch (e: Exception) {
                AppLogger.e("MediaVM.createInterview failed", e)
                Alerts.error("Не удалось создать интервью")
                _actionState.value = UiState.Error(e.message ?: "Ошибка создания интервью")
            }
        }
    }

    fun updateInterview(interviewId: String, userId: String, data: InterviewUpdateDto) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repo.updateInterview(interviewId, data)
                Alerts.success("Интервью обновлено")
                _actionState.value = UiState.Success("Интервью обновлено")
                loadInterviews(userId)
            } catch (e: Exception) {
                AppLogger.e("MediaVM.updateInterview failed", e)
                Alerts.error("Не удалось обновить интервью")
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
                AppLogger.e("MediaVM.deleteInterview failed", e)
                Alerts.error("Не удалось удалить интервью")
                _actionState.value = UiState.Error(e.message ?: "Ошибка удаления интервью")
            }
        }
    }
}
