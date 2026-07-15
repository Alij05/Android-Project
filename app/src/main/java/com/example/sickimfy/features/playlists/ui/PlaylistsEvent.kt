package com.example.sickimfy.features.playlists.ui

import com.example.sickimfy.features.playlists.domain.model.Playlist

sealed interface PlaylistsEvent {
    data class OnPlaylistSelected(val playlist: Playlist) : PlaylistsEvent
    object OnCreatePlaylistClick : PlaylistsEvent
    object OnRetryClick : PlaylistsEvent
}
