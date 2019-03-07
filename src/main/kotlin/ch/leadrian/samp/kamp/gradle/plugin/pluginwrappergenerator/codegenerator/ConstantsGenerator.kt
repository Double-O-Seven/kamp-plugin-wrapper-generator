package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.codegenerator

import ch.leadrian.samp.kamp.cidl.model.Constant
import ch.leadrian.samp.kamp.cidl.model.Types
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.PluginWrapperGeneratorExtension
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util.addGeneratedAnnotation
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.Writer
import java.nio.file.Path

internal class ConstantsGenerator(
        private val constants: List<Constant>,
        private val extension: PluginWrapperGeneratorExtension,
        outputDirectory: Path
) : CodeGenerator(outputDirectory) {

    private val constantsObjectName = "${extension.pluginName}Constants"

    override val fileName: String = "$constantsObjectName.kt"

    override fun generate(writer: Writer) {
        FileSpec
                .builder(extension.packageName, constantsObjectName)
                .addConstantsObject()
                .build()
                .writeTo(writer)
    }

    private fun FileSpec.Builder.addConstantsObject(): FileSpec.Builder {
        val constantsObjectTypeSpec = TypeSpec
                .objectBuilder(constantsObjectName)
                .addGeneratedAnnotation(ConstantsGenerator::class)
                .addConstantProperties()
                .build()
        return addType(constantsObjectTypeSpec)
    }

    private fun TypeSpec.Builder.addConstantProperties(): TypeSpec.Builder {
        constants.forEach { constant ->
            val initializer = when (constant.type) {
                Types.FLOAT -> constant.value.data + "f"
                else -> constant.value.data
            }
            PropertySpec
                    .builder(
                            constant.name,
                            getKotlinType(
                                    constant.type
                            )
                    )
                    .addModifiers(KModifier.CONST)
                    .initializer(initializer)
                    .build()
                    .let { addProperty(it) }
        }
        return this
    }

}
