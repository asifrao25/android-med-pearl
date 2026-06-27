package com.knowledgepearls.app.navigation

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class AppNavigationBus @Inject constructor() {
    private val _events = MutableSharedFlow<AppNavigationEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<AppNavigationEvent> = _events.asSharedFlow()

    fun emit(event: AppNavigationEvent) {
        _events.tryEmit(event)
    }
}
