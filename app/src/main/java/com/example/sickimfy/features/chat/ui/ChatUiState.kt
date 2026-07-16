package com.example.sickimfy.features.chat.ui

import com.example.sickimfy.features.chat.domain.model.Message

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val messageInput: String = "",
    val isLoading: Boolean = false,
    val isOtherUserTyping: Boolean = false,
    val isConnected: Boolean = true,
    val error: String? = null
)
