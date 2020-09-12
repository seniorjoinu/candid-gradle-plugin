package com.github.seniorjoinu.candid.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

const val CANDIDKT_EXTENSION_NAME = "candid"
const val CANDIDKT_GROUP_NAME = "candid"
const val CANDIDKT_TASK_NAME = "generateCandidKt"
const val CANDIDKT_TASK_DESCRIPTION = "Generates Kotlin source files from Candid language files."

/**
 * The Candid to Kotlin plugin.
 */
abstract class CandidKtPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Add the extension object
        val extension = project.extensions.create(CANDIDKT_EXTENSION_NAME, CandidKtExtension::class.java, project)

        // Add a task that uses configuration from the extension object
        project.tasks.register(CANDIDKT_TASK_NAME, CandidKtTask::class.java) {
            it.didPath.set(extension.didPath)
            it.genPath.set(extension.genPath)
            it.genPackage.set(extension.genPackage)
        }
    }
}
