package com.knowledgepearls.app.data.media

class MediaTooLargeException(
    val maxBytes: Long,
) : Exception("This file is too large to import on device (max ${maxBytes / (1024 * 1024)} MB).")
