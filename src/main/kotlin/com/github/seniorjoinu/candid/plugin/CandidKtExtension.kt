package com.github.seniorjoinu.candid.plugin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * The Candid to Kotlin extension.
 */
abstract class CandidKtExtension @Inject constructor(project: Project) : CandidSourceSetContainer {
    val genPackage: Property<String> = project.objects.property(String::class.java)
    override var sourceSets: NamedDomainObjectContainer<CandidSourceSet>
        @Suppress("UNCHECKED_CAST")
        get() = DslObject(this).extensions.getByName("sourceSets") as NamedDomainObjectContainer<CandidSourceSet>
        internal set(value) {
            DslObject(this).extensions.add("sourceSets", value)
        }
}
