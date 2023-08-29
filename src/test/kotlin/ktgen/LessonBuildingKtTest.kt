package ktgen

import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.stringPattern

class LessonBuildingKtTest : ConcurrentExpectSpec({

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
    }

    context("lessonWords") {

        expect("symbols itself dont need to be contained in the symbol history") {
            val dict = sequenceOf("ab", "abrc", "abrce", "abrcel")
            val charsHistory = ""
            val lessonSymbols = "ab"
            dict.lessonWords(charsHistory, lessonSymbols).toList() shouldBe listOf("ab")
        }

        expect("first lesson's words consist of the first lessons symbols only") {
            val dict = sequenceOf("ab", "abrc", "abrce", "abrcel")
            val charsHistory = "ab"
            val lessonSymbols = "ab"
            dict.lessonWords(charsHistory, lessonSymbols).toList() shouldBe listOf("ab")
        }

        expect("words from lessons 2..n can contain symbols from all last lesson, but definitely contain at least one of the current lesson's symbols") {
            val dict = sequenceOf("ab", "abrc", "abrce", "abrcel")
            val charsHistory = "abrcel"
            val lessonSymbols = "el"
            dict.lessonWords(charsHistory, lessonSymbols).toList() shouldBe listOf("abrce", "abrcel")
        }

        expect("rotate the dict randomly but always preserve the word order") {
            val dict = sequenceOf("a", "b", "c", "d", "e")
            val charsHistory = "abcde"
            val lessonSymbols = "abcde"

            val words = dict.lessonWords(charsHistory, lessonSymbols).toList()
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
            emptySequence<String>().lessonWords(charsHistory, lessonSymbols) shouldBe emptyList()
        }

        expect("result is empty collection when the history is empty") {
            val dict = sequenceOf("ab", "abrc", "abrcel")
            val charsHistory = ""
            val lessonSymbols = "el"
            dict.lessonWords(charsHistory, lessonSymbols).toList() shouldBe emptyList()
        }

        expect("result is empty collection when symbols are digits") {
            val charsHistory = "abrcel"
            val lessonSymbols = "12"
            emptySequence<String>().lessonWords(charsHistory, lessonSymbols).toList() shouldBe emptyList()
        }

        expect("if lesson symbols contain non-letters, ignore them and take words from history based on letters") {
            val dict = sequenceOf("ad", "b", "tt", "a", "cd", "bd", "xx", "d")
            val charsHistory = "abc_[]d().;"
            val lessonSymbols = "_[]d().;"
            dict.lessonWords(charsHistory, lessonSymbols) shouldContainAll listOf("ad", "cd", "bd", "d")
        }

        expect("if lesson symbols consists of non-letters, ignore them and take words from history") {
            val dict = sequenceOf("ab", "b", "tt", "a", "ba", "xx", "bab")
            val charsHistory = "ab_[]().;"
            val lessonSymbols = "_[]().;"
            dict.lessonWords(charsHistory, lessonSymbols) shouldContainAll listOf("ab", "b", "a", "ba", "bab")
        }

        expect("if lesson symbols is a letter group, take words from history containing the group") {
            val dict = sequenceOf("apple", "letter", "lesson", "china", "brain")
            val charsHistory = "ialetrsonch"
            val lessonSymbols = "[tt]"
            dict.lessonWords(charsHistory, lessonSymbols) shouldContainAll listOf("letter")
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

    context("LessonBuilder") {

        context("lesson building") {

            fun L.exampleBuildStep(): L {
                textGenerators.add { _ -> Arb.stringPattern("[A-Za-z0-9.,;@:<>]{100}\t[A-Za-z0-9.,;@:<>]{100}").next() }
                return this@exampleBuildStep
            }

            expect("A lesson counter is added to the title") {
                val lessonBuilder = LessonBuilder(10, 10, listOf("ab", "cd"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson {
                            exampleBuildStep()
                        }
                    }
                }
                lessonBuilder.lessons[0].title shouldBe "1: ab"
                lessonBuilder.lessons[1].title shouldBe "2: cd"
            }

            expect("only the first lesson for the same symbols introduces them as 'newCharacters'") {
                val lessonBuilder = LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson { exampleBuildStep() }
                        lesson { exampleBuildStep() }
                        lesson { exampleBuildStep() }
                    }
                }
                lessonBuilder.lessons[0].newCharacters shouldBe "ab"
                lessonBuilder.lessons[1].newCharacters shouldBe ""
                lessonBuilder.lessons[2].newCharacters shouldBe ""
            }

            expect("apply all build steps") {
                val lessonBuilder = LessonBuilder(30, 400, listOf("abc"), sequenceOf("apple")).apply {
                    forEachLessonSpecification {
                        lesson {
                            repeatSymbols(3)
                            shuffleSymbols(10)
                            alternateSymbols(2)
                        }
                    }
                }
                val lessonText = lessonBuilder.lessons[0].text
                lessonText shouldContain "(abc )+".toRegex()
                lessonText shouldContain "[abc]{10}".toRegex()
                lessonText shouldContain "[a]{2}\\s[b]{2}\\s[c]{2}".toRegex()
            }

            expect("line-length range test") {
                LessonBuilder(-10, 10, listOf("abc"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson { exampleBuildStep() }
                    }
                }.lessons shouldBe emptyList()

                LessonBuilder(-1, 10, listOf("abc"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson {exampleBuildStep() }
                    }
                }.lessons shouldBe emptyList()

                LessonBuilder(0, 10, listOf("abc"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson {exampleBuildStep() }
                    }
                }.lessons shouldBe emptyList()

                LessonBuilder(1, 10, listOf("abc"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson { exampleBuildStep() }
                    }
                }.lessons[0].text.count { !it.isWhitespace() } shouldBe 10

                LessonBuilder(10, 10, listOf("abc"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson { exampleBuildStep() }
                    }
                }.lessons[0].text shouldHaveLength 10
            }

            expect("symbols-per-lesson range test") {
                LessonBuilder(10, -100, listOf("abc"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson { exampleBuildStep() }
                    }
                }.lessons shouldBe emptyList()

                LessonBuilder(10, -10, listOf("abc"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson { exampleBuildStep() }
                    }
                }.lessons shouldBe emptyList()

                LessonBuilder(10, -1, listOf("abc"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson { exampleBuildStep() }
                    }
                }.lessons shouldBe emptyList()

                LessonBuilder(10, 0, listOf("abc"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson { exampleBuildStep() }
                    }
                }.lessons shouldBe emptyList()

                LessonBuilder(10, 1, listOf("abc"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson { exampleBuildStep() }
                    }
                }.lessons[0].text shouldHaveLength 1

                LessonBuilder(10, 10, listOf("abc"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson { exampleBuildStep() }
                    }
                }.lessons[0].text.count { !it.isWhitespace() } shouldBe 10

                val text100 = LessonBuilder(10, 100, listOf("abc"), emptySequence()).apply {
                    forEachLessonSpecification {
                        lesson { exampleBuildStep() }
                    }
                }.lessons[0].text
                text100.split("\n") shouldHaveAtLeastSize 10
                text100.count { !it.isWhitespace() } shouldBe 100
            }

            context("symbols-per-lesson < line-length") {

                expect("result contains symbols-per-lesson non-whitespace characters") {
                    LessonBuilder(20, 8, listOf("abc"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { exampleBuildStep() }
                        }
                    }.lessons[0].text.count { !it.isWhitespace() } shouldBe 8
                }
            }

            context("symbols-per-lesson > line-length") {

                expect("result is multiline containing symbols-per-lesson non-whitespace characters") {
                    val text = LessonBuilder(8, 20, listOf("abc"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { exampleBuildStep() }
                        }
                    }.lessons[0].text
                    text.split("\n") shouldHaveAtLeastSize 2
                    text.count { !it.isWhitespace() } shouldBe 20
                }
            }

            context("repeatSymbols") {

                expect("repeat the input symbols") {
                    LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { repeatSymbols(10) }
                        }
                    }.lessons[0].text shouldBe "ababababab"
                }

                expect("segment-length range test") {
                    LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { repeatSymbols(-10) }
                        }
                    }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { repeatSymbols(-1) }
                        }
                    }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { repeatSymbols(0) }
                        }
                    }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { repeatSymbols(1) }
                        }
                    }.lessons[0].text shouldBe """
                            a b a b a
                            b a b a b
                        """.trimIndent()

                    LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { repeatSymbols(10) }
                        }
                    }.lessons[0].text shouldHaveLength 10
                }

                expect("empty symbols lead to empty result") {
                    LessonBuilder(10, 10, listOf(""), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { repeatSymbols(10) }
                        }
                    }.lessons shouldBe emptyList()
                }

                expect("result is trimmed") {
                    val text = LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { repeatSymbols(10) }
                        }
                    }.lessons[0].text
                    text shouldNotStartWith "\\s"
                    text shouldNotEndWith "\\s"
                }
            }

            context("alternateSymbols") {

                expect("result consists of the input symbols in alternating fashion") {
                    LessonBuilder(14, 10, listOf("abc"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { alternateSymbols(2) }
                        }
                    }.lessons[0].text shouldBe "aa bb cc aa bb"
                }

                expect("empty symbols lead to empty result") {
                    LessonBuilder(10, 10, listOf(""), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { alternateSymbols(10) }
                        }
                    }.lessons shouldBe emptyList()
                }

                expect("segment-length range test") {
                    LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { alternateSymbols(-10) }
                        }
                    }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { alternateSymbols(-1) }
                        }
                    }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10,listOf("ab"),  emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { alternateSymbols(0) }
                        }
                    }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { alternateSymbols(1) }
                        }
                    }.lessons[0].text shouldBe """
                            a b a b a
                            b a b a b
                        """.trimIndent()

                    LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { alternateSymbols(10) }
                        }
                    }.lessons[0].text shouldHaveLength 10
                }

                expect("result is trimmed") {
                    val text = LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { alternateSymbols(5) }
                        }
                    }.lessons[0].text
                    text shouldNotStartWith "\\s"
                    text shouldNotEndWith "\\s"
                }
            }

            context("shuffleSymbols") {

                expect("shuffle the input symbols") {
                    LessonBuilder(10, 10, listOf("ab"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { shuffleSymbols(10) }
                        }
                    }.lessons[0].text shouldMatch "[ab]{10}"
                }

                expect("empty symbols lead to empty result") {
                    LessonBuilder(10, 10, listOf(""), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { shuffleSymbols(10) }
                        }
                    }.lessons shouldBe emptyList()
                }

                expect("segment-length range test") {
                    LessonBuilder(10, 10, listOf("abc"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { shuffleSymbols(-10) }
                        }
                    }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, listOf("abc"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { shuffleSymbols(-1) }
                        }
                    }.lessons shouldBe emptyList()

                    LessonBuilder(10, 10, listOf("abc"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { shuffleSymbols(0) }
                        }
                    }.lessons shouldBe emptyList()

                    val text = LessonBuilder(10, 10, listOf("abc"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { shuffleSymbols(1) }
                        }
                    }.lessons[0].text
                    text.split("\n")[0] shouldMatch "[abc ]{9}" // e.g. "a b c c a " 10 -> trimmed to "a b c c a" 9
                    text.split("\n")[1] shouldMatch "[abc ]{9}"

                    LessonBuilder(10, 10, listOf("abc"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { shuffleSymbols(10) }
                        }
                    }.lessons[0].text shouldHaveLength 10
                }

                expect("result is trimmed") {
                    val text = LessonBuilder(10, 10, listOf("abc"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { shuffleSymbols(1) }
                        }
                    }.lessons[0].text
                    text shouldNotStartWith "\\s"
                    text shouldNotEndWith "\\s"
                }
            }

            context("words") {

                expect("result consists of words from the dictionary consisting of symbols of the symbol history and containing the current lessons symbols") {
                    val text = LessonBuilder(20, 10, listOf("acbreuoy"), sequenceOf("abc", "are", "you")).apply {
                        forEachLessonSpecification {
                            lesson { words() }
                        }
                    }.lessons[0].text
                    text shouldContain "abc"
                    text shouldContain "are"
                    text shouldContain "you"
                }

                expect("empty dictionary leads to empty result") {
                    LessonBuilder(10, 10, listOf("acbreuoy"), emptySequence()).apply {
                        forEachLessonSpecification {
                            lesson { words() }
                        }
                    }.lessons shouldBe emptyList()
                }

                expect("result is trimmed") {
                    val text = LessonBuilder(10, 10, listOf("acbreuoy"), sequenceOf("abc", "are", "you")).apply {
                        forEachLessonSpecification {
                            lesson { words() }
                        }
                    }.lessons[0].text
                    text shouldNotStartWith "\\s"
                    text shouldNotEndWith "\\s"
                }

                expect("each line is trimmed") {
                    val text = LessonBuilder(10, 10, listOf("acbreuoy"), sequenceOf("abc", "are", "you")).apply {
                        forEachLessonSpecification {
                            lesson { words() }
                        }
                    }.lessons[0].text
                    text.split("\n") shouldHaveAtLeastSize 2
                    text.split("\n").forAll { line ->
                        line shouldNotStartWith "\\s"
                        line shouldNotEndWith "\\s"
                    }
                }

                expect("unconditional punctuation marks are prefixed or appended randomly to the words") {
                    LessonBuilder(20, 10, listOf("acb,re"), sequenceOf("abc", "are")).apply {
                        forEachLessonSpecification {
                            lesson { words() }
                        }
                    }.lessons[0].text shouldContain "((,abc)|(abc,)|(,are)|(are,))".toRegex()
                }

                expect("WW punctuation marks are paired around a word") {
                    LessonBuilder(20, 10, listOf("acbre", "<WW>"), sequenceOf("abc", "are")).apply {
                        forEachLessonSpecification {
                            lesson { words() }
                        }
                    }.lessons[1].text shouldContain "((<abc>)|(<are>))".toRegex()
                }

                expect("no word lessons are generated for digits") {
                    LessonBuilder(10, 10, listOf("acbre", "123"), sequenceOf("abc", "are")).apply {
                        forEachLessonSpecification {
                            lesson { words() }
                        }
                    }.lessons.getOrNull(1) shouldBe null
                }
            }

            context("Every") {

                context("summaryLesson") {

                    expect("create a summary lesson every X lesson specifications") {
                        Every(2, listOf("ab", "cd", "ef", "gh"), emptySequence())
                            .summaryLesson { exampleBuildStep() }
                            .indexedLs shouldHaveSize 2
                    }

                    expect("key is the index from the corresponding lesson specification in the lesson-specifications list") {
                        val every = Every(2, listOf("ab", "cd", "ef", "gh"), emptySequence())
                        every.summaryLesson { exampleBuildStep() }
                        every.indexedLs[1] shouldNotBe null
                        every.indexedLs[3] shouldNotBe null
                    }

                    expect("is a summary over the last X lesson specifications") {
                        val every = Every(2, listOf("ab", "cd", "ef", "gh"), emptySequence())
                        every.summaryLesson { exampleBuildStep() }
                        every.indexedLs[1]!!.single().symbols shouldBe "abcd"
                        every.indexedLs[3]!!.single().symbols shouldBe "efgh"
                    }

                    expect("empty result on empty lesson-specifications") {
                        Every(2, emptyList(), emptySequence())
                            .summaryLesson { exampleBuildStep() }
                            .indexedLs shouldHaveSize 0
                    }

                    expect("n range test") {

                        val lessonSpecifications = listOf(
                            "ab", "cd", "ef", "gh",
                            "ij", "kl", "mn", "op",
                            "qr", "st", "uv", "wx")

                        Every(-10, lessonSpecifications, emptySequence())
                            .summaryLesson { exampleBuildStep() }
                            .indexedLs shouldHaveSize 0

                        Every(-1, lessonSpecifications, emptySequence())
                            .summaryLesson { exampleBuildStep() }
                            .indexedLs shouldHaveSize 0

                        Every(0, lessonSpecifications, emptySequence())
                            .summaryLesson { exampleBuildStep() }
                            .indexedLs shouldHaveSize 0

                        Every(1, lessonSpecifications, emptySequence())
                            .summaryLesson { exampleBuildStep() }
                            .indexedLs shouldHaveSize 12

                        Every(10, lessonSpecifications, emptySequence())
                            .summaryLesson { exampleBuildStep() }
                            .indexedLs shouldHaveSize 1
                    }
                }
            }

            context("ForEach") {

                context("lesson") {

                    expect("create a lesson for each lesson-specification") {
                        val forEach = ForEach(listOf("ab", "cd", "ef", "gh"), emptySequence())
                        forEach.lesson { exampleBuildStep() }
                        forEach.indexedLs shouldHaveSize 4
                        forEach.indexedLs[0]!!.single().symbols shouldBe "ab"
                        forEach.indexedLs[1]!!.single().symbols shouldBe "cd"
                        forEach.indexedLs[2]!!.single().symbols shouldBe "ef"
                        forEach.indexedLs[3]!!.single().symbols shouldBe "gh"
                    }

                    expect("can create multiple lessons for each lesson-specification") {
                        val forEach = ForEach(listOf("ab", "cd", "ef", "gh"), emptySequence())
                        forEach.lesson { exampleBuildStep() }
                        forEach.lesson { exampleBuildStep() }
                        forEach.lesson { exampleBuildStep() }
                        forEach.indexedLs shouldHaveSize 4
                        forEach.indexedLs[0]!!.elementAt(0).symbols shouldBe "ab"
                        forEach.indexedLs[0]!!.elementAt(1).symbols shouldBe "ab"
                        forEach.indexedLs[0]!!.elementAt(2).symbols shouldBe "ab"
                        forEach.indexedLs[1]!!.elementAt(0).symbols shouldBe "cd"
                        forEach.indexedLs[1]!!.elementAt(1).symbols shouldBe "cd"
                        forEach.indexedLs[1]!!.elementAt(2).symbols shouldBe "cd"
                        forEach.indexedLs[2]!!.elementAt(0).symbols shouldBe "ef"
                        forEach.indexedLs[2]!!.elementAt(1).symbols shouldBe "ef"
                        forEach.indexedLs[2]!!.elementAt(2).symbols shouldBe "ef"
                        forEach.indexedLs[3]!!.elementAt(0).symbols shouldBe "gh"
                        forEach.indexedLs[3]!!.elementAt(1).symbols shouldBe "gh"
                        forEach.indexedLs[3]!!.elementAt(2).symbols shouldBe "gh"
                    }

                    expect("empty result on empty lesson-specifications") {
                        ForEach(emptyList(), emptySequence())
                            .lesson { exampleBuildStep() }
                            .indexedLs shouldHaveSize 0
                    }
                }
            }

            context("LessonPrototypeComparator") {

                expect("lower position before higher position") {
                    val a = LessonPrototype(0)
                    val b = LessonPrototype(1)
                    LessonPrototypeComparator().compare(a, b) shouldBe -1
                    LessonPrototypeComparator().compare(b, a) shouldBe 1
                }

                context("when on same position") {

                    expect("regular before summary") {
                        val a1 = LessonPrototype(kind = LessonKind.Regular )
                        val b1 = LessonPrototype(kind = LessonKind.Summary)
                        LessonPrototypeComparator().compare(a1, b1) shouldBe -1

                        val a2 = LessonPrototype(kind = LessonKind.Summary)
                        val b2 = LessonPrototype(kind = LessonKind.Regular)
                        LessonPrototypeComparator().compare(a2, b2) shouldBe 1
                    }

                    expect("equal when regular and regular") {
                        val a = LessonPrototype(kind = LessonKind.Regular)
                        val b = LessonPrototype(kind = LessonKind.Regular)
                        LessonPrototypeComparator().compare(a, b) shouldBe 0
                    }

                    expect("equal when summary and summary") {
                        val a = LessonPrototype(kind = LessonKind.Summary)
                        val b = LessonPrototype(kind = LessonKind.Summary)
                        LessonPrototypeComparator().compare(a, b) shouldBe 0
                    }
                }
            }

            context("createLessonPrototypes") {

                expect("create lesson prototypes out of indexed Ls") {
                    val l1 = L(title = "ab", symbols = "ab").exampleBuildStep()
                    val l2 = L(title = "cd", symbols = "cd").exampleBuildStep()
                    val indexedLs: IndexedLs = mutableMapOf(
                        4 to mutableListOf(l1),
                        5 to mutableListOf(l2)
                    )
                    val lessonPrototypes = createLessonPrototypes(indexedLs, 20, 40)
                    lessonPrototypes[0].position shouldBe 4
                    lessonPrototypes[0].symbols shouldBe "ab"
                    lessonPrototypes[0].lesson.title shouldBe "ab"
                    lessonPrototypes[1].position shouldBe 5
                    lessonPrototypes[1].symbols shouldBe "cd"
                    lessonPrototypes[1].lesson.title shouldBe "cd"
                }

                expect("empty result when Ls have no text generators") {
                    val l1 = L(title = "ab", symbols = "ab")
                    val l2 = L(title = "cd", symbols = "cd")
                    val indexedLs: IndexedLs = mutableMapOf(
                        4 to mutableListOf(l1),
                        5 to mutableListOf(l2)
                    )
                    val lessonPrototypes = createLessonPrototypes(indexedLs, 20, 40)
                    lessonPrototypes shouldHaveSize 0
                }

                expect("empty result when there are no Ls") {
                    val indexedLs: IndexedLs = mutableMapOf()
                    val lessonPrototypes = createLessonPrototypes(indexedLs, 20, 40)
                    lessonPrototypes shouldHaveSize 0
                }

                expect("empty result when line-length is negative") {
                    val l1 = L(title = "ab", symbols = "ab").exampleBuildStep()
                    val l2 = L(title = "cd", symbols = "cd").exampleBuildStep()
                    val indexedLs: IndexedLs = mutableMapOf(
                        4 to mutableListOf(l1),
                        5 to mutableListOf(l2)
                    )
                    createLessonPrototypes(indexedLs, -1, 40) shouldHaveSize 0
                }

                expect("empty result when line-length is 0") {
                    val l1 = L(title = "ab", symbols = "ab").exampleBuildStep()
                    val l2 = L(title = "cd", symbols = "cd").exampleBuildStep()
                    val indexedLs: IndexedLs = mutableMapOf(
                        4 to mutableListOf(l1),
                        5 to mutableListOf(l2)
                    )
                    createLessonPrototypes(indexedLs, 0, 40) shouldHaveSize 0
                }

                expect("empty result when symbols-per-lesson is negative") {
                    val l1 = L(title = "ab", symbols = "ab").exampleBuildStep()
                    val l2 = L(title = "cd", symbols = "cd").exampleBuildStep()
                    val indexedLs: IndexedLs = mutableMapOf(
                        4 to mutableListOf(l1),
                        5 to mutableListOf(l2)
                    )
                    createLessonPrototypes(indexedLs, 20, -1) shouldHaveSize 0
                }

                expect("empty result when symbols-per-lesson is 0") {
                    val l1 = L(title = "ab", symbols = "ab").exampleBuildStep()
                    val l2 = L(title = "cd", symbols = "cd").exampleBuildStep()
                    val indexedLs: IndexedLs = mutableMapOf(
                        4 to mutableListOf(l1),
                        5 to mutableListOf(l2)
                    )
                    createLessonPrototypes(indexedLs, 20, 0) shouldHaveSize 0
                }
            }

            context("calculateLessonText") {

                expect("calculate the lesson text out of Ls build steps (text generators)") {
                    val l = L(title = "ab", symbols = "ab").exampleBuildStep()
                    calculateLessonText(20, 40, l) shouldNotBe ""
                }

                expect("lesson text has exactly symbols-per-lesson non-whitespace characters") {
                    val l = L(title = "ab", symbols = "ab").exampleBuildStep()
                    calculateLessonText(20, 40, l).count { !it.isWhitespace() } shouldBe 40
                }

                expect("each line of the lesson text is about line-length symbols long (words are not split)") {
                    val l = L(title = "ab", symbols = "ab").exampleBuildStep()
                    val lines = calculateLessonText(20, 40, l).split("\n".toRegex())
                    lines[0] shouldHaveLength 20
                    lines[1] shouldHaveLength 20
                }

                expect("empty result when l has no build steps (text generators)") {
                    val l = L(title = "ab", symbols = "ab")
                    calculateLessonText(20, 40, l) shouldBe ""
                }

                expect("empty result when line-length is negative") {
                    val l = L(title = "ab", symbols = "ab").exampleBuildStep()
                    calculateLessonText(-1, 40, l) shouldBe ""
                }

                expect("empty result when line-length is 0") {
                    val l = L(title = "ab", symbols = "ab").exampleBuildStep()
                    calculateLessonText(0, 40, l) shouldBe ""
                }

                expect("empty result when symbols-per-lesson is negative") {
                    val l = L(title = "ab", symbols = "ab").exampleBuildStep()
                    calculateLessonText(20, -1, l) shouldBe ""
                }

                expect("empty result when symbols-per-lesson is 0") {
                    val l = L(title = "ab", symbols = "ab").exampleBuildStep()
                    calculateLessonText(20, 0, l) shouldBe ""
                }
            }

            context("setNewCharacters") {

                expect("set symbols as newCharacters only for the first lesson of these symbols (because this lesson introduces these symbols)") {
                    val prototypes = listOf(
                        LessonPrototype(symbols = "ab", lesson = Lesson()),
                        LessonPrototype(symbols = "ab", lesson = Lesson()),
                        LessonPrototype(symbols = "ab", lesson = Lesson()),
                        LessonPrototype(symbols = "cd", lesson = Lesson()),
                        LessonPrototype(symbols = "cd", lesson = Lesson())
                    )
                    val withCorrectlySetNewCharacters = prototypes.setNewCharacters()
                    withCorrectlySetNewCharacters[0].lesson.newCharacters shouldBe "ab"
                    withCorrectlySetNewCharacters[1].lesson.newCharacters shouldBe ""
                    withCorrectlySetNewCharacters[2].lesson.newCharacters shouldBe ""
                    withCorrectlySetNewCharacters[3].lesson.newCharacters shouldBe "cd"
                    withCorrectlySetNewCharacters[4].lesson.newCharacters shouldBe ""
                }

                expect("work in order of the prototypes list (position does not matter)") {
                    val prototypes = listOf(
                        LessonPrototype(position = 9, symbols = "ab", lesson = Lesson(title = "b")),
                        LessonPrototype(position = 10, symbols = "ab", lesson = Lesson(title = "c")),
                        LessonPrototype(position = 8, symbols = "ab", lesson = Lesson(title = "a"))
                    )
                    val withCorrectlySetNewCharacters = prototypes.setNewCharacters()
                    withCorrectlySetNewCharacters[0].lesson.newCharacters shouldBe "ab"
                    withCorrectlySetNewCharacters[0].lesson.title shouldBe "b"
                    withCorrectlySetNewCharacters[1].lesson.newCharacters shouldBe ""
                    withCorrectlySetNewCharacters[1].lesson.title shouldBe "c"
                    withCorrectlySetNewCharacters[2].lesson.newCharacters shouldBe ""
                    withCorrectlySetNewCharacters[2].lesson.title shouldBe "a"
                }

                expect("empty result on empty input") {
                    val prototypes = emptyList<LessonPrototype>()
                    val withCorrectlySetNewCharacters = prototypes.setNewCharacters()
                    withCorrectlySetNewCharacters shouldHaveSize 0
                }
            }

            context("filter") {

                expect("remove lessons which are not matched by filters") {
                    val iHateCds = { _: Lesson?, current: Lesson -> current.title != "cd" }
                    val filtered = listOf(
                        LessonPrototype(lesson = Lesson(title = "ab")),
                        LessonPrototype(lesson = Lesson(title = "ab")),
                        LessonPrototype(lesson = Lesson(title = "ab")),
                        LessonPrototype(lesson = Lesson(title = "cd")),
                        LessonPrototype(lesson = Lesson(title = "cd"))
                    ).filter(listOf(iHateCds))
                    filtered shouldHaveSize 3
                    filtered.none { it.lesson.title == "cd" } shouldBe true
                }

                expect("filter collection can be empty, no filters are applied then") {
                    val filtered = listOf(
                        LessonPrototype(lesson = Lesson(title = "ab")),
                        LessonPrototype(lesson = Lesson(title = "ab")),
                        LessonPrototype(lesson = Lesson(title = "ab")),
                        LessonPrototype(lesson = Lesson(title = "cd")),
                        LessonPrototype(lesson = Lesson(title = "cd"))
                    ).filter(emptyList())
                    filtered shouldHaveSize 5
                }

                expect("lessons introducing new characters are always kept, they are not affected by filters") {
                    val iHateAbs = { _: Lesson?, current: Lesson -> current.title != "ab" }
                    val iHateCds = { _: Lesson?, current: Lesson -> current.title != "cd" }
                    val filtered = listOf(
                        LessonPrototype(lesson = Lesson(title = "ab", newCharacters = "ab")),
                        LessonPrototype(lesson = Lesson(title = "ab")),
                        LessonPrototype(lesson = Lesson(title = "ab")),
                        LessonPrototype(lesson = Lesson(title = "cd", newCharacters = "cd")),
                        LessonPrototype(lesson = Lesson(title = "cd"))
                    ).filter(listOf(iHateAbs, iHateCds))
                    filtered shouldHaveSize 2
                    filtered[0].lesson.newCharacters shouldBe "ab"
                    filtered[1].lesson.newCharacters shouldBe "cd"
                }

                expect("summary lessons are always kept, they are not affected by filters") {
                    val iHateAllLessons = { _: Lesson?, _: Lesson -> false }
                    val filtered = listOf(
                        LessonPrototype(lesson = Lesson(title = "ab")),
                        LessonPrototype(lesson = Lesson(title = "ab")),
                        LessonPrototype(lesson = Lesson(title = "ab summary"), kind = LessonKind.Summary),
                    ).filter(listOf(iHateAllLessons))
                    filtered shouldHaveSize 1
                    filtered[0].lesson.title shouldBe "ab summary"
                }
            }

            context("enumerate") {

                expect("prototypes are enumerated in list order; a counter to the lesson title will be added") {
                    val enumerated = listOf(
                        LessonPrototype(symbols = "ab"),
                        LessonPrototype(symbols = "cd"),
                        LessonPrototype(symbols = "ef"),
                        LessonPrototype(symbols = "gh"),
                        LessonPrototype(symbols = "ij")
                    ).enumerate()
                    enumerated[0].lesson.title shouldStartWith "1"
                    enumerated[0].symbols shouldBe "ab"
                    enumerated[1].lesson.title shouldStartWith "2"
                    enumerated[1].symbols shouldBe "cd"
                    enumerated[2].lesson.title shouldStartWith "3"
                    enumerated[2].symbols shouldBe "ef"
                    enumerated[3].lesson.title shouldStartWith "4"
                    enumerated[3].symbols shouldBe "gh"
                    enumerated[4].lesson.title shouldStartWith "5"
                    enumerated[4].symbols shouldBe "ij"
                }

                expect("empty input leads to empty output") {
                    emptyList<LessonPrototype>().enumerate() shouldHaveSize 0
                }
            }

            context("apply") {

                expect("apply all builder steps; resulting created lessons are in the lessons collection afterwards") {
                    val lb = LessonBuilder(20, 40, listOf("ab", "cd"), emptySequence())
                        .apply {
                            forEachLessonSpecification {
                                lesson {
                                    exampleBuildStep()
                                }
                            }
                        }
                    lb.lessons shouldHaveSize 2
                }

                expect("empty lesson-specification input leads to empty output") {
                    val lb = LessonBuilder(20, 40, emptyList(), emptySequence())
                        .apply {
                            forEachLessonSpecification {
                                lesson {
                                    repeatSymbols(3)
                                }
                            }
                        }
                    lb.lessons shouldHaveSize 0
                }

                expect("created lessons are enumerated (in their title) starting at 1") {
                    val lb = LessonBuilder(20, 40, listOf("ab", "cd"), emptySequence())
                        .apply {
                            forEachLessonSpecification {
                                lesson {
                                    exampleBuildStep()
                                }
                            }
                        }
                    lb.lessons shouldHaveSize 2
                    lb.lessons[0].title shouldStartWith "1"
                    lb.lessons[0].newCharacters shouldBe "ab"
                    lb.lessons[1].title shouldStartWith "2"
                    lb.lessons[1].newCharacters shouldBe "cd"
                }
            }

            context("forEachLessonSpecification") {

                expect("calls all build steps for each input specification; results are added to the lesson-builders' indexedLs collection") {
                    LessonBuilder(20, 40, listOf("ab", "cd"), emptySequence())
                        .apply {
                            val forEach = forEachLessonSpecification {
                                lesson {
                                    exampleBuildStep()
                                    exampleBuildStep()
                                }
                            }
                            indexedLs shouldHaveSize 2
                            indexedLs[0]!!.first().symbols shouldBe "ab"
                            indexedLs[0]!!.last().symbols shouldBe "ab"
                            indexedLs[1]!!.first().symbols shouldBe "cd"
                            indexedLs[1]!!.last().symbols shouldBe "cd"
                            forEach
                        }
                }

                expect("empty lesson-specification input leads to empty output") {
                    LessonBuilder(20, 40, emptyList(), emptySequence())
                        .apply {
                            val forEach = forEachLessonSpecification {
                                lesson {
                                    exampleBuildStep()
                                }
                            }
                            indexedLs shouldHaveSize 0
                            forEach
                        }
                }

                expect("empty body results in empty output") {
                    LessonBuilder(20, 40, listOf("ab", "cd"), emptySequence())
                        .apply {
                            val forEach = forEachLessonSpecification { }
                            indexedLs shouldHaveSize 0
                            forEach
                        }
                }
            }

            context("every") {

                expect("calls all build steps aggregated for every n-th input specification; results are added to the lesson-builders' indexedLs collection") {
                    LessonBuilder(20, 40, listOf("ab", "cd", "ef", "gh"), emptySequence())
                        .apply {
                            val every = every(2) {
                                summaryLesson {
                                    exampleBuildStep()
                                }
                            }
                            indexedLs shouldHaveSize 2
                            indexedLs[1]!!.single().symbols shouldBe "abcd" // index 1 = cd -> summary lesson for indices 0,1 = abcd
                            indexedLs[3]!!.single().symbols shouldBe "efgh" // index 3 = gh -> summary lesson for indices 2,3 = efgh
                            every
                        }
                }

                expect("empty lesson-specification input leads to empty output") {
                    LessonBuilder(20, 40, emptyList(), emptySequence())
                        .apply {
                            val every = every(2) {
                                summaryLesson {
                                    exampleBuildStep()
                                }
                            }
                            indexedLs shouldHaveSize 0
                            every
                        }
                }

                expect("empty body results in empty output") {
                    LessonBuilder(20, 40, listOf("ab", "cd"), emptySequence())
                        .apply {
                            val every = every(2) { }
                            indexedLs shouldHaveSize 0
                            every
                        }
                }
            }
        }

        context("IndexedLs") {

            context("putAppend") {

                expect("create new entry with l added to new list value when map is empty") {
                    val indexedLs: IndexedLs = mutableMapOf()
                    val l = L()
                    indexedLs.putAppend(23, l)
                    indexedLs shouldHaveSize 1
                    indexedLs[23]!! shouldHaveSize 1
                    indexedLs[23]!!.single() shouldBe l
                }

                expect("append l to list value when key already exists") {
                    val l = L(title = "a")
                    val indexedLs: IndexedLs = mutableMapOf(23 to mutableListOf(l))
                    val l2 = L(title = "b")
                    indexedLs.putAppend(23, l2)
                    indexedLs shouldHaveSize 1
                    indexedLs[23]!! shouldHaveSize 2
                    indexedLs[23]!!.first() shouldBe l
                    indexedLs[23]!!.last() shouldBe l2
                }
            }
        }

        context("withLessonFilter") {

            expect("lessons introducing new characters are generally not affected by filters") {
                val exampleFilterRemovingLessons = { _: Lesson?, _: Lesson -> false }
                val lessons = LessonBuilder(10, 10, listOf("ab"), emptySequence())
                    .withLessonFilter(exampleFilterRemovingLessons)
                    .apply {
                        forEachLessonSpecification {
                            lesson { shuffleSymbols(10) }
                        }
                    }.lessons
                lessons shouldHaveSize 1
                lessons[0].newCharacters shouldBe "ab"
            }

            expect("lessons can be removed by filters") {
                val exampleFilterRemovingLessons = { _: Lesson?, _: Lesson -> false }
                val lessons = LessonBuilder(10, 10, listOf("ab"), emptySequence())
                    .withLessonFilter(exampleFilterRemovingLessons)
                    .apply {
                        forEachLessonSpecification {
                            lesson { shuffleSymbols(10) } // introduces new characters; thus will be kept
                            lesson { shuffleSymbols(10) } // should be removed
                        }
                    }.lessons
                lessons shouldHaveSize 1
                lessons[0].newCharacters shouldBe "ab"
            }

            expect("all filters will be applied") {
                val iHateNumbers = { _: Lesson?, l: Lesson -> !l.text.contains("\\d+".toRegex()) }
                val iHateA = { _: Lesson?, l: Lesson -> !l.text.contains("a") }
                val lessons = LessonBuilder(10, 10, listOf("ab", "123", "cd"), emptySequence())
                    .withLessonFilter(iHateNumbers, iHateA)
                    .apply {
                        forEachLessonSpecification {
                            lesson { shuffleSymbols(10) }
                            lesson { shuffleSymbols(10) } // removed for ab and 123
                        }
                    }.lessons
                lessons shouldHaveSize 4
                lessons[0].title shouldContain "ab"   // introduces ab; thus will be kept
                lessons[1].title shouldContain "123"  // introduces 123; thus will be kept
                lessons[2].title shouldContain "cd"   // introduces cd; thus will be kept
                lessons[3].title shouldContain "cd"   // no filter hates cd :)
            }
        }
    }
})