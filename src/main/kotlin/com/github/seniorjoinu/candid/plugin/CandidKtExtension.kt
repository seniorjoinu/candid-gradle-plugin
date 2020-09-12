package com.github.seniorjoinu.candid.plugin

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * The Candid to Kotlin extension.
 */
abstract class CandidKtExtension @Inject constructor(project: Project) {
    val didPath: RegularFileProperty = project.objects.fileProperty()
    val genPath: Property<String> = project.objects.property(String::class.java)
    val genPackage: Property<String> = project.objects.property(String::class.java)
}
