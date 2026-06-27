package com.knowledgepearls.app.ui.media

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

/**
 * Reads preview material from Office Open XML packages (DOCX, PPTX, etc.) without Quick Look.
 * Ported from iOS PearlsKit OfficeOpenXMLPreviewSource.
 */
object OfficeOpenXmlPreviewSource {
    private val zipSignature = byteArrayOf(0x50, 0x4B, 0x03, 0x04)

    fun isZipArchive(data: ByteArray): Boolean =
        data.size >= 4 && data.copyOfRange(0, 4).contentEquals(zipSignature)

    fun isWordProcessingOpenXml(data: ByteArray): Boolean {
        if (!isZipArchive(data)) return false
        val sample = data.copyOfRange(0, minOf(data.size, 524_288))
        return containsAscii(sample, "word/")
    }

    fun isPresentationOpenXml(data: ByteArray): Boolean {
        if (!isZipArchive(data)) return false
        val sample = data.copyOfRange(0, minOf(data.size, 524_288))
        return containsAscii(sample, "ppt/")
    }

    fun docxEmbeddedThumbnailData(data: ByteArray): ByteArray? {
        if (!isWordProcessingOpenXml(data)) return null
        for (name in listOf("docProps/thumbnail.jpeg", "docProps/thumbnail.jpg", "docProps/thumbnail.png")) {
            extractEntry(data, name)?.let { return it }
        }
        return null
    }

    fun pptxEmbeddedThumbnailData(data: ByteArray): ByteArray? {
        if (!isPresentationOpenXml(data)) return null
        for (name in listOf("docProps/thumbnail.jpeg", "docProps/thumbnail.jpg", "docProps/thumbnail.png")) {
            extractEntry(data, name)?.let { return it }
        }
        return null
    }

    fun docxFirstMediaImageData(data: ByteArray): ByteArray? {
        if (!isWordProcessingOpenXml(data)) return null
        return extractFirstEntry(data) { name ->
            name.startsWith("word/media/") &&
                (name.endsWith(".jpeg", ignoreCase = true) ||
                    name.endsWith(".jpg", ignoreCase = true) ||
                    name.endsWith(".png", ignoreCase = true) ||
                    name.endsWith(".webp", ignoreCase = true))
        }
    }

    fun pptxFirstMediaImageData(data: ByteArray): ByteArray? {
        if (!isPresentationOpenXml(data)) return null
        return extractFirstEntry(data) { name ->
            name.startsWith("ppt/media/") &&
                (name.endsWith(".jpeg", ignoreCase = true) ||
                    name.endsWith(".jpg", ignoreCase = true) ||
                    name.endsWith(".png", ignoreCase = true) ||
                    name.endsWith(".webp", ignoreCase = true))
        }
    }

    fun docxParagraphs(data: ByteArray): List<String> {
        val xmlData = extractEntry(data, "word/document.xml") ?: return emptyList()
        val xml = xmlData.decodeToString()
        return extractParagraphs(xml, textTag = "w:t")
    }

    fun pptxSlideParagraphs(data: ByteArray): List<List<String>> {
        if (!isPresentationOpenXml(data)) return emptyList()
        val slideEntries = listEntries(data)
            .filter { it.matches(Regex("ppt/slides/slide\\d+\\.xml")) }
            .sortedBy { entry ->
                entry.substringAfter("slide").substringBefore(".xml").toIntOrNull() ?: 0
            }

        return slideEntries.mapNotNull { entryName ->
            val xml = extractEntry(data, entryName)?.decodeToString() ?: return@mapNotNull null
            val paragraphs = extractDrawingMlParagraphs(xml)
            if (paragraphs.isEmpty()) null else paragraphs
        }
    }

    fun docxHtml(data: ByteArray): String? {
        val paragraphs = docxParagraphs(data)
        if (paragraphs.isEmpty()) return null
        return paragraphsToHtml(paragraphs, title = "Document")
    }

    fun pptxSlidesHtml(data: ByteArray): List<String> {
        return pptxSlideParagraphs(data).mapIndexed { index, paragraphs ->
            paragraphsToHtml(paragraphs, title = "Slide ${index + 1}")
        }
    }

    fun extractEntry(data: ByteArray, targetName: String): ByteArray? =
        extractFirstEntry(data) { it == targetName || it.endsWith("/$targetName") }

    private fun listEntries(data: ByteArray): List<String> {
        val names = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(data)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    names += entry.name
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return names
    }

    private fun extractFirstEntry(data: ByteArray, predicate: (String) -> Boolean): ByteArray? {
        ZipInputStream(ByteArrayInputStream(data)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && predicate(entry.name)) {
                    return zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return null
    }

    private fun extractParagraphs(xml: String, textTag: String): List<String> {
        val paragraphPattern = Regex("<w:p[\\s>][\\s\\S]*?</w:p>")
        val textPattern = Regex("<$textTag[^>]*>(.*?)</$textTag>", RegexOption.DOT_MATCHES_ALL)

        return paragraphPattern.findAll(xml).mapNotNull { match ->
            val block = match.value
            val parts = textPattern.findAll(block)
                .map { decodeXml(it.groupValues[1].trim()) }
                .filter { it.isNotBlank() }
                .toList()
            parts.joinToString(" ").takeIf { it.isNotBlank() }
        }.toList()
    }

    private fun extractDrawingMlParagraphs(xml: String): List<String> {
        val paragraphPattern = Regex("<a:p[\\s>][\\s\\S]*?</a:p>", RegexOption.DOT_MATCHES_ALL)
        val textPattern = Regex("<a:t[^>]*>(.*?)</a:t>", RegexOption.DOT_MATCHES_ALL)

        return paragraphPattern.findAll(xml).mapNotNull { match ->
            textPattern.findAll(match.value)
                .map { decodeXml(it.groupValues[1].trim()) }
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .takeIf { it.isNotBlank() }
        }.toList()
    }

    private fun decodeXml(value: String): String =
        value
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    fun paragraphsToHtml(paragraphs: List<String>, title: String): String {
        val body = paragraphs.joinToString("\n") { paragraph ->
            "<p>${escapeHtml(paragraph)}</p>"
        }
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <style>
                body {
                  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                  font-size: 16px;
                  line-height: 1.55;
                  color: #1c1c1e;
                  background: #ffffff;
                  margin: 0;
                  padding: 20px 22px 32px;
                }
                h1 {
                  font-size: 13px;
                  font-weight: 600;
                  color: #6b7280;
                  margin: 0 0 16px;
                  letter-spacing: 0.02em;
                  text-transform: uppercase;
                }
                p { margin: 0 0 14px; }
              </style>
            </head>
            <body>
              <h1>${escapeHtml(title)}</h1>
              $body
            </body>
            </html>
        """.trimIndent()
    }

    private fun containsAscii(data: ByteArray, needle: String): Boolean {
        val pattern = needle.toByteArray(Charsets.US_ASCII)
        if (pattern.isEmpty() || data.size < pattern.size) return false
        outer@ for (index in 0..data.size - pattern.size) {
            for (offset in pattern.indices) {
                if (data[index + offset] != pattern[offset]) continue@outer
            }
            return true
        }
        return false
    }
}
