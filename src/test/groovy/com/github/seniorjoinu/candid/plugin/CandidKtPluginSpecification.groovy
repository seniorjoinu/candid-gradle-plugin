package com.github.seniorjoinu.candid.plugin

import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

import static CandidKtPluginKt.CANDIDKT_TASK_NAME
import static com.github.seniorjoinu.candid.plugin.CandidKtPluginKt.CANDIDKT_GROUP_NAME
import static com.github.seniorjoinu.candid.plugin.CandidKtPluginKt.CANDIDKT_TASK_DESCRIPTION
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

/**
 * System under specification: {@link CandidKtPlugin}; strictly speaking this a functional test even so it was added to the unit test source set.
 * @author tglaeser
 */
class CandidKtPluginSpecification extends Specification {
    @Shared TemporaryFolder testProjectDir = new TemporaryFolder()
    @Shared File buildFile
    @Shared File didFile
    @Shared String genFileName
    @Shared String genPackageName

    def cleanupSpec() { testProjectDir.delete() }

    def cleanup() { buildFile.delete() }

    def setupSpec() {
        given: 'a test fixture'
        genFileName = 'greet'
        genPackageName = 'tld.d.etc'
        testProjectDir = new TemporaryFolder()
        testProjectDir.create()
        didFile = testProjectDir.newFile("${genFileName}.did")
        didFile << """
            service : {
              "greet": (text) -> (text);
            }
        """
    }

    def setup() {
        given: 'the plugin is applied'
        buildFile = testProjectDir.newFile('build.gradle')
        given: 'the plugin is applied'
        buildFile << """
            plugins {
                id 'com.github.seniorjoinu.candid'
            }
        """
    }

    def "positive execute 'gradle tasks'"() {
        given: 'a task name'
        def taskName = 'tasks'

        when: 'the build is executed with the task name as start parameter'
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(taskName).withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$taskName").outcome == SUCCESS
        result.output.contains("${CANDIDKT_GROUP_NAME.capitalize()} tasks")
        result.output.contains("$CANDIDKT_TASK_NAME - $CANDIDKT_TASK_DESCRIPTION")
    }

    def "positive execute 'gradle help --task generateCandidKt'"() {
        given: 'a task name'
        def taskName = 'help'
        buildFile << """
            candid {
                didPath = project.file("$didFile.name")
                genPath = "$genFileName"
                genPackage = "$genPackageName"
            }
        """

        when: 'the build is executed with the task name as start parameter'
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(taskName, '--task', CANDIDKT_TASK_NAME, '-S').withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$taskName").outcome == SUCCESS
        result.output.contains("Detailed task information for $CANDIDKT_TASK_NAME")
        result.output.contains("Path${System.lineSeparator()}     :$CANDIDKT_TASK_NAME")
        result.output.contains("Description${System.lineSeparator()}     $CANDIDKT_TASK_DESCRIPTION")
        result.output.contains("Group${System.lineSeparator()}     $CANDIDKT_GROUP_NAME")
    }

    def "positive execute 'gradle generateCandidKt'"() {
        given: 'the candid extension is configured'
        buildFile << """
            candid {
                didPath = project.file("$didFile.name")
                genPath = "$genFileName"
                genPackage = "$genPackageName"
            }
        """

        when: "the build is executed with the task 'generateCandidKt' as start parameter"
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(CANDIDKT_TASK_NAME).withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$CANDIDKT_TASK_NAME").outcome == SUCCESS
        File ktFile = Paths.get(testProjectDir.root.canonicalPath, 'build', genFileName, genPackageName.replace('.', '/'), "${genFileName}.kt").toFile()

        and: 'the Kotlin file is generated'
        Files.exists(ktFile.toPath())
        Files.size(ktFile.toPath()) != 0
    }

    def "positive execute 'gradle generateCandidKt' with defaults"() {
        given: 'the candid extension is configured'
        buildFile << """
            candid {
                didPath = project.file("$didFile.name")
            }
        """

        when: "the build is executed with the task 'generateCandidKt' as start parameter"
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(CANDIDKT_TASK_NAME).withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$CANDIDKT_TASK_NAME").outcome == SUCCESS
        File ktFile = Paths.get(testProjectDir.root.canonicalPath, 'build', 'output', "output.kt").toFile()

        and: 'the Kotlin file is generated'
        Files.exists(ktFile.toPath())
        Files.size(ktFile.toPath()) != 0
    }
}
