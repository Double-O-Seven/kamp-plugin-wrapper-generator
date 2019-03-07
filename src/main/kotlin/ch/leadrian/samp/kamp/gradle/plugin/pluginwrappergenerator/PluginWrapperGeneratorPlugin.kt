package ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

open class PluginWrapperGeneratorPlugin : Plugin<Project> {

    companion object {

        const val GENERATED_SOURCE_DIRECTORY = "generated-src/main/java"

    }

    override fun apply(project: Project) {
        applyJavaPlugin(project)
        createExtension(project)
        configureSourceSets(project)
    }

    private fun applyJavaPlugin(project: Project) {
        project.pluginManager.apply(JavaPlugin::class.java)
    }

    private fun createExtension(project: Project) {
        project.extensions.create("pluginWrapperGenerator", PluginWrapperGeneratorExtension::class.java)
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