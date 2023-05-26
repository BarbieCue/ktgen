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

fun Collection<String>.lessonWords(charsHistory: String, lessonSymbols: String): List<String> {
    if (isEmpty() || lessonSymbols.areDigits()) return emptyList()
    val list = toList()
    val rand = Random.nextInt(0, max(size / 2, 1))
    return list.subList(rand, size).plus(list.subList(0, rand))
        .filter { it.consistsOfAny(charsHistory.plus(lessonSymbols).letters()) &&
                if (lessonSymbols.isLetterGroup()) // words for letter groups
                    it.contains(lessonSymbols.unpackLetterGroup())
                else if (lessonSymbols.containsLetters()) // words for letters
                    it.containsAny(lessonSymbols.letters())
                else true // words for e.g. punctuation marks
        }
}

fun StringBuilder.newCharacters(symbols: String): String {
    val new = symbols.unpack().filter { !toString().contains(it) }
    this.append(new)
    return new
}

internal fun String.isVeryDifferent(other: String?): Boolean {
    if (other == null) return true
    val levenshteinDistance = LevenshteinDistance().apply(other, this)
    if (levenshteinDistance == 0) return false
    val similarityScore = levenshteinDistance.toDouble() / length.toDouble()
    return similarityScore > 0.6 // 0 = equal; 1 = completely different
}

internal fun String.isExciting(): Boolean {
    val words = split("\\s".toRegex())
    if (words.size <= 10) return true
    val differentWords = words.distinct()
    return differentWords.size > 10
}

class LessonBuilder(
    private val lineLength: Int,
    private val symbolsPerLesson: Int,
    private val dictionary: Collection<String>
) {
    private val lessonCtr = generateSequence(1) { it + 1 }.iterator()
    private val symbolHistory = StringBuilder()
    private val createdLessons = mutableListOf<Lesson>()
    val lessons: List<Lesson> get() = createdLessons

    fun newLesson(title: String, lessonSymbols: String, buildStep: L.() -> L): LessonBuilder =
        newLesson(title, listOf(lessonSymbols), buildStep)

    fun newLesson(title: String, symbols: List<String>, buildStep: L.() -> L): LessonBuilder {
        val text = calculateLessonText(lineLength, symbolsPerLesson, symbols, symbolHistory, dictionary, buildStep)
        if (text.isEmpty()) return this
        val newCharacters = symbolHistory.newCharacters(symbols.joinToString(""))
        val lastLesson = createdLessons.lastOrNull()
        if (newCharacters.isNotEmpty() || (text.isExciting() && text.isVeryDifferent(lastLesson?.text)))
            createdLessons.add(Lesson(title = "${lessonCtr.next()}: $title", newCharacters = newCharacters, text = text))
        return this
    }

    private fun calculateLessonText(
        lineLength: Int,
        symbolsPerLesson: Int,
        symbols: List<String>,
        charsHistory: StringBuilder,
        dictionary: Collection<String>,
        init: L.() -> L): String {
        val l = L(symbols, charsHistory.toString(), dictionary).init()
        if (l.buildSteps.isEmpty() || lineLength <= 0 || symbolsPerLesson <= 0) return ""
        val textSingleLine = invokeConcat(symbolsPerLesson, l.buildSteps)
        if (textSingleLine.isEmpty()) return ""
        return textSingleLine.toTextBlock(symbolsPerLesson, lineLength)
    }
}

class L(
    private val symbols: List<String>,
    private val charsHistory: String,
    private val dictionary: Collection<String>) {

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
                val words = dictionary.lessonWords(charsHistory, it)
                if (punctuationMarks.isEmpty()) words.joinRepeat(numberOfSymbols / symbols.size)
                else words.prefixOrAppendPunctuationMarks(punctuationMarks).joinRepeat(numberOfSymbols / symbols.size)
            }
        }
        return this
    }
}