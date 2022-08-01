package com.github.steanky.element.core;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * Standard implementation of {@link ElementInspector}. Uses reflection to automatically infer factories and processors
 * from methods/constructors, in compliance with the general element model specification.
 */
public class BasicElementInspector implements ElementInspector {
    private record ElementSpec(List<ElementParameter> parameters, int dataIndex) {}

    private record ElementParameter(Key typeKey, Key nameKey, DataResolver<Object, Object> resolver) {}

    private final KeyParser keyParser;

    /**
     * Creates a new instance of this class using the provided {@link KeyParser}.
     *
     * @param keyParser the KeyParser used to parse keys from strings
     */
    public BasicElementInspector(final @NotNull KeyParser keyParser) {
        this.keyParser = Objects.requireNonNull(keyParser);
    }

    private static ElementFactory<?, ?> getFactory(final Class<?> elementClass, final Method[] declaredMethods,
            final boolean hasProcessor, final KeyParser parser) {
        Method factoryMethod = null;
        for (final Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(FactoryMethod.class)) {
                if (factoryMethod != null) {
                    formatException(elementClass, "has more than one FactoryMethod");
                }

                validatePublicStatic(elementClass, declaredMethod, () -> "FactoryMethod not declared public static");
                validateReturnType(elementClass, ElementFactory.class, declaredMethod,
                        () -> "FactoryMethod must return an ElementFactory");
                validateNoParameters(elementClass, declaredMethod, () -> "FactoryMethod has parameters");

                final ParameterizedType type = validateParameterizedReturnType(elementClass, declaredMethod,
                        () -> "FactoryMethod returned raw parameterized class");
                final Type[] typeArguments = type.getActualTypeArguments();
                if(typeArguments.length != 2) {
                    //this is likely unreachable, as we are guaranteed to be an instance of ElementFactory
                    formatException(elementClass, "Unexpected number of type arguments on FactoryMethod return type");
                }

                validateGenericType(elementClass, elementClass, typeArguments[1], () -> "FactoryMethod must return a " +
                        "factory whose return type is assignable to the class it belongs");

                factoryMethod = declaredMethod;
            }
        }

        if (factoryMethod != null) {
            final ElementFactory<?, ?> factory = ReflectionUtils.invokeMethod(factoryMethod, null);
            if (factory == null) {
                formatException(elementClass, "FactoryMethod returned null");
            }

            return factory;
        }

        final Constructor<?>[] declaredConstructors = elementClass.getDeclaredConstructors();
        Constructor<?> factoryConstructor = null;
        for (final Constructor<?> declaredConstructor : declaredConstructors) {
            if (declaredConstructor.isAnnotationPresent(FactoryMethod.class)) {
                if (factoryConstructor != null) {
                    formatException(elementClass, "cannot have more than one factory constructor");
                }

                factoryConstructor = declaredConstructor;
            }
        }

        if (factoryConstructor == null) {
            formatException(elementClass, "could not find a suitable factory method or constructor");
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
                    formatException(elementClass, "more than one ElementData on constructor factory");
                }

                if(parameter.isAnnotationPresent(ElementDependency.class)) {
                    formatException(elementClass, "ElementDependency present on data parameter");
                }

                if(parameter.isAnnotationPresent(Composite.class)) {
                    formatException(elementClass, "Composite present on data parameter");
                }

                dataParameterIndex = i;
                continue;
            }

            ElementDependency dependency = parameter.getAnnotation(ElementDependency.class);
            if (dependency == null) {
                dependency = parameter.getType().getAnnotation(ElementDependency.class);
            }

            final Composite composite = parameter.getAnnotation(Composite.class);
            if(composite != null) {
                if(dependency != null) {
                    formatException(elementClass, "a parameter is annotated with both ElementDependency and Composite");
                }

                final Class<?> compositeType = parameter.getType();
                final ElementModel modelAnnotation = compositeType.getDeclaredAnnotation(ElementModel.class);
                if(modelAnnotation == null) {
                    formatException(elementClass, "Composite parameter type must have the ElementModel annotation");
                }

                @Subst(Constants.NAMESPACE_OR_KEY)
                final String name = composite.value();
                final Key compositeName = name.equals(Composite.DEFAULT_VALUE) ? null : parser.parseKey(name);

                final Method[] compositeMethods = compositeType.getDeclaredMethods();
                Method resolverMethod = null;
                for (final Method compositeMethod : compositeMethods) {
                    final ResolverMethod resolverMethodAnnotation = compositeMethod.getAnnotation(ResolverMethod.class);
                    if (resolverMethodAnnotation != null) {
                        if(resolverMethod != null) {
                            formatException(compositeType, "has more than one ResolverMethod");
                        }

                        validatePublicStatic(compositeType, compositeMethod, () -> "ResolverMethod not declared " +
                                "public static");
                        validateReturnType(compositeType, DataResolver.class, compositeMethod,
                                () -> "ResolverMethod must return a DataResolver");
                        validateNoParameters(compositeType, compositeMethod, () -> "ResolverMethod has parameters");

                        resolverMethod = compositeMethod;
                    }
                }

                if(resolverMethod == null) {
                    @Subst(Constants.NAMESPACE_OR_KEY)
                    final String type = modelAnnotation.value();
                    final Key typeKey = parser.parseKey(type);

                    elementParameters.add(new ElementParameter(typeKey, null, null));
                }
                else {
                    final DataResolver<Object, Object> resolver = ReflectionUtils.invokeMethod(resolverMethod, null);
                    elementParameters.add(new ElementParameter(null, compositeName, resolver));
                }

                continue;
            }
            else if(dependency == null) {
                formatException(elementClass, "parameter missing annotation");
            }

            @Subst(Constants.NAMESPACE_OR_KEY)
            final String value = dependency.value();

            @Subst(Constants.NAMESPACE_OR_KEY)
            final String name = dependency.name();

            elementParameters.add(new ElementParameter(parser.parseKey(value), name
                    .equals(ElementDependency.DEFAULT_NAME) ? null : parser.parseKey(name), null));
        }

        if (dataParameterIndex == -1 && hasProcessor) {
            formatException(elementClass,
                    "no data parameter found on constructor factory, but class specifies a processor");
        }

        if (dataParameterIndex != -1 && !hasProcessor) {
            formatException(elementClass,
                    "found data parameter on constructor factory, but class does not specify a processor");
        }

        elementParameters.trimToSize();

        final ElementSpec elementSpec = new ElementSpec(elementParameters, dataParameterIndex);
        return (data, dependencyProvider, builder) -> {
            final Object[] args = resolveArguments(data, dependencyProvider, builder, elementSpec);
            return ReflectionUtils.invokeConstructor(finalFactoryConstructor, args);
        };
    }

    private static Object[] resolveArguments(final Object data, final DependencyProvider provider,
            final ElementBuilder builder, final ElementSpec spec) {
        final Object[] args;
        if (spec.dataIndex == -1) {
            args = new Object[spec.parameters.size()];
            for (int i = 0; i < spec.parameters.size(); i++) {
                args[i] = processParameter(null, spec.parameters.get(i), provider, builder);
            }

            return args;
        }

        args = new Object[spec.parameters.size() + 1];
        args[spec.dataIndex] = data;
        for (int i = 0; i < spec.dataIndex; i++) {
            args[i] = processParameter(data, spec.parameters.get(i), provider, builder);
        }

        for (int i = spec.dataIndex + 1; i < args.length; i++) {
            args[i] = processParameter(data, spec.parameters.get(i - 1), provider, builder);
        }

        return args;
    }

    private static Object processParameter(final Object data, final ElementParameter parameter,
            final DependencyProvider provider, final ElementBuilder builder) {
        if(parameter.typeKey != null && parameter.nameKey == null && parameter.resolver == null) {
            //case 1: composite dependency with no data
            return builder.loadElement(parameter.typeKey, provider);
        }

        if(parameter.typeKey == null) {
            //case 2: no known composite type (must be extracted from data)
            return builder.loadElement(parameter.resolver.resolveCompositeData(data, parameter.nameKey), provider);
        }

        //case 3: dependency, not a composite type
        return provider.provide(parameter.typeKey, parameter.nameKey);
    }

    private static ConfigProcessor<?> getProcessor(final Class<?> elementClass,
            final Method[] declaredMethods) {
        Method processorMethod = null;
        for (final Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(ProcessorMethod.class)) {
                if (processorMethod != null) {
                    formatException(elementClass, "cannot have more than one ProcessorMethod");
                }

                validatePublicStatic(elementClass, declaredMethod, () -> "ProcessorMethod must be public static");
                validateNoParameters(elementClass, declaredMethod, () -> "ProcessorMethod must have no parameters");
                validateReturnType(elementClass, ConfigProcessor.class, declaredMethod, () -> "ProcessorMethod must " +
                        "return a ConfigProcessor");

                validateParameterizedReturnType(elementClass, declaredMethod,
                        () -> "ProcessorMethod cannot return a raw generic");

                processorMethod = declaredMethod;
            }
        }

        if (processorMethod != null) {
            final ConfigProcessor<?> processor = ReflectionUtils.invokeMethod(processorMethod, null);
            if (processor == null) {
                formatException(elementClass, "ProcessorMethod returned null");
            }

            return processor;
        }

        return null;
    }

    private static void validateNoParameters(final Class<?> elementClass, final Method method,
            final Supplier<String> exceptionMessage) {
        if (method.getParameterCount() != 0) {
            formatException(elementClass, exceptionMessage.get());
        }
    }

    private static void validatePublicStatic(final Class<?> elementClass, final Member member,
            final Supplier<String> exceptionMessage) {
        final int modifiers = member.getModifiers();
        if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
            formatException(elementClass, exceptionMessage.get());
        }
    }

    private static void validateReturnType(final Class<?> elementClass, final Class<?> requiredType,
            final Method method, final Supplier<String> exceptionMessage) {
        final Class<?> returnType = method.getReturnType();
        if (!requiredType.isAssignableFrom(returnType)) {
            formatException(elementClass, exceptionMessage.get());
        }
    }

    private static void validateGenericType(final Class<?> elementClass, final Class<?> requiredType,
            final Type actualType, final Supplier<String> exceptionMessage) {
        if (!requiredType.isAssignableFrom(ReflectionUtils.getUnderlyingClass(actualType))) {
            formatException(elementClass, exceptionMessage.get());
        }
    }

    private static ParameterizedType validateParameterizedReturnType(final Class<?> elementClass, final Method method,
            final Supplier<String> exceptionMessage) {
        final Type genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType)) {
            formatException(elementClass, exceptionMessage.get());
        }

        return (ParameterizedType) genericReturnType;
    }

    private static void formatException(final Class<?> elementClass, final String message) {
        throw new ElementException(elementClass + ": " + message);
    }

    @Override
    public @NotNull Information inspect(final @NotNull Class<?> elementClass) {
        if(!Modifier.isStatic(elementClass.getModifiers()) && elementClass.getDeclaringClass() != null) {
            throw new ElementException(elementClass + " is non-static and has a declaring class");
        }

        final Method[] declaredMethods = elementClass.getDeclaredMethods();
        final ConfigProcessor<?> processor = getProcessor(elementClass, declaredMethods);
        final ElementFactory<?, ?> factory = getFactory(elementClass, declaredMethods, processor != null,
                keyParser);

        return new Information(processor, factory);
    }
}