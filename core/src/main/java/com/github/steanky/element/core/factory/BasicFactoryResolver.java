package com.github.steanky.element.core.factory;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.data.DataInspector;
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
import java.util.function.Function;

import static com.github.steanky.element.core.util.Validate.*;

public class BasicFactoryResolver implements FactoryResolver {
    private record ElementSpec(List<ElementParameter> parameters, int dataIndex) {}

    private record ElementParameter(Key typeKey, Key nameKey, Function<Object, Object> resolver) {}

    private final KeyParser keyParser;
    private final DataInspector dataInspector;
    private final ElementTypeIdentifier elementTypeIdentifier;

    public BasicFactoryResolver(@NotNull KeyParser keyParser, final @NotNull DataInspector dataInspector,
            final @NotNull ElementTypeIdentifier elementTypeIdentifier) {
        this.keyParser = Objects.requireNonNull(keyParser);
        this.dataInspector = Objects.requireNonNull(dataInspector);
        this.elementTypeIdentifier = Objects.requireNonNull(elementTypeIdentifier);
    }

    @Override
    public @NotNull ElementFactory<?, ?> resolveFactory(@NotNull Class<?> elementClass, boolean hasProcessor) {
        final Method[] declaredMethods = elementClass.getDeclaredMethods();
        Method factoryMethod = null;
        for (final Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(FactoryMethod.class)) {
                if (factoryMethod != null) {
                    throw formatException(elementClass, "more than one FactoryMethod");
                }

                validatePublicStatic(elementClass, declaredMethod, () -> "FactoryMethod not declared public static");
                validateReturnType(elementClass, declaredMethod, ElementFactory.class,
                        () -> "FactoryMethod does not return an ElementFactory");
                validateNoDeclaredParameters(elementClass, declaredMethod, () -> "FactoryMethod has parameters");

                final ParameterizedType type = validateParameterizedReturnType(elementClass, declaredMethod,
                        () -> "FactoryMethod returned raw parameterized class");
                final Type[] typeArguments = type.getActualTypeArguments();
                if (typeArguments.length != 2) {
                    //this is likely unreachable, as we are guaranteed to be an instance of ElementFactory
                    throw formatException(elementClass, "Unexpected number of type arguments on FactoryMethod return type");
                }

                validateGenericType(elementClass, elementClass, typeArguments[1], () -> "FactoryMethod returned a " +
                        "factory whose constructed type is not assignable to this class");

                factoryMethod = declaredMethod;
            }
        }

        if (factoryMethod != null) {
            final ElementFactory<?, ?> factory = ReflectionUtils.invokeMethod(factoryMethod, null);
            if (factory == null) {
                throw formatException(elementClass, "FactoryMethod returned null");
            }

            return factory;
        }

        final Constructor<?>[] declaredConstructors = elementClass.getDeclaredConstructors();
        Constructor<?> factoryConstructor = null;
        for (final Constructor<?> declaredConstructor : declaredConstructors) {
            if (declaredConstructor.isAnnotationPresent(FactoryMethod.class)) {
                if (factoryConstructor != null) {
                    throw formatException(elementClass, "more than one factory constructor");
                }

                factoryConstructor = declaredConstructor;
            }
        }

        if (factoryConstructor == null) {
            throw formatException(elementClass, "no suitable factory method or constructor");
        }

        final Constructor<?> finalFactoryConstructor = factoryConstructor;
        final Parameter[] parameters = factoryConstructor.getParameters();
        if (parameters.length == 0) {
            return (data, dependencyProvider, builder) -> ReflectionUtils.invokeConstructor(finalFactoryConstructor);
        }

        final ArrayList<ElementParameter> elementParameters = new ArrayList<>(parameters.length);
        int dataParameterIndex = -1;
        Class<?> dataClass = null;
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];

            if (parameter.isAnnotationPresent(ElementData.class) ||
                    parameter.getType().isAnnotationPresent(ElementData.class)) {
                if (dataParameterIndex != -1) {
                    throw formatException(elementClass, "more than one ElementData on constructor factory");
                }

                if (parameter.isAnnotationPresent(ElementDependency.class)) {
                    throw formatException(elementClass, "ElementDependency present on data parameter");
                }

                if (parameter.isAnnotationPresent(Composite.class)) {
                    throw formatException(elementClass, "Composite present on data parameter");
                }

                dataParameterIndex = i;
                dataClass = parameter.getType();
                continue;
            }

            ElementDependency dependency = parameter.getDeclaredAnnotation(ElementDependency.class);
            if (dependency == null) {
                dependency = parameter.getType().getDeclaredAnnotation(ElementDependency.class);
            }

            final Composite composite = parameter.getDeclaredAnnotation(Composite.class);
            if (composite != null) {
                if (dependency != null) {
                    throw formatException(elementClass, "a parameter is annotated with both ElementDependency and Composite");
                }

                final Key elementType;
                try {
                    elementType = elementTypeIdentifier.identify(parameter.getType());
                }
                catch (ElementException ignored) {
                    throw formatException(elementClass, "Composite parameter used on a non-element class");
                }

                final Function<Object, Object> resolver;
                if(dataClass == null) { //we have no data, so our child should not have data either
                    resolver = ignored -> elementType;
                }
                else {
                    resolver = dataInspector.extractResolvers(dataClass).get(elementType);
                }

                elementParameters.add(new ElementParameter(null, null, resolver));
                continue;
            } else if (dependency == null) {
                throw formatException(elementClass, "parameter missing annotation");
            }

            final String name = dependency.name();
            elementParameters.add(new ElementParameter(parseKey(keyParser, dependency.value()),
                    name.equals(ElementDependency.DEFAULT_NAME) ? null : parseKey(keyParser, name), null));
        }

        if (dataParameterIndex == -1 && hasProcessor) {
            throw formatException(elementClass,
                    "no data parameter found on constructor factory, but class specifies a processor");
        }

        if (dataParameterIndex != -1 && !hasProcessor) {
            throw formatException(elementClass,
                    "found data parameter on constructor factory, but class does not specify a processor");
        }

        elementParameters.trimToSize();

        final ElementSpec elementSpec = new ElementSpec(elementParameters, dataParameterIndex);
        return (data, dependencyProvider, builder) -> {
            final Object[] args = resolveArguments(data, dependencyProvider, elementSpec, builder);
            return ReflectionUtils.invokeConstructor(finalFactoryConstructor, args);
        };
    }

    private static Object[] resolveArguments(final Object data, final DependencyProvider provider,
            final ElementSpec spec, final ElementBuilder builder) {
        final Object[] args;
        if (spec.dataIndex == -1) {
            args = new Object[spec.parameters.size()];
            for (int i = 0; i < spec.parameters.size(); i++) {
                args[i] = processParameter(spec.parameters.get(i), provider, builder, data);
            }

            return args;
        }

        args = new Object[spec.parameters.size() + 1];
        args[spec.dataIndex] = data;
        for (int i = 0; i < spec.dataIndex; i++) {
            args[i] = processParameter(spec.parameters.get(i), provider, builder, data);
        }

        for (int i = spec.dataIndex + 1; i < args.length; i++) {
            args[i] = processParameter(spec.parameters.get(i - 1), provider, builder, data);
        }

        return args;
    }

    private static Object processParameter(final ElementParameter parameter, final DependencyProvider provider,
            final ElementBuilder builder, final Object data) {
        if(parameter.resolver == null) {
            return provider.provide(parameter.typeKey, parameter.nameKey);
        }

        return builder.loadElement(parameter.resolver.apply(data), provider);
    }

    private static Key parseKey(final KeyParser parser, final @Subst(Constants.NAMESPACE_OR_KEY) String keyString) {
        return parser.parseKey(keyString);
    }
}