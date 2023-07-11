package org.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.io.File

internal suspend fun readLessonSpecification(input: String): Collection<String> = try {
    val text = loadText(input)
    if (text.isEmpty()) emptyList()
    else {
        if (input.endsWith(".xml")) {
            val keyboardLayout = KeyboardLayout.create(text, false)
            keyboardLayout?.toLessonSpecification() ?: emptyList()
        } else {
            val keyboardLayout = KeyboardLayout.create(text, false)
            keyboardLayout?.toLessonSpecification() ?: parseLessonSpecificationText(text)
        }
    }
} catch (e: Exception) {
    System.err.println("${e.message} ($input)")
    emptyList()
}

internal suspend fun loadText(input: String): String {
    val file = File(input)
    if (file.exists()) return file.readText().trim()
    else if (isValidUrl(input)) {
        val response = HttpClient(CIO).request(input)
        return if (response.status == HttpStatusCode.OK) {
            response.bodyAsText().trim()
        } else {
            System.err.println("Input URL seems to be invalid. Status Code: ${response.status}")
            ""
        }
    }
    return input.trim()
}

// todo test me
internal fun isValidUrl(url: String): Boolean = try {
    if (url.trim().isEmpty()) false
    else {
        Url(url).toURI()
        true
    }
} catch (e: Exception) {
    false
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