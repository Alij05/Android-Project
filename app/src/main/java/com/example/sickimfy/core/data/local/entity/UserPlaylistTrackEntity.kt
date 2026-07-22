package com.example.sickimfy.core.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "user_playlist_tracks",
    primaryKeys = ["playlistId", "trackId"]
)
data class UserPlaylistTrackEntity(
    val playlistId: Long,
    val trackId: String,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val audioUrl: String?,
    val addedAt: Long = System.currentTimeMillis()
)
