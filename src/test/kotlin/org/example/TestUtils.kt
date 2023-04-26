package org.example

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class FileTest {

    private val files = mutableListOf<Path>()

    internal fun tmpFile(name: String): Path {
        val file = kotlin.io.path.createTempFile(name)
        files.add(file)
        return file
    }

    @AfterAll
    fun deleteFiles() {
        files.forEach { it.deleteIfExists() }
    }
}