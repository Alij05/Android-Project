package com.example.sickimfy.features.home.data.repository

import com.example.sickimfy.core.network.SickimfyApi
import com.example.sickimfy.core.network.dto.PlaylistSummaryDto
import com.example.sickimfy.core.network.dto.toDomain
import com.example.sickimfy.features.home.domain.model.Track
import com.example.sickimfy.features.home.domain.repository.HomeFeed
import com.example.sickimfy.features.home.domain.repository.HomeRepository
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: SickimfyApi
) : HomeRepository {
    override suspend fun getHomeFeed(): HomeFeed {
        val response = api.getHome()
        val playlistCards = response.publicPlaylists.map { it.toTrackCard() }
        return HomeFeed(
            featured = response.featuredTracks.map { it.toDomain() },
            popular = response.featuredTracks.ifEmpty { response.latestTracks }.map { it.toDomain() },
            latest = response.latestTracks.map { it.toDomain() },
            globalPlaylists = response.publicPlaylists
                .filterNot { it.title.containsPersianText() }
                .map { it.toTrackCard() },
            localPlaylists = response.publicPlaylists
                .filter { it.title.containsPersianText() }
                .map { it.toTrackCard() }
        ).let { feed ->
            if (feed.globalPlaylists.isEmpty() && feed.localPlaylists.isEmpty()) {
                feed.copy(globalPlaylists = playlistCards)
            } else feed
        }
    }

    private fun PlaylistSummaryDto.toTrackCard() = Track(
        id = "playlist-$id",
        title = title,
        artist = owner.displayName,
        imageUrl = coverImageUrl.orEmpty(),
        duration = "$trackCount",
        albumName = description
    )

    private fun String.containsPersianText() = any { it in '\u0600'..'\u06FF' }
}
