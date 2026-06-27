package com.knowledgepearls.app.data.connectivity

import com.knowledgepearls.app.data.remote.SupabaseConfig
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class BackendHealthMonitor @Inject constructor(
    private val connectivityMonitor: ConnectivityMonitor,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pollJob: Job? = null

    private var userDismissedAlert = false
    private var wasUnreachable = false

    private val _state = MutableStateFlow(BackendHealthState())
    val state: StateFlow<BackendHealthState> = _state.asStateFlow()

    fun start() {
        pollJob?.cancel()
        pollJob = scope.launch {
            while (isActive) {
                checkNow()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stop() {
        pollJob?.cancel()
        pollJob = null
    }

    suspend fun checkNow() {
        if (!connectivityMonitor.state.value.isConnected) return
        val reachable = performHealthCheck()
        applyHealthResult(reachable)
    }

    fun dismissUnavailableAlert() {
        userDismissedAlert = true
        _state.update { it.copy(showUnavailableAlert = false) }
    }

    fun dismissRestoredNotice() {
        _state.update { it.copy(showRestoredNotice = false) }
    }

    suspend fun retryNow() {
        userDismissedAlert = false
        checkNow()
        if (!_state.value.isBackendReachable) {
            _state.update { it.copy(showUnavailableAlert = true) }
        }
    }

    private fun applyHealthResult(reachable: Boolean) {
        _state.update { current ->
            if (reachable) {
                val showRestored = wasUnreachable
                if (showRestored) {
                    userDismissedAlert = false
                }
                wasUnreachable = false
                current.copy(
                    isBackendReachable = true,
                    hasCompletedHealthCheck = true,
                    showUnavailableAlert = false,
                    showRestoredNotice = showRestored,
                )
            } else {
                wasUnreachable = true
                val showAlert = !userDismissedAlert &&
                    !connectivityMonitor.state.value.showOfflinePrompt
                current.copy(
                    isBackendReachable = false,
                    hasCompletedHealthCheck = true,
                    showUnavailableAlert = showAlert,
                )
            }
        }
    }

    private suspend fun performHealthCheck(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("${SupabaseConfig.URL}/auth/v1/health")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8_000
                readTimeout = 8_000
                setRequestProperty("apikey", SupabaseConfig.ANON_KEY)
                setRequestProperty("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
            }
            try {
                connection.responseCode in 200..299
            } finally {
                connection.disconnect()
            }
        }.getOrDefault(false)
    }

    private companion object {
        const val POLL_INTERVAL_MS = 30_000L
    }
}

data class BackendHealthState(
    val isBackendReachable: Boolean = true,
    val hasCompletedHealthCheck: Boolean = false,
    val showUnavailableAlert: Boolean = false,
    val showRestoredNotice: Boolean = false,
)
