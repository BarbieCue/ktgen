package org.example

import io.kotest.core.spec.style.ExpectSpec
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute
import kotlin.io.path.deleteIfExists

abstract class IOExpectSpec(body: IOExpectSpec.() -> Unit = {}) : ExpectSpec() {
    init {
        body()
        afterEach { files.forEach { it.deleteIfExists() } }
        afterEach { server.forEach { it.stop() } }
    }

    private val files = mutableListOf<Path>()

    fun tmpFile(name: String, vararg attributes: FileAttribute<*>): Path {
        val file = kotlin.io.path.createTempFile(prefix = name, attributes = attributes)
        files.add(file)
        return file
    }

    private val server = mutableListOf<ApplicationEngine>()

    fun startLocalhostWebServer(port: Int, module: Application.() -> Unit) {
        val s = embeddedServer(Netty, port, host = "0.0.0.0", module = module)
        s.start(false)
        server.add(s)
    }
}