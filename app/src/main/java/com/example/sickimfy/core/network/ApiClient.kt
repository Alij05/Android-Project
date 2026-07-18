package com.example.sickimfy.core.network

import com.example.sickimfy.core.network.dto.AuthRequestDto
import com.example.sickimfy.core.network.dto.AuthResponseDto
import com.example.sickimfy.core.network.dto.HomeResponseDto
import com.example.sickimfy.core.network.dto.PlaylistSummaryDto
import com.example.sickimfy.core.network.dto.ProfileDto
import com.example.sickimfy.core.network.dto.TrackPageDto
import com.example.sickimfy.core.network.dto.ConversationSummaryDto
import com.example.sickimfy.core.network.dto.CreateConversationRequestDto
import com.example.sickimfy.core.network.dto.FollowedUserDto
import com.example.sickimfy.core.network.dto.MessageDto
import com.example.sickimfy.core.network.dto.SendMessageRequestDto
import com.example.sickimfy.core.network.dto.PublicProfileDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SickimfyApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequestDto): AuthResponseDto

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequestDto): AuthResponseDto

    @GET("api/home")
    suspend fun getHome(): HomeResponseDto

    @GET("api/tracks")
    suspend fun getTracks(
        @Query("query") query: String? = null,
        @Query("genre") genre: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): TrackPageDto

    @GET("api/playlists/public")
    suspend fun getPublicPlaylists(): List<PlaylistSummaryDto>

    @GET("api/playlists")
    suspend fun getMyPlaylists(): List<PlaylistSummaryDto>

    @GET("api/profile/me")
    suspend fun getMyProfile(): ProfileDto

    @PATCH("api/profile/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): ProfileDto

    @POST("api/profile/me/upgrade")
    suspend fun upgradeProfile(): ProfileDto

    @GET("api/users")
    suspend fun searchUsers(@Query("query") query: String?): List<PublicProfileDto>

    @GET("api/follows")
    suspend fun getFollowedUsers(): List<FollowedUserDto>

    @POST("api/follows/{userId}")
    suspend fun followUser(@retrofit2.http.Path("userId") userId: Int)

    @DELETE("api/follows/{userId}")
    suspend fun unfollowUser(@retrofit2.http.Path("userId") userId: Int)

    @GET("api/conversations")
    suspend fun getConversations(): List<ConversationSummaryDto>

    @POST("api/conversations")
    suspend fun getOrCreateConversation(@Body request: CreateConversationRequestDto): ConversationSummaryDto

    @GET("api/conversations/{id}/messages")
    suspend fun getMessages(@retrofit2.http.Path("id") conversationId: Int): List<MessageDto>

    @POST("api/conversations/{id}/messages")
    suspend fun sendMessage(
        @retrofit2.http.Path("id") conversationId: Int,
        @Body request: SendMessageRequestDto
    ): MessageDto

    @GET("api/playlists/{id}")
    suspend fun getPlaylistDetails(@retrofit2.http.Path("id") id: Int): com.example.sickimfy.core.network.dto.PlaylistDetailsDto

    @GET("api/tracks/{id}")
    suspend fun getTrack(@retrofit2.http.Path("id") id: Int): com.example.sickimfy.core.network.dto.TrackDto
}

data class UpdateProfileRequestDto(
    val displayName: String? = null,
    val avatarUrl: String? = null
)

