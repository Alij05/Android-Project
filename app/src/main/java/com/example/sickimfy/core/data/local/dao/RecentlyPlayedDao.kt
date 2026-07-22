package com.example.sickimfy.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sickimfy.core.data.local.entity.RecentlyPlayedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {
    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT 50")
    fun observeAll(): Flow<List<RecentlyPlayedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: RecentlyPlayedEntity)

    @Query("DELETE FROM recently_played WHERE trackId = :trackId")
    suspend fun delete(trackId: String)
}
