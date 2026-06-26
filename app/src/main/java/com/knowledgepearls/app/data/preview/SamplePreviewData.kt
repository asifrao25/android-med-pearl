package com.knowledgepearls.app.data.preview

import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import com.knowledgepearls.app.data.local.model.ClinicalCasePayload
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.local.model.withClinicalCasePayload

object SamplePreviewData {

    val standardPearl = PearlWithMedia(
        pearl = KnowledgePearlEntity(
            id = "preview-standard",
            title = "Beta-blockers in heart failure",
            notes = "Start low, go slow. Carvedilol and bisoprolol have the strongest evidence.",
            sourceReference = "ESC Heart Failure Guidelines 2023",
            tags = listOf("cardiology", "heart-failure"),
            updatedAt = System.currentTimeMillis(),
        ),
        mediaItems = emptyList(),
    )

    val favouritePearl = PearlWithMedia(
        pearl = KnowledgePearlEntity(
            id = "preview-favourite",
            title = "Wells score for PE",
            notes = "Clinical probability before imaging.",
            isFavourite = true,
            tags = listOf("respiratory", "emergency"),
        ),
        mediaItems = emptyList(),
    )

    val clinicalCasePearl = PearlWithMedia(
        pearl = KnowledgePearlEntity(
            id = "preview-clinical-case",
            title = "Chest pain in a 55-year-old",
            sourceReference = "Teaching case",
        ).withClinicalCasePayload(
            ClinicalCasePayload(
                history = "Central chest pain radiating to the jaw, diaphoresis.",
                examination = "Pale, clammy. BP 90/60. Heart sounds normal.",
                investigation = "ECG: ST elevation V2–V4. Troponin rising.",
                diagnosis = "Anterior STEMI",
                discussion = "Primary PCI pathway activated.",
                references = "ESC ACS guidelines",
            ),
        ),
        mediaItems = listOf(
            PearlMediaEntity(
                id = "preview-media-ecg",
                pearlId = "preview-clinical-case",
                type = "image",
                filename = "ecg.jpg",
                sectionTag = "investigation",
            ),
        ),
    )

    val publicPendingPearl = PearlWithMedia(
        pearl = KnowledgePearlEntity(
            id = "preview-public",
            title = "Submitted to public feed",
            notes = "Awaiting moderation.",
            isSharedPublicly = true,
            publicPearlID = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
            publicPearlStatus = "pending",
        ),
        mediaItems = emptyList(),
    )

    val allPearls: List<PearlWithMedia> = listOf(
        standardPearl,
        favouritePearl,
        clinicalCasePearl,
        publicPendingPearl,
    )
}
