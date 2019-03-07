package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator

import ch.leadrian.samp.kamp.cidl.model.Function
import ch.leadrian.samp.kamp.cidl.model.InterfaceDefinitionUnit
import ch.leadrian.samp.kamp.cidl.parser.FileInterfaceDefinitionSource
import ch.leadrian.samp.kamp.cidl.parser.InterfaceDefinitionParser
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.codegenerator.CallbackManagerGenerator
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.codegenerator.CallbacksInterfaceGenerator
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.codegenerator.ConstantsGenerator
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.codegenerator.NativeFunctionsGenerator
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.hasNoImplementation
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.isCallback
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.isNative
import org.gradle.api.DefaultTask
import org.gradle.api.internal.file.FileLookup
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Path
import javax.inject.Inject

open class GeneratePluginWrapperTask
@Inject
constructor(private val fileLookup: FileLookup) : DefaultTask() {

    @get:Nested
    internal val extension: PluginWrapperGeneratorExtension by lazy {
        project.extensions.getByType(PluginWrapperGeneratorExtension::class.java)
    }

    private val interfaceDefinitionFiles: List<Path> by lazy {
        extension.interfaceDefinitionFiles.map { fileLookup.fileResolver.resolve(it).toPath() }
    }

    private val interfaceDefinition: InterfaceDefinitionUnit by lazy {
        with(InterfaceDefinitionParser()) {
            val interfaceDefinitionSources = interfaceDefinitionFiles
                    .map { FileInterfaceDefinitionSource(it) }
                    .toTypedArray()
            parse(*interfaceDefinitionSources)
        }
    }

    private val callbacks: List<Function> by lazy {
        interfaceDefinition.functions.filter { it.isCallback && !it.hasNoImplementation }
    }

    private val nativeFunctions: List<Function> by lazy {
        interfaceDefinition.functions.filter { it.isNative && !it.hasNoImplementation }
    }

    private val generatedSourceDirectory: Path by lazy {
        project
                .buildDir
                .toPath()
                .resolve(PluginWrapperGeneratorPlugin.GENERATED_SOURCE_DIRECTORY)
                .resolve(extension.packageName.replace('.', File.separatorChar))
    }

    private val callbackManagerGenerator: CallbackManagerGenerator? by lazy {
        when {
            callbacks.isNotEmpty() -> CallbackManagerGenerator(
                    callbacks,
                    extension,
                    generatedSourceDirectory
            )
            else -> null
        }
    }

    private val callbacksInterfaceGenerator: CallbacksInterfaceGenerator? by lazy {
        when {
            callbacks.isNotEmpty() -> CallbacksInterfaceGenerator(
                    callbacks,
                    extension,
                    generatedSourceDirectory
            )
            else -> null
        }
    }

    private val constantsGenerator: ConstantsGenerator? by lazy {
        when {
            interfaceDefinition.constants.isNotEmpty() -> ConstantsGenerator(
                    interfaceDefinition.constants,
                    extension,
                    generatedSourceDirectory
            )
            else -> null
        }
    }

    private val nativeFunctionsGenerator: NativeFunctionsGenerator? by lazy {
        when {
            nativeFunctions.isNotEmpty() ->
                NativeFunctionsGenerator(
                        nativeFunctions,
                        extension,
                        generatedSourceDirectory
                )
            else -> null
        }
    }

    @OutputFiles
    fun getOutputFiles(): List<Path> {
        val outputFiles = mutableListOf<Path>()
        callbackManagerGenerator?.outputFile?.let { outputFiles.add(it) }
        callbacksInterfaceGenerator?.outputFile?.let { outputFiles.add(it) }
        constantsGenerator?.outputFile?.let { outputFiles.add(it) }
        nativeFunctionsGenerator?.outputFile?.let { outputFiles.add(it) }
        return outputFiles
    }

    @TaskAction
    fun generatePluginWrapper() {
        callbackManagerGenerator?.generate()
        callbacksInterfaceGenerator?.generate()
        constantsGenerator?.generate()
        nativeFunctionsGenerator?.generate()
    }

}