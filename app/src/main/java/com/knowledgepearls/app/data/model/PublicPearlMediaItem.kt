package com.knowledgepearls.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PublicPearlMediaItem(
    val type: String = "photo",
    val url: String = "",
    val path: String? = null,
    val filename: String? = null,
)
