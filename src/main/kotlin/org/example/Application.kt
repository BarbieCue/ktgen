package org.example

import kotlinx.cli.*
import java.io.File


internal fun readCourseSymbols(path: String): Collection<String> = try {
    val text = File(path).readText().trim()
    parseCourseSymbols(text)
} catch (e: Exception) {
    System.err.println("${e.message} ($path)")
    emptyList()
}

internal fun parseCourseSymbols(text: String): Collection<String> {
    val list = text.split("\\s+".toRegex())
    return if (list.size == 1 && list.single().isEmpty()) emptyList() else list
}

internal fun KeyboardLayout.toCourseSymbols(): List<String> {
    val hands = hands(this)
    val keyPairs = pairKeys(hands)
    val ordered = customOrder(keyPairs)
    return lowerLetters(ordered).plus(upperLetters(ordered))
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

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {

    val parser = ArgParser("ktgen", strictSubcommandOptionsOrder = true)

    // IO
    class Input: Subcommand("input", "The course definition. More information can be found in the readme file.") {

        val inputFile by option(ArgType.String, "file", "f", "Path to a course definition file.").default("")
        val stdin by option(ArgType.String, "stdin", "i", "The course definition as single string.").default("")
        val keyboardFile by option(ArgType.String, "keyboard-layout", "k", "Path to a keyboard layout xml file (KTouch export). Generates a course especially for that keyboard layout.").default("")
        val value = mutableListOf<String>()

        override fun execute() {
            value.addAll(readCourseSymbols(inputFile))
            value.addAll(parseCourseSymbols(stdin))
            if (keyboardFile.isNotEmpty()) KeyboardLayout.create(keyboardFile)?.toCourseSymbols()?.forEach { value.add(it) }
        }
    }
    val input = Input()
    parser.subcommands(input)

    TODO("register input and output correctly.")

    class Output: Subcommand("output", "Output to a file or to stdout.") {

        val outputFile by parser.option(ArgType.String, "file", "f", "Write the course to this file (create or overwrite)").default("")
        val stdout by parser.option(ArgType.Boolean, "stdout", "o", "Write course to stdout").default(false)
        val value = mutableListOf<String>()

        override fun execute() {
            if (outputFile.isNotEmpty()) value.add(outputFile)
            if (stdout) value.add("stdout")
        }
    }
    val output = Output()
    parser.subcommands(output)

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

    if (input.value.isEmpty()) {
        System.err.println("Missing (or empty) course definition. No course is created.")
        return
    }

    val course = createCourse(input.value, dictionary, lineLength, symbolsPerLesson)
    writeCourse(course, output.value)
}