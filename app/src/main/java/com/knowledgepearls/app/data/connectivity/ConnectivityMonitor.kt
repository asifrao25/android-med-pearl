package com.knowledgepearls.app.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class ConnectivityMonitor @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _state = MutableStateFlow(ConnectivityState())
    val state: StateFlow<ConnectivityState> = _state.asStateFlow()

    private var isMonitoring = false

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            applyConnected(true)
        }

        override fun onLost(network: Network) {
            applyConnected(isCurrentlyConnected())
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            applyConnected(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        }
    }

    fun start() {
        if (isMonitoring) return
        isMonitoring = true
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        applyConnected(isCurrentlyConnected())
    }

    fun stop() {
        if (!isMonitoring) return
        runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        isMonitoring = false
    }

    fun continueOffline() {
        _state.update { it.copy(isOfflineMode = true, showOfflinePrompt = false) }
    }

    fun retryConnection() {
        val connected = isCurrentlyConnected()
        applyConnected(connected)
        if (connected) {
            _state.update { it.copy(isOfflineMode = false, showOfflinePrompt = false) }
        } else {
            _state.update { it.copy(showOfflinePrompt = true) }
        }
    }

    private fun applyConnected(connected: Boolean) {
        _state.update { current ->
            if (connected) {
                current.copy(
                    isConnected = true,
                    hasResolvedPath = true,
                    isOfflineMode = false,
                    showOfflinePrompt = false,
                )
            } else {
                current.copy(
                    isConnected = false,
                    hasResolvedPath = true,
                    showOfflinePrompt = !current.isOfflineMode,
                )
            }
        }
    }

    private fun isCurrentlyConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

data class ConnectivityState(
    val isConnected: Boolean = true,
    val hasResolvedPath: Boolean = false,
    val isOfflineMode: Boolean = false,
    val showOfflinePrompt: Boolean = false,
)
