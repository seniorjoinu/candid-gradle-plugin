plugins {
    kotlin("jvm") version "1.3.70"
    id("com.gradle.plugin-publish") version "0.12.0"
    groovy
    `java-gradle-plugin`
    `project-report`
}

group = "com.github.seniorjoinu"
version = "0.1-rc5"

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
    implementation("com.github.seniorjoinu:candid-kt:0.1-rc7")
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0-M1")
    testImplementation("org.spockframework:spock-core:2.0-M3-groovy-2.5")
}

tasks.test {
    useJUnitPlatform()
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

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
