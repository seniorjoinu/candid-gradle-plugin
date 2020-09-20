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
    @Shared String didName
    @Shared String packageName

    def cleanupSpec() { testProjectDir.delete() }

    def cleanup() { buildFile.delete() }

    def setupSpec() {
        given: 'a test fixture'
        didName = 'greet'
        packageName = 'tld.d.etc'
        testProjectDir = new TemporaryFolder()
        testProjectDir.create()
    }

    def setup() {
        given: 'the plugin is applied'
        buildFile = testProjectDir.newFile('build.gradle')
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
        given: 'a did file'
        File didFile = createDidFile(didName, 'src', 'main', 'candid')

        when: "the build is executed with task 'generateCandidKt' as start parameter"
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(CANDIDKT_TASK_NAME, '--warning-mode', 'all').withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$CANDIDKT_TASK_NAME").outcome == SUCCESS
        File ktFile = Paths.get(testProjectDir.root.canonicalPath, "build/$CANDIDKT_TASK_DESTINATION_PREFIX/main", didName.capitalize(), "${didName}.did.kt").toFile()

        and: 'the Kotlin file is generated'
        Files.exists(ktFile.toPath())
        Files.size(ktFile.toPath()) != 0

        cleanup: 'the did file'
        didFile.delete()
    }

    def "positive execute 'gradle generateCandidKt' with reconfigured destination directory"() {
        given: 'a did file and a configured candid extension'
        File didFile = createDidFile(didName, 'src', 'main', 'candid')
        buildFile << """
            candid {
                sourceSets.main.candid.destinationDirectory.fileValue(project.file('build/output'))
                genPackage = "$packageName"
            }
        """.stripIndent()

        when: "the build is executed with task 'generateCandidKt' as start parameter"
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(CANDIDKT_TASK_NAME).withDebug(true).withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$CANDIDKT_TASK_NAME").outcome == SUCCESS
        File ktFile = Paths.get(testProjectDir.root.canonicalPath, "build/output", didName.capitalize(), packageName.replace('.', '/'), "${didName}.did.kt").toFile()

        and: 'the Kotlin file is generated'
        Files.exists(ktFile.toPath())
        Files.size(ktFile.toPath()) != 0

        cleanup: 'the did file'
        didFile.delete()
    }

    def "positive execute 'gradle generateCandidKt' with nested did files under different source directories"() {
        given: 'two did files under different source directories and a configured candid extension'
        File didFile = createDidFile(didName, 'src', 'main', 'candid', 'tld', 'd', 'etc')
        File otherDidFile = createDidFile('other-greet', 'src', 'other', 'candid', 'tld', 'd', 'etc')
        buildFile << """
            candid {
                sourceSets.main.candid.srcDir 'src/other/candid'
            }
        """.stripIndent()

        when: "the build is executed with task 'generateCandidKt' as start parameter"
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(CANDIDKT_TASK_NAME).withDebug(true).withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$CANDIDKT_TASK_NAME").outcome == SUCCESS
        File ktFile = Paths.get(testProjectDir.root.canonicalPath, "build/$CANDIDKT_TASK_DESTINATION_PREFIX/main", didName.capitalize(), 'tld/d/etc', "${didName}.did.kt").toFile()
        File otherKtFile = Paths.get(testProjectDir.root.canonicalPath, "build/$CANDIDKT_TASK_DESTINATION_PREFIX/main", 'OtherGreet', 'tld/d/etc', "other-greet.did.kt").toFile()

        and: 'the Kotlin files are generated'
        Files.exists(ktFile.toPath())
        Files.size(ktFile.toPath()) != 0
        Files.exists(otherKtFile.toPath())
        Files.size(otherKtFile.toPath()) != 0

        cleanup: 'the did file'
        didFile.delete()
        otherDidFile.delete()
    }

    def "positive execute 'gradle generateIntegTestCandidKt'"() {
        given: 'a did file under a new source set and a configured candid extension'
        def taskName = 'generateIntegTestCandidKt'
        File didFile = createDidFile(didName, 'src', 'integTest', 'candid')
        buildFile << """
            candid {
                sourceSets {
                    integTest
                }
            }
        """.stripIndent()

        when: "the build is executed with task 'generateIntegTestCandidKt' as start parameter"
        def result = GradleRunner.create().withProjectDir(testProjectDir.root).forwardStdOutput(new PrintWriter(System.out)).forwardStdError(new PrintWriter(System.err)).withArguments(taskName).withDebug(true).withPluginClasspath().build()

        then: 'it completes successfully'
        result.task(":$taskName").outcome == SUCCESS
        File ktFile = Paths.get(testProjectDir.root.canonicalPath, "build/$CANDIDKT_TASK_DESTINATION_PREFIX/integTest", didName.capitalize(), "${didName}.did.kt").toFile()

        and: 'the Kotlin file is generated'
        Files.exists(ktFile.toPath())
        Files.size(ktFile.toPath()) != 0

        cleanup: 'the did file'
        didFile.delete()
    }

    private createDidFile(String didName, String... folderNames) {
        File sourceSetDir = new File(testProjectDir.root, folderNames.join('/'))
        sourceSetDir.mkdirs()
        File didFile = new File(sourceSetDir, "${didName}.did")
        didFile << """
            service : {
              "greet": (text) -> (text);
            }
        """.stripIndent()
    }
}
