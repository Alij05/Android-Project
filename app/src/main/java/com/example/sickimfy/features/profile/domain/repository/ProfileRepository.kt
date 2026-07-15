package com.example.sickimfy.features.profile.domain.repository

data class UserProfile(
    val displayName: String,
    val avatarUrl: String?,
    val isPremium: Boolean
)

interface ProfileRepository {
    suspend fun getProfile(): UserProfile
    suspend fun upgrade(): UserProfile
}
