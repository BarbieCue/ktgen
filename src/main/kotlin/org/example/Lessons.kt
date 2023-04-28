package org.example

import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random


/*
 String filter
 */

val wwRegex = "\\p{Punct}*WW\\p{Punct}*".toRegex()
fun ww(s: String): String = wwRegex.find(s)?.value ?: ""
fun wwUnpack(s: String): String = ww(s).replace("WW", "")

val lettersRegex = "[A-Za-züäößÜÄÖẞ]+".toRegex()
fun letters(s: String): String =
    lettersRegex.findAll(
        s.replace(wwRegex, "")
         .replace(letterGroupRegex, ""))
        .joinToString("") { it.value }

fun digits(s: String): String = "\\d+".toRegex().findAll(s).joinToString("") { it.value }

val letterGroupRegex = "\\[${lettersRegex.pattern}\\]".toRegex()
fun letterGroup(s: String): String = letterGroupRegex.find(s.replace(ww(s), ""))?.value ?: ""
fun letterGroupUnpack(s: String): String = letterGroup(s).replace("[", "").replace("]", "")

fun unconditionalPunctuation(s: String): String =
    "\\p{Punct}+".toRegex()
        .find(s.replace(ww(s), "")
               .replace(letterGroup(s), ""))?.value ?: ""

fun StringBuilder.newCharacters(symbols: String): String {
    val new = unpack(symbols).filter { !this.toString().contains(it) }
    this.append(new)
    return new
}

fun unpack(symbols: String): String {
    return if (symbols.matches(wwRegex)) wwUnpack(symbols)
    else if (symbols.matches(letterGroupRegex)) letterGroupUnpack(symbols)
    else symbols
}

fun Collection<String>.lessonWords(charsHistory: String, lessonSymbols: String): List<String> {
    if (isEmpty()) return emptyList()
    val list = toList()
    val rand = Random.nextInt(0, max(size / 2, 1))
    return list.subList(rand, size).plus(list.subList(0, rand))
        .filter { it.consistsOfAny(letters(charsHistory)) &&

                // words for letter groups
                if (letterGroup(lessonSymbols).isNotEmpty())
                    it.contains(letterGroupUnpack(lessonSymbols))

                // words for letters
                else if (letters(lessonSymbols).isNotEmpty())
                    it.containsAny(letters(lessonSymbols))

                // words for e.g. punctuation marks
                else true
        }
}


/*
 Lesson builder
 */

fun buildLesson(title: String = "", lineLength: Int, symbolsPerLesson: Int, newCharacters: String = "", init: L.() -> L): Lesson {
    val l = L(title = title).init()
    if (l.buildSteps.isEmpty() || lineLength <= 0 || symbolsPerLesson <= 0)
        return Lesson(id = l.id, title = title, newCharacters = newCharacters, text = "")

    val textAsSingleLine = invokeConcatSingleLine(symbolsPerLesson, l.buildSteps)
    if (textAsSingleLine.isEmpty())
        return Lesson(id = l.id, title = l.title, newCharacters = newCharacters, text = "")

    val text = toTextBlock(textAsSingleLine, symbolsPerLesson, lineLength)
    return Lesson(id = l.id, title = title, newCharacters = newCharacters, text = text)
}

internal fun symbolsPerGenerator(symbolsPerLesson: Int, numberOfGenerators: Int): Int {
    return if (symbolsPerLesson <= 0 || numberOfGenerators <= 0) 0
    else if (symbolsPerLesson < numberOfGenerators) symbolsPerLesson
    else symbolsPerLesson / max(numberOfGenerators, 1)
}

internal fun invokeConcatSingleLine(symbolsPerLesson: Int, textGenerators: List<TextGenerator>): String {
    if (symbolsPerLesson <= 0 || textGenerators.isEmpty()) return ""
    val symbolsPerGenerator = symbolsPerGenerator(symbolsPerLesson, textGenerators.size)
    return textGenerators.invokeConcat(symbolsPerGenerator, symbolsPerLesson)
}

internal fun List<TextGenerator>.invokeConcat(symbolsPerGenerator: Int, symbolsTotal: Int): String {
    return buildString {
        while (symbolsCount() < symbolsTotal) {
            this@invokeConcat.forEach {
                val generatorResult = it(symbolsPerGenerator)
                for (c in generatorResult) {
                    append(c)
                    if (symbolsCount() >= symbolsTotal) return@buildString
                }
                append(" ")
            }
            if (symbolsCount() == 0) return ""
        }
    }.trim()
}

internal fun StringBuilder.symbolsCount(): Int = count { !it.isWhitespace() }

internal fun String.symbolsCount(): Int = count { !it.isWhitespace() }

fun String.normalizeWhitespaces(): String = replace("\\s{2,}".toRegex(), " ")

fun toTextBlock(str: String, symbolsTotal: Int, lineLength: Int): String {
    if (str.isEmpty() || symbolsTotal <= 0 || lineLength <= 0) return ""
    if (str.length < lineLength) return str
    return if (lineLength > symbolsTotal)
        buildString {
            for (c in str) {
                append(c)
                if (symbolsCount() == symbolsTotal) break
            }
        }.trim()
    else {
        buildString {
            val bound = min(symbolsTotal, str.symbolsCount())
            var mutableStr = str.normalizeWhitespaces()
            while (symbolsCount() < bound) {
                val line = mutableStr.take(lineLength)
                appendLine(line.trim())
                mutableStr = mutableStr.substringAfter(line).trim()
            }
            while (symbolsCount() > symbolsTotal) delete(length - 1, length)
        }.trim()
    }
}

typealias TextGenerator = (numberOfSymbols: Int) -> String

class L(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
) {
    val buildSteps = mutableListOf<TextGenerator>()

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

    fun alternatingSymbols(symbols: String, segmentLength: Int): L {
        buildSteps.add { numberOfSymbols ->
            val stretched = symbols.map { "$it".repeat(max(segmentLength, 0)) }.joinToString("")
            segment(repeat(stretched, numberOfSymbols), segmentLength)
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