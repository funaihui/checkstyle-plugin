package io.gitlab.arturbosch.detekt.idea

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiFile
import com.intellij.util.io.exists
import io.github.detekt.tooling.api.DetektProvider
import io.github.detekt.tooling.api.UnexpectedError
import io.github.detekt.tooling.api.spec.ProcessingSpec
import io.github.detekt.tooling.api.spec.RulesSpec
import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.api.UnstableApi
import io.gitlab.arturbosch.detekt.idea.config.DetektConfigStorage
import io.gitlab.arturbosch.detekt.idea.util.DirectExecuter
import io.gitlab.arturbosch.detekt.idea.util.extractPaths
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class ConfiguredService(private val project: Project) {

    private val storage = DetektConfigStorage.instance(project)

    fun validate(): List<String> {
        val messages = mutableListOf<String>()

        pluginPaths().filter { Files.notExists(it) }.forEach { messages += "扩展jar文件 <b>$it</b> 不存在" }

        configPaths().filter { Files.notExists(it) }.forEach { messages += "配置文件 <b>$it</b> 不存在" }

        val baseline = baseline()
        if (baseline != null && !baseline.exists()) {
            messages += "The provided baseline file <b>$baseline</b> does not exist."
        }

        return messages
    }

    private fun settings(filename: String, autoCorrect: Boolean) = ProcessingSpec {
        project {
            basePath = project.guessProjectDir()?.canonicalPath?.let { Paths.get(it) }
            inputPaths = listOf(Paths.get(filename))
        }
        rules {
            this.autoCorrect = autoCorrect
            activateAllRules = false
            maxIssuePolicy = RulesSpec.MaxIssuePolicy.AllowAny
        }
        config {
            // Do not throw an error during annotation mode as it is a common scenario
            // that the IntelliJ plugin is behind detekt core version-wise (new unknown config properties).
            shouldValidateBeforeAnalysis = false
            useDefaultConfig = true
            configPaths = configPaths()
        }
        baseline {
            path = baseline()
        }
        extensions {
            fromPaths { pluginPaths() }
            disableExtension(FORMATTING_RULE_SET_ID)
        }
        execution {
            executorService = DirectExecuter()
        }
    }

    private fun configPaths(): List<Path> {
        val config = "/checkstyle/config.yml"
        return extractPaths(createConfigFile(config, "detekt.yml") ?: "", project)
    }

    private fun createConfigFile(copyTo: String, filename: String): String? {
        try {
            val copyToFile = File(project.basePath, copyTo)
            if (copyToFile.exists()) return copyToFile.path
            if (!copyToFile.parentFile.exists()) {
                copyToFile.parentFile.mkdirs()
            }
            val fontStream = this::class.java.classLoader.getResourceAsStream(filename) ?: return null
            val fileOutputStream = FileOutputStream(copyToFile)
            val buffer = ByteArray(1024 * 10)
            var length: Int
            while (fontStream.read(buffer).also { length = it } > 0) {
                fileOutputStream.write(buffer, 0, length)
            }
            fileOutputStream.close()
            fontStream.close()
            return copyToFile.absolutePath
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun pluginPaths(): List<Path> {
        val checkJar = "/checkstyle/checkstyle.jar"
        return extractPaths(createConfigFile(checkJar, "detekt-extension-1.0-SNAPSHOT.jar") ?: "", project)
    }

    private fun baseline(): Path? = null

    fun execute(file: PsiFile, autoCorrect: Boolean): List<Finding> {
        val pathToAnalyze = file.virtualFile?.canonicalPath ?: return emptyList()
        return execute(file.text, pathToAnalyze, autoCorrect)
    }

    @OptIn(UnstableApi::class)
    fun execute(fileContent: String, filename: String, autoCorrect: Boolean): List<Finding> {
        if (filename == SPECIAL_FILENAME_FOR_DEBUGGING) {
            return emptyList()
        }

        val spec: ProcessingSpec = settings(filename, autoCorrect)
        val detekt = DetektProvider.load().get(spec)
        val result = detekt.run(fileContent, filename)

        when (val error = result.error) {
            is UnexpectedError -> throw error.cause
            null -> Unit
            else -> throw error
        }

        return result.container?.findings?.flatMap { it.value } ?: emptyList()
    }
}
