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

Edit the course definition file `course_definition.ktgen` for your needs.

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

Provide some kind of dictionary to add words to your lessons.
The first lessons are mostly created with random combinations of letters,
because there are not enough letters to find meaningful **words**.
The word order will be preserved by default, making it possible to build 
meaningful **sentences** automatically.

There are two ways to equip _ktgen_ with a dictionary.


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


## Course definition

As mentioned above, the `course_definition.ktgen` file describes your course.
The following format applies here.


### Separation with whitespace characters

Each segment separated by whitespace characters defines new symbols
for which lessons are generated.

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
2. Words consisting of `ab`
3. Random permutations of `cdef`
4. Words consisting of `abcdef` where each word contains at least one of the symbols `cdef`
5. Random permutations of `gh`
6. Words consisting of `abcdefgh` where each word contains at least one of the symbols `gh`
7. Random permutations of `AB`
8. Words consisting of `abcdefghAB` where each word contains at least one of the symbols `AB`
9. Random permutations of `CD`
10. Words consisting of `abcdefghABCD` where each word contains at least one of the symbols `CD`
11. Random permutations of `EFGH`
12. Words consisting of `abcdefghABCDEFGH` where each word contains at least one of the symbols `EFGH`
13. Random permutations of `123`
14. Random permutations of `,.`
15. Words consisting of `abcdefghABCDEFGH` where `,` or `.` is prefixed or appended randomly

However, no word lessons are generated if no matching words can be found in the dictionary.

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


### Combination

Generating keyboard layout lessons can be combined with
your custom lesson generation.
This happens automatically when the `course_definition.ktgen` file
is not empty.
Keyboard layout lessons are generated first,
your custom lessons afterwards.

## Help

```shell
java -jar build/libs/ktgen.jar --help
```
