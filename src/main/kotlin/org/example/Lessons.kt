package org.example

import java.util.*
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
                    if (letters(lessonSymbols).isEmpty()) true
                    else it.containsAny(letters(lessonSymbols))
        }
}


/*
 String filter
 */

fun ww(s: String): String {
    val matchResult = "\\p{Punct}*WW\\p{Punct}*".toRegex().find(s)
    return matchResult?.value ?: ""
}

fun wwSymbols(s: String): String = ww(s).replace("WW", "")

val lettersRegex = "[A-Za-züäößÜÄÖẞ]+".toRegex()
fun letters(s: String): String =
    lettersRegex
        .findAll(s.replace(ww(s), ""))
        .joinToString("") { it.value }

fun digits(s: String): String =
    "\\d+".toRegex()
        .findAll(s.replace(ww(s), ""))
        .joinToString("") { it.value }

fun unconditionalPunctuation(s: String): String =
    "\\p{Punct}*".toRegex()
        .findAll(s.replace(ww(s), ""))
        .joinToString("") { it.value }


/*
 Lesson builder
 */

fun buildLesson(title: String, lineLength: Int, newCharacters: String = "", init: L.() -> L): Lesson =
    L(title = title, newCharacters = newCharacters, lineLength = lineLength).init().build()

class L(
    private val id: String = UUID.randomUUID().toString(),
    private val title: String,
    private val newCharacters: String,
    private val lineLength: Int,
    private val text: String = ""
) {

    internal fun build(): Lesson = Lesson(id = id, title = title, newCharacters = newCharacters, text = text)

    private val sb = StringBuilder()

    fun shuffledSymbolsLine(symbols: String, segmentLength: Int): L {
        sb.appendLine(cutEnd(segment(shuffle(repeat(symbols, lineLength)), segmentLength), lineLength))
        return L(id, title, newCharacters, lineLength, sb.toString().trimEnd())
    }

    fun repeatedSymbolsLine(symbols: String, segmentLength: Int): L {
        sb.appendLine(cutEnd(segment(repeat(symbols, lineLength), segmentLength), lineLength))
        return L(id, title, newCharacters, lineLength, sb.toString().trimEnd())
    }

    fun wordsMultiline(words: Collection<String>, wordCount: Int): L {
        sb.appendLine(toText(words.takeRepeat(wordCount), lineLength))
        return L(id, title, newCharacters, lineLength, sb.toString().trimEnd())
    }

    fun randomLeftRightPunctuationMarks(wwString: String, segmentLength: Int): L {
        sb.appendLine(cutEnd(segment(punctuationMarksLine(wwString, lineLength, true), segmentLength), lineLength))
        return L(id, title, newCharacters, lineLength, sb.toString().trimEnd())
    }

    fun wordsWithLeftRightPunctuationMarksMultiline(words: Collection<String>, wwString: String, wordCount: Int): L {
        sb.appendLine(toText(wordsWithPunctuationMarks(words.takeRepeat(wordCount), wwString, true), lineLength))
        return L(id, title, newCharacters, lineLength, sb.toString().trimEnd())
    }

    fun randomUnconditionalPunctuationMarks(punctuationMarks: String, segmentLength: Int): L {
        sb.appendLine(cutEnd(segment(punctuationMarksLine(punctuationMarks, lineLength), segmentLength), lineLength))
        return L(id, title, newCharacters, lineLength, sb.toString().trimEnd())
    }

    fun wordsWithUnconditionalPunctuationMarksMultiline(words: Collection<String>, punctuationMarks: String, wordCount: Int): L {
        sb.appendLine(toText(wordsWithPunctuationMarks(words.takeRepeat(wordCount), punctuationMarks), lineLength))
        return L(id, title, newCharacters, lineLength, sb.toString().trimEnd())
    }
}