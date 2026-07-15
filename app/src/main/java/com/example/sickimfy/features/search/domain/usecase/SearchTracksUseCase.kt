package com.example.sickimfy.features.search.domain.usecase

import com.example.sickimfy.features.home.domain.model.Track
import com.example.sickimfy.features.search.domain.repository.SearchRepository
import javax.inject.Inject

class SearchTracksUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(query: String): List<Track> = repository.searchTracks(query)
}
