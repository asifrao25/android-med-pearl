package com.knowledgepearls.app.data.local.model

import kotlinx.serialization.Serializable

@Serializable
data class ClinicalCasePayload(
    val history: String = "",
    val examination: String = "",
    val investigation: String = "",
    val diagnosis: String = "",
    val discussion: String = "",
    val references: String = "",
)

enum class ClinicalCaseSection(val label: String) {
    Examination("Examination"),
    Investigation("Investigation"),
    Discussion("Discussion"),
}
