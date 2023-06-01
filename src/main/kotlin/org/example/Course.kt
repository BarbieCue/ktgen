package org.example

import java.io.File

fun createCourse(
    lessonSpecifications: List<String>,
    dictionary: Sequence<String>,
    lineLength: Int,
    symbolsPerLesson: Int
): Course {

    if (lessonSpecifications.isEmpty() || lineLength <= 0 || symbolsPerLesson <= 0)
        return Course()

    val lessonBuilder = LessonBuilder(lineLength, symbolsPerLesson, dictionary)
        .withLessonFilter(Filter.relativeLevenshteinDistanceFromLessonBefore(0.6))
        .withLessonFilter(Filter.containsAtLeastDifferentWords(10))


    lessonSpecifications.forEachIndexed { idx, symbols ->

        lessonBuilder.newLesson(symbols, symbols) {
            alternateSymbols(3)
            repeatSymbols(2)
            shuffleSymbols(4)
            alternateSymbols(1)
            repeatSymbols(3)
            shuffleSymbols(4)
            alternateSymbols(3)
        }

        lessonBuilder.newLesson("$symbols + text", symbols) {
            words()
            alternateSymbols(4)
            words()
            words()
            words()
            shuffleSymbols(2)
            words()
        }

        lessonBuilder.newLesson("Text $symbols", symbols) {
            words()
        }

        if (idx > 0 && idx % 5 == 0) {
            lessonBuilder.newLesson(
                "Check: ${lessonSpecifications.subList(idx - 5, idx + 1).joinToString(" ")}",
                lessonSpecifications.subList(idx - 5, idx + 1)) {
                words()
                alternateSymbols(2)
                words()
                words()
                words()
                shuffleSymbols(5)
                words()
            }
        }
    }

    return Course(lessons = lessonBuilder.lessons)
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