package org.example

import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.*
import org.junit.jupiter.api.Test

class LessonsKtTest {

    @Test
    fun `StringBuilder newCharacters empty result when input chars are already contained`() {
        val history = "abcd"
        val sb = StringBuilder(history)
        sb.newCharacters("a") shouldBe ""
        sb.newCharacters("ab") shouldBe ""
        sb.newCharacters("cd") shouldBe ""
        sb.newCharacters("abcd") shouldBe ""
        sb.newCharacters("dcab") shouldBe ""
    }

    @Test
    fun `StringBuilder newCharacters new symbols will be added and returned`() {
        val sbEmpty = StringBuilder("")
        sbEmpty.newCharacters("x") shouldBe "x"
        sbEmpty.toString() shouldContain "x"

        val sbWithHistory = StringBuilder("abcd")
        sbWithHistory.newCharacters("x") shouldBe "x"
        sbWithHistory.toString() shouldContain "abcdx"
    }

    @Test
    fun `StringBuilder newCharacters empty input empty output`() {
        val history = "abcd"
        val sb = StringBuilder(history)
        sb.newCharacters("") shouldBe ""
    }

    @Test
    fun `lessonWords example first lesson (ab)`() {
        val dict = setOf("ab", "abrc", "abrcel")
        val charsHistory = "ab" // remember: the order matters
        val lessonSymbols = "ab"
        dict.lessonWords(charsHistory, lessonSymbols) shouldBe setOf("ab")
    }

    @Test
    fun `lessonWords example second lesson (rc)`() {
        val dict = setOf("ab", "abrc", "abrcel")
        val charsHistory = "abrc" // remember: the order matters
        val lessonSymbols = "rc"
        dict.lessonWords(charsHistory, lessonSymbols) shouldBe setOf("abrc")
    }

    @Test
    fun `lessonWords example third lesson (el)`() {
        val dict = setOf("ab", "abrc", "abrcel")
        val charsHistory = "abrcel" // remember: the order matters
        val lessonSymbols = "el"
        dict.lessonWords(charsHistory, lessonSymbols) shouldBe setOf("abrcel")
    }

    @Test
    fun `lessonWords rotates the dict randomly but always preserves the word order`() {
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

    @Test
    fun `lessonWords dict is empty`() {
        val charsHistory = "abrcel"
        val lessonSymbols = "el"
        emptySet<String>().lessonWords(charsHistory, lessonSymbols) shouldBe emptyList()
    }

    @Test
    fun `lessonWords empty history`() {
        val dict = setOf("ab", "abrc", "abrcel")
        val charsHistory = ""
        val lessonSymbols = "el"
        dict.lessonWords(charsHistory, lessonSymbols) shouldBe emptyList()
    }

    @Test
    fun `lessonWords if lesson symbols contains non-letters, ignore them and take words from history based on letters`() {
        val dict = setOf("ad", "b", "tt", "a", "cd", "bd", "xx", "d")
        val charsHistory = "abc_[]d().;"
        val lessonSymbols = "_[]d().;"
        dict.lessonWords(charsHistory, lessonSymbols) shouldContainExactlyInAnyOrder listOf("ad", "cd", "bd", "d")
    }

    @Test
    fun `lessonWords if lesson symbols consists of non-letters, ignore them and take words from history`() {
        val dict = setOf("ab", "b", "tt", "a", "ba", "xx", "bab")
        val charsHistory = "ab_[]().;"
        val lessonSymbols = "_[]().;"
        dict.lessonWords(charsHistory, lessonSymbols) shouldContainExactlyInAnyOrder listOf("ab", "b", "a", "ba", "bab")
    }

    @Test
    fun `ww should return the WW part`() {
        ww("WW") shouldBe "WW"
        ww("(WW)") shouldBe "(WW)"
        ww("WW)=/\\") shouldBe "WW)=/\\"
        ww("_}*?/{(WW") shouldBe "_}*?/{(WW"
        ww("W") shouldBe ""
        ww("") shouldBe ""
    }

    @Test
    fun `ww should ignore everything else`() {
        ww("abcABC(WW)") shouldBe "(WW)"
        ww("abcABC(WW)1abcABC") shouldBe "(WW)"
        ww("abc:ABC(WW)1a,bc_ABC") shouldBe "(WW)"
        ww("1WW1") shouldBe "WW"
        ww("1abcWW") shouldBe "WW"
        ww("()=abcWW") shouldBe "WW"
    }

    @Test
    fun `wwSymbols should remove the WW in a WW string and return only the symbols`() {
        wwSymbols("(WW)") shouldBe "()"
        wwSymbols("WW)=/\\") shouldBe ")=/\\"
        wwSymbols("_}*?/{(WW") shouldBe "_}*?/{("
        wwSymbols("WW") shouldBe ""
        wwSymbols("W") shouldBe ""
        wwSymbols("") shouldBe ""
    }

    @Test
    fun `wwSymbols ignore everything else`() {
        wwSymbols("abcABC(WW)") shouldBe "()"
        wwSymbols("abcABC(WW)1abcABC") shouldBe "()"
        wwSymbols("abc:ABC(WW)1a,bc_ABC") shouldBe "()"
        wwSymbols("abcABCWW") shouldBe ""
        wwSymbols("1WW1") shouldBe ""
        wwSymbols("1abcWW") shouldBe ""
        wwSymbols("()=abcWW") shouldBe ""
    }

    @Test
    fun `unconditionalPunctuation should return punctuation marks which are not related to WW`() {
        unconditionalPunctuation("!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~") shouldBe "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
        unconditionalPunctuation("abc") shouldBe ""
        unconditionalPunctuation("abc:") shouldBe ":"
        unconditionalPunctuation("123") shouldBe ""
        unconditionalPunctuation("123:") shouldBe ":"
        unconditionalPunctuation("1,2(WW)") shouldBe ","
        unconditionalPunctuation("()=abc=(WW)=") shouldBe "()="
        unconditionalPunctuation("()=abc[WW]") shouldBe "()="
    }

    @Test
    fun `unconditionalPunctuation should ignore WW part`() {
        unconditionalPunctuation("abcABC(WW)") shouldBe ""
        unconditionalPunctuation("abcABCWW") shouldBe ""
        unconditionalPunctuation("1WW1") shouldBe ""
        unconditionalPunctuation("1abcWW") shouldBe ""
        unconditionalPunctuation("()=abcWW") shouldBe "()="
        unconditionalPunctuation("abcWWc()=") shouldBe "()="
    }

    @Test
    fun `letters regex test`() {
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".matches(lettersRegex) shouldBe true
        "0123456789".matches(lettersRegex) shouldBe false
        "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~".matches(lettersRegex) shouldBe false
    }

    @Test
    fun `letters should return letters`() {
        letters("W") shouldBe "W"
        letters("üäöß") shouldBe "üäöß"
        letters("abc") shouldBe "abc"
        letters("a,bc()=;deXXf") shouldBe "abcdeXXf"
        letters("abc123deXXf") shouldBe "abcdeXXf"
        letters("123\"") shouldBe ""
        letters("") shouldBe ""
    }

    @Test
    fun `letters should ignore WW part`() {
        letters("WW") shouldBe ""
        letters("(WW)") shouldBe ""
        letters("abc(WW)") shouldBe "abc"
        letters("abc(WW)=;def") shouldBe "abcdef"
    }

    @Test
    fun `digits should return digits`() {
        digits("0123456789") shouldBe "0123456789"
        digits("1,2") shouldBe "12"
        digits("abc") shouldBe ""
        digits(",()=;") shouldBe ""
        digits("abc123deXXf") shouldBe "123"
        digits("abc123deXXf,)(456") shouldBe "123456"
        digits("123\"") shouldBe "123"
        digits("") shouldBe ""
    }

    @Test
    fun `digits should ignore WW part`() {
        digits("WW") shouldBe ""
        digits("(WW)") shouldBe ""
        digits("123(WW)") shouldBe "123"
        digits("123(WW)=;456") shouldBe "123456"
    }

    @Test
    fun `buildLesson properties assignment`() {
        val lesson = buildLesson("my lesson", 10, "ab") {
            repeatedSymbolsLine("a", 10)
        }
        lesson.title shouldBe "my lesson"
        lesson.newCharacters shouldBe "ab"
        lesson.text.length shouldBe 10
    }

    @Test
    fun `buildLesson repeatedSymbolsLine happy`() {
        buildLesson("", 10) {
            repeatedSymbolsLine("ab", 10)
        }.text shouldBe "ababababab"

        buildLesson("", 10) {
            repeatedSymbolsLine("ab", 3)
        }.text shouldBe "aba bab ab"
    }

    @Test
    fun `buildLesson repeatedSymbolsLine empty input`() {
        buildLesson("", 10) {
            repeatedSymbolsLine("", 10)
        }.text.length shouldBe 0
    }

    @Test
    fun `buildLesson repeatedSymbolsLine line length range test`() {
        buildLesson("", -10) {
            repeatedSymbolsLine("abc", 10)
        }.text shouldHaveLength 0

        buildLesson("", -1) {
            repeatedSymbolsLine("abc", 10)
        }.text shouldHaveLength 0

        buildLesson("", 0) {
            repeatedSymbolsLine("abc", 10)
        }.text shouldHaveLength 0

        buildLesson("", 1) {
            repeatedSymbolsLine("abc", 10)
        }.text shouldHaveLength 1

        buildLesson("", 10) {
            repeatedSymbolsLine("abc", 10)
        }.text shouldHaveLength 10
    }

    @Test
    fun `buildLesson repeatedSymbolsLine segment length range test`() {
        buildLesson("", 10) {
            repeatedSymbolsLine("abc", -10)
        }.text shouldBe ""

        buildLesson("", 10) {
            repeatedSymbolsLine("abc", -1)
        }.text shouldBe ""

        buildLesson("", 10) {
            repeatedSymbolsLine("abc", 0)
        }.text shouldBe ""

        buildLesson("", 10) {
            repeatedSymbolsLine("abc", 1)
        }.text shouldBe "a b c a b"

        buildLesson("", 10) {
            repeatedSymbolsLine("abc", 10)
        }.text shouldHaveLength 10
    }

    @Test
    fun `buildLesson repeatedSymbolsLine trimmed`() {
        val lesson = buildLesson("", 10) {
            repeatedSymbolsLine("abc", 1)
        }
        lesson.text.take(1) shouldNotMatch "\\s"
        lesson.text.takeLast(1) shouldNotMatch "\\s"
    }

    @Test
    fun `buildLesson shuffledSymbolsLine happy`() {
        buildLesson("", 10) {
            shuffledSymbolsLine("ab", 10)
        }.text shouldMatch "[ab]{10}"

        buildLesson("", 10) {
            repeatedSymbolsLine("ab", 3)
        }.text shouldMatch "[ab ]{10}"
    }

    @Test
    fun `buildLesson shuffledSymbolsLine empty input`() {
        buildLesson("", 10) {
            shuffledSymbolsLine("", 10)
        }.text.length shouldBe 0
    }

    @Test
    fun `buildLesson shuffledSymbolsLine line length range test`() {
        buildLesson("", -10) {
            shuffledSymbolsLine("abc", 10)
        }.text shouldHaveLength 0

        buildLesson("", -1) {
            shuffledSymbolsLine("abc", 10)
        }.text shouldHaveLength 0

        buildLesson("", 0) {
            shuffledSymbolsLine("abc", 10)
        }.text shouldHaveLength 0

        buildLesson("", 1) {
            shuffledSymbolsLine("abc", 10)
        }.text shouldHaveLength 1

        buildLesson("", 10) {
            shuffledSymbolsLine("abc", 10)
        }.text shouldHaveLength 10
    }

    @Test
    fun `buildLesson shuffledSymbolsLine segment length range test`() {
        buildLesson("", 10) {
            shuffledSymbolsLine("abc", -10)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            shuffledSymbolsLine("abc", -1)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            shuffledSymbolsLine("abc", 0)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            shuffledSymbolsLine("abc", 1)
        }.text shouldMatch "[abc ]{9}" // e.g. "a b c c a " 10 -> trimmed to "a b c c a" 9

        buildLesson("", 10) {
            shuffledSymbolsLine("abc", 10)
        }.text shouldHaveLength 10
    }

    @Test
    fun `buildLesson shuffledSymbolsLine trimmed`() {
        val lesson = buildLesson("", 10) {
            shuffledSymbolsLine("abc", 1)
        }
        lesson.text.take(1) shouldNotMatch "\\s"
        lesson.text.takeLast(1) shouldNotMatch "\\s"
    }

    @Test
    fun `buildLesson wordsMultiline happy`() {
        buildLesson("", 10) {
            wordsMultiline(listOf("hi", "are", "you", "ready"), 2)
        }.text shouldBe "hi are"

        buildLesson("", 4) {
            wordsMultiline(listOf("hi", "are", "you", "ready"), 10)
        }.text shouldBe """
            hi
            are
            you
            hi
            are
            you
            hi
            are
        """.trimIndent()
    }

    @Test
    fun `buildLesson wordsMultiline empty input`() {
        buildLesson("", 10) {
            wordsMultiline(emptyList(), 10)
        }.text shouldBe ""
    }

    @Test
    fun `buildLesson wordsMultiline line length range test`() {
        buildLesson("", -10) {
            wordsMultiline(listOf("hi", "are", "you", "ready"), 10)
        }.text.split('\n').forAll { line -> line shouldHaveLength 0 }

        buildLesson("", -1) {
            wordsMultiline(listOf("c", "hi", "are", "you", "ready"), 10)
        }.text.split('\n').forAll { line -> line shouldHaveLength 0 }

        buildLesson("", 0) {
            wordsMultiline(listOf("c", "hi", "are", "you", "ready"), 10)
        }.text.split('\n').forAll { line -> line shouldHaveLength 0 }

        buildLesson("", 1) {
            wordsMultiline(listOf("c", "hi", "are", "you", "ready"), 10)
        }.text.split('\n').forAll { line -> line shouldHaveLength 1 }

        buildLesson("", 10) {
            wordsMultiline(listOf("c", "hi", "are", "you", "ready"), 10)
        }.text.split('\n').forAll { line -> line shouldHaveMaxLength 10 }
    }

    @Test
    fun `buildLesson wordsMultiline word count range test`() {
        buildLesson("", 10) {
            wordsMultiline(listOf("hi", "are", "you", "ready"), -10)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            wordsMultiline(listOf("hi", "are", "you", "ready"), -1)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            wordsMultiline(listOf("hi", "are", "you", "ready"), 0)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            wordsMultiline(listOf("hi", "are", "you", "ready"), 1)
        }.text shouldBe "hi"

        buildLesson("", 10) {
            wordsMultiline(listOf("hi", "are", "you", "ready"), 10)
        }.text.split("\\s".toRegex()) shouldHaveSize 10
    }

    @Test
    fun `buildLesson wordsMultiline trimmed`() {
        buildLesson("", 10) {
            wordsMultiline(listOf("hi", "are", "you", "ready"), 10)
        }.text.split("\\s").forAll { line ->
            line.take(1) shouldNotMatch "\\s"
            line.takeLast(1) shouldNotMatch "\\s"
        }
    }

    @Test
    fun `buildLesson randomLeftRightPunctuationMarks happy`() {
        buildLesson("", 2) {
            randomLeftRightPunctuationMarks("[{WW}]", 2)
        }.text shouldBeIn setOf("[]", "{}")

        buildLesson("", 10) {
            randomLeftRightPunctuationMarks("[{WW,", 10)
        }.text shouldMatch "[\\[\\{,]{10}"

        buildLesson("", 10) {
            randomLeftRightPunctuationMarks("WW}]", 10)
        }.text shouldMatch "[\\]\\}]{10}"
    }

    @Test
    fun `buildLesson randomLeftRightPunctuationMarks empty input`() {
        buildLesson("", 10) {
            randomLeftRightPunctuationMarks("", 10)
        }.text shouldBe ""
    }

    @Test
    fun `buildLesson randomLeftRightPunctuationMarks line length range test`() {
        buildLesson("", -10) {
            randomLeftRightPunctuationMarks("[{WW}].,", 10)
        }.text shouldHaveLength 0

        buildLesson("", -1) {
            randomLeftRightPunctuationMarks("[{WW}].,", 10)
        }.text shouldHaveLength 0

        buildLesson("", 0) {
            randomLeftRightPunctuationMarks("[{WW}].,", 10)
        }.text shouldHaveLength 0

        buildLesson("", 1) {
            randomLeftRightPunctuationMarks("[{WW}].,", 10)
        }.text shouldHaveLength 1

        buildLesson("", 10) {
            randomLeftRightPunctuationMarks("[{WW}].,", 10)
        }.text shouldHaveLength 10
    }

    @Test
    fun `buildLesson randomLeftRightPunctuationMarks segment length range test`() {
        buildLesson("", 10) {
            randomLeftRightPunctuationMarks("[{WW}].,", -10)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            randomLeftRightPunctuationMarks("[{WW}].,", -1)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            randomLeftRightPunctuationMarks("[{WW}].,", 0)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            randomLeftRightPunctuationMarks("[{WW}].,", 1)
        }.text shouldMatch "[\\{\\[\\]\\}., ]{9}" // e.g. "{ } [ ] , " 10 -> trimmed to "{ } [ ] ," 9

        buildLesson("", 10) {
            randomLeftRightPunctuationMarks("[{WW}].,", 10)
        }.text shouldHaveLength 10
    }

    @Test
    fun `buildLesson randomLeftRightPunctuationMarks trimmed`() {
        val lesson = buildLesson("", 10) {
            randomLeftRightPunctuationMarks("[{WW}].,", 1)
        }
        lesson.text.take(1) shouldNotMatch "\\s"
        lesson.text.takeLast(1) shouldNotMatch "\\s"
    }

    @Test
    fun `buildLesson wordsWithLeftRightPunctuationMarksMultiline happy`() {
        buildLesson("", 10) {
            wordsWithLeftRightPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), "[WW]", 10)
        }.text shouldBe """
            [hi] [are]
            [you]
            [ready]
            [hi] [are]
            [you]
            [ready]
            [hi] [are]
        """.trimIndent()

        buildLesson("", 10) {
            wordsWithLeftRightPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), "WW,", 10)
        }.text shouldBe """
            hi, are,
            you,
            ready, hi,
            are, you,
            ready, hi,
            are,
        """.trimIndent()
    }

    @Test
    fun `buildLesson wordsWithLeftRightPunctuationMarksMultiline empty input`() {
        buildLesson("", 10) {
            wordsWithLeftRightPunctuationMarksMultiline(emptyList(), "", 10)
        }.text shouldBe ""
    }

    @Test
    fun `buildLesson wordsWithLeftRightPunctuationMarksMultiline line length range test`() {
        buildLesson("", -10) {
            wordsWithLeftRightPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), "[WW]", 10)
        }.text.split('\n').forAll { line -> line shouldHaveLength 0 }

        buildLesson("", -1) {
            wordsWithLeftRightPunctuationMarksMultiline(listOf("c", "hi", "are", "you", "ready"), "[WW]", 10)
        }.text.split('\n').forAll { line -> line shouldHaveLength 0 }

        buildLesson("", 0) {
            wordsWithLeftRightPunctuationMarksMultiline(listOf("c", "hi", "are", "you", "ready"), "[WW]", 10)
        }.text.split('\n').forAll { line -> line shouldHaveLength 0 }

        buildLesson("", 1) {
            wordsWithLeftRightPunctuationMarksMultiline(listOf("c", "hi", "are", "you", "ready"), "[WW]", 10)
        }.text.split('\n').forAll { line -> line shouldHaveLength 0 } // shortest is "[c]"

        buildLesson("", 10) {
            wordsWithLeftRightPunctuationMarksMultiline(listOf("c", "hi", "are", "you", "ready"), "[WW]", 10)
        }.text.split('\n').forAll { line -> line shouldHaveMaxLength 10 }
    }

    @Test
    fun `buildLesson wordsWithLeftRightPunctuationMarksMultiline word count range test`() {
        buildLesson("", 10) {
            wordsWithLeftRightPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), "{WW}", -10)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            wordsWithLeftRightPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), "{WW},", -1)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            wordsWithLeftRightPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), "{WW},", 0)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            wordsWithLeftRightPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), "{WW}", 1)
        }.text shouldBe "{hi}"

        buildLesson("", 10) {
            wordsWithLeftRightPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), "{WW}", 10)
        }.text.split("\\s".toRegex()) shouldHaveSize 10
    }

    @Test
    fun `buildLesson wordsWithLeftRightPunctuationMarksMultiline trimmed`() {
        buildLesson("", 10) {
            wordsWithLeftRightPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), "[{WW}].,", 10)
        }.text.split("\\s").forAll { line ->
            line.take(1) shouldNotMatch "\\s"
            line.takeLast(1) shouldNotMatch "\\s"
        }
    }

    @Test
    fun `buildLesson randomUnconditionalPunctuationMarks happy`() {
        buildLesson("", 10) {
            randomUnconditionalPunctuationMarks(";_.,", 10)
        }.text shouldMatch "[;_.,]{10}"

        buildLesson("", 10) {
            randomUnconditionalPunctuationMarks(";_.,", 3)
        }.text shouldMatch "[;_., ]{10}"
    }

    @Test
    fun `buildLesson randomUnconditionalPunctuationMarks empty input`() {
        buildLesson("", 10) {
            randomUnconditionalPunctuationMarks("", 10)
        }.text shouldBe ""
    }

    @Test
    fun `buildLesson randomUnconditionalPunctuationMarks line length range test`() {
        buildLesson("", -10) {
            randomUnconditionalPunctuationMarks(";_.,", 10)
        }.text shouldHaveLength 0

        buildLesson("", -1) {
            randomUnconditionalPunctuationMarks(";_.,", 10)
        }.text shouldHaveLength 0

        buildLesson("", 0) {
            randomUnconditionalPunctuationMarks(";_.,", 10)
        }.text shouldHaveLength 0

        buildLesson("", 1) {
            randomUnconditionalPunctuationMarks(";_.,", 10)
        }.text shouldHaveLength 1

        buildLesson("", 10) {
            randomUnconditionalPunctuationMarks(";_.,", 10)
        }.text shouldHaveLength 10
    }

    @Test
    fun `buildLesson randomUnconditionalPunctuationMarks segment length range test`() {
        buildLesson("", 10) {
            randomUnconditionalPunctuationMarks(";_.,", -10)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            randomUnconditionalPunctuationMarks(";_.,", -1)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            randomUnconditionalPunctuationMarks(";_.,", 0)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            randomUnconditionalPunctuationMarks(";_.,", 1)
        }.text shouldMatch "[;_., ]{9}" // e.g. "; , , . _ " 10 -> trimmed to "; , , . _" 9

        buildLesson("", 10) {
            randomUnconditionalPunctuationMarks(";_.,", 10)
        }.text shouldHaveLength 10
    }

    @Test
    fun `buildLesson randomUnconditionalPunctuationMarks trimmed`() {
        val lesson = buildLesson("", 10) {
            randomUnconditionalPunctuationMarks(";_.,", 1)
        }
        lesson.text.take(1) shouldNotMatch "\\s"
        lesson.text.takeLast(1) shouldNotMatch "\\s"
    }

    @Test
    fun `buildLesson wordsWithUnconditionalPunctuationMarksMultiline happy`() {
        buildLesson("", 10) {
            wordsWithUnconditionalPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), ".", 10)
        }.text.split("\\s".toRegex()).forAll { it.startsWith('.') || it.endsWith('.') }
    }

    @Test
    fun `buildLesson wordsWithUnconditionalPunctuationMarksMultiline empty input`() {
        buildLesson("", 10) {
            wordsWithUnconditionalPunctuationMarksMultiline(emptyList(), "", 10)
        }.text shouldBe ""
    }

    @Test
    fun `buildLesson wordsWithUnconditionalPunctuationMarksMultiline line length range test`() {
        buildLesson("", -10) {
            wordsWithUnconditionalPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), ".", 10)
        }.text.split('\n').forAll { line -> line shouldHaveLength 0 }

        buildLesson("", -1) {
            wordsWithUnconditionalPunctuationMarksMultiline(listOf("c", "hi", "are", "you", "ready"), ".", 10)
        }.text.split('\n').forAll { line -> line shouldHaveLength 0 }

        buildLesson("", 0) {
            wordsWithUnconditionalPunctuationMarksMultiline(listOf("c", "hi", "are", "you", "ready"), ".", 10)
        }.text.split('\n').forAll { line -> line shouldHaveLength 0 }

        buildLesson("", 1) {
            wordsWithUnconditionalPunctuationMarksMultiline(listOf("c", "hi", "are", "you", "ready"), ".", 10)
        }.text.split('\n').forAll { line -> line shouldHaveLength 0 } // shortest is ".c" or "c."

        buildLesson("", 10) {
            wordsWithUnconditionalPunctuationMarksMultiline(listOf("c", "hi", "are", "you", "ready"), ".", 10)
        }.text.split('\n').forAll { line -> line shouldHaveMaxLength 10 }
    }

    @Test
    fun `buildLesson wordsWithUnconditionalPunctuationMarksMultiline word count range test`() {
        buildLesson("", 10) {
            wordsWithUnconditionalPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), ".", -10)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            wordsWithUnconditionalPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), ".", -1)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            wordsWithUnconditionalPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), ".", 0)
        }.text shouldHaveLength 0

        buildLesson("", 10) {
            wordsWithUnconditionalPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), ".", 1)
        }.text shouldBeIn listOf("hi.", ".hi")

        buildLesson("", 10) {
            wordsWithUnconditionalPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), ".", 10)
        }.text.split("\\s".toRegex()) shouldHaveSize 10
    }

    @Test
    fun `buildLesson wordsWithUnconditionalPunctuationMarksMultiline trimmed`() {
        buildLesson("", 10) {
            wordsWithUnconditionalPunctuationMarksMultiline(listOf("hi", "are", "you", "ready"), "_.,", 10)
        }.text.split("\\s").forAll { line ->
            line.take(1) shouldNotMatch "\\s"
            line.takeLast(1) shouldNotMatch "\\s"
        }
    }
}