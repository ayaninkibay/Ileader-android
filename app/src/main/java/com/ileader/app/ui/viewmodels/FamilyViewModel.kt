package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.FamilyLinkDto
import com.ileader.app.data.remote.dto.ParentalApprovalDto
import com.ileader.app.data.repository.FamilyRepository
import kotlinx.coroutines.launch

data class FamilyData(
    val links: List<FamilyLinkDto>,
    val pendingApprovals: List<ParentalApprovalDto>
) {
    fun childLinks(userId: String) = links.filter { it.parentId == userId }
    fun parentLinks(userId: String) = links.filter { it.childId == userId }
}

class FamilyViewModel : ViewModel() {
    private val repo = FamilyRepository()

    var state by mutableStateOf<UiState<FamilyData>>(UiState.Loading)
        private set

    var actionState by mutableStateOf<UiState<Unit>?>(null)
        private set

    fun load(userId: String) {
        viewModelScope.launch {
            state = UiState.Loading
            try {
                val links = repo.getFamilyLinks(userId)
                val approvals = repo.getPendingApprovals(userId)
                state = UiState.Success(FamilyData(links, approvals))
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun linkChild(parentId: String, childEmail: String) {
        viewModelScope.launch {
            actionState = UiState.Loading
            try {
                val child = repo.findChildByEmail(childEmail)
                    ?: throw Exception("Пользователь с email $childEmail не найден")
                val childId = child.id ?: throw Exception("ID не найден")
                repo.createFamilyLink(parentId, childId)
                actionState = UiState.Success(Unit)
                load(parentId)
            } catch (e: Exception) {
                actionState = UiState.Error(e.message ?: "Ошибка привязки")
            }
        }
    }

    fun confirmLink(linkId: String, userId: String) {
        viewModelScope.launch {
            try {
                repo.confirmFamilyLink(linkId)
                load(userId)
            } catch (e: Exception) {
                actionState = UiState.Error(e.message ?: "Ошибка подтверждения")
            }
        }
    }

    fun removeLink(linkId: String, userId: String) {
        viewModelScope.launch {
            try {
                repo.removeFamilyLink(linkId)
                load(userId)
            } catch (e: Exception) {
                actionState = UiState.Error(e.message ?: "Ошибка удаления")
            }
        }
    }

    fun respondToApproval(approvalId: String, approved: Boolean, userId: String, comment: String? = null) {
        viewModelScope.launch {
            try {
                repo.respondToApproval(approvalId, approved, comment)
                load(userId)
            } catch (e: Exception) {
                actionState = UiState.Error(e.message ?: "Ошибка")
            }
        }
    }

    fun clearAction() {
        actionState = null
    }
}
