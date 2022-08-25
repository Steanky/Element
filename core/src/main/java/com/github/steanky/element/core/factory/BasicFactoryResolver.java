package com.github.steanky.element.core.factory;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.ElementTypeIdentifier;
import com.github.steanky.element.core.annotation.DataName;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.data.DataInspector;
import com.github.steanky.element.core.data.DataInspector.PathFunction;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.util.ReflectionUtils;
import net.kyori.adventure.key.Key;
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
    private final ElementTypeIdentifier elementTypeIdentifier;
    private final DataInspector dataInspector;

    /**
     * Creates a new instance of this class.
     *
     * @param keyParser             the {@link KeyParser} implementation used to interpret strings as keys
     * @param elementTypeIdentifier the {@link ElementTypeIdentifier} used to extract type keys from element classes
     * @param dataInspector         the {@link DataInspector} object used to extract {@link PathFunction}s from data
     *                              classes
     */
    public BasicFactoryResolver(final @NotNull KeyParser keyParser,
            final @NotNull ElementTypeIdentifier elementTypeIdentifier, final @NotNull DataInspector dataInspector) {
        this.keyParser = Objects.requireNonNull(keyParser);
        this.elementTypeIdentifier = Objects.requireNonNull(elementTypeIdentifier);
        this.dataInspector = Objects.requireNonNull(dataInspector);
    }

    private static Key parseKey(final KeyParser parser, final @Subst(Constants.NAMESPACE_OR_KEY) String keyString) {
        return parser.parseKey(keyString);
    }

    private Object[] resolveArguments(final Object objectData, final ElementContext context,
            final DependencyProvider provider, final ElementSpec spec) {
        final Object[] args;
        if (spec.dataIndex == -1) {
            args = new Object[spec.parameters.size()];
            for (int i = 0; i < spec.parameters.size(); i++) {
                args[i] = processParameter(spec.parameters.get(i), context, provider, spec.pathSpec, objectData);
            }

            return args;
        }

        args = new Object[spec.parameters.size() + 1];
        args[spec.dataIndex] = objectData;

        for (int i = 0; i < spec.dataIndex; i++) {
            args[i] = processParameter(spec.parameters.get(i), context, provider, spec.pathSpec, objectData);
        }

        for (int i = spec.dataIndex + 1; i < args.length; i++) {
            args[i] = processParameter(spec.parameters.get(i - 1), context, provider, spec.pathSpec, objectData);
        }

        return args;
    }

    private Object processParameter(final ElementParameter parameter, final ElementContext context,
            final DependencyProvider provider, final DataInspector.PathSpec pathSpec, final Object data) {
        if (parameter.isDependency) {
            return provider.provide(parameter.type, parameter.id);
        }

        final PathFunction.PathInfo info = pathSpec.infoMap().get(parameter.id);
        final Collection<? extends String> path = pathSpec.pathFunction().apply(data, parameter.id);
        if (info.isCollection()) {
            final Collection<Object> collection = new ArrayList<>(path.size());
            for (final String elementPath : path) {
                collection.add(info.annotation().cache() ? context.provideAndCache(elementPath, provider) : context
                        .provide(elementPath, provider));
            }

            return collection;
        }

        final String onlyPath = path.iterator().next();
        return info.annotation().cache() ? context.provideAndCache(onlyPath, provider) : context.provide(onlyPath,
                provider);
    }

    @Override
    public @NotNull ElementFactory<?, ?> resolveFactory(final @NotNull Class<?> elementClass,
            final boolean hasProcessor) {
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

                final Type requiredType = TypeUtils.parameterize(ElementFactory.class, TypeUtils.WILDCARD_ALL, TypeUtils
                        .wildcardType().withUpperBounds(elementClass).build());
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
                throw elementException(elementClass, "FactoryMethod returned null");
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
        final Parameter[] parameters = factoryConstructor.getParameters();
        if (parameters.length == 0) {
            return (objectData, data, dependencyProvider) -> ReflectionUtils.invokeConstructor(finalFactoryConstructor);
        }

        final ArrayList<ElementParameter> elementParameters = new ArrayList<>(parameters.length);
        int dataParameterIndex = -1;
        Class<?> dataClass = null;
        boolean hasComposite = false;
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];

            if (parameter.isAnnotationPresent(DataObject.class) ||
                    parameter.getType().isAnnotationPresent(DataObject.class)) {
                if (dataParameterIndex != -1) {
                    throw elementException(elementClass, "more than one ElementData on constructor factory");
                }

                if (parameter.isAnnotationPresent(Dependency.class)) {
                    throw elementException(elementClass, "Dependency present on data parameter");
                }

                if (parameter.isAnnotationPresent(DataName.class)) {
                    throw elementException(elementClass, "DataName present on data parameter");
                }

                dataParameterIndex = i;
                dataClass = parameter.getType();
                continue;
            }

            Dependency dependency = parameter.getDeclaredAnnotation(Dependency.class);
            if (dependency == null) {
                dependency = parameter.getType().getDeclaredAnnotation(Dependency.class);
            }

            if (dependency == null) {
                final Key name;
                final DataName nameAnnotation = parameter.getDeclaredAnnotation(DataName.class);
                if (nameAnnotation == null) {
                    try {
                        name = elementTypeIdentifier.identify(parameter.getType());
                    } catch (ElementException e) {
                        throw elementException(elementClass,
                                "unnamed composite dependency or missing dependency annotation", e);
                    }
                } else {
                    name = parseKey(keyParser, nameAnnotation.value());
                }

                elementParameters.add(new ElementParameter(parameter, null, name, false));
                hasComposite = true;
                continue;
            }

            final String name = dependency.name();
            elementParameters.add(new ElementParameter(parameter, parseKey(keyParser, dependency.value()),
                    name.equals(Constants.DEFAULT) ? null : parseKey(keyParser, name), true));
        }

        if (hasComposite && dataClass == null) {
            throw elementException(elementClass, "found composite dependency, but no data class");
        }

        if (dataParameterIndex == -1 && hasProcessor) {
            throw elementException(elementClass,
                    "no data parameter found on constructor factory, but class specifies a processor");
        }

        if (dataParameterIndex != -1 && !hasProcessor) {
            throw elementException(elementClass,
                    "found data parameter on constructor factory, but class does not specify a processor");
        }

        elementParameters.trimToSize();

        final DataInspector.PathSpec pathSpec = dataClass == null ? null : dataInspector.inspectData(dataClass);
        if (pathSpec != null) {
            final Map<Key, PathFunction.PathInfo> infoMap = pathSpec.infoMap();

            int nonDependencyCount = 0;
            for (final ElementParameter parameter : elementParameters) {
                if (!parameter.isDependency) {
                    final PathFunction.PathInfo info = infoMap.get(parameter.id);
                    if (info == null) {
                        throw elementException(elementClass, "missing element dependency with parameter name '" +
                                parameter.id + "'");
                    }

                    if (info.isCollection()) {
                        validateType(elementClass, DataInspector.COLLECTION_TYPE, parameter.parameter
                                .getParameterizedType(), () -> "parameter with name '" + parameter.id + "' must " +
                                "be assignable to Collection<? extends String>");
                    }
                    else {
                        validateType(elementClass, String.class, parameter.parameter.getType(), () -> "parameter " +
                                "with name '" + parameter.id + "' must be assignable to String");
                    }

                    nonDependencyCount++;
                }
            }

            final int size = infoMap.size();
            if (nonDependencyCount != size) {
                throw elementException(elementClass, "unexpected number of element dependency parameters, needed " +
                        size + ", was " + nonDependencyCount);
            }
        }

        final ElementSpec elementSpec = new ElementSpec(elementParameters, dataParameterIndex, pathSpec);
        return (objectData, data, dependencyProvider) -> ReflectionUtils.invokeConstructor(finalFactoryConstructor,
                resolveArguments(objectData, data, dependencyProvider, elementSpec));
    }

    private record ElementSpec(List<ElementParameter> parameters, int dataIndex,
            DataInspector.PathSpec pathSpec) {}

    private record ElementParameter(Parameter parameter, Key type, Key id, boolean isDependency) {}
}