package org.example

import java.io.File

internal fun readLessonSpecification(path: String): Collection<String> = try {
    val text = File(path).readText().trim()
    parseLessonSpecification(text)
} catch (e: Exception) {
    System.err.println("${e.message} ($path)")
    emptyList()
}

internal fun parseLessonSpecification(text: String): Collection<String> {
    val list = text.split("\\s+".toRegex())
    return if (list.size == 1 && list.single().isEmpty()) emptyList() else list
}

internal fun KeyboardLayout.toLessonSpecification(): Collection<String> {
    val hands = hands(this)
    val keyPairs = pairKeys(hands)
    val ordered = customOrder(keyPairs)
    return lowerLetters(ordered).plus(upperLetters(ordered))
}