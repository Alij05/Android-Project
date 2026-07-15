package com.example.sickimfy.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.sickimfy.core.data.local.entity.OfflineMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineMessageDao {
    @Query("SELECT * FROM offline_messages WHERE conversationId = :conversationId ORDER BY messageId")
    fun observeConversation(conversationId: Int): Flow<List<OfflineMessageEntity>>

    @Upsert
    suspend fun upsertAll(messages: List<OfflineMessageEntity>)

    @Query("DELETE FROM offline_messages WHERE conversationId = :conversationId")
    suspend fun clearConversation(conversationId: Int)
}
