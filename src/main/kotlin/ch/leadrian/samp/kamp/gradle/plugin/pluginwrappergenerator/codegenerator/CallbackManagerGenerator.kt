package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.codegenerator

import ch.leadrian.samp.kamp.cidl.model.Function
import ch.leadrian.samp.kamp.cidl.model.Types
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.PluginWrapperGeneratorExtension
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.addGeneratedAnnotation
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.kotlinType
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
                .classBuilder(callbackManagerClassName)
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
            initializeSpec.addCallbackCreation(callback)
        }
        return addFunction(initializeSpec.build())
    }

    private fun FunSpec.Builder.addCallbackCreation(callback: Function) {
        val format = when {
            callback.parameters.isNotEmpty() -> {
                val parameterization = callback.parameters.joinToString(", ") { "%T" }
                "%N.create${callback.parameters.size}<$parameterization>(%S)"
            }
            else -> "%N.create0(%S)"
        }
        val args = mutableListOf<Any>(amxCallbackFactoryPropertySpec)
        callback.parameters.forEach { args.add(it.kotlinType) }
        args.add(callback.name)
        beginControlFlow(format, *args.toTypedArray())
        addCallbackInvocationStatement(callback)
        endControlFlow()
    }

    private fun FunSpec.Builder.addCallbackInvocationStatement(callback: Function) {
        val lambdaParameters = callback.parameters.joinToString(", ") { it.name }
        val callbackName = callback.transformName(extension.callbacksCaseFormat, extension.prefixesToRemove)
        var statement = when {
            callback.parameters.isNotEmpty() -> "$lambdaParameters -> %N.$callbackName($lambdaParameters)"
            else -> "%N.$callbackName()"
        }
        when {
            callback.type == Types.BOOL -> statement = "$statement.let { result -> if (result) 1 else 0 }"
            callback.type == Types.VOID -> statement = "$statement; 0"
            callback.type != Types.INT -> throw IllegalArgumentException("Unsupported callback return type: ${callback.type}")
        }
        addStatement(statement, callbacksInterfacePropertySpec)
    }

}
