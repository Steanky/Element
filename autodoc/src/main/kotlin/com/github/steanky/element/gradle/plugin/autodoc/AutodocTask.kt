package com.github.steanky.element.gradle.plugin.autodoc

import com.github.steanky.element.core.ElementFactory
import com.github.steanky.element.core.annotation.DataObject
import com.github.steanky.element.core.annotation.FactoryMethod
import com.github.steanky.element.core.annotation.document.Description
import com.github.steanky.element.core.annotation.document.Name
import com.github.steanky.element.core.annotation.document.Type
import com.github.steanky.element.core.key.Constants
import com.sun.source.util.JavacTask
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceTask

import org.gradle.api.tasks.TaskAction
import java.util.regex.Pattern
import javax.lang.model.element.*
import javax.tools.JavaFileObject
import javax.tools.ToolProvider

abstract class AutodocTask : SourceTask() {
    companion object {
        val PATTERN: Pattern = Pattern.compile(Constants.KEY_PATTERN)
    }

    var targetConfiguration: Configuration = project.configurations.getAt("runtimeClasspath")
        @Internal get

    var ext: AutodocPlugin.Extension = project.extensions.getByType(AutodocPlugin.Extension::class.java)
        @Input get

    @TaskAction
    fun generateAutodoc() {
        val settings = ext.resolve()

        val model = Model(processClasses(settings).sortedBy { it.type }, settings)

        val folder = project.buildDir.resolve("elementAutodoc")
        folder.mkdir()

        val json = Json.encodeToJsonElement(model)

        folder.resolve("model.json").writeText(json.toString())
    }

    private fun processClasses(settings: Settings) : List<Element> {
        val files = source.files
        val compiler = ToolProvider.getSystemJavaCompiler()
        println(compiler.javaClass)
        val logger = project.logger

        val processTime = if (settings.recordTime) { System.currentTimeMillis() } else 0

        compiler.getStandardFileManager(null, null, null).use {
            val sources = it.getJavaFileObjectsFromFiles(files).filter { it.kind == JavaFileObject.Kind.SOURCE }
            it.handleOption("-cp", listOf(targetConfiguration.files.map { "$it" }.joinToString(":"))
                    .listIterator())

            val javacTask = compiler.getTask(null, it, null, null, null, sources) as JavacTask

            var nullableModelAnnotation: com.github.steanky.element.core.annotation.Model? = null
            return javacTask.analyze().filter { it.kind == ElementKind.CLASS }.filter {
                nullableModelAnnotation = it.getAnnotation(com.github.steanky.element.core.annotation.Model::class.java)
                nullableModelAnnotation != null
            }.map {
                it as TypeElement

                val modelAnnotation = nullableModelAnnotation as com.github.steanky.element.core.annotation.Model
                val value = modelAnnotation.value
                if (!PATTERN.matcher(value).matches()) {
                    logger.error("Model type ${it.simpleName} has invalid type name")
                }

                val name = it.getAnnotation(Name::class.java)?.value ?: it.simpleName.toString()
                val description = it.getAnnotation(Description::class.java)?.value ?: ""

                val parameters = extractParameters(it, extractDataElement(it, logger), logger)

                Element(value, name, description, parameters, processTime)
            }
        }
    }

    private fun extractDataElement(typeElement: TypeElement, logger: Logger): TypeElement? {
        val elements = typeElement.enclosedElements
        val factoryMethods = elements.filter { it.getAnnotation(FactoryMethod::class.java) != null }
        if (factoryMethods.size > 1) {
            logger.error("More than one factory method found on ${typeElement.simpleName}")
            return null
        }

        val factoryMethod = factoryMethods.firstOrNull()
        if (factoryMethod == null) {
            logger.error("Could not find valid factory method or constructor on ${typeElement.simpleName}")
            return null
        }

        when (factoryMethod.kind) {
            ElementKind.CONSTRUCTOR -> {
                val executable = factoryMethod as ExecutableElement
                val dataParameters = executable.parameters.filter {
                    (it.getAnnotation(DataObject::class.java)
                            ?: it.asType().getAnnotation(DataObject::class.java)) != null
                }

                if (dataParameters.size > 1) {
                    logger.error("More than one data parameter found in ${typeElement.simpleName}")
                    return null
                }

                return dataParameters.firstOrNull()?.asType() as TypeElement?
            }
            ElementKind.METHOD -> {
                val executable = factoryMethod as ExecutableElement
                if (!executable.modifiers.contains(Modifier.STATIC)) {
                    logger.error("FactoryMethod in ${typeElement.simpleName} is not static")
                    return null
                }

                val type = executable.returnType as TypeElement
                val qualifiedName = type.qualifiedName.toString()
                if (qualifiedName != ElementFactory::class.java.canonicalName) {
                    logger.error("FactoryMethod in ${typeElement.simpleName} does not return an ElementFactory")
                    return null
                }

                val typeParameters = type.typeParameters
                if (typeParameters.size != 2) {
                    logger.error("FactoryMethod return type in ${typeElement.simpleName} does not have the right " +
                            "number of parameters")
                    return null
                }

                return typeParameters[0].asType() as TypeElement
            }
            else -> {
                logger.error("FactoryMethod in ${typeElement.simpleName} declared on ${factoryMethod.kind}")
                return null
            }
        }
    }

    private fun extractParameters(element: TypeElement, dataElement: TypeElement?, logger: Logger): List<Parameter> {
        if (dataElement == null) {
            //data-less elements have no parameters
            return listOf()
        }

        val params = element.getAnnotationsByType(com.github.steanky.element.core.annotation.document.Parameter::class.java)
        if (params.isNotEmpty()) {
            return params.map {
                Parameter(it.type, it.name, it.behavior)
            }
        }

        if (dataElement.kind == ElementKind.RECORD) {
            return dataElement.recordComponents.map {
                val typeElement = it.asType() as TypeElement

                val type = extractType(typeElement)
                val name = it.simpleName.toString()
                val behavior = it.getAnnotation(Description::class.java)?.value ?: ""

                Parameter(type, name, behavior)
            }
        }

        logger.error("Could not infer parameters for ${dataElement.simpleName}, as it is neither a record nor does " +
                "its element class provide any Parameter annotations")
        return listOf()
    }

    private fun extractType(componentType: TypeElement): String {
        val typeAnnotation = componentType.getAnnotation(Type::class.java)
        if (typeAnnotation != null) {
            return typeAnnotation.value
        }

        when (componentType.qualifiedName.toString()) {
            "java.lang.String" -> {
                return "string"
            }
            "boolean", "java.lang.Boolean" -> {
                return "boolean"
            }
            "byte", "short", "int", "long", "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long" -> {
                return "number"
            }
            "float", "double", "java.lang.Float", "java.lang.Double" -> {
                return "decimal"
            }
            else -> {
                val components = componentType.qualifiedName.split(".")
                if (components.isEmpty()) {
                    return ""
                }

                return components.last().lowercase()
            }
        }
    }
}