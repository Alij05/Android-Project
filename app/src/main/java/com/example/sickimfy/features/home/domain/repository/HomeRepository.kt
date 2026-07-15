package com.example.sickimfy.features.home.domain.repository

import com.example.sickimfy.features.home.domain.model.Track

data class HomeFeed(
    val featured: List<Track>,
    val popular: List<Track>,
    val latest: List<Track>,
    val globalPlaylists: List<Track>,
    val localPlaylists: List<Track>
)

interface HomeRepository {
    suspend fun getHomeFeed(): HomeFeed
}
