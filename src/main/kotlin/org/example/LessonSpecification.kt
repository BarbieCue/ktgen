package org.example

import java.io.File

internal fun readLessonSpecificationFile(path: String): Collection<String> = try {
    val keyboardLayout = KeyboardLayout.create(path, false)
    if (keyboardLayout != null) keyboardLayout.toLessonSpecification()
    else {
        val text = File(path).readText().trim()
        val list = text.split("\\s+".toRegex())
        if (list.size == 1 && list.single().isEmpty()) emptyList() else list
    }
} catch (e: Exception) {
    System.err.println("${e.message} ($path)")
    emptyList()
}

internal fun KeyboardLayout.toLessonSpecification(): Collection<String> {
    val hands = hands(this)
    val keyPairs = pairKeys(hands)
    val ordered = customOrder(keyPairs)
    return lowerLetters(ordered).plus(upperLetters(ordered))
}