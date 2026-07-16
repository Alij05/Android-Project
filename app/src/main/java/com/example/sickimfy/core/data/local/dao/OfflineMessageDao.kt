package com.example.sickimfy.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.sickimfy.core.data.local.entity.OfflineMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineMessageDao {
    @Query("SELECT * FROM offline_messages WHERE (senderId = :userId OR receiverId = :userId) ORDER BY timestamp ASC")
    fun observeConversation(userId: String): Flow<List<OfflineMessageEntity>>

    @Query("SELECT * FROM offline_messages WHERE (senderId = :userId OR receiverId = :userId) AND (:before IS NULL OR timestamp < :before) ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getConversation(userId: String, before: Long? = null, limit: Int = 50): List<OfflineMessageEntity>

    @Upsert
    suspend fun upsert(message: OfflineMessageEntity)

    @Upsert
    suspend fun upsertAll(messages: List<OfflineMessageEntity>)

    @Query("UPDATE offline_messages SET status = :status WHERE messageId = :messageId")
    suspend fun updateStatus(messageId: String, status: String)

    @Query("DELETE FROM offline_messages WHERE (senderId = :userId OR receiverId = :userId)")
    suspend fun clearConversation(userId: String)
}
