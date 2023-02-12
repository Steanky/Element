package com.github.steanky.element.gradle.plugin.autodoc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property

class AutodocPlugin : Plugin<Project> {
    interface AutodocPluginExtension {
        val message: Property<String>
    }

    override fun apply(project: Project) {
        project.extensions.create("elementAutodoc", AutodocPluginExtension::class.java)
        project.tasks.create("elementAutodoc", AutodocTask::class.java)
    }
}