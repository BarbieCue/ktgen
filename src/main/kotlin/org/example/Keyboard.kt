package org.example

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
        val byFinger = keyboardLayout.keys.keys.groupBy { it.fingerIndex }.mapValues { it.value.sortedWith(LevelComparator()) }.values
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

fun customOrder(keyPairs: List<KeyPair>): List<KeyPair> {
    val path = setOf( // for example when using a QWERTY layout like 'english (USA)':
        keyPairs.filter { it.finger == 3 && it.level == 2 && it.index == 0 }, // fj
        keyPairs.filter { it.finger == 2 && it.level == 2 && it.index == 0 }, // dk
        keyPairs.filter { it.finger == 1 && it.level == 2 && it.index == 0 }, // sl
        keyPairs.filter { it.finger == 0 && it.level == 2 && it.index == 0 }, // a;
        keyPairs.filter { it.finger == 3 && it.level == 2 && it.index == 1 }, // gh
        keyPairs.filter { it.finger == 3 && it.level == 1 && it.index == 1 }, // ty
        keyPairs.filter { it.finger == 3 && it.level == 3 && it.index == 0 }, // vm
        keyPairs.filter { it.finger == 3 && it.level == 3 && it.index == 1 }, // bn
        keyPairs.filter { it.finger == 3 && it.level == 1 && it.index == 0 }, // ru
        keyPairs.filter { it.finger == 2 && it.level == 1 && it.index == 0 }, // ei
        keyPairs.filter { it.finger == 2 && it.level == 3 && it.index == 0 }, // c,
        keyPairs.filter { it.finger == 1 && it.level == 1 && it.index == 0 }, // wo
        keyPairs.filter { it.finger == 1 && it.level == 3 && it.index == 0 }, // x.
        keyPairs.filter { it.finger == 0 && it.level == 1 && it.index == 0 }, // qp
        keyPairs.filter { it.finger == 0 && it.level == 3 && it.index == 0 }  // z/
    ).flatten()
    return path.plus(keyPairs.minus(path.toSet()))
}

fun filter(keyPairs: List<KeyPair>, pattern: Regex): List<String> {
    return keyPairs.map { pair ->
        val l = pair.pair.first?.chars?.firstOrNull { it.text.matches(pattern) }?.text
        val r = pair.pair.second?.chars?.firstOrNull { it.text.matches(pattern) }?.text
        (l ?: "") + (r ?: "")
    }.filter { it.isNotEmpty() }
}

fun upperLetters(keyPairs: List<KeyPair>): List<String> = filter(keyPairs, "[A-ZÜÄÖẞ]+".toRegex())
fun lowerLetters(keyPairs: List<KeyPair>): List<String> = filter(keyPairs, "[a-züäöß]+".toRegex())

fun KeyboardLayout.toCourseSymbols(): List<String> {
    val hands = hands(this)
    val keyPairs = pairKeys(hands)
    val ordered = customOrder(keyPairs)
    return lowerLetters(ordered).plus(upperLetters(ordered))
}