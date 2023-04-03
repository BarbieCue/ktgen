package org.example

import java.io.File

fun readCourseSymbols(path: String): List<String> = try {
    val text = File(path).readText().trim()
    val list = text.split('\n')
    if (list.size == 1 && list.single().isEmpty()) emptyList() else list
} catch (e: Exception) {
    println(e.message)
    emptyList()
}

fun writeCourseFile(path: String, course: Course): Boolean = try {
    File(path).writeText(course.toXml())
    true
} catch (e: Exception) {
    println(e.message)
    false
}