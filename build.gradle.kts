import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.springframework.boot") version "3.5.15"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.spring") version "2.4.0"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

group = "no.fintlabs"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.fromTarget("25"))
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
    mavenLocal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("no.novari:flyt-web-instance-gateway:2.0.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn("ktlintCheck")
}
