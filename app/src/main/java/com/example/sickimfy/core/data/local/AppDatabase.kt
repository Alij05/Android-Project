package com.example.sickimfy.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun likedTrackDao(): LikedTrackDao
    abstract fun downloadedTrackDao(): DownloadedTrackDao
    abstract fun offlineMessageDao(): OfflineMessageDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS offline_messages")
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS offline_messages (
                        messageId TEXT NOT NULL PRIMARY KEY,
                        senderId TEXT NOT NULL,
                        receiverId TEXT NOT NULL,
                        content TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        trackId TEXT,
                        trackTitle TEXT,
                        trackArtist TEXT,
                        trackCoverUrl TEXT
                    )"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_offline_messages_receiverId ON offline_messages(receiverId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_offline_messages_timestamp ON offline_messages(timestamp)")
            }
        }
    }
}
