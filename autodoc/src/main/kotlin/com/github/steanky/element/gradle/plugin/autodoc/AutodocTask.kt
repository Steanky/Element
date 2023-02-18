package com.github.steanky.element.gradle.plugin.autodoc

import com.github.steanky.element.core.ElementFactory
import com.github.steanky.element.core.annotation.Child
import com.github.steanky.element.core.annotation.ChildPath
import com.github.steanky.element.core.annotation.DataObject
import com.github.steanky.element.core.annotation.FactoryMethod
import com.github.steanky.element.core.annotation.document.Description
import com.github.steanky.element.core.annotation.document.Group
import com.github.steanky.element.core.annotation.document.Name
import com.github.steanky.element.core.annotation.document.Type
import com.github.steanky.element.core.key.Constants
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.*
import java.io.File

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

    var ext: AutodocPlugin.Extension = project.extensions.getByType(AutodocPlugin.Extension::class.java)
        @Input get

    var outputFile: File = project.buildDir.resolve("elementAutodoc").resolve("model.json")
        @OutputFile get

    @TaskAction
    fun generateAutodoc() {
        val settings = ext.resolve()
        val model = Model(processClasses(source.files, settings).sortedBy { it.type }, settings)
        val json = Json.encodeToJsonElement(model)

        outputFile.writeText(json.toString())
    }

    private fun processClasses(files: Iterable<File>, settings: Settings) : List<Element> {
        val compiler = ToolProvider.getSystemJavaCompiler()
        val logger = project.logger

        val processTime = if (settings.recordTime) { System.currentTimeMillis() } else 0

        val elementList = mutableListOf<Element>()
        compiler.getStandardFileManager(null, null, null).use {
            val sources = it.getJavaFileObjectsFromFiles(files).filter { file ->
                file.kind == JavaFileObject.Kind.SOURCE
            }

            it.handleOption("-cp", listOf(project.configurations.getAt("compileClasspath").files.map { "$it" }
                    .joinToString(":")).listIterator())

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

            stringType = elementUtils.getTypeElement(base, "java.lang.String").asType()
            numberType = elementUtils.getTypeElement(base, "java.lang.Number").asType()

            objectType = elementUtils.getTypeElement(base, "java.lang.Object").asType()
            booleanType = elementUtils.getTypeElement(base, "java.lang.Boolean").asType()
            characterType = elementUtils.getTypeElement(base, "java.lang.Character").asType()
            byteType = elementUtils.getTypeElement(base, "java.lang.Byte").asType()
            shortType = elementUtils.getTypeElement(base, "java.lang.Short").asType()
            integerType = elementUtils.getTypeElement(base, "java.lang.Integer").asType()
            longType = elementUtils.getTypeElement(base, "java.lang.Long").asType()
            floatType = elementUtils.getTypeElement(base, "java.lang.Float").asType()
            doubleType = elementUtils.getTypeElement(base, "java.lang.Double").asType()
        }


        private fun javax.lang.model.element.Element.asTypeElement(): TypeElement? {
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

            logger.error("Element $this missing a Model annotation")
            return null
        }

        private fun javax.lang.model.element.Element.name(): String {
            getAnnotation(Name::class.java)?.let { name ->
                return name.value
            }

            return this.simpleName.toString()
        }

        private fun TypeElement.group(): String {
            getAnnotation(Group::class.java)?.let { group ->
                return group.value
            }

            super.processingEnv.elementUtils.getPackageOf(this)?.getAnnotation(Group::class.java)?.let { group ->
                return group.value
            }

            logger.error("Element $this missing Group annotation")
            return ""
        }

        private fun javax.lang.model.element.Element.type(): String? {
            getAnnotation(Type::class.java)?.let { type ->
                return type.value
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

        private fun TypeElement.dataType(): Pair<TypeElement, Map<String, VariableElement>>? {
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
                        element.asTypeElement()?.let { typeElement ->
                            if (typeElement.qualifiedName.toString() != ElementFactory::class.java.canonicalName) {
                                val typeParameters = typeElement.typeParameters
                                if (typeParameters.size == 2) {
                                    typeParameters[0].asTypeElement()?.let {
                                        return Pair(it, mapOf())
                                    }
                                }
                            }
                        }
                    }

                    logger.error("Static FactoryMethod of element $this has invalid return type $returnType")
                    null
                }
                ElementKind.CONSTRUCTOR -> {
                    val mappings = hashMapOf<String, VariableElement>()
                    val dataParameters = executableElement.parameters.mapNotNull { parameter ->
                        parameter.getAnnotation(Child::class.java)?.let outer@{ child ->
                            val childValue = child.value
                            if (childValue != Constants.DEFAULT) {
                                if (mappings.putIfAbsent(childValue, parameter) != null) {
                                    logger.error("Child mapping $childValue occurs twice")
                                }
                            } else {
                                parameter.asType().getAnnotation(com.github.steanky.element.core.annotation.Model::class.java)
                                        ?.let { model ->
                                            if (mappings.putIfAbsent(model.value, parameter) != null) {
                                                logger.error("Ambiguous mapping to ${model.value}")
                                            }

                                            return@outer
                                        }

                                logger.error("Child mapping $childValue does not annotate an element type")
                            }
                        }

                        parameter.getAnnotation(DataObject::class.java)?.let {
                            return@mapNotNull parameter
                        }

                        processingEnv.typeUtils.asElement(parameter.asType())?.let { element ->
                            element.getAnnotation(DataObject::class.java)?.let {
                                return@mapNotNull element.asTypeElement()
                            }
                        }
                    }

                    if (dataParameters.size > 1) {
                        logger.error("Constructor FactoryMethod of element $this has more than one data parameter")
                        return null
                    }

                    if (dataParameters.isEmpty()) {
                        val nestedDataObjects = this.enclosedElements.filter { it.kind == ElementKind.RECORD &&
                                it.getAnnotation(DataObject::class.java) != null }

                        if (nestedDataObjects.size > 1) {
                            logger.error("More than one nested data object")
                            return null
                        }

                        return nestedDataObjects.firstOrNull()?.asTypeElement()?.let {
                            return Pair(it, mappings)
                        }
                    }

                    return dataParameters.first().asTypeElement()?.let {
                        return Pair(it, mappings)
                    }
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
                element.asTypeElement()?.model()?.let { (model, typeElement) ->
                    val type = model.value
                    val name = typeElement.name()
                    val group = typeElement.group()
                    val description = typeElement.description()

                    val parameterList = processParameters(typeElement)

                    elementList.add(Element(type, name, group, description, parameterList, processTime))
                }
            }

            return false
        }

        private fun processParameters(typeElement: TypeElement): List<Parameter> {
            typeElement.dataType()?.let { (dataType, mappings) ->
                val parameters = dataType
                        .getAnnotationsByType(com.github.steanky.element.core.annotation.document.Parameter::class.java)
                if (!parameters.isNullOrEmpty()) {
                    return parameters.map { parameter ->
                        Parameter(parameter.type, parameter.name, parameter.behavior)
                    }
                }

                if (dataType.kind == ElementKind.RECORD) {
                    return dataType.recordComponents.map { component ->
                        val accessor = component.accessor

                        val type = accessor.type() ?: tryLink(accessor, mappings) ?: simpleTypeName(component.asType())
                        val name = accessor.name()
                        val behavior = accessor.description()

                        Parameter(type, name, behavior)
                    }
                }

                logger.error("Could not resolve parameters for data class $typeElement as it is not a record")
            }

            return listOf()
        }

        private fun tryLink(component: ExecutableElement, map: Map<String, VariableElement>): String? {
            component.getAnnotation(ChildPath::class.java)?.let { childPath ->
                map[childPath.value]?.let { variableElement ->
                    processingEnv.typeUtils.asElement(variableElement.asType()).asTypeElement()?.model()?.let { (model, _) ->
                        return model.value
                    }

                    return null
                }

                logger.error("No child dependency named ${childPath.value}")
                return null
            }

            return null
        }

        private fun simpleTypeName(componentType: TypeMirror): String {
            val typeUtils = processingEnv.typeUtils

            return when(componentType.kind) {
                TypeKind.BOOLEAN -> "boolean"
                TypeKind.BYTE, TypeKind.SHORT, TypeKind.INT, TypeKind.LONG -> "whole number"
                TypeKind.CHAR -> "string"
                TypeKind.FLOAT, TypeKind.DOUBLE -> "decimal number"
                TypeKind.ARRAY -> "list of ${simpleTypeName((componentType as ArrayType).componentType)}"
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

                    return collectionType("set", typeUtils, componentType, setType) ?:
                    collectionType("list", typeUtils, componentType, collectionType) ?:
                    mapType(typeUtils, componentType) ?: componentType.asElement().simpleName.toString()
                }
                TypeKind.TYPEVAR -> {
                    componentType as TypeVariable
                    return simpleTypeName(componentType.upperBound)
                }
                TypeKind.WILDCARD -> {
                    componentType as WildcardType
                    return simpleTypeName(componentType.extendsBound ?: objectType)
                }
                else -> {
                    logger.error("Unrecognized type $componentType")
                    return ""
                }
            }
        }

        private fun collectionType(name: String, typeUtils: Types, component: DeclaredType, collectionType: TypeMirror): String? {
            if (typeUtils.isSameType(typeUtils.erasure(component), collectionType)) {
                val typeArguments = component.typeArguments
                if (typeArguments.size == 0) {
                    return "$name of any"
                }

                return "$name of ${simpleTypeName(typeArguments[0])}"
            }

            if (typeUtils.isAssignable(component, collectionType)) {
                typeUtils.directSupertypes(component).forEach { superMirror ->
                    superMirror as DeclaredType

                    val mirrorErasure = typeUtils.erasure(superMirror)
                    if (typeUtils.isSameType(collectionType, mirrorErasure)) {
                        return "$name of ${simpleTypeName(superMirror.typeArguments[0])}"
                    }
                }

                return "$name of any"
            }

            return null
        }

        private fun mapType(typeUtils: Types, component: DeclaredType): String? {
            if (typeUtils.isSameType(mapType, typeUtils.erasure(component))) {
                val typeArguments = component.typeArguments
                if (typeArguments.size != 2) {
                    return "map of any"
                }

                return "map of ${simpleTypeName(typeArguments[0])} -> ${simpleTypeName(typeArguments[1])}"
            }

            if (typeUtils.isAssignable(component, mapType)) {
                typeUtils.directSupertypes(component).forEach {superMirror ->
                    superMirror as DeclaredType

                    val mirorErasure = typeUtils.erasure(superMirror)
                    if (typeUtils.isSameType(mapType, mirorErasure)) {
                        val typeArguments = superMirror.typeArguments
                        return "map of ${simpleTypeName(typeArguments[0])} -> ${simpleTypeName(typeArguments[1])}"
                    }
                }

                return "map of any -> any"
            }

            return null
        }
    }
}