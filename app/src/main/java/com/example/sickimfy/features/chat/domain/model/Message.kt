package com.example.sickimfy.features.chat.domain.model

import com.example.sickimfy.core.network.MessageStatus

data class Message(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.SENT,
    val isFromMe: Boolean = false,
    val trackId: String? = null,
    val trackTitle: String? = null,
    val trackArtist: String? = null,
    val trackCoverUrl: String? = null
)
