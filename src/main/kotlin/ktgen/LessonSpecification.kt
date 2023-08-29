package ktgen

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.io.File

internal suspend fun readLessonSpecification(input: String): Collection<String> {
    return if (input.isFile() || input.isHttpUri()) {
        val text = loadTextFromFileOrWeb(input)
        if (input.endsWith(".xml")) {
            val keyboardLayout = KeyboardLayout.create(text, false)
            keyboardLayout?.toLessonSpecification() ?: emptyList()
        } else if (input.endsWith(".ktgen")) {
            parseLessonSpecificationText(text)
        } else emptyList()
    } else {
        parseLessonSpecificationText(input)
    }
}

internal suspend fun loadTextFromFileOrWeb(input: String): String {
    val file = File(input)
    if (file.exists()) return file.readText().trim()
    val text = try {
        val response = HttpClient(CIO).get(input)
        if (response.status == HttpStatusCode.OK) {
            response.bodyAsText().trim()
        } else {
            System.err.println("URL seems to be invalid ($input). Status Code: ${response.status}")
            ""
        }
    } catch (e: Exception) {
        System.err.println("Error while requesting '$input'. Please provide a link to a valid .ktgen (lesson specification) or .xml (keyboard) file. The URL must end with .ktgen or .xml.")
        ""
    }
    return text
}

internal fun String.isHttpUri(): Boolean = try {
    if (trim().isEmpty()) false
    else {
        Url(this).toURI()
        startsWith("http")
    }
} catch (e: Exception) {
    false
}

internal fun String.isFile(): Boolean = File(this).exists()

internal fun parseLessonSpecificationText(text: String): Collection<String> {
    val list = text.trim().split("\\s+".toRegex())
    return if (list.all { it.isEmpty() }) emptyList() else list
}

internal fun KeyboardLayout.toLessonSpecification(): Collection<String> {
    val hands = hands(this)
    val keyPairs = pairKeys(hands)
    return keyPairs.customOrder().mapChars()
}