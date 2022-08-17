package com.github.steanky.element.core.factory;

import com.github.steanky.element.core.annotation.DataName;
import com.github.steanky.element.core.annotation.Data;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.data.DataInspector;
import com.github.steanky.element.core.data.ElementData;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.element.ElementBuilder;
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

    /**
     * Creates a new instance of this class.
     *
     * @param keyParser the {@link KeyParser} implementation used to interpret strings as keys
     */
    public BasicFactoryResolver(final @NotNull KeyParser keyParser,
            final @NotNull ElementTypeIdentifier elementTypeIdentifier) {
        this.keyParser = Objects.requireNonNull(keyParser);
        this.elementTypeIdentifier = Objects.requireNonNull(elementTypeIdentifier);
    }

    private static Object[] resolveArguments(final Key type, final Key id, final ElementData data, final ElementBuilder builder,
            final DependencyProvider provider, final ElementSpec spec) {
        final Object[] args;
        if (spec.dataIndex == -1) {
            args = new Object[spec.parameters.size()];
            for (int i = 0; i < spec.parameters.size(); i++) {
                args[i] = processParameter(spec.parameters.get(i), data, builder, provider);
            }

            return args;
        }

        args = new Object[spec.parameters.size() + 1];
        args[spec.dataIndex] = data.provide(type, id);

        for (int i = 0; i < spec.dataIndex; i++) {
            args[i] = processParameter(spec.parameters.get(i), data, builder, provider);
        }

        for (int i = spec.dataIndex + 1; i < args.length; i++) {
            args[i] = processParameter(spec.parameters.get(i - 1), data, builder, provider);
        }

        return args;
    }

    private static Object processParameter(final ElementParameter parameter, final ElementData data,
            final ElementBuilder builder, final DependencyProvider provider) {
        if (parameter.isDependency) {
            return provider.provide(parameter.typeKey, parameter.nameKey);
        }

        return builder.build(parameter.typeKey, parameter.nameKey, data, provider);
    }

    private static Key parseKey(final KeyParser parser, final @Subst(Constants.NAMESPACE_OR_KEY) String keyString) {
        return parser.parseKey(keyString);
    }

    @Override
    public @NotNull ElementFactory<?, ?> resolveFactory(@NotNull Class<?> elementClass, boolean hasProcessor) {
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
            return (type, id, data, dependencyProvider, builder) ->
                    ReflectionUtils.invokeConstructor(finalFactoryConstructor);
        }

        final ArrayList<ElementParameter> elementParameters = new ArrayList<>(parameters.length);
        int dataParameterIndex = -1;
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];

            if (parameter.isAnnotationPresent(Data.class) ||
                    parameter.getType().isAnnotationPresent(Data.class)) {
                if (dataParameterIndex != -1) {
                    throw elementException(elementClass, "more than one ElementData on constructor factory");
                }

                if (parameter.isAnnotationPresent(Dependency.class)) {
                    throw elementException(elementClass, "ElementDependency present on data parameter");
                }

                if(parameter.isAnnotationPresent(DataName.class)) {
                    throw elementException(elementClass, "DataName present on data parameter");
                }

                dataParameterIndex = i;
                continue;
            }

            Dependency dependency = parameter.getDeclaredAnnotation(Dependency.class);
            if (dependency == null) {
                dependency = parameter.getType().getDeclaredAnnotation(Dependency.class);
            }

            if (dependency == null) {
                final Key typeKey = elementTypeIdentifier.identify(parameter.getType());
                final DataName nameAnnotation = parameter.getDeclaredAnnotation(DataName.class);

                elementParameters.add(new ElementParameter(typeKey, nameAnnotation == null ? null : parseKey(keyParser,
                        nameAnnotation.value()), false));
                continue;
            }

            final String name = dependency.name();
            elementParameters.add(new ElementParameter(parseKey(keyParser, dependency.value()),
                    name.equals(Dependency.DEFAULT_NAME) ? null : parseKey(keyParser, name), true));
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

        final ElementSpec elementSpec = new ElementSpec(elementParameters, dataParameterIndex);
        return (type, id, data, dependencyProvider, builder) -> ReflectionUtils.invokeConstructor(
                finalFactoryConstructor, resolveArguments(type, id, data, builder, dependencyProvider, elementSpec));
    }

    private record ElementSpec(List<ElementParameter> parameters, int dataIndex) {}

    private record ElementParameter(Key typeKey, Key nameKey, boolean isDependency) {}
}