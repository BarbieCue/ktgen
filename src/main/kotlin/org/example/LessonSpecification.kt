package org.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.io.File
import java.net.MalformedURLException

internal suspend fun readLessonSpecification(input: String): Collection<String> = try {
    val text = loadText(input)
    if (text.isEmpty()) emptyList()
    else {
        val keyboardLayout = KeyboardLayout.create(text, false)
        keyboardLayout?.toLessonSpecification() ?: parseLessonSpecificationText(text)
    }
} catch (e: Exception) {
    System.err.println("${e.message} ($input)")
    emptyList()
}

// todo test me
internal suspend fun loadText(input: String): String {
    val file = File(input)
    if (file.exists()) return file.readText().trim()
    else if (isValidUrl(input)) {
        val response = HttpClient(CIO).request(input)
        if (response.status == HttpStatusCode.OK) {
            val text = response.bodyAsText()
            if (text.isNotEmpty()) return text
        } else {
            System.err.println("Input URL seems to be invalid. Status Code: ${response.status}")
            return ""
        }
    }
    return input
}

// todo test me
internal fun isValidUrl(url: String): Boolean = try {
    Url(url)
    true
} catch (e: MalformedURLException) {
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