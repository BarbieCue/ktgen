package org.example

import io.kotest.matchers.shouldBe
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

class LessonSpecificationKtTest : IOExpectSpec({

    context("readLessonSpecification") {

        expect("read whitespace separated characters as lesson specification from file") {
            val file = tmpFile("ktgen_lesson_specification_test${UUID.randomUUID()}")
            file.writeText("ab cd\n{WW}     ,.\n\n  1234")
            readLessonSpecification(file.absolutePathString()) shouldBe listOf("ab", "cd", "{WW}", ",.", "1234")
        }

        expect("empty collection when file is empty") {
            val file = tmpFile("ktgen_lesson_specification_test${UUID.randomUUID()}")
            file.writeText("")
            readLessonSpecification(file.absolutePathString()) shouldBe emptyList()
        }

        expect("empty collection when file not found") {
            readLessonSpecification("a_non_existing_file") shouldBe emptyList()
        }

        expect("empty collection when path is empty") {
            readLessonSpecification("") shouldBe emptyList()
        }
    }

    context("parseLessonSpecification") {

        expect("read whitespace separated characters") {
            parseLessonSpecification("ab\t cd\n{WW}     ,.\n\n  1234") shouldBe listOf("ab", "cd", "{WW}", ",.", "1234")
        }

        expect("result is empty when input is empty") {
            parseLessonSpecification("") shouldBe emptyList()
        }
    }

    context("KeyboardLayout") {

        context("toLessonSpecification") {

            expect("english USA return string list of paired symbols") {
                val keyboardLayout = exampleKeyboardEnglishUSA()
                keyboardLayout!!.toLessonSpecification() shouldBe listOf(
                    "fj", "dk", "sl", "a", "gh", "ty", "vm",
                    "bn", "ru", "ei", "c", "wo", "x", "qp", "z",
                    "FJ", "DK", "SL", "A", "GH", "TY", "VM",
                    "BN", "RU", "EI", "C", "WO", "X", "QP", "Z"
                )
            }
        }
    }
})