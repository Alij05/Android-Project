package com.example.sickimfy.core.network.dto

data class AuthRequestDto(
    val email: String,
    val password: String,
    val displayName: String? = null
)

data class AuthResponseDto(val token: String, val user: ProfileDto)

data class ProfileDto(
    val id: Int,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val isPremium: Boolean,
    val createdAt: String
)

data class TrackDto(
    val id: Int,
    val title: String,
    val artistName: String,
    val albumName: String?,
    val genre: String?,
    val coverImageUrl: String?,
    val audioUrl: String?,
    val durationSeconds: Int?,
    val isFeatured: Boolean,
    val createdAt: String
)

data class TrackPageDto(
    val items: List<TrackDto>,
    val offset: Int,
    val limit: Int,
    val total: Int
)

data class PublicProfileDto(
    val id: Int,
    val displayName: String,
    val avatarUrl: String?,
    val isPremium: Boolean
)

data class PlaylistSummaryDto(
    val id: Int,
    val title: String,
    val description: String?,
    val coverImageUrl: String?,
    val isPublic: Boolean,
    val owner: PublicProfileDto,
    val trackCount: Int,
    val createdAt: String
)

data class HomeResponseDto(
    val featuredTracks: List<TrackDto>,
    val latestTracks: List<TrackDto>,
    val publicPlaylists: List<PlaylistSummaryDto>
)

data class FollowedUserDto(
    val user: PublicProfileDto,
    val followedAt: String
)

data class ConversationSummaryDto(
    val id: Int,
    val participant: PublicProfileDto,
    val lastMessage: MessageDto?,
    val createdAt: String
)

data class CreateConversationRequestDto(
    val participantId: Int
)

data class MessageDto(
    val id: Int,
    val conversationId: Int,
    val senderId: Int,
    val content: String?,
    val sharedTrack: TrackDto?,
    val status: String,
    val createdAt: String
)

data class SendMessageRequestDto(
    val content: String? = null,
    val sharedTrackId: Int? = null
)

