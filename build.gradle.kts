plugins {
    `project-report`
    `java-gradle-plugin`
    kotlin("jvm") version embeddedKotlinVersion
    id("com.gradle.plugin-publish") version "0.18.0"
}

group = "com.github.seniorjoinu"
version = "0.1-rc25"

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
}

dependencies {
    implementation("com.github.seniorjoinu:candid-kt:0.1-rc25-2")
    implementation("org.jetbrains.kotlin:kotlin-reflect") { version { strictly(embeddedKotlinVersion) } }
    implementation("org.jetbrains.kotlin:kotlin-stdlib") { version { strictly(embeddedKotlinVersion) } }
    implementation("org.jetbrains.kotlin:kotlin-stdlib-common") { version { strictly(embeddedKotlinVersion) } }
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") { version { strictly(embeddedKotlinVersion) } }

    testImplementation("io.kotest:kotest-assertions-core:4.3.2")
    testImplementation("io.kotest:kotest-runner-junit5:4.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
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
