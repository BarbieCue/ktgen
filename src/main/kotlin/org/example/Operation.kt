package org.example

import java.util.*
import kotlin.text.repeat as charSeqRepeat


fun String.shuffle(): String {
    val carr = toCharArray()
    carr.shuffle()
    return carr.concatToString().trim()
}

fun String.repeat(length: Int): String =
    if (length <= 0 || isEmpty()) ""
    else charSeqRepeat(length/this.length+1).slice((0 until length)).trim()

fun String.segment(length: Int): String {
    if (length < 1) return ""
    return buildString {
        for (from in 0..this@segment.length step length) {
            val to = if (from + length >= this@segment.length) this@segment.length - 1 else from + length - 1
            append(this@segment.slice((from..to)))
            append(' ')
        }
    }.trim()
}


fun Collection<String>.joinRepeat(sumNonWhitespaceChars: Int): String =
    if (isEmpty() || sumNonWhitespaceChars <= 0
        || all { it.isEmpty() }
        || none { it.length <= sumNonWhitespaceChars }) ""
    else {
        val wordLoop = filter { it.length <= sumNonWhitespaceChars }.repeatInfinite().iterator()
        buildString {
            var ctr = 0
            while (true) {
                val word = wordLoop.next().trim()
                ctr += word.length
                if (ctr > sumNonWhitespaceChars) {
                    val tooMuch = ctr - sumNonWhitespaceChars
                    val fillWord = this@joinRepeat.find { it.length == word.length - tooMuch }
                    if (fillWord != null) append(fillWord)
                    else append(word.take(word.length - tooMuch))
                    break
                }
                append(word)
                append(" ")
            }
        }.trim()
    }

fun <T> Collection<T>.repeatInfinite() = sequence {
    if (any()) while (true) yieldAll(this@repeatInfinite)
}

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

internal fun String.splitWWLeftRight(): Pair<String, String> =
    substringBefore("WW") to substringAfter("WW")

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

fun Collection<String>.prefixOrAppendPunctuationMarks(punctuationMarks: String): Collection<String> {
    if (isEmpty() || punctuationMarks.isEmpty()) return emptyList()
    val (left, right) = punctuationMarks.splitWWLeftRight()
    return map {
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