package com.example.sickimfy.features.search.domain.repository

import com.example.sickimfy.features.home.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    suspend fun searchTracks(query: String): List<Track>
    fun observeHistory(): Flow<List<String>>
    suspend fun saveQuery(query: String)
    suspend fun deleteQuery(query: String)
    suspend fun clearHistory()
}
