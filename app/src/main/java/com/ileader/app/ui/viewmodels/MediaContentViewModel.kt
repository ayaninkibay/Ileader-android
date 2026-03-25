package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.ArticleInsertDto
import com.ileader.app.data.remote.dto.ArticleStatsDto
import com.ileader.app.data.remote.dto.ArticleUpdateDto
import com.ileader.app.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MediaContentData(
    val articles: List<ArticleDto>,
    val stats: ArticleStatsDto
)

sealed class ArticleSaveState {
    data object Idle : ArticleSaveState()
    data object Saving : ArticleSaveState()
    data object Success : ArticleSaveState()
    data class Error(val message: String) : ArticleSaveState()
}

class MediaContentViewModel : ViewModel() {
    private val repo = MediaRepository()

    private val _state = MutableStateFlow<UiState<MediaContentData>>(UiState.Loading)
    val state: StateFlow<UiState<MediaContentData>> = _state

    private val _articleDetail = MutableStateFlow<UiState<ArticleDto>?>(null)
    val articleDetail: StateFlow<UiState<ArticleDto>?> = _articleDetail

    private val _saveState = MutableStateFlow<ArticleSaveState>(ArticleSaveState.Idle)
    val saveState: StateFlow<ArticleSaveState> = _saveState

    private var currentUserId: String = ""

    fun load(userId: String) {
        currentUserId = userId
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

    fun createArticle(
        title: String,
        content: String?,
        excerpt: String?,
        category: String?,
        tags: List<String>?,
        status: String = "draft"
    ) {
        viewModelScope.launch {
            _saveState.value = ArticleSaveState.Saving
            try {
                repo.createArticle(
                    ArticleInsertDto(
                        authorId = currentUserId,
                        title = title,
                        content = content?.ifBlank { null },
                        excerpt = excerpt?.ifBlank { null },
                        category = category,
                        tags = tags?.filter { it.isNotBlank() }?.ifEmpty { null },
                        status = status
                    )
                )
                _saveState.value = ArticleSaveState.Success
                load(currentUserId)
            } catch (e: Exception) {
                _saveState.value = ArticleSaveState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    fun updateArticle(
        articleId: String,
        title: String?,
        content: String?,
        excerpt: String?,
        category: String?,
        tags: List<String>?,
        status: String? = null
    ) {
        viewModelScope.launch {
            _saveState.value = ArticleSaveState.Saving
            try {
                repo.updateArticle(
                    articleId,
                    ArticleUpdateDto(
                        title = title,
                        content = content,
                        excerpt = excerpt,
                        category = category,
                        tags = tags?.filter { it.isNotBlank() }?.ifEmpty { null },
                        status = status
                    )
                )
                _saveState.value = ArticleSaveState.Success
                load(currentUserId)
            } catch (e: Exception) {
                _saveState.value = ArticleSaveState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    fun deleteArticle(articleId: String) {
        viewModelScope.launch {
            _saveState.value = ArticleSaveState.Saving
            try {
                repo.deleteArticle(articleId)
                _saveState.value = ArticleSaveState.Success
                clearDetail()
                load(currentUserId)
            } catch (e: Exception) {
                _saveState.value = ArticleSaveState.Error(e.message ?: "Ошибка удаления")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = ArticleSaveState.Idle
    }
}
