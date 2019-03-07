package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.codegenerator

import ch.leadrian.samp.kamp.cidl.model.Types
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName

private val KOTLIN_TYPE_MAPPING: Map<String, ClassName> = mapOf(
        Types.BOOL to BOOLEAN,
        Types.CHAR to CHAR,
        Types.FLOAT to FLOAT,
        Types.INT to INT,
        Types.STRING to String::class.asClassName(),
        Types.VOID to UNIT
)

private val KOTLIN_OUT_TYPE_MAPPING: Map<String, ClassName> = mapOf(
        Types.FLOAT to ClassName("ch.leadrian.samp.kamp.core.api.amx", "MutableFloatCell"),
        Types.INT to ClassName("ch.leadrian.samp.kamp.core.api.amx", "MutableIntCell"),
        Types.STRING to ClassName("ch.leadrian.samp.kamp.core.runtime.types", "OutputString")
)

fun getKotlinType(typeName: String): ClassName =
        KOTLIN_TYPE_MAPPING[typeName] ?: throw IllegalArgumentException("Unknown Kotlin type: $typeName")

fun getKotlinOutType(typeName: String): ClassName =
        KOTLIN_OUT_TYPE_MAPPING[typeName] ?: throw IllegalArgumentException("Unknown Kotlin out type: $typeName")