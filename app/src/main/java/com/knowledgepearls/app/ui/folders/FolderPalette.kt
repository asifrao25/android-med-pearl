package com.knowledgepearls.app.ui.folders

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

object FolderPalette {
    private val pairs = listOf(
        Color(0xFF6B5CE7) to Color(0xFF947AFF),
        Color(0xFFED407A) to Color(0xFFFF708F),
        Color(0xFF14B8A6) to Color(0xFF2ED6C8),
        Color(0xFFF5A623) to Color(0xFFFFC261),
        Color(0xFF3B82F6) to Color(0xFF61A5FA),
        Color(0xFF8B5CF6) to Color(0xFFAE7FFA),
        Color(0xFFE65247) to Color(0xFFFA7A66),
        Color(0xFF2EA173) to Color(0xFF4DC28F),
    )

    fun gradient(folderId: String): Brush {
        val (start, end) = pairs[stableIndex(folderId)]
        return Brush.linearGradient(listOf(start, end))
    }

    private fun stableIndex(folderId: String): Int =
        abs(folderId.hashCode()) % pairs.size
}
