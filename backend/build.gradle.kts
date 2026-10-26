import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.4.21"

    id("org.jetbrains.kotlin.plugin.spring") version "2.4.21"
    id("org.springframework.boot") version "4.1.2"
    id("org.openapi.generator") version "7.26.0"
    application
}

group = "xyz.engsmyre"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.2"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-webclient")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // Flyway migrates over JDBC at startup only; the application itself uses R2DBC
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.postgresql:r2dbc-postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-r2dbc")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.wiremock:wiremock-standalone:3.13.2")
    testImplementation("com.atlassian.oai:swagger-request-validator-core:2.44.9")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.bootJar {
    archiveFileName.set("app.jar")
}

application {
    mainClass.set("xyz.engsmyre.moviedescriptiongame.MovieDescriptionGameApplicationKt")
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
    inputSpec.set("${rootProject.projectDir}/../api/openapi.yaml")
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
            "useCoroutines" to "true",
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
