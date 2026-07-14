package com.example.sickimfy.features.home.domain.model

/**
 * Pure domain representation of a music track.
 * Decoupled from remote DTOs and local entities.
 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val duration: String,
    val albumName: String? = null
)