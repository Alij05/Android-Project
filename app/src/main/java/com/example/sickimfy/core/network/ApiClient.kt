package com.example.sickimfy.core.network

import com.example.sickimfy.core.network.dto.AuthRequestDto
import com.example.sickimfy.core.network.dto.AuthResponseDto
import com.example.sickimfy.core.network.dto.HomeResponseDto
import com.example.sickimfy.core.network.dto.PlaylistSummaryDto
import com.example.sickimfy.core.network.dto.ProfileDto
import com.example.sickimfy.core.network.dto.TrackPageDto
import retrofit2.http.Body
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
}

data class UpdateProfileRequestDto(
    val displayName: String? = null,
    val avatarUrl: String? = null
)
