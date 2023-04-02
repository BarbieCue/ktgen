package org.example

import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldContainAnyOf
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldHaveMaxLength
import io.kotest.matchers.string.shouldNotMatch
import org.junit.jupiter.api.Test

class OperationKtTest {

    @Test
    fun `shuffle happy`() {
        shuffle("abcdefghijk") shouldHaveLength 11
        shuffle("abcdefghijk") shouldNotBe "abcdefghijk"
    }

    @Test
    fun `shuffle empty input`() {
        shuffle("") shouldBe ""
    }

    @Test
    fun `shuffle trimmed`() {
        shuffle("  abc  ").take(1) shouldNotMatch "\\s"
        shuffle("  abc  ").takeLast(1) shouldNotMatch "\\s"
    }

    @Test
    fun `repeat happy`() {
        repeat("abc", 10) shouldHaveLength 10
        repeat("abc", 10) shouldBe "abcabcabca"
    }

    @Test
    fun `repeat empty input`() {
        repeat("", 10) shouldBe ""
    }

    @Test
    fun `repeat trimmed`() {
        repeat("  abc  ", 10).take(1) shouldNotMatch "\\s"
        repeat("  abc  ", 10).takeLast(1) shouldNotMatch "\\s"
    }

    @Test
    fun `repeat length range test`() {
        repeat("abc", -100) shouldHaveLength 0
        repeat("abc", -1) shouldHaveLength 0
        repeat("abc", 0) shouldHaveLength 0
        repeat("abc", 1) shouldHaveLength 1
        repeat("abc", 100) shouldHaveLength 100
    }

    @Test
    fun `segment happy`() {
        segment("abcdefghi", 2) shouldHaveLength 13 // 9 chars + 4 new whitespaces
        segment("abcdefghi", 2) shouldBe "ab cd ef gh i"
    }

    @Test
    fun `segment empty input`() {
        segment("", 2) shouldBe ""
    }

    @Test
    fun `segment trimmed`() {
        segment("  abc  ", 10).take(1) shouldNotMatch "\\s"
        segment("  abc  ", 10).takeLast(1) shouldNotMatch "\\s"
    }

    @Test
    fun `segment length range test`() {
        segment("abcdefghi", -100) shouldHaveLength 0
        segment("abcdefghi", -1) shouldHaveLength 0
        segment("abcdefghi", 0) shouldHaveLength 0
        segment("abcdefghi", 1) shouldHaveLength 17  // a b c d e f g h i
        segment("abcdefghi", 3) shouldHaveLength 11  // abc def ghi
        segment("abcdefghi", 100) shouldHaveLength 9 // abcdefghi
    }

    @Test
    fun `cutEnd happy`() {
        cutEnd("abc", 2) shouldBe "ab"
    }

    @Test
    fun `cutEnd trimmed`() {
        cutEnd("  abc   ", 5).take(1) shouldNotMatch "\\s"
        cutEnd("  abc   ", 5).takeLast(1) shouldNotMatch "\\s"
    }

    @Test
    fun `cutEnd empty input`() {
        segment("", 2) shouldBe ""
    }

    @Test
    fun `cutEnd length range test`() {
        cutEnd("abc", -100) shouldHaveLength 0
        cutEnd("abc", -1) shouldHaveLength 0
        cutEnd("abc", 0) shouldHaveLength 0
        cutEnd("abc", 1) shouldHaveLength 1  // a
        cutEnd("abc", 3) shouldHaveLength 3  // abc
        cutEnd("abc", 100) shouldHaveLength 3 // abc
    }

    @Test
    fun `toText happy`() {
        val words = setOf(
            "building", "metrics", "perimeter", "expensive", "a", "b", "c",
            "d", "engine", "of", "house", "train", "car", "bird", "door"
        )
        toText(words, 20) shouldBe """
            building metrics
            perimeter expensive
            a b c d engine of
            house train car bird
            door
        """.trimIndent()
    }

    @Test
    fun `toText empty input`() {
        toText(emptySet(), 20) shouldBe ""
    }

    @Test
    fun `toText all words are shorter than line length`() {
        toText(setOf("a", "b", "c"), 2) shouldBe """
            a
            b
            c
        """.trimIndent()
    }

    @Test
    fun `toText all words are as long as line length`() {
        toText(setOf("a", "b", "c"), 1) shouldBe """
            a
            b
            c
        """.trimIndent()
    }

    @Test
    fun `toText all words are longer than line length`() {
        toText(setOf("aaa", "bbb", "ccc"), 1) shouldBe ""
    }

    @Test
    fun `toText trimmed`() {
        toText(setOf("aaa  ", "  bbb", "  ccc  "), 4).take(1) shouldNotMatch "\\s"
        toText(setOf("aaa  ", "  bbb", "  ccc  "), 4).takeLast(1) shouldNotMatch "\\s"
    }

    @Test
    fun `toText line length range test`() {
        val words = setOf(
            "building", "metrics", "perimeter", "expensive", "a", "b", "c",
            "d", "engine", "of", "house", "train", "car", "bird", "door"
        )
        toText(words, -10).split('\n').forAll { line -> line shouldHaveMaxLength 0 }
        toText(words, -1).split('\n').forAll { line -> line shouldHaveMaxLength 0 }
        toText(words, 0).split('\n').forAll { line -> line shouldHaveMaxLength 0 }
        toText(words, 1).split('\n').forAll { line -> line shouldHaveMaxLength 1 }
        toText(words, 10).split('\n').forAll { line -> line shouldHaveMaxLength 10 }
    }

    @Test
    fun `toText does not exceed line-length when concatenating words`() {
        val words = setOf(
            "building", "metrics", "perimeter", "expensive", "a", "b", "c",
            "d", "engine", "of", "house", "train", "car", "bird", "door"
        )
        toText(words, 20).lines().forAll { it.length shouldBeLessThanOrEqual 20 }
        toText(words, 20) shouldBe """
            building metrics
            perimeter expensive
            a b c d engine of
            house train car bird
            door
        """.trimIndent()
    }

    @Test
    fun `takeRepeat happy`() {
        val words = setOf("building", "metrics", "perimeter")
        words.takeRepeat(1)  shouldBe listOf("building")
        words.takeRepeat(3)  shouldBe listOf("building", "metrics", "perimeter")
        words.takeRepeat(10)  shouldBe listOf(
            "building", "metrics", "perimeter",
            "building", "metrics", "perimeter",
            "building", "metrics", "perimeter",
            "building")
    }

    @Test
    fun `takeRepeat empty source`() {
        val words = emptySet<String>()
        words.takeRepeat(1) shouldBe emptyList()
        words.takeRepeat(3) shouldBe emptyList()
        words.takeRepeat(10) shouldBe emptyList()
    }

    @Test
    fun `takeRepeat trimmed`() {
        val words = emptySet<String>()
        words.takeRepeat(4).forAll {
            it.take(1) shouldNotMatch "\\s"
            it.takeLast(1) shouldNotMatch "\\s"
        }
    }

    @Test
    fun `takeRepeat length range test`() {
        val words = setOf("building", "metrics", "perimeter")
        words.takeRepeat(-10) shouldHaveSize 0
        words.takeRepeat(-1) shouldHaveSize 0
        words.takeRepeat(0)  shouldHaveSize 0
        words.takeRepeat(1)  shouldHaveSize 1
        words.takeRepeat(10)  shouldHaveSize 10
    }

    @Test
    fun `splitWWLeftRight happy`() {
        splitWWLeftRight("[{WW}]") shouldBe Pair("[{", "}]")
        splitWWLeftRight("[{WW") shouldBe Pair("[{", "")
        splitWWLeftRight("WW") shouldBe Pair("", "")
    }

    @Test
    fun `splitWWLeftRight empty`() {
        splitWWLeftRight("") shouldBe Pair("", "")
    }

    @Test
    fun `splitWWLeftRight wrong separator`() {
        splitWWLeftRight("XXX") shouldBe Pair("XXX", "XXX")
    }

    @Test
    fun `pairs map contents`() {
        pairs shouldContainExactly hashMapOf(
            '(' to ')',
            '[' to ']',
            '{' to '}',
            '<' to '>',
            '"' to '"',
            '\'' to '\'',
            '`' to '`'
        )
    }

    @Test
    fun `pairsRevers map contents`() {
        pairsRevers shouldContainExactly hashMapOf(
            ')'  to '(',
            ']'  to '[',
            '}'  to '{',
            '>'  to '<',
            '"'  to '"',
            '\'' to '\'',
            '`'  to '`'
        )
    }

    @Test
    fun `randomPair happy`() {
        repeat(10) {
            randomPair("[", "]") shouldBe ("[" to "]")
            randomPair("[{", "}]") shouldBeIn listOf(("[" to "]"), ("{" to "}"))
            randomPair("\"", "\"") shouldBe ("\"" to "\"")
        }
    }

    @Test
    fun `randomPair left is empty`() {
        repeat(10) {
            randomPair("", "}") shouldBe ("" to "}")
            randomPair("", "T") shouldBe ("" to "T")
        }
    }

    @Test
    fun `randomPair right is empty`() {
        repeat(10) {
            randomPair("<", "") shouldBe ("<" to "")
            randomPair("X", "") shouldBe ("X" to "")
        }
    }

    @Test
    fun `randomPair both are empty`() {
        repeat(10) {
            randomPair("", "") shouldBe ("" to "")
        }
    }

    @Test
    fun `randomPair on both unknown symbols, one side (randomly) should be empty`() {
        repeat(10) {
            randomPair("X", "T") shouldBeIn listOf(("X" to ""), ("" to "T"))
        }
    }

    @Test
    fun `randomPair on one known and one unknown symbol, one side (randomly) should be empty`() {
        repeat(10) {
            randomPair("(", "T") shouldBeIn listOf(("(" to ""), ("" to "T"))
            randomPair("X", ")") shouldBeIn listOf(("X" to ""), ("" to ")"))
        }
    }

    @Test
    fun `randomPair known and unknown symbols, one side (randomly) should be empty`() {
        repeat(10) {
            randomPair("(X", "T") shouldBeIn listOf(("X" to ""), ("(" to ""), ("" to "T"))
            randomPair("X", "T)") shouldBeIn listOf(("X" to ""), ("" to "T"), ("" to ")"))
            randomPair("(X", "T)") shouldBeIn listOf(("X" to ""), ("" to "T"), ("(" to ""), ("" to ")"), ("(" to ")"))
        }
    }

    @Test
    fun `punctuationMarksLine do not try to pair`() {
        val wwString = "!WW="
        repeat(10) {
            punctuationMarksLine(wwString, 3) shouldBeIn setOf(
                "!!!", "!!=", "!==", "===", "=!!", "==!", "!=!", "=!="
            )
        }
    }

    @Test
    fun `punctuationMarksLine do not try to pair, left and right empty`() {
        val wwString = "WW"
        repeat(10) {
            punctuationMarksLine(wwString, 3) shouldBe ""
        }
    }

    @Test
    fun `punctuationMarksLine do not try to pair, empty input`() {
        val wwString = ""
        repeat(10) {
            punctuationMarksLine(wwString, 3) shouldBe ""
        }
    }

    @Test
    fun `punctuationMarksLine do not try to pair, WW missing`() {
        val wwString = "()"
        repeat(10) {
            punctuationMarksLine(wwString, 2) shouldBeIn setOf(
                "((", "))", "()", ")("
            )
        }
    }

    @Test
    fun `punctuationMarksLine do not try to pair, length test`() {
        val wwString = "!WW="
        repeat(10) {
            punctuationMarksLine(wwString, -3) shouldHaveLength 0
            punctuationMarksLine(wwString, -1) shouldHaveLength 0
            punctuationMarksLine(wwString, 0) shouldHaveLength 0
            punctuationMarksLine(wwString, 1) shouldHaveLength 1
            punctuationMarksLine(wwString, 3) shouldHaveLength 3
        }
    }

    @Test
    fun `punctuationMarksLine try to pair`() {
        val wwString = "!WW="
        repeat(10) {
            punctuationMarksLine(wwString, 3, true) shouldBeIn setOf(
                "!!!", "!!=", "!==", "===", "=!!", "==!", "!=!", "=!="
            )
        }
    }

    @Test
    fun `punctuationMarksLine try to pair, length test`() {
        val wwString = "!WW="
        repeat(10) {
            punctuationMarksLine(wwString, -3, true) shouldHaveLength 0
            punctuationMarksLine(wwString, -1, true) shouldHaveLength 0
            punctuationMarksLine(wwString, 0, true) shouldHaveLength 0
            punctuationMarksLine(wwString, 1, true) shouldHaveLength 1
            punctuationMarksLine(wwString, 3, true) shouldHaveLength 3
        }
    }

    @Test
    fun `punctuationMarksLine try to pair, WW missing`() {
        val wwString = "()"
        repeat(10) {
            punctuationMarksLine(wwString, 2) shouldBeIn setOf(
                "((", "))", "()", ")("
            )
        }
    }

    @Test
    fun `wordsWithPunctuationMarks do not try to pair`() {
        val words = setOf("grape", "apple", "pear")
        val wwString = "!WW="
        repeat(10) {
            wordsWithPunctuationMarks(words, wwString) shouldContainAnyOf setOf(
                "=grape", "=apple", "=pear",
                "grape=", "apple=", "pear=",
                "!grape", "!apple", "!pear",
                "grape!", "apple!", "pear!")
        }
    }

    @Test
    fun `wordsWithPunctuationMarks do not try to pair, left and right empty`() {
        val words = setOf("grape", "apple", "pear")
        val wwString = "WW"
        repeat(10) {
            wordsWithPunctuationMarks(words, wwString) shouldContainAnyOf setOf(
                "grape", "apple", "pear")
        }
    }

    @Test
    fun `wordsWithPunctuationMarks do not try to pair, empty input`() {
        val words = setOf("grape", "apple", "pear")
        val wwString = ""
        repeat(10) {
            wordsWithPunctuationMarks(words, wwString) shouldContainAnyOf setOf(
                "grape", "apple", "pear")
        }
    }

    @Test
    fun `wordsWithPunctuationMarks try to pair, no pair-able characters`() {
        val words = setOf("grape", "apple", "pear")
        val wwString = "!WW="
        repeat(10) {
            wordsWithPunctuationMarks(words, wwString, true) shouldContainAnyOf setOf(
                "grape=", "apple=", "pear=",
                "!grape", "!apple", "!pear")
        }
    }

    @Test
    fun `wordsWithPunctuationMarks try to pair, build pairs`() {
        val words = setOf("grape", "apple", "pear")
        val wwString = "(WW)"
        repeat(10) {
            wordsWithPunctuationMarks(words, wwString, true) shouldContainAnyOf setOf(
                "(grape)", "(apple)", "(pear)")
        }
    }

    @Test
    fun `wordsWithPunctuationMarks try to pair, mix pair-able and non-pair-able characters`() {
        val words = setOf("grape", "apple", "pear")
        val wwString = "!?(WW)="
        repeat(10) {
            wordsWithPunctuationMarks(words, wwString, true) shouldContainAnyOf setOf(
                "!grape", "!apple", "!pear",
                "?grape", "?apple", "?pear",
                "grape=", "apple=", "pear=",
                "(grape)", "(apple)", "(pear)")
        }
    }

    @Test
    fun `wordsWithPunctuationMarks try to pair, left and right empty`() {
        val words = setOf("grape", "apple", "pear")
        val wwString = "WW"
        repeat(10) {
            wordsWithPunctuationMarks(words, wwString, true) shouldContainAnyOf setOf(
                "grape", "apple", "pear")
        }
    }

    @Test
    fun `wordsWithPunctuationMarks try to pair, empty input`() {
        val words = setOf("grape", "apple", "pear")
        val wwString = ""
        repeat(10) {
            wordsWithPunctuationMarks(words, wwString, true) shouldContainAnyOf setOf(
                "grape", "apple", "pear")
        }
    }
}