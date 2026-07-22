package com.example.sickimfy.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendAvailability @Inject constructor() {
    private val _isReachable = MutableStateFlow(true)
    val isReachable: StateFlow<Boolean> = _isReachable.asStateFlow()

    fun markReachable() {
        _isReachable.value = true
    }

    fun markUnavailable() {
        _isReachable.value = false
    }
}

class BackendAvailabilityInterceptor @Inject constructor(
    private val availability: BackendAvailability
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = try {
        chain.proceed(chain.request()).also { availability.markReachable() }
    } catch (exception: IOException) {
        availability.markUnavailable()
        throw exception
    }
}
