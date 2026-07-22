package com.example.sickimfy.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sickimfy.core.data.local.dao.DownloadedTrackDao
import com.example.sickimfy.core.data.local.dao.LikedTrackDao
import com.example.sickimfy.core.data.local.dao.OfflineMessageDao
import com.example.sickimfy.core.data.local.dao.SearchHistoryDao
import com.example.sickimfy.core.data.local.dao.RecentlyPlayedDao
import com.example.sickimfy.core.data.local.dao.UserPlaylistDao
import com.example.sickimfy.core.data.local.entity.DownloadedTrackEntity
import com.example.sickimfy.core.data.local.entity.LikedTrackEntity
import com.example.sickimfy.core.data.local.entity.OfflineMessageEntity
import com.example.sickimfy.core.data.local.entity.SearchHistoryEntity
import com.example.sickimfy.core.data.local.entity.RecentlyPlayedEntity
import com.example.sickimfy.core.data.local.entity.UserPlaylistEntity
import com.example.sickimfy.core.data.local.entity.UserPlaylistTrackEntity

@Database(
    entities = [
        SearchHistoryEntity::class,
        LikedTrackEntity::class,
        DownloadedTrackEntity::class,
        OfflineMessageEntity::class,
        RecentlyPlayedEntity::class,
        UserPlaylistEntity::class,
        UserPlaylistTrackEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun likedTrackDao(): LikedTrackDao
    abstract fun downloadedTrackDao(): DownloadedTrackDao
    abstract fun offlineMessageDao(): OfflineMessageDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun userPlaylistDao(): UserPlaylistDao

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS recently_played (
                        trackId TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        artist TEXT NOT NULL,
                        imageUrl TEXT NOT NULL,
                        audioUrl TEXT,
                        playedAt INTEGER NOT NULL
                    )"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recently_played_playedAt ON recently_played(playedAt)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS user_playlists (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    createdAt INTEGER NOT NULL
                )""")
                db.execSQL("""CREATE TABLE IF NOT EXISTS user_playlist_tracks (
                    playlistId INTEGER NOT NULL,
                    trackId TEXT NOT NULL,
                    title TEXT NOT NULL,
                    artist TEXT NOT NULL,
                    imageUrl TEXT NOT NULL,
                    audioUrl TEXT,
                    addedAt INTEGER NOT NULL,
                    PRIMARY KEY(playlistId, trackId)
                )""")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_user_playlist_tracks_playlistId ON user_playlist_tracks(playlistId)")
            }
        }
    }
}
