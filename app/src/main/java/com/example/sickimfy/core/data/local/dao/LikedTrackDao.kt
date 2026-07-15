package com.example.sickimfy.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.sickimfy.core.data.local.entity.LikedTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedTrackDao {
    @Query("SELECT * FROM liked_tracks ORDER BY likedAt DESC")
    fun observeAll(): Flow<List<LikedTrackEntity>>

    @Upsert
    suspend fun upsert(track: LikedTrackEntity)

    @Query("DELETE FROM liked_tracks WHERE trackId = :trackId")
    suspend fun delete(trackId: String)
}
