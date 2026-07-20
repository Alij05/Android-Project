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
        val customBaseUrl = prefs.apiBaseUrl

        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val customHttpUrl = customBaseUrl.toHttpUrlOrNull()
        if (customHttpUrl != null) {
            val originalUrl = originalRequest.url
            val newUrl = originalUrl.newBuilder()
                .scheme(customHttpUrl.scheme)
                .host(customHttpUrl.host)
                .port(customHttpUrl.port)
                .build()
            requestBuilder.url(newUrl)
        }

        return chain.proceed(requestBuilder.build())
    }
}
