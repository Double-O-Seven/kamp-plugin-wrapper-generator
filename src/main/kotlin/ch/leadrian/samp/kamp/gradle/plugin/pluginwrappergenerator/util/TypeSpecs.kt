package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.util

import ch.leadrian.samp.kamp.annotations.Generated
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.TypeSpec
import java.time.LocalDateTime
import kotlin.reflect.KClass

fun TypeSpec.Builder.addGeneratedAnnotation(generatedBy: KClass<*>): TypeSpec.Builder {
    return addAnnotation(
            AnnotationSpec
                    .builder(Generated::class.java)
                    .addMember("value = [%S]", generatedBy.java.name)
                    .addMember("date = %S", LocalDateTime.now().toString())
                    .build()
    )
}