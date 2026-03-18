plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

repositories {
    mavenCentral()
}

dependencies {
    // 1. Timefold (BOM ishlatish shart, versiyalarni moslash uchun)
    implementation(platform("ai.timefold.solver:timefold-solver-bom:1.10.0"))
    implementation("ai.timefold.solver:timefold-solver-core")
    // Eslatma: 'timefold-solver-kotlin' kerak emas, 'core' o'zi Kotlin'ni qo'llab-quvvatlaydi.

    // 2. Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")

    // 3. Boshqa kutubxonalar
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.exposed:exposed-core:0.50.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
//    implementation(project(":"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(project(":core"))
}

kotlin {
    jvmToolchain(17)
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false // Bu modulni alohida JAR qilma degani
}

tasks.getByName<Jar>("jar") {
    enabled = true // Shunchaki oddiy kutubxona sifatida yig'gin
}