package com.github.seniorjoinu.candid.plugin

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import java.io.File
import java.util.Locale

internal val Project.candidExtension: CandidKtExtension
    get() = extensions.getByName(CANDID_EXTENSION_NAME) as CandidKtExtension

interface CandidSourceSetContainer {
    val sourceSets: NamedDomainObjectContainer<CandidSourceSet>
}

interface CandidSourceSet : Named {
    val candid: SourceDirectorySet
    fun candid(action: Action<Any?>): SourceDirectorySet

    val taskName: String

    fun dependsOn(other: CandidSourceSet)
    val dependsOn: Set<CandidSourceSet>

    companion object {
        const val SOURCE_SET_NAME_MAIN = "main"
    }
}

internal class DefaultCandidSourceSetFactory(
        project: Project
) : CandidSourceSetFactory<DefaultCandidSourceSet>(project) {

    override val itemClass: Class<DefaultCandidSourceSet>
        get() = DefaultCandidSourceSet::class.java

    override fun doCreateSourceSet(name: String): DefaultCandidSourceSet {
        return DefaultCandidSourceSet(project, name)
    }
}

internal abstract class CandidSourceSetFactory<T : CandidSourceSet> internal constructor(
        protected val project: Project
) : NamedDomainObjectFactory<CandidSourceSet> {

    abstract val itemClass: Class<T>

    override fun create(name: String): T {
        val result = doCreateSourceSet(name)
        setUpSourceSetDefaults(result)
        return result
    }

    protected open fun defaultSourceLocation(sourceSetName: String): File = project.file("src/$sourceSetName")

    protected open fun setUpSourceSetDefaults(sourceSet: T) {
        sourceSet.candid.srcDir(File(defaultSourceLocation(sourceSet.name), "candid"))
    }

    protected abstract fun doCreateSourceSet(name: String): T
}

internal class DefaultCandidSourceSet(
        project: Project,
        private val displayName: String
) : CandidSourceSet {

    override val candid: SourceDirectorySet = createDefaultSourceDirectorySet(project, name).apply {
        filter.include("**/*.did")
        destinationDirectory.fileValue(project.buildDir.resolve("$CANDID_DESTINATION_PREFIX_KOTLIN/$name"))
    }


    override fun candid(action: Action<Any?>): SourceDirectorySet = candid.apply { action.execute(this) }

    override fun getName(): String = displayName

    override val taskName = if (name == CandidSourceSet.SOURCE_SET_NAME_MAIN) CANDID_TASK_NAME else CANDID_TASK_NAME.replace("generate", "generate${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}")

    override fun dependsOn(other: CandidSourceSet) {
        dependsOnSourceSetsImpl.add(other)
    }

    private val dependsOnSourceSetsImpl = mutableSetOf<CandidSourceSet>()

    override val dependsOn: Set<CandidSourceSet>
        get() = dependsOnSourceSetsImpl

    override fun toString(): String = "Candid '$name' source set"
}

private fun createDefaultSourceDirectorySet(project: Project, name: String): SourceDirectorySet = project.objects.sourceDirectorySet(name, name)
