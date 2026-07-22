package com.example.sickimfy.core.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING
}

@Singleton
class WebSocketManager @Inject constructor() {
    private var baseUrl: String = ""
    private var authToken: String = ""
    private var reconnectAttempt: Int = 0
    private var otherUserId: String = ""

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<ChatMessageDto>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<ChatMessageDto> = _incomingMessages.asSharedFlow()

    private val _typingEvents = MutableSharedFlow<TypingEvent>(extraBufferCapacity = 16)
    val typingEvents: SharedFlow<TypingEvent> = _typingEvents.asSharedFlow()

    private val _messageStatuses = MutableSharedFlow<MessageStatusUpdate>(extraBufferCapacity = 16)
    val messageStatuses: SharedFlow<MessageStatusUpdate> = _messageStatuses.asSharedFlow()

    private var conversationId: Int? = null

    fun connect(baseUrl: String, authToken: String, conversationId: Int, otherUserId: String) {
        this.baseUrl = baseUrl
        this.authToken = authToken
        this.conversationId = conversationId
        this.otherUserId = otherUserId
        reconnectAttempt = 0
        doConnect()
    }

    private fun doConnect() {
        _connectionState.value = ConnectionState.CONNECTING

        val path = conversationId?.let { "ws/conversations/$it" } ?: "ws/chat"
        val request = Request.Builder()
            .url("${baseUrl.replace("http", "ws")}$path?token=$authToken")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.CONNECTED
                reconnectAttempt = 0
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch {
                    try {
                        val json = JSONObject(text)
                        val type = json.optString("type", "")

                        when (type.uppercase()) {
                            "MESSAGE" -> {
                                val message = json.optJSONObject("message") ?: return@launch
                                val sharedTrack = message.optJSONObject("sharedTrack")
                                val senderId = message.optInt("senderId").toString()
                                val dto = ChatMessageDto(
                                    id = message.optInt("id").toString(),
                                    senderId = if (senderId == otherUserId) otherUserId else "me",
                                    receiverId = if (senderId == otherUserId) "me" else otherUserId,
                                    content = message.optStringOrNull("content").orEmpty(),
                                    timestamp = System.currentTimeMillis(),
                                    status = if (senderId == otherUserId) MessageStatus.DELIVERED else MessageStatus.SENT,
                                    trackId = sharedTrack?.optInt("id")?.takeIf { it > 0 }?.toString(),
                                    trackTitle = sharedTrack?.optStringOrNull("title"),
                                    trackArtist = sharedTrack?.optStringOrNull("artistName"),
                                    trackCoverUrl = sharedTrack?.optStringOrNull("coverImageUrl")
                                )
                                _incomingMessages.emit(dto)
                            }
                            "TYPING" -> {
                                val event = TypingEvent(
                                    userId = json.optInt("senderId").toString(),
                                    isTyping = json.optBoolean("isTyping", false)
                                )
                                _typingEvents.emit(event)
                            }
                            "STATUS" -> {
                                val update = MessageStatusUpdate(
                                    messageId = json.optInt("messageId").toString(),
                                    status = runCatching { MessageStatus.valueOf(json.optString("status", "SENT")) }
                                        .getOrDefault(MessageStatus.SENT)
                                )
                                _messageStatuses.emit(update)
                            }
                        }
                    } catch (_: Exception) {
                        // Ignore malformed messages
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.RECONNECTING
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        reconnectAttempt++
        val delayMs = minOf(1000L * reconnectAttempt, 30_000L)
        scope.launch {
            kotlinx.coroutines.delay(delayMs)
            doConnect()
        }
    }

    fun sendMessage(
        receiverId: String,
        content: String,
        trackId: String? = null,
        trackTitle: String? = null,
        trackArtist: String? = null,
        trackCoverUrl: String? = null
    ) {
        val json = JSONObject().apply {
            put("type", "message")
            put("receiverId", receiverId)
            put("content", content)
            trackId?.let { put("trackId", it) }
            trackTitle?.let { put("trackTitle", it) }
            trackArtist?.let { put("trackArtist", it) }
            trackCoverUrl?.let { put("trackCoverUrl", it) }
        }
        webSocket?.send(json.toString())
    }

    fun sendTypingIndicator(receiverId: String, isTyping: Boolean) {
        val json = JSONObject().apply {
            put("type", "TYPING")
            put("isTyping", isTyping)
        }
        webSocket?.send(json.toString())
    }

    fun sendReadReceipt(messageId: String) {
        val json = JSONObject().apply {
            put("type", "READ_RECEIPT")
            put("messageId", messageId.toIntOrNull())
        }
        webSocket?.send(json.toString())
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}

private fun JSONObject.optStringOrNull(name: String): String? =
    if (isNull(name)) null else optString(name).takeIf { it.isNotBlank() }

data class ChatMessageDto(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.SENT,
    val trackId: String? = null,
    val trackTitle: String? = null,
    val trackArtist: String? = null,
    val trackCoverUrl: String? = null
)

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ
}

data class TypingEvent(
    val userId: String,
    val isTyping: Boolean
)

data class MessageStatusUpdate(
    val messageId: String,
    val status: MessageStatus
)
