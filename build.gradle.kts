import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.0"
    id("io.ktor.plugin") version "2.3.3"
}

application {
    mainClass.set("org.example.ApplicationKt")
}

ktor {
    fatJar {
        archiveFileName.set("ktgen.jar")
    }
}

repositories {
    mavenCentral()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events = mutableSetOf(TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
    }
}

dependencies {

    // cli
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")

    // xml
    implementation("io.github.pdvrieze.xmlutil:core-jvm:0.86.1")
    implementation("io.github.pdvrieze.xmlutil:serialization-jvm:0.86.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.0")

    // web
    implementation("it.skrape:skrapeit:+")
    implementation("io.ktor:ktor-client-core:2.3.3")
    implementation("io.ktor:ktor-client-cio:2.3.3")

    // kotest
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-json-jvm:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.6.2")
    testImplementation("io.kotest:kotest-property:5.6.2")

    // ktor for testing
    testImplementation("io.ktor:ktor-server-core-jvm:2.3.3")
    testImplementation("io.ktor:ktor-server-netty-jvm:2.3.3")
    testImplementation("io.ktor:ktor-network:2.3.3")

    testImplementation("ch.qos.logback:logback-classic:1.4.11")
}