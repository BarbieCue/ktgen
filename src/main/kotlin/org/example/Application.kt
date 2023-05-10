package org.example

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default


fun main(args: Array<String>) {

    val parser = ArgParser("ktgen", strictSubcommandOptionsOrder = false)

    // IO
    val inputFile by parser.option(ArgType.String, "input-file", "if", "Path to a course definition file.").default("")
    val stdin by parser.option(ArgType.String, "stdin", "i", "The course definition as single string.").default("")
    val keyboardFile by parser.option(ArgType.String, "keyboard-layout", "k", "Path to a keyboard layout xml file (KTouch export). Generates a course especially for that keyboard layout.").default("")
    val input = mutableListOf<String>()

    val outputFile by parser.option(ArgType.String, "output-file", "of", "Write the course to this file (create or overwrite)").default("")
    val stdout by parser.option(ArgType.Boolean, "stdout", "o", "Write course to stdout").default(false)
    val output = mutableListOf<String>()

    // Dictionary
    val textFile by parser.option(ArgType.String, "text-file", "file", "Path to a dictionary input file. Can be an arbitrary text file containing continuous text (whitespace and/or newline separated words).").default("")
    val website by parser.option(ArgType.String, "website", "web", "Url of a website from which text will be scraped as dictionary input. Use a page of your choice that contains text.").default("")
    val dictionarySize by parser.option(ArgType.Int, "dictionary-size", "size", "Set the size of the dictionary in words.").default(7000)

    // Lesson settings
    val symbolsPerLesson by parser.option(ArgType.Int, "lesson-length", "length", "Limits the number of symbols per lesson.").default(300)
    val lineLength by parser.option(ArgType.Int, "line-length", "line", "Max length of each line.").default(50)
    val minWordLength by parser.option(ArgType.Int, "min-word-length", "min", "Take only words having this minimal length.").default(2)
    val maxWordLength by parser.option(ArgType.Int, "max-word-length", "max", "Take only words having this maximal length.").default(100)

    parser.parse(args)

    if (inputFile.isNotEmpty()) input.addAll(readCourseSymbols(inputFile))
    if (stdin.isNotEmpty()) input.addAll(parseCourseSymbols(stdin))
    if (keyboardFile.isNotEmpty()) KeyboardLayout.create(keyboardFile)?.toCourseSymbols()?.forEach { input.add(it) }

    if (outputFile.isNotEmpty()) output.add(outputFile)
    if (stdout) output.add("stdout")

    if (symbolsPerLesson <= 0) {
        System.err.println("The lesson length must be at least 1.")
        return
    }

    if (lineLength <= 0) {
        System.err.println("The average line length must be at least 1.")
        return
    }

    if (minWordLength > maxWordLength)
        System.err.println("Attention: The minimum word length is greater than maximum word length. No dictionary is used.")

    if (dictionarySize <= 0)
        System.err.println("Attention: The dictionary size must be at least 1. No dictionary is used.")

    val dictionary = buildDictionary(textFile, website, minWordLength, maxWordLength, dictionarySize)

    if (input.isEmpty()) {
        System.err.println("Missing (or empty) course definition. No course is created.")
        return
    }

    val course = createCourse(input, dictionary, lineLength, symbolsPerLesson)
    writeCourse(course, output)
}