package ktgen

import kotlin.math.min

class LevelComparator : Comparator<Key> {
    override fun compare(k1: Key, k2: Key): Int = k1.top - k2.top
}

class LeftToRight : Comparator<Key> {
    override fun compare(k1: Key, k2: Key): Int = k1.left - k2.left
}

class RightToLeft : Comparator<Key> {
    override fun compare(k1: Key, k2: Key): Int = k2.left - k1.left
}

fun hands(keyboardLayout: KeyboardLayout): Pair<Hand, Hand>? {
    return if (keyboardLayout.keys.keys.map { it.fingerIndex }.toSet().size != 8) { // expect 8 fingers
        System.err.println("Error on creating key-finger map from keyboard. Please provide a valid 8-finger keyboard KTouch export.")
        null
    }
    else {
        val byFinger = keyboardLayout.keys.keys
            .groupBy { it.fingerIndex }
            .toSortedMap()
            .mapValues { it.value.sortedWith(LevelComparator()) }.values
        val byLevel = byFinger.map { entry -> entry.groupBy { it.top }.values }
        val sorted = byLevel.mapIndexed { fingerIndex, level ->
            level.map { keys ->
                when (fingerIndex) {
                    0, 1, 2, 4 -> keys.sortedWith(RightToLeft())
                    else -> keys.sortedWith(LeftToRight())
                }
            }
        }
        val leftHand = sorted.take(4)
        val rightHand = sorted.takeLast(4).reversed()
        leftHand to rightHand
    }
}

/*
  Hand structure:

  Outer list: 8 fingers (0 .. 7; read from left to right)
    List: n levels (keyboard rows) per finger (0 .. n; usually four; read from top down)
      Inner list: n keys per level (0 .. n; usually one, two or max three; read from left to right or right to left)

  Hand[finger][level][key]

  Example with layout english (USA):
  Access ASDF-keys on right hand or ;LKJ-keys on left hand
    Hand[0][2][0]
    Hand[1][2][0]
    Hand[2][2][0]
    Hand[3][2][0]

  See unit tests for more examples
*/
typealias Hand = List<List<List<Key>>>

data class KeyPair(val pair: Pair<Key?, Key?>, val finger: Int, val level: Int, val index: Int)

fun pairKeys(hands: Pair<Hand, Hand>?): List<KeyPair> {
    if (hands == null) return emptyList()
    val (left, right) = hands
    val result = mutableListOf<KeyPair>()
    val noOpponent = mutableListOf<Key>()

    for (finger in (0 .. 3)) {
        for (level in (0 .. min(left[finger].size - 1, right[finger].size - 1))) {
            val lKeys = left[finger][level]
            val rKeys = right[finger][level]
            if (lKeys.size == rKeys.size) {
                lKeys.forEachIndexed { index, key ->
                    result.add(KeyPair(key to rKeys[index], finger, level, index))
                }
            } else if (lKeys.size > rKeys.size) {
                lKeys.forEachIndexed { index, lkey ->
                    if (rKeys.size > index)
                        result.add(KeyPair(lkey to rKeys[index], finger, level, index))
                    else noOpponent.add(lkey)
                }
            } else { // rKeys.size > lKeys.size
                rKeys.forEachIndexed { index, rkey ->
                    if (lKeys.size > index)
                        result.add(KeyPair(lKeys[index] to rkey, finger, level, index))
                    else noOpponent.add(rkey)
                }
            }
        }
    }

    for (i in (0 until noOpponent.size) step 2) {
        result.add(KeyPair(noOpponent.elementAtOrNull(i) to noOpponent.elementAtOrNull(i + 1), -1, -1, -1))
    }

    return result
}

fun List<KeyPair>.customOrder(): List<KeyPair> {
    val path = setOf( // for example when using a QWERTY layout like 'english (USA)':
        filter { it.finger == 3 && it.level == 2 && it.index == 0 }, // fj
        filter { it.finger == 2 && it.level == 2 && it.index == 0 }, // dk
        filter { it.finger == 1 && it.level == 2 && it.index == 0 }, // sl
        filter { it.finger == 0 && it.level == 2 && it.index == 0 }, // a;
        filter { it.finger == 3 && it.level == 2 && it.index == 1 }, // gh
        filter { it.finger == 3 && it.level == 1 && it.index == 1 }, // ty
        filter { it.finger == 3 && it.level == 3 && it.index == 0 }, // vm
        filter { it.finger == 3 && it.level == 3 && it.index == 1 }, // bn
        filter { it.finger == 3 && it.level == 1 && it.index == 0 }, // ru
        filter { it.finger == 2 && it.level == 1 && it.index == 0 }, // ei
        filter { it.finger == 2 && it.level == 3 && it.index == 0 }, // c,
        filter { it.finger == 1 && it.level == 1 && it.index == 0 }, // wo
        filter { it.finger == 1 && it.level == 3 && it.index == 0 }, // x.
        filter { it.finger == 0 && it.level == 1 && it.index == 0 }, // qp
        filter { it.finger == 0 && it.level == 3 && it.index == 0 }  // z/
    ).flatten()
    return path.plus(minus(path.toSet()))
}

enum class CharPosition(val value: String) {
    DEFAULT("hidden"),
    BOTTOM_LEFT("bottomLeft"),
    TOP_LEFT("topLeft"),
    BOTTOM_RIGHT("bottomRight"),
    TOP_RIGHT("topRight"),
}

internal fun Key.charAtPosition(position: CharPosition) =
    chars.firstOrNull { it.position == position.value }

internal fun Pair<Key?, Key?>.mapCharsByPosition(leftPosition: CharPosition, rightPositions: List<CharPosition>): Pair<Char?, Char?>? {
    if (!rightPositions.contains(leftPosition)) return null
    val leftChar = first?.charAtPosition(leftPosition)
    for (right in rightPositions.subList(rightPositions.indexOf(leftPosition), rightPositions.size)) {
        val rightChar = second?.charAtPosition(right) ?: continue
        return leftChar to rightChar
    }
    return leftChar to null
}

internal fun List<String>.withoutDuplicateSingles() =
    filter { single ->
        if (single.length == 1) filter { it.length == 2 }.none { it.contains(single) }
        else true
    }

internal fun List<String>.onlyAllowedSymbols(): List<String> =
    map { it.map { char ->
        if ("$char".matches(lettersRegex) ||
            "$char".matches(digitRegex) ||
            "$char".matches(punctuationRegex)) char
        else ""}.joinToString("")
    }.filter { it.isNotEmpty() }

fun List<KeyPair>.mapChars(): List<String> {
    val positions = listOf(
        CharPosition.DEFAULT,
        CharPosition.BOTTOM_LEFT,
        CharPosition.TOP_LEFT,
        CharPosition.BOTTOM_RIGHT,
        CharPosition.TOP_RIGHT,
    )
    val result = mutableListOf<String>()
    positions.forEach { position ->
        forEach {
            val pair = it.pair.mapCharsByPosition(position, positions)
            val leftCharSymbol = pair?.first?.text ?: ""
            val rightCharSymbol = pair?.second?.text ?: ""
            val concat = leftCharSymbol + rightCharSymbol
            if (concat.isNotEmpty() && !result.contains(concat)) result.add(concat)
        }
    }
    return result.onlyAllowedSymbols().withoutDuplicateSingles()
}