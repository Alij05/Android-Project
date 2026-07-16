package com.example.sickimfy.features.chat.data.remote.dto

data class MessageDto(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long,
    val status: String,
    val trackId: String? = null,
    val trackTitle: String? = null,
    val trackArtist: String? = null,
    val trackCoverUrl: String? = null
)
