package org.example

import java.io.File

fun createCourse(
    lessonSpecifications: Collection<String>,
    dictionary: Collection<String>,
    lineLength: Int,
    symbolsPerLesson: Int
): Course {

    if (lessonSpecifications.isEmpty() || lineLength <= 0 || symbolsPerLesson <= 0)
        return Course()

    val lessons = mutableListOf<Lesson?>()
    val lessonBuilder = LessonBuilder(lineLength, symbolsPerLesson, dictionary)

    lessonSpecifications.forEach { symbols ->

        lessons.add(
            lessonBuilder.newLesson(symbols, symbols) {
                alternateSymbols(3)
                repeatSymbols(2)
                shuffleSymbols(4)
                repeatSymbols(3)
                alternateSymbols(3)
            }
        )

        lessons.add(
            lessonBuilder.newLesson("$symbols + text", symbols) {
                words()
                repeatSymbols(2)
                words()
                words()
                shuffleSymbols(2)
                words()
            }
        )

        lessons.add(
            lessonBuilder.newLesson("Text $symbols", symbols) {
                words()
            }
        )
    }

    return Course(lessons = lessons.filterNotNull())
}


internal fun writeCourse(course: Course, to: List<String>) {
    to.forEach {
        if (it == "stdout") println(course.toXml())
        else writeCourseFile(it, course)
    }
}

internal fun writeCourseFile(path: String, course: Course) = try {
    File(path).writeText(course.toXml())
} catch (e: Exception) {
    System.err.println("${e.message} ($path)")
}