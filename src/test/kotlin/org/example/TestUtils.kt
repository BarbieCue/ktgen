package org.example

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute
import kotlin.io.path.deleteIfExists

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class FileTest {

    private val files = mutableListOf<Path>()

    internal fun tmpFile(name: String, vararg attributes: FileAttribute<*>): Path {
        val file = kotlin.io.path.createTempFile(prefix = name, attributes = attributes)
        files.add(file)
        return file
    }

    @AfterAll
    fun deleteFiles() {
        files.forEach { it.deleteIfExists() }
    }
}