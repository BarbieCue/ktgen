package org.example

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.collections.*
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

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

    context("joinRepeat") {

        expect("result is a single line") {
            val words = setOf(
                "building", "metrics", "perimeter",
                "expensive", "a", "b", "c", "d")

            checkAll(20, Arb.int(10, 10000)) {
                joinRepeat(words, it) shouldNotContain "\n"
            }
        }

        expect("result consists of joined words, separated by whitespaces") {
            val words = setOf(
                "building", "metrics", "perimeter",
                "expensive", "a", "b", "c", "d")

            joinRepeat(words, 33) shouldBe "building metrics perimeter expensive"
        }

        expect("result contains the exact amount of non-whitespace symbols") {
            val words = setOf(
                "building", "metrics", "perimeter",
                "expensive", "a", "b", "c", "d")

            joinRepeat(words, 30).count { !it.isWhitespace() } shouldBe 30
            joinRepeat(words, 60).count { !it.isWhitespace() } shouldBe 60
        }

        expect("empty input leads to empty output") {
            joinRepeat(emptySet(), 20) shouldBe ""
        }

        expect("result is empty when there is no word having a length <= sum-non-whitespace-chars") {
            joinRepeat(setOf("aaa", "bbb", "ccc"), 1) shouldBe ""
            joinRepeat(setOf("a  "), 1) shouldBe ""
        }

        expect("ignore too long words") {
            joinRepeat(setOf("too_long", "ok"), 2) shouldBe "ok"
            joinRepeat(setOf("too_long", "ok"), 4) shouldBe "ok ok"
        }

        expect("find a word of necessary length out of order, if necessary and exists") {
            joinRepeat(setOf("ab", "cde", "fgh"), 7) shouldBe "ab cde ab"
            // "fgh" would exceed the length of 7, thus take "ab"
        }

        expect("cut last word if cannot find a word of necessary length") {
            joinRepeat(setOf("abc", "def"), 5) shouldBe "abc de"
            // "def" would exceed the length of 7 and no word of length 2 can be found, thus cut "def" to "de"
        }

        expect("result is trimmed") {
            joinRepeat(setOf("aaa  ", "  bbb", "  ccc  "), 9) shouldNotStartWith "\\s"
            joinRepeat(setOf("aaa  ", "  bbb", "  ccc  "), 9) shouldNotEndWith "\\s"
        }

        expect("each word is trimmed") {
            joinRepeat(setOf("aaa  ", "  bbb", "  ccc  "), 9) shouldBe "aaa bbb ccc"
        }

        expect("sum-non-whitespace-chars range test") {
            val words = setOf(
            "building", "metrics", "perimeter", "expensive", "a", "b", "c",
            "d", "engine", "of", "house", "train", "car", "bird", "door")

            joinRepeat(words, -10) shouldHaveLength 0
            joinRepeat(words, -1) shouldHaveLength 0
            joinRepeat(words, 0) shouldHaveLength 0
            joinRepeat(words, 1).count { !it.isWhitespace() } shouldBe 1
            joinRepeat(words, 10).filter { !it.isWhitespace() } shouldHaveMaxLength 10
        }
    }

    context("Collection") {

        context("repeatInfinite") {

            expect("repeat the source collection in order") {
                val repeated = setOf("a", "b", "c").repeatInfinite().take(7).toList()
                repeated shouldHaveSize 7
                repeated[0] shouldBe "a"
                repeated[1] shouldBe "b"
                repeated[2] shouldBe "c"
                repeated[3] shouldBe "a"
                repeated[4] shouldBe "b"
                repeated[5] shouldBe "c"
                repeated[6] shouldBe "a"
            }

            expect("empty source collection leads to empty result") {
                val looped = emptyList<String>().repeatInfinite().toList()
                looped shouldHaveSize 0
            }
        }
    }

    context("splitWWLeftRight") {

        expect("splits the WW part into left and right") {
            splitWWLeftRight("[{WW}]") shouldBe Pair("[{", "}]")
            splitWWLeftRight("[{WW") shouldBe Pair("[{", "")
            splitWWLeftRight("WW") shouldBe Pair("", "")
        }

        expect("empty input leads to empty output") {
            splitWWLeftRight("") shouldBe Pair("", "")
        }

        expect("left and right will contain the original input, when WW is missing") {
            splitWWLeftRight("XXX") shouldBe Pair("XXX", "XXX")
            splitWWLeftRight("a") shouldBe Pair("a", "a")
            splitWWLeftRight("aW") shouldBe Pair("aW", "aW")
            splitWWLeftRight("[{}]") shouldBe Pair("[{}]", "[{}]")
            splitWWLeftRight("[{") shouldBe Pair("[{", "[{")
        }
    }

    context("pairing") {

        expect("pairs-map should have exactly this content") {
            pairs shouldContainExactly hashMapOf(
            '(' to ')',
            '[' to ']',
            '{' to '}',
            '<' to '>',
            '"' to '"',
            '\'' to '\'',
            '`' to '`')
        }

        expect("pairsRevers-map should have exactly this content") {
            pairsRevers shouldContainExactly hashMapOf(
            ')'  to '(',
            ']'  to '[',
            '}'  to '{',
            '>'  to '<',
            '"'  to '"',
            '\'' to '\'',
            '`'  to '`')
        }

        context("randomPair") {

            expect("map related symbols to each other") {
                repeat(10) {
                    randomPair("[", "]") shouldBe ("[" to "]")
                    randomPair("[{", "}]") shouldBeIn listOf(("[" to "]"), ("{" to "}"))
                    randomPair("\"", "\"") shouldBe ("\"" to "\"")
                }
            }

            expect("left output will be empty when left input is empty") {
                repeat(10) {
                    randomPair("", "}") shouldBe ("" to "}")
                    randomPair("", "T") shouldBe ("" to "T")
                }
            }

            expect("right output will be empty when right input is empty") {
                repeat(10) {
                    randomPair("<", "") shouldBe ("<" to "")
                    randomPair("X", "") shouldBe ("X" to "")
                }
            }

            expect("output pair will be empty when input pair is empty") {
                randomPair("", "") shouldBe ("" to "")
            }

            expect("one side (randomly) of the output pair will be empty, when both input symbols are not map-able") {
                repeat(10) {
                    randomPair("X", "T") shouldBeIn listOf(("X" to ""), ("" to "T"))
                }
            }

            expect("one side (randomly) of the output pair will be empty, when one of the input symbols is not map-able") {
                repeat(10) {
                    randomPair("(", "T") shouldBeIn listOf(("(" to ""), ("" to "T"))
                    randomPair("X", ")") shouldBeIn listOf(("X" to ""), ("" to ")"))
                    randomPair("(X", "T") shouldBeIn listOf(("X" to ""), ("(" to ""), ("" to "T"))
                    randomPair("X", "T)") shouldBeIn listOf(("X" to ""), ("" to "T"), ("" to ")"))
                    randomPair("(X", "T)") shouldBeIn listOf(("X" to ""), ("" to "T"), ("(" to ""), ("" to ")"), ("(" to ")"))
                }
            }
        }
    }

    context("punctuationMarks") {

        expect("result is a random permutation of the input symbols") {
            repeat(10) {
                punctuationMarks("!WW=", 3) shouldMatch "[!=]{3}".toRegex()
            }
        }

        expect("input is not limited to punctuation marks") {
            repeat(10) {
                punctuationMarks("abc", 3) shouldMatch "[abc]{3}".toRegex()
                punctuationMarks("123", 3) shouldMatch "[123]{3}".toRegex()
            }
        }

        expect("empty input leads to empty output") {
            repeat(10) {
                punctuationMarks("", 3) shouldBe ""
            }
        }

        expect("result is empty, when input contains an empty WW string") {
            repeat(10) {
                punctuationMarks("WW", 3) shouldBe ""
            }
        }

        expect("result has the specified length") {
            repeat(10) {
                punctuationMarks("!WW=", -100) shouldHaveLength 0
                punctuationMarks("!WW=", -1) shouldHaveLength 0
                punctuationMarks("!WW=", 0) shouldHaveLength 0
                punctuationMarks("!WW=", 1) shouldHaveLength 1
                punctuationMarks("!WW=", 100) shouldHaveLength 100
            }
        }
    }

    context("wordsWithPunctuationMarks") {

        expect("randomly prefix or append randomly selected punctuation marks to the given words") {
            repeat(10) {
                wordsWithPunctuationMarks(setOf("grape", "apple", "pear"), "!;") shouldContainAnyOf setOf(
                    "!grape", "!apple", "!pear",
                    "grape!", "apple!", "pear!",
                    ";grape", ";apple", ";pear",
                    "grape;", "apple;", "pear;")
            }
        }

        expect("with WW string, randomly prefix (left) or append (right) randomly selected punctuation marks to the given words") {
            repeat(10) {
                wordsWithPunctuationMarks(setOf("grape", "apple", "pear"), "!WW=") shouldContainAnyOf setOf(
                    "grape=", "apple=", "pear=",
                    "!grape", "!apple", "!pear")
            }
        }

        expect("with WW string, always build pairs when symbols are pair-able") {
            repeat(10) {
                wordsWithPunctuationMarks(setOf("grape", "apple", "pear"), "{[WW]}") shouldContainAnyOf setOf(
                    "{grape}", "{apple}", "{pear}",
                    "[grape]", "[apple]", "[pear]")
            }
        }

        expect("input words will be output words when an empty WW part is passed") {
            repeat(10) {
                wordsWithPunctuationMarks(setOf("grape", "apple", "pear"), "WW") shouldContainExactlyInAnyOrder
                        setOf("grape", "apple", "pear")
            }
        }

        expect("result is empty when punctuation marks are empty") {
            repeat(10) {
                wordsWithPunctuationMarks(setOf("grape", "apple", "pear"), "") shouldBe emptyList()
            }
        }

        expect("result is empty when word list is empty") {
            repeat(10) {
                wordsWithPunctuationMarks(emptySet(), "!WW=") shouldBe emptyList()
            }
        }

        expect("pair-able and non-pair-able punctuation marks can be mixed") {
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
})