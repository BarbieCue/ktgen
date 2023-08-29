package ktgen

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.io.PrintStream


class ApplicationKtTest : ExpectSpec({

    val errStreamCaptor = ByteArrayOutputStream()
    beforeEach {
        errStreamCaptor.reset()
        System.setErr(PrintStream(errStreamCaptor))
    }

    afterEach {
        System.setErr(System.err)
    }

    expect("write 'missing input' message to stderr on missing lesson specification") {
        main(arrayOf(""))
        errStreamCaptor.toString().trim() shouldBe "Missing (or empty) lesson specification. No course is created."
    }

    expect("write 'lesson length to small' message to stderr if lesson length < 1") {
        main(arrayOf("abc", "-length", "0"))
        errStreamCaptor.toString().trim() shouldBe "The lesson length must be at least 1."
    }

    expect("write 'line length to small' message to stderr if line length < 1") {
        main(arrayOf("abc", "-line", "0"))
        errStreamCaptor.toString().trim() shouldBe "The average line length must be at least 1."
    }

    expect("write 'text distance out of range' message to stderr if it is not in regular range") {
        val message = "Text distance must be in [0.0, 1.0]. Proceeding without text distance check."

        main(arrayOf("abc", "-td", "-0.1"))
        errStreamCaptor.toString().trim() shouldBe message

        errStreamCaptor.reset()

        main(arrayOf("abc", "-td", "1.1"))
        errStreamCaptor.toString().trim() shouldBe message
    }

    expect("write 'skipping word diversity check' message to stderr if word diversity input is < 1") {
        main(arrayOf("abc", "-wd", "0"))
        errStreamCaptor.toString().trim() shouldBe "Minimum word diversity per lesson must be at least 1. Proceeding without word diversity check."
    }

    expect("write 'proceeding without dictionary' message to stderr if min word length is greater than max word length") {
        main(arrayOf("abc", "-min", "10", "-max", "2"))
        errStreamCaptor.toString().trim() shouldBe "The minimum word length is greater than maximum word length. No dictionary is used."
    }

    expect("write 'proceeding without dictionary' message to stderr if dictionary size is defined < 1") {
        main(arrayOf("abc", "-size", "0"))
        errStreamCaptor.toString().trim() shouldBe "The dictionary size must be at least 1. No dictionary is used."
    }
})