package org.example

import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.stringPattern
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
    fun `unpack happy`() {
        unpack("{WW}") shouldBe "{}"
        unpack("[sch]") shouldBe "sch"
        unpack("apple") shouldBe "apple"
    }

    @Test
    fun `unpack returns original input, if it is not packed`() {
        unpack("apple") shouldBe "apple"
    }

    @Test
    fun `unpack unpacks ww strings`() {
        unpack("{WW}") shouldBe "{}"
    }

    @Test
    fun `unpack unpacks letter groups`() {
        unpack("[sch]") shouldBe "sch"
    }

    @Test
    fun `unpack empty input`() {
        unpack("") shouldBe ""
    }

    @Test
    fun `unpack input contains whitespaces`() {
        unpack("a b c ") shouldBe "a b c "
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
    fun `lessonWords if lesson symbols is a letter group, take words from history which contain the group`() {
        val dict = setOf("apple", "letter", "lesson", "china", "brain")
        val charsHistory = "ialetrsonch"
        val lessonSymbols = "[tt]"
        dict.lessonWords(charsHistory, lessonSymbols) shouldContainExactlyInAnyOrder listOf("letter")
    }

    @Test
    fun `ww regex should match WW strings`() {
        "WW,." shouldMatch wwRegex
        "+WW" shouldMatch wwRegex
        "[WW]" shouldMatch wwRegex
        "WW" shouldMatch wwRegex
    }

    @Test
    fun `ww regex should not match anything else`() {
        "" shouldNotMatch letterGroupRegex.pattern
        "W" shouldNotMatch letterGroupRegex.pattern
        ",." shouldNotMatch letterGroupRegex.pattern
        "abc" shouldNotMatch letterGroupRegex.pattern
        "1" shouldNotMatch letterGroupRegex.pattern
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
    fun `ww should return the first WW part only`() {
        ww("(WW)(WW)") shouldBe "(WW)("
        ww("(WW)';[{(WW)}]") shouldBe "(WW)';[{("
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
    fun `wwUnpack should remove the WW in a WW string and return only the symbols`() {
        wwUnpack("(WW)") shouldBe "()"
        wwUnpack("WW)=/\\") shouldBe ")=/\\"
        wwUnpack("_}*?/{(WW") shouldBe "_}*?/{("
        wwUnpack("WW") shouldBe ""
        wwUnpack("W") shouldBe ""
        wwUnpack("") shouldBe ""
    }

    @Test
    fun `wwUnpack ignore everything else`() {
        wwUnpack("abcABC(WW)") shouldBe "()"
        wwUnpack("abcABC(WW)1abcABC") shouldBe "()"
        wwUnpack("abc:ABC(WW)1a,bc_ABC") shouldBe "()"
        wwUnpack("abcABCWW") shouldBe ""
        wwUnpack("1WW1") shouldBe ""
        wwUnpack("1abcWW") shouldBe ""
        wwUnpack("()=abcWW") shouldBe ""
    }

    @Test
    fun `letterGroupRegex should match letter groups`() {
        "[sch]" shouldMatch letterGroupRegex
    }

    @Test
    fun `letterGroupRegex should not match anything else`() {
        "[sch]a" shouldNotMatch letterGroupRegex.pattern
        "a[sch]" shouldNotMatch letterGroupRegex.pattern
        "[]" shouldNotMatch letterGroupRegex.pattern
        "abc" shouldNotMatch letterGroupRegex.pattern
        "1" shouldNotMatch letterGroupRegex.pattern
        "" shouldNotMatch letterGroupRegex.pattern
    }

    @Test
    fun `letterGroup should return groups of letters`() {
        letterGroup("[sch]") shouldBe "[sch]"
        letterGroup("[123]") shouldBe ""
        letterGroup("[%';]") shouldBe ""
    }

    @Test
    fun `letterGroup should return the first group`() {
        letterGroup("[sch][ch][ss][tt]") shouldBe "[sch]"
        letterGroup("[tt][ch][ss]") shouldBe "[tt]"
    }

    @Test
    fun `letterGroup should not return empty an group`() {
        letterGroup("[]") shouldBe ""
    }

    @Test
    fun `letterGroup should not return groups of non-letters`() {
        letterGroup("[123]") shouldBe ""
        letterGroup("[%';]") shouldBe ""
    }

    @Test
    fun `letterGroup should not return groups containing non-letters`() {
        letterGroup("[sch12]") shouldBe ""
        letterGroup("[sch%';]") shouldBe ""
    }

    @Test
    fun `letterGroup should ignore surrounding square brackets`() {
        letterGroup("[[sch]]") shouldBe "[sch]"
        letterGroup("[[[sch]]]") shouldBe "[sch]"
    }

    @Test
    fun `letterGroup WW part can not be a letter group`() {
        letterGroup("[WW]") shouldBe ""
        letterGroup("{[WW]}") shouldBe ""
    }


    @Test
    fun `letterGroupUnpack should return the letters of the group`() {
        letterGroupUnpack("[sch]") shouldBe "sch"
    }

    @Test
    fun `letterGroupUnpack should return the first group`() {
        letterGroupUnpack("[sch][ch][ss][tt]") shouldBe "sch"
        letterGroupUnpack("[tt][ch][ss]") shouldBe "tt"
    }

    @Test
    fun `letterGroupUnpack should not return empty an group`() {
        letterGroupUnpack("[]") shouldBe ""
    }

    @Test
    fun `letterGroupUnpack should not return groups of non-letters`() {
        letterGroupUnpack("[123]") shouldBe ""
        letterGroupUnpack("[%';]") shouldBe ""
    }

    @Test
    fun `letterGroupUnpack should not return groups containing non-letters`() {
        letterGroupUnpack("[sch12]") shouldBe ""
        letterGroupUnpack("[sch%';]") shouldBe ""
    }

    @Test
    fun `letterGroupUnpack should ignore surrounding square brackets`() {
        letterGroupUnpack("[[sch]]") shouldBe "sch"
        letterGroupUnpack("[[[sch]]]") shouldBe "sch"
    }

    @Test
    fun `letterGroupUnpack WW part can not be a letter group`() {
        letterGroupUnpack("[WW]") shouldBe ""
        letterGroupUnpack("{[WW]}") shouldBe ""
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
        letters("abc(WW)abc(WW)';abc") shouldBe "abcabcabc"
        letters("abc(WW)=;def") shouldBe "abcdef"
    }

    @Test
    fun `letters should ignore letter groups`() {
        letters("abc[sch]def") shouldBe "abcdef"
        letters("abcdef[sch]") shouldBe "abcdef"
        letters("[sch]abcdef") shouldBe "abcdef"
        letters("[sch]abcdef[tt]") shouldBe "abcdef"
        letters("abc[]def") shouldBe "abcdef"
        letters("abcdef[]") shouldBe "abcdef"
        letters("[]abcdef") shouldBe "abcdef"
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
    fun `symbolsPerGenerator table test`() {
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

    @Test
    fun `invokeConcatSingleLine returns concatenated generators results as single line, whitespace separated`() {
        fun repeatA(n: Int) = "a".repeat(n)
        fun repeatB(n: Int) = "b".repeat(n)
        fun repeatC(n: Int) = "c".repeat(n)
        val generators = listOf(::repeatA, ::repeatB, ::repeatC)
        val result = invokeConcatSingleLine(10, generators)
        result shouldBe "aaa bbb ccc a"
    }

    @Test
    fun `invokeConcatSingleLine on single generator return the generators result with length of symbols-per-lesson`() {
        fun repeatA(n: Int) = "a".repeat(n)
        val generators = listOf(::repeatA)
        invokeConcatSingleLine(10, generators) shouldBe "aaaaaaaaaa"
    }

    @Test
    fun `invokeConcatSingleLine on many generators return all generators results separated by whitespace`() {
        fun repeatA(n: Int) = "a".repeat(n)
        fun repeatB(n: Int) = "b".repeat(n)
        val generators = listOf(::repeatA, ::repeatB)
        invokeConcatSingleLine(10, generators) shouldBe "aaaaa bbbbb"
    }

    @Test
    fun `invokeConcatSingleLine on many generators each generators result has length of symbols-per-lesson divided by number-of-generators`() {
        fun repeatA(n: Int) = "a".repeat(n)
        fun repeatB(n: Int) = "b".repeat(n)
        fun repeatC(n: Int) = "c".repeat(n)
        val generators = listOf(::repeatA, ::repeatB, ::repeatC)
        val lines = invokeConcatSingleLine(9, generators).split(" ")
        lines[0] shouldHaveLength 3 // aaa
        lines[1] shouldHaveLength 3 // bbb
        lines[2] shouldHaveLength 3 // ccc
    }

    @Test
    fun `invokeConcatSingleLine re-invoke generators if necessary to get as many symbols as symbols-per-lesson`() {
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

    @Test
    fun `invokeConcatSingleLine return empty string when generators have empty output`() {
        val generators = listOf{_: Int -> ""}
        invokeConcatSingleLine(3, generators) shouldBe ""
    }

    @Test
    fun `List of TextGenerator invokeConcat returns concatenated generators results as single line, whitespace separated`() {
        fun repeatA(n: Int) = "a".repeat(n)
        fun repeatB(n: Int) = "b".repeat(n)
        fun repeatC(n: Int) = "c".repeat(n)
        val generators = listOf(::repeatA, ::repeatB, ::repeatC)
        val result = generators.invokeConcat(3, 9)
        result shouldBe "aaa bbb ccc"
    }

    @Test
    fun `List of TextGenerator invokeConcat re-invoke generators if necessary to get the total number of symbols`() {
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

    @Test
    fun `List of TextGenerator invokeConcat return empty string when generators have empty output`() {
        val generators = listOf{_: Int -> ""}
        generators.invokeConcat(3, 10) shouldBe ""
    }

    @Test
    fun `StringBuilder symbolsCount counts non-whitespace characters`() {
        StringBuilder(". a b c ] 1").symbolsCount() shouldBe 6
    }

    @Test
    fun `String takeCountingSymbols takes until there are as many as total-symbols non-whitespace chars in the result`() {
        ". a b c ] 1".takeCountingSymbols(3) shouldBe ". a b"
    }

    @Test
    fun `String takeCountingSymbols return empty string when total-symbols is zero or negative`() {
        ". a b c ] 1".takeCountingSymbols(0) shouldBe ""
        ". a b c ] 1".takeCountingSymbols(-1) shouldBe ""
    }

    @Test
    fun `String takeCountingSymbols return empty string when source string is empty`() {
        "".takeCountingSymbols(0) shouldBe ""
    }

    @Test
    fun `addLineBreaks breaks the input string into lines of max length line-length`() {
        val result = addLineBreaks("abcdef abc abc abcdef abc", 20, 5)
        result shouldBe """
            abcde
            f abc
            abc a
            bcdef
            ab
        """.trimIndent()
        val lines = result.split("\n")
        lines[0] shouldHaveMaxLength 5
        lines[1] shouldHaveMaxLength 5
        lines[2] shouldHaveMaxLength 5
        lines[3] shouldHaveMaxLength 5
        lines[4] shouldHaveMaxLength 5
    }

    @Test
    fun `addLineBreaks property test`() {
        val arbitraryBuilder = Arb.stringPattern("[A-Za-z0-9.,;\\[\\]{}\t]{30}")
        repeat(20) {
            val lines = addLineBreaks(arbitraryBuilder.next(), 20, 5).split("\n")
            lines shouldHaveAtLeastSize 4
            lines[0] shouldHaveMinLength 4
            lines[0] shouldHaveMaxLength 5
            lines[1] shouldHaveMinLength 4
            lines[1] shouldHaveMaxLength 5
            lines[2] shouldHaveMinLength 4
            lines[2] shouldHaveMaxLength 5
            lines[3] shouldHaveMinLength 4
            lines[3] shouldHaveMaxLength 5
        }
    }

    @Test
    fun `addLineBreaks return empty string when input string is empty`() {
        addLineBreaks("", 20, 5) shouldBe ""
    }

    @Test
    fun `addLineBreaks return empty string when symbols-total is zero or negative`() {
        addLineBreaks("abc abc abc", 0, 10) shouldBe ""
        addLineBreaks("abc abc abc", -1, 10) shouldBe ""
    }

    @Test
    fun `addLineBreaks return empty string when line-length is zero or negative`() {
        addLineBreaks("abc abc abc", 10, 0) shouldBe ""
        addLineBreaks("abc abc abc", 10, -1) shouldBe ""
    }

    @Test
    fun `buildLesson repeatSymbols happy`() {
        buildLesson(lineLength = 20, symbolsPerLesson = 10) {
            repeatSymbols("ab", 10)
        }.text shouldBe "ababababab"

        buildLesson(lineLength = 20, symbolsPerLesson = 10) {
            repeatSymbols("ab", 3)
        }.text shouldBe "aba bab aba b"
    }

    @Test
    fun `buildLesson repeatSymbols empty input`() {
        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            repeatSymbols("", 10)
        }.text.length shouldBe 0
    }

    @Test
    fun `buildLesson repeatSymbols line length range test`() {
        buildLesson(lineLength = -10, symbolsPerLesson = 10) {
            repeatSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = -1, symbolsPerLesson = 10) {
            repeatSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 0, symbolsPerLesson = 10) {
            repeatSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 1, symbolsPerLesson = 10) {
            repeatSymbols("abc", 10)
        }.text.count { !it.isWhitespace() } shouldBe 10

        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            repeatSymbols("abc", 10)
        }.text shouldHaveLength 10
    }

    @Test
    fun `buildLesson repeatSymbols segment length range test`() {
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

    @Test
    fun `buildLesson repeatSymbols symbols per lesson range test`() {
        buildLesson(lineLength = 10, symbolsPerLesson = -100) {
            repeatSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 10, symbolsPerLesson = -10) {
            repeatSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 10, symbolsPerLesson = -1) {
            repeatSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 10, symbolsPerLesson = 0) {
            repeatSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 10, symbolsPerLesson = 1) {
            repeatSymbols("abc", 10)
        }.text shouldHaveLength 1

        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            repeatSymbols("abc", 10)
        }.text.count { !it.isWhitespace() } shouldBe 10

        val text100 = buildLesson(lineLength = 10, symbolsPerLesson = 100) {
            repeatSymbols("abc", 10)
        }.text
        text100.split("\n") shouldHaveSize 10
        text100.count { !it.isWhitespace() } shouldBe 100
    }

    @Test
    fun `buildLesson repeatSymbols trimmed`() {
        val lesson = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            repeatSymbols("abc", 1)
        }
        lesson.text shouldNotStartWith "\\s"
        lesson.text shouldNotEndWith "\\s"
    }

    @Test
    fun `buildLesson alternatingSymbols happy`() {
        buildLesson(lineLength = 13, symbolsPerLesson = 10) {
            alternatingSymbols("abc", 2)
        }.text shouldBe "aa bb cc aa bb"
    }

    @Test
    fun `buildLesson alternatingSymbols empty input`() {
        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            alternatingSymbols("", 10)
        }.text.length shouldBe 0
    }

    @Test
    fun `buildLesson alternatingSymbols line length range test`() {
        buildLesson(lineLength = -10, symbolsPerLesson = 10) {
            alternatingSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = -1, symbolsPerLesson = 10) {
            alternatingSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 0, symbolsPerLesson = 10) {
            alternatingSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 1, symbolsPerLesson = 10) {
            alternatingSymbols("abc", 10)
        }.text.count { !it.isWhitespace() } shouldBe 10

        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            alternatingSymbols("abc", 10)
        }.text shouldHaveLength 10
    }

    @Test
    fun `buildLesson alternatingSymbols segment length range test`() {
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

    @Test
    fun `buildLesson alternatingSymbols symbols per lesson range test`() {
        buildLesson(lineLength = 10, symbolsPerLesson = -100) {
            alternatingSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 10, symbolsPerLesson = -10) {
            alternatingSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 10, symbolsPerLesson = -1) {
            alternatingSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 10, symbolsPerLesson = 0) {
            alternatingSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 10, symbolsPerLesson = 1) {
            alternatingSymbols("abc", 10)
        }.text shouldHaveLength 1

        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            alternatingSymbols("abc", 10)
        }.text.count { !it.isWhitespace() } shouldBe 10

        val text100 = buildLesson(lineLength = 10, symbolsPerLesson = 100) {
            alternatingSymbols("abc", 10)
        }.text
        text100.split("\n") shouldHaveSize 10
        text100.count { !it.isWhitespace() } shouldBe 100
    }

    @Test
    fun `buildLesson alternatingSymbols trimmed`() {
        val lesson = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            alternatingSymbols("abc", 1)
        }
        lesson.text shouldNotStartWith "\\s"
        lesson.text shouldNotEndWith "\\s"
    }

    @Test
    fun `buildLesson properties assignment`() {
        val lesson = buildLesson("my lesson", 10, 10,"ab") {
            repeatSymbols("a", 10)
        }
        lesson.title shouldBe "my lesson"
        lesson.newCharacters shouldBe "ab"
        lesson.text.length shouldBe 10
    }

    @Test
    fun `buildLesson line length smaller than symbols per lesson`() {
        buildLesson(lineLength = 10, symbolsPerLesson = 20) {
            repeatSymbols("ab", 10)
        }.text shouldBe """
            ababababab
            ababababab
        """.trimIndent()

        buildLesson(lineLength = 10, symbolsPerLesson = 20) {
            repeatSymbols("ab", 2)
        }.text shouldBe """
            ab ab ab a
            b ab ab ab
            ab ab ab
        """.trimIndent()

        buildLesson(lineLength = 10, symbolsPerLesson = 20) {
            repeatSymbols("ab", 1)
        }.text shouldBe """
            a b a b a
            b a b a b
            a b a b a
            b a b a b
        """.trimIndent()
    }

    @Test
    fun `buildLesson line length greater than symbols per lesson`() {
        buildLesson(lineLength = 20, symbolsPerLesson = 10) {
            repeatSymbols("ab", 10)
        }.text shouldBe "ababababab"

        buildLesson(lineLength = 20, symbolsPerLesson = 10) {
            repeatSymbols("ab", 2)
        }.text shouldBe "ab ab ab ab ab"

        buildLesson(lineLength = 20, symbolsPerLesson = 10) {
            repeatSymbols("ab", 1)
        }.text shouldBe "a b a b a b a b a b"
    }

    @Test
    fun `buildLesson line length equals symbols per lesson`() {
        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            repeatSymbols("ab", 10)
        }.text shouldBe "ababababab"

        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            repeatSymbols("ab", 2)
        }.text shouldBe """
            ab ab ab a
            b ab
        """.trimIndent()

        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            repeatSymbols("ab", 1)
        }.text shouldBe """
            a b a b a
            b a b a b
        """.trimIndent()
    }

    @Test
    fun `buildLesson shuffledSymbols happy`() {
        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            shuffledSymbols("ab", 10)
        }.text shouldMatch "[ab]{10}"

        val text = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            repeatSymbols("ab", 1)
        }.text
        text.split("\n")[0] shouldMatch "[ab ]{9}" // a b a b a
        text.split("\n")[1] shouldMatch "[ab ]{9}" // b a b a b
    }

    @Test
    fun `buildLesson shuffledSymbols empty input`() {
        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            shuffledSymbols("", 10)
        }.text.length shouldBe 0
    }

    @Test
    fun `buildLesson shuffledSymbols line length range test`() {
        buildLesson(lineLength = -10, symbolsPerLesson = 10) {
            shuffledSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = -1, symbolsPerLesson = 10) {
            shuffledSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 0, symbolsPerLesson = 10) {
            shuffledSymbols("abc", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 1, symbolsPerLesson = 10) {
            shuffledSymbols("abc", 10)
        }.text.count { !it.isWhitespace() } shouldBe 10

        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            shuffledSymbols("abc", 10)
        }.text shouldHaveLength 10
    }

    @Test
    fun `buildLesson shuffledSymbols segment length range test`() {
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

    @Test
    fun `buildLesson shuffledSymbols trimmed`() {
        val lesson = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            shuffledSymbols("abc", 1)
        }
        lesson.text shouldNotStartWith "\\s"
        lesson.text shouldNotEndWith "\\s"
    }

    @Test
    fun `buildLesson words happy`() {
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

    @Test
    fun `buildLesson words empty input`() {
        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            words(emptyList())
        }.text shouldBe ""
    }

    @Test
    fun `buildLesson words line length range test`() {
        buildLesson(lineLength = -10, symbolsPerLesson = 10) {
            words(listOf("hi", "are", "you", "ready"))
        }.text shouldHaveLength 0

        buildLesson(lineLength = -1, symbolsPerLesson = 10) {
            words(listOf("c", "hi", "are", "you", "ready"))
        }.text shouldHaveLength 0

        buildLesson(lineLength = 0, symbolsPerLesson = 10) {
            words(listOf("c", "hi", "are", "you", "ready"))
        }.text shouldHaveLength 0

        buildLesson(lineLength = 1, symbolsPerLesson = 10) {
            words(listOf("c", "hi", "are", "you", "ready"))
        }.text.split('\n') shouldHaveSize 10

        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            words(listOf("c", "hi", "are", "you", "ready"))
        }.text.count { !it.isWhitespace() } shouldBe 10
    }

    @Test
    fun `buildLesson words trimmed`() {
        val text = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            words(listOf("hi", "are", "you", "ready"))
        }.text
        text.split("\\s".toRegex()) shouldHaveSize 4
        text.split("\\s".toRegex()).forAll { line ->
            line shouldNotStartWith "\\s"
            line shouldNotEndWith "\\s"
        }
    }

    @Test
    fun `buildLesson randomLeftRightPunctuationMarks happy`() {
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

    @Test
    fun `buildLesson randomLeftRightPunctuationMarks empty input`() {
        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            randomLeftRightPunctuationMarks("", 10)
        }.text shouldBe ""
    }

    @Test
    fun `buildLesson randomLeftRightPunctuationMarks line length range test`() {
        buildLesson(lineLength = -10, symbolsPerLesson = 10) {
            randomLeftRightPunctuationMarks("[{WW}].,", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = -1, symbolsPerLesson = 10) {
            randomLeftRightPunctuationMarks("[{WW}].,", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 0, symbolsPerLesson = 10) {
            randomLeftRightPunctuationMarks("[{WW}].,", 10)
        }.text shouldHaveLength 0

        buildLesson(lineLength = 1, symbolsPerLesson = 10) {
            randomLeftRightPunctuationMarks("[{WW}].,", 10)
        }.text.count { !it.isWhitespace() } shouldBe 10

        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            randomLeftRightPunctuationMarks("[{WW}].,", 10)
        }.text shouldHaveLength 10
    }

    @Test
    fun `buildLesson randomLeftRightPunctuationMarks segment length range test`() {
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

    @Test
    fun `buildLesson randomLeftRightPunctuationMarks trimmed`() {
        val lesson = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            randomLeftRightPunctuationMarks("[{WW}].,", 1)
        }
        lesson.text shouldNotStartWith "\\s"
        lesson.text shouldNotEndWith "\\s"
    }

    @Test
    fun `buildLesson wordsWithLeftRightPunctuationMarks happy`() {
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

    @Test
    fun `buildLesson wordsWithLeftRightPunctuationMarks empty input`() {
        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            wordsWithLeftRightPunctuationMarks(emptyList(), "")
        }.text shouldBe ""
    }

    @Test
    fun `buildLesson wordsWithLeftRightPunctuationMarks line length range test`() {
        buildLesson(lineLength = -10, symbolsPerLesson = 10) {
            wordsWithLeftRightPunctuationMarks(listOf("hi", "are", "you", "ready"), "[WW]")
        }.text shouldHaveLength 0

        buildLesson(lineLength = -1, symbolsPerLesson = 10) {
            wordsWithLeftRightPunctuationMarks(listOf("c", "hi", "are", "you", "ready"), "[WW]")
        }.text shouldHaveLength 0

        buildLesson(lineLength = 0, symbolsPerLesson = 10) {
            wordsWithLeftRightPunctuationMarks(listOf("c", "hi", "are", "you", "ready"), "[WW]")
        }.text shouldHaveLength 0

        val text = buildLesson(lineLength = 1, symbolsPerLesson = 10) {
            wordsWithLeftRightPunctuationMarks(listOf("c", "hi", "are", "you", "ready"), "[WW]")
        }.text
        text.split('\n') shouldHaveSize 10
        text.split('\n').forAll { line -> line shouldHaveLength 1 }

        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            wordsWithLeftRightPunctuationMarks(listOf("c", "hi", "are", "you", "ready"), "[WW]")
        }.text.count { !it.isWhitespace() } shouldBe 10
    }

    @Test
    fun `buildLesson wordsWithLeftRightPunctuationMarks trimmed`() {
        val text = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            wordsWithLeftRightPunctuationMarks(listOf("hi", "are", "you", "ready"), "[{WW}].,")
        }.text
        text.split("\\s".toRegex()) shouldHaveAtLeastSize 3
        text.split("\\s".toRegex()).forAll { line ->
            line shouldNotStartWith "\\s"
            line shouldNotEndWith "\\s"
        }
    }

    @Test
    fun `buildLesson wordsWithUnconditionalPunctuationMarksMultiline happy`() {
        val text = buildLesson(lineLength = 20, symbolsPerLesson = 34) {
            wordsWithUnconditionalPunctuationMarks(listOf("hi", "are", "you", "ready"), ".")
        }.text
        text.count { !it.isWhitespace() } shouldBe 34
        text.split("\\s".toRegex())
            .forAll { it shouldBeIn setOf(
                "hi.", ".hi",
                "are.", ".are",
                "you.", ".you",
                "ready.", ".ready")
            }

        val text2 = buildLesson(lineLength = 20, symbolsPerLesson = 34) {
            wordsWithUnconditionalPunctuationMarks(listOf("hi", "are", "you", "ready"), "[WW]")
        }.text
        text2.count { !it.isWhitespace() } shouldBe 34
        text2.replace("\n", "")
            .split("\\s".toRegex())
            .forAll { it shouldBeIn setOf("[hi]", "[are]", "[you]", "[ready]") }
    }

    @Test
    fun `buildLesson wordsWithUnconditionalPunctuationMarks empty word list`() {
        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            wordsWithUnconditionalPunctuationMarks(emptyList(), ".,;")
        }.text shouldHaveLength 0
    }

    @Test
    fun `buildLesson wordsWithUnconditionalPunctuationMarks no punctuation marks`() {
        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            wordsWithUnconditionalPunctuationMarks(listOf("hi", "are", "you", "ready"), "")
        }.text shouldHaveLength 0
    }

    @Test
    fun `buildLesson wordsWithUnconditionalPunctuationMarks line length range test`() {
        buildLesson(lineLength = -10, symbolsPerLesson = 10) {
            wordsWithUnconditionalPunctuationMarks(listOf("hi", "are", "you", "ready"), ".")
        }.text shouldHaveLength 0

        buildLesson(lineLength = -1, symbolsPerLesson = 10) {
            wordsWithUnconditionalPunctuationMarks(listOf("c", "hi", "are", "you", "ready"), ".")
        }.text shouldHaveLength 0

        buildLesson(lineLength = 0, symbolsPerLesson = 10) {
            wordsWithUnconditionalPunctuationMarks(listOf("c", "hi", "are", "you", "ready"), ".")
        }.text shouldHaveLength 0

        val text = buildLesson(lineLength = 1, symbolsPerLesson = 10) {
            wordsWithUnconditionalPunctuationMarks(listOf("c", "hi", "are", "you", "ready"), ".")
        }.text
        text.split('\n') shouldHaveSize 10
        text.split('\n').forAll { line -> line shouldHaveLength 1 }

        buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            wordsWithUnconditionalPunctuationMarks(listOf("c", "hi", "are", "you", "ready"), ".")
        }.text.count { !it.isWhitespace() } shouldBe 10
    }

    @Test
    fun `buildLesson wordsWithUnconditionalPunctuationMarks trimmed`() {
        val text = buildLesson(lineLength = 10, symbolsPerLesson = 10) {
            wordsWithUnconditionalPunctuationMarks(listOf("hi", "are", "you", "ready"), "_.,")
        }.text
        text.split("\\s".toRegex()) shouldHaveAtLeastSize 3
        text.split("\\s".toRegex()).forAll { line ->
            line shouldNotStartWith "\\s"
            line shouldNotEndWith "\\s"
        }
    }

    @Test
    fun `buildLesson apply multiple build steps happy`() {
        buildLesson(lineLength = 30, symbolsPerLesson = 300) {
            repeatSymbols("abc", 2)
            shuffledSymbols("def", 10)
            wordsWithUnconditionalPunctuationMarks(listOf("hi", "are", "you", "ready"), ",.;")
            words(listOf("hi", "are", "you", "ready"))
            shuffledSymbols("yz", 2)
            repeatSymbols("{}", 3)
        }.text.count { !it.isWhitespace() } shouldBe 300
    }
}