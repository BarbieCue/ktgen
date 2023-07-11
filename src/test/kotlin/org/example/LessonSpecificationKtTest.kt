package org.example

import io.kotest.matchers.shouldBe
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

class LessonSpecificationKtTest : IOExpectSpec({

    val port = 30121

    context("readLessonSpecification") {

        expect("empty collection when input is empty") {
            readLessonSpecification("") shouldBe emptyList()
        }

        expect("empty collection when file exists but is empty") {
            val file = tmpFile("ktgen_lesson_specification_test${UUID.randomUUID()}")
            file.writeText("")
            readLessonSpecification(file.absolutePathString()) shouldBe emptyList()
        }

        expect("empty collection when cannot connect to remote host") {
            readLessonSpecification("http://localhost:12345") shouldBe emptyList()
        }

        expect("empty collection when web ressource not found") {
            startLocalhostWebServer(port, Application::lessonSpecification)
            readLessonSpecification("http://localhost:$port/${UUID.randomUUID()}") shouldBe emptyList()
        }

        expect("empty collection when web ressource exists, but is empty") {
            startLocalhostWebServer(port, Application::lessonSpecification)
            readLessonSpecification("http://localhost:$port/empty") shouldBe emptyList()
        }


        context("lesson specification from string") {

            expect("can read lesson specification directly from input string") {
                readLessonSpecification("ab cd ef gh") shouldBe listOf("ab", "cd", "ef", "gh")
            }

            expect("input string can have many whitespace characters") {
                readLessonSpecification("\n  ab \t  cd  \n ef gh  ") shouldBe listOf("ab", "cd", "ef", "gh")
            }
        }


        context("lesson specification from file") {

            expect("can read lesson specification from file") {
                val file = tmpFile("ktgen_lesson_specification_test${UUID.randomUUID()}")
                file.writeText("ab cd ef gh")
                readLessonSpecification(file.absolutePathString()) shouldBe listOf("ab", "cd", "ef", "gh")
            }

            expect("file content can have many whitespace characters") {
                val file = tmpFile("ktgen_lesson_specification_test${UUID.randomUUID()}")
                file.writeText("\n   \t ab cd\n{WW}     ,.\n\n  1234")
                readLessonSpecification(file.absolutePathString()) shouldBe listOf("ab", "cd", "{WW}", ",.", "1234")
            }
        }


        context("lesson specification from web") {

            expect("can read lesson specification from web") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                readLessonSpecification("http://localhost:$port/spec") shouldBe listOf("ab", "cd", "ef", "gh")
            }

            expect("specification from web can have many whitespace characters") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                readLessonSpecification("http://localhost:$port/spec-whitespaces") shouldBe listOf("ab", "cd", "ef", "gh")
            }
        }


        context("keyboard layout from file") {

            expect("can extract lesson specification from a keyboard layout xml file") {
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

            expect("empty collection when keyboard layout file contains invalid xml") {
                val file = tmpFile("english-usa-${UUID.randomUUID()}.xml")
                file.writeText("""
                  <?xml version="1.0"?>
                  <keyboa
                """.trimEnd())
                readLessonSpecification(file.absolutePathString()) shouldBe emptyList()
            }
        }

        context("keyboard layout from web") {
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

private fun Application.lessonSpecification() {
    routing {

        get("/empty") {
            call.respond("")
        }

        get("/spec") {
            call.respondText("""
                ab cd
                ef gh
            """.trimIndent())
        }

        get("/spec-whitespaces") {
            call.respondText("   \n ab cd    \n \t ef gh\n")
        }

        get("/keyboard") {
            call.respondText(ktouchKeyboardLayoutEnglishUSA)
        }
    }
}