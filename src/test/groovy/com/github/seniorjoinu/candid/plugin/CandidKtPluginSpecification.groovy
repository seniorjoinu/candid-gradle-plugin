package com.github.seniorjoinu.candid.plugin

import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

import static CandidKtPluginKt.CANDIDKT_TASK_NAME
import static CandidKtPluginKt.CANDIDKT_GROUP_NAME
import static CandidKtPluginKt.CANDIDKT_TASK_DESTINATION_PREFIX
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
        File mainSourceSetDir = testProjectDir.newFolder('src', 'main', 'candid')
        mainSourceSetDir.mkdirs()
        didFile = new File(mainSourceSetDir, "${genFileName}.did")
        didFile << """
            service : {
              "greet": (text) -> (text);
            }
        """.stripIndent()
    }

    def setup() {
        given: 'the plugin is applied'
        buildFile = testProjectDir.newFile('build.gradle')
        given: 'the plugin is applied'
        buildFile << """
            plugins {
                id 'com.github.seniorjoinu.candid'
            }
        """.stripIndent()
    }

    def "positive execute 'gradle tasks'"() {
        given: 'a task name'
        def taskName = 'tasks'

        when: 'the build is executed with the task name as start parameter'
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(taskName).withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$taskName").outcome == SUCCESS
        result.output.contains("${CANDIDKT_GROUP_NAME.capitalize()} tasks")
        result.output.contains("$CANDIDKT_TASK_NAME - Generates Kotlin source files from the 'main' source set Candid language files.")
        result.output.contains("generateTestCandidKt - Generates Kotlin source files from the 'test' source set Candid language files.")
    }

    def "positive execute 'gradle help --task generateCandidKt'"() {
        given: 'a task name'
        def taskName = 'help'

        when: 'the build is executed with the task name as start parameter'
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(taskName, '--task', CANDIDKT_TASK_NAME, '-S').withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$taskName").outcome == SUCCESS
        result.output.contains("Detailed task information for $CANDIDKT_TASK_NAME")
        result.output.contains("Path${System.lineSeparator()}     :$CANDIDKT_TASK_NAME")
        result.output.contains("Description${System.lineSeparator()}     Generates Kotlin source files from the 'main' source set Candid language files.")
        result.output.contains("Group${System.lineSeparator()}     $CANDIDKT_GROUP_NAME")
    }

    def "positive execute 'gradle generateCandidKt' with defaults"() {
        when: "the build is executed with the task 'generateCandidKt' as start parameter"
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(CANDIDKT_TASK_NAME, '--warning-mode', 'all').withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$CANDIDKT_TASK_NAME").outcome == SUCCESS
        File ktFile = Paths.get(testProjectDir.root.canonicalPath, "build/$CANDIDKT_TASK_DESTINATION_PREFIX/main", genFileName.capitalize(), "${genFileName.capitalize()}.kt").toFile()

        and: 'the Kotlin file is generated'
        Files.exists(ktFile.toPath())
        Files.size(ktFile.toPath()) != 0
    }

    def "positive execute 'gradle generateCandidKt'"() {
        given: 'the candid extension is configured'
        buildFile << """
            candid {
                sourceSets {
                    main {
                        candid {
                            destinationDirectory.fileValue(project.file('build/output'))
                        }
                    }
                }
                genPackage = "$genPackageName"
            }
        """.stripIndent()

        when: "the build is executed with the task 'generateCandidKt' as start parameter"
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(CANDIDKT_TASK_NAME).withDebug(true).withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$CANDIDKT_TASK_NAME").outcome == SUCCESS
        File ktFile = Paths.get(testProjectDir.root.canonicalPath, "build/output", genFileName.capitalize(), genPackageName.replace('.', '/'), "${genFileName.capitalize()}.kt").toFile()

        and: 'the Kotlin file is generated'
        Files.exists(ktFile.toPath())
        Files.size(ktFile.toPath()) != 0
    }

    def "positive execute 'gradle generateCandidKt' with defaults but nested did file"() {
        given: 'the candid extension is configured with did file under a new source set'
        File nestedSourceDir = testProjectDir.newFolder('src', 'main', 'candid', 'tld', 'd', 'etc')
        nestedSourceDir.mkdirs()
        didFile.delete()
        didFile = new File(nestedSourceDir, "${genFileName}.did")
        didFile << """
            service : {
              "greet": (text) -> (text);
            }
        """.stripIndent()

        when: "the build is executed with the task 'generateIntegTestCandidKt' as start parameter"
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(CANDIDKT_TASK_NAME).withDebug(true).withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$CANDIDKT_TASK_NAME").outcome == SUCCESS
        File ktFile = Paths.get(testProjectDir.root.canonicalPath, "build/$CANDIDKT_TASK_DESTINATION_PREFIX/main", genFileName.capitalize(), "${genFileName.capitalize()}.kt").toFile()

        and: 'the Kotlin file is generated'
        Files.exists(ktFile.toPath())
        Files.size(ktFile.toPath()) != 0

        cleanup: 'the did file used by this test only'
        didFile.delete()
    }

    def "positive execute 'gradle generateIntegTestCandidKt'"() {
        given: 'the candid extension is configured with did file under a new source set'
        def taskName = 'generateIntegTestCandidKt'
        File integTestSourceSetDir = testProjectDir.newFolder('src', 'integTest', 'candid')
        integTestSourceSetDir.mkdirs()
        didFile = new File(integTestSourceSetDir, "${genFileName}.did")
        didFile << """
            service : {
              "greet": (text) -> (text);
            }
        """.stripIndent()
        buildFile << """
            candid {
                sourceSets {
                    integTest
                }
            }
        """.stripIndent()

        when: "the build is executed with the task 'generateIntegTestCandidKt' as start parameter"
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(taskName).withDebug(true).withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$taskName").outcome == SUCCESS
        File ktFile = Paths.get(testProjectDir.root.canonicalPath, "build/$CANDIDKT_TASK_DESTINATION_PREFIX/integTest", genFileName.capitalize(), "${genFileName.capitalize()}.kt").toFile()

        and: 'the Kotlin file is generated'
        Files.exists(ktFile.toPath())
        Files.size(ktFile.toPath()) != 0

        cleanup: 'the did file used by this test only'
        didFile.delete()
    }
}
