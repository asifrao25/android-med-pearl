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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.knowledgepearls.app.data.capture.PickedMedia
import com.knowledgepearls.app.data.capture.mediaTypeForFilename
import java.io.File

@Composable
fun rememberMediaPickers(
    onMediaPicked: (PickedMedia) -> Unit,
): MediaPickers {
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { readUri(context, it)?.let(onMediaPicked) }
    }

    val multiGalleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(8),
    ) { uris ->
        uris.forEach { uri -> readUri(context, uri)?.let(onMediaPicked) }
    }

    val documentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { readUri(context, it)?.let(onMediaPicked) }
    }

    val cameraUri = remember { mutableListOf<Uri>() }
    val pendingCameraLaunch = remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            cameraUri.firstOrNull()?.let { uri ->
                readUri(context, uri)?.let(onMediaPicked)
            }
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

private fun readUri(context: Context, uri: Uri): PickedMedia? {
    return runCatching {
        val filename = queryDisplayName(context, uri) ?: "attachment"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        if (bytes.isEmpty()) return null
        PickedMedia(
            bytes = bytes,
            filename = filename,
            type = mediaTypeForFilename(filename),
        )
    }.getOrNull()
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
