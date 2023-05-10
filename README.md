# ktgen

[KTouch](https://github.com/KDE/ktouch) Course Generator


## Prerequisites

Java 17 or higher

### Build ktgen
```shell
./gradlew buildFatJar
```


## Basic use


### Define your course

Edit the lesson specification file `lesson_specification.ktgen` for your needs.

```text
ab cd ef gh ij kl mn op qr st uv wx yz
AB CD EF GH IJ KL MN OP QR ST UV WX YZ
WW,. WW!? "WW" 'WW' (WW) {WW} [WW] <WW>
~` +* #$ |& ^- :; 01 23 45 67 89
```

The format is explained below.


### Run ktgen

```shell
java -jar build/libs/ktgen.jar
```


### Done

Your KTouch course has been generated and written to `ktgen_course.xml`.
It can be imported into KTouch.


## Dictionary

Provide some kind of dictionary to **add words to your lessons**.
The first lessons are mostly created with random combinations of letters,
because there are not enough letters to find meaningful words.
The word order will be preserved by default, making it possible to build
meaningful **sentences** automatically.

There are two ways to equip _ktgen_ with a dictionary. They can be combined with each other.


### Text file

Use `-file <path>` to point to a dictionary file.
No matter if it contains continuous text or one word per line.

```shell
java -jar build/libs/ktgen.jar -file README.md
```


### Website

Link a website of your choice with `-web <url>` and let _ktgen_
extract text from it.

```shell
java -jar build/libs/ktgen.jar -web https://en.wikipedia.org/wiki/Barbie
```

![keyboard path](docs/text-from-website.jpg)


## Lesson specification

As mentioned above, the `lesson_specification.ktgen` file describes the lessons for your course.
The following format applies here.


### Separation with whitespace characters

Each segment separated by whitespace characters defines new symbols
for which lessons are generated. The read order is left to right and top down.

```text
ab cdef gh
AB
CD
EFGH
123
,.
```

Here we have 8 segments of new symbols to learn: `ab`, `cdef`, `gh`, `AB`, `CD`, `EFGH`, `123`, `,.`.


#### This example leads to the following lessons:

1. Random permutations of `ab`
2. Random permutations of `ab` mixed with words consisting of `ab`
3. Words consisting of `ab`
4. Random permutations of `cdef`
5. Random permutations of `cdef` mixed with words consisting of `abcdef` where each
   word contains at least one of the symbols `cdef`
6. Such words only
7. Random permutations of `gh`
8. Random permutations of `gh` mixed with words consisting of `abcdefgh` where each
   word contains at least one of the symbols `gh`
9. Such words only
10. Random permutations of `AB`
11. Random permutations of `AB` mixed with words consisting of `abcdefghAB` where each
    word contains at least one of the symbols `AB`
12. Such words only
13. Random permutations of `CD`
14. Random permutations of `CD` mixed with words consisting of `abcdefghABCD` where each
    word contains at least one of the symbols `CD`
15. Such words only
16. Random permutations of `EFGH`
17. Random permutations of `EFGH` mixed with words consisting of `abcdefghABCDEFGH` where each
    word contains at least one of the symbols `EFGH`
18. Such words only
19. Random permutations of `123`
20. Random permutations of `,.`
21. Words consisting of `abcdefghABCDEFGH` where `,` or `.` is prefixed or appended randomly

However, no lessons containing words are generated if no matching words can be found in the dictionary.


### Allowed symbols

Allowed symbols are `A-Z`, `ÄÖÜ`, `ß` in lowercase and uppercase, `0-9`
and `!"#$%&'()*+,-./:;<=>?@[]^_{|}~\`.


### Punctuation marks

If the side does not matter so that a punctuation mark can be to the left or to the right of the word,
you can just write it down.

```text
ab cd ef gh ij kl mn op qr st uv wx yz
\/ +- ,;
```

This produces words like `\pear`, `barbie/`, `+pear`, `-apple`, `,barbie`, `pear;`, ..

But, if the side matters, so that the punctuation mark can only be
to the left or only to the right of a word, then use the `WW` pattern (_WW_ stands for _word_).

```text
ab cd ef gh ij kl mn op qr st uv wx yz
WW., [(WW)] {WW}
```

This produces words like `(pear)`, `[barbie]`, `{pear}`, `apple.`, `barbie,` ..


### Letter groups

In every language, letters appear as fixed groups.
For example, _"tt"_, _"ss"_, _"ch"_, _"nn"_ or _"sch"_.
Lessons for such groups are generated with square brackets.

```text
ab cd ef gh ij kl mn op qr st uv wx yz
[ch] [tt] [ss]
```

This produces words like `china`, `letter`, `lesson`.


## Keyboard layout

You can use a keyboard layout to create finger-by-finger lessons that
focus on the key neighbors. Lowercase and uppercase letters are contained.
A course created in this way follows an intuitive path across the keys
on the keyboard, starting with the basic finger position.

![keyboard path](docs/keyboardpath.jpg)


1. Export a keyboard layout from KTouch (e.g `german-layout.xml`)
2. Pass it to _ktgen_ with `-k german-layout.xml`

```shell
java -jar build/libs/ktgen.jar -k docs/german-layout.xml
```


## Stdin and Stdout

- You can pass the lesson specification as string to stdin. For example `-i "ab cd ef [WW]"`
- The created course can be written to stdout using the `-o`.


### IO combination


#### Input

All possibilities of entering lesson specifications can be combined (additive).
They apply as follows:

1. Stdin `-i <string>`
2. Specification file `-if <file>`
3. Keyboard layout `-k <file>`

The specification file `lesson_specification.ktgen` is used if none of these options is set.


#### Output

Write the course to stdout (`-o`) or to a file (`-of <file>`) or to both.
The course is written to the `ktgen_course.xml` file if none of the options is set.


## Help

```shell
java -jar build/libs/ktgen.jar --help
```


## Examples

A course with lessons defined in _mylessons.txt_,
containing words from the file _mydict.txt_ and from the website _https://example.com_

```shell
java -jar build/libs/ktgen.jar -file mydict.txt -web https://example.com -if mylessons.txt
```

A course for the german keyboard layout,
containing words from the website _https://de.wikipedia.org/wiki/Ameisen_

```shell
java -jar build/libs/ktgen.jar -k docs/german-layout.xml -web https://de.wikipedia.org/wiki/Ameisen
```

A course with two lessons `asdf` and `jklö`, read from stdin and write to stdout

```shell
java -jar build/libs/ktgen.jar -i "asdf jklö" -o
```