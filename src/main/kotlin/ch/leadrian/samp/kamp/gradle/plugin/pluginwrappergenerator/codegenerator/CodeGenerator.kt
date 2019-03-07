package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.codegenerator

import java.io.Writer
import java.nio.file.Files
import java.nio.file.Path

internal abstract class CodeGenerator(private val outputDirectory: Path) {

    protected abstract val fileName: String

    val outputFile: Path by lazy {
        outputDirectory.resolve(fileName)
    }

    fun generate() {
        Files.createDirectories(outputDirectory)
        Files.newBufferedWriter(outputFile).use(this::generate)
    }

    protected abstract fun generate(writer: Writer)

}