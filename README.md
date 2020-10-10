[![Build Status](https://travis-ci.org/seniorjoinu/candid-kt-gradle-plugin.svg?branch=master)](https://travis-ci.org/seniorjoinu/candid-kt-gradle-plugin)

### Candid Gradle Plugin
Enables [candid-kt](https://github.com/seniorjoinu/candid-kt) into Gradle pipeline 

#### Build Configuration
```kotlin
// build.gradle.kts
buildscript {
    repositories {
        maven { setUrl("https://dl.bintray.com/hotkeytlt/maven") }
        maven { setUrl("https://jitpack.io") }
    }
}

plugins {
  id("com.github.seniorjoinu.candid") version "0.1-rc24"
}

repositories {
    maven { setUrl("https://dl.bintray.com/hotkeytlt/maven") }
    maven { setUrl("https://jitpack.io") }
}

candid {
    sourceSets {
        main {
            candid {
                srcDir("path to the did files")
            }
        }
    }
    genPackage = "package of the generated kotlin file"
}
```

#### Build
```
$ gradle generateCandidKt
```

#### More Information
```
$ gradle tasks
$ gradle help --task generateCandidKt
```
