package org.example

import java.io.File

suspend fun createCourse(
    lessonSpecifications: List<String>,
    dictionary: Sequence<String>,
    lineLength: Int,
    symbolsPerLesson: Int,
    vararg lessonFilter: LessonFilter
): Course {

    if (lessonSpecifications.isEmpty() || lineLength <= 0 || symbolsPerLesson <= 0)
        return Course()

    val lessonBuilder = LessonBuilder(lineLength, symbolsPerLesson, lessonSpecifications, dictionary)
        .withLessonFilter(*lessonFilter)
        .apply {

            forEachLessonSpecification {

                lesson {
                    alternateSymbols(3)
                    repeatSymbols(2)
                    shuffleSymbols(4)
                    alternateSymbols(1)
                    repeatSymbols(3)
                    shuffleSymbols(4)
                    alternateSymbols(3)
                }

                lesson {
                    words()
                    alternateSymbols(4)
                    words()
                    words()
                    words()
                    shuffleSymbols(2)
                    words()
                }

                lesson {
                    words()
                }
            }

            every(3) {

                summaryLesson {
                    repeatSymbols(4)
                    words()
                    words()
                    shuffleSymbols(2)
                    words()
                    words()
                    shuffleSymbols(4)
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