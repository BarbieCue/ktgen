plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("io.ktor.plugin") version "2.2.4"
}

group = "org.example"
version = "0.0.1"

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

tasks.test {
    useJUnitPlatform()
}

dependencies {

    // Scraping
    implementation("it.skrape:skrapeit:+")

    // CLI arguments
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")

    // Xml
    implementation("io.github.pdvrieze.xmlutil:core-jvm:0.85.0")
    implementation("io.github.pdvrieze.xmlutil:serialization-jvm:0.85.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.10")
    testImplementation("io.kotest:kotest-assertions-json-jvm:5.5.5")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.5")
    testImplementation("io.kotest:kotest-property:5.5.5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("io.ktor:ktor-server-core-jvm:2.2.4")
    testImplementation("io.ktor:ktor-server-netty-jvm:2.2.4")
    testImplementation("ch.qos.logback:logback-classic:1.4.6")
    testImplementation("io.mockk:mockk:1.13.4")
}