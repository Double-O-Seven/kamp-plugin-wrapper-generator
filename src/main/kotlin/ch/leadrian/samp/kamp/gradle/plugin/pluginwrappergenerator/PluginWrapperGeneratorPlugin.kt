package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

open class PluginWrapperGeneratorPlugin : Plugin<Project> {

    companion object {

        const val GENERATED_SOURCE_DIRECTORY = "generated-src/main/java"

    }

    override fun apply(project: Project) {
        applyJavaPlugin(project)
        createExtension(project)
        configureTask(project)
        configureSourceSets(project)
    }

    private fun applyJavaPlugin(project: Project) {
        project.pluginManager.apply(JavaPlugin::class.java)
    }

    private fun createExtension(project: Project) {
        project.extensions.create("pluginWrapperGenerator", PluginWrapperGeneratorExtension::class.java)
    }

    private fun configureTask(project: Project) {
        val generatePluginWrapperTask = project.tasks.create(
                "generatePluginWrapper",
                GeneratePluginWrapperTask::class.java
        )
        project.tasks.withType(JavaCompile::class.java) { it.dependsOn(generatePluginWrapperTask) }
        project.tasks.withType(KotlinCompile::class.java) { it.dependsOn(generatePluginWrapperTask) }
    }

    private fun configureSourceSets(project: Project) {
        project
                .convention
                .getPlugin(JavaPluginConvention::class.java)
                .sourceSets
                .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                .java
                .srcDir(project.buildDir.resolve(GENERATED_SOURCE_DIRECTORY))
    }

}