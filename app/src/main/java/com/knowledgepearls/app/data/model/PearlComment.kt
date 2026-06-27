package com.knowledgepearls.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PearlComment(
    val id: String,
    @SerialName("pearl_id") val pearlId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("author_name") val authorName: String = "",
    val body: String = "",
    @SerialName("created_at") val createdAt: String = "",
)
