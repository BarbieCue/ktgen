package org.example

import java.util.Random


/*
 Chars
 */

fun shuffle(symbols: String): String {
    val carr = symbols.toCharArray()
    carr.shuffle()
    return carr.concatToString().trim()
}

fun repeat(symbols: String, length: Int): String =
    if (length <= 0 || symbols.isEmpty()) ""
    else symbols.repeat(length/symbols.length+1).slice((0 until length)).trim()

fun segment(symbols: String, length: Int): String {
    if (length < 1) return ""
    return buildString {
        for (from in 0..symbols.length step length) {
            val to = if (from + length >= symbols.length) symbols.length - 1 else from + length - 1
            append(symbols.slice((from..to)))
            append(' ')
        }
    }.trim()
}


/*
 Words
 */

fun joinRepeat(words: Collection<String>, sumNonWhitespaceChars: Int): String =
    if (words.isEmpty() || sumNonWhitespaceChars <= 0
        || words.all { it.isEmpty() }
        || words.none { it.length <= sumNonWhitespaceChars }) ""
    else {
        val wordLoop = words
            .filter { it.length <= sumNonWhitespaceChars }
            .asSequence().repeatInfinite().iterator()
        buildString {
            var ctr = 0
            while (true) {
                val word = wordLoop.next().trim()
                ctr += word.length
                if (ctr > sumNonWhitespaceChars) {
                    val tooMuch = ctr - sumNonWhitespaceChars
                    val fillWord = words.find { it.length == word.length - tooMuch }
                    if (fillWord != null) append(fillWord)
                    else append(word.take(word.length - tooMuch))
                    break
                }
                append(word)
                append(" ")
            }
        }.trim()
    }

fun <T> Sequence<T>.repeatInfinite() = sequence {
    if (any()) while (true) yieldAll(this@repeatInfinite)
}


/*
 Symbols
 */

internal val pairs = hashMapOf(
    '(' to ')',
    '[' to ']',
    '{' to '}',
    '<' to '>',
    '"' to '"',
    '\'' to '\'',
    '`' to '`'
)

internal val pairsRevers = pairs.map { it.value to it.key }.toMap()

internal fun splitWWLeftRight(wwString: String): Pair<String, String> =
    wwString.substringBefore("WW") to wwString.substringAfter("WW")

internal fun randomPair(left: String, right: String): Pair<String, String> {
    return if (left.isNotEmpty() && right.isNotEmpty())
        if (Random().nextInt(100) > 50) {
            val l = left.random()
            val r = if (pairs.containsKey(l) && right.contains(pairs[l]!!)) pairs[l] else ""
            "$l" to "$r"
        } else {
            val r = right.random()
            val l = if (pairsRevers.containsKey(r) && left.contains(pairsRevers[r]!!)) pairsRevers[r] else ""
            "$l" to "$r"
        }
    else if (left.isEmpty() && right.isNotEmpty()) {
        "" to "${right.random()}"
    }
    else if (left.isNotEmpty() && right.isEmpty()) {
        "${left.random()}" to ""
    }
    else {
        "" to ""
    }
}

fun punctuationMarks(punctuationMarks: String, length: Int): String {
    if (punctuationMarks.isEmpty() || length <= 0) return ""
    val (left, right) = splitWWLeftRight(punctuationMarks)
    if ((left+right).isEmpty()) return ""
    return buildString {
        while (this.length < length) {
            val (l, r) = randomPair(left, right)
            append(if (punctuationMarks.matches(wwRegex)) "$l$r" else {
                val set = (l+r).toSet()
                if (set.isNotEmpty()) "${set.random()}" else ""
            })
        }
    }
}

fun wordsWithPunctuationMarks(words: Collection<String>, punctuationMarks: String): List<String> {
    if (words.isEmpty() || punctuationMarks.isEmpty()) return emptyList()
    val (left, right) = splitWWLeftRight(punctuationMarks)
    return words.map {
        val (l, r) = randomPair(left, right)
        if (punctuationMarks.matches(wwRegex)) "$l$it$r" else {
            if ((l+r).isEmpty()) it
            else if (Random().nextInt(100) > 50)
                "$it${(l+r).random()}"
            else
                "${(l+r).toSet().random()}$it"
        }
    }
}