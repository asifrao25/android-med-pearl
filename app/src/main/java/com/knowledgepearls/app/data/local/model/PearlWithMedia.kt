package com.knowledgepearls.app.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity

data class PearlWithMedia(
    @Embedded val pearl: KnowledgePearlEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "pearlId",
    )
    val mediaItems: List<PearlMediaEntity>,
)
