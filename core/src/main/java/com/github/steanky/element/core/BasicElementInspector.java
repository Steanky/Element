package com.github.steanky.element.core;

import com.github.steanky.element.core.annotation.ElementData;
import com.github.steanky.element.core.annotation.ElementDependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.ProcessorMethod;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Standard implementation of {@link ElementInspector}. Uses reflection to automatically infer factories and processors
 * from methods/structures, in compliance with the general element model specification.
 */
public class BasicElementInspector implements ElementInspector {
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
                    formatException(elementClass, "cannot have more than one ElementProcessor method");
                }

                validatePublicStatic(elementClass, declaredMethod, () -> "FactoryMethod must be public static");
                validateNoParameters(elementClass, declaredMethod, () -> "FactoryMethod must have no parameters");
                validateReturnType(elementClass, ElementFactory.class, declaredMethod,
                        () -> "FactoryMethod must " + "return an ElementFactory");

                final ParameterizedType type = validateParameterizedReturnType(elementClass, declaredMethod,
                        () -> "FactoryMethod cannot return a raw parameterized class");
                if (!elementClass.isAssignableFrom(
                        ReflectionUtils.getUnderlyingClass(type.getActualTypeArguments()[1]))) {
                    formatException(elementClass, "FactoryMethod must return a factory whose return type is " +
                            "assignable to the class it belongs");
                }

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
            return (keyed, dependencyProvider) -> ReflectionUtils.invokeConstructor(finalFactoryConstructor);
        }

        final ArrayList<Key> dependencyTypeKeys = new ArrayList<>(parameters.length);
        final ArrayList<Key> dependencyNameKeys = new ArrayList<>(parameters.length);
        int dataParameterIndex = -1;
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            if (parameter.isAnnotationPresent(ElementData.class) ||
                    parameter.getType().isAnnotationPresent(ElementData.class)) {
                if (dataParameterIndex != -1) {
                    formatException(elementClass, "more than one ElementData on constructor factory");
                }

                dataParameterIndex = i;
                continue;
            }

            ElementDependency dependency = parameter.getAnnotation(ElementDependency.class);
            if (dependency == null) {
                dependency = parameter.getType().getAnnotation(ElementDependency.class);
                if (dependency == null) {
                    formatException(elementClass,
                            "missing ElementDependency annotation on parameter of constructor factory");
                }
            }

            @Subst(Constants.NAMESPACE_OR_KEY)
            final String value = dependency.value();
            dependencyTypeKeys.add(parser.parseKey(value));

            @Subst(Constants.NAMESPACE_OR_KEY)
            final String name = dependency.name();
            dependencyNameKeys.add(name.equals(ElementDependency.DEFAULT_NAME) ? null : parser.parseKey(name));
        }

        if (dataParameterIndex == -1 && hasProcessor) {
            formatException(elementClass,
                    "no data parameter found on constructor factory, but class specifies a processor");
        }

        if (dataParameterIndex != -1 && !hasProcessor) {
            formatException(elementClass,
                    "found data parameter on constructor factory, but class does not specify a processor");
        }

        dependencyTypeKeys.trimToSize();
        dependencyNameKeys.trimToSize();

        final int finalDataParameterIndex = dataParameterIndex;
        return (keyed, dependencyProvider) -> {
            final Object[] args;
            if (finalDataParameterIndex == -1) {
                args = new Object[dependencyTypeKeys.size()];
                for (int i = 0; i < dependencyTypeKeys.size(); i++) {
                    args[i] = dependencyProvider.provide(dependencyTypeKeys.get(i), dependencyNameKeys.get(i));
                }

                return ReflectionUtils.invokeConstructor(finalFactoryConstructor, args);
            }

            args = new Object[dependencyTypeKeys.size() + 1];
            args[finalDataParameterIndex] = keyed;
            for (int i = 0; i < finalDataParameterIndex; i++) {
                args[i] = dependencyProvider.provide(dependencyTypeKeys.get(i), dependencyNameKeys.get(i));
            }

            for (int i = finalDataParameterIndex + 1; i < parameters.length; i++) {
                args[i] = dependencyProvider.provide(dependencyTypeKeys.get(i - 1), dependencyNameKeys.get(i - 1));
            }

            return ReflectionUtils.invokeConstructor(finalFactoryConstructor, args);
        };
    }

    private static ConfigProcessor<? extends Keyed> getProcessor(final Class<?> elementClass,
            final Method[] declaredMethods) {
        Method processorMethod = null;
        for (final Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(ProcessorMethod.class)) {
                if (processorMethod != null) {
                    formatException(elementClass, "cannot have more than one ProcessorMethod");
                }

                validatePublicStatic(elementClass, declaredMethod, () -> "ProcessorMethod must be public static");
                validateNoParameters(elementClass, declaredMethod, () -> "ProcessorMethod must have no parameters");
                validateReturnType(elementClass, ConfigProcessor.class, declaredMethod,
                        () -> "ProcessorMethod must " + "return a ConfigProcessor");

                final ParameterizedType type = validateParameterizedReturnType(elementClass, declaredMethod,
                        () -> "ProcessorMethod cannot return a raw generic");
                final Class<?> underlying = ReflectionUtils.getUnderlyingClass(type.getActualTypeArguments()[0]);
                if (!Keyed.class.isAssignableFrom(underlying)) {
                    formatException(elementClass, "ConfigProcessor returned by the ProcessorMethod must process Keyed" +
                            " or a subclass of Keyed");
                }

                processorMethod = declaredMethod;
            }
        }

        if (processorMethod != null) {
            final ConfigProcessor<? extends Keyed> processor = ReflectionUtils.invokeMethod(processorMethod, null);
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
        final Method[] declaredMethods = elementClass.getDeclaredMethods();
        final ConfigProcessor<? extends Keyed> processor = getProcessor(elementClass, declaredMethods);
        final ElementFactory<?, ?> factory = getFactory(elementClass, declaredMethods, processor != null,
                keyParser);

        return new Information(processor, factory);
    }
}