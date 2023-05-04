package org.example

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldContainAnyOf
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.*
import org.junit.jupiter.api.Test

class OperationKtTest : ExpectSpec({

    context("shuffle") {

        expect("shuffle the input symbols") {
            shuffle("abcdefghijk") shouldHaveLength 11
            shuffle("abcdefghijk") shouldNotBe "abcdefghijk"
            shuffle("abcdefghijk") shouldMatch "[abcdefghijk]{11}".toRegex()
        }

        expect("also shuffle input whitespaces") {
            shuffle("  abc  ") shouldMatch "[ abc]+".toRegex()
        }

        expect("empty input leads to empty output") {
            shuffle("") shouldBe ""
        }

        expect("result is trimmed") {
            shuffle("  abc  ") shouldNotStartWith "\\s"
            shuffle("  abc  ") shouldNotEndWith "\\s"
        }
    }

    context("repeat") {

        expect("repeat the input symbols") {
            repeat("abc", 10) shouldBe "abcabcabca"
        }

        expect("also repeat input whitespaces") {
            repeat("  abc  ", 10) shouldBe "abc    a"
        }

        expect("empty input leads to empty output") {
            repeat("", 10) shouldBe ""
        }

        expect("result is trimmed") {
            repeat("  a  ", 10) shouldNotStartWith "\\s"
            repeat("  a  ", 10) shouldNotEndWith "\\s"
        }

        expect("length range test") {
            repeat("abc", -100) shouldHaveLength 0
            repeat("abc", -1) shouldHaveLength 0
            repeat("abc", 0) shouldHaveLength 0
            repeat("abc", 1) shouldHaveLength 1
            repeat("abc", 100) shouldHaveLength 100
        }
    }

    context("segment") {

        expect("segment the input string") {
            segment("abcdefghi", 2) shouldHaveLength 13
            segment("abcdefghi", 2) shouldBe "ab cd ef gh i"
        }

        expect("also treat whitespaces as normal symbols") {
            segment("a b c", 2) shouldBe "a  b  c"
        }

        expect("empty input leads to empty output") {
            segment("", 2) shouldBe ""
        }

        expect("result is trimmed") {
            segment("  abc  ", 10) shouldNotStartWith "\\s"
            segment("  abc  ", 10) shouldNotEndWith "\\s"
        }

        expect("length range test") {
            segment("abcdefghi", -100) shouldHaveLength 0
            segment("abcdefghi", -1) shouldHaveLength 0
            segment("abcdefghi", 0) shouldHaveLength 0
            segment("abcdefghi", 1) shouldHaveLength 17  // a b c d e f g h i
            segment("abcdefghi", 3) shouldHaveLength 11  // abc def ghi
            segment("abcdefghi", 100) shouldHaveLength 9 // abcdefghi
        }
    }
})

class OperationKtTestDeleteMe {
    @Test
    fun `joinRepeat happy`() {
        val words = setOf(
            "building", "metrics", "perimeter",
            "expensive", "a", "b", "c", "d"
        )

        joinRepeat(words, 30)shouldBe
                "building metrics perimeter expens"
        joinRepeat(words, 30).count { !it.isWhitespace() } shouldBe 30

        joinRepeat(words, 60) shouldBe
                "building metrics perimeter expensive a b c d building metrics building"
        joinRepeat(words, 60).count { !it.isWhitespace() } shouldBe 60
    }

    @Test
    fun `joinRepeat empty input`() {
        joinRepeat(emptySet(), 20) shouldBe ""
    }

    @Test
    fun `joinRepeat all words are longer than line the length in non whitespace chars`() {
        joinRepeat(setOf("aaa", "bbb", "ccc"), 1) shouldBe ""
    }

    @Test
    fun `joinRepeat ignore too long words`() {
        joinRepeat(setOf("too_long", "ok"), 2) shouldBe "ok"
        joinRepeat(setOf("too_long", "ok"), 4) shouldBe "ok ok"
    }

    @Test
    fun `joinRepeat should find a word of necessary length out of order if exists`() {
        joinRepeat(setOf("ab", "cde", "fgh"), 7) shouldBe "ab cde ab"
        // "fgh" would exceed the length of 7, thus take "ab"
    }

    @Test
    fun `joinRepeat should cut last word if cannot find a word of necessary length`() {
        joinRepeat(setOf("abc", "def"), 5) shouldBe "abc de"
        // "def" would exceed the length of 7 and no word of length 2 can be found, thus cut "def" to "de"
    }

    @Test
    fun `joinRepeat trimmed`() {
        joinRepeat(setOf("aaa  ", "  bbb", "  ccc  "), 9) shouldNotStartWith "\\s"
        joinRepeat(setOf("aaa  ", "  bbb", "  ccc  "), 9) shouldNotEndWith "\\s"
    }

    @Test
    fun `joinRepeat each word trimmed`() {
        joinRepeat(setOf("aaa  ", "  bbb", "  ccc  "), 9) shouldBe "aaa bbb ccc"
    }

    @Test
    fun `joinRepeat length non whitespace chars range test`() {
        val words = setOf(
            "building", "metrics", "perimeter", "expensive", "a", "b", "c",
            "d", "engine", "of", "house", "train", "car", "bird", "door"
        )
        joinRepeat(words, -10) shouldHaveLength 0
        joinRepeat(words, -1) shouldHaveLength 0
        joinRepeat(words, 0) shouldHaveLength 0
        joinRepeat(words, 1).count { !it.isWhitespace() } shouldBe 1
        joinRepeat(words, 10).filter { !it.isWhitespace() } shouldHaveMaxLength 10
    }

    @Test
    fun `Sequence repeatInfinite happy`() {
        val repeated = setOf("a", "b", "c").asSequence().repeatInfinite().take(7).toList()
        repeated shouldHaveSize 7
        repeated[0] shouldBe "a"
        repeated[1] shouldBe "b"
        repeated[2] shouldBe "c"
        repeated[3] shouldBe "a"
        repeated[4] shouldBe "b"
        repeated[5] shouldBe "c"
        repeated[6] shouldBe "a"
    }

    @Test
    fun `Sequence repeatInfinite empty sequence`() {
        val looped = emptyList<String>().asSequence().repeatInfinite().toList()
        looped shouldHaveSize 0
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
    fun `splitWWLeftRight no separator`() {
        splitWWLeftRight("[{}]") shouldBe Pair("[{}]", "[{}]")
        splitWWLeftRight("[{") shouldBe Pair("[{", "[{")
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
    fun `randomPair both unknown symbols, one side (randomly) should be empty`() {
        repeat(10) {
            randomPair("X", "T") shouldBeIn listOf(("X" to ""), ("" to "T"))
        }
    }

    @Test
    fun `randomPair one known and one unknown symbol, one side (randomly) should be empty`() {
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
    fun `punctuationMarks non-pair-able symbols`() {
        val punctuationMarks = "!WW="
        repeat(10) {
            punctuationMarks(punctuationMarks, 3) shouldBeIn setOf(
                "!!!", "!!=", "!==", "===", "=!!", "==!", "!=!", "=!="
            )
        }
    }

    @Test
    fun `punctuationMarks WW left and right empty`() {
        val punctuationMarks = "WW"
        repeat(10) {
            punctuationMarks(punctuationMarks, 3) shouldBe ""
        }
    }

    @Test
    fun `punctuationMarks empty punctuation marks`() {
        val punctuationMarks = ""
        repeat(10) {
            punctuationMarks(punctuationMarks, 3) shouldBe ""
        }
    }

    @Test
    fun `punctuationMarks no WW`() {
        val punctuationMarks = "()"
        repeat(10) {
            punctuationMarks(punctuationMarks, 2) shouldBeIn setOf(
                "((", "))", "()", ")("
            )
        }
    }

    @Test
    fun `punctuationMarks length range test`() {
        val punctuationMarks = "!WW="
        repeat(10) {
            punctuationMarks(punctuationMarks, -100) shouldHaveLength 0
            punctuationMarks(punctuationMarks, -1) shouldHaveLength 0
            punctuationMarks(punctuationMarks, 0) shouldHaveLength 0
            punctuationMarks(punctuationMarks, 1) shouldHaveLength 1
            punctuationMarks(punctuationMarks, 100) shouldHaveLength 100
        }
    }

    @Test
    fun `wordsWithPunctuationMarks no WW`() {
        val words = setOf("grape", "apple", "pear")
        val punctuationMarks = "!;"
        repeat(10) {
            wordsWithPunctuationMarks(words, punctuationMarks) shouldContainAnyOf setOf(
                "!grape", "!apple", "!pear",
                "grape!", "apple!", "pear!",
                ";grape", ";apple", ";pear",
                "grape;", "apple;", "pear;")
        }
    }

    @Test
    fun `wordsWithPunctuationMarks WW with non-pair-able symbols`() {
        val words = setOf("grape", "apple", "pear")
        val punctuationMarks = "!WW="
        repeat(10) {
            wordsWithPunctuationMarks(words, punctuationMarks) shouldContainAnyOf setOf(
                "=grape", "=apple", "=pear",
                "grape=", "apple=", "pear=",
                "!grape", "!apple", "!pear",
                "grape!", "apple!", "pear!")
        }
    }

    @Test
    fun `wordsWithPunctuationMarks WW with pair-able symbols`() {
        val words = setOf("grape", "apple", "pear")
        val punctuationMarks = "{[WW]}"
        repeat(10) {
            wordsWithPunctuationMarks(words, punctuationMarks) shouldContainAnyOf setOf(
                "{grape}", "{apple}", "{pear}",
                "[grape]", "[apple]", "[pear]")
        }
    }

    @Test
    fun `wordsWithPunctuationMarks WW left and right empty`() {
        val words = setOf("grape", "apple", "pear")
        val punctuationMarks = "WW"
        repeat(10) {
            wordsWithPunctuationMarks(words, punctuationMarks) shouldContainAnyOf setOf(
                "grape", "apple", "pear")
        }
    }

    @Test
    fun `wordsWithPunctuationMarks empty punctuation marks`() {
        val words = setOf("grape", "apple", "pear")
        val punctuationMarks = ""
        repeat(10) {
            wordsWithPunctuationMarks(words, punctuationMarks) shouldBe emptyList()
        }
    }

    @Test
    fun `wordsWithPunctuationMarks empty word list`() {
        val words = emptySet<String>()
        val punctuationMarks = "!WW="
        repeat(10) {
            wordsWithPunctuationMarks(words, punctuationMarks) shouldBe emptyList()
        }
    }

    @Test
    fun `wordsWithPunctuationMarks mix pair-able and non-pair-able symbols`() {
        val words = setOf("grape", "apple", "pear")
        val punctuationMarks = "!?(WW)="
        repeat(10) {
            wordsWithPunctuationMarks(words, punctuationMarks) shouldContainAnyOf setOf(
                "!grape", "!apple", "!pear",
                "?grape", "?apple", "?pear",
                "grape=", "apple=", "pear=",
                "(grape)", "(apple)", "(pear)")
        }
    }
}