package com.example.sickimfy.features.chat.data.repository

import com.example.sickimfy.core.data.local.dao.OfflineMessageDao
import com.example.sickimfy.core.data.local.entity.OfflineMessageEntity
import com.example.sickimfy.core.network.ConnectionState
import com.example.sickimfy.core.network.MessageStatus
import com.example.sickimfy.core.network.WebSocketManager
import com.example.sickimfy.features.chat.domain.model.Message
import com.example.sickimfy.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val webSocketManager: WebSocketManager,
    private val offlineMessageDao: OfflineMessageDao
) : ChatRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val typingUsers = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    init {
        scope.launch {
            webSocketManager.typingEvents.collect { event ->
                typingUsers.value = typingUsers.value.toMutableMap().apply {
                    put(event.userId, event.isTyping)
                }
            }
        }
    }

    override fun observeMessages(userId: String): Flow<List<Message>> {
        return combine(
            offlineMessageDao.observeConversation(userId),
            webSocketManager.incomingMessages
        ) { localMessages, incoming ->
            val localMapped = localMessages.map { it.toDomain() }

            val incomingMessage = if (incoming.senderId == userId || incoming.receiverId == userId) {
                listOf(incoming.toDomain(userId))
            } else emptyList()

            (localMapped + incomingMessage).sortedBy { it.timestamp }
        }
    }

    override suspend fun sendMessage(
        receiverId: String,
        content: String,
        trackId: String?,
        trackTitle: String?,
        trackArtist: String?,
        trackCoverUrl: String?
    ) {
        val tempId = "temp_${System.currentTimeMillis()}"

        offlineMessageDao.upsert(
            OfflineMessageEntity(
                messageId = tempId,
                senderId = "me",
                receiverId = receiverId,
                content = content,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENDING.name,
                trackId = trackId,
                trackTitle = trackTitle,
                trackArtist = trackArtist,
                trackCoverUrl = trackCoverUrl
            )
        )

        webSocketManager.sendMessage(
            receiverId = receiverId,
            content = content,
            trackId = trackId,
            trackTitle = trackTitle,
            trackArtist = trackArtist,
            trackCoverUrl = trackCoverUrl
        )

        offlineMessageDao.updateStatus(tempId, MessageStatus.SENT.name)
    }

    override suspend fun loadHistory(userId: String, before: Long?, limit: Int): List<Message> {
        return offlineMessageDao.getConversation(userId, before, limit).map { it.toDomain() }
    }

    override fun observeTyping(userId: String): Flow<Boolean> {
        return typingUsers.map { it[userId] == true }
    }

    override fun observeConnectionState(): Flow<Boolean> {
        return webSocketManager.connectionState.map { it == ConnectionState.CONNECTED }
    }

    override suspend fun sendReadReceipt(messageId: String) {
        webSocketManager.sendReadReceipt(messageId)
        offlineMessageDao.updateStatus(messageId, MessageStatus.READ.name)
    }
    override suspend fun sendTypingIndicator(userId: String, isTyping: Boolean) {
        // موقتاً خالی یا در صورت وجود، ارسال به وب‌ساکت
    }

    private fun OfflineMessageEntity.toDomain(): Message {
        return Message(
            id = messageId,
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            timestamp = timestamp,
            status = try { MessageStatus.valueOf(status) } catch (_: Exception) { MessageStatus.SENT },
            isFromMe = senderId == "me",
            trackId = trackId,
            trackTitle = trackTitle,
            trackArtist = trackArtist,
            trackCoverUrl = trackCoverUrl
        )
    }

    private fun com.example.sickimfy.core.network.ChatMessageDto.toDomain(currentUserId: String): Message {
        return Message(
            id = id,
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            timestamp = timestamp,
            status = status,
            isFromMe = senderId == currentUserId,
            trackId = trackId,
            trackTitle = trackTitle,
            trackArtist = trackArtist,
            trackCoverUrl = trackCoverUrl
        )
    }
}
