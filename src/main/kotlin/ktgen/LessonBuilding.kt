package ktgen

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random


typealias TextGenerator = (numberOfSymbols: Int) -> String

internal suspend fun invokeConcat(symbolsPerLesson: Int, textGenerators: List<TextGenerator>): String {
    val symbolsPerGenerator = symbolsPerGenerator(symbolsPerLesson, textGenerators.size)
    return textGenerators.invokeConcat(symbolsPerGenerator, symbolsPerLesson)
}

internal fun symbolsPerGenerator(symbolsPerLesson: Int, numberOfGenerators: Int): Int {
    return if (symbolsPerLesson <= 0 || numberOfGenerators <= 0) 0
    else if (symbolsPerLesson < numberOfGenerators) symbolsPerLesson
    else symbolsPerLesson / max(numberOfGenerators, 1)
}

internal suspend fun List<TextGenerator>.invokeConcat(symbolsPerGenerator: Int, symbolsTotal: Int): String {
    if (symbolsPerGenerator <= 0) return ""
    val results = coroutineScope {
        mapIndexed { index, generator ->
            async { index to generator(symbolsPerGenerator) }
        }.awaitAll()
    }.sortedBy { it.first }.map { it.second }

    return buildString {
        while (symbolsCount() < symbolsTotal) {
            results.forEach {
                for (c in it) {
                    append(c)
                    if (symbolsCount() >= symbolsTotal) return@buildString
                }
                append(" ")
            }
            if (symbolsCount() == 0) return ""
        }
    }.trim()
}

internal typealias IndexedLs = MutableMap<Int, MutableCollection<L>>

internal fun IndexedLs.putAppend(index: Int, l: L) {
    if (contains(index)) this[index]!!.add(l)
    else this[index] = mutableListOf(l)
}

abstract class Builder(
    val lessonSpecifications: List<String>,
    val dictionary: Sequence<String>) {
    val indexedLs: IndexedLs = mutableMapOf()
}

class Every(private val n: Int, lessonSpecifications: List<String>, dictionary: Sequence<String>) : Builder(lessonSpecifications, dictionary) {

    fun summaryLesson(buildStep: L.() -> L): Every {
        if (n <= 0) return this
        val chunked = lessonSpecifications.chunked(n)
        val chunkedIndexed = List(lessonSpecifications.size) { index ->
            if ((index + 1) % n == 0) {
                val lastN = chunked[index/n].joinToString("")
                index to lastN
            }
            else null
        }.filterNotNull()
        chunkedIndexed.forEach {
            val (index, symbols) = it
            val symbolsHistoryUpToIndex = lessonSpecifications.subList(0, index).joinToString("")
            indexedLs.putAppend(index, L(LessonKind.Summary, "Summary $symbols", symbols, dictionary, symbolsHistoryUpToIndex).buildStep())
        }
        return this
    }
}

class ForEach(lessonSpecifications: List<String>,
              dictionary: Sequence<String>) : Builder(lessonSpecifications, dictionary) {

    suspend fun lesson(buildStep: L.() -> L): ForEach {
        coroutineScope {
            lessonSpecifications.forEachIndexed { index, symbols ->
                launch {
                    val completeSymbolsHistory = lessonSpecifications.subList(0, index).joinToString("")
                    indexedLs.putAppend(index, L(LessonKind.Regular, symbols, symbols, dictionary, completeSymbolsHistory).buildStep())
                }
            }
        }
        return this
    }
}

internal data class LessonPrototype(
    val position: Int = 0,
    val kind: LessonKind = LessonKind.Regular,
    val symbols: String = "",
    val lesson: Lesson = Lesson()
)

internal class LessonPrototypeComparator : Comparator<LessonPrototype> {

    override fun compare(o1: LessonPrototype, o2: LessonPrototype): Int {
        val position = o1.position - o2.position
        return if (position == 0) lessonKindCompare(o1, o2)
        else position
    }

    private fun lessonKindCompare(o1: LessonPrototype, o2: LessonPrototype): Int {
        return if (o1.kind == LessonKind.Regular && o2.kind == LessonKind.Regular) 0
        else if (o1.kind == LessonKind.Summary && o2.kind == LessonKind.Summary) 0
        else if (o1.kind == LessonKind.Regular && o2.kind == LessonKind.Summary) -1
        else 1
    }
}

internal suspend fun createLessonPrototypes(
    indexedLs: IndexedLs,
    lineLength: Int,
    symbolsPerLesson: Int): List<LessonPrototype> = coroutineScope {
    if (lineLength <= 0 || symbolsPerLesson <= 0) emptyList()
    else indexedLs.map { entry ->
        async {
            val (index, ls) = entry
            ls.mapNotNull { l ->
                val text = calculateLessonText(lineLength, symbolsPerLesson, l)
                if (text.isNotEmpty()) LessonPrototype(index, l.kind, l.symbols, Lesson(title = l.title, text = text))
                else null
            }
        }
    }.awaitAll().flatten()
}

internal suspend fun calculateLessonText(lineLength: Int, symbolsPerLesson: Int, l: L): String {
    if (l.textGenerators.isEmpty() || lineLength <= 0 || symbolsPerLesson <= 0) return ""
    val textSingleLine = invokeConcat(symbolsPerLesson, l.textGenerators)
    if (textSingleLine.isEmpty()) return ""
    return textSingleLine.toTextBlock(symbolsPerLesson, lineLength)
}

internal fun Collection<LessonPrototype>.setNewCharacters(): List<LessonPrototype> {
    val characterHistory = StringBuilder()
    return map { prototype ->
        val newCharacters = characterHistory.newCharacters(prototype.symbols)
        prototype.copy(lesson = prototype.lesson.copy(newCharacters = newCharacters))
    }
}

internal fun StringBuilder.newCharacters(symbols: String): String {
    val new = symbols.unpack().filter { !toString().contains(it) }
    this.append(new)
    return new
}

internal fun List<LessonPrototype>.filter(lessonFilter: Collection<LessonFilter>): List<LessonPrototype> {
    return filterIndexed { i, current ->
        val last = this[max(i-1, 0)]
        val introducesNewCharacters = current.lesson.newCharacters.isNotEmpty()
        val isSummary = current.kind == LessonKind.Summary
        if (introducesNewCharacters || isSummary) true
        else lessonFilter.all { it(last.lesson, current.lesson) }
    }
}

internal fun Collection<LessonPrototype>.enumerate(): List<LessonPrototype> {
    val lessonCtr = generateSequence(1) { it + 1 }.iterator()
    return map { prototype ->
        prototype.copy(lesson = prototype.lesson.copy(title = "${lessonCtr.next()}: ${prototype.lesson.title}"))
    }
}

class LessonBuilder(private val lineLength: Int,
                    private val symbolsPerLesson: Int,
                    lessonSpecifications: List<String>,
                    dictionary: Sequence<String>) : Builder(lessonSpecifications, dictionary) {

    val lessons = mutableListOf<Lesson>()

    private val lessonFilter = mutableListOf<LessonFilter>()

    fun withLessonFilter(vararg filter: LessonFilter): LessonBuilder {
        lessonFilter.addAll(filter)
        return this
    }

    suspend fun apply(init: suspend LessonBuilder.() -> LessonBuilder): LessonBuilder {
        init()
        val lessonPrototypes = createLessonPrototypes(indexedLs, lineLength, symbolsPerLesson)
        val sorted = lessonPrototypes.sortedWith(LessonPrototypeComparator())
        val withNewCharactersInfo = sorted.setNewCharacters()
        val filtered = withNewCharactersInfo.filter(lessonFilter)
        val enumerated = filtered.enumerate()

        enumerated.forEach {
            lessons.add(it.lesson)
        }
        return this
    }

    suspend fun forEachLessonSpecification(init: suspend ForEach.() -> Unit): LessonBuilder {
        val forEach = ForEach(lessonSpecifications, dictionary)
        forEach.init()
        forEach.indexedLs.forEach { (index, ls) ->
            ls.forEach { l -> indexedLs.putAppend(index, l) }
        }
        return this
    }

    fun every(nLessonSpecifications: Int, init: Every.() -> Unit): LessonBuilder {
        val every = Every(nLessonSpecifications, lessonSpecifications, dictionary)
        every.init()
        every.indexedLs.forEach { (index, ls) ->
            ls.forEach { l -> indexedLs.putAppend(index, l) }
        }
        return this
    }
}

enum class LessonKind { Regular, Summary }

class L(
    val kind: LessonKind = LessonKind.Regular,
    val title: String = "",
    val symbols: String = "",
    private val dictionary: Sequence<String> = emptySequence(),
    private val symbolsHistory: String = ""
) {

    val textGenerators = mutableListOf<TextGenerator>()

    fun shuffleSymbols(segmentLength: Int): L {
        textGenerators.add { numberOfSymbols ->
            symbols.unpack().repeat(numberOfSymbols).shuffle().segment(segmentLength)
        }
        return this
    }

    fun repeatSymbols(segmentLength: Int): L {
        textGenerators.add { numberOfSymbols ->
            symbols.unpack().repeat(numberOfSymbols).segment(segmentLength)
        }
        return this
    }

    fun alternateSymbols(segmentLength: Int): L {
        textGenerators.add { numberOfSymbols ->
            val stretched = symbols.unpack().map { char -> "$char".repeat(max(segmentLength, 0)) }.joinToString("")
            stretched.repeat(numberOfSymbols).segment(segmentLength)
        }
        return this
    }

    fun words(): L {
        textGenerators.add { numberOfSymbols ->
            val punctuationMarks = symbols.ww().ifEmpty { symbols.punctuationMarks() }
            val words = dictionary.lessonWords(symbolsHistory, symbols)
            if (punctuationMarks.isEmpty()) words.joinRepeat(numberOfSymbols)
            else words.prefixOrAppendPunctuationMarks(punctuationMarks).joinRepeat(numberOfSymbols)
        }
        return this
    }
}

fun Sequence<String>.lessonWords(symbolHistory: String, symbols: String): Collection<String> {
    if (none() || symbols.areDigits()) return emptyList()
    val historyLetters = (symbolHistory+symbols).letters()
    val letters = symbols.letters()
    val isLetterGroup = symbols.isLetterGroup()
    val containsLetters = symbols.containsLetters()
    val filtered = filter {
        it.consistsOf(historyLetters) &&
                if (isLetterGroup)
                    it.contains(symbols.unpackLetterGroup())
                else if (containsLetters)
                    it.containsAny(letters)
                else true
    }.toList()
    val rand = Random.nextInt(0, max(filtered.size - 1, 1))
    return filtered.subList(rand, filtered.size).plus(filtered.subList(0, rand))
}