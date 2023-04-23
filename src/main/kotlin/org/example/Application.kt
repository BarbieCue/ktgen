package org.example

import kotlinx.cli.*


fun main(args: Array<String>) {

    val parser = ArgParser("ktgen", strictSubcommandOptionsOrder = true)

    // Course definition
    val courseDefinition by parser.option(ArgType.String, "course-definition", "c", "Path to a course definition input file. For more information please read the readme file.").default("course_definition.ktgen")
    val keyboard by parser.option(ArgType.String, "keyboard", "k", "Path to a keyboard layout xml file (KTouch export). Generates finger-wise lowercase and uppercase letter lessons.").default("")
    val outputFile by parser.option(ArgType.String, "output", "o", "Output file.").default("ktgen_course.xml")
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

    val courseSymbols =
        if (keyboard.isNotEmpty())
            KeyboardLayout.create(keyboard)
                ?.toCourseSymbols()
                ?.plus(readCourseSymbols(courseDefinition))
                ?: readCourseSymbols(courseDefinition)
        else
            readCourseSymbols(courseDefinition)
    val dictionary = buildDictionary(textFile, website, minWordLength, maxWordLength, dictionarySize)

    if (courseSymbols.isEmpty()) {
        System.err.println("Missing (or empty) course input. No course is created.")
        return
    }

    val course = createCourse(courseSymbols, dictionary, lineLength, symbolsPerLesson)

    if (printCourseToStdout)
        println(course.toXml())

    if (writeCourseFile(outputFile, course) && !printCourseToStdout)
        println("-> $outputFile")
}