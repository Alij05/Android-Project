package com.example.sickimfy.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liked_tracks")
data class LikedTrackEntity(
    @PrimaryKey val trackId: String,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val audioUrl: String?,
    val likedAt: Long = System.currentTimeMillis()
)
