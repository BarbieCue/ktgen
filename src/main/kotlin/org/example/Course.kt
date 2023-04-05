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

fun writeCourseFile(path: String, course: Course) = try {
    File(path).writeText(course.toXml())
    true
} catch (e: Exception) {
    println(e.message)
    false
}

fun createCourse(
    courseSymbols: List<String>,
    dictionary: Collection<String>,
    lineLength: Int,
    wordsPerLesson: Int
): Course {

    val lessons = mutableListOf<Lesson>()
    val lessonCtr = generateSequence(1) { it + 1 }.iterator()
    val charsHistory = StringBuilder()

    courseSymbols.forEach { lessonSymbols ->

        val letters = letters(lessonSymbols)
        if (letters.isNotEmpty()) {
            lessons.add(
                buildLesson(
                    "${lessonCtr.next()}: $letters",
                    lineLength,
                    charsHistory.newCharacters(letters)) {
                    repeatedSymbolsLine(letters, 2)
                    repeatedSymbolsLine(letters, 3)
                    shuffledSymbolsLine(letters, 4)
                    repeatedSymbolsLine(letters, 3)
                    repeatedSymbolsLine(letters, 2)
                }
            )
            val words = dictionary.lessonWords(charsHistory.toString(), lessonSymbols)
            if (words.isNotEmpty()) {
                lessons.add(
                    buildLesson(
                        "${lessonCtr.next()}: Text ($letters)",
                        lineLength,
                        charsHistory.newCharacters(letters)) {
                        wordsMultiline(words, wordsPerLesson)
                    }
                )
            }
        }

        val digits = digits(lessonSymbols)
        if (digits.isNotEmpty()) {
            lessons.add(
                buildLesson(
                    "${lessonCtr.next()}: $digits",
                    lineLength,
                    charsHistory.newCharacters(digits)) {
                    repeatedSymbolsLine(digits, 1)
                    repeatedSymbolsLine(digits, 3)
                    shuffledSymbolsLine(digits, 5)
                    repeatedSymbolsLine(digits, 3)
                    repeatedSymbolsLine(digits, 1)
                }
            )
        }

        val leftRightPunctuationMarks = ww(lessonSymbols)
        if (leftRightPunctuationMarks.isNotEmpty()) {
            lessons.add(
                buildLesson(
                    "${lessonCtr.next()}: ${wwSymbols(lessonSymbols)}",
                    lineLength,
                    charsHistory.newCharacters(wwSymbols(lessonSymbols))) {
                    randomLeftRightPunctuationMarks(leftRightPunctuationMarks, 2)
                    randomLeftRightPunctuationMarks(leftRightPunctuationMarks, 3)
                    randomLeftRightPunctuationMarks(leftRightPunctuationMarks, 4)
                    randomLeftRightPunctuationMarks(leftRightPunctuationMarks, 3)
                    randomLeftRightPunctuationMarks(leftRightPunctuationMarks, 2)
                }
            )

            val words = dictionary.lessonWords(charsHistory.toString(), lessonSymbols)
            if (words.isNotEmpty())
                lessons.add(
                    buildLesson(
                        "${lessonCtr.next()}: Text ${wwSymbols(lessonSymbols)}",
                        lineLength,
                        charsHistory.newCharacters(wwSymbols(lessonSymbols))) {
                        wordsWithLeftRightPunctuationMarksMultiline(words, leftRightPunctuationMarks, wordsPerLesson)
                    }
                )
        }

        val unconditionalPunctuationMarks = unconditionalPunctuation(lessonSymbols)
        if (unconditionalPunctuationMarks.isNotEmpty()) {
            lessons.add(
                buildLesson(
                    "${lessonCtr.next()}: $unconditionalPunctuationMarks",
                    lineLength,
                    charsHistory.newCharacters(unconditionalPunctuationMarks)) {
                    randomUnconditionalPunctuationMarks(unconditionalPunctuationMarks, 2)
                    randomUnconditionalPunctuationMarks(unconditionalPunctuationMarks, 3)
                    randomUnconditionalPunctuationMarks(unconditionalPunctuationMarks, 4)
                    randomUnconditionalPunctuationMarks(unconditionalPunctuationMarks, 3)
                    randomUnconditionalPunctuationMarks(unconditionalPunctuationMarks, 2)
                }
            )

            val words = dictionary.lessonWords(charsHistory.toString(), lessonSymbols)
            if (words.isNotEmpty()) {
                lessons.add(
                    buildLesson(
                        "${lessonCtr.next()}: Text $unconditionalPunctuationMarks",
                        lineLength,
                        charsHistory.newCharacters(unconditionalPunctuationMarks)) {
                        wordsWithUnconditionalPunctuationMarksMultiline(words, unconditionalPunctuationMarks, wordsPerLesson)
                    }
                )
            }
        }
    }

    return Course(lessons = lessons)
}
