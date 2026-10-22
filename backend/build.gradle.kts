import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.0"

    id("org.jetbrains.kotlin.plugin.spring") version "2.3.0"
    id("org.springframework.boot") version "4.0.1"
    id("org.openapi.generator") version "7.14.0"
    application
}

group = "xyz.engsmyre"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux:4.0.1")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:4.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.11.0")
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:2.3.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test:4.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("xyz/engsmyre/moviedescriptiongame/MovieDescriptionGameApplication")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Keep javac in line with the Kotlin target (and the JDK 21 in the Dockerfile) on newer local JDKs
tasks.withType<JavaCompile> {
    options.release.set(21)
}

// Spec-first: controllers' interfaces and the DTOs are generated from api/openapi.yaml.
// Edit the spec, not the generated code.
val generatedApiDir = layout.buildDirectory.dir("generated/openapi")

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/api/openapi.yaml")
    outputDir.set(generatedApiDir.map { it.asFile.path })
    apiPackage.set("xyz.engsmyre.moviedescriptiongame.api.generated")
    modelPackage.set("xyz.engsmyre.moviedescriptiongame.api.model")
    packageName.set("xyz.engsmyre.moviedescriptiongame.api.generated")
    typeMappings.set(mapOf("DateTime" to "Instant"))
    importMappings.set(mapOf("Instant" to "java.time.Instant"))
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "reactive" to "true",
            "useSpringBoot3" to "true",
            "useTags" to "true",
            "skipDefaultInterface" to "true",
            "useBeanValidation" to "false",
            "documentationProvider" to "none",
            "annotationLibrary" to "none",
            "serializationLibrary" to "jackson",
            "enumPropertyNaming" to "UPPERCASE",
            "exceptionHandler" to "false",
            "gradleBuildFile" to "false",
        )
    )
}

sourceSets.main {
    kotlin.srcDir(generatedApiDir.map { it.dir("src/main/kotlin") })
}

tasks.named("compileKotlin") { dependsOn("openApiGenerate") }
