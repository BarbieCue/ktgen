package org.example

import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeyboardKtTest {

    private val files = mutableListOf<String>()

    @AfterAll
    fun deleteFiles() {
        files.forEach { File(it).delete() }
    }

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
    fun `hands single level, 8 keys, one single key for each finger`() {
        val keyboardLayout = mockk<KeyboardLayout>()
        val keyList = mockk<Keys> { every { keys } returns listOf(
            mockedKey("a", 0, 0),
            mockedKey("s", 1, 1),
            mockedKey("d", 2, 2),
            mockedKey("f", 3, 3),
            mockedKey("j", 4, 4),
            mockedKey("k", 5, 5),
            mockedKey("l", 6, 6),
            mockedKey("ö", 7, 7),
        ) }
        every { keyboardLayout.keys } returns keyList

        val (left, right) = hands(keyboardLayout)!!

        // The algorithm reads fingers for both hands from outer to inner; from little finger to index finger:
        // 0 little, 1 ring, 2 middle, 3 index

        // 4 fingers (finger index 0-3)
        left shouldHaveSize 4

        left[0][0] shouldHaveSize 1 // little
        left[0][0].first().chars.single().text shouldBe "a"
        left[1][0] shouldHaveSize 1 // ring
        left[1][0].first().chars.single().text shouldBe "s"
        left[2][0] shouldHaveSize 1 // middle
        left[2][0].first().chars.single().text shouldBe "d"
        left[3][0] shouldHaveSize 1 // index
        left[3][0].first().chars.single().text shouldBe "f"

        // 4 fingers (finger index 4-7)
        right shouldHaveSize 4

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
    fun `hands single level, key sorting test, 16 keys, multiple keys per finger `() {
        val keyboardLayout = mockk<KeyboardLayout>()
        val keyList = mockk<Keys> { every { keys } returns listOf(
            mockedKey("a1", 0, 0),
            mockedKey("a2", 0, 1),
            mockedKey("b1", 1, 2),
            mockedKey("b2", 1, 3),
            mockedKey("c1", 2, 4),
            mockedKey("c2", 2, 5),
            mockedKey("d1", 3, 6),
            mockedKey("d2", 3, 7),
            mockedKey("e1", 4, 8),
            mockedKey("e2", 4, 9),
            mockedKey("f1", 5, 10),
            mockedKey("f2", 5, 11),
            mockedKey("g1", 6, 12),
            mockedKey("g2", 6, 13),
            mockedKey("h1", 7, 14),
            mockedKey("h2", 7, 15),
        ) }
        every { keyboardLayout.keys } returns keyList

        val (left, right) = hands(keyboardLayout)!!

        // left hand's keys on the same level are ordered right to left, except index
        left[0][0] shouldHaveSize 2 // little
        left[0][0][0].chars.single().text shouldBe "a2"
        left[0][0][1].chars.single().text shouldBe "a1"
        left[1][0] shouldHaveSize 2 // ring
        left[1][0][0].chars.single().text shouldBe "b2"
        left[1][0][1].chars.single().text shouldBe "b1"
        left[2][0] shouldHaveSize 2 // middle
        left[2][0][0].chars.single().text shouldBe "c2"
        left[2][0][1].chars.single().text shouldBe "c1"
        left[3][0] shouldHaveSize 2 // index
        left[3][0][0].chars.single().text shouldBe "d1"
        left[3][0][1].chars.single().text shouldBe "d2"

        // right hand's keys on the same level are ordered left to right, except index
        right[3][0] shouldHaveSize 2 // index
        right[3][0][0].chars.single().text shouldBe "e2"
        right[3][0][1].chars.single().text shouldBe "e1"
        right[2][0] shouldHaveSize 2 // middle
        right[2][0][0].chars.single().text shouldBe "f1"
        right[2][0][1].chars.single().text shouldBe "f2"
        right[1][0] shouldHaveSize 2 // ring
        right[1][0][0].chars.single().text shouldBe "g1"
        right[1][0][1].chars.single().text shouldBe "g2"
        right[0][0] shouldHaveSize 2 // little
        right[0][0][0].chars.single().text shouldBe "h1"
        right[0][0][1].chars.single().text shouldBe "h2"
    }

    @Test
    fun `hands single level, less than 8 fingers`() {
        val keyboardLayout = mockk<KeyboardLayout>()

        // finger index 0 - 6 = only 7 fingers
        val keyList = mockk<Keys> { every { keys } returns listOf(
            mockedKey("a", 0, 0),
            mockedKey("s", 1, 1),
            mockedKey("d", 2, 2),
            mockedKey("f", 3, 3),
            mockedKey("j", 4, 4),
            mockedKey("k", 5, 5),
            mockedKey("l", 6, 6),
        ) }
        every { keyboardLayout.keys } returns keyList
        hands(keyboardLayout) shouldBe null
    }

    @Test
    fun `hands single level, more than 8 fingers`() {
        val keyboardLayout = mockk<KeyboardLayout>()

        // finger index 0 - 8 = 9 fingers
        val keyList = mockk<Keys> { every { keys } returns listOf(
            mockedKey("a", 0, 0),
            mockedKey("s", 1, 1),
            mockedKey("d", 2, 2),
            mockedKey("f", 3, 3),
            mockedKey("j", 4, 4),
            mockedKey("k", 5, 5),
            mockedKey("l", 6, 6),
            mockedKey("m", 7, 7),
            mockedKey("n", 8, 8),
        ) }
        every { keyboardLayout.keys } returns keyList
        hands(keyboardLayout) shouldBe null
    }

    @Test
    fun `hands multi level, 16 keys, each finger has two keys from different levels`() {

        // The algorithm handles 4 levels and starts counting them from top down
        // 0 = top, 1 = upper, 2 = middle, 3 = lower

        val keyboardLayout = mockk<KeyboardLayout>()
        val keyList = mockk<Keys> { every { keys } returns listOf(
            mockedKey("a1", 0, 0, 0),
            mockedKey("a2", 0, 1, 1),
            mockedKey("b1", 1, 0, 2),
            mockedKey("b2", 1, 1, 3),
            mockedKey("c1", 2, 0, 4),
            mockedKey("c2", 2, 1, 5),
            mockedKey("d1", 3, 0, 6),
            mockedKey("d2", 3, 1, 7),
            mockedKey("e1", 4, 0, 8),
            mockedKey("e2", 4, 1, 9),
            mockedKey("f1", 5, 0, 10),
            mockedKey("f2", 5, 1, 11),
            mockedKey("g1", 6, 0, 12),
            mockedKey("g2", 6, 1, 13),
            mockedKey("h1", 7, 0, 14),
            mockedKey("h2", 7, 1, 15),
        ) }
        every { keyboardLayout.keys } returns keyList

        val (left, right) = hands(keyboardLayout)!!

        // The algorithm iterates through the levels of a finger from top down
        // 0 = top -> 1 = upper -> 2 = middle -> 3 = lower

        // left hand's keys

        // little
        left[0][0][0].chars.single().text shouldBe "a1"
        left[0][1][0].chars.single().text shouldBe "a2"
        // ring
        left[1][0][0].chars.single().text shouldBe "b1"
        left[1][1][0].chars.single().text shouldBe "b2"
        // middle
        left[2][0][0].chars.single().text shouldBe "c1"
        left[2][1][0].chars.single().text shouldBe "c2"
        // index
        left[3][0][0].chars.single().text shouldBe "d1"
        left[3][1][0].chars.single().text shouldBe "d2"

        // right hand's keys

        // little
        right[3][0][0].chars.single().text shouldBe "e1"
        right[3][1][0].chars.single().text shouldBe "e2"
        // ring
        right[2][0][0].chars.single().text shouldBe "f1"
        right[2][1][0].chars.single().text shouldBe "f2"
        // middle
        right[1][0][0].chars.single().text shouldBe "g1"
        right[1][1][0].chars.single().text shouldBe "g2"
        // index
        right[0][0][0].chars.single().text shouldBe "h1"
        right[0][1][0].chars.single().text shouldBe "h2"
    }

    @Test
    fun `hands when keyboard layout has no keys`() {
        val kb = KeyboardLayout("...", "keyless keyboard", "keyless keyboard", 1000, 1000, Keys(emptyList(), emptyList()))
        hands(kb) shouldBe null
    }

    @Test
    fun `pairKeys happy`() {
        val keyboardLayout = mockk<KeyboardLayout>()
        val keyList = mockk<Keys> { every { keys } returns listOf(

            // left
            mockedKey("a", 0, 0), // little
            mockedKey("s", 1, 1), // ring
            mockedKey("d", 2, 2), // middle
            mockedKey("f", 3, 3), // index

            // right
            mockedKey("j", 4, 4), // index
            mockedKey("k", 5, 5), // middle
            mockedKey("l", 6, 6), // ring
            mockedKey(";", 7, 7), // little
        ) }
        every { keyboardLayout.keys } returns keyList

        val hands = hands(keyboardLayout)
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
    fun `pairKeys no opponents`() {
        val keyboardLayout = mockk<KeyboardLayout>()
        val keyList = mockk<Keys> { every { keys } returns listOf(

            // left
            mockedKey("a", 0, 0), // little
            mockedKey("s", 1, 1), // ring
            mockedKey("d", 2, 2), // middle
            mockedKey("f", 3, 3), // index

            mockedKey("g", 3, 4), // index, has no opponent

            // right
            mockedKey("j", 4, 4), // index
            mockedKey("k", 5, 5), // middle
            mockedKey("l", 6, 6), // ring
            mockedKey(";", 7, 7), // little
        ) }
        every { keyboardLayout.keys } returns keyList

        val hands = hands(keyboardLayout)
        val pairs = pairKeys(hands)

        pairs[0].pair.first!!.chars.first().text shouldBe "a"  // left little to
        pairs[0].pair.second!!.chars.first().text shouldBe ";" // right little

        pairs[1].pair.first!!.chars.first().text shouldBe "s"  // left ring to
        pairs[1].pair.second!!.chars.first().text shouldBe "l" // right ring

        pairs[2].pair.first!!.chars.first().text shouldBe "d"  // left middle to
        pairs[2].pair.second!!.chars.first().text shouldBe "k" // right middle

        pairs[3].pair.first!!.chars.first().text shouldBe "f"  // left index to
        pairs[3].pair.second!!.chars.first().text shouldBe "j" // right index

        pairs[4].pair.first!!.chars.first().text shouldBe "g" // no opponent
        pairs[4].finger shouldBe -1
        pairs[4].level shouldBe -1
        pairs[4].index shouldBe -1
        pairs[4].pair.second shouldBe null
    }

    private fun mockedKey(char: String, fingerindex: Int, positionLeft: Int) = mockk<Key> {
        every { chars } returns listOf(mockk { every { text } returns char })
        every { fingerIndex } returns fingerindex
        every { top } returns 0
        every { left } returns positionLeft
    }

    private fun mockedKey(char: String, fingerindex: Int, positionTop: Int, positionLeft: Int) = mockk<Key> {
        every { chars } returns listOf(mockk { every { text } returns char })
        every { fingerIndex } returns fingerindex
        every { top } returns positionTop
        every { left } returns positionLeft
    }

    @Test
    fun `pairKeys input is null`() {
        pairKeys(null) shouldBe emptyList()
    }

    @Test
    fun `customOrder keyboard layout english USA happy`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText(keyboardLayoutEnglishUSA)
        val keyboardLayout = KeyboardLayout.create(filename)
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
    fun `customOrder empty input`() {
        customOrder(emptyList()) shouldBe emptyList()
    }

    @Test
    fun `filter keyboard layout english USA happy`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText(keyboardLayoutEnglishUSA)
        val keyboardLayout = KeyboardLayout.create(filename)
        val hands = hands(keyboardLayout!!)
        val pairs = pairKeys(hands)
        filter(pairs, "[0-9]+".toRegex()).forAll { it shouldMatch "[0-9]+".toRegex() }
        filter(pairs, "[A-Z]+".toRegex()).forAll { it shouldMatch "[A-Z]+".toRegex() }
        filter(pairs, "[a-z]+".toRegex()).forAll { it shouldMatch "[a-z]+".toRegex() }
    }

    @Test
    fun `filter empty input`() {
        filter(emptyList(), ".*".toRegex()) shouldBe emptyList()
    }

    @Test
    fun `upperLetters keyboard layout english USA happy`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText(keyboardLayoutEnglishUSA)
        val keyboardLayout = KeyboardLayout.create(filename)
        val hands = hands(keyboardLayout!!)
        val pairs = pairKeys(hands)
        upperLetters(pairs).forAll { it shouldMatch "[A-ZÜÄÖẞ]+".toRegex() }
    }

    @Test
    fun `upperLetters empty input`() {
        upperLetters(emptyList()) shouldBe emptyList()
    }

    @Test
    fun `lowerLetters keyboard layout english USA happy`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText(keyboardLayoutEnglishUSA)
        val keyboardLayout = KeyboardLayout.create(filename)
        val hands = hands(keyboardLayout!!)
        val pairs = pairKeys(hands)
        lowerLetters(pairs).forAll { it shouldMatch "[a-züäöß]+".toRegex() }
    }

    @Test
    fun `lowerLetters empty input`() {
        lowerLetters(emptyList()) shouldBe emptyList()
    }

    @Test
    fun `KeyboardLayout toCourseSymbols english USA happy`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText(keyboardLayoutEnglishUSA)
        val keyboardLayout = KeyboardLayout.create(filename)
        keyboardLayout!!.toCourseSymbols() shouldBe listOf(
            "fj", "dk", "sl", "a", "gh", "ty", "vm",
            "bn", "ru", "ei", "c", "wo", "x", "qp", "z",
            "FJ", "DK", "SL", "A", "GH", "TY", "VM",
            "BN", "RU", "EI", "C", "WO", "X", "QP", "Z"
        )
    }

    @Test
    fun `create keyboard layout from file, error no such file`() {
        KeyboardLayout.create("") shouldBe null
    }

    @Test
    fun `create keyboard layout from file, error invalid xml`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText(
            """
              <?xml version="1.0"?>
              <keyboa
            """.trimEnd()
        )
        KeyboardLayout.create(filename) shouldBe null
    }

    @Test
    fun `create keyboard layout from file, error empty keyboard file`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText("")
        KeyboardLayout.create(filename) shouldBe null
    }

    @Test
    fun `create keyboard layout from file happy`() {
        val filename = UUID.randomUUID().toString()
        files.add(filename)
        File(filename).writeText(keyboardLayoutEnglishUSA)
        KeyboardLayout.create(filename) shouldNotBe null
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