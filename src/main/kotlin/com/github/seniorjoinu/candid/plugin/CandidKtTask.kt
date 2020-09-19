package com.github.seniorjoinu.candid.plugin

import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import senior.joinu.candid.CandidCodeGenerator
import java.io.File

/**
 * The Candid to Kotlin task.
 */
abstract class CandidKtTask : SourceTask() {

    @get:Internal internal abstract var sourceSetName: String

    @get:Internal internal val sourceSet: CandidSourceSet
        get() = project.candidExtension.sourceSets.getByName(sourceSetName)

    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE) internal var didFiles: FileCollection = project.files()
    @get:Input @get:Optional abstract val genPath: Property<String>
    @get:Input @get:Optional abstract val genPackage: Property<String>

    @get:OutputDirectory val destinationDir by lazy { project.buildDir.resolve("$CANDIDKT_TASK_DESTINATION_PREFIX/$sourceSetName") }

    @TaskAction fun execute() {
        val prettyTag = genPath.orNull?.let { "[$it]" } ?: "[empty]"

        logger.lifecycle("$prettyTag genPackage :: ${genPackage.orNull}")
        logger.lifecycle("$prettyTag  outputDir :: ${destinationDir.absolutePath}")

        destinationDir.mkdirs()

        didFiles.forEach {
            var outputFile = File(destinationDir, it.nameWithoutExtension.capitalize())
            logger.lifecycle("$prettyTag outputFile :: ${outputFile.absolutePath}")
            CandidCodeGenerator.generateFor(it.toPath(), outputFile.toPath(), genPackage.getOrElse(""))
        }
    }
}
