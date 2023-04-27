package org.example

import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

class KeyboardKtTest : FileTest() {

    @Test
    fun `Level Comparator lower top value comes before higher top value`() {
        val first = Key(top = 0)
        val second = Key(top = 10)
        LevelComparator().compare(first, second) shouldBe -10
        LevelComparator().compare(second, first) shouldBe 10
    }

    @Test
    fun `Level Comparator equal when top values are equal`() {
        LevelComparator().compare(Key(top = 20), Key(top = 20)) shouldBe 0
    }

    @Test
    fun `LeftToRight Comparator lower left value comes before higher left value`() {
        val first = Key(left = 0)
        val second = Key(left = 10)
        LeftToRight().compare(first, second) shouldBe -10
        LeftToRight().compare(second, first) shouldBe 10
    }

    @Test
    fun `LeftToRight Comparator equal when left values are equal`() {
        LeftToRight().compare(Key(left = 20), Key(left = 20)) shouldBe 0
    }

    @Test
    fun `RightToLeft Comparator higher left value comes before lower left value`() {
        val first = Key(left = 10)
        val second = Key(left = 0)
        RightToLeft().compare(first, second) shouldBe -10
        RightToLeft().compare(second, first) shouldBe 10
    }

    @Test
    fun `RightToLeft Comparator equal when left values are equal`() {
        RightToLeft().compare(Key(left = 20), Key(left = 20)) shouldBe 0
    }

    @Test
    fun `hands expect 8 fingers, indicated by having 8 different finger indices`() {
        val kb = KeyboardLayout(keys = Keys(keys = listOf(
            Key(fingerIndex = 0),
            Key(fingerIndex = 1),
            Key(fingerIndex = 2),
            Key(fingerIndex = 3),
            Key(fingerIndex = 4),
            Key(fingerIndex = 5),
            Key(fingerIndex = 6),
            Key(fingerIndex = 7),
        )))
        hands(kb) shouldNotBe null
    }

    @Test
    fun `hands return null when there are not exactly 8 fingers (indicated by finger indices)`() {
        val kbSevenFingers = KeyboardLayout(keys = Keys(keys = listOf(
            Key(fingerIndex = 0),
            Key(fingerIndex = 1),
            Key(fingerIndex = 2),
            Key(fingerIndex = 3),
            Key(fingerIndex = 4),
            Key(fingerIndex = 5),
            Key(fingerIndex = 6),
        )))
        hands(kbSevenFingers) shouldBe null

        val kbNineFingers = KeyboardLayout(keys = Keys(keys = listOf(
            Key(fingerIndex = 0),
            Key(fingerIndex = 1),
            Key(fingerIndex = 2),
            Key(fingerIndex = 3),
            Key(fingerIndex = 4),
            Key(fingerIndex = 5),
            Key(fingerIndex = 6),
            Key(fingerIndex = 7),
            Key(fingerIndex = 8),
        )))
        hands(kbNineFingers) shouldBe null
    }

    @Test
    fun `hands read left and right hand`() {
        val keyboardLayout = KeyboardLayout(keys = Keys(keys = listOf(
            Key(fingerIndex = 0, chars = listOf(Char(text = "a"))),
            Key(fingerIndex = 1, chars = listOf(Char(text = "s"))),
            Key(fingerIndex = 2, chars = listOf(Char(text = "d"))),
            Key(fingerIndex = 3, chars = listOf(Char(text = "f"))),
            Key(fingerIndex = 4, chars = listOf(Char(text = "j"))),
            Key(fingerIndex = 5, chars = listOf(Char(text = "k"))),
            Key(fingerIndex = 6, chars = listOf(Char(text = "l"))),
            Key(fingerIndex = 7, chars = listOf(Char(text = "ö"))),
        )))

        val (left, right) = hands(keyboardLayout)!!

        // The algorithm reads fingers for both hands from little finger to index finger:
        // 0 little, 1 ring, 2 middle, 3 index

        left shouldHaveSize 4 // 4 fingers
        left[0][0] shouldHaveSize 1 // little
        left[0][0].first().chars.single().text shouldBe "a"
        left[1][0] shouldHaveSize 1 // ring
        left[1][0].first().chars.single().text shouldBe "s"
        left[2][0] shouldHaveSize 1 // middle
        left[2][0].first().chars.single().text shouldBe "d"
        left[3][0] shouldHaveSize 1 // index
        left[3][0].first().chars.single().text shouldBe "f"

        right shouldHaveSize 4 // 4 fingers
        right[3][0] shouldHaveSize 1 // index
        right[3][0].first().chars.single().text shouldBe "j"
        right[2][0] shouldHaveSize 1 // middle
        right[2][0].first().chars.single().text shouldBe "k"
        right[1][0] shouldHaveSize 1 // ring
        right[1][0].first().chars.single().text shouldBe "l"
        right[0][0] shouldHaveSize 1 // little
        right[0][0].first().chars.single().text shouldBe "ö"
    }

    @Test
    fun `hands order by finger-index, 0-3 is left hand, 4-7 is right hand`() {
        val keyboardLayout = KeyboardLayout(keys = Keys(keys = listOf(
            Key(fingerIndex = 0, chars = listOf(Char(text = "leftLittle"))),
            Key(fingerIndex = 1, chars = listOf(Char(text = "leftRing"))),
            Key(fingerIndex = 2, chars = listOf(Char(text = "leftMiddle"))),
            Key(fingerIndex = 3, chars = listOf(Char(text = "leftIndex"))),
            Key(fingerIndex = 4, chars = listOf(Char(text = "rightIndex"))),
            Key(fingerIndex = 5, chars = listOf(Char(text = "rightMiddle"))),
            Key(fingerIndex = 6, chars = listOf(Char(text = "rightRing"))),
            Key(fingerIndex = 7, chars = listOf(Char(text = "rightLittle"))),
        )))

        val (left, right) = hands(keyboardLayout)!!

        left[0][0][0].chars.single().text shouldBe "leftLittle"
        left[1][0][0].chars.single().text shouldBe "leftRing"
        left[2][0][0].chars.single().text shouldBe "leftMiddle"
        left[3][0][0].chars.single().text shouldBe "leftIndex"

        right[0][0][0].chars.single().text shouldBe "rightLittle"
        right[1][0][0].chars.single().text shouldBe "rightRing"
        right[2][0][0].chars.single().text shouldBe "rightMiddle"
        right[3][0][0].chars.single().text shouldBe "rightIndex"
    }

    @Test
    fun `hands the order of the keys in the source key list does not matter (only finger-index matters)`() {
        val keyboardLayout = KeyboardLayout(keys = Keys(keys = listOf(
            Key(fingerIndex = 1, chars = listOf(Char(text = "leftRing"))),
            Key(fingerIndex = 3, chars = listOf(Char(text = "leftIndex"))),
            Key(fingerIndex = 7, chars = listOf(Char(text = "rightLittle"))),
            Key(fingerIndex = 2, chars = listOf(Char(text = "leftMiddle"))),
            Key(fingerIndex = 0, chars = listOf(Char(text = "leftLittle"))),
            Key(fingerIndex = 4, chars = listOf(Char(text = "rightIndex"))),
            Key(fingerIndex = 6, chars = listOf(Char(text = "rightRing"))),
            Key(fingerIndex = 5, chars = listOf(Char(text = "rightMiddle"))),
        )))

        val (left, right) = hands(keyboardLayout)!!

        left[0][0][0].chars.single().text shouldBe "leftLittle"
        left[1][0][0].chars.single().text shouldBe "leftRing"
        left[2][0][0].chars.single().text shouldBe "leftMiddle"
        left[3][0][0].chars.single().text shouldBe "leftIndex"

        right[0][0][0].chars.single().text shouldBe "rightLittle"
        right[1][0][0].chars.single().text shouldBe "rightRing"
        right[2][0][0].chars.single().text shouldBe "rightMiddle"
        right[3][0][0].chars.single().text shouldBe "rightIndex"
    }

    @Test
    fun `hands separate levels via the top value of the keys (left hand test)`() {
        val keyboardLayout = KeyboardLayout(keys = Keys(keys = listOf(
            Key(fingerIndex = 0, chars = listOf(Char(text = "2")), top = 0),
            Key(fingerIndex = 0, chars = listOf(Char(text = "w")), top = 1),
            Key(fingerIndex = 0, chars = listOf(Char(text = "s")), top = 2),
            Key(fingerIndex = 0, chars = listOf(Char(text = "x")), top = 3),

            Key(fingerIndex = 1), Key(fingerIndex = 2), Key(fingerIndex = 3),
            Key(fingerIndex = 4), Key(fingerIndex = 5), Key(fingerIndex = 6),
            Key(fingerIndex = 7),
        )))

        val (left, _) = hands(keyboardLayout)!!

        val littleFinger = left[0]
        littleFinger[0][0].chars.single().text shouldBe "2" // level 0
        littleFinger[1][0].chars.single().text shouldBe "w" // level 1
        littleFinger[2][0].chars.single().text shouldBe "s" // level 2
        littleFinger[3][0].chars.single().text shouldBe "x" // level 3
    }

    @Test
    fun `hands separate levels via the top value of the keys (right hand test)`() {
        val keyboardLayout = KeyboardLayout(keys = Keys(keys = listOf(
            Key(fingerIndex = 0), Key(fingerIndex = 1), Key(fingerIndex = 2),
            Key(fingerIndex = 3), Key(fingerIndex = 4), Key(fingerIndex = 5),
            Key(fingerIndex = 6),

            Key(fingerIndex = 7, chars = listOf(Char(text = "0")), top = 0),
            Key(fingerIndex = 7, chars = listOf(Char(text = "o")), top = 1),
            Key(fingerIndex = 7, chars = listOf(Char(text = "l")), top = 2),
            Key(fingerIndex = 7, chars = listOf(Char(text = ".")), top = 3),
        )))

        val (_, right) = hands(keyboardLayout)!!

        val littleFinger = right[0]
        littleFinger[0][0].chars.single().text shouldBe "0" // level 0
        littleFinger[1][0].chars.single().text shouldBe "o" // level 1
        littleFinger[2][0].chars.single().text shouldBe "l" // level 2
        littleFinger[3][0].chars.single().text shouldBe "." // level 3
    }

    @Test
    fun `hands sort keys per level of left little finger right to left`() {
        val first = Key(fingerIndex = 0, chars = listOf(Char(text = "1")), top = 0, left = 10)
        val second = Key(fingerIndex = 0, chars = listOf(Char(text = "2")), top = 0, left = 0)
        val keyboardLayout = KeyboardLayout(keys = Keys(keys = listOf(
            first, second,
            Key(fingerIndex = 1), Key(fingerIndex = 2), Key(fingerIndex = 3),
            Key(fingerIndex = 4), Key(fingerIndex = 5), Key(fingerIndex = 6),
            Key(fingerIndex = 7),
        )))

        val (left, _) = hands(keyboardLayout)!!

        val littleFinger = left[0]
        littleFinger[0][0].chars.single().text shouldBe "1"
        littleFinger[0][1].chars.single().text shouldBe "2"
    }

    @Test
    fun `hands sort keys per level of left ring finger right to left`() {
        val first = Key(fingerIndex = 1, chars = listOf(Char(text = "1")), top = 0, left = 10)
        val second = Key(fingerIndex = 1, chars = listOf(Char(text = "2")), top = 0, left = 0)
        val keyboardLayout = KeyboardLayout(keys = Keys(keys = listOf(
            first, second,
            Key(fingerIndex = 0), Key(fingerIndex = 2), Key(fingerIndex = 3),
            Key(fingerIndex = 4), Key(fingerIndex = 5), Key(fingerIndex = 6),
            Key(fingerIndex = 7),
        )))

        val (left, _) = hands(keyboardLayout)!!

        val ringFinger = left[1]
        ringFinger[0][0].chars.single().text shouldBe "1"
        ringFinger[0][1].chars.single().text shouldBe "2"
    }

    @Test
    fun `hands sort keys per level of left middle finger right to left`() {
        val first = Key(fingerIndex = 2, chars = listOf(Char(text = "1")), top = 0, left = 10)
        val second = Key(fingerIndex = 2, chars = listOf(Char(text = "2")), top = 0, left = 0)
        val keyboardLayout = KeyboardLayout(keys = Keys(keys = listOf(
            first, second,
            Key(fingerIndex = 1), Key(fingerIndex = 0), Key(fingerIndex = 3),
            Key(fingerIndex = 4), Key(fingerIndex = 5), Key(fingerIndex = 6),
            Key(fingerIndex = 7),
        )))

        val (left, _) = hands(keyboardLayout)!!

        val middleFinger = left[2]
        middleFinger[0][0].chars.single().text shouldBe "1"
        middleFinger[0][1].chars.single().text shouldBe "2"
    }

    @Test
    fun `hands sort keys per level of left index finger left to right`() {
        val first = Key(fingerIndex = 3, chars = listOf(Char(text = "1")), top = 0, left = 0)
        val second = Key(fingerIndex = 3, chars = listOf(Char(text = "2")), top = 0, left = 10)
        val keyboardLayout = KeyboardLayout(keys = Keys(keys = listOf(
            first, second,
            Key(fingerIndex = 1), Key(fingerIndex = 2), Key(fingerIndex = 0),
            Key(fingerIndex = 4), Key(fingerIndex = 5), Key(fingerIndex = 6),
            Key(fingerIndex = 7),
        )))

        val (left, _) = hands(keyboardLayout)!!

        val indexFinger = left[3]
        indexFinger[0][0].chars.single().text shouldBe "1"
        indexFinger[0][1].chars.single().text shouldBe "2"
    }

    @Test
    fun `hands sort keys per level of right little finger left to right`() {
        val first = Key(fingerIndex = 7, chars = listOf(Char(text = "1")), top = 0, left = 0)
        val second = Key(fingerIndex = 7, chars = listOf(Char(text = "2")), top = 0, left = 10)
        val keyboardLayout = KeyboardLayout(keys = Keys(keys = listOf(
            first, second,
            Key(fingerIndex = 1), Key(fingerIndex = 2), Key(fingerIndex = 3),
            Key(fingerIndex = 4), Key(fingerIndex = 5), Key(fingerIndex = 6),
            Key(fingerIndex = 0),
        )))

        val (_, right) = hands(keyboardLayout)!!

        val littleFinger = right[0]
        littleFinger[0][0].chars.single().text shouldBe "1"
        littleFinger[0][1].chars.single().text shouldBe "2"
    }

    @Test
    fun `hands sort keys per level of right ring finger left to right`() {
        val first = Key(fingerIndex = 6, chars = listOf(Char(text = "1")), top = 0, left = 0)
        val second = Key(fingerIndex = 6, chars = listOf(Char(text = "2")), top = 0, left = 10)
        val keyboardLayout = KeyboardLayout(keys = Keys(keys = listOf(
            first, second,
            Key(fingerIndex = 1), Key(fingerIndex = 2), Key(fingerIndex = 3),
            Key(fingerIndex = 4), Key(fingerIndex = 5), Key(fingerIndex = 0),
            Key(fingerIndex = 7),
        )))

        val (_, right) = hands(keyboardLayout)!!

        val ringFinger = right[1]
        ringFinger[0][0].chars.single().text shouldBe "1"
        ringFinger[0][1].chars.single().text shouldBe "2"
    }

    @Test
    fun `hands sort keys per level of right middle finger left to right`() {
        val first = Key(fingerIndex = 5, chars = listOf(Char(text = "1")), top = 0, left = 0)
        val second = Key(fingerIndex = 5, chars = listOf(Char(text = "2")), top = 0, left = 10)
        val keyboardLayout = KeyboardLayout(keys = Keys(keys = listOf(
            first, second,
            Key(fingerIndex = 1), Key(fingerIndex = 2), Key(fingerIndex = 3),
            Key(fingerIndex = 4), Key(fingerIndex = 0), Key(fingerIndex = 6),
            Key(fingerIndex = 7),
        )))

        val (_, right) = hands(keyboardLayout)!!

        val middleFinger = right[2]
        middleFinger[0][0].chars.single().text shouldBe "1"
        middleFinger[0][1].chars.single().text shouldBe "2"
    }

    @Test
    fun `hands sort keys per level of right index finger right to left`() {
        val first = Key(fingerIndex = 4, chars = listOf(Char(text = "1")), top = 0, left = 10)
        val second = Key(fingerIndex = 4, chars = listOf(Char(text = "2")), top = 0, left = 0)
        val keyboardLayout = KeyboardLayout(keys = Keys(keys = listOf(
            first, second,
            Key(fingerIndex = 1), Key(fingerIndex = 2), Key(fingerIndex = 3),
            Key(fingerIndex = 0), Key(fingerIndex = 5), Key(fingerIndex = 6),
            Key(fingerIndex = 7),
        )))

        val (_, right) = hands(keyboardLayout)!!

        val indexFinger = right[3]
        indexFinger[0][0].chars.single().text shouldBe "1"
        indexFinger[0][1].chars.single().text shouldBe "2"
    }

    @Test
    fun `hands return null when keyboard layout has no keys`() {
        val kb = KeyboardLayout(keys = Keys(emptyList()))
        hands(kb) shouldBe null
    }

    @Test
    fun `pairKeys mirrors right hand keys to left hand keys`() {
        val hands = hands(KeyboardLayout(keys = Keys(keys = listOf(
            Key(fingerIndex = 0, chars = listOf(Char(text = "a"))),
            Key(fingerIndex = 1, chars = listOf(Char(text = "s"))),
            Key(fingerIndex = 2, chars = listOf(Char(text = "d"))),
            Key(fingerIndex = 3, chars = listOf(Char(text = "f"))),
            Key(fingerIndex = 4, chars = listOf(Char(text = "j"))),
            Key(fingerIndex = 5, chars = listOf(Char(text = "k"))),
            Key(fingerIndex = 6, chars = listOf(Char(text = "l"))),
            Key(fingerIndex = 7, chars = listOf(Char(text = ";"))),
        ))))

        val pairs = pairKeys(hands)

        pairs[0].pair.first!!.chars.first().text shouldBe "a"  // left little to
        pairs[0].pair.second!!.chars.first().text shouldBe ";" // right little

        pairs[1].pair.first!!.chars.first().text shouldBe "s"  // left ring to
        pairs[1].pair.second!!.chars.first().text shouldBe "l" // right ring

        pairs[2].pair.first!!.chars.first().text shouldBe "d"  // left middle to
        pairs[2].pair.second!!.chars.first().text shouldBe "k" // right middle

        pairs[3].pair.first!!.chars.first().text shouldBe "f"  // left index to
        pairs[3].pair.second!!.chars.first().text shouldBe "j" // right index
    }

    @Test
    fun `pairKeys keys having no opponents are at the end of the result list`() {
        val hands = hands(KeyboardLayout(keys = Keys(keys = listOf(
            Key(fingerIndex = 0, chars = listOf(Char(text = "a"))),
            Key(fingerIndex = 1, chars = listOf(Char(text = "s"))),
            Key(fingerIndex = 2, chars = listOf(Char(text = "d"))),
            Key(fingerIndex = 3, chars = listOf(Char(text = "f")), left = 0),
            Key(fingerIndex = 3, chars = listOf(Char(text = "g")), left = 10), // has no opponent

            Key(fingerIndex = 4, chars = listOf(Char(text = "j"))),
            Key(fingerIndex = 5, chars = listOf(Char(text = "k"))),
            Key(fingerIndex = 6, chars = listOf(Char(text = "l"))),
            Key(fingerIndex = 7, chars = listOf(Char(text = ";"))),
        ))))
        val pairs = pairKeys(hands)

        pairs.last().pair.first!!.chars.first().text shouldBe "g"
    }

    @Test
    fun `pairKeys keys having no opponents are in result pair first position, second is null`() {
        val hands = hands(KeyboardLayout(keys = Keys(keys = listOf(
            Key(fingerIndex = 0, chars = listOf(Char(text = "a"))),
            Key(fingerIndex = 1, chars = listOf(Char(text = "s"))),
            Key(fingerIndex = 2, chars = listOf(Char(text = "d"))),
            Key(fingerIndex = 3, chars = listOf(Char(text = "f")), left = 0),
            Key(fingerIndex = 3, chars = listOf(Char(text = "g")), left = 10), // has no opponent

            Key(fingerIndex = 4, chars = listOf(Char(text = "j"))),
            Key(fingerIndex = 5, chars = listOf(Char(text = "k"))),
            Key(fingerIndex = 6, chars = listOf(Char(text = "l"))),
            Key(fingerIndex = 7, chars = listOf(Char(text = ";"))),
        ))))
        val pairs = pairKeys(hands)

        pairs.last().pair.first!!.chars.first().text shouldBe "g"
        pairs.last().pair.second shouldBe null
    }

    @Test
    fun `pairKeys properties finger, level and index of keys having no opponents are set to -1`() {
        val hands = hands(KeyboardLayout(keys = Keys(keys = listOf(
            Key(fingerIndex = 0, chars = listOf(Char(text = "a"))),
            Key(fingerIndex = 1, chars = listOf(Char(text = "s"))),
            Key(fingerIndex = 2, chars = listOf(Char(text = "d"))),
            Key(fingerIndex = 3, chars = listOf(Char(text = "f")), left = 0),
            Key(fingerIndex = 3, chars = listOf(Char(text = "g")), left = 10), // has no opponent

            Key(fingerIndex = 4, chars = listOf(Char(text = "j"))),
            Key(fingerIndex = 5, chars = listOf(Char(text = "k"))),
            Key(fingerIndex = 6, chars = listOf(Char(text = "l"))),
            Key(fingerIndex = 7, chars = listOf(Char(text = ";"))),
        ))))
        val pairs = pairKeys(hands)

        pairs.last().finger shouldBe -1
        pairs.last().level shouldBe -1
        pairs.last().index shouldBe -1
    }

    @Test
    fun `pairKeys return empty list when input is null`() {
        pairKeys(null) shouldBe emptyList()
    }

    @Test
    fun `customOrder keyboard layout english USA happy`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText(keyboardLayoutEnglishUSA)
        val keyboardLayout = KeyboardLayout.create(file.absolutePathString())
        val hands = hands(keyboardLayout!!)
        val pairs = pairKeys(hands)

        val customOrder = customOrder(pairs)
        customOrder[0].pair.first!!.chars.map { it.text } shouldContain "f"
        customOrder[0].pair.second!!.chars.map { it.text } shouldContain "j"
        customOrder[1].pair.first!!.chars.map { it.text } shouldContain "d"
        customOrder[1].pair.second!!.chars.map { it.text } shouldContain "k"
        customOrder[2].pair.first!!.chars.map { it.text } shouldContain "s"
        customOrder[2].pair.second!!.chars.map { it.text } shouldContain "l"
        customOrder[3].pair.first!!.chars.map { it.text } shouldContain "a"
        customOrder[3].pair.second!!.chars.map { it.text } shouldContain ";"
        customOrder[4].pair.first!!.chars.map { it.text } shouldContain "g"
        customOrder[4].pair.second!!.chars.map { it.text } shouldContain "h"
        customOrder[5].pair.first!!.chars.map { it.text } shouldContain "t"
        customOrder[5].pair.second!!.chars.map { it.text } shouldContain "y"
        customOrder[6].pair.first!!.chars.map { it.text } shouldContain "v"
        customOrder[6].pair.second!!.chars.map { it.text } shouldContain "m"
        customOrder[7].pair.first!!.chars.map { it.text } shouldContain "b"
        customOrder[7].pair.second!!.chars.map { it.text } shouldContain "n"
        customOrder[8].pair.first!!.chars.map { it.text } shouldContain "r"
        customOrder[8].pair.second!!.chars.map { it.text } shouldContain "u"
        customOrder[9].pair.first!!.chars.map { it.text } shouldContain "e"
        customOrder[9].pair.second!!.chars.map { it.text } shouldContain "i"
        customOrder[10].pair.first!!.chars.map { it.text } shouldContain "c"
        customOrder[10].pair.second!!.chars.map { it.text } shouldContain ","
        customOrder[11].pair.first!!.chars.map { it.text } shouldContain "w"
        customOrder[11].pair.second!!.chars.map { it.text } shouldContain "o"
        customOrder[12].pair.first!!.chars.map { it.text } shouldContain "x"
        customOrder[12].pair.second!!.chars.map { it.text } shouldContain "."
        customOrder[13].pair.first!!.chars.map { it.text } shouldContain "q"
        customOrder[13].pair.second!!.chars.map { it.text } shouldContain "p"
        customOrder[14].pair.first!!.chars.map { it.text } shouldContain "z"
        customOrder[14].pair.second!!.chars.map { it.text } shouldContain "/"
    }

    @Test
    fun `customOrder return empty list on empty input`() {
        customOrder(emptyList()) shouldBe emptyList()
    }

    @Test
    fun `filter keyboard layout english USA filters pairs where the keys match the filter pattern`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText(keyboardLayoutEnglishUSA)
        val keyboardLayout = KeyboardLayout.create(file.absolutePathString())
        val hands = hands(keyboardLayout!!)
        val pairs = pairKeys(hands)

        val letterPairs = filter(pairs, "[a-z]+".toRegex())
        letterPairs shouldHaveSize 15
        letterPairs.forAll { it shouldMatch "[a-z]+".toRegex() }

        val digitPairs = filter(pairs, "[0-9]+".toRegex())
        digitPairs shouldHaveSize 5
        digitPairs.forAll { it shouldMatch "[0-9]+".toRegex() }
    }

    @Test
    fun `filter return empty list on empty input`() {
        filter(emptyList(), ".*".toRegex()) shouldBe emptyList()
    }

    @Test
    fun `upperLetters keyboard layout english USA return upper letter pairs`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText(keyboardLayoutEnglishUSA)
        val keyboardLayout = KeyboardLayout.create(file.absolutePathString())
        val hands = hands(keyboardLayout!!)
        val pairs = pairKeys(hands)
        val letterPairs = upperLetters(pairs)
        letterPairs shouldHaveSize 15
        letterPairs.forAll { it shouldMatch "[A-ZÜÄÖẞ]+".toRegex() }
    }

    @Test
    fun `upperLetters return empty list on empty input`() {
        upperLetters(emptyList()) shouldBe emptyList()
    }

    @Test
    fun `lowerLetters keyboard layout english USA return lower letter pairs`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText(keyboardLayoutEnglishUSA)
        val keyboardLayout = KeyboardLayout.create(file.absolutePathString())
        val hands = hands(keyboardLayout!!)
        val pairs = pairKeys(hands)
        val letterPairs = lowerLetters(pairs)
        letterPairs shouldHaveSize 15
        letterPairs.forAll { it shouldMatch "[a-züäöß]+".toRegex() }
    }

    @Test
    fun `lowerLetters return empty list on empty input`() {
        lowerLetters(emptyList()) shouldBe emptyList()
    }

    @Test
    fun `KeyboardLayout toCourseSymbols english USA return string list of paired symbols`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText(keyboardLayoutEnglishUSA)
        val keyboardLayout = KeyboardLayout.create(file.absolutePathString())
        keyboardLayout!!.toCourseSymbols() shouldBe listOf(
            "fj", "dk", "sl", "a", "gh", "ty", "vm",
            "bn", "ru", "ei", "c", "wo", "x", "qp", "z",
            "FJ", "DK", "SL", "A", "GH", "TY", "VM",
            "BN", "RU", "EI", "C", "WO", "X", "QP", "Z"
        )
    }

    @Test
    fun `create keyboard layout from file, error no such file, return null`() {
        KeyboardLayout.create("") shouldBe null
    }

    @Test
    fun `create keyboard layout from file, error invalid xml, return null`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText(
            """
              <?xml version="1.0"?>
              <keyboa
            """.trimEnd()
        )
        KeyboardLayout.create(file.absolutePathString()) shouldBe null
    }

    @Test
    fun `create keyboard layout from file, error empty keyboard file, return null`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText("")
        KeyboardLayout.create(file.absolutePathString()) shouldBe null
    }

    @Test
    fun `create keyboard layout from file happy`() {
        val file = tmpFile(UUID.randomUUID().toString())
        file.writeText(keyboardLayoutEnglishUSA)
        KeyboardLayout.create(file.absolutePathString()) shouldNotBe null
    }

    private val keyboardLayoutEnglishUSA = """
            <?xml version="1.0"?>
            <keyboardLayout>
             <id>{6a1fed47-1713-437c-931e-2ebc3ba1f366}</id>
             <title>English (USA)</title>
             <name>us</name>
             <width>1430</width>
             <height>480</height>
             <keys>
              <key top="200" left="180" width="80" height="80" fingerIndex="0">
               <char modifier="right_shift" position="topLeft">A</char>
               <char position="hidden">a</char>
              </key>
              <key top="200" left="280" width="80" height="80" fingerIndex="1">
               <char modifier="right_shift" position="topLeft">S</char>
               <char position="hidden">s</char>
              </key>
              <key top="200" left="380" width="80" height="80" fingerIndex="2">
               <char modifier="right_shift" position="topLeft">D</char>
               <char position="hidden">d</char>
              </key>
              <key top="200" left="480" width="80" height="80" hasHapticMarker="true" fingerIndex="3">
               <char modifier="right_shift" position="topLeft">F</char>
               <char position="hidden">f</char>
              </key>
              <key top="200" left="780" width="80" height="80" hasHapticMarker="true" fingerIndex="4">
               <char modifier="left_shift" position="topLeft">J</char>
               <char position="hidden">j</char>
              </key>
              <key top="200" left="880" width="80" height="80" fingerIndex="5">
               <char modifier="left_shift" position="topLeft">K</char>
               <char position="hidden">k</char>
              </key>
              <key top="200" left="980" width="80" height="80" fingerIndex="6">
               <char modifier="left_shift" position="topLeft">L</char>
               <char position="hidden">l</char>
              </key>
              <key top="200" left="1080" width="80" height="80" fingerIndex="7">
               <char modifier="left_shift" position="topLeft">:</char>
               <char position="bottomLeft">;</char>
              </key>
              <key top="0" left="0" width="80" height="80" fingerIndex="0">
               <char modifier="right_shift" position="topLeft">~</char>
               <char position="bottomLeft">`</char>
              </key>
              <key top="0" left="100" width="80" height="80" fingerIndex="0">
               <char modifier="right_shift" position="topLeft">!</char>
               <char position="bottomLeft">1</char>
              </key>
              <key top="0" left="200" width="80" height="80" fingerIndex="1">
               <char modifier="right_shift" position="topLeft">@</char>
               <char position="bottomLeft">2</char>
              </key>
              <key top="0" left="300" width="80" height="80" fingerIndex="2">
               <char modifier="right_shift" position="topLeft">#</char>
               <char position="bottomLeft">3</char>
              </key>
              <key top="0" left="400" width="80" height="80" fingerIndex="3">
               <char modifier="right_shift" position="topLeft">${'$'}</char>
               <char position="bottomLeft">4</char>
              </key>
              <key top="0" left="500" width="80" height="80" fingerIndex="3">
               <char modifier="right_shift" position="topLeft">%</char>
               <char position="bottomLeft">5</char>
              </key>
              <key top="0" left="600" width="80" height="80" fingerIndex="4">
               <char modifier="left_shift" position="topLeft">^</char>
               <char position="bottomLeft">6</char>
              </key>
              <key top="0" left="700" width="80" height="80" fingerIndex="4">
               <char modifier="left_shift" position="topLeft">&amp;</char>
               <char position="bottomLeft">7</char>
              </key>
              <key top="0" left="800" width="80" height="80" fingerIndex="5">
               <char modifier="left_shift" position="topLeft">*</char>
               <char position="bottomLeft">8</char>
              </key>
              <key top="0" left="900" width="80" height="80" fingerIndex="6">
               <char modifier="left_shift" position="topLeft">(</char>
               <char position="bottomLeft">9</char>
              </key>
              <key top="0" left="1000" width="80" height="80" fingerIndex="7">
               <char modifier="left_shift" position="topLeft">)</char>
               <char position="bottomLeft">0</char>
              </key>
              <key top="0" left="1100" width="80" height="80" fingerIndex="7">
               <char modifier="left_shift" position="topLeft">_</char>
               <char position="bottomLeft">-</char>
              </key>
              <key top="0" left="1200" width="80" height="80" fingerIndex="7">
               <char modifier="left_shift" position="topLeft">+</char>
               <char position="bottomLeft">=</char>
              </key>
              <key top="100" left="150" width="80" height="80" fingerIndex="0">
               <char modifier="right_shift" position="topLeft">Q</char>
               <char position="hidden">q</char>
              </key>
              <key top="100" left="250" width="80" height="80" fingerIndex="1">
               <char modifier="right_shift" position="topLeft">W</char>
               <char position="hidden">w</char>
              </key>
              <key top="100" left="350" width="80" height="80" fingerIndex="2">
               <char modifier="right_shift" position="topLeft">E</char>
               <char position="hidden">e</char>
              </key>
              <key top="100" left="450" width="80" height="80" fingerIndex="3">
               <char modifier="right_shift" position="topLeft">R</char>
               <char position="hidden">r</char>
              </key>
              <key top="100" left="550" width="80" height="80" fingerIndex="3">
               <char modifier="right_shift" position="topLeft">T</char>
               <char position="hidden">t</char>
              </key>
              <key top="300" left="230" width="80" height="80" fingerIndex="0">
               <char modifier="right_shift" position="topLeft">Z</char>
               <char position="hidden">z</char>
              </key>
              <key top="100" left="750" width="80" height="80" fingerIndex="4">
               <char modifier="left_shift" position="topLeft">U</char>
               <char position="hidden">u</char>
              </key>
              <key top="100" left="850" width="80" height="80" fingerIndex="5">
               <char modifier="left_shift" position="topLeft">I</char>
               <char position="hidden">i</char>
              </key>
              <key top="100" left="950" width="80" height="80" fingerIndex="6">
               <char modifier="left_shift" position="topLeft">O</char>
               <char position="hidden">o</char>
              </key>
              <key top="100" left="1050" width="80" height="80" fingerIndex="7">
               <char modifier="left_shift" position="topLeft">P</char>
               <char position="hidden">p</char>
              </key>
              <key top="100" left="1150" width="80" height="80" fingerIndex="7">
               <char modifier="left_shift" position="topLeft">{</char>
               <char position="bottomLeft">[</char>
              </key>
              <key top="100" left="1250" width="80" height="80" fingerIndex="7">
               <char modifier="left_shift" position="topLeft">}</char>
               <char position="bottomLeft">]</char>
              </key>
              <key top="200" left="580" width="80" height="80" fingerIndex="3">
               <char modifier="right_shift" position="topLeft">G</char>
               <char position="hidden">g</char>
              </key>
              <key top="200" left="680" width="80" height="80" fingerIndex="4">
               <char modifier="left_shift" position="topLeft">H</char>
               <char position="hidden">h</char>
              </key>
              <key top="200" left="1180" width="80" height="80" fingerIndex="7">
               <char modifier="left_shift" position="topLeft">"</char>
               <char position="bottomLeft">'</char>
              </key>
              <key top="100" left="1350" width="80" height="80" fingerIndex="7">
               <char modifier="left_shift" position="topLeft">|</char>
               <char position="bottomLeft">\</char>
              </key>
              <key top="100" left="650" width="80" height="80" fingerIndex="4">
               <char modifier="left_shift" position="topLeft">Y</char>
               <char position="hidden">y</char>
              </key>
              <key top="300" left="330" width="80" height="80" fingerIndex="1">
               <char modifier="right_shift" position="topLeft">X</char>
               <char position="hidden">x</char>
              </key>
              <key top="300" left="430" width="80" height="80" fingerIndex="2">
               <char modifier="right_shift" position="topLeft">C</char>
               <char position="hidden">c</char>
              </key>
              <key top="300" left="530" width="80" height="80" fingerIndex="3">
               <char modifier="right_shift" position="topLeft">V</char>
               <char position="hidden">v</char>
              </key>
              <key top="300" left="630" width="80" height="80" fingerIndex="3">
               <char modifier="right_shift" position="topLeft">B</char>
               <char position="hidden">b</char>
              </key>
              <key top="300" left="730" width="80" height="80" fingerIndex="4">
               <char modifier="left_shift" position="topLeft">N</char>
               <char position="hidden">n</char>
              </key>
              <key top="300" left="830" width="80" height="80" fingerIndex="4">
               <char modifier="left_shift" position="topLeft">M</char>
               <char position="hidden">m</char>
              </key>
              <key top="300" left="930" width="80" height="80" fingerIndex="5">
               <char modifier="left_shift" position="topLeft">&lt;</char>
               <char position="bottomLeft">,</char>
              </key>
              <key top="300" left="1030" width="80" height="80" fingerIndex="6">
               <char modifier="left_shift" position="topLeft">></char>
               <char position="bottomLeft">.</char>
              </key>
              <key top="300" left="1130" width="80" height="80" fingerIndex="7">
               <char modifier="left_shift" position="topLeft">?</char>
               <char position="bottomLeft">/</char>
              </key>
              <specialKey top="100" left="0" width="130" height="80" type="tab"/>
              <specialKey top="200" left="1280" width="150" height="80" type="return"/>
              <specialKey top="300" left="1230" width="200" height="80" modifierId="right_shift" type="shift"/>
              <specialKey top="400" left="1150" width="130" height="80" label="Alt" type="other"/>
              <specialKey top="400" left="1300" width="130" height="80" label="Ctrl" type="other"/>
              <specialKey top="400" left="150" width="130" height="80" label="Alt" type="other"/>
              <specialKey top="400" left="0" width="130" height="80" label="Ctrl" type="other"/>
              <specialKey top="400" left="300" width="830" height="80" type="space"/>
              <specialKey top="300" left="0" width="210" height="80" modifierId="left_shift" type="shift"/>
              <specialKey top="200" left="0" width="160" height="80" type="capslock"/>
              <specialKey top="0" left="1300" width="130" height="80" type="backspace"/>
             </keys>
            </keyboardLayout>
        """.trimIndent()
}