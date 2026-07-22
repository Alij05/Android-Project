package com.example.sickimfy.core.network.dto

import com.example.sickimfy.features.home.domain.model.Track
import com.example.sickimfy.core.network.resolveMediaUrl

fun TrackDto.toDomain(apiBaseUrl: String): Track = Track(
    id = id.toString(),
    title = title,
    artist = artistName,
    imageUrl = resolveMediaUrl(coverImageUrl, apiBaseUrl).orEmpty(),
    duration = durationSeconds.toDurationLabel(),
    albumName = albumName,
    audioUrl = resolveMediaUrl(audioUrl, apiBaseUrl)
)

private fun Int?.toDurationLabel(): String {
    if (this == null) return "--:--"
    return "%d:%02d".format(this / 60, this % 60)
}
