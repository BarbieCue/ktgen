package org.example

import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.stringPattern

class LessonKtTest : ConcurrentExpectSpec({

    context("StringBuilder extensions") {

        context("newCharacters") {

            expect("empty result when input chars are already contained") {
                val history = "abcd"
                val sb = StringBuilder(history)
                sb.newCharacters("a") shouldBe ""
                sb.newCharacters("ab") shouldBe ""
                sb.newCharacters("cd") shouldBe ""
                sb.newCharacters("abcd") shouldBe ""
                sb.newCharacters("dcab") shouldBe ""
            }

            expect("new symbols will be added and returned") {
                val sbEmpty = StringBuilder("")
                sbEmpty.newCharacters("x") shouldBe "x"
                sbEmpty.toString() shouldContain "x"
                val sbWithHistory = StringBuilder("abcd")
                sbWithHistory.newCharacters("x") shouldBe "x"
                sbWithHistory.toString() shouldContain "abcdx"
            }

            expect("empty input leads to empty output") {
                val history = "abcd"
                val sb = StringBuilder(history)
                sb.newCharacters("") shouldBe ""
            }
        }

        context("symbolsCount") {

            expect("counts non-whitespace characters") {
                StringBuilder(". a b c ] 1").symbolsCount() shouldBe 6
                StringBuilder("  \t  \n ").symbolsCount() shouldBe 0
                StringBuilder().symbolsCount() shouldBe 0
            }
        }
    }

    context("lessonWords") {

        expect("first lesson's words consist of the first lessons symbols only") {
            val dict = setOf("ab", "abrc", "abrce", "abrcel")
            val charsHistory = "ab"
            val lessonSymbols = "ab"
            dict.lessonWords(charsHistory, lessonSymbols) shouldBe setOf("ab")
        }

        expect("words from lessons 2..n can contain symbols from all last lesson, but definitely contain at least one of the current lesson's symbols") {
            val dict = setOf("ab", "abrc", "abrce", "abrcel")
            val charsHistory = "abrcel"
            val lessonSymbols = "el"
            dict.lessonWords(charsHistory, lessonSymbols) shouldBe setOf("abrce", "abrcel")
        }

        expect("rotate the dict randomly but always preserve the word order") {
            val dict = setOf("a", "b", "c", "d", "e")
            val charsHistory = "abcde"
            val lessonSymbols = "abcde"

            val words = dict.lessonWords(charsHistory, lessonSymbols)
            when(words.first()) {
                "a"  -> words shouldBe listOf("a", "b", "c", "d", "e")
                "b"  -> words shouldBe listOf("b", "c", "d", "e", "a")
                "c"  -> words shouldBe listOf("c", "d", "e", "a", "b")
                "d"  -> words shouldBe listOf("d", "e", "a", "b", "c")
                "e"  -> words shouldBe listOf("e", "a", "b", "c", "d")
                else -> throw Exception("ouch!")
            }
        }

        expect("result is empty collection when input dict is empty") {
            val charsHistory = "abrcel"
            val lessonSymbols = "el"
            emptySet<String>().lessonWords(charsHistory, lessonSymbols) shouldBe emptyList()
        }

        expect("result is empty collection when the history is empty") {
            val dict = setOf("ab", "abrc", "abrcel")
            val charsHistory = ""
            val lessonSymbols = "el"
            dict.lessonWords(charsHistory, lessonSymbols) shouldBe emptyList()
        }

        expect("if lesson symbols contain non-letters, ignore them and take words from history based on letters") {
            val dict = setOf("ad", "b", "tt", "a", "cd", "bd", "xx", "d")
            val charsHistory = "abc_[]d().;"
            val lessonSymbols = "_[]d().;"
            dict.lessonWords(charsHistory, lessonSymbols) shouldContainExactlyInAnyOrder listOf("ad", "cd", "bd", "d")
        }

        expect("if lesson symbols consists of non-letters, ignore them and take words from history") {
            val dict = setOf("ab", "b", "tt", "a", "ba", "xx", "bab")
            val charsHistory = "ab_[]().;"
            val lessonSymbols = "_[]().;"
            dict.lessonWords(charsHistory, lessonSymbols) shouldContainExactlyInAnyOrder listOf("ab", "b", "a", "ba", "bab")
        }

        expect("if lesson symbols is a letter group, take words from history containing the group") {
            val dict = setOf("apple", "letter", "lesson", "china", "brain")
            val charsHistory = "ialetrsonch"
            val lessonSymbols = "[tt]"
            dict.lessonWords(charsHistory, lessonSymbols) shouldContainExactlyInAnyOrder listOf("letter")
        }
    }

    context("wwRegex") {

        expect("matches WW parts") {
            "WW,." shouldMatch wwRegex
            "+WW" shouldMatch wwRegex
            "[WW]" shouldMatch wwRegex
            "WW" shouldMatch wwRegex
        }

        expect("should not match anything else") {
            "" shouldNotMatch wwRegex.pattern
            "W" shouldNotMatch wwRegex.pattern
            ",." shouldNotMatch wwRegex.pattern
            "abc" shouldNotMatch wwRegex.pattern
            "1" shouldNotMatch wwRegex.pattern
        }
    }

    context("letterGroupRegex") {

        expect("match letter groups") {
            "[sch]" shouldMatch letterGroupRegex
        }

        expect("not match non-letter groups") {
            "[sch]a" shouldNotMatch letterGroupRegex.pattern
            "a[sch]" shouldNotMatch letterGroupRegex.pattern
            "[]" shouldNotMatch letterGroupRegex.pattern
            "abc" shouldNotMatch letterGroupRegex.pattern
            "1" shouldNotMatch letterGroupRegex.pattern
            "" shouldNotMatch letterGroupRegex.pattern
        }
    }

    context("lower letters regex") {

        expect("match lowercase letters") {
            "abcdefghijklmnopqrstuvwxyzäöüß" shouldMatch lowerLetters
            "aaa" shouldMatch lowerLetters
        }

        expect("not match digits") {
            "0123456789" shouldNotMatch lowerLetters.pattern
        }

        expect("not match punctuation marks") {
            "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~" shouldNotMatch lowerLetters.pattern
        }
    }

    context("upper letters regex") {

        expect("match uppercase letters") {
            "ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜẞ" shouldMatch upperLetters
            "AAA" shouldMatch upperLetters
        }

        expect("not match digits") {
            "0123456789" shouldNotMatch upperLetters.pattern
        }

        expect("not match punctuation marks") {
            "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~" shouldNotMatch upperLetters.pattern
        }
    }

    context("letters regex") {

        expect("match lowercase and uppercase letters") {
            "abcdefghijklmnopqrstuvwxyzäöüßABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜẞ" shouldMatch lettersRegex
            "aaa" shouldMatch lettersRegex
            "AAA" shouldMatch lettersRegex
        }

        expect("not match digits") {
            "0123456789" shouldNotMatch lettersRegex.pattern
        }

        expect("not match punctuation marks") {
            "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~" shouldNotMatch lettersRegex.pattern
        }
    }

    context("digits regex") {

        expect("match digits") {
            "123" shouldMatch digitRegex
        }

        expect("does not match non-digits") {
            "123abc" shouldNotMatch digitRegex.pattern
            "abc" shouldNotMatch digitRegex.pattern
            "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~" shouldNotMatch digitRegex.pattern
        }
    }

    context("punctuation regex") {

        expect("match punctuation marks") {
            "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~" shouldMatch punctuationRegex
        }

        expect("does not match non-punctuation marks") {
            "123" shouldNotMatch punctuationRegex.pattern
            "abc" shouldNotMatch punctuationRegex.pattern
        }
    }

    context("symbolsPerGenerator") {

        expect("table test") {
            table(
                headers("symbols-per-lesson", "number-of-generators", "symbolsPerGenerator"),
                row(-100, 100, 0),
                row(-1, 100, 0),
                row(0, 100, 0),
                row(1, 100, 1),
                row(100, 100, 1),

                row(100, -100, 0),
                row(100, -1, 0),
                row(100, 0, 0),
                row(100, 1, 100),

                row(0, 0, 0),
                row(-1, -1, 0),
                row(-10, -10, 0),
                row(-100, -100, 0),
            ).forAll { a, b, result ->
                symbolsPerGenerator(a, b) shouldBe result
            }
        }
    }


    context("invokeConcat") {

        expect("returns concatenated generators results as single line, single whitespace separated") {
            fun repeatA(n: Int) = "a".repeat(n)
            fun repeatB(n: Int) = "b".repeat(n)
            fun repeatC(n: Int) = "c".repeat(n)
            val generators = listOf(::repeatA, ::repeatB, ::repeatC)
            val result = invokeConcat(10, generators)
            result shouldBe "aaa bbb ccc a"
        }

        expect("on single generator return the generators result with length of symbols-per-lesson") {
            fun repeatA(n: Int) = "a".repeat(n)
            val generators = listOf(::repeatA)
            invokeConcat(10, generators) shouldBe "aaaaaaaaaa"
        }

        expect("on many generators return all generators results separated by whitespace") {
            fun repeatA(n: Int) = "a".repeat(n)
            fun repeatB(n: Int) = "b".repeat(n)
            val generators = listOf(::repeatA, ::repeatB)
            invokeConcat(10, generators) shouldBe "aaaaa bbbbb"
        }

        expect("on many generators each generators result has length of symbols-per-lesson divided by number-of-generators") {
            fun repeatA(n: Int) = "a".repeat(n)
            fun repeatB(n: Int) = "b".repeat(n)
            fun repeatC(n: Int) = "c".repeat(n)
            val generators = listOf(::repeatA, ::repeatB, ::repeatC)
            val lines = invokeConcat(9, generators).split(" ")
            lines[0] shouldHaveLength 3 // aaa
            lines[1] shouldHaveLength 3 // bbb
            lines[2] shouldHaveLength 3 // ccc
        }

        expect("re-invoke generators if necessary to get as many symbols as symbols-per-lesson") {
            fun repeatA(n: Int) = "a".repeat(n)
            fun repeatB(n: Int) = "b".repeat(n)
            fun repeatC(n: Int) = "c".repeat(n)
            val generators = listOf(::repeatA, ::repeatB, ::repeatC)
            val lines = invokeConcat(10, generators).split(" ")

            lines[0] shouldHaveLength 3
            lines[1] shouldHaveLength 3
            lines[2] shouldHaveLength 3
            lines[3] shouldHaveLength 1

            lines[0] shouldBe "aaa"
            lines[1] shouldBe "bbb"
            lines[2] shouldBe "ccc"
            lines[3] shouldBe "a" // re-invoked first generator
        }

        expect("return empty string when generators have empty output") {
            fun repeatA(n: Int) = "a".repeat(n)
            fun repeatB(n: Int) = "b".repeat(n)
            fun emptyResult(n: Int) = ""
            fun repeatC(n: Int) = "c".repeat(n)
            val generators = listOf(::repeatA, ::repeatB, ::emptyResult, ::repeatC)
            invokeConcat(3, generators) shouldBe ""
        }
    }


    context("List<TextGenerator>") {

        context("invokeConcat") {

            expect("return concatenated generators results as single line, whitespace separated") {
                fun repeatA(n: Int) = "a".repeat(n)
                fun repeatB(n: Int) = "b".repeat(n)
                fun repeatC(n: Int) = "c".repeat(n)
                val generators = listOf(::repeatA, ::repeatB, ::repeatC)
                val result = generators.invokeConcat(3, 9)
                result shouldBe "aaa bbb ccc"
            }

            expect("result contains exactly the total number of non-whitespace characters (symbols-total)") {
                fun repeatA(n: Int) = "a".repeat(n)
                fun repeatB(n: Int) = "b".repeat(n)
                fun repeatC(n: Int) = "c".repeat(n)
                val generators = listOf(::repeatA, ::repeatB, ::repeatC)
                val result = generators.invokeConcat(3, 200)
                result.count { !it.isWhitespace() } shouldBe 200
            }

            expect("re-invoke generators from the beginning if necessary, to reach the total number of symbols") {
                fun repeatA(n: Int) = "a".repeat(n)
                fun repeatB(n: Int) = "b".repeat(n)
                fun repeatC(n: Int) = "c".repeat(n)
                val generators = listOf(::repeatA, ::repeatB, ::repeatC)
                val segments = generators.invokeConcat(3, 23).split(" ")

                segments shouldHaveSize 8

                segments[0] shouldHaveLength 3
                segments[1] shouldHaveLength 3
                segments[2] shouldHaveLength 3
                segments[3] shouldHaveLength 3
                segments[4] shouldHaveLength 3
                segments[5] shouldHaveLength 3
                segments[6] shouldHaveLength 3
                segments[7] shouldHaveLength 2

                segments[0] shouldBe "aaa"
                segments[1] shouldBe "bbb"
                segments[2] shouldBe "ccc"
                segments[3] shouldBe "aaa"
                segments[4] shouldBe "bbb"
                segments[5] shouldBe "ccc"
                segments[6] shouldBe "aaa"
                segments[7] shouldBe "bb"
            }

            expect("return empty string when there it at least one generator with empty output") {
                fun repeatA(n: Int) = "a".repeat(n)
                fun repeatB(n: Int) = "b".repeat(n)
                fun emptyResult(n: Int) = ""
                fun repeatC(n: Int) = "c".repeat(n)
                val generators = listOf(::repeatA, ::repeatB, ::emptyResult, ::repeatC)
                generators.invokeConcat(3, 10) shouldBe ""
            }

            expect("return empty string when symbols-per-generator is <= 0") {
                fun repeatA(n: Int) = "a".repeat(n)
                fun repeatB(n: Int) = "b".repeat(n)
                fun repeatC(n: Int) = "c".repeat(n)
                val generators = listOf(::repeatA, ::repeatB, ::repeatC)
                generators.invokeConcat(0, 10) shouldBe ""
                generators.invokeConcat(-1, 10) shouldBe ""
            }
        }
    }

    context("String extensions") {

        context("unpack") {

            expect("unpack pure WW strings") {
                "{WW}".unpack() shouldBe "{}"
            }

            expect("unpack WW substrings in place") {
                "abc{WW}123".unpack() shouldBe "abc{}123"
            }

            expect("unpack pure letter groups") {
                "[sch]".unpack() shouldBe "sch"
            }

            expect("unpack letter group substrings in place") {
                "abc[sch]123".unpack() shouldBe "abcsch123"
            }

            expect("return original input, if it is not packed") {
                "apple pear".unpack() shouldBe "apple pear"
            }

            expect("empty input leads to empty output") {
                "".unpack() shouldBe ""
            }
        }

        context("ww") {

            expect("return the WW part") {
                "WW".ww() shouldBe "WW"
                "(WW)".ww() shouldBe "(WW)"
                "WW)=/\\".ww() shouldBe "WW)=/\\"
                "_}*?/{(WW".ww() shouldBe "_}*?/{(WW"
            }

            expect("return empty string when there is no WW part") {
                "W".ww() shouldBe ""
                "abc".ww() shouldBe ""
            }

            expect("empty input leads to empty output") {
                "".ww() shouldBe ""
            }

            expect("WW must be capital letters") {
                "{WW}".ww() shouldBe "{WW}"
                "{ww}".ww() shouldBe ""
                "{Ww}".ww() shouldBe ""
                "{wW}".ww() shouldBe ""
            }

            expect("return the first WW part only") {
                "(WW)(WW)".ww() shouldBe "(WW)("
                "(WW)';[{(WW)}]".ww() shouldBe "(WW)';[{("
            }

            expect("ignore everything else") {
                "abcABC(WW)".ww() shouldBe "(WW)"
                "abcABC(WW)1abcABC".ww() shouldBe "(WW)"
                "abc:ABC(WW)1a,bc_ABC".ww() shouldBe "(WW)"
                "1WW1".ww() shouldBe "WW"
                "1abcWW".ww() shouldBe "WW"
                "()=abcWW".ww() shouldBe "WW"
            }
        }

        context("wwUnpack") {

            expect("remove the WW from WW strings") {
                "WW".unpackWW() shouldBe ""
                "(WW)".unpackWW() shouldBe "()"
                "WW)=/\\".unpackWW() shouldBe ")=/\\"
                "_}*?/{(WW".unpackWW() shouldBe "_}*?/{("
            }

            expect("return input string when it contains no WW substring") {
                "1234".unpackWW() shouldBe "1234"
            }

            expect("empty input leads to empty output") {
                "".unpackWW() shouldBe ""
            }
        }

        context("letterGroup") {

            expect("return group of letters inclusive square brackets") {
                "[sch]".letterGroup() shouldBe "[sch]"
            }

            expect("return the first group only") {
                "[sch][ch][ss][tt]".letterGroup() shouldBe "[sch]"
                "[tt][ch][ss]".letterGroup() shouldBe "[tt]"
            }

            expect("return empty string on empty group") {
                "[]".letterGroup() shouldBe ""
            }

            expect("empty input leads to empty output") {
                "".letterGroup() shouldBe ""
            }

            expect("return empty string when group consists of non-letters") {
                "[123]".letterGroup() shouldBe ""
                "[%';]".letterGroup() shouldBe ""
            }

            expect("return empty string when group contains non-letters") {
                "[sch12]".letterGroup() shouldBe ""
                "[sch%';]".letterGroup() shouldBe ""
            }

            expect("ignore surrounding square brackets") {
                "[[sch]]".letterGroup() shouldBe "[sch]"
                "[[[sch]]]".letterGroup() shouldBe "[sch]"
            }

            expect("WW part can not be a letter group") {
                "[WW]".letterGroup() shouldBe ""
                "{[WW]}".letterGroup() shouldBe ""
            }
        }

        context("letterGroupUnpack") {

            expect("return the letters of the group") {
                "[sch]".unpackLetterGroup() shouldBe "sch"
            }

            expect("unpack a letter group in place") {
                "abc[sch]123".unpackLetterGroup() shouldBe "abcsch123"
            }

            expect("handle the first group only") {
                "[sch][ch][ss][tt]".unpackLetterGroup() shouldBe "sch[ch][ss][tt]"
                "[tt][ch][ss]".unpackLetterGroup() shouldBe "tt[ch][ss]"
            }

            expect("an empty square bracket pair is not treated as letter group") {
                "[]".unpackLetterGroup() shouldBe "[]"
            }

            expect("empty input leads to empty output") {
                "".unpackLetterGroup() shouldBe ""
            }

            expect("return input string when it is not a letter group") {
                "[123]".unpackLetterGroup() shouldBe "[123]"
                "[%';]".unpackLetterGroup() shouldBe "[%';]"
                "abc".unpackLetterGroup() shouldBe "abc"
                "123".unpackLetterGroup() shouldBe "123"
                ",.".unpackLetterGroup() shouldBe ",."
            }

            expect("WW part can not be a letter group") {
                "[WW]".unpackLetterGroup() shouldBe ""
                "{[WW]}".unpackLetterGroup() shouldBe ""
            }
        }

        context("unconditionalPunctuation") {

            expect("return punctuation marks which are not related to WW") {
                "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~".punctuationMarks() shouldBe "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
                "abc".punctuationMarks() shouldBe ""
                "abc:".punctuationMarks() shouldBe ":"
                "123".punctuationMarks() shouldBe ""
                "123:".punctuationMarks() shouldBe ":"
                "1,2(WW)".punctuationMarks() shouldBe ","
                "()=abc[WW]-".punctuationMarks() shouldBe "()="
            }

            expect("ignore WW part") {
                "abcABC(WW)".punctuationMarks() shouldBe ""
                "abcABCWW".punctuationMarks() shouldBe ""
                "1WW1".punctuationMarks() shouldBe ""
                "1abcWW".punctuationMarks() shouldBe ""
            }
        }

        context("letters") {

            expect("return lowercase and uppercase letters") {
                "W".letters() shouldBe "W"
                "üäöß".letters() shouldBe "üäöß"
                "abc".letters() shouldBe "abc"
                "a,bc()=;deXXf".letters() shouldBe "abcdeXXf"
                "abc123deXXf".letters() shouldBe "abcdeXXf"
                "".letters() shouldBe ""
            }

            expect("ignore digits") {
                "123\"".letters() shouldBe ""
            }

            expect("ignore punctuation marks") {
                ",.';%{}[]()".letters() shouldBe ""
            }

            expect("ignore WW part") {
                "WW".letters() shouldBe ""
                "(WW)".letters() shouldBe ""
                "abc(WW)abc(WW)';abc".letters() shouldBe "abcabcabc"
                "abc(WW)=;def".letters() shouldBe "abcdef"
            }

            expect("ignore letter groups") {
                "abc[sch]def".letters() shouldBe "abcdef"
                "abcdef[sch]".letters() shouldBe "abcdef"
                "[sch]abcdef".letters() shouldBe "abcdef"
                "[sch]abcdef[tt]".letters() shouldBe "abcdef"
                "abc[]def".letters() shouldBe "abcdef"
                "abcdef[]".letters() shouldBe "abcdef"
                "[]abcdef".letters() shouldBe "abcdef"
            }

            expect("return empty string on empty input") {
                "".letters() shouldBe ""
            }
        }

        context("containsLetters") {

            expect("false when string is a letter group") {
                "[abc]".containsLetters() shouldBe false
            }

            expect("false when string is a ww string") {
                "[WW]".containsLetters() shouldBe false
            }

            expect("true when string contains letters") {
                "abc".containsLetters() shouldBe true
                "abc123".containsLetters() shouldBe true
            }

            expect("false when string does not contain letters") {
                "123".containsLetters() shouldBe false
                ",.".containsLetters() shouldBe false
            }

            expect("false on empty string") {
                "".containsLetters() shouldBe false
            }
        }

        context("areDigits") {

            expect("true when string consists only of digits") {
                "0123456789".areDigits() shouldBe true
            }

            expect("false when string does not consists only of digits") {
                "abc".areDigits() shouldBe false
                "abc123".areDigits() shouldBe false
                ".,".areDigits() shouldBe false
                "1,3".areDigits() shouldBe false
                "1.4".areDigits() shouldBe false
            }
        }

        context("isLetterGroup") {

            expect("true when string is a letter group") {
                "[sch]".isLetterGroup() shouldBe true
            }

            expect("false when string is not a letter group") {
                "abc".isLetterGroup() shouldBe false
                "abc123".isLetterGroup() shouldBe false
                ".,".isLetterGroup() shouldBe false
                "1".isLetterGroup() shouldBe false
                "1,3".isLetterGroup() shouldBe false
            }

            expect("false for WW strings") {
                "[WW]".isLetterGroup() shouldBe false
            }

            expect("false on empty string") {
                "".isLetterGroup() shouldBe false
            }
        }

        context("toTextBlock") {

            expect("result contains exactly symbols-total non-whitespace characters") {
                "abcdef abc abc abcdef abc".toTextBlock(20, 5).count { !it.isWhitespace() } shouldBe 20
            }

            expect("the result line length is about line-length, words are not broken by line breaks") {
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
    }

    context("LessonBuilder") {

        context("newLesson") {

            fun L.exampleBuildStep(): L {
                buildSteps.add { _ -> Arb.stringPattern("[A-Za-z0-9.,;@:<>]{100}\t[A-Za-z0-9.,;@:<>]{100}").next() }
                return this@exampleBuildStep
            }

            expect("A lesson counter is added to the title") {
                val lessonBuilder = LessonBuilder(10, 10, emptyList()).newLesson("ab", "ab") {
                    exampleBuildStep()
                }
                lessonBuilder.lessons[0].title shouldBe "1: ab"
                lessonBuilder.newLesson("cd", "cd") {
                    exampleBuildStep()
                }.lessons[1].title shouldBe "2: cd"
            }

            expect("only the first lesson for new symbols introduces them as 'newCharacters'") {
                val lessonBuilder = LessonBuilder(10, 10, emptyList())
                    .newLesson("my first ab lesson", "ab") {
                        exampleBuildStep()
                    }
                lessonBuilder.lessons[0].newCharacters shouldBe "ab"
                lessonBuilder.newLesson("my second ab lesson", "ab") {
                    exampleBuildStep()
                }.lessons[1].newCharacters shouldBe ""
            }

            expect("apply all build steps") {
                val lessonBuilder = LessonBuilder(30, 400, listOf("apple"))
                    .newLesson("my lesson", "abc") {
                        repeatSymbols(3)
                        shuffleSymbols(10)
                        alternateSymbols(2)
                    }
                val lessonText = lessonBuilder.lessons[0].text
                lessonText shouldContain "(abc )+".toRegex()
                lessonText shouldContain "[abc]{10}".toRegex()
                lessonText shouldContain "[a]{2}\\s[b]{2}\\s[c]{2}".toRegex()
            }

            expect("line-length range test") {
                LessonBuilder(-10, 10, emptyList())
                    .newLesson("my lesson", "abc") {
                        exampleBuildStep()
                    }.lessons shouldBe emptyList()

                LessonBuilder(-1, 10, emptyList())
                    .newLesson("my lesson", "abc") {
                        exampleBuildStep()
                    }.lessons shouldBe emptyList()

                LessonBuilder(0, 10, emptyList())
                    .newLesson("my lesson", "abc") {
                        exampleBuildStep()
                    }.lessons shouldBe emptyList()

                LessonBuilder(1, 10, emptyList())
                    .newLesson("my lesson", "abc") {
                        exampleBuildStep()
                    }.lessons[0].text.count { !it.isWhitespace() } shouldBe 10

                LessonBuilder(10, 10, emptyList())
                    .newLesson("my lesson", "abc") {
                        exampleBuildStep()
                    }.lessons[0].text shouldHaveLength 10
            }

            expect("symbols-per-lesson range test") {
                LessonBuilder(10, -100, emptyList())
                    .newLesson("my lesson", "abc") {
                        exampleBuildStep()
                    }.lessons shouldBe emptyList()

                LessonBuilder(10, -10, emptyList())
                    .newLesson("my lesson", "abc") {
                        exampleBuildStep()
                    }.lessons shouldBe emptyList()

                LessonBuilder(10, -1, emptyList())
                    .newLesson("my lesson", "abc") {
                        exampleBuildStep()
                    }.lessons shouldBe emptyList()

                LessonBuilder(10, 0, emptyList())
                    .newLesson("my lesson", "abc") {
                        exampleBuildStep()
                    }.lessons shouldBe emptyList()

                LessonBuilder(10, 1, emptyList())
                    .newLesson("my lesson", "abc") {
                        exampleBuildStep()
                    }.lessons[0].text shouldHaveLength 1

                LessonBuilder(10, 10, emptyList())
                    .newLesson("my lesson", "abc") {
                        exampleBuildStep()
                    }.lessons[0].text.count { !it.isWhitespace() } shouldBe 10

                val text100 = LessonBuilder(10, 100, emptyList())
                    .newLesson("my lesson", "abc") {
                        exampleBuildStep()
                    }.lessons[0].text
                text100.split("\n") shouldHaveAtLeastSize 10
                text100.count { !it.isWhitespace() } shouldBe 100
            }

            context("symbols-per-lesson < line-length") {

                expect("result contains symbols-per-lesson non-whitespace characters") {
                    LessonBuilder(20, 8, emptyList())
                        .newLesson("my lesson", "abc") {
                            exampleBuildStep()
                        }.lessons[0].text.count { !it.isWhitespace() } shouldBe 8
                }
            }

            context("symbols-per-lesson > line-length") {

                expect("result is multiline containing symbols-per-lesson non-whitespace characters") {
                    val text = LessonBuilder(8, 20, emptyList())
                        .newLesson("my lesson", "abc") {
                            exampleBuildStep()
                        }.lessons[0].text
                    text.split("\n") shouldHaveAtLeastSize 2
                    text.count { !it.isWhitespace() } shouldBe 20
                }
            }

            context("repeatSymbols") {

                expect("repeat the input symbols") {
                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            repeatSymbols(10)
                        }.lessons[0].text shouldBe "ababababab"
                }

                expect("create content for all symbols from the input list") {
                    val text = LessonBuilder(20, 40, emptyList())
                        .newLesson("my lesson", listOf("ab", "cd")) {
                            repeatSymbols(10)
                        }.lessons[0].text
                    text shouldContain "a"
                    text shouldContain "b"
                    text shouldContain "c"
                    text shouldContain "d"
                }

                expect("segment-length range test") {
                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            repeatSymbols(-10)
                        }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            repeatSymbols(-1)
                        }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            repeatSymbols(0)
                        }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            repeatSymbols(1)
                        }.lessons[0].text shouldBe """
                            a b a b a
                            b a b a b
                        """.trimIndent()

                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            repeatSymbols(10)
                        }.lessons[0].text shouldHaveLength 10
                }

                expect("empty symbols lead to empty result") {
                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "") {
                            repeatSymbols(10)
                        }.lessons shouldBe emptyList()
                }

                expect("result is trimmed") {
                    val text = LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            repeatSymbols(10)
                        }.lessons[0].text
                    text shouldNotStartWith "\\s"
                    text shouldNotEndWith "\\s"
                }
            }

            context("alternateSymbols") {

                expect("result consists of the input symbols in alternating fashion") {
                    LessonBuilder(14, 10, emptyList())
                        .newLesson("my lesson", "abc") {
                            alternateSymbols(2)
                        }.lessons[0].text shouldBe "aa bb cc aa bb"
                }

                expect("create content for all symbols from the input list") {
                    val text = LessonBuilder(20, 40, emptyList())
                        .newLesson("my lesson", listOf("ab", "cd")) {
                            alternateSymbols(10)
                        }.lessons[0].text
                    text shouldContain "a"
                    text shouldContain "b"
                    text shouldContain "c"
                    text shouldContain "d"
                }

                expect("empty symbols lead to empty result") {
                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "") {
                            alternateSymbols(10)
                        }.lessons shouldBe emptyList()
                }

                expect("segment-length range test") {
                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            alternateSymbols(-10)
                        }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            alternateSymbols(-1)
                        }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            alternateSymbols(0)
                        }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            alternateSymbols(1)
                        }.lessons[0].text shouldBe """
                            a b a b a
                            b a b a b
                        """.trimIndent()

                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            alternateSymbols(10)
                        }.lessons[0].text shouldHaveLength 10
                }

                expect("result is trimmed") {
                    val text = LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            alternateSymbols(5)
                        }.lessons[0].text
                    text shouldNotStartWith "\\s"
                    text shouldNotEndWith "\\s"
                }
            }

            context("shuffleSymbols") {

                expect("shuffle the input symbols") {
                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "ab") {
                            shuffleSymbols(10)
                        }.lessons[0].text shouldMatch "[ab]{10}"
                }

                expect("create content for all symbols from the input list") {
                    val text = LessonBuilder(20, 40, emptyList())
                        .newLesson("my lesson", listOf("ab", "cd")) {
                            shuffleSymbols(10)
                        }.lessons[0].text
                    text shouldContain "a"
                    text shouldContain "b"
                    text shouldContain "c"
                    text shouldContain "d"
                }

                expect("empty symbols lead to empty result") {
                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "") {
                            shuffleSymbols(10)
                        }.lessons shouldBe emptyList()
                }

                expect("segment-length range test") {
                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "abc") {
                            shuffleSymbols(-10)
                        }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "abc") {
                            shuffleSymbols(-1)
                        }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "abc") {
                            shuffleSymbols(0)
                        }.lessons shouldBe emptyList()

                    val text = LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "abc") {
                            shuffleSymbols(1)
                        }.lessons[0].text
                    text.split("\n")[0] shouldMatch "[abc ]{9}" // e.g. "a b c c a " 10 -> trimmed to "a b c c a" 9
                    text.split("\n")[1] shouldMatch "[abc ]{9}"

                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "abc") {
                            shuffleSymbols(10)
                        }.lessons[0].text shouldHaveLength 10
                }

                expect("result is trimmed") {
                    val text = LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "abc") {
                            shuffleSymbols(1)
                        }.lessons[0].text
                    text shouldNotStartWith "\\s"
                    text shouldNotEndWith "\\s"
                }
            }

            context("words") {

                expect("result consists of words from the dictionary consisting of symbols of the symbol history and containing the current lessons symbols") {
                    val text = LessonBuilder(20, 10, listOf("abc", "are", "you"))
                        .newLesson("my lesson", "acbreuoy") {
                            words()
                        }.lessons[0].text
                    text shouldContain "abc"
                    text shouldContain "are"
                    text shouldContain "you"
                }

                expect("empty dictionary leads to empty result") {
                    LessonBuilder(10, 10, emptyList())
                        .newLesson("my lesson", "acbreuoy") {
                            words()
                        }.lessons shouldBe emptyList()
                }

                expect("create content for all symbols from the input list") {
                    val text = LessonBuilder(20, 40, listOf("ab", "cd"))
                        .newLesson("my lesson", listOf("ab", "cd")) {
                            words()
                        }.lessons[0].text
                    text shouldContain "a"
                    text shouldContain "b"
                    text shouldContain "c"
                    text shouldContain "d"
                }

                expect("result is trimmed") {
                    val text = LessonBuilder(10, 10, listOf("abc", "are", "you"))
                        .newLesson("my lesson", "acbreuoy") {
                            words()
                        }.lessons[0].text
                    text shouldNotStartWith "\\s"
                    text shouldNotEndWith "\\s"
                }

                expect("each line is trimmed") {
                    val text = LessonBuilder(10, 10, listOf("abc", "are", "you"))
                        .newLesson("my lesson", "acbreuoy") {
                            words()
                        }.lessons[0].text
                    text.split("\n") shouldHaveAtLeastSize 2
                    text.split("\n").forAll { line ->
                        line shouldNotStartWith "\\s"
                        line shouldNotEndWith "\\s"
                    }
                }

                expect("unconditional punctuation marks are prefixed or appended randomly to the words") {
                    LessonBuilder(20, 10, listOf("abc", "are"))
                        .newLesson("my lesson", "acb,re") {
                            words()
                        }.lessons[0].text shouldContain "((,abc)|(abc,)|(,are)|(are,))".toRegex()
                }

                expect("WW punctuation marks are paired around a word") {
                    LessonBuilder(20, 10, listOf("abc", "are"))
                        .newLesson("building symbol history", "acbre") {
                            exampleBuildStep()
                        }.newLesson("my lesson", "<WW>") {
                            words()
                        }.lessons[1].text shouldContain "((<abc>)|(<are>))".toRegex()
                }

                expect("no word lessons are generated for digits") {
                    LessonBuilder(10, 10, listOf("abc", "are"))
                        .newLesson("building symbol history", "acbre") {
                            exampleBuildStep()
                        }.newLesson("my lesson", "123") {
                            words()
                        }.lessons.getOrNull(1) shouldBe null
                }
            }

            expect("result is empty when at least one build step has no (empty) output") {

                fun L.nothing(): L {
                    buildSteps.add { _ -> "" }
                    return this@nothing
                }

                val lessonBuilder = LessonBuilder(10, 10, emptyList())
                lessonBuilder.newLesson("building symbol history", "abc") {
                    shuffleSymbols(2)
                    nothing()
                    repeatSymbols(10)
                }.lessons shouldBe emptyList()
            }
        }

        context("LessonFilter") {

            context("Filter") {

                context("relativeLevenshteinDistanceFromLessonBefore") {

                    expect("true when different for more than X percent") {
                        val filterFun = Filter.relativeLevenshteinDistanceFromLessonBefore(0.6)
                        filterFun(Lesson(text = "0123456789"), Lesson(text = "0121111111")) shouldBe true
                    }

                    expect("false when different for less than X percent") {
                        val filterFun = Filter.relativeLevenshteinDistanceFromLessonBefore(0.6)
                        filterFun(Lesson(text = "0123456789"), Lesson(text = "0123456111")) shouldBe false
                    }

                    expect("true when last lesson is null") {
                        val filterFun = Filter.relativeLevenshteinDistanceFromLessonBefore(0.6)
                        filterFun(null, Lesson(text = "0123456111")) shouldBe true
                    }
                }

                context("containsAtLeastDifferentWords") {

                    expect("true when lesson text contains n different words") {
                        val filterFun = Filter.containsAtLeastDifferentWords(3)
                        filterFun(null, Lesson(text = "abc def ghi")) shouldBe true
                    }

                    expect("true when lesson text contains more than n different words") {
                        val filterFun = Filter.containsAtLeastDifferentWords(3)
                        filterFun(null, Lesson(text = "abc def ghi jkl")) shouldBe true
                    }

                    expect("true when lesson text contains less than n words in sum") {
                        val filterFun = Filter.containsAtLeastDifferentWords(3)
                        filterFun(null, Lesson(text = "abc")) shouldBe true
                    }

                    expect("false when lesson text is empty") {
                        val filterFun = Filter.containsAtLeastDifferentWords(3)
                        filterFun(null, Lesson(text = "")) shouldBe false
                    }
                }
            }

            context("relatedLevenshteinDistance") {

                expect("0.0 when equals") {
                    "abc".relativeLevenshteinDistance("abc") shouldBe 0.0
                }

                expect("1.0 when other is null") {
                    "abc".relativeLevenshteinDistance(null) shouldBe 1.0
                }

                expect("1.0 when other is empty") {
                    "abc".relativeLevenshteinDistance("") shouldBe 1.0
                }

                expect("1.0 when completely different") {
                    "abc".relativeLevenshteinDistance("def") shouldBe 1.0
                }

                expect("[0.0..1.0] when partly different") {
                    val distance = "abc".relativeLevenshteinDistance("axx")
                    distance shouldBeGreaterThan 0.0
                    distance shouldBeLessThan 1.0
                }

                expect("0.0 when source string is empty") {
                    "".relativeLevenshteinDistance("abc") shouldBe 0.0
                }
            }

            context("differentWords") {

                expect("true when contains n or more different words") {
                    "a aa b bb ab ba aaab".differentWords(3) shouldBe true
                }

                expect("true when contains less than n words in sum") {
                    "ab ba aa bb".differentWords(10) shouldBe true
                }

                expect("false when contains n or less different words") {
                    "abc abc abc abc abc".differentWords(3) shouldBe false
                }

                expect("false on empty source string") {
                    "".differentWords(10) shouldBe false
                }
            }
        }
    }
})