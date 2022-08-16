package com.github.steanky.element.core.factory;

import com.github.steanky.element.core.annotation.ElementData;
import com.github.steanky.element.core.annotation.ElementDependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.element.ElementFactory;
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

    /**
     * Creates a new instance of this class.
     *
     * @param keyParser the {@link KeyParser} implementation used to interpret strings as keys
     */
    public BasicFactoryResolver(@NotNull KeyParser keyParser) {
        this.keyParser = Objects.requireNonNull(keyParser);
    }

    private static Object[] resolveArguments(final Object data, final DependencyProvider provider,
            final ElementSpec spec) {
        final Object[] args;
        if (spec.dataIndex == -1) {
            args = new Object[spec.parameters.size()];
            for (int i = 0; i < spec.parameters.size(); i++) {
                args[i] = processParameter(spec.parameters.get(i), provider);
            }

            return args;
        }

        args = new Object[spec.parameters.size() + 1];
        args[spec.dataIndex] = data;
        for (int i = 0; i < spec.dataIndex; i++) {
            args[i] = processParameter(spec.parameters.get(i), provider);
        }

        for (int i = spec.dataIndex + 1; i < args.length; i++) {
            args[i] = processParameter(spec.parameters.get(i - 1), provider);
        }

        return args;
    }

    private static Object processParameter(final ElementParameter parameter, final DependencyProvider provider) {
        return provider.provide(parameter.typeKey, parameter.nameKey);
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
            return (data, dependencyProvider, builder) -> ReflectionUtils.invokeConstructor(finalFactoryConstructor);
        }

        final ArrayList<ElementParameter> elementParameters = new ArrayList<>(parameters.length);
        int dataParameterIndex = -1;
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];

            if (parameter.isAnnotationPresent(ElementData.class) ||
                    parameter.getType().isAnnotationPresent(ElementData.class)) {
                if (dataParameterIndex != -1) {
                    throw elementException(elementClass, "more than one ElementData on constructor factory");
                }

                if (parameter.isAnnotationPresent(ElementDependency.class)) {
                    throw elementException(elementClass, "ElementDependency present on data parameter");
                }

                dataParameterIndex = i;
                continue;
            }

            ElementDependency dependency = parameter.getDeclaredAnnotation(ElementDependency.class);
            if (dependency == null) {
                dependency = parameter.getType().getDeclaredAnnotation(ElementDependency.class);
            }

            if (dependency == null) {
                throw elementException(elementClass, "parameter missing annotation");
            }

            final String name = dependency.name();
            elementParameters.add(new ElementParameter(parseKey(keyParser, dependency.value()),
                    name.equals(ElementDependency.DEFAULT_NAME) ? null : parseKey(keyParser, name)));
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
        return (data, dependencyProvider, builder) -> ReflectionUtils.invokeConstructor(finalFactoryConstructor,
                resolveArguments(data, dependencyProvider, elementSpec));
    }

    private record ElementSpec(List<ElementParameter> parameters, int dataIndex) {}

    private record ElementParameter(Key typeKey, Key nameKey) {}
}