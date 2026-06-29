package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity

sealed class AddToMyFeedResult {
    data class Saved(val pearl: KnowledgePearlEntity) : AddToMyFeedResult()
    data class AlreadyInFeed(val pearl: KnowledgePearlEntity) : AddToMyFeedResult()
}
