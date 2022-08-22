package com.github.steanky.element.core.factory;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.annotation.DataName;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.data.ElementContext;
import com.github.steanky.element.core.data.DataInspector;
import com.github.steanky.element.core.data.DataInspector.PathFunction;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.element.ElementFactory;
import com.github.steanky.element.core.element.ElementTypeIdentifier;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.util.ReflectionUtils;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private Object[] resolveArguments(final Object objectData, final ElementContext data,
            final DependencyProvider provider, final ElementSpec spec) {
        final Object[] args;
        if (spec.dataIndex == -1) {
            args = new Object[spec.parameters.size()];
            for (int i = 0; i < spec.parameters.size(); i++) {
                args[i] = processParameter(spec.pathFunction, spec.parameters.get(i), objectData, data, provider);
            }

            return args;
        }

        args = new Object[spec.parameters.size() + 1];
        args[spec.dataIndex] = objectData;

        for (int i = 0; i < spec.dataIndex; i++) {
            args[i] = processParameter(spec.pathFunction, spec.parameters.get(i), objectData, data, provider);
        }

        for (int i = spec.dataIndex + 1; i < args.length; i++) {
            args[i] = processParameter(spec.pathFunction, spec.parameters.get(i - 1), objectData, data, provider);
        }

        return args;
    }

    private Object processParameter(final DataInspector.PathFunction pathFunction, final ElementParameter parameter,
            final Object objectData, final ElementContext data, final DependencyProvider provider) {
        if (parameter.isDependency) {
            return provider.provide(parameter.type, parameter.id);
        }

        final Key dataPath = pathFunction.apply(objectData, parameter.id);
        return data.provide(dataPath, provider);
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

                final ParameterizedType type = validateParameterizedReturnType(declaredMethod,
                        () -> "FactoryMethod returned raw parameterized class");
                final Type[] typeArguments = type.getActualTypeArguments();
                if (typeArguments.length != 2) {
                    //this is likely unreachable, as we are guaranteed to be an instance of ElementFactory
                    throw elementException(elementClass,
                            "unexpected number of type arguments on FactoryMethod return type");
                }

                validateGenericType(elementClass, elementClass, typeArguments[1], () -> "FactoryMethod returned a " +
                        "factory whose constructed type is not assignable to this class");

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

                elementParameters.add(new ElementParameter(null, name, false));
                hasComposite = true;
                continue;
            }

            final String name = dependency.name();
            elementParameters.add(new ElementParameter(parseKey(keyParser, dependency.value()),
                    name.equals(Dependency.DEFAULT_NAME) ? null : parseKey(keyParser, name), true));
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

        final DataInspector.PathFunction pathFunction =
                dataClass == null ? null : dataInspector.pathFunction(dataClass);
        final ElementSpec elementSpec = new ElementSpec(elementParameters, dataParameterIndex, pathFunction);
        return (objectData, data, dependencyProvider) -> ReflectionUtils.invokeConstructor(finalFactoryConstructor,
                resolveArguments(objectData, data, dependencyProvider, elementSpec));
    }

    private record ElementSpec(List<ElementParameter> parameters, int dataIndex,
            DataInspector.PathFunction pathFunction) {}

    private record ElementParameter(Key type, Key id, boolean isDependency) {}
}