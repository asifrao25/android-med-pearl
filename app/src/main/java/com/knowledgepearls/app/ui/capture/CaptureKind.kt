package com.knowledgepearls.app.ui.capture

import androidx.compose.ui.graphics.Color

enum class CaptureKind(
    val navTitle: String,
    val heroTitle: String,
    val heroSubtitle: String,
    val primary: Color,
    val secondary: Color,
) {
    QuickText(
        navTitle = "Quick Pearl",
        heroTitle = "Quick Pearl",
        heroSubtitle = "Capture a pearl with notes and optional photos, videos, or files.",
        primary = Color(0xFF59D18C),
        secondary = Color(0xFF2EADB8),
    ),
    WebLink(
        navTitle = "Add Web Link",
        heroTitle = "Save a Web Link",
        heroSubtitle = "Paste a URL — we'll fetch a preview and help you cite the source.",
        primary = Color(0xFF33B8F2),
        secondary = Color(0xFF5973FA),
    ),
    Media(
        navTitle = "Add Media",
        heroTitle = "Add Media",
        heroSubtitle = "Attach photos, videos, or documents, then add context and tags.",
        primary = Color(0xFF736BF9),
        secondary = Color(0xFFB861F2),
    ),
    ClinicalCase(
        navTitle = "Clinical Case",
        heroTitle = "Clinical Case",
        heroSubtitle = "Structured patient case with history, exam, investigation, and discussion.",
        primary = Color(0xFFF5A623),
        secondary = Color(0xFFE06B1F),
    ),
}
