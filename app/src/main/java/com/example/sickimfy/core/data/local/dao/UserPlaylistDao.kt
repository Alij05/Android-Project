package com.example.sickimfy.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sickimfy.core.data.local.entity.UserPlaylistEntity
import com.example.sickimfy.core.data.local.entity.UserPlaylistTrackEntity
import kotlinx.coroutines.flow.Flow

data class UserPlaylistSummary(
    val id: Long,
    val title: String,
    val trackCount: Int
)

@Dao
interface UserPlaylistDao {
    @Query("""
        SELECT p.id, p.title, COUNT(t.trackId) AS trackCount
        FROM user_playlists p
        LEFT JOIN user_playlist_tracks t ON t.playlistId = p.id
        GROUP BY p.id, p.title
        ORDER BY p.createdAt DESC
    """)
    fun observePlaylists(): Flow<List<UserPlaylistSummary>>

    @Insert
    suspend fun createPlaylist(playlist: UserPlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrack(track: UserPlaylistTrackEntity)

    @Query("SELECT * FROM user_playlist_tracks WHERE playlistId = :playlistId ORDER BY addedAt DESC")
    fun observeTracks(playlistId: Long): Flow<List<UserPlaylistTrackEntity>>
}
