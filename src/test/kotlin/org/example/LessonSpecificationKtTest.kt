package org.example

import io.kotest.matchers.shouldBe
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

class LessonSpecificationKtTest : IOExpectSpec({

    context("readLessonSpecification") {

        expect("read whitespace separated character groups as lesson specification from file") {
            val file = tmpFile("ktgen_lesson_specification_test${UUID.randomUUID()}")
            file.writeText("ab cd\n{WW}     ,.\n\n  1234")
            readLessonSpecification(file.absolutePathString()) shouldBe listOf("ab", "cd", "{WW}", ",.", "1234")
        }

        expect("read lesson specification from a keyboard layout xml file") {
            val file = tmpFile("english-usa-${UUID.randomUUID()}.xml")
            file.writeText(ktouchKeyboardLayoutEnglishUSA)
            readLessonSpecification(file.absolutePathString()) shouldBe listOf(
                "fj", "dk", "sl", "a;", "gh", "ty",
                "vm", "bn", "ru", "ei", "c,", "wo",
                "x.", "qp", "z/", "10", "`-", "29",
                "38", "47", "56", "=[", "]\\", "'",
                "FJ", "DK", "SL", "A:", "GH", "TY",
                "VM", "BN", "RU", "EI", "C<", "WO",
                "X>", "QP", "Z?", "!)", "~_", "@(",
                "#*", "$&", "%^", "+{", "}|", "\"")
        }

        expect("read lesson specification directly from input string, when it is not empty and not an existing file path") {
            readLessonSpecification("ab cd ef gh") shouldBe listOf("ab", "cd", "ef", "gh")
        }

        expect("empty collection when file exists but is empty") {
            val file = tmpFile("ktgen_lesson_specification_test${UUID.randomUUID()}")
            file.writeText("")
            readLessonSpecification(file.absolutePathString()) shouldBe emptyList()
        }

        expect("empty collection when input is empty") {
            readLessonSpecification("") shouldBe emptyList()
        }
    }

    context("parseLessonSpecificationText") {

        expect("split content at whitespace characters (trimmed)") {
            parseLessonSpecificationText("ab cd\n{WW}     ,.\n\n  1234") shouldBe listOf(
                "ab", "cd", "{WW}", ",.", "1234")
        }

        expect("empty input leads to empty output") {
            parseLessonSpecificationText("") shouldBe emptyList()
        }

        expect("empty output when input is empty after trim") {
            parseLessonSpecificationText(" \n\t  ") shouldBe emptyList()
        }
    }

    context("KeyboardLayout") {

        context("toLessonSpecification") {

            expect("english USA return string list of custom ordered, paired symbols") {
                val keyboardLayout = exampleKeyboardEnglishUSA()
                keyboardLayout!!.toLessonSpecification() shouldBe listOf(
                    "fj", "dk", "sl", "a;", "gh", "ty",
                    "vm", "bn", "ru", "ei", "c,", "wo",
                    "x.", "qp", "z/", "10", "`-", "29",
                    "38", "47", "56", "=[", "]\\", "'",
                    "FJ", "DK", "SL", "A:", "GH", "TY",
                    "VM", "BN", "RU", "EI", "C<", "WO",
                    "X>", "QP", "Z?", "!)", "~_", "@(",
                    "#*", "$&", "%^", "+{", "}|", "\"")
            }
        }
    }
})