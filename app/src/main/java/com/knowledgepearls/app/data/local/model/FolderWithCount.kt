package com.knowledgepearls.app.data.local.model

import androidx.room.Embedded
import com.knowledgepearls.app.data.local.entity.FolderEntity

data class FolderWithCount(
    @Embedded val folder: FolderEntity,
    val pearlCount: Int,
)
