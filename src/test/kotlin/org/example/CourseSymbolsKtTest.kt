package org.example

import io.kotest.matchers.shouldBe
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

class CourseSymbolsKtTest : IOExpectSpec({

    context("readCourseSymbols") {

        expect("read whitespace separated characters as course symbols from file") {
            val file = tmpFile("ktgen_course_definition_test${UUID.randomUUID()}")
            file.writeText("ab cd\n{WW}     ,.\n\n  1234")
            readCourseSymbols(file.absolutePathString()) shouldBe listOf("ab", "cd", "{WW}", ",.", "1234")
        }

        expect("empty collection when file is empty") {
            val file = tmpFile("ktgen_course_definition_test${UUID.randomUUID()}")
            file.writeText("")
            readCourseSymbols(file.absolutePathString()) shouldBe emptyList()
        }

        expect("empty collection when file not found") {
            readCourseSymbols("a_non_existing_file") shouldBe emptyList()
        }

        expect("empty collection when path is empty") {
            readCourseSymbols("") shouldBe emptyList()
        }
    }

    context("parseCourseSymbols") {

        expect("read whitespace separated characters") {
            parseCourseSymbols("ab\t cd\n{WW}     ,.\n\n  1234") shouldBe listOf("ab", "cd", "{WW}", ",.", "1234")
        }

        expect("result is empty when input is empty") {
            parseCourseSymbols("") shouldBe emptyList()
        }
    }

    context("KeyboardLayout") {

        context("toCourseSymbols") {

            expect("english USA return string list of paired symbols") {
                val keyboardLayout = exampleKeyboardEnglishUSA()
                keyboardLayout!!.toCourseSymbols() shouldBe listOf(
                    "fj", "dk", "sl", "a", "gh", "ty", "vm",
                    "bn", "ru", "ei", "c", "wo", "x", "qp", "z",
                    "FJ", "DK", "SL", "A", "GH", "TY", "VM",
                    "BN", "RU", "EI", "C", "WO", "X", "QP", "Z"
                )
            }
        }
    }
})