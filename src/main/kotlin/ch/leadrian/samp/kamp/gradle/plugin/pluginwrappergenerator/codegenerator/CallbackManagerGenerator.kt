package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.codegenerator

import ch.leadrian.samp.kamp.cidl.model.Function
import ch.leadrian.samp.kamp.cidl.model.Types
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.PluginWrapperGeneratorExtension
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.addGeneratedAnnotation
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.transformName
import com.google.common.base.CaseFormat.LOWER_CAMEL
import com.google.common.base.CaseFormat.UPPER_CAMEL
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.Writer
import java.nio.file.Path
import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.inject.Singleton

internal class CallbackManagerGenerator(
        private val callbacks: List<Function>,
        private val extension: PluginWrapperGeneratorExtension,
        outputDirectory: Path
) : CodeGenerator(outputDirectory) {

    private val callbackManagerClassName = "${extension.pluginName}CallbackManager"

    private val callbacksInterfaceClass = ClassName(extension.packageName, "${extension.pluginName}Callbacks")

    private val amxCallbackFactoryClass = ClassName(
            packageName = "ch.leadrian.samp.kamp.core.api.amx",
            simpleName = "AmxCallbackFactory"
    )

    private val amxCallbackFactoryParameterSpec = ParameterSpec
            .builder("amxCallbackFactory", amxCallbackFactoryClass)
            .build()

    private val amxCallbackFactoryPropertySpec = PropertySpec
            .builder(amxCallbackFactoryParameterSpec.name, amxCallbackFactoryParameterSpec.type)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%N", amxCallbackFactoryParameterSpec)
            .build()

    private val callbacksInterfaceParameterSpec = ParameterSpec
            .builder(UPPER_CAMEL.to(LOWER_CAMEL, callbacksInterfaceClass.simpleName), callbacksInterfaceClass)
            .build()

    private val callbacksInterfacePropertySpec = PropertySpec
            .builder(callbacksInterfaceParameterSpec.name, callbacksInterfaceParameterSpec.type)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%N", callbacksInterfaceParameterSpec)
            .build()

    override val fileName: String = "$callbackManagerClassName.kt"

    override fun generate(writer: Writer) {
        FileSpec
                .builder(extension.packageName, callbackManagerClassName)
                .addCallbackManagerClass()
                .build()
                .writeTo(writer)
    }

    private fun FileSpec.Builder.addCallbackManagerClass(): FileSpec.Builder {
        val callsbacksInterfaceTypeSpec = TypeSpec
                .interfaceBuilder(callbacksInterfaceClass)
                .addGeneratedAnnotation(CallbackManagerGenerator::class)
                .addConstructor()
                .addInitializeFunction()
                .addModifiers(KModifier.INTERNAL)
                .addAnnotation(Singleton::class)
                .build()
        return addType(callsbacksInterfaceTypeSpec)
    }

    private fun TypeSpec.Builder.addConstructor(): TypeSpec.Builder {
        val constructorSpec = FunSpec
                .constructorBuilder()
                .addAnnotation(Inject::class)
                .addParameter(amxCallbackFactoryParameterSpec)
                .addParameter(callbacksInterfaceParameterSpec)
                .build()
        primaryConstructor(constructorSpec)
        addProperty(amxCallbackFactoryPropertySpec)
        addProperty(callbacksInterfacePropertySpec)
        return this
    }

    private fun TypeSpec.Builder.addInitializeFunction(): TypeSpec.Builder {
        val initializeSpec = FunSpec
                .builder("initialize")
                .addAnnotation(PostConstruct::class)
        callbacks.forEach { callback ->
            val lambdaParameters = callback.parameters.joinToString(", ") { it.name }
            val callbackName = callback.transformName(extension.callbacksCaseFormat, extension.prefixesToRemove)
            var statement = when {
                callback.parameters.isNotEmpty() -> "$lambdaParameters -> %N.$callbackName($lambdaParameters)"
                else -> "%N.$callbackName()"
            }
            if (callback.type == Types.BOOL) {
                statement = "$statement.let { result -> if (result) 1 else 0 }"
            } else if (callback.type != Types.INT) {
                throw IllegalArgumentException("Unsupported callback return type: ${callback.type}")
            }
            initializeSpec
                    .beginControlFlow(
                            "%N.create${callback.parameters.size}(%S)",
                            amxCallbackFactoryClass,
                            callback.name
                    )
                    .addStatement(statement, callbacksInterfacePropertySpec)
                    .endControlFlow()
        }
        return addFunction(initializeSpec.build())
    }

}
