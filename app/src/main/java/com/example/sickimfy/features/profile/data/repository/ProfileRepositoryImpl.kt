package com.example.sickimfy.features.profile.data.repository

import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import com.example.sickimfy.core.network.SickimfyApi
import com.example.sickimfy.core.network.dto.ProfileDto
import com.example.sickimfy.features.profile.domain.repository.ProfileRepository
import com.example.sickimfy.features.profile.domain.repository.UserProfile
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val api: SickimfyApi,
    private val preferences: UserPreferencesDataStore
) : ProfileRepository {
    override suspend fun getProfile(): UserProfile = requireSession { api.getMyProfile() }.toDomain()

    override suspend fun upgrade(): UserProfile = requireSession { api.upgradeProfile() }
        .also { preferences.setPremium(it.isPremium) }
        .toDomain()

    private suspend fun requireSession(block: suspend () -> ProfileDto): ProfileDto {
        check(!preferences.accessToken().isNullOrBlank()) { "Sign in is required to load the profile" }
        return block()
    }

    private fun ProfileDto.toDomain() = UserProfile(displayName, avatarUrl, isPremium)
}
