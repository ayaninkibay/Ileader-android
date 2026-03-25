package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ViewerNewsViewModel : ViewModel() {
    private val repo = ViewerRepository()

    private val _state = MutableStateFlow<UiState<List<ArticleDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ArticleDto>>> = _state

    private val _articleDetail = MutableStateFlow<UiState<ArticleDto>?>(null)
    val articleDetail: StateFlow<UiState<ArticleDto>?> = _articleDetail

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val articles = repo.getPublishedArticles()
                _state.value = UiState.Success(articles)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun loadArticle(articleId: String) {
        viewModelScope.launch {
            _articleDetail.value = UiState.Loading
            try {
                val article = repo.getArticleDetail(articleId)
                _articleDetail.value = UiState.Success(article)
            } catch (e: Exception) {
                _articleDetail.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}
