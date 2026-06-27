package com.knowledgepearls.app.ui.media

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object DocumentPresenter {
    fun openLocalFile(context: Context, file: File): Boolean {
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(context, "Document file not found", Toast.LENGTH_SHORT).show()
            return false
        }
        return runCatching {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            val mime = DocumentSupport.mimeType(file.name)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open document"))
            true
        }.getOrElse { error ->
            val message = when {
                error is IllegalArgumentException -> "Unable to share this file with other apps"
                else -> "No app available to open this document"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            false
        }
    }
}
