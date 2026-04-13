import org.gradle.kotlin.dsl.testImplementation

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

    // Spring Security - Asosiy xavfsizlik kutubxonasi
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Agar JWT ishlatmoqchi bo'lsak, bular ham kerak bo'ladi (ixtiyoriy)
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    implementation(project(":Matrix"))
    implementation(project(":core"))

    implementation("com.google.zxing:core:3.5.2") // QR generatsiya
    implementation("com.google.zxing:javase:3.5.2")
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.awaitility:awaitility:4.2.0") // WebSocket kechikishlarini kutish uchun
    // build.gradle.kts (Kotlin bo'lsa)
    testImplementation("com.h2database:h2")

    // yoki build.gradle (Groovy bo'lsa)
    testImplementation("com.h2database:h2")
    // Excel (.xlsx) fayllarni o'qish uchun
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    implementation("ai.timefold.solver:timefold-solver-spring-boot-starter:1.10.0")

    // Agar yuqoridagidan keyin ham topmasa, core'ni ham qo'shib qo'y:
    implementation("ai.timefold.solver:timefold-solver-core:1.10.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}