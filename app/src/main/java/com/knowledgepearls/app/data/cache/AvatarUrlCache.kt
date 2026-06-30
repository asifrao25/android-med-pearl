package com.knowledgepearls.app.data.cache

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvatarUrlCache @Inject constructor() {
    private val cache = ConcurrentHashMap<String, String?>()
    private val order = ArrayDeque<String>()

    fun get(userId: String): String? {
        val key = userId.trim().lowercase()
        if (key.isEmpty()) return null
        return if (cache.containsKey(key)) cache[key] else null
    }

    fun isCached(userId: String): Boolean {
        val key = userId.trim().lowercase()
        return key.isNotEmpty() && cache.containsKey(key)
    }

    fun put(userId: String, url: String?) {
        val key = userId.trim().lowercase()
        if (key.isEmpty()) return
        if (!cache.containsKey(key)) {
            order.addLast(key)
            while (order.size > MAX_ENTRIES) {
                val evicted = order.removeFirst()
                cache.remove(evicted)
            }
        }
        cache[key] = url?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun clear() {
        cache.clear()
        order.clear()
    }

    private companion object {
        const val MAX_ENTRIES = 128
    }
}
