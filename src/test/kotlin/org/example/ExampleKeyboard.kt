package org.example

import kotlinx.serialization.serializer
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML

internal fun exampleKeyboardEnglishUSA(): KeyboardLayout? = try {
    val xml = XML {
        xmlVersion = XmlVersion.XML10
    }
    val serializer = serializer<KeyboardLayout>()
    xml.decodeFromString(serializer, ktouchKeyboardLayoutEnglishUSA)
} catch (e: Exception) {
    System.err.println("Error on parsing example keyboard ${e.message}")
    throw e
}

internal val ktouchKeyboardLayoutEnglishUSA = """
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