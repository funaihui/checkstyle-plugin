import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

project.group = "com.lollitech.checkstyle"
project.version = "1.0.0"

repositories {
    mavenCentral()
}

plugins {
    id("org.jetbrains.intellij").version("1.6.0")
    id("com.github.ben-manes.versions").version("0.42.0")
    kotlin("jvm").version("1.6.20")
    id("com.github.breadmoirai.github-release").version("2.2.12")
}

dependencies {
    api(libs.detekt.api)
    api(libs.detekt.cli)
    api(libs.detekt.tooling)
    api(libs.kotlin.compilerEmbeddable)

    api(libs.detekt.core)
    api(libs.detekt.rules)
    api(libs.detekt.formatting)

    testImplementation(libs.detekt.testUtils)
    testImplementation(libs.assertj.core)
    testImplementation(libs.junit.jupiter)
}

val jvmVersion = JavaVersion.VERSION_11
val currentJavaVersion = JavaVersion.current()
check(currentJavaVersion.isCompatibleWith(jvmVersion)) {
    "the current JVM ($currentJavaVersion) is incompatible with $jvmVersion"
}

java {
    sourceCompatibility = jvmVersion
    targetCompatibility = jvmVersion
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = jvmVersion.toString()
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showExceptions = true
        showCauses = true
        exceptionFormat = FULL
    }
}


intellij {
    pluginName.set("CheckStyle Plugin")
    version.set("212.5712.43")
//    localPath.set("/Applications/Android Studio.app")
    updateSinceUntilBuild.set(false)
//    version.set("2022.1")
    plugins.set(listOf("IntelliLang", "Kotlin","android"))
}

