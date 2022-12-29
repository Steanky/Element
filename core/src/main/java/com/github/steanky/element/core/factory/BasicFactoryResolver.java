package com.github.steanky.element.core.factory;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.data.DataInspector;
import com.github.steanky.element.core.data.DataInspector.PathFunction;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.element.core.util.ReflectionUtils;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;

import static com.github.steanky.element.core.util.Validate.*;

/**
 * Basic implementation of {@link FactoryResolver}. Can resolve classes with either explicit factories (a
 * {@code public static} method annotated with {@link FactoryMethod}) or factory constructors.
 */
public class BasicFactoryResolver implements FactoryResolver {
    private final KeyParser keyParser;
    private final DataInspector dataInspector;
    private final ContainerCreator containerCreator;
    private final MappingProcessorSource processorSource;

    /**
     * Creates a new instance of this class.
     *
     * @param keyParser         the {@link KeyParser} implementation used to interpret strings as keys
     * @param dataInspector     the {@link DataInspector} object used to extract {@link PathFunction}s from data
     *                          classes
     * @param collectionCreator the {@link ContainerCreator} used to reflectively create collection instances when
     *                          necessary, when requiring multiple element dependencies
     * @param processorSource   the {@link MappingProcessorSource} used to create {@link ConfigProcessor}
     *                          implementations on-demand for data classes.
     *                          {@link MappingProcessorSource.Builder#ignoringLengths()} should be used to avoid issues
     *                          when deserializing composite elements
     */
    public BasicFactoryResolver(final @NotNull KeyParser keyParser, final @NotNull DataInspector dataInspector,
            final @NotNull ContainerCreator collectionCreator, final @NotNull MappingProcessorSource processorSource) {
        this.keyParser = Objects.requireNonNull(keyParser);
        this.dataInspector = Objects.requireNonNull(dataInspector);
        this.containerCreator = Objects.requireNonNull(collectionCreator);
        this.processorSource = Objects.requireNonNull(processorSource);
    }

    private static ElementPath path(ElementPath dataPath, String relativePath) {
        return dataPath.resolve(relativePath);
    }

    @Override
    public @NotNull ElementFactory<?, ?> resolveFactory(final @NotNull Class<?> elementClass,
            final @NotNull Mutable<ConfigProcessor<?>> processor) {
        final Method[] declaredMethods = elementClass.getDeclaredMethods();
        Method factoryMethod = null;
        for (final Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(FactoryMethod.class)) {
                if (factoryMethod != null) {
                    throw elementException(elementClass, "more than one FactoryMethod");
                }

                validateModifiersPresent(declaredMethod, () -> "FactoryMethod not declared public static",
                        Modifier.PUBLIC, Modifier.STATIC);
                validateReturnType(declaredMethod, ElementFactory.class,
                        () -> "FactoryMethod does not return an ElementFactory");
                validateParameterCount(declaredMethod, 0, () -> "FactoryMethod has parameters");

                final Type requiredType = TypeUtils.parameterize(ElementFactory.class, TypeUtils.WILDCARD_ALL,
                        TypeUtils.wildcardType().withUpperBounds(elementClass).build());
                validateType(elementClass, requiredType, declaredMethod.getGenericReturnType(),
                        () -> "FactoryMethod returned type not assignable to ElementFactory<?, ? extends T> where T " +
                                "is the element type");
                factoryMethod = declaredMethod;
            }
        }

        //if an explicit factory method is provided, use that and don't try to infer one from the constructor
        if (factoryMethod != null) {
            final ElementFactory<?, ?> factory = ReflectionUtils.invokeMethod(factoryMethod, null);
            if (factory == null) {
                throw elementException(elementClass, "explicit FactoryMethod returned null");
            }

            return factory;
        }

        final Constructor<?>[] declaredConstructors = elementClass.getDeclaredConstructors();
        Constructor<?> factoryConstructor = null;
        for (final Constructor<?> declaredConstructor : declaredConstructors) {
            if (declaredConstructor.isAnnotationPresent(FactoryMethod.class)) {
                if (factoryConstructor != null) {
                    throw elementException(elementClass, "more than one factory constructor");
                }

                validateModifiersPresent(declaredConstructor, () -> "factory constructor must be public",
                        Modifier.PUBLIC);

                factoryConstructor = declaredConstructor;
            }
        }

        if (factoryConstructor == null) {
            throw elementException(elementClass, "no suitable factory method or constructor");
        }

        final Constructor<?> finalFactoryConstructor = factoryConstructor;
        final Parameter[] parameters = finalFactoryConstructor.getParameters();
        if (parameters.length == 0) {
            return (objectData, dataPath, data, dependencyProvider) -> ReflectionUtils.invokeConstructor(
                    finalFactoryConstructor);
        }

        final ElementParameter[] elementParameters = new ElementParameter[parameters.length];
        boolean hasComposite = false;
        Class<?> dataClass = null;

        final Set<Key> childKeys = new HashSet<>(5);
        for (int i = 0; i < elementParameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Class<?> parameterType = parameter.getType();

            DataObject dataObjectAnnotation = parameter.getAnnotation(DataObject.class);
            if (dataObjectAnnotation == null) {
                dataObjectAnnotation = parameterType.getAnnotation(DataObject.class);
            }

            final boolean hasDataAnnotation = dataObjectAnnotation != null;
            if (hasDataAnnotation) {
                if (dataClass != null) {
                    throw elementException(elementClass, "cannot have more than one DataObject parameter");
                }

                dataClass = parameterType;
            }

            final Depend dependAnnotation = parameter.getAnnotation(Depend.class);
            final boolean hasDependAnnotation = dependAnnotation != null;

            if (hasDataAnnotation && hasDependAnnotation) {
                //ambiguous case, we don't know if we should deserialize or load a dependency
                throw elementException(elementClass, "parameter is annotated with both DataObject and Depend");
            }

            final Model modelAnnotation = parameterType.getAnnotation(Model.class);
            final boolean hasModelAnnotation = modelAnnotation != null;

            final Child childAnnotation = parameter.getAnnotation(Child.class);
            final boolean hasChildAnnotation = childAnnotation != null;

            if (hasChildAnnotation) {
                //these cases aren't inherently problematic, but indicate improper usage or confusion
                if (hasDataAnnotation) {
                    throw elementException(elementClass, "parameter is annotated with both Child and DataObject");
                }

                if (hasDependAnnotation) {
                    throw elementException(elementClass, "parameter is annotated with both Child and Depend");
                }
            }

            final ParameterType type;
            final Key info;
            final boolean container;
            final boolean cache;
            if (hasDataAnnotation) {
                type = ParameterType.DATA;
                info = null;
                container = false;
                cache = false;
            } else if (hasDependAnnotation) {
                //if the parameter has Depend, treat it as a regular dependency
                type = ParameterType.DEPENDENCY;

                @Subst(Constants.NAMESPACE_OR_KEY) final String value = dependAnnotation.value();
                final boolean isDefault = value.equals(Constants.DEFAULT);
                info = isDefault ? null : keyParser.parseKey(value);
                container = false;
                cache = false;
            } else if (hasChildAnnotation) {
                //parameters with Child, but no Depend, are composite dependencies
                type = ParameterType.COMPOSITE;

                @Subst(Constants.NAMESPACE_OR_KEY) final String value;

                final boolean childProvidesPath = !childAnnotation.value().equals(Constants.DEFAULT);

                if (hasModelAnnotation) {
                    //parameter is itself a Model type; we can use its model key as a data path
                    value = childProvidesPath ? childAnnotation.value() : modelAnnotation.value();
                    container = false;
                } else {
                    //parameter is not a Model type, but it might be a collection of model types
                    final Token<?> parameterGenericType = Token.ofType(parameter.getParameterizedType());

                    if (containerCreator.isContainerType(parameterGenericType)) {
                        final Class<?> modelClass = containerCreator.extractComponentType(parameterGenericType)
                                .rawType();

                        final Model model = modelClass.getAnnotation(Model.class);
                        if (model == null && !childProvidesPath) {
                            //could not find Model annotation on component type, and we have no child path
                            throw elementException(elementClass,
                                    "extracted component type of parameter does not have a Model annotation, and the " +
                                            "Child annotation does not provide a path");
                        }

                        value = childProvidesPath ? childAnnotation.value() : model.value();
                        container = true;
                    }
                    else if (childProvidesPath) {
                        //no model annotation, not a collection, but we have a child path so use that
                        value = childAnnotation.value();
                        container = false;
                    }
                    else {
                        //child paths are required in cases where we can't obtain a model
                        throw elementException(elementClass, "could not infer data path from non-element Child " +
                                "dependency");
                    }
                }

                info = keyParser.parseKey(value);

                if (!childKeys.add(info)) {
                    throw elementException(elementClass, "duplicate child key " + info);
                }

                //set composite flag, so we inspect the data class afterwards for path keys
                hasComposite = true;

                final Cache cacheAnnotation = parameter.getAnnotation(Cache.class);
                cache = cacheAnnotation != null && cacheAnnotation.value();
            } else {
                //parameters with nothing are treated as dependencies
                type = ParameterType.DEPENDENCY;
                info = null;
                container = false;
                cache = false;
            }

            elementParameters[i] = new ElementParameter(parameter, type, info, container, cache);
        }

        DataInspector.DataInformation data = null;
        if (dataClass != null) {
            if (hasComposite) {
                data = dataInspector.inspectData(dataClass);
            }
        } else if (hasComposite) {
            final Class<?>[] children = elementClass.getDeclaredClasses();

            Class<?> inferredDataClass = null;
            for (final Class<?> child : children) {
                if (child.isAnnotationPresent(DataObject.class)) {
                    if (inferredDataClass != null) {
                        throw elementException(elementClass, "there may only be one inferred data class");
                    }

                    inferredDataClass = child;
                }
            }

            if (inferredDataClass == null) {
                throw elementException(elementClass, "unable to infer data class");
            }

            dataClass = inferredDataClass;
            data = dataInspector.inspectData(dataClass);
        }

        if (data != null) {
            final Map<Key, PathFunction.PathInfo> functionMap = data.infoMap();
            for (final Key requiredKey : childKeys) {
                if (!functionMap.containsKey(requiredKey)) {
                    throw elementException(elementClass, "data class missing required child key " + requiredKey);
                }
            }
        }

        if (processor.getValue() == null && dataClass != null) {
            processor.setValue(processorSource.processorFor(Token.ofClass(dataClass)));
        }

        return buildFactory(finalFactoryConstructor, elementParameters, data);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ElementFactory<?, ?> buildFactory(final Constructor<?> constructor, final ElementParameter[] parameters,
            final DataInspector.DataInformation dataInformation) {
        return (objectData, dataPath, context, dependencyProvider) -> {
            //objects that will be used to construct the new model
            final Object[] args = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                final ElementParameter parameter = parameters[i];

                args[i] = switch (parameter.type) {
                    case DATA -> objectData;
                    case DEPENDENCY -> dependencyProvider.provide(
                            DependencyProvider.key(Token.ofType(parameter.parameter.getParameterizedType()),
                                    parameter.info));
                    case COMPOSITE -> {
                        //inspect the data at runtime if necessary
                        //this happens for element classes whose data only consists of paths
                        final DataInspector.DataInformation information = Objects.requireNonNullElseGet(dataInformation,
                                () -> dataInspector.inspectData(objectData.getClass()));

                        final Collection<String> paths = information.pathFunction().apply(objectData, parameter.info);
                        final Object object;
                        if (parameter.container) {
                            object = containerCreator.createContainer(parameter.parameter.getType(), paths.size());

                            if (object.getClass().isArray()) {
                                final Iterator<String> pathsIterator = paths.iterator();
                                for (int j = 0; pathsIterator.hasNext(); j++) {
                                    final String path = pathsIterator.next();
                                    Array.set(object, j,
                                            context.provide(path(dataPath, path), dependencyProvider, parameter.cache));
                                }
                            } else {
                                final Collection objects = (Collection) object;
                                for (final String path : paths) {
                                    objects.add(
                                            context.provide(path(dataPath, path), dependencyProvider, parameter.cache));
                                }
                            }
                        } else {
                            if (paths.isEmpty()) {
                                throw new ElementException("no path found to construct child element");
                            }

                            final String path = paths.iterator().next();
                            object = context.provide(path(dataPath, path), dependencyProvider, parameter.cache);
                        }

                        yield object;
                    }
                };
            }

            return ReflectionUtils.invokeConstructor(constructor, args);
        };
    }

    private enum ParameterType {
        DATA, DEPENDENCY, COMPOSITE
    }

    private record ElementParameter(Parameter parameter, ParameterType type, Key info, boolean container,
            boolean cache) {}
}