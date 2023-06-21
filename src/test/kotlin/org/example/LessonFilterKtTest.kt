package org.example

import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe


class LessonFilterKtTest : ConcurrentExpectSpec({

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

                expect("true when previous lesson is null") {
                    val filterFun = Filter.relativeLevenshteinDistanceFromLessonBefore(0.6)
                    filterFun(null, Lesson(text = "0123456111")) shouldBe true
                }

                expect("true when minimumDistance exceeds the upper limit") {
                    val filterFun = Filter.relativeLevenshteinDistanceFromLessonBefore(1.1)
                    filterFun(Lesson(), Lesson()) shouldBe true
                }

                expect("true when minimumDistance is less than the lower limit") {
                    val filterFun = Filter.relativeLevenshteinDistanceFromLessonBefore(-0.1)
                    filterFun(Lesson(), Lesson()) shouldBe true
                }
            }

            context("lessonContainsAtLeastDifferentWords") {

                expect("true when lesson text contains n different words") {
                    val filterFun = Filter.lessonContainsAtLeastDifferentWords(3)
                    filterFun(null, Lesson(text = "abc def ghi")) shouldBe true
                }

                expect("true when lesson text contains more than n different words") {
                    val filterFun = Filter.lessonContainsAtLeastDifferentWords(3)
                    filterFun(null, Lesson(text = "abc def ghi jkl")) shouldBe true
                }

                expect("true when lesson text contains less than n words in sum") {
                    val filterFun = Filter.lessonContainsAtLeastDifferentWords(3)
                    filterFun(null, Lesson(text = "abc")) shouldBe true
                }

                expect("false when lesson text contains less than n different words") {
                    val filterFun = Filter.lessonContainsAtLeastDifferentWords(3)
                    filterFun(null, Lesson(text = "abc abc abc abc abc")) shouldBe false
                }

                expect("false when lesson text is empty") {
                    val filterFun = Filter.lessonContainsAtLeastDifferentWords(3)
                    filterFun(null, Lesson(text = "")) shouldBe false
                }

                expect("true when n is 0") {
                    val filterFun = Filter.lessonContainsAtLeastDifferentWords(0)
                    filterFun(null, Lesson(text = "abc def ghi jkl")) shouldBe true
                }

                expect("true when n is negative") {
                    val filterFun = Filter.lessonContainsAtLeastDifferentWords(-1)
                    filterFun(null, Lesson(text = "abc def ghi jkl")) shouldBe true
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

            expect("true when n is 0") {
                "a aa b bb ab ba aaab".differentWords(0) shouldBe true
            }

            expect("true when n is negative") {
                "a aa b bb ab ba aaab".differentWords(-1) shouldBe true
            }
        }
    }
})