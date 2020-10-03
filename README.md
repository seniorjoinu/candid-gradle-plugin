[![](https://jitci.com/gh/seniorjoinu/candid-kt-gradle-plugin/svg)](https://jitci.com/gh/seniorjoinu/candid-kt-gradle-plugin)

### Candid-kt Gradle plugin

Enables [candid-kt](https://github.com/seniorjoinu/candid-kt) into Gradle pipeline 

#### Install

```groovy
// build.gradle

buildscript {
    repositories {
        ...
        maven { setUrl("https://dl.bintray.com/hotkeytlt/maven") }
        maven { setUrl("https://jitpack.io") }
        ...
    }
}

plugins {
  ...
  id 'com.github.seniorjoinu.candid' version '0.1-rc24'
  ...
}
...
repositories {
    ...
    maven { setUrl("https://dl.bintray.com/hotkeytlt/maven") }
    maven { setUrl("https://jitpack.io") }
    ...
}
...
candid {
    sourceSets {
        main {
            candid {
                srcDir "path to the did files"
            }
        }
    }
    genPackage = "package of the generated kotlin file"
}
```

#### Usage

This command will generate a .kt file in the specified directory with everything you need to interact with your canisters

`gradle generateCandidKt`
