package com.example.sickimfy.features.playlists.domain.model

import androidx.compose.ui.graphics.Color

enum class PlaylistType {
    INTERNATIONAL, DOMESTIC, USER
}

data class Playlist(
    val id: String,
    val title: String,
    val trackCount: Int,
    val type: PlaylistType,
    val gradientColors: List<Color>
)