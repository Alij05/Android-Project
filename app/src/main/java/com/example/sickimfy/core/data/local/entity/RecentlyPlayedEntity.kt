package com.example.sickimfy.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "recently_played", indices = [Index("playedAt")])
data class RecentlyPlayedEntity(
    @PrimaryKey val trackId: String,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val audioUrl: String?,
    val playedAt: Long = System.currentTimeMillis()
)
