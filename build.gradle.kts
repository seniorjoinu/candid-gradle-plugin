import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.gradle.plugin-publish") version "0.12.0"
    `java-gradle-plugin`
    kotlin("jvm") version "1.4.10"
}

group = "com.github.seniorjoinu"
version = "0.1-rc9"

gradlePlugin {
    plugins {
        create("candidKt") {
            id = "com.github.seniorjoinu.candid"
            implementationClass = "com.github.seniorjoinu.candid.plugin.CandidKtPlugin"
        }
    }
}

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://dl.bintray.com/hotkeytlt/maven") }
}

dependencies {
    implementation("com.github.seniorjoinu:candid-kt:0.1-rc9")
    implementation(kotlin("stdlib-jdk8"))
}

pluginBundle {
    vcsUrl = "https://github.com/seniorjoinu/candid-kt-gradle-plugin"
    website = "https://github.com/seniorjoinu/candid-kt"
    description = "Gradle plugin that lets you use candid-kt code generator as a Gradle configurable task"
    tags = listOf("dfinity", "candid", "ic", "candid-kt", "android")

    (plugins) {
        "candidKt" {
            displayName = "Candid-kt Gradle plugin"
        }
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
