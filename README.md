# ktgen

[KTouch](https://github.com/KDE/ktouch) Course Generator


## Basic use


### 1. Define your course

Edit the lesson specification file `lesson_specification.ktgen` for your needs.

```text
ab cd ef gh ij kl mn op qr st uv wx yz
AB CD EF GH IJ KL MN OP QR ST UV WX YZ
WW,. WW!? "WW" 'WW' (WW) {WW} [WW] <WW>
~` +* #$ |& ^- :; 01 23 45 67 89
```

The format is explained below.


### 2. Run ktgen

Via Docker or Java (explained below).


### 3. Done

Your KTouch course has been generated and written to `ktgen_course.xml`.
It can be imported into KTouch.


## Run with existing Docker Image

```shell
docker run \
-v path/to/your/lesson_specification.ktgen:/app/lesson_specification.ktgen \
barbiecue/ktgen:latest /app/lesson_specification.ktgen \
-o > ktgen_course.xml
```

Pass your custom *lesson_specification.ktgen* to the docker container via volume bind.
So change `path/to/your/lesson_specification.ktgen` accordingly.


## Run locally with Java

Requires Java 17 or higher

### Build ktgen

```shell
./gradlew buildFatJar
```

### Run ktgen

```shell
java -jar build/libs/ktgen.jar
```

## Dictionary

Provide some kind of dictionary to **add words to your course**.
The first lessons are mostly created with random combinations of letters,
because there are not enough letters to find meaningful words.
As the number of letters increases, more and more words can be found.
The word order will be preserved by default, making it possible to build
meaningful **sentences** automatically.

There are two ways to equip *ktgen* with a dictionary. They can be combined with each other.


### Text file

Use `-file <path>` to point to a dictionary file.
No matter if it contains continuous text or one word per line.

```shell
java -jar build/libs/ktgen.jar -file README.md
```


### Website

Link a website of your choice with `-web <url>` and let *ktgen*
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
to the left or only to the right of a word, then use the `WW` pattern (*WW* stands for *word*).

```text
ab cd ef gh ij kl mn op qr st uv wx yz
WW., [(WW)] {WW}
```

This produces words like `(pear)`, `[barbie]`, `{pear}`, `apple.`, `barbie,` ..


### Letter groups

In every language, letters appear as fixed groups.
For example, *"tt"*, *"ss"*, *"ch"*, *"nn"* or *"sch"*.
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
2. Pass it as argument to *ktgen*

```shell
java -jar build/libs/ktgen.jar docs/examples/german-layout.xml
```


## Combining lesson specifications

Multiple lessons specifications can be combined to a single course, simply by passing them as arguments to *ktgen*.
They are applied in order. For example:

```shell
java -jar build/libs/ktgen.jar lesson_specification.ktgen docs/examples/german-layout.xml docs/examples/letters.ktgen
```
The first lessons are created from *lesson_specification.ktgen*.
Then the lessons for the German keyboard layout follow and at the end of the course 
the lessons from the file *another_spec.txt*.

The specification file `lesson_specification.ktgen` is used by default,
if no lesson specification is passed as argument.


## Output

You can write the course to stdout `-o` or to a file `-of <file>` or to both.

The course will be written to the `ktgen_course.xml` file by default if none of the options are set.


## Examples

-   A course that starts with lessons for *letters.ktgen* and ends with lessons for *digits.ktgen*
    written to *ktgen_course.xml*.

    ```shell
    java -jar build/libs/ktgen.jar ./docs/examples/letters.ktgen ./docs/examples/digits.ktgen
    ```

    ```shell
    docker run \
    -v ./docs/examples/:/app/files \
    barbiecue/ktgen:latest /app/files/letters.ktgen /app/files/digits.ktgen \
    -o > ktgen_course.xml
    ```

-   A course with lessons from *letters.ktgen* containing words from the file 
    *mydict.txt* and from the website *https://docs.dagger.io/* written to *ktgen_course.xml*.
    
    ```shell
    java -jar build/libs/ktgen.jar -file ./docs/examples/mydict.txt -web https://docs.dagger.io/ ./docs/examples/letters.ktgen
    ```
    
    ```shell
    docker run \
    -v ./docs/examples/:/app/files \
    barbiecue/ktgen:latest /app/files/letters.ktgen \
    -file /app/files/mydict.txt \
    -web https://docs.dagger.io/ \
    -o > ktgen_course.xml
    ```

-   A course for the german keyboard layout containing words from the website _https://de.wikipedia.org/wiki/Ameisen_
    written to *ktgen_course.xml*.
    
    ```shell
    java -jar build/libs/ktgen.jar docs/examples/german-layout.xml -web https://de.wikipedia.org/wiki/Ameisen
    ```

    ```shell
    docker run \
    -v ./docs/examples/:/app/files \
    barbiecue/ktgen:latest /app/files/german-layout.xml \
    -web https://de.wikipedia.org/wiki/Ameisen \
    -o > ktgen_course.xml
    ```

-   A course for the german keyboard layout containing words from the website _https://de.wikipedia.org/wiki/Ameisen_
    where each word is max 10 characters long and each lesson has a length of 500 characters. 
    Written to *ktgen_course.xml*.

    ```shell
    java -jar build/libs/ktgen.jar docs/examples/german-layout.xml -web https://de.wikipedia.org/wiki/Ameisen -max 10 -length 500
    ```

    ```shell
    docker run \
    -v ./docs/examples/:/app/files \
    barbiecue/ktgen:latest /app/files/german-layout.xml \
    -web https://de.wikipedia.org/wiki/Ameisen \
    -max 10 \
    -length 500 \
    -o > ktgen_course.xml
    ```

-   A course for the german keyboard layout written to the file *my_ktouch_course.xml*.
    
    ```shell
    java -jar build/libs/ktgen.jar -of my_ktouch_course.xml docs/examples/german-layout.xml
    ```

    ```shell
    docker run \
    -v ./docs/examples/:/app/files \
    barbiecue/ktgen:latest /app/files/german-layout.xml \
    -o > my_ktouch_course.xml
    ```

-   A course for *lesson_specification.ktgen* written to stdout.
    
    ```shell
    java -jar build/libs/ktgen.jar -o
    ```

    ```shell
    docker run \
    -v ./lesson_specification.ktgen:/app/lesson_specification.ktgen \
    barbiecue/ktgen:latest /app/lesson_specification.ktgen -o
    ```


## Help

```shell
java -jar build/libs/ktgen.jar --help
```