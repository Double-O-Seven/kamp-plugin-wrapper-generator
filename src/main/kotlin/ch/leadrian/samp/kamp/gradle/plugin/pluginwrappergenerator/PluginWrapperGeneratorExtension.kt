package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator

import com.google.common.base.CaseFormat
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional

open class PluginWrapperGeneratorExtension {

    @get:Input
    lateinit var packageName: String

    @get:Input
    lateinit var pluginName: String

    @get:InputFiles
    internal val interfaceDefinitionFiles: MutableList<Any> = mutableListOf()

    fun interfaceDefinitionFiles(vararg values: Any) {
        interfaceDefinitionFiles.addAll(values)
    }

    fun interfaceDefintionFile(value: Any) {
        interfaceDefinitionFiles.add(value)
    }

    @get:Input
    internal val prefixesToRemove: MutableSet<String> = mutableSetOf()

    fun removePrefix(vararg prefixes: String) {
        prefixesToRemove += prefixes
    }

    @get:[Optional Input]
    var nativeFunctionsCaseFormat: Any? = null

    @get:[Optional Input]
    var callbacksCaseFormat: Any? = CaseFormat.UPPER_CAMEL

}