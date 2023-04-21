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
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DictionaryKtTest {

    private val files = mutableListOf<String>()

    @AfterAll
    fun deleteFiles() {
        files.forEach { File(it).delete() }
    }

    @Test
    fun `extractWords happy`() {
        val text = "They'd lost their   7   keys (one each), \n\n in the so called _Good-Old-Greens_; (respectively_ äöü [ÜÄÖ] {niße})."
        val words = extractWords(text, 1, 100)
        words shouldBe listOf(
            "They", "d", "lost", "their", "keys", "one", "each", "in", "the",
            "so", "called", "Good", "Old", "Greens", "respectively", "äöü", "ÜÄÖ", "niße")
    }

    @Test
    fun `extractWords empty input`() {
        val text = ""
        val words = extractWords(text, 1, 100)
        words shouldBe emptyList()
    }

    @Test
    fun `extractWords input is null`() {
        val text = null
        val words = extractWords(text, 1, 100)
        words shouldBe emptyList()
    }

    @Test
    fun `extractWords max length smaller than min length`() {
        val text = "They'd lost their   7   keys (one each), \n\n in the so called _Good-Old-Greens_; (respectively_ äöü [ÜÄÖ] {niße})."
        val words = extractWords(text, 100, 1)
        words shouldBe emptyList()
    }

    @Test
    fun `consistsOfAny order does not matter`() {
        "abc".consistsOfAny("abc") shouldBe true
        "abc".consistsOfAny("cba") shouldBe true
        "abc".consistsOfAny("bca") shouldBe true
    }

    @Test
    fun `consistsOfAny there can be more symbols`() {
        "abc".consistsOfAny("abcdef") shouldBe true
        "abc".consistsOfAny("abcx")   shouldBe true
        "abc".consistsOfAny("abc1")   shouldBe true
        "abc".consistsOfAny("cba22")  shouldBe true
    }

    @Test
    fun `consistsOfAny duplicates are ignored`() {
        "abc".consistsOfAny("aaabbaccc")  shouldBe true
        "abc".consistsOfAny("xaaabbaccc") shouldBe true
        "abbaaab".consistsOfAny("ab") shouldBe true
        "abcabc".consistsOfAny("cba") shouldBe true
        "abcabc".consistsOfAny("cbaxy") shouldBe true
    }

    @Test
    fun `consistsOfAny only specified symbols are allowed`() {
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

    @Test
    fun `containsAny true`() {
        "abc".containsAny("a")   shouldBe true
        "abc".containsAny("b")   shouldBe true
        "abc".containsAny("c")   shouldBe true
        "abc".containsAny("cba") shouldBe true
        "abc".containsAny("cb")  shouldBe true
        "abc".containsAny("cbx") shouldBe true
        "abc".containsAny("xbx") shouldBe true
    }

    @Test
    fun `containsAny false`() {
        "abc".containsAny("x") shouldBe false
    }

    @Test
    fun `containsAny empty symbols`() {
        "abc".containsAny("") shouldBe false
    }

    @Test
    fun `textFromFile happy`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText("ctie uax xph eob")
        textFromFile(filename) shouldBe "ctie uax xph eob"
    }

    @Test
    fun `textFromFile no file`() {
        textFromFile("a_non_existing_file") shouldBe null
    }

    @Test
    fun `textFromFile path empty`() {
        textFromFile("") shouldBe null
    }


    private val ports = generateSequence(30116) { it + 1 }.iterator()

    @Test
    fun `textFromWebsite 200 content`() {
        val port = ports.next()
        embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)
        textFromWebsite("http://localhost:$port/") shouldBe "Example Domain Example Domain This domain is for use in illustrative examples in documents. You may use this domain in literature without prior coordination or asking for permission. More information..."
    }

    @Test
    fun `textFromWebsite 200 empty`() {
        val port = ports.next()
        embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)
        textFromWebsite("http://localhost:$port/empty") shouldBe ""
    }

    @Test
    fun `textFromWebsite 404 not found`() {
        val port = ports.next()
        embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)
        textFromWebsite("http://localhost:$port/no-such-path") shouldBe ""
    }

    @Test
    fun `textFromWebsite protocol missing`() {
        val port = ports.next()
        embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)
        textFromWebsite("localhost:$port/") shouldBe ""
    }

    @Test
    fun `textFromWebsite unknown host`() {
        textFromWebsite("http://cgtsirenbml8hcduygesrtiyelschtibyesr") shouldBe ""
    }

    @Test
    fun `textFromWebsite empty url`() {
        textFromWebsite("") shouldBe ""
    }

    @Test
    fun `buildDictionary happy`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText("apple pear grape")
        val port = ports.next()
        embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)
        val dict = buildDictionary(filename, "http://localhost:$port/", 0, 100, 1000)
        dict shouldContainAll setOf(
            "Domain", "Example", "More", "This", "You", "asking",
            "coordination", "documents", "domain", "examples", "for",
            "Example", "illustrative", "in", "information", "is",
            "literature", "may", "or", "permission", "prior",
            "this", "use", "without", "pear", "apple", "grape")
    }

    @Test
    fun `buildDictionary allow duplicates`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText("apple pear grape apple orange")
        val dict = buildDictionary(filename, "", 0, 100, 5)
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
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText("apple pear grape")
        val dict = buildDictionary(filename, "", 0, 100, 1000)
        dict shouldContainAll setOf("pear", "apple", "grape")
    }

    @Test
    fun `buildDictionary min word length`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText("apple pear grape")
        val port = ports.next()
        embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)

        val dict4 = buildDictionary(filename, "http://localhost:$port/", 4, 100, 1000)
        dict4.forAll { it shouldHaveMinLength 4 }

        val dict100 = buildDictionary(filename, "http://localhost:$port/", 100, 100, 1000)
        dict100 shouldBe emptyList()

        val dictNegative = buildDictionary(filename, "http://localhost:$port/", -1, 100, 1000)
        dictNegative shouldContainAll setOf(
            "Domain", "Example", "More", "This", "You", "asking",
            "coordination", "documents", "domain", "examples", "for",
            "Example", "illustrative", "in", "information", "is",
            "literature", "may", "or", "permission", "prior",
            "this", "use", "without", "pear", "apple", "grape")
    }

    @Test
    fun `buildDictionary max word length`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText("apple pear grape")
        val port = ports.next()
        embeddedServer(Netty, port, host = "0.0.0.0", module = Application::exampleCom).start(wait = false)

        val dict4 = buildDictionary(filename, "http://localhost:$port/", 0, 4, 1000)
        dict4.forAll { it shouldHaveMaxLength 4 }

        val dict0 = buildDictionary(filename, "http://localhost:$port/", 0, 0, 1000)
        dict0 shouldBe emptyList()

        val dictNegative = buildDictionary(filename, "http://localhost:$port/", 0, -1, 1000)
        dictNegative shouldBe emptyList()
    }

    @Test
    fun `buildDictionary repeat words when dictionary-max-length is greater than the number of existing words`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText("apple pear grape")
        buildDictionary(filename, "", 0, 100, 1000) shouldHaveSize 1000
    }

    @Test
    fun `buildDictionary dictionary-max-length range test`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText("apple pear grape")
        buildDictionary(filename, "", 0, 100, -10) shouldHaveSize 0
        buildDictionary(filename, "", 0, 100, -1) shouldHaveSize 0
        buildDictionary(filename, "", 0, 100, 0) shouldHaveSize 0
        buildDictionary(filename, "", 0, 100, 1) shouldHaveSize 1
        buildDictionary(filename, "", 0, 100, 10) shouldHaveSize 10
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