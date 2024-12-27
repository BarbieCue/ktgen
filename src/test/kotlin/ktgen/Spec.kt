package ktgen

import io.kotest.common.ExperimentalKotest
import io.kotest.core.config.ProjectConfiguration
import io.kotest.core.spec.style.ExpectSpec
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.net.ServerSocket
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

abstract class IOExpectSpec(body: IOExpectSpec.() -> Unit = {}) : ExpectSpec() {

    init {
        afterEach { files.forEach { it.deleteIfExists() } }
        afterEach { if (::server.isInitialized) server.stop() }
        body()
    }

    private val files = mutableListOf<Path>()

    fun tmpFile(name: String, suffix: String? = null, vararg attributes: FileAttribute<*>): Path {
        val file = kotlin.io.path.createTempFile(prefix = name, attributes = attributes, suffix = suffix)
        files.add(file)
        return file
    }

    private lateinit var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

    fun startLocalHttpServer(module: Application.() -> Unit): String {
        val freePort = findFreePort()
        server = embeddedServer(Netty, configure = {
          connector {
              host = "0.0.0.0"
              port = freePort
          }
        }, module = module)
        server.start(false)

        return "http://0.0.0.0:$freePort"
    }

    private fun findFreePort() = ServerSocket(0).use { it.localPort }
}