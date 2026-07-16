package com.example.sickimfy.features.chat.data.remote

import com.example.sickimfy.core.network.WebSocketManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatWebSocketClient @Inject constructor(
    private val webSocketManager: WebSocketManager
) {
    fun connect(baseUrl: String, token: String, conversationId: Int) {
        webSocketManager.connect(baseUrl, token, conversationId)
    }

    fun disconnect() {
        webSocketManager.disconnect()
    }
}
