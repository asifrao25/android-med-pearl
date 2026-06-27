package com.knowledgepearls.app.ui.capture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.knowledgepearls.app.data.capture.PickResult
import com.knowledgepearls.app.data.capture.PickedMedia
import com.knowledgepearls.app.data.capture.PickedUriReader
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun rememberMediaPickers(
    onMediaPicked: (PickedMedia) -> Unit,
    onPickFailed: ((String) -> Unit)? = null,
): MediaPickers {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun notifyFailure(message: String) {
        if (onPickFailed != null) {
            onPickFailed(message)
        } else {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    fun handlePickedUri(uri: Uri) {
        scope.launch {
            when (val result = withContext(Dispatchers.IO) { PickedUriReader.read(context, uri) }) {
                is PickResult.Success -> onMediaPicked(result.media)
                is PickResult.Failure -> notifyFailure(result.message)
            }
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
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let(::handlePickedUri)
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
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
            documentLauncher.launch("*/*")
        },
        pickDocumentWithPersistedAccess = {
            openDocumentLauncher.launch(PickedUriReader.openDocumentMimeTypes())
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
    val pickDocumentWithPersistedAccess: () -> Unit = pickDocument,
    val takePhoto: () -> Unit,
)

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
