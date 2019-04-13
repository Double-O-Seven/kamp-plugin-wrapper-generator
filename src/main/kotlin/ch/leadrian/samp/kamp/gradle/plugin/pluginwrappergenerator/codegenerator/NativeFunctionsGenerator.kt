package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.codegenerator

import ch.leadrian.samp.kamp.cidl.model.Function
import ch.leadrian.samp.kamp.cidl.model.Types
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.PluginWrapperGeneratorExtension
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.addGeneratedAnnotation
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.kotlinType
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.transformName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.Writer
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton

internal class NativeFunctionsGenerator(
        private val nativeFunctions: List<Function>,
        private val extension: PluginWrapperGeneratorExtension,
        outputDirectory: Path
) : CodeGenerator(outputDirectory) {

    private val nativeFunctionsClassName = "${extension.pluginName}NativeFunctions"

    private val amxNativeFunctionPropertySpecs by lazy {
        nativeFunctions.map { nativeFunction ->
            val baseType = ClassName(
                    "ch.leadrian.samp.kamp.core.api.amx",
                    "AmxNativeFunction${nativeFunction.parameters.size}"
            )
            val parameterizedType = when {
                nativeFunction.parameters.isNotEmpty() -> baseType.parameterizedBy(*nativeFunction.parameters.map { it.kotlinType }.toTypedArray())
                else -> baseType
            }
            PropertySpec
                    .builder(nativeFunction.name, parameterizedType)
                    .addModifiers(KModifier.PRIVATE)
                    .delegate("%T()", parameterizedType)
                    .build()
        }
    }

    override val fileName: String = "$nativeFunctionsClassName.kt"

    override fun generate(writer: Writer) {
        FileSpec
                .builder(extension.packageName, nativeFunctionsClassName)
                .addNativeFunctionsClass()
                .build()
                .writeTo(writer)
    }

    private fun FileSpec.Builder.addNativeFunctionsClass(): FileSpec.Builder {
        val nativeFunctionsTypeSpec = TypeSpec
                .classBuilder(nativeFunctionsClassName)
                .addGeneratedAnnotation(NativeFunctionsGenerator::class)
                .addAnnotation(Singleton::class.java)
                .primaryConstructor(
                        FunSpec
                                .constructorBuilder()
                                .addAnnotation(Inject::class.java)
                                .addModifiers(KModifier.INTERNAL)
                                .build()
                )
                .addProperties(amxNativeFunctionPropertySpecs)
                .addNativeFunctions()
                .build()
        return addType(nativeFunctionsTypeSpec)
    }

    private fun TypeSpec.Builder.addNativeFunctions(): TypeSpec.Builder {
        amxNativeFunctionPropertySpecs.mapIndexed { index, propertySpec ->
            addFunction(buildNativeFunctionSpec(propertySpec, nativeFunctions[index]))
        }
        return this
    }

    private fun buildNativeFunctionSpec(propertySpec: PropertySpec, nativeFunction: Function): FunSpec {
        val parameterSpecs = nativeFunction.parameters.map { ParameterSpec.builder(it.name, it.kotlinType).build() }
        val nativeFunctionSpec = FunSpec
                .builder(nativeFunction.transformName(extension.nativeFunctionsCaseFormat, extension.prefixesToRemove))
                .returns(
                        getKotlinType(
                                nativeFunction.type
                        )
                )
                .addParameters(parameterSpecs)
        var format = "%N(" + parameterSpecs.joinToString(", ") { "%N" } + ")"
        val args = mutableListOf<Any>(propertySpec)
        parameterSpecs.forEach { args.add(it) }
        format = when (nativeFunction.type) {
            Types.VOID -> "$format; return 0"
            Types.INT -> "return $format"
            Types.BOOL -> "return $format!=0"
            Types.FLOAT -> "return Float.fromBits($format)"
            else -> throw IllegalArgumentException("Unsupported return type: ${nativeFunction.type}")
        }
        nativeFunctionSpec.addStatement(format, *args.toTypedArray())
        return nativeFunctionSpec.build()
    }

}
