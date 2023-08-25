package org.example

import org.apache.commons.text.similarity.LevenshteinDistance

typealias LessonFilter = (Lesson?, Lesson) -> Boolean

class Filter {
    companion object {
        fun relativeLevenshteinDistanceFromLessonBefore(minimumDistance: Double): LessonFilter {
            return { previousLesson: Lesson?, lesson: Lesson ->
                if (minimumDistance !in (0.0 .. 1.0)) true // distance: 0 = equal; 1 = completely different
                else lesson.text.relativeLevenshteinDistance(previousLesson?.text) > minimumDistance
            }
        }
        fun lessonContainsAtLeastDifferentWords(n: Int): LessonFilter {
            return { _: Lesson?, lesson: Lesson ->
                lesson.text.differentWords(n)
            }
        }
    }
}

internal fun String.relativeLevenshteinDistance(other: String?): Double {
    if (isEmpty()) return 0.0
    if (other.isNullOrEmpty()) return 1.0
    val levenshteinDistance = LevenshteinDistance().apply(this, other)
    return if (levenshteinDistance == 0) 0.0
           else levenshteinDistance.toDouble() / length.toDouble()
}

internal fun String.differentWords(n: Int): Boolean {
    if (isEmpty()) return false
    val words = split("\\s".toRegex())
    if (words.size <= n) return true
    val differentWords = words.distinct()
    return differentWords.size > n
}