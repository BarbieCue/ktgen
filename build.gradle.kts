import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"

    id("org.jetbrains.kotlinx.kover") version "0.9.1"
    id("io.ktor.plugin") version "3.2.0"
}

application {
    mainClass.set("ktgen.ApplicationKt")
}

ktor {
    fatJar {
        archiveFileName.set("ktgen.jar")
    }
}

repositories {
    mavenCentral()
}

tasks.check {
    dependsOn(tasks.koverXmlReport)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events = mutableSetOf(TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
    }
}

dependencies {

    // cli
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")

    // xml
    implementation("io.github.pdvrieze.xmlutil:core:0.91.1")
    implementation("io.github.pdvrieze.xmlutil:serialization-jvm:0.91.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1")

    // web
    implementation("it.skrape:skrapeit:1.3.0-alpha.2")
    implementation("io.ktor:ktor-client-core:3.2.0")
    implementation("io.ktor:ktor-client-cio:3.2.0")

    // kotest
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-json-jvm:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.9.1")
    testImplementation("io.kotest:kotest-property:5.9.1")

    // ktor for testing
    testImplementation("io.ktor:ktor-server-core-jvm:3.2.0")
    testImplementation("io.ktor:ktor-server-netty-jvm:3.2.0")
    testImplementation("io.ktor:ktor-network:3.2.0")

    testImplementation("ch.qos.logback:logback-classic:1.5.18")
}