package com.knowledgepearls.app.data.repository

data class MediaImportResult(
    val attempted: Int,
    val imported: Int,
) {
    val failed: Int get() = (attempted - imported).coerceAtLeast(0)
    val hasPartialFailure: Boolean get() = failed > 0 && imported > 0
    val isCompleteFailure: Boolean get() = attempted > 0 && imported == 0
}
