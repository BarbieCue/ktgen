package org.example

import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldContainAnyOf
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll


class OperationKtTest : ConcurrentExpectSpec({

    context("String extensions") {

        context("shuffle") {

            expect("shuffle the input symbols") {
                "abcdefghijk".shuffle() shouldHaveLength 11
                "abcdefghijk".shuffle() shouldNotBe "abcdefghijk"
                "abcdefghijk".shuffle() shouldMatch "[abcdefghijk]{11}".toRegex()
            }

            expect("also shuffle input whitespaces") {
                "  abc  ".shuffle() shouldMatch "[ abc]+".toRegex()
            }

            expect("empty input leads to empty output") {
                "".shuffle() shouldBe ""
            }

            expect("result is trimmed") {
                "  abc  ".shuffle() shouldNotStartWith "\\s"
                "  abc  ".shuffle() shouldNotEndWith "\\s"
            }
        }

        context("repeat") {

            expect("repeat the input symbols") {
                "abc".repeat(10) shouldBe "abcabcabca"
            }

            expect("also repeat input whitespaces") {
                "  abc  ".repeat(10) shouldBe "abc    a"
            }

            expect("empty input leads to empty output") {
                "".repeat(10) shouldBe ""
            }

            expect("result is trimmed") {
                "  a  ".repeat(10) shouldNotStartWith "\\s"
                "  a  ".repeat(10) shouldNotEndWith "\\s"
            }

            expect("length range test") {
                "abc".repeat(-100) shouldHaveLength 0
                "abc".repeat(-1) shouldHaveLength 0
                "abc".repeat(0) shouldHaveLength 0
                "abc".repeat(1) shouldHaveLength 1
                "abc".repeat(100) shouldHaveLength 100
            }
        }

        context("segment") {

            expect("segment the input string") {
                "abcdefghi".segment(2) shouldHaveLength 13
                "abcdefghi".segment(2) shouldBe "ab cd ef gh i"
            }

            expect("also treat whitespaces as normal symbols") {
                "a b c".segment(2) shouldBe "a  b  c"
            }

            expect("empty input leads to empty output") {
                "".segment(2) shouldBe ""
            }

            expect("result is trimmed") {
                "  abc  ".segment(10) shouldNotStartWith "\\s"
                "  abc  ".segment(10) shouldNotEndWith "\\s"
            }

            expect("length range test") {
                "abcdefghi".segment(-100) shouldHaveLength 0
                "abcdefghi".segment(-1) shouldHaveLength 0
                "abcdefghi".segment(0) shouldHaveLength 0
                "abcdefghi".segment(1) shouldHaveLength 17  // a b c d e f g h i
                "abcdefghi".segment(3) shouldHaveLength 11  // abc def ghi
                "abcdefghi".segment(100) shouldHaveLength 9 // abcdefghi
            }
        }

        context("splitWWLeftRight") {

            expect("splits the WW part into left and right") {
                "[{WW}]".splitWWLeftRight() shouldBe Pair("[{", "}]")
                "[{WW".splitWWLeftRight() shouldBe Pair("[{", "")
                "WW".splitWWLeftRight() shouldBe Pair("", "")
            }

            expect("empty input leads to empty output") {
                "".splitWWLeftRight() shouldBe Pair("", "")
            }

            expect("left and right will contain the original input, when WW is missing") {
                "XXX".splitWWLeftRight() shouldBe Pair("XXX", "XXX")
                "a".splitWWLeftRight() shouldBe Pair("a", "a")
                "aW".splitWWLeftRight() shouldBe Pair("aW", "aW")
                "[{}]".splitWWLeftRight() shouldBe Pair("[{}]", "[{}]")
                "[{".splitWWLeftRight() shouldBe Pair("[{", "[{")
            }
        }

        context("symbolsCount") {

            expect("counts non-whitespace characters") {
                StringBuilder(". a b c ] 1").symbolsCount() shouldBe 6
                StringBuilder("  \t  \n ").symbolsCount() shouldBe 0
                StringBuilder().symbolsCount() shouldBe 0
            }
        }

        context("symbolsCount") {

            expect("counts non-whitespace characters") {
                ". a b c ] 1".symbolsCount() shouldBe 6
                "  \t  \n ".symbolsCount() shouldBe 0
                "".symbolsCount() shouldBe 0
            }
        }

        context("normalizeWhitespaces") {

            expect("replaces two or more chained whitespaces with a single whitespace") {
                "a  \t \tb   \n c".normalizeWhitespaces() shouldBe "a b c"
            }

            expect("input is output when the input does not contain any whitespaces") {
                "abc".normalizeWhitespaces() shouldBe "abc"
            }

            expect("empty input leads to empty output") {
                "".normalizeWhitespaces() shouldBe ""
            }
        }

        context("toTextBlock") {

            expect("result contains exactly symbols-total non-whitespace characters") {
                "abcdef abc abc abcdef abc".toTextBlock(20, 5).count { !it.isWhitespace() } shouldBe 20
            }

            expect("result contains whitespace characters") {
                "abcdef abc abc abcdef abc".toTextBlock(20, 5).count { it.isWhitespace() } shouldBe 4
            }

            expect("cut symbols from the end, when source string is larger than symbols-total") {
                "12345678901234567890" // 20 chars long
                    .toTextBlock(18, 5) shouldEndWith "8"
            }

            expect("the result line length is about line-length, words are not split by line breaks") {
                val result = "abcdef abc abc abcdef abc".toTextBlock(20, 5)
                val lines = result.split("\n")
                lines[0] shouldHaveLength 6
                lines[1] shouldHaveLength 3
                lines[2] shouldHaveLength 3
                lines[3] shouldHaveLength 6
                lines[4] shouldHaveLength 2
                result shouldBe """
                    abcdef
                    abc
                    abc
                    abcdef
                    ab
                """.trimIndent()
            }

            expect("split result into lines of line-length when source string is a single word longer than line length") {
                val lines = "abcdefabcabcabcdefabca".toTextBlock(20, 5).split("\n".toRegex())
                lines[0] shouldHaveLength 5
                lines[1] shouldHaveLength 5
                lines[2] shouldHaveLength 5
                lines[3] shouldHaveLength 5
            }

            expect("normalize multiple chained whitespace characters into a single one") {
                "abc    abc  \t abc   abc abc abc    abc".toTextBlock(20, 5) shouldBe """
                    abc
                    abc
                    abc
                    abc
                    abc
                    abc
                    ab
                    """.trimIndent()
            }

            expect("an input string length smaller than symbols-total leads to result having input string length") {
                "abc def ghi".toTextBlock(20, 5) shouldBe """
                    abc
                    def
                    ghi
                """.trimIndent()
            }

            expect("input string length is smaller than line-length leads to result having input string length") {
                "ac".toTextBlock(20, 5) shouldBe "ac"
            }

            expect("return empty string when input string is empty") {
                "".toTextBlock(20, 5) shouldBe ""
            }

            expect("return empty string when symbols-total is zero or negative") {
                "abc abc abc".toTextBlock(0, 10) shouldBe ""
                "abc abc abc".toTextBlock(-1, 10) shouldBe ""
            }

            expect("return empty string when line-length is zero or negative") {
                "abc abc abc".toTextBlock(10, 0) shouldBe ""
                "abc abc abc".toTextBlock(10, -1) shouldBe ""
            }
        }

        context("substringAtNearestWhitespace") {

            expect("return substring from 0 to the last word before or after desired-length") {
                "hi, my name is luka, i live on the second floor"
                    .substringAtNearestWhitespace(10) shouldBe "hi, my name"
            }

            expect("empty input leads to empty output") {
                "".substringAtNearestWhitespace(10) shouldBe ""
            }

            expect("desired-length range test") {
                "hi, my name is luka".substringAtNearestWhitespace(-10) shouldBe ""
                "hi, my name is luka".substringAtNearestWhitespace(-1) shouldBe ""
                "hi, my name is luka".substringAtNearestWhitespace(0) shouldBe ""
                "hi, my name is luka".substringAtNearestWhitespace(1) shouldBe "h"
                "hi, my name is luka".substringAtNearestWhitespace(2) shouldBe "hi,"
                "hi, my name is luka".substringAtNearestWhitespace(3) shouldBe "hi,"
                "hi, my name is luka".substringAtNearestWhitespace(10) shouldBe "hi, my name"
            }

            expect("return input string when desired-length is larger than the length of the input string") {
                "hi, my name is luka".substringAtNearestWhitespace(20) shouldBe "hi, my name is luka"
            }

            expect("return a substring from 0 up to desired-length when the input string does not contain any whitespaces") {
                "hi,mynameisluka".substringAtNearestWhitespace(1) shouldBe "h"
                "hi,mynameisluka".substringAtNearestWhitespace(5) shouldBe "hi,my"
                "hi,mynameisluka".substringAtNearestWhitespace(10) shouldBe "hi,mynamei"
                "hi,mynameisluka".substringAtNearestWhitespace(20) shouldBe "hi,mynameisluka"
            }
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

    context("Collection<String> extensions") {

        context("joinRepeat") {

            expect("result is a single line") {
                val words = setOf(
                    "building", "metrics", "perimeter",
                    "expensive", "a", "b", "c", "d")

                checkAll(20, Arb.int(10, 10000)) {
                    words.joinRepeat(it) shouldNotContain "\n"
                }
            }

            expect("result consists of joined words, separated by whitespaces") {
                val words = setOf(
                    "building", "metrics", "perimeter",
                    "expensive", "a", "b", "c", "d")

                words.joinRepeat(33) shouldBe "building metrics perimeter expensive"
            }

            expect("result contains the exact amount of non-whitespace symbols") {
                val words = setOf(
                    "building", "metrics", "perimeter",
                    "expensive", "a", "b", "c", "d")

                words.joinRepeat(30).count { !it.isWhitespace() } shouldBe 30
                words.joinRepeat(60).count { !it.isWhitespace() } shouldBe 60
            }

            expect("empty input leads to empty output") {
                emptySet<String>().joinRepeat(20) shouldBe ""
            }

            expect("result is empty when there is no word having a length <= sum-non-whitespace-chars") {
                setOf("aaa", "bbb", "ccc").joinRepeat(1) shouldBe ""
                setOf("a  ").joinRepeat(1) shouldBe ""
            }

            expect("result is empty when all items of the input collection are empty") {
                setOf("", "", "").joinRepeat(1) shouldBe ""
            }

            expect("ignore too long words") {
                setOf("too_long", "ok").joinRepeat(2) shouldBe "ok"
                setOf("too_long", "ok").joinRepeat(4) shouldBe "ok ok"
            }

            expect("find a word of necessary length out of order, if necessary and exists") {
                setOf("ab", "cde", "fgh").joinRepeat(7) shouldBe "ab cde ab"
                // "fgh" would exceed the length of 7, thus take "ab"
            }

            expect("cut last word if cannot find a word of necessary length") {
                setOf("abc", "def").joinRepeat(5) shouldBe "abc de"
                // "def" would exceed the length of 7 and no word of length 2 can be found, thus cut "def" to "de"
            }

            expect("result is trimmed") {
                setOf("aaa  ", "  bbb", "  ccc  ").joinRepeat(9) shouldNotStartWith "\\s"
                setOf("aaa  ", "  bbb", "  ccc  ").joinRepeat(9) shouldNotEndWith "\\s"
            }

            expect("each word is trimmed") {
                setOf("aaa  ", "  bbb", "  ccc  ").joinRepeat(9) shouldBe "aaa bbb ccc"
            }

            expect("sum-non-whitespace-chars range test") {
                val words = setOf(
                    "building", "metrics", "perimeter", "expensive", "a", "b", "c",
                    "d", "engine", "of", "house", "train", "car", "bird", "door")

                words.joinRepeat(-10) shouldHaveLength 0
                words.joinRepeat(-1) shouldHaveLength 0
                words.joinRepeat(0) shouldHaveLength 0
                words.joinRepeat(1).count { !it.isWhitespace() } shouldBe 1
                words.joinRepeat(10).filter { !it.isWhitespace() } shouldHaveMaxLength 10
            }
        }

        context("prefixOrAppendPunctuationMarks") {

            expect("randomly prefix or append randomly selected punctuation marks to the given words") {
                repeat(10) {
                    setOf("grape", "apple", "pear").prefixOrAppendPunctuationMarks("!;") shouldContainAnyOf setOf(
                        "!grape", "!apple", "!pear",
                        "grape!", "apple!", "pear!",
                        ";grape", ";apple", ";pear",
                        "grape;", "apple;", "pear;")
                }
            }

            expect("with WW string, randomly prefix (left) or append (right) randomly selected punctuation marks to the given words") {
                repeat(10) {
                    setOf("grape", "apple", "pear").prefixOrAppendPunctuationMarks("!WW=") shouldContainAnyOf setOf(
                        "grape=", "apple=", "pear=",
                        "!grape", "!apple", "!pear")
                }
            }

            expect("with WW string, always build pairs when symbols are pair-able") {
                repeat(10) {
                    setOf("grape", "apple", "pear").prefixOrAppendPunctuationMarks("{[WW]}") shouldContainAnyOf setOf(
                        "{grape}", "{apple}", "{pear}",
                        "[grape]", "[apple]", "[pear]")
                }
            }

            expect("input words will be output words when an empty WW part is passed") {
                repeat(10) {
                    setOf("grape", "apple", "pear").prefixOrAppendPunctuationMarks("WW") shouldContainExactlyInAnyOrder
                            setOf("grape", "apple", "pear")
                }
            }

            expect("result is empty when punctuation marks are empty") {
                repeat(10) {
                    setOf("grape", "apple", "pear").prefixOrAppendPunctuationMarks("") shouldBe emptyList()
                }
            }

            expect("result is empty when word list is empty") {
                repeat(10) {
                    emptySet<String>().prefixOrAppendPunctuationMarks("!WW=") shouldBe emptyList()
                }
            }

            expect("pair-able and non-pair-able punctuation marks can be mixed") {
                val words = setOf("grape", "apple", "pear")
                val punctuationMarks = "!?(WW)="
                repeat(10) {
                    words.prefixOrAppendPunctuationMarks(punctuationMarks) shouldContainAnyOf setOf(
                        "!grape", "!apple", "!pear",
                        "?grape", "?apple", "?pear",
                        "grape=", "apple=", "pear=",
                        "(grape)", "(apple)", "(pear)")
                }
            }
        }
    }
})