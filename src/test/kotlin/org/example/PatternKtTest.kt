package org.example

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.string.shouldNotMatch

class PatternKtTest : ConcurrentExpectSpec({
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
})