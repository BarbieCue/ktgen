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

fun cutEnd(str: String, length: Int): String = str.take(if (length < 0) 0 else length).trim()


/*
 Words
 */

fun toText(words: Collection<String>, lineLength: Int): String =
    if (words.isEmpty() || lineLength < 1 || words.none { it.length <= lineLength }) ""
    else words.filter { it.length <= lineLength }.reduce { acc, word ->
        if ("$acc $word".lines().last().length <= lineLength)
            "$acc $word"
        else
            "$acc\n$word"
}.trim()

fun Collection<String>.takeRepeat(n: Int): List<String> {
    if (isEmpty()) return emptyList()
    var idx = 0
    return (0 until n).map {
        if (idx == this.size) idx = 0
        elementAt(idx++)
    }.toList()
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

fun punctuationMarksLine(wwString: String, length: Int, tryToPair: Boolean = false): String {
    val (left, right) = splitWWLeftRight(wwString)
    if ((left+right).isEmpty()) return ""
    return buildString {
        while (this.length < length) {
            val (l, r) = randomPair(left, right)
            append(if (tryToPair) "$l$r" else {
                val set = (l+r).toSet()
                if (set.isNotEmpty()) "${set.random()}" else ""
            })
        }
    }
}

fun wordsWithPunctuationMarks(words: Collection<String>, wwString: String, tryToPair: Boolean = false): List<String> {
    val (left, right) = splitWWLeftRight(wwString)
    return words.map {
        val (l, r) = randomPair(left, right)
        if (tryToPair) "$l$it$r" else {
            if ((l+r).isEmpty()) it
            else if (Random().nextInt(100) > 50)
                    "$it${(l+r).random()}"
                 else
                    "${(l+r).toSet().random()}$it"
        }
    }
}