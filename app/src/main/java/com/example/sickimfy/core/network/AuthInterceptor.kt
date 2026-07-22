package com.example.sickimfy.core.network

import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferences: UserPreferencesDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = runBlocking { preferences.preferences.first() }
        val token = prefs.accessToken
        val apiBaseUrl = prefs.apiBaseUrl

        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        if (apiBaseUrl.isNotBlank()) {
            val newHttpUrl = apiBaseUrl.toHttpUrlOrNull()
            if (newHttpUrl != null) {
                val originalUrl = originalRequest.url
                val newUrl = originalUrl.newBuilder()
                    .scheme(newHttpUrl.scheme)
                    .host(newHttpUrl.host)
                    .port(newHttpUrl.port)
                    .build()
                requestBuilder.url(newUrl)
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}
