package com.knowledgepearls.app.ui.media

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

sealed interface DocumentOpenResult {
    data object ViewInApp : DocumentOpenResult
    data object OpenedExternally : DocumentOpenResult
    data class Failed(val message: String) : DocumentOpenResult
}

object DocumentOpener {
    fun usesInAppPdfViewer(url: String, filename: String): Boolean {
        val effectiveName = effectiveMediaFilename(filename, url)
        return DocumentSupport.isPdf(effectiveName, url)
    }

    suspend fun resolveLocalFile(
        context: Context,
        url: String,
        filename: String,
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val effectiveName = effectiveMediaFilename(filename, url)
            val file = DocumentFileResolver.resolveFile(context.cacheDir, url, effectiveName)
            if (!file.exists() || !file.canRead() || file.length() <= 0L) {
                error("Document file not found")
            }
            file
        }
    }

    suspend fun openDocument(
        context: Context,
        url: String,
        filename: String,
    ): DocumentOpenResult {
        if (usesInAppPdfViewer(url, filename)) {
            return DocumentOpenResult.ViewInApp
        }

        val file = resolveLocalFile(context, url, filename).getOrElse { error ->
            return DocumentOpenResult.Failed(
                error.message?.takeIf { it.isNotBlank() } ?: "Couldn't load document",
            )
        }

        return withContext(Dispatchers.Main) {
            if (DocumentPresenter.openLocalFile(context, file)) {
                DocumentOpenResult.OpenedExternally
            } else {
                DocumentOpenResult.Failed(
                    "No app available to open this document. Install Word, Google Docs, or a similar viewer.",
                )
            }
        }
    }
}
