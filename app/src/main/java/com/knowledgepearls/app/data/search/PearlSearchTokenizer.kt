package com.knowledgepearls.app.data.search

internal object PearlSearchTokenizer {
    private val tokenSplit = Regex("[^a-z0-9]+")

    fun tokenize(text: String): List<String> =
        split(text.lowercase()).filter { it.length >= 2 }.distinct()

    fun split(text: String): List<String> =
        tokenSplit.split(text).filter { it.isNotEmpty() }
}
