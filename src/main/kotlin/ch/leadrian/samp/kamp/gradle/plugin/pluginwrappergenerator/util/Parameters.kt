package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util

import ch.leadrian.samp.kamp.cidl.model.Parameter
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.codegenerator.getKotlinOutType
import ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.codegenerator.getKotlinType
import com.squareup.kotlinpoet.TypeName

val Parameter.isOutParameter: Boolean
    get() = hasAttribute("out")

val Parameter.kotlinType: TypeName
    get() = if (isOutParameter) {
        getKotlinOutType(type)
    } else {
        getKotlinType(type)
    }