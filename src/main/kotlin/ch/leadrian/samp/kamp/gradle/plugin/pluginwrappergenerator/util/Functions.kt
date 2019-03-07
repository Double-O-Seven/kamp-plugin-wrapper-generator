package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util

import ch.leadrian.samp.kamp.cidl.model.Function
import com.google.common.base.CaseFormat
import com.google.common.base.Enums

val Function.isCallback: Boolean
    get() = hasAttribute("callback")

val Function.isNative: Boolean
    get() = hasAttribute("native")

val Function.hasNoImplementation: Boolean
    get() = hasAttribute("noimpl")

fun Function.transformName(caseFormat: Any?, prefixesToRemove: Set<String>): String {
    var transformedName = name
    prefixesToRemove.forEach {
        transformedName = transformedName.removePrefix(it)
    }
    val pluginFunctionsCaseFormat = caseFormat?.let {
        Enums.getIfPresent(CaseFormat::class.java, it.toString()).orNull()
                ?: throw IllegalArgumentException("Unknown case format: $it")
    }
    pluginFunctionsCaseFormat?.to(CaseFormat.LOWER_CAMEL, transformedName)?.let { transformedName = it }
    return transformedName
}
