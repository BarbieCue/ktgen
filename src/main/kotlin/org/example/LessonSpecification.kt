package org.example

import java.io.File

internal fun readLessonSpecification(input: String): Collection<String> = try {
    val file = File(input)
    if (file.exists()) {
        val keyboardLayout = KeyboardLayout.create(file, false)
        if (keyboardLayout != null) keyboardLayout.toLessonSpecification()
        else {
            val text = file.readText().trim()
            parseLessonSpecificationText(text)
        }
    }
    else parseLessonSpecificationText(input)
} catch (e: Exception) {
    System.err.println("${e.message} ($input)")
    emptyList()
}

internal fun parseLessonSpecificationText(text: String): Collection<String> {
    val list = text.split("\\s+".toRegex())
    return if (list.all { it.isEmpty() }) emptyList() else list
}

internal fun KeyboardLayout.toLessonSpecification(): Collection<String> {
    val hands = hands(this)
    val keyPairs = pairKeys(hands)
    return keyPairs.customOrder().mapChars()
}