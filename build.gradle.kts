import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"

    application
}

group = "jm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.http4k:http4k-bom:5.4.0.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-undertow")
    implementation("org.http4k:http4k-client-apache")
    implementation("org.http4k:http4k-format-jackson")

    implementation("io.github.microutils:kotlin-logging:3.0.5")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}