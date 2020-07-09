package com.github.seniorjoinu.candid.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import senior.joinu.candid.CandidCodeGenerator
import java.lang.RuntimeException
import java.nio.file.Paths

open class CandidKtPluginExtension {
    var didPath: String? = null
    var genPath: String? = null
    var genPackage: String? = null
}

class CandidKtPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("candid", CandidKtPluginExtension::class.java)
        project.task("generateCandidKt") {
            it.doLast {
                CandidCodeGenerator.generateFor(
                    Paths.get(extension.didPath ?: throw RuntimeException("No didPath was provided")),
                    Paths.get(extension.genPath ?: throw RuntimeException("No genPath was provided")),
                    extension.genPackage ?: throw RuntimeException("No genPackage was provided")
                )
            }
        }
    }
}
