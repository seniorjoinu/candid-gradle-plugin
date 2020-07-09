### Candid-kt Gradle plugin

Enables [candid-kt](https://github.com/seniorjoinu/candid-kt) into Gradle pipeline 

#### Install

```groovy
// build.gradle

plugins {
  ...
  id 'com.github.seniorjoinu.candid' version '0.1-rc4'
  ...
}

...

candid {
  didPath = "path to the did file"
  getPath = "path to the generated kotlin file"
  genPackage = "package of the generated kotlin file"
}
```

#### Usage

This command will generate a .kt file in the specified directory with everything you need to interact with your canisters

`gradle generateCandidKt`
