package com.knowledgepearls.app.ui.media

import android.content.Context
import android.widget.Toast
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaSlide
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest

object DocumentMediaActions {
    suspend fun openSlide(
        context: Context,
        slide: PublicPearlMediaSlide.Document,
        onOpenPdfInApp: (PublicPearlMediaViewerRequest) -> Unit,
    ) {
        if (DocumentOpener.usesInAppPdfViewer(slide.url, slide.filename)) {
            onOpenPdfInApp(PublicPearlMediaViewerRequest(listOf(slide), initialIndex = 0))
            return
        }

        showToast(context, "Opening ${DocumentSupport.documentLabel(slide.filename)}…")
        when (val result = DocumentOpener.openDocument(context, slide.url, slide.filename)) {
            DocumentOpenResult.ViewInApp -> {
                onOpenPdfInApp(PublicPearlMediaViewerRequest(listOf(slide), initialIndex = 0))
            }
            DocumentOpenResult.OpenedExternally -> Unit
            is DocumentOpenResult.Failed -> showToast(context, result.message)
        }
    }

    suspend fun openAttachment(
        context: Context,
        url: String,
        filename: String,
        onOpenPdfInApp: (PublicPearlMediaViewerRequest) -> Unit,
    ) {
        openSlide(
            context = context,
            slide = PublicPearlMediaSlide.Document(url = url, filename = filename),
            onOpenPdfInApp = onOpenPdfInApp,
        )
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
