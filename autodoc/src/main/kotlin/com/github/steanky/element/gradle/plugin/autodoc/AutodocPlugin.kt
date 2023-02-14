package com.github.steanky.element.gradle.plugin.autodoc

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

fun AutodocPlugin.Extension.resolve(): Settings {
    val projectDescription = this.projectDescription.get()
    val projectUrl = this.projectUrl.get()
    val founded = this.founded.get()
    val maintainers = this.maintainers.get()
    val recordTime = this.recordTime.get()

    return Settings(projectDescription, projectUrl, founded, maintainers, recordTime)
}

@Serializable
data class Model(val elements: List<Element>, val settings: Settings)

@Serializable
data class Element(val type: String,
                   val name: String,
                   val description: String,
                   val parameters: List<Parameter>,
                   val lastUpdated: Long)

@Serializable
data class Parameter (val type: String,
                      val name: String,
                      val behavior: String)
@Serializable
data class Settings(val projectDescription: String,
                    val projectUrl: String,
                    val founded: Long,
                    val maintainers: List<String>,
                    @Transient val recordTime: Boolean = true)

class AutodocPlugin : Plugin<Project> {
    interface Extension {
        val projectDescription: Property<String>
        val projectUrl: Property<String>
        val founded: Property<Long>
        val maintainers: ListProperty<String>
        val recordTime: Property<Boolean>
    }

    override fun apply(project: Project) {
        val ext = project.extensions.create("elementAutodoc", Extension::class.java)
        project.tasks.create("elementAutodoc", AutodocTask::class.java).dependsOn("build")

        ext.projectDescription.convention("")
        ext.projectUrl.convention("")
        ext.founded.convention(System.currentTimeMillis())
        ext.maintainers.convention(listOf())
        ext.recordTime.convention(true)
    }
}