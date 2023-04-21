package org.example

import java.util.*
import kotlin.math.max
import kotlin.random.Random


fun StringBuilder.newCharacters(symbols: String): String {
    val new = symbols.filter { !this.toString().contains(it) }
    this.append(new)
    return new
}

fun Collection<String>.lessonWords(charsHistory: String, lessonSymbols: String): List<String> {
    if (isEmpty()) return emptyList()
    val list = toList()
    val rand = Random.nextInt(0, size / 2)
    return list.subList(rand, size).plus(list.subList(0, rand))
        .filter { it.consistsOfAny(letters(charsHistory)) &&

                // words for letter groups
                if (letterGroup(lessonSymbols).isNotEmpty())
                    it.contains(letterGroupLetters(lessonSymbols))

                // words for letters
                else if (letters(lessonSymbols).isNotEmpty())
                    it.containsAny(letters(lessonSymbols))

                // words for e.g. symbols
                else true
        }
}


/*
 String filter
 */

val wwRegex = "\\p{Punct}*WW\\p{Punct}*".toRegex()
fun ww(s: String): String = wwRegex.find(s)?.value ?: ""

fun wwSymbols(s: String): String = ww(s).replace("WW", "")

val lettersRegex = "[A-Za-züäößÜÄÖẞ]+".toRegex()
fun letters(s: String): String =
    lettersRegex.findAll(
        s.replace(wwRegex, "")
         .replace(letterGroupRegex, ""))
        .joinToString("") { it.value }

fun digits(s: String): String = "\\d+".toRegex().findAll(s).joinToString("") { it.value }

val letterGroupRegex = "\\[${lettersRegex.pattern}\\]".toRegex()
fun letterGroup(s: String): String = letterGroupRegex.find(s.replace(ww(s), ""))?.value ?: ""
fun letterGroupLetters(s: String): String = letterGroup(s).replace("[", "").replace("]", "")

fun unconditionalPunctuation(s: String): String =
    "\\p{Punct}+".toRegex()
        .find(s.replace(ww(s), "")
               .replace(letterGroup(s), ""))?.value ?: ""


/*
 Lesson builder
 */

fun buildLesson(title: String = "", lineLength: Int, symbolsPerLesson: Int, newCharacters: String = "", init: L.() -> L): Lesson =
    L(title = title, newCharacters = newCharacters, lineLength = lineLength, symbolsPerLesson = symbolsPerLesson).init().build()

class L(
    private val id: String = UUID.randomUUID().toString(),
    private val title: String = "",
    private val newCharacters: String = "",
    private val lineLength: Int = 0,
    private val symbolsPerLesson: Int = 0,
) {
    internal fun build(): Lesson {
        if (buildSteps.isEmpty() || lineLength <= 0 || symbolsPerLesson <= 0)
            return Lesson(id = id, title = title, newCharacters = newCharacters, text = "")

        val numberOfSymbols =
            if (symbolsPerLesson < buildSteps.size) symbolsPerLesson
            else symbolsPerLesson / max(buildSteps.size, 1)

        val str = buildString {
            var idx = 0
            while (count { !it.isWhitespace() } < symbolsPerLesson) {
                val str = buildSteps[idx](numberOfSymbols)
                append(str)
                append(" ")
                idx++
                if (idx == buildSteps.size && count { !it.isWhitespace() } == 0) return@buildString
                if (idx == buildSteps.size) idx = 0
            }
        }.trim()

        if (str.isEmpty())
            return Lesson(id = id, title = title, newCharacters = newCharacters, text = "")

        val text = buildString {
            if (lineLength <= symbolsPerLesson) {
                val sb = StringBuilder(str.trim())
                while (count { !it.isWhitespace() } < symbolsPerLesson) {
                    val line = sb.take(lineLength).trim()
                    sb.delete(0, lineLength)
                    while (sb.isNotEmpty() && sb.first().isWhitespace())
                        sb.delete(0, 1)
                    appendLine(line)
                    while (count { !it.isWhitespace() } > symbolsPerLesson)
                        delete(length - 1, length)
                }
            } else {
                str.forEach { char ->
                    append(char)
                    val symbolsCnt = count { !it.isWhitespace() }
                    if (symbolsCnt == symbolsPerLesson) {
                        return@buildString
                    }
                }
            }
        }.trim()

        return Lesson(id = id, title = title, newCharacters = newCharacters, text = text)
    }

    private val buildSteps = mutableListOf<(numberOfSymbols: Int) -> String>()

    fun shuffledSymbols(symbols: String, segmentLength: Int): L {
        buildSteps.add { numberOfSymbols ->
            segment(shuffle(repeat(symbols, numberOfSymbols)), segmentLength)
        }
        return this
    }

    fun repeatSymbols(symbols: String, segmentLength: Int): L {
        buildSteps.add { numberOfSymbols ->
            segment(repeat(symbols, numberOfSymbols), segmentLength)
        }
        return this
    }

    fun words(words: Collection<String>): L {
        buildSteps.add { numberOfSymbols ->
            joinRepeat(words, numberOfSymbols)
        }
        return this
    }

    fun randomLeftRightPunctuationMarks(wwString: String, segmentLength: Int): L {
        buildSteps.add { numberOfSymbols ->
            segment(punctuationMarks(wwString, numberOfSymbols), segmentLength)
        }
        return this
    }

    fun wordsWithLeftRightPunctuationMarks(words: Collection<String>, wwString: String): L {
        buildSteps.add { numberOfSymbols ->
            joinRepeat(wordsWithPunctuationMarks(words, wwString), numberOfSymbols)
        }
        return this
    }

    fun wordsWithUnconditionalPunctuationMarks(words: Collection<String>, punctuationMarks: String): L {
        buildSteps.add { numberOfSymbols ->
            joinRepeat(wordsWithPunctuationMarks(words, punctuationMarks), numberOfSymbols)
        }
        return this
    }
}