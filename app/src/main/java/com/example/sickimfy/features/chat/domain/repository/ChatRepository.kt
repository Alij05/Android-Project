package com.example.sickimfy.features.chat.domain.repository

import com.example.sickimfy.features.chat.domain.model.Message
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(userId: String): Flow<List<Message>>
    suspend fun sendMessage(receiverId: String, content: String, trackId: String? = null, trackTitle: String? = null, trackArtist: String? = null, trackCoverUrl: String? = null)
    suspend fun loadHistory(userId: String, before: Long? = null, limit: Int = 50): List<Message>
    fun observeTyping(userId: String): Flow<Boolean>
    fun observeConnectionState(): Flow<Boolean>
    suspend fun sendReadReceipt(messageId: String)
    suspend fun sendTypingIndicator(userId: String, isTyping: Boolean)}
