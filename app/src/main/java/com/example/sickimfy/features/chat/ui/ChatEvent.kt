package com.example.sickimfy.features.chat.ui

sealed interface ChatEvent {
    data class OnMessageInputChanged(val message: String) : ChatEvent
    data object OnSendMessage : ChatEvent
    data class OnShareTrack(
        val trackId: String,
        val trackTitle: String,
        val trackArtist: String,
        val trackCoverUrl: String
    ) : ChatEvent
    data class OnMessageRead(val messageId: String) : ChatEvent
    data object OnRetryConnection : ChatEvent
    data class OnPlayTrack(
        val trackId: String,
        val title: String,
        val artist: String,
        val coverUrl: String
    ) : ChatEvent
}
