package com.github.steanky.element.gradle.plugin.autodoc

import org.gradle.api.DefaultTask

import org.gradle.api.tasks.TaskAction

abstract class AutodocTask : DefaultTask() {
    @TaskAction
    fun generateAutodoc() {
        val ext = project.extensions.getByType(AutodocPlugin.AutodocPluginExtension::class.java)
        println(ext.message.get())
    }
}