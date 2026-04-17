plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    id("io.ktor.plugin") version "3.4.2"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

group = "br.com.trabalhoarretado"
version = "0.0.1"

application {
    mainClass.set("br.com.trabalhoarretado.ApplicationKt")
}

ktor {
    fatJar {
        archiveFileName.set("trabalho-arretado-api.jar")
    }
}

kotlin {
    jvmToolchain(21)
}

ktlint {
    version.set("1.5.0")
}

repositories {
    mavenCentral()
}

val exposedVersion = "1.2.0"
val koinVersion = "4.2.1"

dependencies {
    // Ktor server — versions managed by io.ktor.plugin BOM
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")

    // Exposed ORM 1.x
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")

    // Database
    implementation("com.zaxxer:HikariCP:7.0.2")
    runtimeOnly("org.postgresql:postgresql:42.7.10")

    // Flyway
    implementation("org.flywaydb:flyway-core:10.13.0")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:10.13.0")

    // Koin
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    // Security
    implementation("org.mindrot:jbcrypt:0.4")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.32")

    // Test
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.12")
}
