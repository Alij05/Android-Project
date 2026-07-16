package com.example.sickimfy.features.chat.data.remote

import com.example.sickimfy.core.network.WebSocketManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatWebSocketClient @Inject constructor(
    private val webSocketManager: WebSocketManager
) {
    fun connect(baseUrl: String, token: String) {
        webSocketManager.connect(baseUrl, token)
    }

    fun disconnect() {
        webSocketManager.disconnect()
    }
}
