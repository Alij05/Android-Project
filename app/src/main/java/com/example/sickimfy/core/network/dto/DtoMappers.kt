package com.example.sickimfy.core.network.dto

import com.example.sickimfy.features.home.domain.model.Track

fun TrackDto.toDomain(): Track = Track(
    id = id.toString(),
    title = title,
    artist = artistName,
    imageUrl = coverImageUrl.orEmpty(),
    duration = durationSeconds.toDurationLabel(),
    albumName = albumName,
    audioUrl = audioUrl
)

private fun Int?.toDurationLabel(): String {
    if (this == null) return "--:--"
    return "%d:%02d".format(this / 60, this % 60)
}
