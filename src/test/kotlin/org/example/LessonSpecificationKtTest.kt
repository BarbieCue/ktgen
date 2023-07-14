package org.example

import io.kotest.matchers.shouldBe
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

class LessonSpecificationKtTest : IOExpectSpec({

    val port = 30122

    context("readLessonSpecification") {

        expect("empty collection when input is empty") {
            readLessonSpecification("") shouldBe emptyList()
        }

        expect("empty collection when input contains only whitespace characters") {
            readLessonSpecification("  \n\t   \n") shouldBe emptyList()
        }

        expect("empty collection when cannot connect to remote host") {
            readLessonSpecification("http://localhost:12345/spec.ktgen") shouldBe emptyList()
        }

        expect("empty collection when web ressource not found") {
            startLocalhostWebServer(port, Application::lessonSpecification)
            readLessonSpecification("http://localhost:$port/${UUID.randomUUID()}.ktgen") shouldBe emptyList()
        }

        expect("empty collection when web ressource exists but is empty") {
            startLocalhostWebServer(port, Application::lessonSpecification)
            readLessonSpecification("http://localhost:$port/empty.ktgen") shouldBe emptyList()
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
                val file = tmpFile("ktgen_lesson_specification_test${UUID.randomUUID()}", suffix = ".ktgen")
                file.writeText("ab cd ef gh")
                readLessonSpecification(file.absolutePathString()) shouldBe listOf("ab", "cd", "ef", "gh")
            }

            expect("empty collection when file exists but is empty") {
                val file = tmpFile("file-${UUID.randomUUID()}", suffix = ".ktgen")
                file.writeText("")
                readLessonSpecification(file.absolutePathString()) shouldBe emptyList()
            }

            expect("file content can have many whitespace characters") {
                val file = tmpFile("ktgen_lesson_specification_test${UUID.randomUUID()}", suffix = ".ktgen")
                file.writeText("\n   \t ab cd\n{WW}     ,.\n\n  1234")
                readLessonSpecification(file.absolutePathString()) shouldBe listOf("ab", "cd", "{WW}", ",.", "1234")
            }
        }

        context("lesson specification from web") {

            expect("can read lesson specification from web when url ends with .ktgen)") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                readLessonSpecification("http://localhost:$port/spec.ktgen") shouldBe listOf("ab", "cd", "ef", "gh")
            }

            expect("empty collection when url does not end with .ktgen") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                readLessonSpecification("http://localhost:$port/spec") shouldBe emptyList()
            }

            expect("specification from web can have many whitespace characters") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                readLessonSpecification("http://localhost:$port/spec-whitespaces.ktgen") shouldBe listOf("ab", "cd", "ef", "gh")
            }
        }

        context("keyboard layout from file") {

            expect("can extract lesson specification from a keyboard layout xml file when file path ends with .xml)") {
                val file = tmpFile("english-usa-${UUID.randomUUID()}", suffix = ".xml")
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

            expect("empty collection when file path does not end with .xml") {
                val file = tmpFile("english-usa-${UUID.randomUUID()}")
                file.writeText(ktouchKeyboardLayoutEnglishUSA)
                readLessonSpecification(file.absolutePathString()) shouldBe emptyList()
            }

            expect("empty collection when file path ends with .xml but xml content cannot be parsed") {
                val file = tmpFile("keyboard-${UUID.randomUUID()}", suffix = ".xml")
                file.writeText("""
                  <?xml version="1.0"?>
                  <keyboa
                """.trimEnd())
                readLessonSpecification(file.absolutePathString()) shouldBe emptyList()
            }

            expect("empty collection when file exists but is empty") {
                val file = tmpFile("keyboard-${UUID.randomUUID()}", suffix = ".xml")
                file.writeText("")
                readLessonSpecification(file.absolutePathString()) shouldBe emptyList()
            }
        }

        context("keyboard layout from web") {

            expect("can read and extract lesson specification from a keyboard layout from web when url ends with .xml") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                readLessonSpecification("http://localhost:$port/keyboard.xml") shouldBe listOf(
                    "fj", "dk", "sl", "a;", "gh", "ty",
                    "vm", "bn", "ru", "ei", "c,", "wo",
                    "x.", "qp", "z/", "10", "`-", "29",
                    "38", "47", "56", "=[", "]\\", "'",
                    "FJ", "DK", "SL", "A:", "GH", "TY",
                    "VM", "BN", "RU", "EI", "C<", "WO",
                    "X>", "QP", "Z?", "!)", "~_", "@(",
                    "#*", "$&", "%^", "+{", "}|", "\"")
            }

            expect("empty collection when url does not end with .xml") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                readLessonSpecification("http://localhost:$port/keyboard") shouldBe emptyList()
            }

            expect("empty collection when url ends with .xml but xml content cannot be parsed") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                readLessonSpecification("http://localhost:$port/malformed-keyboard.xml") shouldBe emptyList()
            }

            expect("empty collection when web ressource exists but is empty") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                readLessonSpecification("http://localhost:$port/empty-keyboard.xml") shouldBe emptyList()
            }
        }
    }

    context("loadTextFromFileOrWeb") {

        context("read from file") {

            expect("can read string content from file") {
                val file = tmpFile("file-${UUID.randomUUID()}", suffix = ".ktgen")
                file.writeText("some text content :)")
                loadTextFromFileOrWeb(file.absolutePathString()) shouldBe "some text content :)"
            }

            expect("empty when file is empty") {
                val file = tmpFile("file-${UUID.randomUUID()}")
                file.writeText("")
                loadTextFromFileOrWeb(file.absolutePathString()) shouldBe ""
            }

            expect("result is trimmed") {
                val file = tmpFile("file-${UUID.randomUUID()}")
                file.writeText("   a b c \n\t   \t")
                loadTextFromFileOrWeb(file.absolutePathString()) shouldBe "a b c"
            }
        }

        context("read from web") {

            expect("can read text content from web when url ends with .ktgen") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                loadTextFromFileOrWeb("http://localhost:$port/spec.ktgen") shouldBe """
                    ab cd
                    ef gh
                """.trimIndent()
            }

            expect("can read text content from web when url ends with .xml") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                loadTextFromFileOrWeb("http://localhost:$port/keyboard.xml") shouldBe ktouchKeyboardLayoutEnglishUSA
            }

            expect("empty when url does not end with .xml or .ktgen") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                loadTextFromFileOrWeb("http://localhost:$port/example") shouldBe ""
            }

            expect("empty when web ressource is empty") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                loadTextFromFileOrWeb("http://localhost:$port/empty.ktgen") shouldBe ""
            }

            expect("empty when response code is not 200") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                loadTextFromFileOrWeb("http://localhost:$port/non-200.ktgen") shouldBe ""
            }

            expect("result is trimmed") {
                startLocalhostWebServer(port, Application::lessonSpecification)
                loadTextFromFileOrWeb("http://localhost:$port/spec-whitespaces.ktgen") shouldBe "ab cd ef gh"
            }
        }
    }

    context("isHttpUri") {

        expect("true when input is a valid URI (RFC2396) and starts with http") {
            "http://www.math.uio.no/faq/compression-faq/part1.html".isHttpUri() shouldBe true
        }

        expect("false when input is a valid URI (RFC2396) but does not start with http") {
            "www.math.uio.no/faq/compression-faq/part1.html".isHttpUri() shouldBe false
        }

        expect("false when input is not a valid URI (RFC2396)") {
            "http://faqcompression-{}faqpart1html".isHttpUri() shouldBe false
            "ab cd ef gh".isHttpUri() shouldBe false
        }

        expect("false when input is empty") {
            "".isHttpUri() shouldBe false
        }
    }

    context("isFile") {

        expect("true when file exists") {
            val file = tmpFile("file-${UUID.randomUUID()}")
            file.absolutePathString().isFile()
        }

        expect("false when file not exists") {
            "abc".isFile()
        }

        expect("false on empty input") {
            "".isFile()
        }
    }

    context("parseLessonSpecificationText") {

        expect("split content at whitespace characters (trimmed)") {
            parseLessonSpecificationText("  ab cd\n{WW}     ,.\n\n  1234 ") shouldBe listOf(
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

        get("/empty.ktgen") {
            call.respondText("")
        }

        get("/non-200.ktgen") {
            call.respond(HttpStatusCode.NotFound, "ab cd ef gh")
        }

        get("/spec") {
            call.respondText("""
                ab cd
                ef gh
            """.trimIndent())
        }

        get("/spec.ktgen") {
            call.respondText("""
                ab cd
                ef gh
            """.trimIndent())
        }

        get("/spec-whitespaces.ktgen") {
            call.respondText("   \n ab cd ef gh\n")
        }

        get("/no-suffix") {
            call.respondText("ab cd ef gh")
        }

        get("/keyboard.xml") {
            call.respondText(ktouchKeyboardLayoutEnglishUSA)
        }

        get("/malformed-keyboard.xml") {
            call.respondText("""
                  <?xml version="1.0"?>
                  <keyboa
                """.trimEnd())
        }

        get("/empty-keyboard.xml") {
            call.respondText("")
        }
    }
}