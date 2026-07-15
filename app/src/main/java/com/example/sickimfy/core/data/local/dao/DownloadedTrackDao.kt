package com.example.sickimfy.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.sickimfy.core.data.local.entity.DownloadedTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedTrackDao {
    @Query("SELECT * FROM downloaded_tracks ORDER BY downloadedAt DESC")
    fun observeAll(): Flow<List<DownloadedTrackEntity>>

    @Query("SELECT * FROM downloaded_tracks WHERE trackId = :trackId LIMIT 1")
    suspend fun find(trackId: String): DownloadedTrackEntity?

    @Upsert
    suspend fun upsert(track: DownloadedTrackEntity)

    @Query("DELETE FROM downloaded_tracks WHERE trackId = :trackId")
    suspend fun delete(trackId: String)
}
