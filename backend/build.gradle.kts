import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.0"

    id("org.jetbrains.kotlin.plugin.spring") version "2.3.0"
    id("org.springframework.boot") version "4.0.1"
    application
}

group = "xyz.engsmyre"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:4.0.1")
    implementation("org.springframework.boot:spring-boot-starter-webflux:4.0.1")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:4.0.1")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.0")
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:2.3.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test:4.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}

application {
    mainClass.set("xyz/engsmyre/moviedescriptiongame/MovieDescriptionGameApplication")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}