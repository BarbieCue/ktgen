package org.example

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
                val line = mutableStr.take(lineLength)
                appendLine(line.trim())
                mutableStr = mutableStr.substringAfter(line).trim()
            }
            while (symbolsCount() > symbolsTotal) delete(length - 1, length)
        }.trim()
    }
}

fun Collection<String>.lessonWords(charsHistory: String, lessonSymbols: String): List<String> {
    if (isEmpty() || lessonSymbols.areDigits()) return emptyList()
    val list = toList()
    val rand = Random.nextInt(0, max(size / 2, 1))
    return list.subList(rand, size).plus(list.subList(0, rand))
        .filter { it.consistsOfAny(charsHistory.plus(lessonSymbols).letters()) &&

                // words for letter groups
                if (lessonSymbols.isLetterGroup())
                    it.contains(lessonSymbols.unpackLetterGroup())

                // words for letters
                else if (lessonSymbols.containsLetters())
                    it.containsAny(lessonSymbols.letters())

                // words for e.g. punctuation marks
                else true
        }
}

fun StringBuilder.newCharacters(symbols: String): String {
    val new = symbols.unpack().filter { !toString().contains(it) }
    this.append(new)
    return new
}

class LessonBuilder(
    private val lineLength: Int,
    private val symbolsPerLesson: Int,
    private val dictionary: Collection<String>
) {
    private val lessonCtr = generateSequence(1) { it + 1 }.iterator()
    private val charsHistory = StringBuilder()

    fun newLesson(title: String, lessonSymbols: String, buildStep: L.() -> L): Lesson? =
        newLesson(lessonCtr.next(), title, lineLength, symbolsPerLesson, lessonSymbols, charsHistory, dictionary, buildStep)

    private fun newLesson(
        lessonCtr: Int,
        title: String,
        lineLength: Int,
        symbolsPerLesson: Int,
        lessonSymbols: String,
        charsHistory: StringBuilder,
        dictionary: Collection<String>,
        init: L.() -> L): Lesson? {

        val words = dictionary.lessonWords(charsHistory.toString(), lessonSymbols)

        val l = L(lessonSymbols, words).init()
        if (l.buildSteps.isEmpty() || lineLength <= 0 || symbolsPerLesson <= 0) return null

        val textSingleLine = invokeConcat(symbolsPerLesson, l.buildSteps)
        if (textSingleLine.isEmpty()) return null

        val text = textSingleLine.toTextBlock(symbolsPerLesson, lineLength)
        return Lesson(title = "${lessonCtr}: $title", newCharacters = charsHistory.newCharacters(lessonSymbols), text = text)
    }
}

class L(private val symbols: String, private val words: Collection<String>) {

    val buildSteps = mutableListOf<TextGenerator>()

    fun shuffleSymbols(segmentLength: Int): L {
        buildSteps.add { numberOfSymbols ->
            symbols.unpack().repeat(numberOfSymbols).shuffle().segment(segmentLength)
        }
        return this
    }

    fun repeatSymbols(segmentLength: Int): L {
        buildSteps.add { numberOfSymbols ->
            symbols.unpack().repeat(numberOfSymbols).segment(segmentLength)
        }
        return this
    }

    fun alternateSymbols(segmentLength: Int): L {
        buildSteps.add { numberOfSymbols ->
            val stretched = symbols.unpack().map { "$it".repeat(max(segmentLength, 0)) }.joinToString("")
            stretched.repeat(numberOfSymbols).segment(segmentLength)
        }
        return this
    }

    fun words(): L {
        val punctuationMarks = symbols.ww().ifEmpty { symbols.punctuationMarks() }
        buildSteps.add { numberOfSymbols ->
            if (punctuationMarks.isEmpty()) words.joinRepeat(numberOfSymbols)
            else words.prefixOrAppendPunctuationMarks(punctuationMarks).joinRepeat(numberOfSymbols)
        }
        return this
    }
}