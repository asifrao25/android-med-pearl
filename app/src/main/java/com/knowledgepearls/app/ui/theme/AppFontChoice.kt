package com.knowledgepearls.app.ui.theme

enum class AppFontChoice(
    val storageKey: String,
    val label: String,
    val subtitle: String,
    val previewLine: String = "Clinical pearls should be easy to read at a glance.",
) {
    Inter(
        storageKey = "inter",
        label = "Inter",
        subtitle = "Clear, modern text designed for screens",
    ),
    SourceSans3(
        storageKey = "source_sans_3",
        label = "Source Sans 3",
        subtitle = "Relaxed reading for longer notes",
    ),
    System(
        storageKey = "system",
        label = "System default",
        subtitle = "Your device's built-in font (Roboto on most Android phones)",
    ),
    ;

    companion object {
        fun fromStorageKey(key: String?): AppFontChoice =
            entries.firstOrNull { it.storageKey == key } ?: Inter
    }
}
