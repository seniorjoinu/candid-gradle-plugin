package com.github.seniorjoinu.candid.plugin

import groovy.lang.Closure
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.util.ConfigureUtil
import java.io.File

internal val Project.candidExtension: CandidKtExtension
    get() = extensions.getByName(CANDIDKT_EXTENSION_NAME) as CandidKtExtension

interface CandidSourceSetContainer {
    val sourceSets: NamedDomainObjectContainer<CandidSourceSet>
}

interface CandidSourceSet : Named {
    val candid: SourceDirectorySet
    fun candid(configureClosure: Closure<Any?>): SourceDirectorySet

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
        destinationDirectory.fileValue(project.buildDir.resolve("$CANDIDKT_TASK_DESTINATION_PREFIX/$name"))
    }


    override fun candid(configureClosure: Closure<Any?>): SourceDirectorySet = candid.apply { ConfigureUtil.configure(configureClosure, this) }

    override fun getName(): String = displayName

    override fun dependsOn(other: CandidSourceSet) {
        dependsOnSourceSetsImpl.add(other)
    }

    private val dependsOnSourceSetsImpl = mutableSetOf<CandidSourceSet>()

    override val dependsOn: Set<CandidSourceSet>
        get() = dependsOnSourceSetsImpl

    override fun toString(): String = "Candid '$name' source set"
}

private fun createDefaultSourceDirectorySet(project: Project, name: String): SourceDirectorySet = project.objects.sourceDirectorySet(name, name)
