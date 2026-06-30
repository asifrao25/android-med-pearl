package com.knowledgepearls.app.data.share

import com.knowledgepearls.app.data.capture.PickedMedia
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.entity.MediaType
import com.knowledgepearls.app.data.local.model.clinicalCasePayload
import com.knowledgepearls.app.data.local.model.effectiveSourceReference
import com.knowledgepearls.app.data.local.model.isClinicalCase
import com.knowledgepearls.app.data.model.PearlSharePayload
import com.knowledgepearls.app.data.model.PublicPearlMediaItem
import java.security.MessageDigest

/** Matches iOS `PearlShareService.contentIdentity` for cross-platform duplicate checks. */
object PearlContentIdentity {
    private const val FIELD_SEPARATOR = '\u001e'

    fun forPearl(pearl: KnowledgePearlEntity, pickedMedia: List<PickedMedia>): String =
        compute(
            title = pearl.title,
            notes = if (pearl.isClinicalCase()) "" else pearl.notes,
            tags = pearl.tags,
            contentKind = pearl.contentKind,
            sourceUrl = pearl.sourceURL,
            sourceReference = pearl.effectiveSourceReference(),
            casePayload = if (pearl.isClinicalCase()) pearl.clinicalCasePayload() else null,
            mediaItems = pickedMedia.map {
                MediaSignature(
                    type = publicContentType(it.type, it.filename),
                    filename = it.filename,
                    section = it.sectionTag,
                )
            },
        )

    fun forPayload(payload: PearlSharePayload): String {
        val isClinical = payload.contentKind == "clinical_case"
        return compute(
            title = payload.title,
            notes = if (isClinical) "" else payload.notes,
            tags = payload.tags,
            contentKind = payload.contentKind,
            sourceUrl = payload.sourceUrl,
            sourceReference = payload.sourceReference,
            casePayload = if (isClinical) payload.casePayload else null,
            mediaItems = payload.mediaItems.map {
                MediaSignature(
                    type = it.type,
                    filename = it.resolvedFilename,
                    section = it.section.orEmpty(),
                )
            },
        )
    }

    private data class MediaSignature(
        val type: String,
        val filename: String,
        val section: String,
    )

    private fun compute(
        title: String,
        notes: String,
        tags: List<String>,
        contentKind: String,
        sourceUrl: String?,
        sourceReference: String,
        casePayload: com.knowledgepearls.app.data.local.model.ClinicalCasePayload?,
        mediaItems: List<MediaSignature>,
    ): String {
        val parts = buildList {
            add(normalized(title))
            add(normalized(notes))
            add(contentKind)
            add(normalized(sourceUrl.orEmpty()))
            add(normalized(sourceReference))
            add(tags.map(::normalized).sorted().joinToString(","))
            if (casePayload != null) {
                add(normalized(casePayload.history))
                add(normalized(casePayload.examination))
                add(normalized(casePayload.investigation))
                add(normalized(casePayload.diagnosis))
                add(normalized(casePayload.discussion))
                add(normalized(casePayload.references))
            }
            add(
                mediaItems
                    .map { "${it.type}|${it.filename}|${it.section}" }
                    .sorted()
                    .joinToString(";"),
            )
        }
        return sha256Hex(parts.joinToString(FIELD_SEPARATOR.toString()))
    }

    private fun normalized(value: String): String = value.trim()

    private fun sha256Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun publicContentType(type: String, filename: String): String = when (type) {
        MediaType.VIDEO -> "video"
        MediaType.IMAGE -> "photo"
        MediaType.PDF, MediaType.DOCUMENT -> {
            val ext = filename.substringAfterLast('.', "").lowercase()
            if (ext in setOf("mp4", "mov", "m4v", "webm")) "video" else "document"
        }
        else -> "photo"
    }
}
