package com.knowledgepearls.app.ui.capture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.knowledgepearls.app.data.capture.PickedMedia
import com.knowledgepearls.app.data.capture.mediaTypeForFilename
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun rememberMediaPickers(
    onMediaPicked: (PickedMedia) -> Unit,
): MediaPickers {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun handlePickedUri(uri: Uri) {
        scope.launch {
            readUri(context, uri)?.let(onMediaPicked)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let(::handlePickedUri)
    }

    val multiGalleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(8),
    ) { uris ->
        uris.forEach(::handlePickedUri)
    }

    val documentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { picked ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    picked,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            handlePickedUri(picked)
        }
    }

    val cameraUri = remember { mutableListOf<Uri>() }
    val pendingCameraLaunch = remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            cameraUri.firstOrNull()?.let(::handlePickedUri)
        }
    }

    val launchCamera: () -> Unit = {
        val uri = CaptureMediaUri.create(context)
        cameraUri.clear()
        cameraUri.add(uri)
        cameraLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && pendingCameraLaunch.value) {
            launchCamera()
        }
        pendingCameraLaunch.value = false
    }

    return MediaPickers(
        pickGallery = {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        },
        pickMultipleGallery = {
            multiGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        },
        pickDocument = {
            documentLauncher.launch(arrayOf("*/*"))
        },
        takePhoto = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                launchCamera()
            } else {
                pendingCameraLaunch.value = true
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
    )
}

data class MediaPickers(
    val pickGallery: () -> Unit,
    val pickMultipleGallery: () -> Unit,
    val pickDocument: () -> Unit,
    val takePhoto: () -> Unit,
)

private suspend fun readUri(context: Context, uri: Uri): PickedMedia? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val filename = queryDisplayName(context, uri) ?: "attachment"
            val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes()
            } ?: return@withContext null
            if (bytes.isEmpty()) return@withContext null
            PickedMedia(
                bytes = bytes,
                filename = filename,
                type = mediaTypeForFilename(filename),
            )
        }.getOrNull()
    }
}

private fun queryDisplayName(context: Context, uri: Uri): String? {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index)
        }
    }
    return uri.lastPathSegment?.substringAfterLast('/')
}

private object CaptureMediaUri {
    fun create(context: Context): Uri {
        val dir = File(context.cacheDir, "camera").also { it.mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }
}
