package org.example

import kotlinx.cli.*
import kotlinx.coroutines.runBlocking


fun main(args: Array<String>) = runBlocking {

    val parser = ArgParser("ktgen")

    // IO
    val lessonSpecification by parser.argument(ArgType.String, description = "Path(s) to lesson specification files like 'lesson_specification.ktgen'. Keyboard layout xml files (KTouch export) are also valid.").vararg().optional()
    val outputFile by parser.option(ArgType.String, "output-file", "of", "Write the course to this file (create or overwrite)").default("")
    val stdout by parser.option(ArgType.Boolean, "stdout", "o", "Write course to stdout").default(false)

    // Dictionary
    val textFile by parser.option(ArgType.String, "text-file", "file", "Path to a dictionary input file. Can be an arbitrary text file containing continuous text (whitespace and/or newline separated words).").default("")
    val website by parser.option(ArgType.String, "website", "web", "Url of a website from which text will be scraped as dictionary input. Use a page of your choice that contains text.").default("")
    val dictionarySize by parser.option(ArgType.Int, "dictionary-size", "size", "Set the size of the dictionary in words (can contain duplicates in order to preserve meaningful sentences).").default(4000)

    // Lesson settings
    val symbolsPerLesson by parser.option(ArgType.Int, "lesson-length", "length", "Limits the number of symbols per lesson.").default(300)
    val lineLength by parser.option(ArgType.Int, "line-length", "line", "Max length of each line.").default(50)
    val minWordLength by parser.option(ArgType.Int, "min-word-length", "min", "Take only words having this minimal length.").default(2)
    val maxWordLength by parser.option(ArgType.Int, "max-word-length", "max", "Take only words having this maximal length.").default(100)

    // Lesson filters
    val textDistance by parser.option(ArgType.Double, "text-distance", "td",
        "The text of a created lesson must have a minimum difference from the previous lessons' text. Otherwise drop the lesson from the result. " +
                "Value range [0.0, 1.0] where 0.0 means lessons can be equal (take all lessons). 1.0 means lessons must be completely different not to be dropped.").default(0.6)
    val wordDiversity by parser.option(ArgType.Int, "word-diversity", "wd", "Drop a lesson from the result when it consists of less than n different words / segments.").default(10)

    parser.parse(args)

    val input = mutableListOf<String>()
    if (lessonSpecification.isEmpty()) input.addAll(readLessonSpecificationFile("lesson_specification.ktgen"))
    else lessonSpecification.forEach { input.addAll(readLessonSpecificationFile(it)) }

    val output = mutableListOf<String>()
    if (outputFile.isEmpty() && !stdout) output.add("ktgen_course.xml")
    if (outputFile.isNotEmpty()) output.add(outputFile)
    if (stdout) output.add("stdout")

    if (symbolsPerLesson <= 0) {
        System.err.println("The lesson length must be at least 1.")
        return@runBlocking
    }

    if (lineLength <= 0) {
        System.err.println("The average line length must be at least 1.")
        return@runBlocking
    }

    if (textDistance !in (0.0 .. 1.0))
        System.err.println("Text distance must be in [0.0, 1.0]. Proceeding without text distance check.")

    if (wordDiversity <= 0)
        System.err.println("Minimum word diversity per lesson must be at least 1. Proceeding without word diversity check.")

    if (minWordLength > maxWordLength)
        System.err.println("The minimum word length is greater than maximum word length. No dictionary is used.")

    if (dictionarySize <= 0)
        System.err.println("The dictionary size must be at least 1. No dictionary is used.")

    val dictionary = buildDictionary(textFile, website, minWordLength, maxWordLength, dictionarySize)

    if (input.isEmpty()) {
        System.err.println("Missing (or empty) lesson specification. No course is created.")
        return@runBlocking
    }

    val course = createCourse(input, dictionary, lineLength, symbolsPerLesson,
        Filter.relativeLevenshteinDistanceFromLessonBefore(textDistance),
        Filter.lessonContainsAtLeastDifferentWords(wordDiversity)
    )
    writeCourse(course, output)
}