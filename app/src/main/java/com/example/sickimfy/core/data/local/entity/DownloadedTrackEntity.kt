package com.example.sickimfy.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_tracks")
data class DownloadedTrackEntity(
    @PrimaryKey val trackId: String,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val localFilePath: String,
    val durationSeconds: Int?,
    val downloadedAt: Long = System.currentTimeMillis()
)
