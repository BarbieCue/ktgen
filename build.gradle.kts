plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    id("io.ktor.plugin") version "2.3.0"
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
}

dependencies {

    // Scraping
    implementation("it.skrape:skrapeit:+")

    // CLI arguments
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")

    // Xml
    implementation("io.github.pdvrieze.xmlutil:core-jvm:0.86.0")
    implementation("io.github.pdvrieze.xmlutil:serialization-jvm:0.86.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")

    // kotest
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-json-jvm:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.6.2")
    testImplementation("io.kotest:kotest-property:5.6.2")

    // ktor
    testImplementation("io.ktor:ktor-server-core-jvm:2.3.0")
    testImplementation("io.ktor:ktor-server-netty-jvm:2.3.0")

    testImplementation("ch.qos.logback:logback-classic:1.4.7")
}