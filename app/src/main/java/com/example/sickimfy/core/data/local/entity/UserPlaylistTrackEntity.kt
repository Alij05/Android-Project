package com.example.sickimfy.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "user_playlist_tracks",
    primaryKeys = ["playlistId", "trackId"],
    indices = [Index("playlistId")]
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
