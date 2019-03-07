package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.codegenerator

import ch.leadrian.samp.kamp.cidl.model.Function
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.PluginWrapperGeneratorExtension
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.addGeneratedAnnotation
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.kotlinType
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.transformName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.Writer
import java.nio.file.Path

internal class CallbacksInterfaceGenerator(
        private val callbacks: List<Function>,
        private val extension: PluginWrapperGeneratorExtension,
        outputDirectory: Path
) : CodeGenerator(outputDirectory) {

    private val callbacksInterfaceClassName = "${extension.pluginName}Callbacks"

    override val fileName: String = "$callbacksInterfaceClassName.kt"

    override fun generate(writer: Writer) {
        FileSpec
                .builder(extension.packageName, callbacksInterfaceClassName)
                .addCallbacksInterfaceClass()
                .build()
                .writeTo(writer)
    }

    private fun FileSpec.Builder.addCallbacksInterfaceClass(): FileSpec.Builder {
        val callsbacksInterfaceTypeSpec = TypeSpec
                .interfaceBuilder(callbacksInterfaceClassName)
                .addGeneratedAnnotation(CallbacksInterfaceGenerator::class)
                .addCallbackFunctions()
                .build()
        return addType(callsbacksInterfaceTypeSpec)
    }

    private fun TypeSpec.Builder.addCallbackFunctions(): TypeSpec.Builder {
        callbacks.forEach { addFunction(buildCallbackFunction(it)) }
        return this
    }

    private fun buildCallbackFunction(function: Function): FunSpec {
        val parameterSpecs = function.parameters.map {
            ParameterSpec.builder(it.name, it.kotlinType).build()
        }
        return FunSpec
                .builder(function.transformName(extension.callbacksCaseFormat, extension.prefixesToRemove))
                .addModifiers(KModifier.ABSTRACT)
                .returns(getKotlinType(function.type))
                .addParameters(parameterSpecs)
                .build()
    }

}
