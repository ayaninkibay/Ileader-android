package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.repository.AvatarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AvatarViewModel : ViewModel() {
    private val repo = AvatarRepository()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadedUrl = MutableStateFlow<String?>(null)
    val uploadedUrl: StateFlow<String?> = _uploadedUrl.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun uploadAvatar(userId: String, imageBytes: ByteArray) {
        viewModelScope.launch {
            _isUploading.value = true
            _error.value = null
            try {
                val url = repo.uploadAvatar(userId, imageBytes)
                _uploadedUrl.value = url
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка загрузки"
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
    fun clearUploadedUrl() { _uploadedUrl.value = null }
}
