package org.example

import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

class DictionaryKtTest : IOExpectSpec({

    context("extractWords") {

        expect("extract words from string, where words are separated by any whitespace characters or punctuation marks") {
            val text = "They'd lost their   7   keys (one each), \n\n in the so called _Good-Old-Greens_; (respectively_ äöü [ÜÄÖ] {niße})."
            val words = extractWords(text, 1, 100)
            words shouldBe listOf(
                "They", "d", "lost", "their", "keys", "one", "each", "in", "the",
                "so", "called", "Good", "Old", "Greens", "respectively", "äöü", "ÜÄÖ", "niße")
        }

        expect("empty result collection on empty input") {
            val text = ""
            val words = extractWords(text, 1, 100)
            words shouldBe emptyList()
        }

        expect("empty result collection when input is null") {
            val text = null
            val words = extractWords(text, 1, 100)
            words shouldBe emptyList()
        }

        expect("empty result collection when max-word-length is smaller than min-word-length") {
            val text = "They'd lost their   7   keys (one each), \n\n in the so called _Good-Old-Greens_; (respectively_ äöü [ÜÄÖ] {niße})."
            val words = extractWords(text, 100, 1)
            words shouldBe emptyList()
        }

        expect("empty result collection when max-word-length is zero or negative") {
            val text = "They'd lost their   7   keys (one each), \n\n in the so called _Good-Old-Greens_; (respectively_ äöü [ÜÄÖ] {niße})."
            extractWords(text, 100, 0) shouldBe emptyList()
            extractWords(text, 100, -1) shouldBe emptyList()
        }

        expect("when min-word-length is negative, default to 0") {
            val text = "They'd lost their   7   keys (one each), \n\n in the so called _Good-Old-Greens_; (respectively_ äöü [ÜÄÖ] {niße})."
            extractWords(text, 0, 100) shouldBe
                    listOf("They", "d", "lost", "their", "keys", "one", "each", "in", "the", "so",
                        "called", "Good", "Old", "Greens", "respectively", "äöü", "ÜÄÖ", "niße")
            extractWords(text, -1, 100) shouldBe
                    listOf("They", "d", "lost", "their", "keys", "one", "each", "in", "the", "so",
                        "called", "Good", "Old", "Greens", "respectively", "äöü", "ÜÄÖ", "niße")
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
            "abc".consistsOfAny("abcx") shouldBe true
            "abc".consistsOfAny("abc1") shouldBe true
            "abc".consistsOfAny("cba22") shouldBe true
        }

        expect("false when not all symbols from the input string are contained in the target string") {
            "abc".consistsOfAny("ab") shouldBe false
            "abc".consistsOfAny("ac") shouldBe false
            "abc".consistsOfAny("bc") shouldBe false
            "abc".consistsOfAny("aaa") shouldBe false
            "abc".consistsOfAny("a") shouldBe false
            "abc".consistsOfAny("b") shouldBe false
            "abc".consistsOfAny("c") shouldBe false
            "abc".consistsOfAny("ABC") shouldBe false
            "abc".consistsOfAny("A") shouldBe false
            "abc".consistsOfAny("B") shouldBe false
            "abc".consistsOfAny("C") shouldBe false
        }

        expect("duplicates are ignored") {
            "abc".consistsOfAny("aaabbaccc") shouldBe true
            "abc".consistsOfAny("xaaabbaccc") shouldBe true
            "abbaaab".consistsOfAny("ab") shouldBe true
            "abcabc".consistsOfAny("cba") shouldBe true
            "abcabc".consistsOfAny("cbaxy") shouldBe true
        }

        expect("false when input string is empty") {
            "abc".containsAny("") shouldBe false
        }
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

    val ports = generateSequence(30121) { it + 1 }.iterator()

    context("textFromWebsite") {

        expect("reads text content from a string containing html, where words are separated by any whitespace characters") {
            val port = ports.next()
            startLocalhostWebServer(port, Application::exampleCom)
            textFromWebsite("http://localhost:$port/") shouldBe "Example Domain Example Domain This domain is for use in illustrative examples in documents. You may use this domain in literature without prior coordination or asking for permission. More information..."
        }

        expect("empty string when the html source string is empty") {
            val port = ports.next()
            startLocalhostWebServer(port, Application::exampleCom)
            textFromWebsite("http://localhost:$port/empty") shouldBe ""
        }

        expect("empty string on error response code (non 2xx)") {
            val port = ports.next()
            startLocalhostWebServer(port, Application::exampleCom)
            textFromWebsite("http://localhost:$port/no-such-path") shouldBe ""
        }

        expect("empty string when protocol is missing") {
            val port = ports.next()
            startLocalhostWebServer(port, Application::exampleCom)
            textFromWebsite("localhost:$port/") shouldBe ""
        }

        expect("empty string on unknown host") {
            textFromWebsite("http://cgtsirenbml8hcduygesrtiyelschtibyesr") shouldBe ""
        }

        expect("empty string when url is empty") {
            textFromWebsite("") shouldBe ""
        }
    }

    context("buildDictionary") {

        expect("build collection of words from website and text file") {
            val file = tmpFile(UUID.randomUUID().toString())
            file.writeText("apple pear grape")
            val port = ports.next()
            startLocalhostWebServer(port, Application::exampleCom)
            val dict = buildDictionary(file.absolutePathString(), "http://localhost:$port/", 0, 100, 1000)
            dict shouldContainAll setOf(
                "Domain", "Example", "More", "This", "You", "asking",
                "coordination", "documents", "domain", "examples", "for",
                "Example", "illustrative", "in", "information", "is",
                "literature", "may", "or", "permission", "prior",
                "this", "use", "without", "pear", "apple", "grape")
        }

        expect("result collection can contain the same word many times") {
            val file = tmpFile(UUID.randomUUID().toString())
            file.writeText("apple pear grape apple orange apple")
            val dict = buildDictionary(file.absolutePathString(), "", 0, 100, 6)
            dict shouldContainAll listOf("apple", "pear", "grape", "apple", "orange", "apple")
            dict.count { it == "apple" } shouldBe 3
        }

        expect("result collection contains only words from website when text file path is empty") {
            val port = ports.next()
            startLocalhostWebServer(port, Application::exampleCom)
            val dict = buildDictionary("", "http://localhost:$port/", 0, 100, 1000)
            dict shouldContainAll setOf(
                "Domain", "Example", "More", "This", "You", "asking",
                "coordination", "documents", "domain", "examples", "for",
                "Example", "illustrative", "in", "information", "is",
                "literature", "may", "or", "permission", "prior",
                "this", "use", "without")
        }

        expect("result collection contains only words from dictionary when website url is empty") {
            val file = tmpFile(UUID.randomUUID().toString())
            file.writeText("apple pear grape")
            val dict = buildDictionary(file.absolutePathString(), "", 0, 100, 1000)
            dict shouldContainAll setOf("pear", "apple", "grape")
        }

        expect("result collection is empty when text file path is empty and website url is empty") {
            buildDictionary("", "", 0, 100, 1000) shouldBe emptyList()
        }

        expect("result collection contains only words having the specified minimal length") {
            val file = tmpFile(UUID.randomUUID().toString())
            file.writeText("to be are see apple pear grape co")
            val dict = buildDictionary(file.absolutePathString(), "", 4, 100, 1000)
            dict shouldContainAll setOf("apple", "pear", "grape")
        }

        expect("when min-word-length is negative, default to 0") {
            val file = tmpFile(UUID.randomUUID().toString())
            file.writeText("to be are see apple pear grape co")
            val dict = buildDictionary(file.absolutePathString(), "", -1, 100, 1000)
            dict shouldContainAll setOf("to", "be", "are", "see", "apple", "pear", "grape", "co")
        }

        expect("result collection contains only words having the specified maximal length") {
            val file = tmpFile(UUID.randomUUID().toString())
            file.writeText("to be are see apple pear grape co")
            val dict = buildDictionary(file.absolutePathString(), "", 0, 3, 1000)
            dict shouldContainAll setOf("to", "be", "are", "see", "co")
            dict.forAll { it.length <= 3 }
        }

        expect("result collection is empty when max-word-length is zero or negative") {
            val file = tmpFile(UUID.randomUUID().toString())
            file.writeText("to be are see apple pear grape co")
            buildDictionary(file.absolutePathString(), "", 0, 0, 1000) shouldBe emptyList()
            buildDictionary(file.absolutePathString(), "", 0, -1, 1000) shouldBe emptyList()
        }

        expect("repeat words when dictionary-max-length is greater than the number of existing words") {
            val file = tmpFile(UUID.randomUUID().toString())
            file.writeText("apple pear")
            val dict = buildDictionary(file.absolutePathString(), "", 0, 100, 1000)
            dict shouldHaveSize 1000
            dict.count { it == "apple" } shouldBe 500
            dict.count { it == "pear" } shouldBe 500
        }
    }
})

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