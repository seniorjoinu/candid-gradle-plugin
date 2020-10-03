package com.github.seniorjoinu.candid.plugin

import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Plugin
import org.gradle.api.Project

const val CANDIDKT_EXTENSION_NAME = "candid"
const val CANDIDKT_GROUP_NAME = "candid"
const val CANDIDKT_TASK_NAME = "generateCandidKt"
const val CANDIDKT_TASK_DESTINATION_PREFIX = "generated/sources/candid/kotlin"

/**
 * The Candid to Kotlin plugin.
 */
abstract class CandidKtPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Add the extension object
        project.extensions.create(CANDIDKT_EXTENSION_NAME, CandidKtExtension::class.java, project)
        project.candidExtension.apply {
            fun candidSourceSetContainer(factory: NamedDomainObjectFactory<CandidSourceSet>) = project.container(CandidSourceSet::class.java, factory)

            sourceSets = candidSourceSetContainer(candidSourceSetFactory(project))
        }

        val mainSourceSet = configureSourceSet(project, CandidSourceSet.SOURCE_SET_NAME_MAIN)
        val testSourceSet = configureSourceSet(project, CandidSourceSet.SOURCE_SET_NAME_TEST)

        project.afterEvaluate {
            project.candidExtension.sourceSets.filter { it != mainSourceSet && it != testSourceSet }.forEach {
                configureSourceSet(project, it.name)
            }
        }
    }

    internal open fun candidSourceSetFactory(project: Project): NamedDomainObjectFactory<CandidSourceSet> =
            DefaultCandidSourceSetFactory(project)

    private fun configureSourceSet(project: Project, sourceSetName: String): CandidSourceSet = with(project.candidExtension) {
        // Add a source set that uses configuration from the extension object
        val sourceSet = sourceSets.maybeCreate(sourceSetName)

        // Add a task that uses configuration from the extension object
        val taskName = if (sourceSetName == CandidSourceSet.SOURCE_SET_NAME_MAIN) CANDIDKT_TASK_NAME else CANDIDKT_TASK_NAME.replace("generate", "generate${sourceSetName.capitalize()}")
        project.tasks.register(taskName, CandidKtTask::class.java) { candidKtTask ->
            candidKtTask.description = "Generates Kotlin sources from Candid language files resolved from the '$sourceSetName' Candid source set."
            candidKtTask.group = CANDIDKT_GROUP_NAME
            candidKtTask.sourceSetName = sourceSetName
            candidKtTask.genPackage.set(genPackage)
            candidKtTask.sourceSet.takeIf { it != sourceSet }?.dependsOn(sourceSet)
            candidKtTask.source(sourceSet.candid)
            candidKtTask.didFiles += sourceSet.candid
        }
        return sourceSet
    }
}
