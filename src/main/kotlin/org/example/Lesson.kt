package org.example

import org.apache.commons.text.similarity.LevenshteinDistance
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random


/*
 Known pattern
 */


val wwRegex = "\\p{Punct}*WW\\p{Punct}*".toRegex()
fun String.ww(): String = wwRegex.find(this)?.value ?: ""
fun String.unpackWW(): String = replace("WW", "")

val lowerLetters = "[a-züäöß]+".toRegex()
val upperLetters = "[A-ZÜÄÖẞ]+".toRegex()
val lettersRegex = "[${lowerLetters.pattern}${upperLetters.pattern}]+".toRegex()
fun String.letters(): String = lettersRegex
    .findAll(replace(wwRegex, "")
        .replace(letterGroupRegex, "")).joinToString("") { it.value }
fun String.containsLetters() = replace(ww(), "").replace(letterGroup(), "").contains(lettersRegex)

val digitRegex = "\\d+".toRegex()
fun String.areDigits(): Boolean = matches(digitRegex)

val letterGroupRegex = "\\[${lettersRegex.pattern}\\]".toRegex()
fun String.letterGroup(): String = letterGroupRegex.find(replace(ww(), ""))?.value ?: ""
fun String.isLetterGroup(): Boolean = letterGroup().isNotEmpty()
fun String.unpackLetterGroup(): String =
    if (letterGroup().isNotEmpty())
        replace(letterGroup(), letterGroup().replace("[", "").replace("]", ""))
    else replace(ww(), "")

val punctuationRegex = "\\p{Punct}+".toRegex()
fun String.punctuationMarks(): String = punctuationRegex.findAll(
    replace(ww(), "").replace(letterGroup(), "")).joinToString("") { it.value }

fun String.unpack(): String = unpackWW().unpackLetterGroup()


/*
 Lesson building
 */


typealias TextGenerator = (numberOfSymbols: Int) -> String

internal fun symbolsPerGenerator(symbolsPerLesson: Int, numberOfGenerators: Int): Int {
    return if (symbolsPerLesson <= 0 || numberOfGenerators <= 0) 0
    else if (symbolsPerLesson < numberOfGenerators) symbolsPerLesson
    else symbolsPerLesson / max(numberOfGenerators, 1)
}

internal fun List<TextGenerator>.invokeConcat(symbolsPerGenerator: Int, symbolsTotal: Int): String {
    if (symbolsPerGenerator <= 0) return ""
    val results = map { it(symbolsPerGenerator) }
    if (results.any { it.isEmpty() }) return ""
    return buildString {
        while (symbolsCount() < symbolsTotal) {
            results.forEach {
                for (c in it) {
                    append(c)
                    if (symbolsCount() >= symbolsTotal) return@buildString
                }
                append(" ")
            }
            if (symbolsCount() == 0) return ""
        }
    }.trim()
}

internal fun invokeConcat(symbolsPerLesson: Int, textGenerators: List<TextGenerator>): String {
    val symbolsPerGenerator = symbolsPerGenerator(symbolsPerLesson, textGenerators.size)
    return textGenerators.invokeConcat(symbolsPerGenerator, symbolsPerLesson)
}

internal fun StringBuilder.symbolsCount(): Int = count { !it.isWhitespace() }

internal fun String.symbolsCount(): Int = count { !it.isWhitespace() }

internal fun String.normalizeWhitespaces(): String = replace("\\s{2,}".toRegex(), " ")

fun String.toTextBlock(symbolsTotal: Int, lineLength: Int): String {
    if (isEmpty() || symbolsTotal <= 0 || lineLength <= 0) return ""
    if (length < lineLength) return this
    return if (lineLength > symbolsTotal)
        buildString {
            for (c in this@toTextBlock) {
                append(c)
                if (symbolsCount() == symbolsTotal) break
            }
        }.trim()
    else {
        buildString {
            val bound = min(symbolsTotal, this@toTextBlock.symbolsCount())
            var mutableStr = this@toTextBlock.normalizeWhitespaces()
            while (symbolsCount() < bound) {
                val line = mutableStr.substringAtNearestWhitespace(lineLength)
                appendLine(line.trim())
                mutableStr = mutableStr.substringAfter(line).trim()
            }
            while (symbolsCount() > symbolsTotal) delete(length - 1, length)
        }.trim()
    }
}

fun String.substringAtNearestWhitespace(desiredLength: Int): String {
    if (isEmpty() || desiredLength <= 0) return ""
    if (desiredLength >= length) return this

    fun String.findNearestWhitespace(startIndex: Int, currentIndex: Int,
                                     forwardIndex: Int = 0, backwardIndex: Int = 0,
                                     backwards: Boolean = false): Int {
        val char = getOrNull(currentIndex) ?: return 0
        if (char.isWhitespace()) return currentIndex
        val nextDirection = if (currentIndex == 0) false // reached string start, only go forward
                            else if (currentIndex >= length) true // reached string end, only go backward
                            else !backwards // change direction
        val nextForwardStep = if (nextDirection) forwardIndex else forwardIndex + 1
        val nextBackwardStep = if (nextDirection) backwardIndex + 1 else backwardIndex
        val nextIndex = if (nextDirection) startIndex - nextBackwardStep else startIndex + nextForwardStep
        return findNearestWhitespace(startIndex, nextIndex, nextForwardStep, nextBackwardStep, nextDirection)
    }

    if (this[desiredLength].isWhitespace()) return substring(0, desiredLength)
    val whitespaceIndex = findNearestWhitespace(desiredLength, desiredLength)
    if (whitespaceIndex == 0) return substring(0, desiredLength)
    return substring(0, whitespaceIndex)
}

fun Sequence<String>.lessonWords(symbolHistory: String, symbols: String): Collection<String> {
    if (none() || symbols.areDigits()) return emptyList()
    val historyLetters = symbolHistory.letters()
    val letters = symbols.letters()
    val isLetterGroup = symbols.isLetterGroup()
    val containsLetters = symbols.containsLetters()
    val filtered = filter {
        it.consistsOfAny(historyLetters) &&
        if (isLetterGroup)
            it.contains(symbols.unpackLetterGroup())
        else if (containsLetters)
            it.containsAny(letters)
        else true
    }.toList()
    val rand = Random.nextInt(0, max(filtered.size - 1, 1))
    return filtered.subList(rand, filtered.size).plus(filtered.subList(0, rand))
}

fun StringBuilder.newCharacters(symbols: String): String {
    val new = symbols.unpack().filter { !toString().contains(it) }
    this.append(new)
    return new
}

typealias LessonFilter = (Lesson?, Lesson) -> Boolean

class Filter {
    companion object {
        fun relativeLevenshteinDistanceFromLessonBefore(minimumDistance: Double): LessonFilter {
            return { lastLesson: Lesson?, lesson: Lesson ->
                // distance: 0 = equal; 1 = completely different
                lesson.text.relativeLevenshteinDistance(lastLesson?.text) > minimumDistance
            }
        }
        fun containsAtLeastDifferentWords(n: Int): LessonFilter {
            return { _: Lesson?, lesson: Lesson ->
                lesson.text.differentWords(n)
            }
        }
    }
}

internal fun String.relativeLevenshteinDistance(other: String?): Double {
    if (isEmpty()) return 0.0
    if (other.isNullOrEmpty()) return 1.0
    val levenshteinDistance = LevenshteinDistance().apply(this, other)
    if (levenshteinDistance == 0) return 0.0
    return levenshteinDistance.toDouble() / length.toDouble()
}

internal fun String.differentWords(n: Int): Boolean {
    if (isEmpty()) return false
    val words = split("\\s".toRegex())
    if (words.size <= n) return true
    val differentWords = words.distinct()
    return differentWords.size > n
}

class LessonBuilder(
    private val lineLength: Int,
    private val symbolsPerLesson: Int,
    private val dictionary: Sequence<String>,
) {
    private val lessonCtr = generateSequence(1) { it + 1 }.iterator()
    private val symbolHistory = StringBuilder()
    private val lessonFilter = mutableListOf<LessonFilter>()
    val lessons = mutableListOf<Lesson>()

    fun withLessonFilter(filter: LessonFilter): LessonBuilder {
        lessonFilter.add(filter)
        return this
    }

    fun newLesson(title: String, lessonSymbols: String, buildStep: L.() -> L): LessonBuilder =
        newLesson(title, listOf(lessonSymbols), buildStep)

    fun newLesson(title: String, symbols: List<String>, buildStep: L.() -> L): LessonBuilder {
        val newCharacters = symbolHistory.newCharacters(symbols.joinToString(""))
        val text = calculateLessonText(lineLength, symbolsPerLesson, symbols, symbolHistory.toString(), dictionary, buildStep)
        if (text.isEmpty()) return this
        val draft = Lesson(title = title, newCharacters = newCharacters, text = text)
        if (newCharacters.isNotEmpty() || lessonFilter.all { it(lessons.lastOrNull(), draft) })
            lessons.add(
                Lesson(
                    title = "${lessonCtr.next()}: ${draft.title}",
                    newCharacters = draft.newCharacters,
                    text = draft.text)
            )
        return this
    }

    private fun calculateLessonText(
        lineLength: Int,
        symbolsPerLesson: Int,
        symbols: List<String>,
        symbolHistory: String,
        dictionary: Sequence<String>,
        init: L.() -> L): String {
        val l = L(symbols, symbolHistory, dictionary).init()
        if (l.buildSteps.isEmpty() || lineLength <= 0 || symbolsPerLesson <= 0) return ""
        val textSingleLine = invokeConcat(symbolsPerLesson, l.buildSteps)
        if (textSingleLine.isEmpty()) return ""
        return textSingleLine.toTextBlock(symbolsPerLesson, lineLength)
    }
}

class L(
    private val symbols: List<String>,
    private val symbolHistory: String,
    private val dictionary: Sequence<String>) {

    val buildSteps = mutableListOf<TextGenerator>()

    fun shuffleSymbols(segmentLength: Int): L {
        buildSteps.add { numberOfSymbols ->
            symbols.joinToString(" ") {
                it.unpack().repeat(numberOfSymbols / symbols.size).shuffle().segment(segmentLength)
            }
        }
        return this
    }

    fun repeatSymbols(segmentLength: Int): L {
        buildSteps.add { numberOfSymbols ->
            symbols.joinToString(" ") {
                it.unpack().repeat(numberOfSymbols / symbols.size).segment(segmentLength)
            }
        }
        return this
    }

    fun alternateSymbols(segmentLength: Int): L {
        buildSteps.add { numberOfSymbols ->
            symbols.joinToString(" ") {
                val stretched = it.unpack().map { char -> "$char".repeat(max(segmentLength, 0)) }.joinToString("")
                stretched.repeat(numberOfSymbols / symbols.size).segment(segmentLength)
            }
        }
        return this
    }

    fun words(): L {
        buildSteps.add { numberOfSymbols ->
            symbols.joinToString(" ") {
                val punctuationMarks = it.ww().ifEmpty { it.punctuationMarks() }
                val words = dictionary.lessonWords(symbolHistory, it)
                if (punctuationMarks.isEmpty()) words.joinRepeat(numberOfSymbols / symbols.size)
                else words.prefixOrAppendPunctuationMarks(punctuationMarks).joinRepeat(numberOfSymbols / symbols.size)
            }
        }
        return this
    }
}