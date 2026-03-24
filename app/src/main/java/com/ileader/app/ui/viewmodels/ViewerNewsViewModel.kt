package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.ileader.app.data.mock.ViewerMockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * News ViewModel — stays on MockData.
 * No articles table in the database yet.
 * TODO: Replace with Supabase when articles table is created.
 */
class ViewerNewsViewModel : ViewModel() {

    private val _articles = MutableStateFlow(ViewerMockData.newsArticles)
    val articles: StateFlow<List<ViewerMockData.NewsArticle>> = _articles

    val categories = ViewerMockData.newsCategories

    fun getArticle(id: String): ViewerMockData.NewsArticle? {
        return ViewerMockData.newsArticles.find { it.id == id }
    }
}
