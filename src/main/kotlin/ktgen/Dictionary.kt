package ktgen

import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import java.io.File
import kotlin.math.max


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

fun extractWords(text: String?, minWordLength: Int, maxWordLength: Int): Sequence<String> =
    if (text.isNullOrEmpty() || minWordLength > maxWordLength || maxWordLength <= 0) emptySequence()
    else {
        val lengthRange = max(minWordLength, 0)..maxWordLength
        text.splitToSequence("\\s+|\\p{Punct}+".toRegex())
            .filter { it.matches(lettersRegex) && it.length in (lengthRange) }
    }

fun String.consistsOf(symbols: String): Boolean = all { symbols.contains(it) }

fun String.containsAny(symbols: String): Boolean = symbols.any { c -> contains(c) }

fun buildDictionary(
    dictionaryPath: String,
    scrapeUrl: String,
    minWordLength: Int,
    maxWordLength: Int,
    dictionarySize: Int
): Sequence<String> {
    if (dictionarySize <= 0) return emptySequence()
    val file = if (dictionaryPath.isEmpty()) "" else textFromFile(dictionaryPath)
    val web = if (scrapeUrl.isEmpty()) "" else textFromWebsite(scrapeUrl)
    val dict = extractWords(file?.plus(" ").plus(web), minWordLength, maxWordLength)
    return dict.take(dictionarySize)
}