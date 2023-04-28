package org.example

import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveMaxLength
import io.kotest.matchers.string.shouldHaveMinLength
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

class DictionaryKtTest : TempFileExpectSpec({

    context("extractWords") {

        expect("extract words from string, where words are separated by any whitespace characters or punctuation marks") {
            val text = "They'd lost their   7   keys (one each), \n\n in the so called _Good-Old-Greens_; (respectively_ äöü [ÜÄÖ] {niße})."
            val words = extractWords(text, 1, 100)
            words shouldBe listOf(
                "They", "d", "lost", "their", "keys", "one", "each", "in", "the",
                "so", "called", "Good", "Old", "Greens", "respectively", "äöü", "ÜÄÖ", "niße")
        }

        expect("return an empty list on empty input") {
            val text = ""
            val words = extractWords(text, 1, 100)
            words shouldBe emptyList()
        }

        expect("return an empty list when input is null") {
            val text = null
            val words = extractWords(text, 1, 100)
            words shouldBe emptyList()
        }

        expect("return an empty list when max-word-length is smaller than min-word-length") {
            val text = "They'd lost their   7   keys (one each), \n\n in the so called _Good-Old-Greens_; (respectively_ äöü [ÜÄÖ] {niße})."
            val words = extractWords(text, 100, 1)
            words shouldBe emptyList()
        }
    }


    context("consistsOfAny") {

        expect("true when the target string consists of the symbols from the input string") {
            "abc".consistsOfAny("abc") shouldBe true
        }

        expect("the symbol order does not matter") {
            "abc".consistsOfAny("bca") shouldBe true
        }

        expect("false when the input string contains different symbols than the target string") {
            "abc".containsAny("x") shouldBe false
        }

        expect("true when the input string contains more symbols than target string") {
            "abc".consistsOfAny("abcdef") shouldBe true
            "abc".consistsOfAny("abcx")   shouldBe true
            "abc".consistsOfAny("abc1")   shouldBe true
            "abc".consistsOfAny("cba22")  shouldBe true
        }

        expect("false when not all symbols from the input string are contained in the target string") {
            "abc".consistsOfAny("ab")  shouldBe false
            "abc".consistsOfAny("ac")  shouldBe false
            "abc".consistsOfAny("bc")  shouldBe false
            "abc".consistsOfAny("aaa") shouldBe false
            "abc".consistsOfAny("a")   shouldBe false
            "abc".consistsOfAny("b")   shouldBe false
            "abc".consistsOfAny("c")   shouldBe false
            "abc".consistsOfAny("ABC") shouldBe false
            "abc".consistsOfAny("A")   shouldBe false
            "abc".consistsOfAny("B")   shouldBe false
            "abc".consistsOfAny("C")   shouldBe false
        }

        expect("duplicates are ignored") {
            "abc".consistsOfAny("aaabbaccc")  shouldBe true
            "abc".consistsOfAny("xaaabbaccc") shouldBe true
            "abbaaab".consistsOfAny("ab") shouldBe true
            "abcabc".consistsOfAny("cba") shouldBe true
            "abcabc".consistsOfAny("cbaxy") shouldBe true
        }

        expect("false when input string is empty") {
            "abc".containsAny("") shouldBe false
        }

        context("textFromFile") {

            expect("reads text content of a file") {
                val file = tmpFile(UUID.randomUUID().toString())
                file.writeText("ctie uax xph eob")
                textFromFile(file.absolutePathString()) shouldBe "ctie uax xph eob"
            }

            expect("null when file does not exist") {
                textFromFile("a_non_existing_file") shouldBe null
            }

            expect("null when file path is empty") {
                textFromFile("") shouldBe null
            }

            expect("empty string when file is empty") {
                val file = tmpFile(UUID.randomUUID().toString())
                file.writeText("")
                textFromFile(file.absolutePathString()) shouldBe ""
            }
        }

        val ports = generateSequence(30116) { it + 1 }.iterator()

        context("textFromWebsite") {

            expect("reads text content from a string containing html, where words are separated by any whitespace characters") {
                val port = ports.next()
                embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)
                textFromWebsite("http://localhost:$port/") shouldBe "Example Domain Example Domain This domain is for use in illustrative examples in documents. You may use this domain in literature without prior coordination or asking for permission. More information..."
            }

            expect("empty string when the html source string is empty") {
                val port = ports.next()
                embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)
                textFromWebsite("http://localhost:$port/empty") shouldBe ""
            }

            expect("empty string on error response code (non 2xx)") {
                val port = ports.next()
                embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)
                textFromWebsite("http://localhost:$port/no-such-path") shouldBe ""
            }

            expect("empty string when protocol is missing") {
                val port = ports.next()
                embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)
                textFromWebsite("localhost:$port/") shouldBe ""
            }

            expect("empty string on unknown host") {
                textFromWebsite("http://cgtsirenbml8hcduygesrtiyelschtibyesr") shouldBe ""
            }

            expect("empty string when url is empty") {
                textFromWebsite("") shouldBe ""
            }
        }
    }
})



class DictionaryKtTestcticticti : FileTest() {

    @Test
    fun `buildDictionary happy`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText("apple pear grape")
        val port = ports.next()
        embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)
        val dict = buildDictionary(file.absolutePathString(), "http://localhost:$port/", 0, 100, 1000)
        dict shouldContainAll setOf(
            "Domain", "Example", "More", "This", "You", "asking",
            "coordination", "documents", "domain", "examples", "for",
            "Example", "illustrative", "in", "information", "is",
            "literature", "may", "or", "permission", "prior",
            "this", "use", "without", "pear", "apple", "grape")
    }

    @Test
    fun `buildDictionary allow duplicates`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText("apple pear grape apple orange")
        val dict = buildDictionary(file.absolutePathString(), "", 0, 100, 5)
        dict shouldBe listOf("apple", "pear", "grape", "apple", "orange")
        dict.count { it == "apple" } shouldBe 2
    }

    @Test
    fun `buildDictionary empty dictionary path`() {
        val port = ports.next()
        embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)
        val dict = buildDictionary("", "http://localhost:$port/", 0, 100, 1000)
        dict shouldContainAll setOf(
            "Domain", "Example", "More", "This", "You", "asking",
            "coordination", "documents", "domain", "examples", "for",
            "Example", "illustrative", "in", "information", "is",
            "literature", "may", "or", "permission", "prior",
            "this", "use", "without")
    }

    @Test
    fun `buildDictionary empty url`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText("apple pear grape")
        val dict = buildDictionary(file.absolutePathString(), "", 0, 100, 1000)
        dict shouldContainAll setOf("pear", "apple", "grape")
    }

    @Test
    fun `buildDictionary min word length`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText("apple pear grape")
        val port = ports.next()
        embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)

        val dict4 = buildDictionary(file.absolutePathString(), "http://localhost:$port/", 4, 100, 1000)
        dict4.forAll { it shouldHaveMinLength 4 }

        val dict100 = buildDictionary(file.absolutePathString(), "http://localhost:$port/", 100, 100, 1000)
        dict100 shouldBe emptyList()

        val dictNegative = buildDictionary(file.absolutePathString(), "http://localhost:$port/", -1, 100, 1000)
        dictNegative shouldContainAll setOf(
            "Domain", "Example", "More", "This", "You", "asking",
            "coordination", "documents", "domain", "examples", "for",
            "Example", "illustrative", "in", "information", "is",
            "literature", "may", "or", "permission", "prior",
            "this", "use", "without", "pear", "apple", "grape")
    }

    @Test
    fun `buildDictionary max word length`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText("apple pear grape")
        val port = ports.next()
        embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)

        val dict4 = buildDictionary(file.absolutePathString(), "http://localhost:$port/", 0, 4, 1000)
        dict4.forAll { it shouldHaveMaxLength 4 }

        val dict0 = buildDictionary(file.absolutePathString(), "http://localhost:$port/", 0, 0, 1000)
        dict0 shouldBe emptyList()

        val dictNegative = buildDictionary(file.absolutePathString(), "http://localhost:$port/", 0, -1, 1000)
        dictNegative shouldBe emptyList()
    }

    @Test
    fun `buildDictionary repeat words when dictionary-max-length is greater than the number of existing words`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText("apple pear grape")
        buildDictionary(file.absolutePathString(), "", 0, 100, 1000) shouldHaveSize 1000
    }

    @Test
    fun `buildDictionary dictionary-max-length range test`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText("apple pear grape")
        buildDictionary(file.absolutePathString(), "", 0, 100, -10) shouldHaveSize 0
        buildDictionary(file.absolutePathString(), "", 0, 100, -1) shouldHaveSize 0
        buildDictionary(file.absolutePathString(), "", 0, 100, 0) shouldHaveSize 0
        buildDictionary(file.absolutePathString(), "", 0, 100, 1) shouldHaveSize 1
        buildDictionary(file.absolutePathString(), "", 0, 100, 10) shouldHaveSize 10
    }
}

private fun Application.exampleCom() {
    routing {

        get("/empty") {
            call.respond("")
        }

        get("/") {
            call.respondText("""
                <!doctype html>
                <html>
                <head>
                    <title>Example Domain</title>

                    <meta charset="utf-8" />
                    <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1" />
                    <style type="text/css">
                        body {
                            background-color: #f0f0f2;
                            margin: 0;
                            padding: 0;
                            font-family: -apple-system, system-ui, BlinkMacSystemFont, "Segoe UI", "Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;

                        }
                        div {
                            width: 600px;
                            margin: 5em auto;
                            padding: 2em;
                            background-color: #fdfdff;
                            border-radius: 0.5em;
                            box-shadow: 2px 3px 7px 2px rgba(0,0,0,0.02);
                        }
                        a:link, a:visited {
                            color: #38488f;
                            text-decoration: none;
                        }
                        @media (max-width: 700px) {
                            div {
                                margin: 0 auto;
                                width: auto;
                            }
                        }
                    </style>
                </head>

                <body>
                <div>
                    <h1>Example Domain</h1>
                    <p>This domain is for use in illustrative examples in documents. You may use this
                        domain in literature without prior coordination or asking for permission.</p>
                    <p><a href="https://www.iana.org/domains/example">More information...</a></p>
                </div>
                </body>
                </html>
            """.trimIndent())
        }
    }
}