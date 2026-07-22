package com.example.sickimfy.features.search.data.repository

import com.example.sickimfy.core.data.local.dao.SearchHistoryDao
import com.example.sickimfy.core.data.local.entity.SearchHistoryEntity
import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import com.example.sickimfy.core.network.SickimfyApi
import com.example.sickimfy.core.network.dto.toDomain
import com.example.sickimfy.features.home.domain.model.Track
import com.example.sickimfy.features.search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val api: SickimfyApi,
    private val historyDao: SearchHistoryDao,
    private val preferences: UserPreferencesDataStore
) : SearchRepository {
    override suspend fun searchTracks(query: String): List<Track> {
        val apiBaseUrl = preferences.preferences.first().apiBaseUrl
        return api.getTracks(query = query, limit = 50).items.map { it.toDomain(apiBaseUrl) }
    }

    override fun observeHistory(): Flow<List<String>> =
        historyDao.observeRecent().map { items -> items.map { it.query } }

    override suspend fun saveQuery(query: String) {
        historyDao.insert(SearchHistoryEntity(query = query.trim()))
    }

    override suspend fun deleteQuery(query: String) = historyDao.delete(query)
    override suspend fun clearHistory() = historyDao.clear()
}
