package org.example

import kotlinx.cli.*
import java.io.File


internal fun readCourseSymbols(path: String): Collection<String> = try {
    val text = File(path).readText().trim()
    parseCourseSymbols(text)
} catch (e: Exception) {
    System.err.println("${e.message} $path")
    emptyList()
}

internal fun parseCourseSymbols(text: String): Collection<String> {
    val list = text.split("\\s+".toRegex())
    return if (list.size == 1 && list.single().isEmpty()) emptyList() else list
}

fun KeyboardLayout.toCourseSymbols(): List<String> {
    val hands = hands(this)
    val keyPairs = pairKeys(hands)
    val ordered = customOrder(keyPairs)
    return lowerLetters(ordered).plus(upperLetters(ordered))
}

fun writeCourseFile(path: String, course: Course) = try {
    File(path).writeText(course.toXml())
    true
} catch (e: Exception) {
    System.err.println(e.message)
    false
}

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {

    val parser = ArgParser("ktgen")

    // Course definition
    class CourseDefinition: Subcommand("course", "The course definition. More information can be found in the readme file.") {

        val file by option(ArgType.String, description = "Path to a course definition file.",
            fullName = "file", shortName = "f").default("")

        val stdin by option(ArgType.String, description = "The course definition as single string.",
            fullName = "stdin", shortName = "i").default("")

        val keyboardFile by option(ArgType.String, description = "Path to a keyboard layout xml file (KTouch export). Generates a course especially for that keyboard layout.",
            fullName = "keyboard-file", shortName = "k").default("")

        val value = mutableListOf<String>()

        override fun execute() {
            value.addAll(readCourseSymbols(file))
            value.addAll(parseCourseSymbols(stdin))
            if (keyboardFile.isNotEmpty()) KeyboardLayout.create(keyboardFile)?.toCourseSymbols()?.forEach { value.add(it) }
        }
    }
    val definition = CourseDefinition()
    parser.subcommands(definition)

    val output by parser.option(ArgType.String, "output", "o", "Output file.").default("ktgen_course.xml")
    val printCourseToStdout by parser.option(ArgType.Boolean, "print", "p", "Output the generated course also on stdout").default(false)

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

    if (definition.value.isEmpty()) {
        System.err.println("Missing (or empty) course definition. No course is created.")
        return
    }

    val course = createCourse(definition.value, dictionary, lineLength, symbolsPerLesson)

    if (printCourseToStdout)
        println(course.toXml())

    if (writeCourseFile(output, course) && !printCourseToStdout)
        println("-> $output")
}