package com.example.sickimfy.features.playlists.data.repository

import androidx.compose.ui.graphics.Color
import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import com.example.sickimfy.core.network.SickimfyApi
import com.example.sickimfy.core.network.dto.PlaylistSummaryDto
import com.example.sickimfy.features.playlists.domain.model.Playlist
import com.example.sickimfy.features.playlists.domain.model.PlaylistType
import com.example.sickimfy.core.network.dto.toDomain
import com.example.sickimfy.features.playlists.domain.repository.PlaylistsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PlaylistsRepositoryImpl @Inject constructor(
    private val api: SickimfyApi,
    private val preferences: UserPreferencesDataStore
) : PlaylistsRepository {
    override suspend fun getPlaylists(): List<Playlist> {
        val public = api.getPublicPlaylists().map { it.toDomain(publicType(it.title)) }
        val mine = if (preferences.accessToken().isNullOrBlank()) {
            emptyList()
        } else {
            api.getMyPlaylists().map { it.toDomain(PlaylistType.USER) }
        }
        return (public + mine).distinctBy { it.id }
    }

    override suspend fun getPlaylistTracks(playlistId: String): List<com.example.sickimfy.features.home.domain.model.Track> {
        val details = api.getPlaylistDetails(playlistId.toInt())
        val apiBaseUrl = preferences.preferences.first().apiBaseUrl
        return details.tracks.map { it.toDomain(apiBaseUrl) }
    }

    private fun PlaylistSummaryDto.toDomain(type: PlaylistType) = Playlist(
        id = id.toString(),
        title = title,
        trackCount = trackCount,
        type = type,
        gradientColors = gradients[id.mod(gradients.size)]
    )

    private fun publicType(title: String) =
        if (title.any { it in '\u0600'..'\u06FF' }) PlaylistType.DOMESTIC else PlaylistType.INTERNATIONAL

    private companion object {
        val gradients = listOf(
            listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
            listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
            listOf(Color(0xFFF12711), Color(0xFFF5AF19)),
            listOf(Color(0xFF396AFC), Color(0xFF2948FF))
        )
    }
}
