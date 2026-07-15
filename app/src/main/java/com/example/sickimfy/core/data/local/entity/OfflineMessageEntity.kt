package com.example.sickimfy.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "offline_messages",
    indices = [Index("conversationId")]
)
data class OfflineMessageEntity(
    @PrimaryKey val messageId: Int,
    val conversationId: Int,
    val senderId: Int,
    val content: String?,
    val sharedTrackId: Int?,
    val status: String,
    val createdAt: String
)
