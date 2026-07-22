package com.example.sickimfy.features.chat.domain.usecase

import com.example.sickimfy.features.chat.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(
        conversationId: Int,
        receiverId: String,
        content: String,
        trackId: String? = null,
        trackTitle: String? = null,
        trackArtist: String? = null,
        trackCoverUrl: String? = null
    ) {
        repository.sendMessage(
            conversationId = conversationId,
            receiverId = receiverId,
            content = content,
            trackId = trackId,
            trackTitle = trackTitle,
            trackArtist = trackArtist,
            trackCoverUrl = trackCoverUrl
        )
    }
}
