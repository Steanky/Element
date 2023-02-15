package com.github.steanky.element.gradle.plugin.autodoc

import com.github.steanky.element.core.ElementFactory
import com.github.steanky.element.core.annotation.DataObject
import com.github.steanky.element.core.annotation.FactoryMethod
import com.github.steanky.element.core.annotation.document.Description
import com.github.steanky.element.core.annotation.document.Name
import com.github.steanky.element.core.annotation.document.Type
import com.github.steanky.element.core.key.Constants
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceTask

import org.gradle.api.tasks.TaskAction
import java.lang.RuntimeException
import java.util.regex.Pattern
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.TypeVariable
import javax.lang.model.type.WildcardType
import javax.lang.model.util.Types
import javax.tools.JavaFileObject
import javax.tools.ToolProvider

abstract class AutodocTask : SourceTask() {
    companion object {
        val PATTERN: Pattern = Pattern.compile(Constants.KEY_PATTERN)
    }

    var targetConfiguration: Configuration = project.configurations.getAt("compileClasspath")
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
        val logger = project.logger

        val processTime = if (settings.recordTime) { System.currentTimeMillis() } else 0

        val elementList = mutableListOf<Element>()
        compiler.getStandardFileManager(null, null, null).use {
            val sources = it.getJavaFileObjectsFromFiles(files).filter { file ->
                file.kind == JavaFileObject.Kind.SOURCE
            }

            it.handleOption("-cp", listOf(targetConfiguration.files.map { "$it" }.joinToString(":"))
                    .listIterator())

            val compilerTask = compiler.getTask(null, it, null, listOf("-proc:only"), null,
                    sources)
            compilerTask.setProcessors(listOf(ProcImpl(logger, processTime, elementList)))

            try {
                compilerTask.call()
            }
            catch (e: RuntimeException) {
                logger.error("Compilation error", e)
            }
        }

        return elementList
    }

    @SupportedSourceVersion(SourceVersion.RELEASE_17)
    @SupportedAnnotationTypes("com.github.steanky.element.core.annotation.Model")
    private class ProcImpl(val logger: Logger, val processTime: Long, val elementList: MutableList<Element>):
            AbstractProcessor() {

        lateinit var collectionType: TypeMirror
        lateinit var setType: TypeMirror
        lateinit var mapType: TypeMirror

        lateinit var stringType: TypeMirror
        lateinit var numberType: TypeMirror

        lateinit var objectType: TypeMirror
        lateinit var booleanType: TypeMirror
        lateinit var characterType: TypeMirror
        lateinit var byteType: TypeMirror
        lateinit var shortType: TypeMirror
        lateinit var integerType: TypeMirror
        lateinit var longType: TypeMirror
        lateinit var floatType: TypeMirror
        lateinit var doubleType: TypeMirror

        override fun init(processingEnv: ProcessingEnvironment?) {
            super.init(processingEnv)
            if (processingEnv == null) {
                return
            }

            val elementUtils = processingEnv.elementUtils
            val typeUtils = processingEnv.typeUtils

            val base = elementUtils.getModuleElement("java.base")

            collectionType = typeUtils.erasure(elementUtils.getTypeElement(base, "java.util.Collection").asType())
            setType = typeUtils.erasure(elementUtils.getTypeElement(base, "java.util.Set").asType())
            mapType = typeUtils.erasure(elementUtils.getTypeElement(base,"java.util.Map").asType())

            stringType = typeUtils.erasure(elementUtils.getTypeElement(base, "java.lang.String").asType())
            numberType = typeUtils.erasure(elementUtils.getTypeElement(base, "java.lang.Number").asType())

            objectType = typeUtils.erasure(elementUtils.getTypeElement(base, "java.lang.Object").asType())
            booleanType = typeUtils.erasure(elementUtils.getTypeElement(base, "java.lang.Boolean").asType())
            characterType = typeUtils.erasure(elementUtils.getTypeElement(base, "java.lang.Character").asType())
            byteType = typeUtils.erasure(elementUtils.getTypeElement(base, "java.lang.Byte").asType())
            shortType = typeUtils.erasure(elementUtils.getTypeElement(base, "java.lang.Short").asType())
            integerType = typeUtils.erasure(elementUtils.getTypeElement(base, "java.lang.Integer").asType())
            longType = typeUtils.erasure(elementUtils.getTypeElement(base, "java.lang.Long").asType())
            floatType = typeUtils.erasure(elementUtils.getTypeElement(base, "java.lang.Float").asType())
            doubleType = typeUtils.erasure(elementUtils.getTypeElement(base, "java.lang.Double").asType())
        }


        private fun javax.lang.model.element.Element.toTypeElement(): TypeElement? {
            return when(kind) {
                ElementKind.CLASS, ElementKind.RECORD -> {
                    this as TypeElement
                }

                else -> {
                    logger.error("Exepected $this to be a class or record, but got $kind instead")
                    null
                }
            }
        }

        private fun TypeElement.model(): Pair<com.github.steanky.element.core.annotation.Model, TypeElement>? {
            getAnnotation(com.github.steanky.element.core.annotation.Model::class.java)?.let { model ->
                if (PATTERN.matcher(model.value).matches()) {
                    return Pair(model, this)
                }

                logger.error("Element $this has a Model annotation with an invalid value: ${model.value}")
                return null
            }

            logger.error("Element $this is missing a Model annotation")
            return null
        }

        private fun javax.lang.model.element.Element.name(): String {
            getAnnotation(Name::class.java)?.let { name ->
                return name.value
            }

            return this.simpleName.toString()
        }

        private fun javax.lang.model.element.Element.type(): String? {
            getAnnotation(Type::class.java)?.let { name ->
                return name.value
            }

            return null
        }

        private fun javax.lang.model.element.Element.description(): String {
            getAnnotation(Description::class.java)?.let { description ->
                return description.value
            }

            logger.error("Element $this missing Description annotation")
            return ""
        }

        private fun TypeElement.dataType(): TypeElement? {
            val factoryMethods = enclosedElements
                    .filter { it.kind == ElementKind.CONSTRUCTOR || it.kind == ElementKind.METHOD }
                    .map { it as ExecutableElement }
                    .mapNotNull { executableElement ->
                        executableElement.getAnnotation(FactoryMethod::class.java)?.let {
                            executableElement
                        }
                    }

            if (factoryMethods.isEmpty()) {
                logger.error("Element $this missing FactoryMethod")
                return null
            }

            if (factoryMethods.size > 1) {
                logger.error("Element $this has more than one FactoryMethod")
                return null
            }

            val executableElement = factoryMethods[0]
            return when(executableElement.kind!!) {
                ElementKind.METHOD -> {
                    if (executableElement.parameters.isNotEmpty()) {
                        logger.error("Static FactoryMethod of element $this has parameters")
                        return null
                    }

                    val returnType = processingEnv.typeUtils.asElement(executableElement.returnType)?.let { element ->
                        element.toTypeElement()?.let { typeElement ->
                            if (typeElement.qualifiedName.toString() != ElementFactory::class.java.canonicalName) {
                                val typeParameters = typeElement.typeParameters
                                if (typeParameters.size == 2) {
                                    typeParameters[0].toTypeElement()?.let {
                                        return it
                                    }
                                }
                            }
                        }
                    }

                    logger.error("Static FactoryMethod of element $this has invalid return type $returnType")
                    null
                }
                ElementKind.CONSTRUCTOR -> {
                    val dataParameters = executableElement.parameters.mapNotNull { parameter ->
                        parameter.getAnnotation(DataObject::class.java)?.let {
                            return@mapNotNull parameter
                        }

                        processingEnv.typeUtils.asElement(parameter.asType())?.let { element ->
                            element.getAnnotation(DataObject::class.java)?.let {
                                return@mapNotNull element.toTypeElement()
                            }
                        }
                    }

                    if (dataParameters.size > 1) {
                        logger.error("Constructor FactoryMethod of element $this has more than one data parameter")
                        return null
                    }

                    return dataParameters.firstOrNull()?.toTypeElement()
                }
                else -> error("Unexpected kind ${executableElement.kind}")
            }
        }

        override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
            if (roundEnv == null) {
                logger.error("RoundEnvironment instance required for annotation processing")
                return false
            }

            if (annotations.isNullOrEmpty()) {
                return false
            }

            val elements = roundEnv.getElementsAnnotatedWith(annotations.first())
            if (elements.isNullOrEmpty()) {
                return false
            }

            elements.forEach { element ->
                element.toTypeElement()?.model()?.let { (model, typeElement) ->
                    val parameterList = processParameters(typeElement)

                    val type = model.value
                    val name = typeElement.name()
                    val description = typeElement.description()

                    elementList.add(Element(type, name, description, parameterList, processTime))
                }
            }

            return false
        }

        private fun processParameters(typeElement: TypeElement): List<Parameter> {
            typeElement.dataType()?.let { dataType ->
                val parameters = dataType
                        .getAnnotationsByType(com.github.steanky.element.core.annotation.document.Parameter::class.java)
                if (!parameters.isNullOrEmpty()) {
                    return parameters.map { parameter ->
                        Parameter(parameter.type, parameter.name, parameter.behavior)
                    }
                }

                if (dataType.kind == ElementKind.RECORD) {
                    return dataType.recordComponents.map { component ->
                        val type = simpleTypeName(component, component.asType())
                        val name = component.name()
                        val behavior = component.description()

                        Parameter(type, name, behavior)
                    }
                }
            }

            logger.error("Could not resolve parameters for $typeElement")
            return listOf()
        }

        private fun simpleTypeName(element: javax.lang.model.element.Element, componentType: TypeMirror): String {
            val typeUtils = processingEnv.typeUtils

            return when(componentType.kind) {
                TypeKind.BOOLEAN -> "boolean"
                TypeKind.BYTE, TypeKind.SHORT, TypeKind.INT, TypeKind.LONG -> "whole number"
                TypeKind.CHAR -> "string"
                TypeKind.FLOAT, TypeKind.DOUBLE -> "decimal number"
                TypeKind.ARRAY -> "list of ${simpleTypeName(element, (componentType as ArrayType).componentType)}"
                TypeKind.DECLARED -> {
                    componentType as DeclaredType

                    if (typeUtils.isSameType(stringType, componentType) || typeUtils.isSameType(characterType, componentType)) {
                        return "string"
                    }
                    else if (typeUtils.isSameType(booleanType, componentType)) {
                        return "boolean"
                    }
                    else if (typeUtils.isSameType(byteType, componentType) ||
                            typeUtils.isSameType(shortType, componentType) ||
                            typeUtils.isSameType(integerType, componentType) ||
                            typeUtils.isSameType(longType, componentType)) {
                        return "whole number"
                    }
                    else if (typeUtils.isSameType(floatType, componentType) ||
                            typeUtils.isSameType(doubleType, componentType)) {
                        return "decimal number"
                    }
                    else if (typeUtils.isSameType(numberType, componentType)) {
                        return "number"
                    }
                    else if (typeUtils.isSameType(objectType, componentType)) {
                        return "any"
                    }

                    element.type() ?:
                    collectionType(element, "set", typeUtils, componentType, setType) ?:
                    collectionType(element, "list", typeUtils, componentType, collectionType) ?:
                    mapType(element, typeUtils, componentType) ?:
                    (element.asType() as DeclaredType).asElement().simpleName.toString()
                }
                TypeKind.TYPEVAR -> {
                    componentType as TypeVariable
                    return simpleTypeName(element, componentType.upperBound)
                }
                TypeKind.WILDCARD -> {
                    componentType as WildcardType
                    return simpleTypeName(element, componentType.extendsBound ?: objectType)
                }
                else -> {
                    logger.error("Unrecognized type $componentType")
                    ""
                }
            }
        }

        private fun collectionType(element: javax.lang.model.element.Element, name: String, typeUtils: Types, component: DeclaredType, collectionType: TypeMirror): String? {
            if (typeUtils.isSameType(typeUtils.erasure(component), collectionType)) {
                val typeArguments = component.typeArguments
                if (typeArguments.size == 0) {
                    return "$name of any"
                }

                return "$name of ${simpleTypeName(element, typeArguments[0])}"
            }

            if (typeUtils.isAssignable(component, collectionType)) {
                typeUtils.directSupertypes(component).forEach { superMirror ->
                    superMirror as DeclaredType

                    val mirrorErasure = typeUtils.erasure(superMirror)
                    if (typeUtils.isSameType(collectionType, mirrorErasure)) {
                        return "$name of ${simpleTypeName(element, superMirror.typeArguments[0])}"
                    }
                }

                return "$name of any"
            }

            return null
        }

        private fun mapType(element: javax.lang.model.element.Element, typeUtils: Types, component: DeclaredType): String? {
            if (typeUtils.isSameType(mapType, typeUtils.erasure(component))) {
                val typeArguments = component.typeArguments
                if (typeArguments.size != 2) {
                    return "map of any"
                }

                return "map of ${simpleTypeName(element, typeArguments[0])} -> ${simpleTypeName(element, typeArguments[1])}"
            }

            if (typeUtils.isAssignable(component, mapType)) {
                typeUtils.directSupertypes(component).forEach {superMirror ->
                    superMirror as DeclaredType

                    val mirorErasure = typeUtils.erasure(superMirror)
                    if (typeUtils.isSameType(mapType, mirorErasure)) {
                        val typeArguments = superMirror.typeArguments
                        return "map of ${simpleTypeName(element, typeArguments[0])} -> ${simpleTypeName(element, typeArguments[1])}"
                    }
                }

                return "map of any -> any"
            }

            return null
        }
    }
}