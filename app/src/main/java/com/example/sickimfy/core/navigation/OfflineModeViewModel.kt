package com.example.sickimfy.core.navigation

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.core.network.BackendAvailability
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class OfflineModeViewModel @Inject constructor(
    @ApplicationContext context: Context,
    backendAvailability: BackendAvailability
) : ViewModel() {
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    val isOffline: StateFlow<Boolean> = combine(
        observeInternetConnectivity(connectivityManager),
        backendAvailability.isReachable
    ) { hasInternet, backendReachable -> !hasInternet || !backendReachable }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}

private fun observeInternetConnectivity(connectivityManager: ConnectivityManager): Flow<Boolean> = callbackFlow {
    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = trySend(isConnected()).let { }
        override fun onLost(network: Network) = trySend(isConnected()).let { }
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = trySend(isConnected()).let { }
    }
    trySend(isConnected())
    connectivityManager.registerDefaultNetworkCallback(callback)
    awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
}
