package com.knowledgepearls.app.data.local.model

import com.knowledgepearls.app.data.local.entity.KnowledgePearlContentKind
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import kotlinx.serialization.json.Json

private val casePayloadJson = Json { ignoreUnknownKeys = true }

fun KnowledgePearlEntity.clinicalCasePayload(): ClinicalCasePayload {
    if (casePayloadJSON.isBlank()) return ClinicalCasePayload()
    return runCatching {
        casePayloadJson.decodeFromString<ClinicalCasePayload>(casePayloadJSON)
    }.getOrDefault(ClinicalCasePayload())
}

fun KnowledgePearlEntity.withClinicalCasePayload(payload: ClinicalCasePayload): KnowledgePearlEntity {
    val json = casePayloadJson.encodeToString(ClinicalCasePayload.serializer(), payload)
    return copy(
        contentKind = KnowledgePearlContentKind.CLINICAL_CASE,
        casePayloadJSON = json,
    )
}

fun KnowledgePearlEntity.isClinicalCase(): Boolean =
    contentKind == KnowledgePearlContentKind.CLINICAL_CASE

fun KnowledgePearlEntity.isQuickPearl(hasMedia: Boolean = false): Boolean {
    if (contentKind == KnowledgePearlContentKind.QUICK) return true
    return contentKind == KnowledgePearlContentKind.STANDARD &&
        !isClinicalCase() &&
        sourceURL.isNullOrBlank() &&
        !hasMedia
}
