package com.github.seniorjoinu.candid.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import senior.joinu.candid.CandidCodeGenerator

/**
 * The Candid to Kotlin task.
 */
abstract class CandidKtTask : DefaultTask() {

    init {
        description = CANDIDKT_TASK_DESCRIPTION
        group = CANDIDKT_GROUP_NAME
    }

    @get:InputFile abstract val didPath: RegularFileProperty
    @get:Input @get:Optional abstract val genPath: Property<String>
    @get:Input @get:Optional abstract val genPackage: Property<String>

    @get:OutputFile val outputDir by lazy { project.layout.buildDirectory.dir(genPath.orElse("output")) }

    @TaskAction fun execute() {
        val prettyTag = genPath.orNull?.let { "[$it]" } ?: "[empty]"

        logger.lifecycle("$prettyTag genPackage :: ${genPackage.orNull}")
        logger.lifecycle("$prettyTag    didPath :: ${didPath.orNull}")
        logger.lifecycle("$prettyTag  outputDir :: ${outputDir.orNull}")

        outputDir.get().asFile.mkdirs()

        CandidCodeGenerator.generateFor(didPath.get().asFile.toPath(), outputDir.get().asFile.toPath(),genPackage.getOrElse(""))
    }
}
