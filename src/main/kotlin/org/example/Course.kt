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

    val lessons = mutableListOf<Lesson>()
    val lessonCtr = generateSequence(1) { it + 1 }.iterator()
    val charsHistory = StringBuilder()

    lessonSpecifications.forEach { lessonSymbols ->

        val newCharacters = charsHistory.newCharacters(lessonSymbols)
        val lessonBuilder = lessonBuilder(lineLength, symbolsPerLesson, newCharacters)
        val words = dictionary.lessonWords(charsHistory.toString(), lessonSymbols)

        // Letter permutations
        val letters = letters(lessonSymbols)
        if (letters.isNotEmpty()) {
            lessons.add(
                lessonBuilder("${lessonCtr.next()}: $letters") {
                    alternatingSymbols(letters, 3)
                    repeatSymbols(letters, 2)
                    shuffledSymbols(letters, 4)
                    repeatSymbols(letters.reversed(), 3)
                    alternatingSymbols(letters, 3)
                }
            )

            // Letters and words mixed
            if (words.isNotEmpty()) {
                lessons.add(
                    lessonBuilder("${lessonCtr.next()}: $letters + Text") {
                        words(words)
                        shuffledSymbols(letters, 2)
                        words(words)
                        words(words)
                        shuffledSymbols(letters, 2)
                        words(words)
                    }
                )
            }

            // Words
            if (words.isNotEmpty()) {
                lessons.add(
                    lessonBuilder("${lessonCtr.next()}: Text $letters") {
                        words(words)
                    }
                )
            }
        }

        // Letter group
        val letterGroup = letterGroup(lessonSymbols)
        if (letterGroup.isNotEmpty()) {
            val groupLetters = unpack(lessonSymbols)
            lessons.add(
                lessonBuilder("${lessonCtr.next()}: Group $groupLetters") {
                    repeatSymbols(groupLetters, groupLetters.length)
                    shuffledSymbols(groupLetters, groupLetters.length)
                    alternatingSymbols(groupLetters, 1)
                    repeatSymbols(groupLetters, groupLetters.length)
                }
            )

            // Letter group and words mixed
            if (words.isNotEmpty()) {
                lessons.add(
                    lessonBuilder("${lessonCtr.next()}: Group $groupLetters + Text") {
                        alternatingSymbols(groupLetters, 4)
                        words(words)
                        words(words)
                        repeatSymbols(groupLetters, groupLetters.length)
                    }
                )
            }

            // Words
            if (words.isNotEmpty()) {
                lessons.add(
                    lessonBuilder("${lessonCtr.next()}: Group Text $groupLetters") {
                        words(words)
                    }
                )
            }
        }

        // Digit permutations
        val digits = digits(lessonSymbols)
        if (digits.isNotEmpty()) {
            lessons.add(
                lessonBuilder("${lessonCtr.next()}: $digits") {
                    alternatingSymbols(digits, 3)
                    repeatSymbols(digits, 3)
                    shuffledSymbols(digits, 5)
                    repeatSymbols(digits.reversed(), 3)
                    alternatingSymbols(digits, 3)
                }
            )
        }

        // Punctuation marks (left right) permutations
        val leftRightPunctuationMarks = ww(lessonSymbols)
        if (leftRightPunctuationMarks.isNotEmpty()) {
            lessons.add(
                lessonBuilder("${lessonCtr.next()}: ${unpack(lessonSymbols)}") {
                    alternatingSymbols(unpack(leftRightPunctuationMarks), 3)
                    randomLeftRightPunctuationMarks(leftRightPunctuationMarks, 2)
                    randomLeftRightPunctuationMarks(leftRightPunctuationMarks, 4)
                    randomLeftRightPunctuationMarks(leftRightPunctuationMarks, 2)
                    alternatingSymbols(unpack(leftRightPunctuationMarks), 3)
                }
            )

            // Punctuation marks words (left right)
            if (words.isNotEmpty())
                lessons.add(
                    lessonBuilder("${lessonCtr.next()}: Text ${unpack(lessonSymbols)}") {
                        wordsWithLeftRightPunctuationMarks(words, leftRightPunctuationMarks)
                    }
                )
        }

        // Punctuation marks permutations
        val unconditionalPunctuationMarks = unconditionalPunctuation(lessonSymbols)
        if (unconditionalPunctuationMarks.isNotEmpty()) {
            lessons.add(
                lessonBuilder("${lessonCtr.next()}: $unconditionalPunctuationMarks") {
                    alternatingSymbols(unconditionalPunctuationMarks, 3)
                    repeatSymbols(unconditionalPunctuationMarks, 2)
                    shuffledSymbols(unconditionalPunctuationMarks, 3)
                    repeatSymbols(unconditionalPunctuationMarks, 4)
                    shuffledSymbols(unconditionalPunctuationMarks, 3)
                    repeatSymbols(unconditionalPunctuationMarks, 2)
                    alternatingSymbols(unconditionalPunctuationMarks, 3)
                }
            )

            // Punctuation marks words
            if (words.isNotEmpty()) {
                lessons.add(
                    lessonBuilder("${lessonCtr.next()}: Text $unconditionalPunctuationMarks") {
                        wordsWithUnconditionalPunctuationMarks(words, unconditionalPunctuationMarks)
                    }
                )
            }
        }
    }

    return Course(lessons = lessons)
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