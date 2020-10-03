package com.github.seniorjoinu.candid.plugin

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotHaveFileSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import java.io.PrintWriter
import java.nio.file.Paths

/**
 * System under specification: {@link CandidKtPlugin}; strictly speaking this a functional test even so it was added to the unit test source set.
 */
class CandidKtPluginSpec : FreeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val didName = "greet"
    val packageName = "tld.d.etc"

    "given a project dir" - {
        val projectDir = TemporaryFolder()
        projectDir.create()
        "and the plugin is applied" - {
            val buildFile = projectDir.newFile("build.gradle")
            buildFile.writeText("""
                plugins {
                    id 'com.github.seniorjoinu.candid'
                }
            """.trimIndent())
            buildFile.appendText(System.lineSeparator())
            "positive executing 'gradle tasks'" - {
                val taskName = "tasks"
                val arguments = listOf(taskName)
                val result = build(projectDir.root, arguments)
                result.task(":$taskName")?.outcome shouldBe TaskOutcome.SUCCESS
                result.output shouldContain "${CANDIDKT_GROUP_NAME.capitalize()} tasks"
                result.output shouldContain "$CANDIDKT_TASK_NAME - Generates Kotlin sources from Candid language files resolved from the 'main' Candid source set."
                result.output shouldContain "generateTestCandidKt - Generates Kotlin sources from Candid language files resolved from the 'test' Candid source set."
            }
            "positive executing 'gradle help --task generateCandidKt'" - {
                val taskName = "help"
                val arguments = listOf(taskName, "--task", CANDIDKT_TASK_NAME)
                val result = build(projectDir.root, arguments)
                result.task(":$taskName")?.outcome shouldBe TaskOutcome.SUCCESS
                result.output shouldContain "Detailed task information for $CANDIDKT_TASK_NAME"
                result.output shouldContain "Path${System.lineSeparator()}     :$CANDIDKT_TASK_NAME"
                result.output shouldContain "Description${System.lineSeparator()}     Generates Kotlin sources from Candid language files resolved from the 'main' Candid source set."
                result.output shouldContain "Group${System.lineSeparator()}     $CANDIDKT_GROUP_NAME"
            }
            "positive executing 'gradle generateCandidKt' with defaults" - {
                val didFile = createDidFile(projectDir.root, didName, "src", "main", "candid")
                val ktFile = Paths.get(projectDir.root.canonicalPath, "build/$CANDIDKT_TASK_DESTINATION_PREFIX/main", "${didName}.did.kt").toFile()
                val arguments = listOf(CANDIDKT_TASK_NAME, "--info", "--warning-mode", "all")
                val result = build(projectDir.root, arguments)
                result.task(":$CANDIDKT_TASK_NAME")?.outcome shouldBe TaskOutcome.SUCCESS
                ktFile.shouldExist()
                ktFile.shouldNotHaveFileSize(0)
                didFile.delete()
            }
            "positive executing 'gradle generateCandidKt' with reconfigured destination directory" - {
                val didFile = createDidFile(projectDir.root, didName, "src", "main", "candid")
                val ktFile = Paths.get(projectDir.root.canonicalPath, "build/output", packageName.replace('.', '/'), "${didName}.did.kt").toFile()
                buildFile.appendText("""
                    candid {
                        sourceSets.main.candid.destinationDirectory.fileValue(project.file('build/output'))
                        genPackage = "$packageName"
                    }
                """.trimIndent())
                val arguments = listOf(CANDIDKT_TASK_NAME, "--info", "--warning-mode", "all")
                val result = build(projectDir.root, arguments)
                result.task(":$CANDIDKT_TASK_NAME")?.outcome shouldBe TaskOutcome.SUCCESS
                ktFile.shouldExist()
                ktFile.shouldNotHaveFileSize(0)
                didFile.delete()
            }
            "positive executing 'gradle generateCandidKt' with nested did files under different source directories" - {
                val didFile = createDidFile(projectDir.root, didName, "src", "main", "candid", "tld", "d", "etc")
                val otherDidFile = createDidFile(projectDir.root, "other-greet", "src", "other", "candid", "tld", "d", "etc")
                val ktFile = Paths.get(projectDir.root.canonicalPath, "build/$CANDIDKT_TASK_DESTINATION_PREFIX/main", "tld/d/etc", "${didName}.did.kt").toFile()
                val otherKtFile = Paths.get(projectDir.root.canonicalPath, "build/$CANDIDKT_TASK_DESTINATION_PREFIX/main", "tld/d/etc", "other-greet.did.kt").toFile()
                buildFile.appendText("""
                    candid {
                        sourceSets.main.candid.srcDir 'src/other/candid'
                    }
                """.trimIndent())
                val arguments = listOf(CANDIDKT_TASK_NAME, "--info", "--warning-mode", "all")
                val result = build(projectDir.root, arguments)
                result.task(":$CANDIDKT_TASK_NAME")?.outcome shouldBe TaskOutcome.SUCCESS
                ktFile.shouldExist()
                otherKtFile.shouldExist()
                ktFile.shouldNotHaveFileSize(0)
                otherKtFile.shouldNotHaveFileSize(0)
                didFile.delete()
                otherDidFile.delete()
            }
            "positive executing 'gradle generateIntegTestCandidKt'" - {
                val taskName = "generateIntegTestCandidKt"
                val didFile = createDidFile(projectDir.root, didName, "src", "integTest", "candid")
                val ktFile = Paths.get(projectDir.root.canonicalPath, "build/$CANDIDKT_TASK_DESTINATION_PREFIX/integTest", "${didName}.did.kt").toFile()
                buildFile.appendText("""
                    candid {
                        sourceSets {
                            integTest
                        }
                    }
                """.trimIndent())
                val arguments = listOf(taskName, "--info", "--warning-mode", "all")
                val result = build(projectDir.root, arguments)
                result.task(":$taskName")?.outcome shouldBe TaskOutcome.SUCCESS
                ktFile.shouldExist()
                ktFile.shouldNotHaveFileSize(0)
                didFile.delete()
            }
            buildFile.delete()
        }
        projectDir.delete()
    }
})

fun createDidFile(projectDir: File, didName: String, vararg folderNames: String): File {
    val sourceSetDir = File(projectDir, folderNames.joinToString("/"))
    sourceSetDir.mkdirs()
    val didFile = File(sourceSetDir, "${didName}.did")
    didFile.writeText("""
            service : {
              "greet": (text) -> (text);
            }
        """.trimIndent())
    return didFile
}

fun build(projectDir: File, arguments: List<String>): BuildResult {
    return GradleRunner.create().withProjectDir(projectDir).forwardStdOutput(PrintWriter(System.out)).forwardStdError(PrintWriter(System.err)).withPluginClasspath().withArguments(arguments).build()
}
