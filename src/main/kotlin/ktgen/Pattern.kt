package ktgen

val wwRegex = "\\p{Punct}*WW\\p{Punct}*".toRegex()
fun String.ww(): String = wwRegex.find(this)?.value ?: ""
fun String.unpackWW(): String = replace("WW", "")

val lowerLetters = "[a-züäöß]+".toRegex()
val upperLetters = "[A-ZÜÄÖẞ]+".toRegex()
val lettersRegex = "[${lowerLetters.pattern}${upperLetters.pattern}]+".toRegex()
fun String.letters(): String = lettersRegex
    .findAll(replace(wwRegex, "")
        .replace(letterGroupRegex, "")).joinToString("") { it.value }
fun String.containsLetters() = replace(ww(), "").replace(letterGroup(), "").contains(lettersRegex)

val digitRegex = "\\d+".toRegex()
fun String.areDigits(): Boolean = matches(digitRegex)

val letterGroupRegex = "\\[${lettersRegex.pattern}\\]".toRegex()
fun String.letterGroup(): String = letterGroupRegex.find(replace(ww(), ""))?.value ?: ""
fun String.isLetterGroup(): Boolean = letterGroup().isNotEmpty()
fun String.unpackLetterGroup(): String =
    if (letterGroup().isNotEmpty())
        replace(letterGroup(), letterGroup().replace("[", "").replace("]", ""))
    else replace(ww(), "")

val punctuationRegex = "\\p{Punct}+".toRegex()
fun String.punctuationMarks(): String = punctuationRegex.findAll(
    replace(ww(), "").replace(letterGroup(), "")).joinToString("") { it.value }

fun String.unpack(): String = unpackWW().unpackLetterGroup()