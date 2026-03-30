package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.launch

class ArticleDetailViewModel : ViewModel() {
    private val repo = ViewerRepository()

    var state by mutableStateOf<UiState<ArticleDto>>(UiState.Loading)
        private set

    fun load(articleId: String) {
        viewModelScope.launch {
            state = UiState.Loading
            try {
                state = UiState.Success(repo.getArticleDetail(articleId))
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Ошибка загрузки статьи")
            }
        }
    }
}
