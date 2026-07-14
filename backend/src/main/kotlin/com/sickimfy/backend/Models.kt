package com.sickimfy.backend

import kotlinx.serialization.Serializable

@Serializable data class ErrorResponse(val message: String)
@Serializable data class HealthResponse(val status: String = "ok")

@Serializable
data class UserProfile(val id: Int, val email: String, val displayName: String, val avatarUrl: String? = null, val isPremium: Boolean, val createdAt: String)

@Serializable
data class PublicProfile(val id: Int, val displayName: String, val avatarUrl: String? = null, val isPremium: Boolean)

@Serializable data class AuthRequest(val email: String, val password: String, val displayName: String? = null)
@Serializable data class AuthResponse(val token: String, val user: UserProfile)
@Serializable data class UpdateProfileRequest(val displayName: String? = null, val avatarUrl: String? = null)

@Serializable
data class Track(
    val id: Int, val title: String, val artistName: String, val albumName: String? = null,
    val genre: String? = null, val coverImageUrl: String? = null, val audioUrl: String? = null,
    val durationSeconds: Int? = null, val isFeatured: Boolean, val createdAt: String
)

@Serializable
data class UpsertTrackRequest(
    val title: String, val artistName: String, val albumName: String? = null, val genre: String? = null,
    val coverImageUrl: String? = null, val audioUrl: String? = null, val durationSeconds: Int? = null,
    val isFeatured: Boolean = false
)

@Serializable data class TrackPage(val items: List<Track>, val offset: Int, val limit: Int, val total: Int)
@Serializable data class HomeResponse(val featuredTracks: List<Track>, val latestTracks: List<Track>, val publicPlaylists: List<PlaylistSummary>)

@Serializable
data class PlaylistSummary(
    val id: Int, val title: String, val description: String? = null, val coverImageUrl: String? = null,
    val isPublic: Boolean, val owner: PublicProfile, val trackCount: Int, val createdAt: String
)

@Serializable data class PlaylistDetails(val playlist: PlaylistSummary, val tracks: List<Track>)
@Serializable data class CreatePlaylistRequest(val title: String, val description: String? = null, val coverImageUrl: String? = null, val isPublic: Boolean = true)
@Serializable data class AddTrackToPlaylistRequest(val trackId: Int)
@Serializable data class SearchHistoryItem(val id: Int, val query: String, val createdAt: String)
@Serializable data class CreateSearchHistoryRequest(val query: String)
@Serializable data class FollowedUser(val user: PublicProfile, val followedAt: String)

@Serializable
data class ConversationSummary(val id: Int, val participant: PublicProfile, val lastMessage: Message? = null, val createdAt: String)

@Serializable data class CreateConversationRequest(val participantId: Int)

@Serializable
data class Message(
    val id: Int, val conversationId: Int, val senderId: Int, val content: String? = null,
    val sharedTrack: Track? = null, val status: MessageStatus, val createdAt: String
)

@Serializable enum class MessageStatus { SENDING, SENT, READ }
@Serializable data class SendMessageRequest(val content: String? = null, val sharedTrackId: Int? = null)
@Serializable data class SocketEvent(val type: SocketEventType, val message: Message? = null, val isTyping: Boolean? = null)
@Serializable enum class SocketEventType { MESSAGE, TYPING }
