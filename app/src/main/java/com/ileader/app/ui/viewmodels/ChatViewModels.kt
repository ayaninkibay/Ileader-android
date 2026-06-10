package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ConversationDto
import com.ileader.app.data.remote.dto.MessageDto
import com.ileader.app.data.repository.ChatRepository
import com.ileader.app.data.util.Alerts
import com.ileader.app.data.util.AppLogger
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecordOrNull
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ConversationsListViewModel : ViewModel() {
    private val repo = ChatRepository()

    private val _state = MutableStateFlow<UiState<List<ConversationDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ConversationDto>>> = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                _state.value = UiState.Success(repo.getConversations(userId))
            } catch (e: Exception) {
                AppLogger.e("ConversationsListVM.load failed", e)
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
}

class ChatViewModel : ViewModel() {
    private val repo = ChatRepository()

    private val _state = MutableStateFlow<UiState<List<MessageDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<MessageDto>>> = _state.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    private var convId: String = ""
    private var myUserId: String = ""
    private var channel: RealtimeChannel? = null
    private var pollJob: Job? = null

    fun load(conversationId: String, userId: String) {
        convId = conversationId
        myUserId = userId
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                _state.value = UiState.Success(repo.getMessages(conversationId))
                markReadQuietly()
            } catch (e: Exception) {
                AppLogger.e("ChatVM.load failed", e)
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки сообщений")
            }
        }
        subscribeToIncoming()
    }

    /**
     * Live updates: Supabase Realtime INSERT-события по messages этого диалога.
     * Если websocket не поднялся (сеть/прокси) — фолбэк на поллинг раз в 5с,
     * чтобы чат всё равно оставался «живым».
     */
    private fun subscribeToIncoming() {
        if (channel != null || pollJob != null) return
        viewModelScope.launch {
            try {
                val ch = SupabaseModule.client.channel("chat-$convId")
                ch.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "messages"
                    filter("conversation_id", FilterOperator.EQ, convId)
                }
                    .onEach { action ->
                        action.decodeRecordOrNull<MessageDto>()?.let(::appendIncoming)
                    }
                    .launchIn(viewModelScope)
                ch.subscribe()
                channel = ch
            } catch (e: Exception) {
                AppLogger.w("ChatVM realtime unavailable, falling back to polling: ${e.message}", e)
                startPolling()
            }
        }
    }

    private fun startPolling() {
        if (pollJob != null) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                try {
                    val fresh = repo.getMessages(convId)
                    val current = (_state.value as? UiState.Success)?.data
                    if (current == null || fresh.size != current.size) {
                        _state.value = UiState.Success(fresh)
                        markReadQuietly()
                    }
                } catch (_: Exception) {
                    // transient network error — next tick retries
                }
            }
        }
    }

    private fun appendIncoming(msg: MessageDto) {
        val current = (_state.value as? UiState.Success)?.data ?: return
        if (current.any { it.id == msg.id }) return // own sends arrive twice
        _state.value = UiState.Success(current + msg)
        if (msg.senderId != myUserId) markReadQuietly()
    }

    private fun markReadQuietly() {
        viewModelScope.launch {
            try {
                repo.markRead(convId, myUserId)
            } catch (e: Exception) {
                AppLogger.w("ChatVM.markRead failed: ${e.message}", e)
            }
        }
    }

    fun send(content: String) {
        if (content.isBlank() || convId.isEmpty()) return
        viewModelScope.launch {
            _sending.value = true
            try {
                val msg = repo.sendMessage(convId, myUserId, content.trim())
                val current = (_state.value as? UiState.Success)?.data ?: emptyList()
                if (current.none { it.id == msg.id }) {
                    _state.value = UiState.Success(current + msg)
                }
            } catch (e: Exception) {
                AppLogger.w("ChatVM.send: ${e.message}", e)
                Alerts.error("Не удалось отправить сообщение")
                // keep state
            } finally {
                _sending.value = false
            }
        }
    }

    override fun onCleared() {
        // viewModelScope уже отменён к моменту onCleared — чистим канал в
        // отдельном scope, иначе websocket-подписка живёт до конца процесса.
        val ch = channel ?: return
        channel = null
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { SupabaseModule.client.realtime.removeChannel(ch) }
        }
    }
}

class StartConversationViewModel : ViewModel() {
    private val repo = ChatRepository()

    private val _state = MutableStateFlow<UiState<String>>(UiState.Loading)
    val state: StateFlow<UiState<String>> = _state.asStateFlow()

    fun start(myUserId: String, otherUserId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val id = repo.createConversation(listOf(myUserId, otherUserId))
                _state.value = UiState.Success(id)
            } catch (e: Exception) {
                AppLogger.e("StartConversationVM.start failed", e)
                Alerts.error("Не удалось начать диалог")
                _state.value = UiState.Error(e.message ?: "Не удалось создать диалог")
            }
        }
    }
}
