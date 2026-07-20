package com.example.sickimfy.features.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.BuildConfig
import com.example.sickimfy.core.data.local.dao.DownloadedTrackDao
import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import com.example.sickimfy.core.network.SickimfyApi
import com.example.sickimfy.core.playback.PlaybackManager
import com.example.sickimfy.features.chat.data.remote.ChatWebSocketClient
import com.example.sickimfy.features.chat.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val chatWebSocketClient: ChatWebSocketClient,
    private val preferences: UserPreferencesDataStore,
    private val playbackManager: PlaybackManager,
    private val downloadedTrackDao: DownloadedTrackDao,
    private val api: SickimfyApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentUserId: String = ""
    private var conversationId: Int = -1

    fun initialize(conversationId: Int, userId: String) {
        this.conversationId = conversationId
        currentUserId = userId
        loadMessages()
        observeTyping()
        observeConnection()

        viewModelScope.launch {
            val token = preferences.preferences.first().accessToken.orEmpty()
            chatWebSocketClient.connect(BuildConfig.API_BASE_URL, token, conversationId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatWebSocketClient.disconnect()
    }

    suspend fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.OnMessageInputChanged -> {
                _uiState.update { it.copy(messageInput = event.message) }
                if (event.message.isNotEmpty()) {
                    chatRepository.sendTypingIndicator(currentUserId, true)
                }
            }
            ChatEvent.OnSendMessage -> {
                val message = _uiState.value.messageInput.trim()
                if (message.isNotEmpty()) {
                    sendMessage(message)
                }
            }
            is ChatEvent.OnShareTrack -> {
                sendTrackMessage(
                    trackId = event.trackId,
                    trackTitle = event.trackTitle,
                    trackArtist = event.trackArtist,
                    trackCoverUrl = event.trackCoverUrl
                )
            }
            is ChatEvent.OnMessageRead -> {
                viewModelScope.launch {
                    chatRepository.sendReadReceipt(event.messageId)
                }
            }
            ChatEvent.OnRetryConnection -> {
                loadMessages()
            }
            is ChatEvent.OnPlayTrack -> {
                viewModelScope.launch {
                    runCatching {
                        val downloaded = downloadedTrackDao.find(event.trackId)
                        if (downloaded != null && java.io.File(downloaded.localFilePath).exists()) {
                            downloaded.localFilePath
                        } else {
                            // Fetch from server to get audio URL
                            api.getTrack(event.trackId.toInt()).audioUrl
                        }
                    }.onSuccess { playUrl ->
                        playbackManager.play(
                            trackId = event.trackId,
                            title = event.title,
                            artist = event.artist,
                            coverUrl = event.coverUrl,
                            audioUrl = playUrl
                        )
                    }
                }
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                chatRepository.observeMessages(currentUserId).collect { messages ->
                    _uiState.update {
                        it.copy(
                            messages = messages,
                            isLoading = false
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load messages") }
            }
        }
    }

    private fun observeTyping() {
        viewModelScope.launch {
            chatRepository.observeTyping(currentUserId).collect { isTyping ->
                _uiState.update { it.copy(isOtherUserTyping = isTyping) }
            }
        }
    }

    private fun observeConnection() {
        viewModelScope.launch {
            chatRepository.observeConnectionState().collect { isConnected ->
                _uiState.update { it.copy(isConnected = isConnected) }
            }
        }
    }

    private fun sendMessage(content: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(messageInput = "") }
            chatRepository.sendMessage(
                receiverId = currentUserId,
                content = content
            )
        }
    }

    private fun sendTrackMessage(
        trackId: String,
        trackTitle: String,
        trackArtist: String,
        trackCoverUrl: String
    ) {
        viewModelScope.launch {
            chatRepository.sendMessage(
                receiverId = currentUserId,
                content = "🎵 $trackTitle - $trackArtist",
                trackId = trackId,
                trackTitle = trackTitle,
                trackArtist = trackArtist,
                trackCoverUrl = trackCoverUrl
            )
        }
    }
}
