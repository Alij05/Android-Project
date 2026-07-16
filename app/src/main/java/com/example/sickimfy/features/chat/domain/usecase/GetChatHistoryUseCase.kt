package com.example.sickimfy.features.chat.domain.usecase

import com.example.sickimfy.features.chat.domain.model.Message
import com.example.sickimfy.features.chat.domain.repository.ChatRepository
import javax.inject.Inject

class GetChatHistoryUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(userId: String, before: Long? = null, limit: Int = 50): List<Message> {
        return repository.loadHistory(userId, before, limit)
    }
}
