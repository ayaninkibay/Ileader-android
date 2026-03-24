package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.ArticleStatsDto
import com.ileader.app.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MediaContentData(
    val articles: List<ArticleDto>,
    val stats: ArticleStatsDto
)

class MediaContentViewModel : ViewModel() {
    private val repo = MediaRepository()

    private val _state = MutableStateFlow<UiState<MediaContentData>>(UiState.Loading)
    val state: StateFlow<UiState<MediaContentData>> = _state

    private val _articleDetail = MutableStateFlow<UiState<ArticleDto>?>(null)
    val articleDetail: StateFlow<UiState<ArticleDto>?> = _articleDetail

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val articles = repo.getMyArticles(userId)
                val stats = repo.getArticleStats(userId)
                _state.value = UiState.Success(MediaContentData(articles, stats))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun loadArticleDetail(articleId: String) {
        viewModelScope.launch {
            _articleDetail.value = UiState.Loading
            try {
                val article = repo.getArticleById(articleId)
                _articleDetail.value = UiState.Success(article)
            } catch (e: Exception) {
                _articleDetail.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun clearDetail() {
        _articleDetail.value = null
    }
}
