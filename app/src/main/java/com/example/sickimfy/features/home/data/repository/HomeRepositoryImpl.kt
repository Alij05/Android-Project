package com.example.sickimfy.features.home.data.repository

import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import com.example.sickimfy.core.network.SickimfyApi
import com.example.sickimfy.core.network.dto.PlaylistSummaryDto
import com.example.sickimfy.core.network.dto.toDomain
import com.example.sickimfy.core.network.resolveMediaUrl
import com.example.sickimfy.features.home.domain.model.Track
import com.example.sickimfy.features.home.domain.repository.HomeFeed
import com.example.sickimfy.features.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: SickimfyApi,
    private val preferences: UserPreferencesDataStore
) : HomeRepository {
    override suspend fun getHomeFeed(): HomeFeed {
        val response = api.getHome()
        val apiBaseUrl = preferences.preferences.first().apiBaseUrl
        val playlistCards = response.publicPlaylists.map { it.toTrackCard(apiBaseUrl) }
        return HomeFeed(
            featured = response.featuredTracks.map { it.toDomain(apiBaseUrl) },
            popular = response.featuredTracks.ifEmpty { response.latestTracks }.map { it.toDomain(apiBaseUrl) },
            latest = response.latestTracks.map { it.toDomain(apiBaseUrl) },
            globalPlaylists = response.publicPlaylists
                .filterNot { it.title.containsPersianText() }
                .map { it.toTrackCard(apiBaseUrl) },
            localPlaylists = response.publicPlaylists
                .filter { it.title.containsPersianText() }
                .map { it.toTrackCard(apiBaseUrl) }
        ).let { feed ->
            if (feed.globalPlaylists.isEmpty() && feed.localPlaylists.isEmpty()) {
                feed.copy(globalPlaylists = playlistCards)
            } else feed
        }
    }

    private fun PlaylistSummaryDto.toTrackCard(apiBaseUrl: String) = Track(
        id = "playlist-$id",
        title = title,
        artist = owner.displayName,
        imageUrl = resolveMediaUrl(coverImageUrl, apiBaseUrl).orEmpty(),
        duration = "$trackCount",
        albumName = description
    )

    private fun String.containsPersianText() = any { it in '\u0600'..'\u06FF' }
}
