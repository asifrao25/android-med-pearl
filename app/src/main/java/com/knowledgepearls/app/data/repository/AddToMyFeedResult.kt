package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity

sealed class AddToMyFeedResult {
    abstract val pearl: KnowledgePearlEntity
    abstract val mediaImport: MediaImportResult?

    data class Saved(
        override val pearl: KnowledgePearlEntity,
        override val mediaImport: MediaImportResult? = null,
    ) : AddToMyFeedResult()

    data class AlreadyInFeed(
        override val pearl: KnowledgePearlEntity,
        override val mediaImport: MediaImportResult? = null,
    ) : AddToMyFeedResult()
}
