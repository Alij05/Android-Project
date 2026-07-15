package com.example.sickimfy.core.network.dto

import org.junit.Assert.assertEquals
import org.junit.Test

class DtoMappersTest {
    @Test
    fun `track dto maps server fields and formats duration`() {
        val dto = TrackDto(
            id = 42,
            title = "Demo",
            artistName = "Artist",
            albumName = "Album",
            genre = "Pop",
            coverImageUrl = "https://example.test/cover.jpg",
            audioUrl = "https://example.test/audio.mp3",
            durationSeconds = 185,
            isFeatured = true,
            createdAt = "2026-07-15"
        )

        val track = dto.toDomain()

        assertEquals("42", track.id)
        assertEquals("Artist", track.artist)
        assertEquals("3:05", track.duration)
        assertEquals(dto.audioUrl, track.audioUrl)
    }
}
