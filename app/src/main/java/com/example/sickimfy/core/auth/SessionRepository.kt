package com.example.sickimfy.core.auth

import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import com.example.sickimfy.core.network.SickimfyApi
import com.example.sickimfy.core.network.dto.AuthRequestDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val api: SickimfyApi,
    private val preferences: UserPreferencesDataStore
) {
    suspend fun login(email: String, password: String) {
        val response = api.login(AuthRequestDto(email, password))
        preferences.setAccessToken(response.token)
        preferences.setPremium(response.user.isPremium)
    }

    suspend fun register(email: String, password: String, displayName: String) {
        val response = api.register(AuthRequestDto(email, password, displayName))
        preferences.setAccessToken(response.token)
        preferences.setPremium(response.user.isPremium)
    }

    suspend fun logout() {
        preferences.setAccessToken(null)
        preferences.setPremium(false)
    }
}
