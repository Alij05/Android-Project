package com.example.sickimfy.features.playlists.domain.repository

import com.example.sickimfy.features.playlists.domain.model.Playlist

interface PlaylistsRepository {
    suspend fun getPlaylists(): List<Playlist>
    suspend fun getPlaylistTracks(playlistId: String): List<com.example.sickimfy.features.home.domain.model.Track>
}
