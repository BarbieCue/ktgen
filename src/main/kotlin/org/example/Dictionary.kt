package org.example

import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import java.io.File


fun textFromWebsite(url: String): String = try {
    skrape(BrowserFetcher) {
        request {
            this.url = url
        }
        response {
            htmlDocument { text }.trim()
        }
    }
} catch (e: Exception) {
    System.err.println("Error while trying to read text from the given website. Message: ${e.message}")
    ""
}


fun textFromFile(path: String): String? = try {
    File(path).readText().trim()
} catch (e: Exception) {
    System.err.println(e.message)
    null
}

fun extractWords(text: String?, minWordLength: Int, maxWordLength: Int): List<String> =
    if (text.isNullOrEmpty()) emptyList() else
        text.split("\\s+|\\p{Punct}+".toRegex())
            .filter { it.matches(lettersRegex) && it.length in (minWordLength..maxWordLength) }

fun String.consistsOfAny(symbols: String): Boolean {
    val chars = toCharArray().distinct()
    return chars.count { symbols.toCharArray().distinct().contains(it) } == length - (length - chars.size)
}

fun String.containsAny(symbols: String): Boolean {
    return symbols.any { c -> contains(c) }
}

fun buildDictionary(
    dictionaryPath: String,
    scrapeUrl: String,
    minWordMaxLength: Int,
    maxWordLength: Int,
    dictionarySize: Int
): Collection<String> {
    if (dictionarySize <= 0) return emptyList()
    val file = if (dictionaryPath.isEmpty()) "" else textFromFile(dictionaryPath)
    val web = if (scrapeUrl.isEmpty()) "" else textFromWebsite(scrapeUrl)
    val dict = extractWords(file?.plus(" ").plus(web), minWordMaxLength, maxWordLength)
    return dict.asSequence().repeatInfinite().take(dictionarySize).toList()
}