plugins {
    //kotlin("jvm") version "2.2.20"

    // Spring Boot pluginlari (Bularsiz kutubxonalar topilmaydi)
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"

    // Kotlin pluginlari
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
}

group = "com.uniface"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starter
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // AWS SDK
    implementation("software.amazon.awssdk:rekognition:2.20.0")
    implementation("software.amazon.awssdk:auth:2.20.0")
    implementation("software.amazon.awssdk:apache-client:2.20.0")

    // Database (PostgreSQL) - FAQAT SHU QOLSIN
    implementation("org.postgresql:postgresql")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}