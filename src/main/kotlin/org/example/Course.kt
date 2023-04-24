package org.example

import java.io.File

fun readCourseSymbols(path: String): List<String> = try {
    val text = File(path).readText().trim()
    val list = text.split("\\s+".toRegex())
    if (list.size == 1 && list.single().isEmpty()) emptyList() else list
} catch (e: Exception) {
    System.err.println(e.message)
    emptyList()
}

fun writeCourseFile(path: String, course: Course) = try {
    File(path).writeText(course.toXml())
    true
} catch (e: Exception) {
    System.err.println(e.message)
    false
}

fun createCourse(
    courseSymbols: List<String>,
    dictionary: Collection<String>,
    lineLength: Int,
    symbolsPerLesson: Int
): Course {

    if (courseSymbols.isEmpty() || lineLength <= 0 || symbolsPerLesson <= 0)
        return Course(lessons = emptyList())

    val lessons = mutableListOf<Lesson>()
    val lessonCtr = generateSequence(1) { it + 1 }.iterator()
    val charsHistory = StringBuilder()

    courseSymbols.forEach { lessonSymbols ->

        val newCharacters = charsHistory.newCharacters(lessonSymbols)
        val words = dictionary.lessonWords(charsHistory.toString(), lessonSymbols)

        // Letter permutations
        val letters = letters(lessonSymbols)
        if (letters.isNotEmpty()) {
            lessons.add(
                buildLesson(
                    "${lessonCtr.next()}: $letters",
                    lineLength, symbolsPerLesson, newCharacters) {
                    alternatingSymbols(letters, 3)
                    repeatSymbols(letters, 2)
                    repeatSymbols(letters, 3)
                    shuffledSymbols(letters, 4)
                    repeatSymbols(letters.reversed(), 3)
                    repeatSymbols(letters.reversed(), 2)
                }
            )

            // Letters and words mixed
            if (words.isNotEmpty()) {
                lessons.add(
                    buildLesson(
                        "${lessonCtr.next()}: $letters and text",
                        lineLength, symbolsPerLesson, newCharacters) {
                        repeatSymbols(letters, 4)
                        words(words)
                        words(words)
                        shuffledSymbols(letters, 4)
                    }
                )
            }

            // Words
            if (words.isNotEmpty()) {
                lessons.add(
                    buildLesson(
                        "${lessonCtr.next()}: Text ($letters)",
                        lineLength, symbolsPerLesson, newCharacters) {
                        words(words)
                    }
                )
            }
        }

        // Letter group
        val letterGroup = letterGroup(lessonSymbols)
        if (letterGroup.isNotEmpty()) {
            val groupLetters = letterGroupUnpack(lessonSymbols)
            lessons.add(
                buildLesson(
                    "${lessonCtr.next()}: $groupLetters",
                    lineLength, symbolsPerLesson, newCharacters) {
                    repeatSymbols(groupLetters, groupLetters.length)
                }
            )

            // Letter group and words mixed
            if (words.isNotEmpty()) {
                lessons.add(
                    buildLesson(
                        "${lessonCtr.next()}: $groupLetters and text",
                        lineLength, symbolsPerLesson, newCharacters) {
                        repeatSymbols(groupLetters, groupLetters.length)
                        words(words)
                        words(words)
                        repeatSymbols(groupLetters, groupLetters.length)
                    }
                )
            }

            // Words
            if (words.isNotEmpty()) {
                lessons.add(
                    buildLesson(
                        "${lessonCtr.next()}: Text $groupLetters",
                        lineLength, symbolsPerLesson, newCharacters) {
                        words(words)
                    }
                )
            }
        }

        // Digit permutations
        val digits = digits(lessonSymbols)
        if (digits.isNotEmpty()) {
            lessons.add(
                buildLesson(
                    "${lessonCtr.next()}: $digits",
                    lineLength, symbolsPerLesson,
                    charsHistory.newCharacters(digits)) {
                    repeatSymbols(digits, 1)
                    repeatSymbols(digits, 3)
                    shuffledSymbols(digits, 5)
                    repeatSymbols(digits.reversed(), 3)
                    repeatSymbols(digits.reversed(), 1)
                }
            )
        }

        // Punctuation marks (left right) permutations
        val leftRightPunctuationMarks = ww(lessonSymbols)
        if (leftRightPunctuationMarks.isNotEmpty()) {
            lessons.add(
                buildLesson(
                    "${lessonCtr.next()}: ${wwUnpack(lessonSymbols)}",
                    lineLength, symbolsPerLesson,
                    charsHistory.newCharacters(wwUnpack(lessonSymbols))) {
                    randomLeftRightPunctuationMarks(leftRightPunctuationMarks, 2)
                    randomLeftRightPunctuationMarks(leftRightPunctuationMarks, 3)
                    randomLeftRightPunctuationMarks(leftRightPunctuationMarks, 4)
                    randomLeftRightPunctuationMarks(leftRightPunctuationMarks, 3)
                    randomLeftRightPunctuationMarks(leftRightPunctuationMarks, 2)
                }
            )

            // Punctuation marks words (left right)
            if (words.isNotEmpty())
                lessons.add(
                    buildLesson(
                        "${lessonCtr.next()}: Text ${wwUnpack(lessonSymbols)}",
                        lineLength, symbolsPerLesson,
                        charsHistory.newCharacters(wwUnpack(lessonSymbols))) {
                        wordsWithLeftRightPunctuationMarks(words, leftRightPunctuationMarks)
                    }
                )
        }

        // Punctuation marks permutations
        val unconditionalPunctuationMarks = unconditionalPunctuation(lessonSymbols)
        if (unconditionalPunctuationMarks.isNotEmpty()) {
            lessons.add(
                buildLesson(
                    "${lessonCtr.next()}: $unconditionalPunctuationMarks",
                    lineLength, symbolsPerLesson,
                    charsHistory.newCharacters(unconditionalPunctuationMarks)) {
                    repeatSymbols(unconditionalPunctuationMarks, 2)
                    shuffledSymbols(unconditionalPunctuationMarks, 3)
                    repeatSymbols(unconditionalPunctuationMarks, 4)
                    shuffledSymbols(unconditionalPunctuationMarks, 3)
                    repeatSymbols(unconditionalPunctuationMarks, 2)
                }
            )

            // Punctuation marks words
            if (words.isNotEmpty()) {
                lessons.add(
                    buildLesson(
                        "${lessonCtr.next()}: Text $unconditionalPunctuationMarks",
                        lineLength, symbolsPerLesson,
                        charsHistory.newCharacters(unconditionalPunctuationMarks)) {
                        wordsWithUnconditionalPunctuationMarks(words, unconditionalPunctuationMarks)
                    }
                )
            }
        }
    }

    return Course(lessons = lessons)
}
