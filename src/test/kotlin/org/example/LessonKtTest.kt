package org.example

import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.stringPattern

class LessonKtTest : ConcurrentExpectSpec({

    context("StringBuilder") {

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

    context("unpack") {

        expect("unpack WW strings") {
            unpack("{WW}") shouldBe "{}"
        }

        expect("unpack letter groups") {
            unpack("[sch]") shouldBe "sch"
        }

        expect("return original input, if it is not packed") {
            unpack("apple pear") shouldBe "apple pear"
        }

        expect("empty input leads to empty output") {
            unpack("") shouldBe ""
        }
    }

    context("lessonWords") {

        expect("lesson-symbols must be contained in the chars-history") {
            setOf("ab", "abrc", "abrcel").lessonWords("abrcel", "el") shouldBe setOf("abrcel")
            setOf("ab", "abrc", "abrcel").lessonWords("abrc", "el") shouldBe emptyList()
        }

        expect("first lesson's words consist of the first lessons symbols only") {
            val dict = setOf("ab", "abrc", "abrcel")
            val charsHistory = "ab"
            val lessonSymbols = "ab"
            dict.lessonWords(charsHistory, lessonSymbols) shouldBe setOf("ab")
        }

        expect("words from lessons 2..n can contain symbols from all last lesson, but definitely contain at least one of the current lesson's symbols") {
            val dict = setOf("ab", "abrc", "abrcel")
            val charsHistory = "abrcel"
            val lessonSymbols = "el"
            dict.lessonWords(charsHistory, lessonSymbols) shouldBe setOf("abrcel")
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

        expect("if lesson symbols contains non-letters, ignore them and take words from history based on letters") {
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

        expect("if lesson symbols is a letter group, take words from history which contain the group") {
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

    context("ww") {

        expect("return the WW part") {
            ww("WW") shouldBe "WW"
            ww("(WW)") shouldBe "(WW)"
            ww("WW)=/\\") shouldBe "WW)=/\\"
            ww("_}*?/{(WW") shouldBe "_}*?/{(WW"
        }

        expect("return empty string when there is no WW part") {
            ww("W") shouldBe ""
            ww("") shouldBe ""
            ww("abc") shouldBe ""
        }

        expect("WW must be capital letters") {
            ww("{WW}") shouldBe "{WW}"
            ww("{ww}") shouldBe ""
            ww("{Ww}") shouldBe ""
            ww("{wW}") shouldBe ""
        }

        expect("return the first WW part only") {
            ww("(WW)(WW)") shouldBe "(WW)("
            ww("(WW)';[{(WW)}]") shouldBe "(WW)';[{("
        }

        expect("ignore everything else") {
            ww("abcABC(WW)") shouldBe "(WW)"
            ww("abcABC(WW)1abcABC") shouldBe "(WW)"
            ww("abc:ABC(WW)1a,bc_ABC") shouldBe "(WW)"
            ww("1WW1") shouldBe "WW"
            ww("1abcWW") shouldBe "WW"
            ww("()=abcWW") shouldBe "WW"
        }
    }

    context("wwUnpack") {

        expect("remove the WW in a WW string and return only the symbols") {
            wwUnpack("(WW)") shouldBe "()"
            wwUnpack("WW)=/\\") shouldBe ")=/\\"
            wwUnpack("_}*?/{(WW") shouldBe "_}*?/{("
            wwUnpack("WW") shouldBe ""
            wwUnpack("W") shouldBe ""
            wwUnpack("") shouldBe ""
        }

        expect("ignore everything else") {
            wwUnpack("abcABC(WW)") shouldBe "()"
            wwUnpack("abcABC(WW)1abcABC") shouldBe "()"
            wwUnpack("abc:ABC(WW)1a,bc_ABC") shouldBe "()"
            wwUnpack("abcABCWW") shouldBe ""
            wwUnpack("1WW1") shouldBe ""
            wwUnpack("1abcWW") shouldBe ""
            wwUnpack("()=abcWW") shouldBe ""
        }
    }

    context("letterGroupRegex") {

        expect("match letter groups") {
            "[sch]" shouldMatch letterGroupRegex
        }

        expect("not match anything else") {
            "[sch]a" shouldNotMatch letterGroupRegex.pattern
            "a[sch]" shouldNotMatch letterGroupRegex.pattern
            "[]" shouldNotMatch letterGroupRegex.pattern
            "abc" shouldNotMatch letterGroupRegex.pattern
            "1" shouldNotMatch letterGroupRegex.pattern
            "" shouldNotMatch letterGroupRegex.pattern
        }
    }

    context("letterGroup") {

        expect("return group of letters inclusive square brackets") {
            letterGroup("[sch]") shouldBe "[sch]"
            letterGroup("[123]") shouldBe ""
            letterGroup("[%';]") shouldBe ""
        }

        expect("return the first group only") {
            letterGroup("[sch][ch][ss][tt]") shouldBe "[sch]"
            letterGroup("[tt][ch][ss]") shouldBe "[tt]"
        }

        expect("return empty string on empty group") {
            letterGroup("[]") shouldBe ""
        }

        expect("return empty string when group consists of non-letters") {
            letterGroup("[123]") shouldBe ""
            letterGroup("[%';]") shouldBe ""
        }

        expect("return empty string when group contains non-letters") {
            letterGroup("[sch12]") shouldBe ""
            letterGroup("[sch%';]") shouldBe ""
        }

        expect("ignore surrounding square brackets") {
            letterGroup("[[sch]]") shouldBe "[sch]"
            letterGroup("[[[sch]]]") shouldBe "[sch]"
        }

        expect("WW part can not be a letter group") {
            letterGroup("[WW]") shouldBe ""
            letterGroup("{[WW]}") shouldBe ""
        }
    }

    context("letterGroupUnpack") {

        expect("return the letters of the group") {
            letterGroupUnpack("[sch]") shouldBe "sch"
        }

        expect("handle the first group only") {
            letterGroupUnpack("[sch][ch][ss][tt]") shouldBe "sch"
            letterGroupUnpack("[tt][ch][ss]") shouldBe "tt"
        }

        expect("return an empty string when the group is empty") {
            letterGroupUnpack("[]") shouldBe ""
        }

        expect("return empty string when group consists of non-letters") {
            letterGroupUnpack("[123]") shouldBe ""
            letterGroupUnpack("[%';]") shouldBe ""
        }

        expect("ignore surrounding square brackets") {
            letterGroupUnpack("[[sch]]") shouldBe "sch"
            letterGroupUnpack("[[[sch]]]") shouldBe "sch"
        }

        expect("WW part can not be a letter group") {
            letterGroupUnpack("[WW]") shouldBe ""
            letterGroupUnpack("{[WW]}") shouldBe ""
        }
    }

    context("unconditionalPunctuation") {

        expect("return punctuation marks which are not related to WW") {
            unconditionalPunctuation("!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~") shouldBe "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
            unconditionalPunctuation("abc") shouldBe ""
            unconditionalPunctuation("abc:") shouldBe ":"
            unconditionalPunctuation("123") shouldBe ""
            unconditionalPunctuation("123:") shouldBe ":"
            unconditionalPunctuation("1,2(WW)") shouldBe ","
            unconditionalPunctuation("()=abc=(WW)=") shouldBe "()="
            unconditionalPunctuation("()=abc[WW]") shouldBe "()="
        }

        expect("ignore WW part") {
            unconditionalPunctuation("abcABC(WW)") shouldBe ""
            unconditionalPunctuation("abcABCWW") shouldBe ""
            unconditionalPunctuation("1WW1") shouldBe ""
            unconditionalPunctuation("1abcWW") shouldBe ""
        }
    }

    context("letters regex") {

        expect("match lowercase and uppercase letters") {
            "abcdefghijklmnopqrstuvwxyzäöüßABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜẞ".matches(lettersRegex) shouldBe true
        }

        expect("not match digits") {
            "0123456789".matches(lettersRegex) shouldBe false
        }

        expect("not match punctuation marks") {
            "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~".matches(lettersRegex) shouldBe false
        }
    }

    context("letters") {

        expect("return lowercase and uppercase letters") {
            letters("W") shouldBe "W"
            letters("üäöß") shouldBe "üäöß"
            letters("abc") shouldBe "abc"
            letters("a,bc()=;deXXf") shouldBe "abcdeXXf"
            letters("abc123deXXf") shouldBe "abcdeXXf"
            letters("") shouldBe ""
        }

        expect("ignore digits") {
            letters("123\"") shouldBe ""
        }

        expect("ignore punctuation marks") {
            letters(",.';%{}[]()") shouldBe ""
        }

        expect("ignore WW part") {
            letters("WW") shouldBe ""
            letters("(WW)") shouldBe ""
            letters("abc(WW)abc(WW)';abc") shouldBe "abcabcabc"
            letters("abc(WW)=;def") shouldBe "abcdef"
        }

        expect("ignore letter groups") {
            letters("abc[sch]def") shouldBe "abcdef"
            letters("abcdef[sch]") shouldBe "abcdef"
            letters("[sch]abcdef") shouldBe "abcdef"
            letters("[sch]abcdef[tt]") shouldBe "abcdef"
            letters("abc[]def") shouldBe "abcdef"
            letters("abcdef[]") shouldBe "abcdef"
            letters("[]abcdef") shouldBe "abcdef"
        }

        expect("return empty string on empty input") {
            letters("") shouldBe ""
        }
    }

    context("digits") {

        expect("return digits") {
            digits("0123456789") shouldBe "0123456789"
        }

        expect("ignore letters") {
            digits("abc") shouldBe ""
            digits("abc123deXXf") shouldBe "123"
            digits("abc123deXXf,)(456") shouldBe "123456"
        }

        expect("ignore punctuation marks") {
            digits(",()=;") shouldBe ""
        }

        expect("ignore WW part") {
            digits("WW") shouldBe ""
            digits("(WW)") shouldBe ""
            digits("123(WW)") shouldBe "123"
            digits("123(WW)=;456") shouldBe "123456"
        }

        expect("ignore letter groups") {
            digits("123[sch]456") shouldBe "123456"
        }

        expect("return empty string on empty input") {
            digits("") shouldBe ""
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


    context("invokeConcatSingleLine") {

        expect("returns concatenated generators results as single line, single whitespace separated") {
            fun repeatA(n: Int) = "a".repeat(n)
            fun repeatB(n: Int) = "b".repeat(n)
            fun repeatC(n: Int) = "c".repeat(n)
            val generators = listOf(::repeatA, ::repeatB, ::repeatC)
            val result = invokeConcatSingleLine(10, generators)
            result shouldBe "aaa bbb ccc a"
        }

        expect("on single generator return the generators result with length of symbols-per-lesson") {
            fun repeatA(n: Int) = "a".repeat(n)
            val generators = listOf(::repeatA)
            invokeConcatSingleLine(10, generators) shouldBe "aaaaaaaaaa"
        }

        expect("on many generators return all generators results separated by whitespace") {
            fun repeatA(n: Int) = "a".repeat(n)
            fun repeatB(n: Int) = "b".repeat(n)
            val generators = listOf(::repeatA, ::repeatB)
            invokeConcatSingleLine(10, generators) shouldBe "aaaaa bbbbb"
        }

        expect("on many generators each generators result has length of symbols-per-lesson divided by number-of-generators") {
            fun repeatA(n: Int) = "a".repeat(n)
            fun repeatB(n: Int) = "b".repeat(n)
            fun repeatC(n: Int) = "c".repeat(n)
            val generators = listOf(::repeatA, ::repeatB, ::repeatC)
            val lines = invokeConcatSingleLine(9, generators).split(" ")
            lines[0] shouldHaveLength 3 // aaa
            lines[1] shouldHaveLength 3 // bbb
            lines[2] shouldHaveLength 3 // ccc
        }

        expect("re-invoke generators if necessary to get as many symbols as symbols-per-lesson") {
            fun repeatA(n: Int) = "a".repeat(n)
            fun repeatB(n: Int) = "b".repeat(n)
            fun repeatC(n: Int) = "c".repeat(n)
            val generators = listOf(::repeatA, ::repeatB, ::repeatC)
            val lines = invokeConcatSingleLine(10, generators).split(" ")

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
            val generators = listOf{_: Int -> ""}
            invokeConcatSingleLine(3, generators) shouldBe ""
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

            expect("re-invoke generators if necessary to get the total number of symbols") {
                fun repeatA(n: Int) = "a".repeat(n)
                fun repeatB(n: Int) = "b".repeat(n)
                fun repeatC(n: Int) = "c".repeat(n)
                val generators = listOf(::repeatA, ::repeatB, ::repeatC)
                val lines = generators.invokeConcat(3, 10).split(" ")

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
                val generators = listOf{_: Int -> ""}
                generators.invokeConcat(3, 10) shouldBe ""
            }
        }
    }

    context("String") {

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

    context("toTextBlock") {

        expect("break the input string into lines of max length line-length") {
            val result = toTextBlock("abcdef abc abc abcdef abc", 20, 5)
            val lines = result.split("\n")
            lines[0] shouldHaveLength 5
            lines[1] shouldHaveLength 5
            lines[2] shouldHaveLength 5
            lines[3] shouldHaveLength 5
            lines[4] shouldHaveLength 2
            result shouldBe """
            abcde
            f abc
            abc a
            bcdef
            ab
        """.trimIndent()
        }

        expect("input string property test") {
            val arbitraryBuilder = Arb.stringPattern("[A-Za-z0-9.,;\\[\\]{}\t]{30}")
            repeat(100) {
                val lines = toTextBlock(arbitraryBuilder.next(), 20, 5).split("\n")
                lines shouldHaveAtLeastSize 4
                lines[0] shouldHaveMinLength 3
                lines[0] shouldHaveMaxLength 5
                lines[1] shouldHaveMinLength 3
                lines[1] shouldHaveMaxLength 5
                lines[2] shouldHaveMinLength 3
                lines[2] shouldHaveMaxLength 5
                lines[3] shouldHaveMinLength 3
                lines[3] shouldHaveMaxLength 5
            }
        }

        expect("normalize multiple chained whitespace characters to a single one") {
            toTextBlock("abc    abc  \t abc   abc abc abc    abc", 20, 5) shouldBe
                    """
                    abc a
                    bc ab
                    c abc
                    abc a
                    bc ab
                    """.trimIndent()
        }

        expect("input string length is smaller than symbols-total leads to result having input string length") {
            toTextBlock("abc def ghi", 20, 5) shouldBe """
            abc d
            ef gh
            i
        """.trimIndent()
        }

        expect("input string length is smaller than line-length leads to result having input string length") {
            toTextBlock("ac", 20, 5) shouldBe "ac"
        }

        expect("return empty string when input string is empty") {
            toTextBlock("", 20, 5) shouldBe ""
        }

        expect("return empty string when symbols-total is zero or negative") {
            toTextBlock("abc abc abc", 0, 10) shouldBe ""
            toTextBlock("abc abc abc", -1, 10) shouldBe ""
        }

        expect("return empty string when line-length is zero or negative") {
            toTextBlock("abc abc abc", 10, 0) shouldBe ""
            toTextBlock("abc abc abc", 10, -1) shouldBe ""
        }
    }

    context("buildLesson") {

        fun L.exampleBuildStep(): L {
            buildSteps.add { _ -> Arb.stringPattern("[A-Za-z0-9.,;@:<>]{100}\t[A-Za-z0-9.,;@:<>]{100}").next() }
            return this@exampleBuildStep
        }

        expect("properties will be correctly assigned") {
            val lesson = buildLesson("my lesson", 10, 10,"ab") {
                exampleBuildStep()
            }
            lesson.title shouldBe "my lesson"
            lesson.newCharacters shouldBe "ab"
            lesson.text.length shouldBe 10
        }

        expect("apply all build steps") {
            val text = buildLesson(lineLength = 30, symbolsPerLesson = 400) {
                repeatSymbols("123", 3)
                shuffledSymbols("def", 10)
                wordsWithUnconditionalPunctuationMarks(listOf("hi"), ";")
                words(listOf("apple"))
                repeatSymbols("{}", 3)
            }.text
            text.count { !it.isWhitespace() } shouldBe 400
            text shouldContain "123"
            text shouldContain "[def]{3,}".toRegex()
            text shouldContain "{}"
            text shouldContain ";*hi;*".toRegex()
            text shouldContain "apple"
        }

        expect("line-length range test") {
            buildLesson(lineLength = -10, symbolsPerLesson = 10) {
                exampleBuildStep()
            }.text shouldHaveLength 0

            buildLesson(lineLength = -1, symbolsPerLesson = 10) {
                exampleBuildStep()
            }.text shouldHaveLength 0

            buildLesson(lineLength = 0, symbolsPerLesson = 10) {
                exampleBuildStep()
            }.text shouldHaveLength 0

            buildLesson(lineLength = 1, symbolsPerLesson = 10) {
                exampleBuildStep()
            }.text.count { !it.isWhitespace() } shouldBe 10

            buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                exampleBuildStep()
            }.text shouldHaveLength 10
        }

        expect("symbols-per-lesson range test") {
            buildLesson(lineLength = 10, symbolsPerLesson = -100) {
                exampleBuildStep()
            }.text shouldHaveLength 0

            buildLesson(lineLength = 10, symbolsPerLesson = -10) {
                exampleBuildStep()
            }.text shouldHaveLength 0

            buildLesson(lineLength = 10, symbolsPerLesson = -1) {
                exampleBuildStep()
            }.text shouldHaveLength 0

            buildLesson(lineLength = 10, symbolsPerLesson = 0) {
                exampleBuildStep()
            }.text shouldHaveLength 0

            buildLesson(lineLength = 10, symbolsPerLesson = 1) {
                exampleBuildStep()
            }.text shouldHaveLength 1

            buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                exampleBuildStep()
            }.text.count { !it.isWhitespace() } shouldBe 10

            val text100 = buildLesson(lineLength = 10, symbolsPerLesson = 100) {
                exampleBuildStep()
            }.text
            text100.split("\n") shouldHaveAtLeastSize 10
            text100.count { !it.isWhitespace() } shouldBe 100
        }

        context("symbols-per-lesson < line-length") {

            expect("result contains symbols-per-lesson non-whitespace characters") {
                buildLesson(lineLength = 20, symbolsPerLesson = 8) {
                    exampleBuildStep()
                }.text.count { !it.isWhitespace() } shouldBe 8
            }
        }

        context("symbols-per-lesson > line-length") {

            expect("result is multiline containing symbols-per-lesson non-whitespace characters") {
                val text = buildLesson(lineLength = 8, symbolsPerLesson = 20) {
                    exampleBuildStep()
                }.text
                text.split("\n") shouldHaveAtLeastSize 2
                text.count { !it.isWhitespace() } shouldBe 20
            }
        }

        context("repeatSymbols") {

            expect("repeat the input symbols") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    repeatSymbols("ab", 10)
                }.text shouldBe "ababababab"
            }

            expect("segment-length range test") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    repeatSymbols("abc", -10)
                }.text shouldHaveLength 0

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    repeatSymbols("abc", -1)
                }.text shouldHaveLength 0

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    repeatSymbols("abc", 0)
                }.text shouldHaveLength 0

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    repeatSymbols("abc", 1)
                }.text shouldBe """
                    a b c a b
                    c a b c a
                """.trimIndent()

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    repeatSymbols("abc", 10)
                }.text shouldHaveLength 10
            }

            expect("empty input leads to empty output") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    repeatSymbols("", 10)
                }.text.length shouldBe 0
            }

            expect("result is trimmed") {
                val text = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    repeatSymbols("abc", 1)
                }.text
                text shouldNotStartWith "\\s"
                text shouldNotEndWith "\\s"
            }
        }

        context("alternatingSymbols") {

            expect("result consists of the input symbols in alternating fashion") {
                buildLesson(lineLength = 14, symbolsPerLesson = 10) {
                    alternatingSymbols("abc", 2)
                }.text shouldBe "aa bb cc aa bb"
            }

            expect("empty input leads to empty output") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    alternatingSymbols("", 10)
                }.text.length shouldBe 0
            }

            expect("segment-length range test") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    alternatingSymbols("abc", -10)
                }.text shouldHaveLength 0

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    alternatingSymbols("abc", -1)
                }.text shouldHaveLength 0

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    alternatingSymbols("abc", 0)
                }.text shouldHaveLength 0

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    alternatingSymbols("abc", 1)
                }.text shouldBe """
                    a b c a b
                    c a b c a
                """.trimIndent()

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    alternatingSymbols("abc", 10)
                }.text shouldHaveLength 10
            }

            expect("result is trimmed") {
                val text = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    alternatingSymbols("abc", 1)
                }.text
                text shouldNotStartWith "\\s"
                text shouldNotEndWith "\\s"
            }
        }

        context("shuffledSymbols") {

            expect("shuffle the input symbols") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    shuffledSymbols("ab", 10)
                }.text shouldMatch "[ab]{10}"
            }

            expect("empty input leads to empty output") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    shuffledSymbols("", 10)
                }.text.length shouldBe 0
            }

            expect("segment-length range test") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    shuffledSymbols("abc", -10)
                }.text shouldHaveLength 0

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    shuffledSymbols("abc", -1)
                }.text shouldHaveLength 0

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    shuffledSymbols("abc", 0)
                }.text shouldHaveLength 0

                val text = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    shuffledSymbols("abc", 1)
                }.text
                text.split("\n")[0] shouldMatch "[abc ]{9}" // e.g. "a b c c a " 10 -> trimmed to "a b c c a" 9
                text.split("\n")[1] shouldMatch "[abc ]{9}"

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    shuffledSymbols("abc", 10)
                }.text shouldHaveLength 10
            }

            expect("result is trimmed") {
                val text = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    shuffledSymbols("abc", 1)
                }.text
                text shouldNotStartWith "\\s"
                text shouldNotEndWith "\\s"
            }
        }

        context("words") {

            expect("result consists of input words") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    words(listOf("hi", "are", "you", "ready"))
                }.text shouldBe """
                    hi are you
                    hi
                """.trimIndent()

                buildLesson(lineLength = 4, symbolsPerLesson = 10) {
                    words(listOf("hi", "are", "you", "ready"))
                }.text shouldBe """
                    hi a
                    re y
                    ou h
                    i
                """.trimIndent()
            }

            expect("empty input leads to empty output") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    words(emptyList())
                }.text shouldBe ""
            }

            expect("result is trimmed") {
                val text = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    words(listOf("hi", "are", "you", "ready"))
                }.text
                text.split("\\s".toRegex()) shouldHaveSize 4
                text.split("\\s".toRegex()).forAll { line ->
                    line shouldNotStartWith "\\s"
                    line shouldNotEndWith "\\s"
                }
            }
        }

        context("randomLeftRightPunctuationMarks") {

            expect("result contains punctuation marks randomly, but left/right condition is met") {
                val text = buildLesson(lineLength = 2, symbolsPerLesson = 10) {
                    randomLeftRightPunctuationMarks("[{WW}]", 2)
                }.text
                text.split("\n") shouldHaveSize 5
                text.split("\n").forAll { it shouldBeIn setOf("[]", "{}") }

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    randomLeftRightPunctuationMarks("[{WW,", 10)
                }.text shouldMatch "[\\[\\{,]{10}"

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    randomLeftRightPunctuationMarks("WW}]", 10)
                }.text shouldMatch "[\\]\\}]{10}"
            }

            expect("empty input leads to empty output") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    randomLeftRightPunctuationMarks("", 10)
                }.text shouldBe ""
            }

            expect("segment-length range test") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    randomLeftRightPunctuationMarks("[{WW}].,", -10)
                }.text shouldHaveLength 0

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    randomLeftRightPunctuationMarks("[{WW}].,", -1)
                }.text shouldHaveLength 0

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    randomLeftRightPunctuationMarks("[{WW}].,", 0)
                }.text shouldHaveLength 0

                val text = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    randomLeftRightPunctuationMarks("[{WW}].,", 1)
                }.text
                text.split("\n") shouldHaveSize 2
                text.split("\n").forAll { it shouldMatch "[\\{\\[\\]\\}., ]{9}" } // e.g. "{ } [ ] , " 10 -> trimmed to "{ } [ ] ," 9

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    randomLeftRightPunctuationMarks("[{WW}].,", 10)
                }.text shouldHaveLength 10
            }

            expect("result is trimmed") {
                val text = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    randomLeftRightPunctuationMarks("[{WW}].,", 1)
                }.text
                text shouldNotStartWith "\\s"
                text shouldNotEndWith "\\s"
            }
        }

        context("wordsWithLeftRightPunctuationMarks") {

            expect("result consists of input words with added left and right punctuation marks") {
                buildLesson(lineLength = 10, symbolsPerLesson = 50) {
                    wordsWithLeftRightPunctuationMarks(listOf("hi", "are", "you", "ready"), "[WW]")
                }.text shouldBe """
                    [hi] [are]
                    [you] [rea
                    dy] [hi] [
                    are] [you]
                    [ready] [h
                    i] [hi]
                """.trimIndent()

                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    wordsWithLeftRightPunctuationMarks(listOf("hi", "are", "you", "ready"), "WW,")
                }.text shouldBe """
                    hi, are, h
                    i,
                """.trimIndent()
            }

            expect("do not add punctuation marks when WW string has no punctuation marks") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    wordsWithLeftRightPunctuationMarks(listOf("hi", "are", "you", "ready"), "WW")
                }.text shouldBe """
                    hi are you
                    hi
                """.trimIndent()
            }

            expect("empty word list leads to empty output") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    wordsWithLeftRightPunctuationMarks(emptyList(), "WW.,;")
                }.text shouldHaveLength 0
            }

            expect("empty WW string leads to empty output") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    wordsWithLeftRightPunctuationMarks(listOf("hi", "are", "you", "ready"), "")
                }.text shouldHaveLength 0
            }

            expect("result is trimmed") {
                val text = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    wordsWithLeftRightPunctuationMarks(listOf("hi", "are", "you", "ready"), "[{WW}].,")
                }.text
                text.split("\\s".toRegex()) shouldHaveAtLeastSize 3
                text.split("\\s".toRegex()).forAll { line ->
                    line shouldNotStartWith "\\s"
                    line shouldNotEndWith "\\s"
                }
            }
        }

        context("wordsWithUnconditionalPunctuationMarks") {

            expect("result consists of input words with added punctuation marks") {
                val text = buildLesson(lineLength = 20, symbolsPerLesson = 34) {
                    wordsWithUnconditionalPunctuationMarks(listOf("hi", "are", "you", "ready"), ".")
                }.text
                text.count { !it.isWhitespace() } shouldBe 34
                text.split("\\s".toRegex()) shouldHaveAtLeastSize 1
                text.split("\\s".toRegex())
                    .forAll { it shouldBeIn setOf(
                        "hi.", ".hi",
                        "are.", ".are",
                        "you.", ".you",
                        "ready.", ".ready")
                    }
            }

            expect("can build pairs when a WW string is passed") {
                val text = buildLesson(lineLength = 20, symbolsPerLesson = 34) {
                    wordsWithUnconditionalPunctuationMarks(listOf("hi", "are", "you", "ready"), "[WW]")
                }.text
                text.count { !it.isWhitespace() } shouldBe 34
                text.replace("\n", "")
                    .split("\\s".toRegex())
                    .forAll { it shouldBeIn setOf("[hi]", "[are]", "[you]", "[ready]") }
            }

            expect("empty word list leads to empty output") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    wordsWithUnconditionalPunctuationMarks(emptyList(), ".,;")
                }.text shouldHaveLength 0
            }

            expect("empty punctuation-marks leads to empty output") {
                buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    wordsWithUnconditionalPunctuationMarks(listOf("hi", "are", "you", "ready"), "")
                }.text shouldHaveLength 0
            }

            expect("result is trimmed") {
                val text = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
                    wordsWithUnconditionalPunctuationMarks(listOf("hi", "are", "you", "ready"), "_.,")
                }.text
                text.split("\\s".toRegex()) shouldHaveAtLeastSize 3
                text.split("\\s".toRegex()).forAll { line ->
                    line shouldNotStartWith "\\s"
                    line shouldNotEndWith "\\s"
                }
            }
        }
    }
})