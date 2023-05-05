package org.example

import io.kotest.common.ExperimentalKotest
import io.kotest.core.config.ProjectConfiguration
import io.kotest.core.spec.style.ExpectSpec
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute
import kotlin.io.path.deleteIfExists

@OptIn(ExperimentalKotest::class)
abstract class ConcurrentExpectSpec(body: ConcurrentExpectSpec.() -> Unit = {}) : ExpectSpec() {
    init {
        concurrency = ProjectConfiguration.MaxConcurrency
        body()
    }
}

abstract class IOExpectSpec(body: IOExpectSpec.() -> Unit = {}) : ConcurrentExpectSpec() {

    init {
        afterEach { files.forEach { it.deleteIfExists() } }
        afterEach { if (::server.isInitialized) server.stop() }
        body()
    }

    private val files = mutableListOf<Path>()

    fun tmpFile(name: String, vararg attributes: FileAttribute<*>): Path {
        val file = kotlin.io.path.createTempFile(prefix = name, attributes = attributes)
        files.add(file)
        return file
    }

    private val mutex = Mutex()
    private lateinit var server: ApplicationEngine

    suspend fun startLocalhostWebServer(port: Int, module: Application.() -> Unit) {
        mutex.withLock {
            server = embeddedServer(Netty, port, host = "0.0.0.0", module = module)
            server.start(false)
        }
    }
}