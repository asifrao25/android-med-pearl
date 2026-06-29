package com.knowledgepearls.app.ui.media

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object DocumentPresenter {
    fun canOpenLocalFile(context: Context, file: File): Boolean {
        if (!file.exists() || !file.canRead()) return false
        return buildViewIntent(context, file) != null
    }

    fun openLocalFile(context: Context, file: File): Boolean {
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(context, "Document file not found", Toast.LENGTH_SHORT).show()
            return false
        }

        val intent = buildViewIntent(context, file)
        if (intent == null) {
            Toast.makeText(
                context,
                "No app available to open this document",
                Toast.LENGTH_LONG,
            ).show()
            return false
        }

        return runCatching {
            context.startActivity(
                Intent.createChooser(intent, DocumentSupport.openActionTitle(file.name)),
            )
            true
        }.getOrElse { error ->
            val message = when (error) {
                is IllegalArgumentException -> "Unable to share this file with other apps"
                else -> "No app available to open this document"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun buildViewIntent(context: Context, file: File): Intent? {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val mimeCandidates = buildList {
            add(DocumentSupport.mimeType(file.name))
            add("*/*")
        }.distinct()

        for (mime in mimeCandidates) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newUri(context.contentResolver, file.name, uri)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                return intent
            }
        }
        return null
    }
}
