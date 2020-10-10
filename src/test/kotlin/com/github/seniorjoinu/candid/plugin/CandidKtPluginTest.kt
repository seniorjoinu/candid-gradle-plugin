package com.github.seniorjoinu.candid.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

/**
 * System under specification: {@link CandidKtPlugin}; strictly speaking this a functional test even so it was added to the unit test source set.
 * @author tglaeser
 */
class CandidKtPluginTest {

    @Test fun `positive apply plugin`() {
        val project = ProjectBuilder.builder().build()
        project.gradle.startParameter.isOffline = true

        project.pluginManager.apply("com.github.seniorjoinu.candid")
        assert(project.tasks.getByName(CANDID_TASK_NAME) is CandidKtTask)
    }
}
