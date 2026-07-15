package com.example.sickimfy.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sickimfy.core.data.local.dao.DownloadedTrackDao
import com.example.sickimfy.core.data.local.dao.LikedTrackDao
import com.example.sickimfy.core.data.local.dao.OfflineMessageDao
import com.example.sickimfy.core.data.local.dao.SearchHistoryDao
import com.example.sickimfy.core.data.local.entity.DownloadedTrackEntity
import com.example.sickimfy.core.data.local.entity.LikedTrackEntity
import com.example.sickimfy.core.data.local.entity.OfflineMessageEntity
import com.example.sickimfy.core.data.local.entity.SearchHistoryEntity

@Database(
    entities = [
        SearchHistoryEntity::class,
        LikedTrackEntity::class,
        DownloadedTrackEntity::class,
        OfflineMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun likedTrackDao(): LikedTrackDao
    abstract fun downloadedTrackDao(): DownloadedTrackDao
    abstract fun offlineMessageDao(): OfflineMessageDao
}
