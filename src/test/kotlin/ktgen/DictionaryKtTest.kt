package ktgen

import io.kotest.inspectors.forAll
import io.kotest.matchers.sequences.shouldContainAll
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
            words.toList() shouldBe listOf(
                "They", "d", "lost", "their", "keys", "one", "each", "in", "the",
                "so", "called", "Good", "Old", "Greens", "respectively", "äöü", "ÜÄÖ", "niße")
        }

        expect("empty result collection on empty input") {
            val text = ""
            val words = extractWords(text, 1, 100)
            words shouldBe emptySequence()
        }

        expect("empty result collection when input is null") {
            val text = null
            val words = extractWords(text, 1, 100)
            words shouldBe emptySequence()
        }

        expect("empty result collection when max-word-length is smaller than min-word-length") {
            val text = "They'd lost their   7   keys (one each), \n\n in the so called _Good-Old-Greens_; (respectively_ äöü [ÜÄÖ] {niße})."
            val words = extractWords(text, 100, 1)
            words shouldBe emptySequence()
        }

        expect("empty result collection when max-word-length is zero or negative") {
            val text = "They'd lost their   7   keys (one each), \n\n in the so called _Good-Old-Greens_; (respectively_ äöü [ÜÄÖ] {niße})."
            extractWords(text, 100, 0) shouldBe emptySequence()
            extractWords(text, 100, -1) shouldBe emptySequence()
        }

        expect("when min-word-length is negative, default to 0") {
            val text = "They'd lost their   7   keys (one each), \n\n in the so called _Good-Old-Greens_; (respectively_ äöü [ÜÄÖ] {niße})."
            extractWords(text, 0, 100).toList() shouldBe
                    listOf("They", "d", "lost", "their", "keys", "one", "each", "in", "the", "so",
                        "called", "Good", "Old", "Greens", "respectively", "äöü", "ÜÄÖ", "niße")
            extractWords(text, -1, 100).toList() shouldBe
                    listOf("They", "d", "lost", "their", "keys", "one", "each", "in", "the", "so",
                        "called", "Good", "Old", "Greens", "respectively", "äöü", "ÜÄÖ", "niße")
        }
    }


    context("consistsOf") {

        expect("true when the target string consists of the symbols from the input string") {
            "abc".consistsOf("abc") shouldBe true
        }

        expect("the symbol order does not matter") {
            "abc".consistsOf("bca") shouldBe true
        }

        expect("true when the input string contains more symbols than target string") {
            "abc".consistsOf("abcdef") shouldBe true
            "abc".consistsOf("abcx") shouldBe true
            "abc".consistsOf("abc1") shouldBe true
            "abc".consistsOf("cba22") shouldBe true
        }

        expect("false when not all symbols from the input string are contained in the target string") {
            "abc".consistsOf("ab") shouldBe false
            "abc".consistsOf("ac") shouldBe false
            "abc".consistsOf("bc") shouldBe false
            "abc".consistsOf("aaa") shouldBe false
            "abc".consistsOf("a") shouldBe false
            "abc".consistsOf("b") shouldBe false
            "abc".consistsOf("c") shouldBe false
            "abc".consistsOf("A") shouldBe false
            "abc".consistsOf("B") shouldBe false
            "abc".consistsOf("C") shouldBe false
        }

        expect("upper lower case is important") {
            "abc".consistsOf("ABC") shouldBe false
        }

        expect("duplicates are ignored") {
            "abc".consistsOf("aaabbaccc") shouldBe true
            "abc".consistsOf("xaaabbaccc") shouldBe true
            "abbaaab".consistsOf("ab") shouldBe true
            "abcabc".consistsOf("cba") shouldBe true
            "abcabc".consistsOf("cbaxy") shouldBe true
        }
    }

    context("containsAny") {

        expect("true when input string contains at least one symbol that is also contained in the target string") {
            "abc".containsAny("a") shouldBe true
            "abc".containsAny("b") shouldBe true
            "abc".containsAny("c") shouldBe true
            "abc".containsAny("ac") shouldBe true
            "abc".containsAny("ax") shouldBe true
        }

        expect("false when input string is empty") {
            "abc".containsAny("") shouldBe false
        }

        expect("false when the input string symbols differ completely from the symbols in the target string") {
            "abc".containsAny("x") shouldBe false
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

    context("textFromWebsite") {

        expect("reads text content from a string containing html, where words are separated by any whitespace characters") {
            val address = startLocalHttpServer(Application::exampleCom)
            textFromWebsite("$address/") shouldBe "Example Domain Example Domain This domain is for use in illustrative examples in documents. You may use this domain in literature without prior coordination or asking for permission. More information..."
        }

        expect("empty string when the html source string is empty") {
            val address = startLocalHttpServer(Application::exampleCom)
            textFromWebsite("$address/empty") shouldBe ""
        }

        expect("empty string on error response code (non 2xx)") {
            val address = startLocalHttpServer(Application::exampleCom)
            textFromWebsite("$address/no-such-path") shouldBe ""
        }

        expect("empty string when protocol is missing") {
            val address = startLocalHttpServer(Application::exampleCom)
            textFromWebsite("${address.substringAfter("http://")}/") shouldBe ""
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
            val address = startLocalHttpServer(Application::exampleCom)
            val dict = buildDictionary(file.absolutePathString(), "$address/", 0, 100, 1000)
            dict shouldContainAll sequenceOf(
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
            val address = startLocalHttpServer(Application::exampleCom)
            val dict = buildDictionary("", "$address/", 0, 100, 1000)
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
            buildDictionary("", "", 0, 100, 1000).toList() shouldBe emptyList()
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
            buildDictionary(file.absolutePathString(), "", 0, 0, 1000).toList() shouldBe emptyList()
            buildDictionary(file.absolutePathString(), "", 0, -1, 1000).toList() shouldBe emptyList()
        }
    }
})

private fun Application.exampleCom() {
    routing {

        get("/empty") {
            call.respondText("")
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